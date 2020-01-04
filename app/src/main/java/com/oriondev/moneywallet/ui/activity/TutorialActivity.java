/*
 * Copyright (c) 2018.
 *
 * This file is part of MoneyWallet.
 *
 * MoneyWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoneyWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oriondev.moneywallet.ui.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.ColorIcon;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 30/07/18.
 */
public class TutorialActivity extends AppIntro2 {

    private static final int REQUEST_NEW_WALLET = 374;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(R.drawable.ic_intro_slide_1, R.string.activity_intro_title_slide_1, R.string.activity_intro_description_slide_1, Color.parseColor("#76bec0"));
        addSlide(R.drawable.ic_intro_slide_2, R.string.activity_intro_title_slide_2, R.string.activity_intro_description_slide_2, Color.parseColor("#8464a9"));
        addSlide(R.drawable.ic_intro_slide_3, R.string.activity_intro_title_slide_3, R.string.activity_intro_description_slide_3, Color.parseColor("#19beed"));
        addSlide(R.drawable.ic_intro_slide_4, R.string.activity_intro_title_slide_4, R.string.activity_intro_description_slide_4, Color.parseColor("#47a0e1"));
        addSlide(R.drawable.ic_intro_slide_5, R.string.activity_intro_title_slide_5, R.string.activity_intro_description_slide_5, Color.parseColor("#4CAF50"));
        showStatusBar(false);
        setColorTransitionsEnabled(true);
    }

    private void addSlide(@DrawableRes int drawable, @StringRes int title, @StringRes int description, int backgroundColor) {
        SliderPage sliderPage = new SliderPage();
        sliderPage.setImageDrawable(drawable);
        sliderPage.setTitle(getString(title));
        sliderPage.setDescription(getString(description));
        sliderPage.setBgColor(backgroundColor);
        addSlide(AppIntroFragment.newInstance(sliderPage));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        getPager().setCurrentItem(getSlides().size() - 1, true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivityForResult(new Intent(this, NewEditWalletActivity.class), REQUEST_NEW_WALLET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_WALLET) {
            if (resultCode == RESULT_OK) {
                insertDefaultCategories();
                setResult(RESULT_OK);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void insertDefaultCategories() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        for (Category category : generateDefaultCategories(this)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Category.NAME, category.getName());
            contentValues.put(Contract.Category.ICON, category.getIcon().toString());
            contentValues.put(Contract.Category.TYPE, category.getType().getValue());
            contentValues.put(Contract.Category.SHOW_REPORT, true);
            contentValues.put(Contract.Category.TAG, category.getTag());
            contentResolver.insert(uri, contentValues);
        }
    }

    private static List<Category> generateDefaultCategories(Context context) {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(getDefaultCategory(context, R.string.default_category_tip, Contract.CategoryType.INCOME, "default::tip", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_prize, Contract.CategoryType.INCOME, "default::prize", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_salary, Contract.CategoryType.INCOME, "default::salary", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_interests, Contract.CategoryType.INCOME, "default::interests", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_sale, Contract.CategoryType.INCOME, "default::sale", Utils.getRandomMDColor()));
        // add expense categories
        categoryList.add(getDefaultCategory(context, R.string.default_category_car_expenses, Contract.CategoryType.EXPENSE, "default::car_expenses", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_travel, Contract.CategoryType.EXPENSE, "default::travel", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_friends, Contract.CategoryType.EXPENSE, "default::friends", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_technology, Contract.CategoryType.EXPENSE, "default::technology", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_food_drinks, Contract.CategoryType.EXPENSE, "default::food_drinks", Utils.getRandomMDColor()));
        categoryList.add(getDefaultCategory(context, R.string.default_category_hobby, Contract.CategoryType.EXPENSE, "default::hobby", Utils.getRandomMDColor()));
        return categoryList;
    }

    private static Category getDefaultCategory(Context context, int nameRes, Contract.CategoryType type, String tag, int color) {
        String name = context.getString(nameRes);
        String label = IconPicker.getColorIconString(name);
        Icon icon = new ColorIcon(Utils.getHexColor(color), label);
        return new Category(-1L, context.getString(nameRes), icon, type, tag);
    }
}