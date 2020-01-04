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

package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.Group;
import com.oriondev.moneywallet.model.OverviewSetting;
import com.oriondev.moneywallet.picker.CategoryPicker;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.Date;

/**
 * Created by andrea on 17/08/18.
 */
public class OverviewSettingDialog extends DialogFragment implements DateTimePicker.Controller, CategoryPicker.Controller {

    private static final String TAG_START_DATE_PICKER = "OverviewSettingDialog::Tag::StartDatePicker";
    private static final String TAG_END_DATE_PICKER = "OverviewSettingDialog::Tag::EndDatePicker";
    private static final String TAG_CATEGORY_PICKER = "OverviewSettingDialog::Tag::CategoryPicker";

    private static final String SS_OVERVIEW_SETTING = "OverviewSettingDialog::SavedState::OverviewSetting";

    public static OverviewSettingDialog newInstance() {
        return new OverviewSettingDialog();
    }

    private Callback mCallback;

    private OverviewSetting mOverviewSetting;

    private MaterialSpinner mStartDateSpinner;
    private MaterialSpinner mEndDateSpinner;
    private MaterialSpinner mGroupTypeSpinner;
    private MaterialSpinner mOverviewTypeSpinner;
    private MaterialSpinner mCashFlowSpinner;
    private MaterialSpinner mCategorySpinner;

