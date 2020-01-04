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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.RecurrenceSetting;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.DateFormatter;

import java.util.Date;

/**
 * Created by andrea on 07/11/18.
 */
public class RecurrencePickerDialog extends DialogFragment implements DateTimePicker.Controller {

    private static final String TAG_START_DATE_PICKER = "RecurrencePickerDialog::Tag::StartDatePicker";
    private static final String TAG_END_DATE_PICKER = "RecurrencePickerDialog::Tag::EndDatePicker";

    private static final String SS_RECURRENCE_SETTING = "RecurrencePickerDialog::SavedState::RecurrenceSetting";

    public static RecurrencePickerDialog newInstance() {
        return new RecurrencePickerDialog();
    }

    private Callback mCallback;

    private RecurrenceSetting mRecurrenceSetting;

    private MaterialSpinner mRecurrenceTypeSpinner;
    private MaterialSpinner mRecurrenceStartDateSpinner;
    private EditText mRecurrenceEveryNumberEditText;
    private TextView mRecurrenceEveryItemTextView;
    private LinearLayout mRecurrenceTypeWeeklyLayout;
    private CheckBox mRecurrenceTypeWeeklySundayRadioButton;
    private CheckBox mRecurrenceTypeWeeklyMondayRadioButton;
    private CheckBox mRecurrenceTypeWeeklyTuesdayRadioButton;
    private CheckBox mRecurrenceTypeWeeklyWednesdayRadioButton;
    private CheckBox mRecurrenceTypeWeeklyThursdayRadioButton;
    private CheckBox mRecurrenceTypeWeeklyFridayRadioButton;
    private CheckBox mRecurrenceTypeWeeklySaturdayRadioButton;
    private LinearLayout mRecurrenceTypeMonthlyLayout;
    private RadioButton mRecurrenceTypeMonthlySameDayRadioButton;
    private MaterialSpinner mRecurrenceEndTypeSpinner;
    private LinearLayout mRecurrenceTimesLayout;
    private EditText mRecurrenceTimesNumberEditText;
    private MaterialSpinner mRecurrenceEndDateSpinner;

