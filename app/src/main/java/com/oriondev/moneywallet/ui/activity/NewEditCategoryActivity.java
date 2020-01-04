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
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.CategoryPicker;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.SQLiteDataException;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andrea on 02/03/18.
 */
public class NewEditCategoryActivity extends NewEditItemActivity implements IconPicker.Controller, CategoryPicker.Controller {

    public static final String TYPE = "NewEditCategoryActivity::Type";

    private static final String TAG_ICON_PICKER = "NewEditCategoryActivity::Tag::IconPicker";
    private static final String TAG_CURRENCY_PICKER = "NewEditCategoryActivity::Tag::CategoryPicker";

    private static final String SS_SYSTEM_CATEGORY = "NewEditCategoryActivity::SavedState::IsSystemCategory";

    private ImageView mIconView;
    private MaterialEditText mNameEditText;
    private MaterialEditText mParentCategoryEditText;
    private RadioButton mIncomeRadioButton;
    private RadioButton mExpenseRadioButton;
    private CheckBox mShowReportCheckBox;

    private IconPicker mIconPicker;
    private CategoryPicker mCategoryPicker;

    private boolean mIsSystemCategory;

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_icon_name_item, parent, true);
        mIconView = view.findViewById(R.id.icon_image_view);
        mNameEditText = view.findViewById(R.id.name_edit_text);
        mNameEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_name_not_valid));
        // attach a listener to the views
        mIconView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mIconPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_category, parent, true);
        mParentCategoryEditText = view.findViewById(R.id.parent_category_edit_text);
        mIncomeRadioButton = view.findViewById(R.id.income_radio_button);
        mExpenseRadioButton = view.findViewById(R.id.expense_radio_button);
        mShowReportCheckBox = view.findViewById(R.id.show_report_check_box);
        // disable edit text capabilities when not needed
        mParentCategoryEditText.setTextViewMode(true);
        // attach a click listener to the picker views
        mParentCategoryEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategoryPicker.showParentPicker(getItemId(), getCurrentCategoryType());
            }

        });
        mParentCategoryEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mCategoryPicker.setCategory(null);
                return false;
            }

        });
        mIncomeRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCategoryPicker.isSelected()) {
                    Category category = mCategoryPicker.getCurrentCategory();
                    if ((isChecked && category.getType() != Contract.CategoryType.INCOME) || (!isChecked && category.getType() != Contract.CategoryType.EXPENSE)) {
                        mCategoryPicker.setCategory(null);
                    }
                }
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        Icon icon = null;
        Category category = null;
        Contract.CategoryType type = null;
        if (savedInstanceState == null) {
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, getItemId());
                String[] projection = new String[] {
                        Contract.Category.NAME,
                        Contract.Category.ICON,
                        Contract.Category.PARENT,
                        Contract.Category.PARENT_NAME,
                        Contract.Category.PARENT_ICON,
                        Contract.Category.PARENT_TYPE,
                        Contract.Category.TYPE,
                        Contract.Category.SHOW_REPORT
                };
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mNameEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)));
                        icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON)));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Category.PARENT))) {
                            category = new Category(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Category.PARENT)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Category.PARENT_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.PARENT_ICON))),
                                    Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.PARENT_TYPE)))
                            );
                        }
                        type = Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)));
                        mShowReportCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Category.SHOW_REPORT)) == 1);
                        mIsSystemCategory = type == Contract.CategoryType.SYSTEM;
                    }
                    cursor.close();
                }
            } else if (getMode() == Mode.NEW_ITEM) {
                Intent intent = getIntent();
                type = (Contract.CategoryType) intent.getSerializableExtra(TYPE);
                if (type == null) {
                    type = Contract.CategoryType.INCOME;
                }
            }
        } else {
            mIsSystemCategory = savedInstanceState.getBoolean(SS_SYSTEM_CATEGORY);
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        mCategoryPicker = CategoryPicker.createPicker(fragmentManager, TAG_CURRENCY_PICKER, category);
        setCurrentCategoryType(type);
        // configure pickers
        mIconPicker.listenOn(mNameEditText);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_category;
            case EDIT_ITEM:
                return R.string.title_activity_edit_category;
            default:
                return -1;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SS_SYSTEM_CATEGORY, mIsSystemCategory);
    }

    private boolean validate(Mode mode) {
        if (mNameEditText.validate()) {
            Category category = mCategoryPicker.getCurrentCategory();
            if (category != null && mode == Mode.EDIT_ITEM) {
                // TODO check if the category is a parent that is going to be a child.
                // TODO this MUST NOT BE ALLOWED or we could generate a hierarchy.
            }
            return category == null || category.getType() == getCurrentCategoryType();
        }
        return false;
    }

    private void setCurrentCategoryType(Contract.CategoryType type) {
        if (mIsSystemCategory) {
            mParentCategoryEditText.setVisibility(View.GONE);
            mIncomeRadioButton.setVisibility(View.GONE);
            mExpenseRadioButton.setVisibility(View.GONE);
        } else if (type != null) {
            switch (type) {
                case INCOME:
                    mIncomeRadioButton.setChecked(true);
                    break;
                case EXPENSE:
                    mExpenseRadioButton.setChecked(true);
                    break;
            }
        }
    }

    private Contract.CategoryType getCurrentCategoryType() {
        if (mIncomeRadioButton.isChecked()) {
            return Contract.CategoryType.INCOME;
        } else {
            return Contract.CategoryType.EXPENSE;
        }
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate(mode)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Category.NAME, mNameEditText.getTextAsString());
            contentValues.put(Contract.Category.ICON, mIconPicker.getCurrentIcon().toString());
            if (!mIsSystemCategory) {
                contentValues.put(Contract.Category.PARENT, mCategoryPicker.isSelected() ? mCategoryPicker.getCurrentCategory().getId() : null);
                contentValues.put(Contract.Category.TYPE, getCurrentCategoryType().getValue());
            }
            contentValues.put(Contract.Category.SHOW_REPORT, mShowReportCheckBox.isChecked());
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    try {
                        contentResolver.insert(DataContentProvider.CONTENT_CATEGORIES, contentValues);
                        setResult(RESULT_OK);
                        finish();
                    } catch (SQLiteDataException e) {
                        // this should never happen because the ui will handle it but a bug may still be here
                        int contentRes = 0;
                        switch (e.getErrorCode()) {
                            case Contract.ErrorCode.CATEGORY_HIERARCHY_NOT_SUPPORTED:
                                contentRes = R.string.message_error_insert_category_deep_hierarchy;
                                break;
                            case Contract.ErrorCode.CATEGORY_NOT_CONSISTENT:
                                contentRes = R.string.message_error_insert_category_not_consistent;
                                break;
                        }
                        if (contentRes != 0) {
                            ThemedDialog.buildMaterialDialog(this)
                                    .title(R.string.title_error)
                                    .content(contentRes)
                                    .positiveText(android.R.string.ok)
                                    .show();
                        }
                    }
                    break;
                case EDIT_ITEM:
                    try {
                        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, getItemId());
                        contentResolver.update(uri, contentValues, null, null);
                        setResult(RESULT_OK);
                        finish();
                    } catch (SQLiteDataException e) {
                        int contentRes = 0;
                        switch (e.getErrorCode()) {
                            case Contract.ErrorCode.CATEGORY_HIERARCHY_NOT_SUPPORTED:
                                contentRes = R.string.message_error_update_category_deep_hierarchy;
                                break;
                            case Contract.ErrorCode.CATEGORY_NOT_CONSISTENT:
                                contentRes = R.string.message_error_update_category_not_consistent;
                                break;
                        }
                        if (contentRes != 0) {
                            ThemedDialog.buildMaterialDialog(this)
                                    .title(R.string.title_error)
                                    .content(contentRes)
                                    .positiveText(android.R.string.ok)
                                    .show();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mIconView);
    }

    @Override
    public void onCategoryChanged(String tag, Category category) {
        if (category != null) {
            mParentCategoryEditText.setText(category.getName());
            if (category.getType() != getCurrentCategoryType()) {
                setCurrentCategoryType(category.getType());
            }
        } else {
            mParentCategoryEditText.setText(null);
        }
    }
}