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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.model.Group;
import com.oriondev.moneywallet.model.OverviewSetting;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.fragment.dialog.OverviewSettingDialog;
import com.oriondev.moneywallet.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 17/08/18.
 */
public class OverviewSettingPicker extends Fragment implements OverviewSettingDialog.Callback {

    private static final String SS_OVERVIEW_SETTING = "OverviewSettingPicker::SavedState::OverviewSetting";

    private Controller mController;

    private OverviewSetting mOverviewSetting;

    private OverviewSettingDialog mOverviewSettingDialog;

    public static OverviewSettingPicker createPicker(FragmentManager fragmentManager, String tag) {
        OverviewSettingPicker placePicker = (OverviewSettingPicker) fragmentManager.findFragmentByTag(tag);
        if (placePicker == null) {
            placePicker = new OverviewSettingPicker();
            fragmentManager.beginTransaction().add(placePicker, tag).commit();
        }
        return placePicker;
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
        Calendar calendar = Calendar.getInstance();
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        Date startDate;
        Date endDate;
        OverviewSetting.Type type;
        Group groupType;
        OverviewSetting.CashFlow cashFlow;
        // check saved picker settings
        long savedStartDate = preferences.getLong("overview_start_date_millis", -1);
        long savedEndDate = preferences.getLong("overview_end_date_millis", -1);
        int savedType = preferences.getInt("overview_type", -1); //
        int savedGroupType = preferences.getInt("overview_group_type", -1);
        int savedCashFlow = preferences.getInt("overview_cash_flow", -1);
        long savedCategoryId = preferences.getLong("overview_category_id", -1);
        if (savedInstanceState != null) {
            mOverviewSetting = savedInstanceState.getParcelable(SS_OVERVIEW_SETTING);
        } else if (savedStartDate != -1 && savedEndDate != -1
                && savedType != -1) {
            calendar.setTimeInMillis(savedStartDate);
            startDate = calendar.getTime();
            calendar.setTimeInMillis(savedEndDate);
            endDate = calendar.getTime();
            groupType = Group.values()[savedGroupType];
            type = OverviewSetting.Type.values()[savedType];
            if (type == OverviewSetting.Type.CATEGORY) {
                mOverviewSetting = new OverviewSetting(startDate, endDate, groupType, savedCategoryId);
            } else { // CASH_FLOW
                cashFlow = OverviewSetting.CashFlow.values()[savedCashFlow];
                mOverviewSetting = new OverviewSetting(startDate, endDate, groupType, cashFlow);
            }
        } else {
            calendar.setFirstDayOfWeek(Calendar.SUNDAY);
            groupType = PreferenceManager.getCurrentGroupType();
            cashFlow = OverviewSetting.CashFlow.NET_INCOMES;
            switch (groupType) {
                case DAILY:
                    // we consider the current week
                    int firstDayOfWeek = PreferenceManager.getFirstDayOfWeek();
                    int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    if (currentDayOfWeek < firstDayOfWeek) {
                        calendar.add(Calendar.DAY_OF_MONTH, -7);
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
                    startDate = calendar.getTime();
                    endDate = DateUtils.addDays(calendar, 6);
                    break;
                case WEEKLY:
                    // we consider the current month
                    int firstDayOfMonth = PreferenceManager.getFirstDayOfMonth();
                    int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    if (currentDayOfMonth < firstDayOfMonth) {
                        calendar.add(Calendar.MONTH, -1);
                    }
                    calendar.set(Calendar.DAY_OF_MONTH, firstDayOfMonth);
                    startDate = calendar.getTime();
                    endDate = DateUtils.addMonthAndDay(calendar, 1, -1);
                    break;
                case MONTHLY:
                    // we consider the current year
                    int currentYear = calendar.get(Calendar.YEAR);
                    calendar.set(currentYear, Calendar.JANUARY, 1);
                    startDate = calendar.getTime();
                    calendar.set(currentYear, Calendar.DECEMBER, 31);
                    endDate = calendar.getTime();
                    break;
                case YEARLY:
                    // we consider the last three years
                    currentYear = calendar.get(Calendar.YEAR);
                    calendar.set(currentYear - 3, Calendar.JANUARY, 1);
                    startDate = calendar.getTime();
                    calendar.set(currentYear, Calendar.DECEMBER, 31);
                    endDate = calendar.getTime();
                    break;
                default:
                    startDate = calendar.getTime();
                    endDate = calendar.getTime();
                    break;
            }
            mOverviewSetting = new OverviewSetting(startDate, endDate, groupType, cashFlow);
        }
        mOverviewSettingDialog = (OverviewSettingDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mOverviewSettingDialog == null) {
            mOverviewSettingDialog = OverviewSettingDialog.newInstance();
        }
        mOverviewSettingDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_OVERVIEW_SETTING, mOverviewSetting);
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onOverviewSettingChanged(getTag(), mOverviewSetting);
        }
    }

    public OverviewSetting getCurrentSettings() {
        return mOverviewSetting;
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    public void showPicker() {
        mOverviewSettingDialog.showPicker(getChildFragmentManager(), getDialogTag(), mOverviewSetting);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onOverviewSettingChanged(OverviewSetting overviewSetting) {
        mOverviewSetting = overviewSetting;
        Calendar calendar = Calendar.getInstance();
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        calendar.setTime(mOverviewSetting.getStartDate());
        editor.putLong("overview_start_date_millis", calendar.getTimeInMillis());
        calendar.setTime(mOverviewSetting.getEndDate());
        editor.putLong("overview_end_date_millis", calendar.getTimeInMillis());
        editor.putInt("overview_group_type", overviewSetting.getGroupType().ordinal());
        OverviewSetting.Type saveType = overviewSetting.getType();
        editor.putInt("overview_type", saveType.ordinal());
        if (saveType == OverviewSetting.Type.CASH_FLOW) {
            editor.putInt("overview_cash_flow", overviewSetting.getCashFlow().ordinal());
        } else { // CATEGORY
            editor.putLong("overview_category_id", overviewSetting.getCategoryId());
        }
        editor.apply();
        fireCallbackSafely();
    }

    public interface Controller {

        void onOverviewSettingChanged(String tag, OverviewSetting overviewSetting);
    }
}