    private DateTimePicker mStartDatePicker;
    private DateTimePicker mEndDatePicker;
    private CategoryPicker mCategoryPicker;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mOverviewSetting = savedInstanceState.getParcelable(SS_OVERVIEW_SETTING);
        }
        // create sub-pickers
        FragmentManager fragmentManager = getChildFragmentManager();
        mStartDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_START_DATE_PICKER, mOverviewSetting.getStartDate());
        mEndDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_END_DATE_PICKER, mOverviewSetting.getEndDate());
        mCategoryPicker = CategoryPicker.createPicker(fragmentManager, TAG_CATEGORY_PICKER, getFirstAvailableCategory(activity));
        // create the dialog
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_overview_setting_title)
                .customView(R.layout.dialog_overview_setting_picker, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mOverviewSetting = getCurrentOverviewSetting();
                        if (mCallback != null) {
                            mCallback.onOverviewSettingChanged(mOverviewSetting);
                        }
                    }

                })
                .show();
        View view = dialog.getCustomView();
        if (view != null) {
            // obtain the references of all the spinners
            mStartDateSpinner = view.findViewById(R.id.start_date_spinner);
            mEndDateSpinner = view.findViewById(R.id.end_date_spinner);
            mGroupTypeSpinner = view.findViewById(R.id.group_type_spinner);
            mOverviewTypeSpinner = view.findViewById(R.id.overview_type_spinner);
            mCashFlowSpinner = view.findViewById(R.id.cash_flow_spinner);
            mCategorySpinner = view.findViewById(R.id.category_spinner);
            // adjust padding for each spinner
            mStartDateSpinner.setPadding(0, mStartDateSpinner.getPaddingTop(), 0, mStartDateSpinner.getPaddingBottom());
            mEndDateSpinner.setPadding(0, mEndDateSpinner.getPaddingTop(), 0, mEndDateSpinner.getPaddingBottom());
            mGroupTypeSpinner.setPadding(0, mGroupTypeSpinner.getPaddingTop(), 0, mGroupTypeSpinner.getPaddingBottom());
            mOverviewTypeSpinner.setPadding(0, mOverviewTypeSpinner.getPaddingTop(), 0, mOverviewTypeSpinner.getPaddingBottom());
            mCashFlowSpinner.setPadding(0, mCashFlowSpinner.getPaddingTop(), 0, mCashFlowSpinner.getPaddingBottom());
            mCategorySpinner.setPadding(0, mCategorySpinner.getPaddingTop(), 0, mCategorySpinner.getPaddingBottom());
            // setup the standard spinners
            mGroupTypeSpinner.setItems(
                    getString(R.string.spinner_item_group_type_daily),
                    getString(R.string.spinner_item_group_type_weekly),
                    getString(R.string.spinner_item_group_type_monthly),
                    getString(R.string.spinner_item_group_type_yearly)
            );
            mOverviewTypeSpinner.setItems(
                    getString(R.string.spinner_item_type_cash_flow),
                    getString(R.string.spinner_item_type_category)
            );
            mCashFlowSpinner.setItems(
                    getString(R.string.spinner_item_cash_flow_incomes),
                    getString(R.string.spinner_item_cash_flow_expenses),
                    getString(R.string.spinner_item_cash_flow_net_incomes)
            );
            // now we can attach the listeners to all the spinners
            mStartDateSpinner.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mStartDatePicker.showDatePicker();
                    mStartDateSpinner.collapse();
                }

            });
            mEndDateSpinner.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mEndDatePicker.showDatePicker();
                    mEndDateSpinner.collapse();
                }

            });
            mOverviewTypeSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    mCashFlowSpinner.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                    mCategorySpinner.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                }

            });
            mCategorySpinner.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCategoryPicker.showPicker(true, true);
                    mCategorySpinner.collapse();
                }

            });
            // now use the current data to update all the views
            switch (mOverviewSetting.getGroupType()) {
                case DAILY:
                    mGroupTypeSpinner.setSelectedIndex(0);
                    break;
                case WEEKLY:
                    mGroupTypeSpinner.setSelectedIndex(1);
                    break;
                case MONTHLY:
                    mGroupTypeSpinner.setSelectedIndex(2);
                    break;
                case YEARLY:
                    mGroupTypeSpinner.setSelectedIndex(3);
                    break;
            }
            switch (mOverviewSetting.getType()) {
                case CASH_FLOW:
                    mOverviewTypeSpinner.setSelectedIndex(0);
                    mCashFlowSpinner.setVisibility(View.VISIBLE);
                    mCategorySpinner.setVisibility(View.GONE);
                    break;
                case CATEGORY:
                    mOverviewTypeSpinner.setSelectedIndex(1);
                    mCashFlowSpinner.setVisibility(View.GONE);
                    mCategorySpinner.setVisibility(View.VISIBLE);
                    break;
            }
            switch (mOverviewSetting.getCashFlow()) {
                case INCOMES:
                    mCashFlowSpinner.setSelectedIndex(0);
                    break;
                case EXPENSES:
                    mCashFlowSpinner.setSelectedIndex(1);
                    break;
                case NET_INCOMES:
                    mCashFlowSpinner.setSelectedIndex(2);
                    break;
            }
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_OVERVIEW_SETTING, getCurrentOverviewSetting());
    }

    private OverviewSetting getCurrentOverviewSetting() {
        Date startDate = mStartDatePicker.getCurrentDateTime();
        Date endDate = mEndDatePicker.getCurrentDateTime();
        Group groupType = null;
        switch (mGroupTypeSpinner.getSelectedIndex()) {
            case 0:
                groupType = Group.DAILY;
                break;
            case 1:
                groupType = Group.WEEKLY;
                break;
            case 2:
                groupType = Group.MONTHLY;
                break;
            case 3:
                groupType = Group.YEARLY;
                break;
        }
        if (mOverviewTypeSpinner.getSelectedIndex() == 0) {
            OverviewSetting.CashFlow cashFlow = null;
            switch (mCashFlowSpinner.getSelectedIndex()) {
                case 0:
                    cashFlow = OverviewSetting.CashFlow.INCOMES;
                    break;
                case 1:
                    cashFlow = OverviewSetting.CashFlow.EXPENSES;
                    break;
                case 2:
                    cashFlow = OverviewSetting.CashFlow.NET_INCOMES;
                    break;
            }
            return new OverviewSetting(startDate, endDate, groupType, cashFlow);
        } else {
            Category category = mCategoryPicker.getCurrentCategory();
            return new OverviewSetting(startDate, endDate, groupType, category != null ? category.getId() : 0L);
        }
    }

    private Category getFirstAvailableCategory(Context context) {
        // this piece of code is executed on main thread during the onCreate phase
        // and cannot be executed in background due to initialization dependencies.
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        String[] projection = new String[] {
                Contract.Category.ID,
                Contract.Category.NAME,
                Contract.Category.ICON,
                Contract.Category.TYPE
        };
        String selection = null;
        String[] selectionArgs = null;
        if (mOverviewSetting.getType() == OverviewSetting.Type.CATEGORY) {
            uri = ContentUris.withAppendedId(uri, mOverviewSetting.getCategoryId());
        } else {
            selection = Contract.Category.TYPE + " != ?";
            selectionArgs = new String[] {String.valueOf(Contract.CategoryType.SYSTEM.getValue())};
        }
        Category category = null;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                category = new Category(
                        cursor.getLong(cursor.getColumnIndex(Contract.Category.ID)),
                        cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)),
                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON))),
                        Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)))
                );
            }
            cursor.close();
        }
        return category;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, OverviewSetting overviewSetting) {
        mOverviewSetting = overviewSetting;
        show(fragmentManager, tag);
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_START_DATE_PICKER:
                mStartDateSpinner.setText(getString(R.string.spinner_item_start_date, DateFormatter.getFormattedDate(date)));
                break;
            case TAG_END_DATE_PICKER:
                mEndDateSpinner.setText(getString(R.string.spinner_item_end_date, DateFormatter.getFormattedDate(date)));
                break;
        }
    }

    @Override
    public void onCategoryChanged(String tag, Category category) {
        if (category != null) {
            mCategorySpinner.setText(category.getName());
        }
    }

    public interface Callback {

        void onOverviewSettingChanged(OverviewSetting overviewSetting);
    }
}