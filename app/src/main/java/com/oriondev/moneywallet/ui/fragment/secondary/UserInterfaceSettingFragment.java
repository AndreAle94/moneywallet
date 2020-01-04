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

package com.oriondev.moneywallet.ui.fragment.secondary;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Group;
import com.oriondev.moneywallet.picker.ColorPicker;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.fragment.dialog.CustomDigitSetupDialog;
import com.oriondev.moneywallet.ui.preference.ColorPreference;
import com.oriondev.moneywallet.ui.preference.ThemedListPreference;
import com.oriondev.moneywallet.ui.view.theme.ITheme;
import com.oriondev.moneywallet.ui.view.theme.ThemeEngine;
import com.oriondev.moneywallet.utils.DateFormatter;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 07/03/18.
 */
public class UserInterfaceSettingFragment extends PreferenceFragmentCompat implements ColorPicker.Controller {

    private static final String TAG_COLOR_INCOME = "UserInterfaceSettingFragment::Tag::ColorIncome";
    private static final String TAG_COLOR_EXPENSE = "UserInterfaceSettingFragment::Tag::ColorExpense";
    private static final String TAG_COLOR_PRIMARY = "UserInterfaceSettingFragment::Tag::ColorPrimary";
    private static final String TAG_COLOR_ACCENT = "UserInterfaceSettingFragment::Tag::ColorAccent";
    private static final String TAG_CUSTOM_DIGIT = "UserInterfaceSettingFragment::Tag::CustomDigit::DialogFragment";

    private static final String THEME_TYPE_LIGHT = "light";
    private static final String THEME_TYPE_DARK = "dark";
    private static final String THEME_TYPE_DEEP_DARK = "deep_dark";

    private ColorPreference mColorIncomePreference;
    private ColorPreference mColorExpensePreference;
    private Preference mCustomDigitsSetupPreference;
    private ThemedListPreference mDateFormatPreference;
    private ThemedListPreference mFirstDayWeekPreference;
    private ThemedListPreference mFirstDayMonthPreference;
    private ThemedListPreference mGroupTypePreference;
    private ColorPreference mColorPrimaryPreference;
    private ColorPreference mColorAccentPreference;
    private ThemedListPreference mThemeTypePreference;

    private ColorPicker mColorIncomePicker;
    private ColorPicker mColorExpensePicker;
    private ColorPicker mColorPrimaryPicker;
    private ColorPicker mColorAccentPicker;

