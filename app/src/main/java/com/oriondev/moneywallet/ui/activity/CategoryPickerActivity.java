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
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelViewPagerActivity;
import com.oriondev.moneywallet.ui.adapter.pager.CategoryViewPagerAdapter;
import com.oriondev.moneywallet.ui.fragment.primary.CategoryListFragment;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andre on 17/03/2018.
 */
public class CategoryPickerActivity extends SinglePanelViewPagerActivity implements CategoryListFragment.Controller {

    public static final String SHOW_SUB_CATEGORIES = "CategoryPickerActivity::Argument::ShowSubCategories";
    public static final String SHOW_SYSTEM_CATEGORIES = "CategoryPickerActivity::Argument::ShowSystemCategories";
    public static final String RESULT_CATEGORY = "CategoryPickerActivity::Result::Category";

    @NonNull
    @Override
    protected PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager) {
        boolean showSubCategories = true;
        boolean showSystemCategories = false;
        Intent intent = getIntent();
        if (intent != null) {
            showSubCategories = intent.getBooleanExtra(SHOW_SUB_CATEGORIES, true);
            showSystemCategories = intent.getBooleanExtra(SHOW_SYSTEM_CATEGORIES, false);
        }
        return new CategoryViewPagerAdapter(fragmentManager, this, showSubCategories, showSystemCategories);
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreatePanelView(inflater, parent, savedInstanceState);
        if (savedInstanceState == null) {
            setViewPagerPosition(1);
        }
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_category_picker;
    }

    @Override
    protected void onFloatingActionButtonClick() {
        Intent intent = new Intent(this, NewEditCategoryActivity.class);
        intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.NEW_ITEM);
        switch (getViewPagerPosition()) {
            case 0:
            case 2:
                intent.putExtra(NewEditCategoryActivity.TYPE, Contract.CategoryType.INCOME);
                break;
            case 1:
                intent.putExtra(NewEditCategoryActivity.TYPE, Contract.CategoryType.EXPENSE);
                break;
        }
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(long id) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, id);
        String[] projection = new String[] {
                Contract.Category.ID,
                Contract.Category.NAME,
                Contract.Category.ICON,
                Contract.Category.TYPE
        };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            Intent intent = new Intent();
            Category category = null;
            if (cursor.moveToFirst()) {
                category = new Category(
                        cursor.getLong(cursor.getColumnIndex(Contract.Category.ID)),
                        cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)),
                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON))),
                        Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)))
                );
            }
            cursor.close();
            intent.putExtra(RESULT_CATEGORY, category);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}