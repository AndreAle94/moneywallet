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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.ViewGroup;

import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 07/03/18.
 */

public class DateTimePicker extends Fragment implements DatePickerDialog.OnDateSetListener, BottomSheetTimePickerDialog.OnTimeSetListener {

    private static final String SS_CURRENT_DATETIME = "DateTimePicker::SavedState::CurrentDateTime";
    private static final String ARG_DEFAULT_DATETIME = "DateTimePicker::Arguments::DefaultDateTime";

    private Controller mController;

    private Calendar mDateTime;

    public static DateTimePicker createPicker(FragmentManager fragmentManager, String tag, Date defaultDate) {
        DateTimePicker dateTimePicker = (DateTimePicker) fragmentManager.findFragmentByTag(tag);
        if (dateTimePicker == null) {
            Bundle arguments = new Bundle();
            if (defaultDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(defaultDate);
                arguments.putSerializable(ARG_DEFAULT_DATETIME, calendar);
            }
            dateTimePicker = new DateTimePicker();
            dateTimePicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(dateTimePicker, tag).commit();
        }
        return dateTimePicker;
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
            mDateTime = (Calendar) savedInstanceState.getSerializable(SS_CURRENT_DATETIME);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mDateTime = (Calendar) arguments.getSerializable(ARG_DEFAULT_DATETIME);
            }
        }
        // check if a date picker or a time picker is already opened and reattach the listener
        FragmentManager fragmentManager = getChildFragmentManager();
        DatePickerDialog datePicker = (DatePickerDialog) fragmentManager.findFragmentByTag(getDatePickerTag());
        if (datePicker != null) {
            datePicker.setOnDateSetListener(this);
        }
        NumberPadTimePickerDialog timePicker = (NumberPadTimePickerDialog) fragmentManager.findFragmentByTag(getTimePickerTag());
        if (timePicker != null) {
            timePicker.setOnTimeSetListener(this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onDateTimeChanged(getTag(), getCurrentDateTime());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_CURRENT_DATETIME, mDateTime);
    }

    public boolean isSelected() {
        return mDateTime != null;
    }

    public Date getCurrentDateTime() {
        return mDateTime != null ? mDateTime.getTime() : null;
    }

    public void setCurrentDateTime(Date dateTime) {
        if (dateTime == null) {
            mDateTime = null;
        } else {
            if (mDateTime == null) {
                mDateTime = Calendar.getInstance();
                mDateTime.setTime(dateTime);
            } else {
                mDateTime.setTime(dateTime);
            }
        }
        fireCallbackSafely();
    }

    public void showDatePicker() {
        Calendar calendar = mDateTime != null ? mDateTime : Calendar.getInstance();
        ThemedDialog.buildDatePickerDialog(this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .build()
                .show(getChildFragmentManager(), getDatePickerTag());
    }

    private String getDatePickerTag() {
        return getTag() + "::DatePicker";
    }

    public void showTimePicker() {
        ThemedDialog.buildNumberPadTimePickerDialog(this, true)
                .build()
                .show(getChildFragmentManager(), getTimePickerTag());
    }

    private String getTimePickerTag() {
        return getTag() + "::TimePicker";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (mDateTime == null) {
            mDateTime = Calendar.getInstance();
        }
        mDateTime.set(year, monthOfYear, dayOfMonth);
        fireCallbackSafely();
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mDateTime.set(Calendar.MINUTE, minute);
        fireCallbackSafely();
    }

    public interface Controller {

        void onDateTimeChanged(String tag, Date date);
    }
}