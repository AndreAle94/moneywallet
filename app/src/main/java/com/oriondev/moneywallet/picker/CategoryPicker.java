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

package com.oriondev.moneywallet.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.activity.CategoryPickerActivity;
import com.oriondev.moneywallet.ui.fragment.dialog.ParentCategoryPickerDialog;

/**
 * Created by andrea on 02/02/18.
 */
public class CategoryPicker extends Fragment implements ParentCategoryPickerDialog.Callback {

    private static final String SS_CURRENT_CATEGORY = "ParentCategoryPicker::SavedState::CurrentCategory";
    private static final String ARG_DEFAULT_CATEGORY = "ParentCategoryPicker::Arguments::DefaultCategory";

    private static final int REQUEST_CATEGORY_PICKER = 1;

    private Controller mController;

    private Category mCurrentCategory;

    private ParentCategoryPickerDialog mParentCategoryPickerDialog;

    public static CategoryPicker createPicker(FragmentManager fragmentManager, String tag, Category defaultCategory) {
        CategoryPicker categoryPicker = (CategoryPicker) fragmentManager.findFragmentByTag(tag);
        if (categoryPicker == null) {
            categoryPicker = new CategoryPicker();
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_CATEGORY, defaultCategory);
            categoryPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(categoryPicker, tag).commit();
        }
        return categoryPicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        } else if (getParentFragment() instanceof Controller) {
            mController = (Controller) getParentFragment();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentCategory = savedInstanceState.getParcelable(SS_CURRENT_CATEGORY);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentCategory = arguments.getParcelable(ARG_DEFAULT_CATEGORY);
            } else {
                mCurrentCategory = null;
            }
        }
        mParentCategoryPickerDialog = (ParentCategoryPickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mParentCategoryPickerDialog == null) {
            mParentCategoryPickerDialog = ParentCategoryPickerDialog.newInstance();
        }
        mParentCategoryPickerDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onCategoryChanged(getTag(), mCurrentCategory);
        }
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_CATEGORY, mCurrentCategory);
    }

    public boolean isSelected() {
        return mCurrentCategory != null;
    }

    public void setCategory(Category category) {
        mCurrentCategory = category;
        fireCallbackSafely();
    }

    public Category getCurrentCategory() {
        return mCurrentCategory;
    }

    public void showPicker() {
        showPicker(true, false);
    }

    public void showPicker(boolean showSubCategories, boolean showSystemCategories) {
        Intent intent = new Intent(getActivity(), CategoryPickerActivity.class);
        intent.putExtra(CategoryPickerActivity.SHOW_SUB_CATEGORIES, showSubCategories);
        intent.putExtra(CategoryPickerActivity.SHOW_SYSTEM_CATEGORIES, showSystemCategories);
        startActivityForResult(intent, REQUEST_CATEGORY_PICKER);
    }

    public void showParentPicker(long categoryId, Contract.CategoryType type) {
        mParentCategoryPickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentCategory, categoryId, type);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CATEGORY_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentCategory = data.getParcelableExtra(CategoryPickerActivity.RESULT_CATEGORY);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onCategorySelected(Category category) {
        mCurrentCategory = category;
        fireCallbackSafely();
    }

    public interface Controller {

        void onCategoryChanged(String tag, Category category);
    }
}