    private CustomDigitSetupDialog mCustomDigitSetupDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_user_interface);
        mColorIncomePreference = (ColorPreference) findPreference("income_color");
        mColorExpensePreference = (ColorPreference) findPreference("expense_color");
        mCustomDigitsSetupPreference = findPreference("custom_digits");
        mDateFormatPreference = (ThemedListPreference) findPreference("date_format");
        mFirstDayWeekPreference = (ThemedListPreference) findPreference("first_day_week");
        mFirstDayMonthPreference = (ThemedListPreference) findPreference("first_day_month");
        mGroupTypePreference = (ThemedListPreference) findPreference("group_type");
        mColorPrimaryPreference = (ColorPreference) findPreference("theme_color_primary");
        mColorAccentPreference = (ColorPreference) findPreference("theme_color_accent");
        mThemeTypePreference = (ThemedListPreference) findPreference("theme_type");
        onCreateFragments(getChildFragmentManager());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // setup appearance preference logic
        mColorIncomePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mColorIncomePicker.showPicker();
                return false;
            }

        });
        mColorExpensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mColorExpensePicker.showPicker();
                return false;
            }

        });
        // setup date format preference: we use a dynamic version that render the current
        // datetime of the device using all the available datetime patterns to help the
        // user to decide which is better for him
        Date now = Calendar.getInstance().getTime();
        mDateFormatPreference.setEntries(new String[] {
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_0),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_1),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_2),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_3),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_4),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_5),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_6),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_7),
                DateFormatter.getFormattedDateTime(now, PreferenceManager.DATE_FORMAT_TYPE_8)
        });
        mDateFormatPreference.setEntryValues(new String[] {
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_0),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_1),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_2),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_3),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_4),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_5),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_6),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_7),
                String.valueOf(PreferenceManager.DATE_FORMAT_TYPE_8)
        });
        // setup the first day of week preference: we use the DateFormatSymbols class to obtain
        // the strings of the weekday names already localized with the current locale
        mFirstDayWeekPreference.setEntries(Arrays.copyOfRange(new DateFormatSymbols().getWeekdays(), 1, 8));
        mFirstDayWeekPreference.setEntryValues(new String[] {
                String.valueOf(Calendar.SUNDAY),
                String.valueOf(Calendar.MONDAY),
                String.valueOf(Calendar.TUESDAY),
                String.valueOf(Calendar.WEDNESDAY),
                String.valueOf(Calendar.THURSDAY),
                String.valueOf(Calendar.FRIDAY),
                String.valueOf(Calendar.SATURDAY)
        });
        // setup the first day of month preference: we set as upper bound the 28 day
        String[] daysInMonth = new String[28];
        for (int i = 0; i < daysInMonth.length; i++) {
            daysInMonth[i] = String.valueOf(i + 1);
        }
        mFirstDayMonthPreference.setEntries(daysInMonth);
        mFirstDayMonthPreference.setEntryValues(daysInMonth);
        // setup the group type preference
        mGroupTypePreference.setEntries(R.array.group_types);
        mGroupTypePreference.setEntryValues(new String[] {
                String.valueOf(PreferenceManager.GROUP_TYPE_DAILY),
                String.valueOf(PreferenceManager.GROUP_TYPE_WEEKLY),
                String.valueOf(PreferenceManager.GROUP_TYPE_MONTHLY),
                String.valueOf(PreferenceManager.GROUP_TYPE_YEARLY)
        });
        // setup theme preference logic
        mColorPrimaryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mColorPrimaryPicker.showPicker();
                return false;
            }

        });
        mColorAccentPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mColorAccentPicker.showPicker();
                return false;
            }

        });
        mThemeTypePreference.setEntries(R.array.theme_types);
        mThemeTypePreference.setEntryValues(new String[] {THEME_TYPE_LIGHT, THEME_TYPE_DARK, THEME_TYPE_DEEP_DARK});
        // setup current values
        setupCurrentDateFormat();
        setupCurrentFirstDayOfWeek();
        setupCurrentFirstDayOfMonth();
        setupCurrentGroupType();
        setupCurrentThemeType();
        // attach listeners to each preference to get notified when a value changes
        mCustomDigitsSetupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mCustomDigitSetupDialog.show(getChildFragmentManager(), TAG_CUSTOM_DIGIT);
                return false;
            }

        });
        mDateFormatPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentDateFormatIndex(index);
                setupCurrentDateFormat();
                return false;
            }

        });
        mFirstDayWeekPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int day = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentFirstDayOfWeek(day);
                setupCurrentFirstDayOfWeek();
                return false;
            }

        });
        mFirstDayMonthPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int day = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentFirstDayOfMonth(day);
                setupCurrentFirstDayOfMonth();
                return false;
            }

        });
        mGroupTypePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int groupType = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentGroupType(Group.fromType(groupType));
                setupCurrentGroupType();
                return false;
            }

        });
        mThemeTypePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                System.out.println("[THEME] onPreferenceChange: " + newValue);
                switch ((String) newValue) {
                    case THEME_TYPE_LIGHT:
                        ThemeEngine.setMode(ThemeEngine.Mode.LIGHT);
                        break;
                    case THEME_TYPE_DARK:
                        ThemeEngine.setMode(ThemeEngine.Mode.DARK);
                        break;
                    case THEME_TYPE_DEEP_DARK:
                        ThemeEngine.setMode(ThemeEngine.Mode.DEEP_DARK);
                        break;
                }
                setupCurrentThemeType();
                return false;
            }

        });
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.setPadding(0, 0, 0, 0);
        return recyclerView;
    }

    private void setupCurrentDateFormat() {
        int index = PreferenceManager.getCurrentDateFormatIndex();
        String summary = DateFormatter.getFormattedDateTime(Calendar.getInstance().getTime(), index);
        mDateFormatPreference.setValue(String.valueOf(index));
        mDateFormatPreference.setSummary(summary);
    }

    private void setupCurrentFirstDayOfWeek() {
        int index = PreferenceManager.getFirstDayOfWeek();
        String[] weekdays = new DateFormatSymbols().getWeekdays();
        mFirstDayWeekPreference.setValue(String.valueOf(index));
        mFirstDayWeekPreference.setSummary(weekdays[index]);
    }

    private void setupCurrentFirstDayOfMonth() {
        int index = PreferenceManager.getFirstDayOfMonth();
        String value = String.valueOf(index);
        mFirstDayMonthPreference.setValue(value);
        mFirstDayMonthPreference.setSummary(value);
    }

    private void setupCurrentGroupType() {
        Group groupType = PreferenceManager.getCurrentGroupType();
        mGroupTypePreference.setValue(String.valueOf(groupType.getType()));
        switch (groupType) {
            case DAILY:
                mGroupTypePreference.setSummary(R.string.setting_item_ui_group_type_daily);
                break;
            case WEEKLY:
                mGroupTypePreference.setSummary(R.string.setting_item_ui_group_type_weekly);
                break;
            case MONTHLY:
                mGroupTypePreference.setSummary(R.string.setting_item_ui_group_type_monthly);
                break;
            case YEARLY:
                mGroupTypePreference.setSummary(R.string.setting_item_ui_group_type_yearly);
                break;
        }
    }

    private void setupCurrentThemeType() {
        ITheme theme = ThemeEngine.getTheme();
        switch (theme.getMode()) {
            case LIGHT:
                mThemeTypePreference.setValue(THEME_TYPE_LIGHT);
                mThemeTypePreference.setSummary(R.string.setting_item_ui_theme_type_light);
                break;
            case DARK:
                mThemeTypePreference.setValue(THEME_TYPE_DARK);
                mThemeTypePreference.setSummary(R.string.setting_item_ui_theme_type_dark);
                break;
            case DEEP_DARK:
                mThemeTypePreference.setValue(THEME_TYPE_DEEP_DARK);
                mThemeTypePreference.setSummary(R.string.setting_item_ui_theme_type_deep_dark);
                break;
        }
    }

    private void onCreateFragments(FragmentManager fragmentManager) {
        ITheme theme = ThemeEngine.getTheme();
        mColorIncomePicker = ColorPicker.createPicker(fragmentManager, TAG_COLOR_INCOME, PreferenceManager.getCurrentIncomeColor(), false, this);
        mColorExpensePicker = ColorPicker.createPicker(fragmentManager, TAG_COLOR_EXPENSE, PreferenceManager.getCurrentExpenseColor(), false, this);
        mColorPrimaryPicker = ColorPicker.createPicker(fragmentManager, TAG_COLOR_PRIMARY, theme.getColorPrimary(), false, this);
        mColorAccentPicker = ColorPicker.createPicker(fragmentManager, TAG_COLOR_ACCENT, theme.getColorAccent(), true, this);
        mCustomDigitSetupDialog = (CustomDigitSetupDialog) fragmentManager.findFragmentByTag(TAG_CUSTOM_DIGIT);
        if (mCustomDigitSetupDialog == null) {
            mCustomDigitSetupDialog = CustomDigitSetupDialog.newInstance();
        }
    }

    @Override
    public void onColorChanged(String tag, int color, boolean autoFired) {
        switch (tag) {
            case TAG_COLOR_INCOME:
                mColorIncomePreference.setColor(color);
                if (!autoFired) {
                    PreferenceManager.setCurrentIncomeColor(color);
                }
                break;
            case TAG_COLOR_EXPENSE:
                mColorExpensePreference.setColor(color);
                if (!autoFired) {
                    PreferenceManager.setCurrentExpenseColor(color);
                }
                break;
            case TAG_COLOR_PRIMARY:
                mColorPrimaryPreference.setColor(color);
                if (!autoFired) {
                    ThemeEngine.setColorPrimary(color);
                }
                break;
            case TAG_COLOR_ACCENT:
                mColorAccentPreference.setColor(color);
                if (!autoFired) {
                    ThemeEngine.setColorAccent(color);
                }
                break;
        }
    }
}