    private DateTimePicker mStartDatePicker;
    private DateTimePicker mEndDatePicker;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mRecurrenceSetting = savedInstanceState.getParcelable(SS_RECURRENCE_SETTING);
        }
        // create the dialog
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_recurrence_title)
                .customView(R.layout.dialog_recurrence_setting_picker, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mRecurrenceSetting = getCurrentRecurrenceSetting();
                        if (mCallback != null) {
                            mCallback.onRecurrenceSettingChanged(mRecurrenceSetting);
                        }
                    }

                })
                .show();
        View view = dialog.getCustomView();
        if (view != null) {
            mRecurrenceTypeSpinner = view.findViewById(R.id.type_spinner);
            mRecurrenceStartDateSpinner = view.findViewById(R.id.start_date_spinner);
            mRecurrenceEveryNumberEditText = view.findViewById(R.id.every_number_edit_text);
            mRecurrenceEveryItemTextView = view.findViewById(R.id.every_item_text_view);
            mRecurrenceTypeWeeklyLayout = view.findViewById(R.id.type_weekly_layout);
            mRecurrenceTypeWeeklySundayRadioButton = view.findViewById(R.id.type_weekly_sunday);
            mRecurrenceTypeWeeklyMondayRadioButton = view.findViewById(R.id.type_weekly_monday);
            mRecurrenceTypeWeeklyTuesdayRadioButton = view.findViewById(R.id.type_weekly_tuesday);
            mRecurrenceTypeWeeklyWednesdayRadioButton = view.findViewById(R.id.type_weekly_wednesday);
            mRecurrenceTypeWeeklyThursdayRadioButton = view.findViewById(R.id.type_weekly_thursday);
            mRecurrenceTypeWeeklyFridayRadioButton = view.findViewById(R.id.type_weekly_friday);
            mRecurrenceTypeWeeklySaturdayRadioButton = view.findViewById(R.id.type_weekly_saturday);
            mRecurrenceTypeMonthlyLayout = view.findViewById(R.id.type_monthly_layout);
            mRecurrenceTypeMonthlySameDayRadioButton = view.findViewById(R.id.type_monthly_repeat_same_day);
            mRecurrenceEndTypeSpinner = view.findViewById(R.id.end_type_spinner);
            mRecurrenceTimesLayout = view.findViewById(R.id.end_type_for_layout);
            mRecurrenceTimesNumberEditText = view.findViewById(R.id.end_type_for_edit_text);
            mRecurrenceEndDateSpinner = view.findViewById(R.id.end_date_spinner);
            // setup margins
            mRecurrenceTypeSpinner.setPadding(0, mRecurrenceTypeSpinner.getPaddingTop(), 0, mRecurrenceTypeSpinner.getPaddingBottom());
            mRecurrenceStartDateSpinner.setPadding(0, mRecurrenceStartDateSpinner.getPaddingTop(), 0, mRecurrenceStartDateSpinner.getPaddingBottom());
            mRecurrenceEndTypeSpinner.setPadding(0, mRecurrenceEndTypeSpinner.getPaddingTop(), 0, mRecurrenceEndTypeSpinner.getPaddingBottom());
            mRecurrenceEndDateSpinner.setPadding(0, mRecurrenceEndDateSpinner.getPaddingTop(), 0, mRecurrenceEndDateSpinner.getPaddingBottom());
            // configure spinners
            mRecurrenceTypeSpinner.setItems(
                    getString(R.string.recurrence_type_daily),
                    getString(R.string.recurrence_type_weekly),
                    getString(R.string.recurrence_type_monthly),
                    getString(R.string.recurrence_type_yearly)
            );
            mRecurrenceEndTypeSpinner.setItems(
                    getString(R.string.recurrence_end_type_forever),
                    getString(R.string.recurrence_end_type_until),
                    getString(R.string.recurrence_end_type_for)
            );
            mRecurrenceTypeSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    switch (position) {
                        case 0:
                            updateRecurrenceType(RecurrenceSetting.TYPE_DAILY, true);
                            break;
                        case 1:
                            updateRecurrenceType(RecurrenceSetting.TYPE_WEEKLY, true);
                            break;
                        case 2:
                            updateRecurrenceType(RecurrenceSetting.TYPE_MONTHLY, true);
                            break;
                        case 3:
                            updateRecurrenceType(RecurrenceSetting.TYPE_YEARLY, true);
                            break;
                    }
                }

            });
            mRecurrenceStartDateSpinner.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mStartDatePicker.showDatePicker();
                    mRecurrenceStartDateSpinner.collapse();
                }

            });
            mRecurrenceTypeWeeklySundayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklyMondayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklyTuesdayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklyWednesdayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklyThursdayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklyFridayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceTypeWeeklySaturdayRadioButton.setOnCheckedChangeListener(mWeekdayChangeListener);
            mRecurrenceEndTypeSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    switch (position) {
                        case 0:
                            updateRecurrenceEndType(RecurrenceSetting.END_FOREVER, true);
                            break;
                        case 1:
                            updateRecurrenceEndType(RecurrenceSetting.END_UNTIL, true);
                            break;
                        case 2:
                            updateRecurrenceEndType(RecurrenceSetting.END_FOR, true);
                            break;
                    }
                }

            });
            mRecurrenceEndDateSpinner.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mEndDatePicker.showDatePicker();
                    mRecurrenceEndDateSpinner.collapse();
                }

            });
        }
        // create sub-pickers
        FragmentManager fragmentManager = getChildFragmentManager();
        mStartDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_START_DATE_PICKER, mRecurrenceSetting.getStartDate());
        mEndDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_END_DATE_PICKER, mRecurrenceSetting.getEndDate());
        // update the whole ui using the recurrence object
        updateRecurrenceType(mRecurrenceSetting.getType(), false);
        updateRecurrenceOffsetValue(mRecurrenceSetting.getOffsetValue());
        updateRecurrenceWeekDays(mRecurrenceSetting.getWeekDays());
        updateRecurrenceMonthDay(mRecurrenceSetting.getMonthDay());
        updateRecurrenceEndType(mRecurrenceSetting.getEndType(), false);
        updateRecurrenceOccurrenceValue(mRecurrenceSetting.getOccurrenceValue());
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_RECURRENCE_SETTING, getCurrentRecurrenceSetting());
    }

    private void updateRecurrenceType(int type, boolean self) {
        switch (type) {
            case RecurrenceSetting.TYPE_DAILY:
                mRecurrenceEveryItemTextView.setText(R.string.recurrence_hint_days);
                break;
            case RecurrenceSetting.TYPE_WEEKLY:
                mRecurrenceEveryItemTextView.setText(R.string.recurrence_hint_weeks);
                break;
            case RecurrenceSetting.TYPE_MONTHLY:
                mRecurrenceEveryItemTextView.setText(R.string.recurrence_hint_months);
                break;
            case RecurrenceSetting.TYPE_YEARLY:
                mRecurrenceEveryItemTextView.setText(R.string.recurrence_hint_years);
                break;
        }
        mRecurrenceTypeWeeklyLayout.setVisibility(type == RecurrenceSetting.TYPE_WEEKLY ? View.VISIBLE : View.GONE);
        mRecurrenceTypeMonthlyLayout.setVisibility(type == RecurrenceSetting.TYPE_MONTHLY ? View.VISIBLE : View.GONE);
        if (!self) {
            switch (type) {
                case RecurrenceSetting.TYPE_DAILY:
                    mRecurrenceTypeSpinner.setSelectedIndex(0);
                    break;
                case RecurrenceSetting.TYPE_WEEKLY:
                    mRecurrenceTypeSpinner.setSelectedIndex(1);
                    break;
                case RecurrenceSetting.TYPE_MONTHLY:
                    mRecurrenceTypeSpinner.setSelectedIndex(2);
                    break;
                case RecurrenceSetting.TYPE_YEARLY:
                    mRecurrenceTypeSpinner.setSelectedIndex(3);
                    break;
            }
        }
    }

    private void updateRecurrenceOffsetValue(int offset) {
        mRecurrenceEveryNumberEditText.setText(String.valueOf(offset));
    }

    private void updateRecurrenceWeekDays(boolean[] days) {
        mRecurrenceTypeWeeklySundayRadioButton.setChecked(days[0]);
        mRecurrenceTypeWeeklyMondayRadioButton.setChecked(days[1]);
        mRecurrenceTypeWeeklyTuesdayRadioButton.setChecked(days[2]);
        mRecurrenceTypeWeeklyWednesdayRadioButton.setChecked(days[3]);
        mRecurrenceTypeWeeklyThursdayRadioButton.setChecked(days[4]);
        mRecurrenceTypeWeeklyFridayRadioButton.setChecked(days[5]);
        mRecurrenceTypeWeeklySaturdayRadioButton.setChecked(days[6]);
    }

    private void updateRecurrenceMonthDay(int flag) {
        mRecurrenceTypeMonthlySameDayRadioButton.setChecked(flag == RecurrenceSetting.FLAG_MONTHLY_SAME_DAY);
    }

    private void updateRecurrenceEndType(int type, boolean self) {
        mRecurrenceEndDateSpinner.setVisibility(type == RecurrenceSetting.END_UNTIL ? View.VISIBLE : View.GONE);
        mRecurrenceTimesLayout.setVisibility(type == RecurrenceSetting.END_FOR ? View.VISIBLE : View.GONE);
        if (!self) {
            switch (type) {
                case RecurrenceSetting.END_FOREVER:
                    mRecurrenceEndTypeSpinner.setSelectedIndex(0);
                    break;
                case RecurrenceSetting.END_UNTIL:
                    mRecurrenceEndTypeSpinner.setSelectedIndex(1);
                    break;
                case RecurrenceSetting.END_FOR:
                    mRecurrenceEndTypeSpinner.setSelectedIndex(2);
                    break;
            }
        }
    }

    private void updateRecurrenceOccurrenceValue(int occurrences) {
        mRecurrenceTimesNumberEditText.setText(String.valueOf(occurrences));
    }

    private int getCurrentRecurrenceType() {
        switch (mRecurrenceTypeSpinner.getSelectedIndex()) {
            case 0:
                return RecurrenceSetting.TYPE_DAILY;
            case 1:
                return RecurrenceSetting.TYPE_WEEKLY;
            case 2:
                return RecurrenceSetting.TYPE_MONTHLY;
            case 3:
                return RecurrenceSetting.TYPE_YEARLY;
        }
        return RecurrenceSetting.TYPE_DAILY;
    }

    private int getCurrentRecurrenceOffset() {
        try {
            return Integer.parseInt(mRecurrenceEveryNumberEditText.getText().toString());
        } catch (NumberFormatException ignore) {}
        return 1;
    }

    private boolean[] getCurrentRecurrenceWeekDays() {
        return new boolean[] {
                mRecurrenceTypeWeeklySundayRadioButton.isChecked(),
                mRecurrenceTypeWeeklyMondayRadioButton.isChecked(),
                mRecurrenceTypeWeeklyTuesdayRadioButton.isChecked(),
                mRecurrenceTypeWeeklyWednesdayRadioButton.isChecked(),
                mRecurrenceTypeWeeklyThursdayRadioButton.isChecked(),
                mRecurrenceTypeWeeklyFridayRadioButton.isChecked(),
                mRecurrenceTypeWeeklySaturdayRadioButton.isChecked()
        };
    }

    private int getCurrentRecurrenceEndType() {
        switch (mRecurrenceEndTypeSpinner.getSelectedIndex()) {
            case 0:
                return RecurrenceSetting.END_FOREVER;
            case 1:
                return RecurrenceSetting.END_UNTIL;
            case 2:
                return RecurrenceSetting.END_FOR;
        }
        return RecurrenceSetting.END_FOREVER;
    }

    private int getCurrentRecurrenceEndOccurrences() {
        try {
            return Integer.parseInt(mRecurrenceTimesNumberEditText.getText().toString());
        } catch (NumberFormatException ignore) {}
        return 1;
    }

    public RecurrenceSetting getCurrentRecurrenceSetting() {
        Date startDate = mStartDatePicker.getCurrentDateTime();
        int type = getCurrentRecurrenceType();
        RecurrenceSetting.Builder builder = new RecurrenceSetting.Builder(startDate, type);
        builder.setOffset(getCurrentRecurrenceOffset());
        if (type == RecurrenceSetting.TYPE_WEEKLY) {
            builder.setRepeatWeekDay(getCurrentRecurrenceWeekDays());
        } else if (type == RecurrenceSetting.TYPE_MONTHLY) {
            builder.setRepeatSameMonthDay();
        }
        switch (getCurrentRecurrenceEndType()) {
            case RecurrenceSetting.END_UNTIL:
                builder.setEndUntil(mEndDatePicker.getCurrentDateTime());
                break;
            case RecurrenceSetting.END_FOR:
                builder.setEndFor(getCurrentRecurrenceEndOccurrences());
                break;
        }
        return builder.build();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, RecurrenceSetting recurrenceSetting) {
        mRecurrenceSetting = recurrenceSetting;
        show(fragmentManager, tag);
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_START_DATE_PICKER:
                mRecurrenceStartDateSpinner.setText(DateFormatter.getFormattedDate(date));
                break;
            case TAG_END_DATE_PICKER:
                mRecurrenceEndDateSpinner.setText(DateFormatter.getFormattedDate(date));
                break;
        }
    }

    private CompoundButton.OnCheckedChangeListener mWeekdayChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!mRecurrenceTypeWeeklySundayRadioButton.isChecked() && !mRecurrenceTypeWeeklyMondayRadioButton.isChecked() && !mRecurrenceTypeWeeklyTuesdayRadioButton.isChecked() && !mRecurrenceTypeWeeklyWednesdayRadioButton.isChecked() && !mRecurrenceTypeWeeklyThursdayRadioButton.isChecked() && !mRecurrenceTypeWeeklyFridayRadioButton.isChecked() && !mRecurrenceTypeWeeklySaturdayRadioButton.isChecked()) {
                // if no checkbox is checked, force this checkbox to not be unchecked
                buttonView.setChecked(true);
            }
        }

    };

    public interface Callback {

        void onRecurrenceSettingChanged(RecurrenceSetting recurrenceSetting);
    }
}