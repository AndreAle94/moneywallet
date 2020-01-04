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

import com.oriondev.moneywallet.model.RecurrenceSetting;
import com.oriondev.moneywallet.ui.fragment.dialog.RecurrencePickerDialog;

/**
 * Created by andrea on 07/11/18.
 */

public class RecurrencePicker extends Fragment implements RecurrencePickerDialog.Callback {

    private static final String SS_RECURRENCE_SETTING = "RecurrencePicker::SavedState::RecurrenceSetting";

    private static final String ARG_RECURRENCE_SETTING = "RecurrencePicker::Argument::RecurrenceSetting";

    private Controller mController;

    private RecurrenceSetting mRecurrenceSetting;

    private RecurrencePickerDialog mRecurrencePickerDialog;

    public static RecurrencePicker createPicker(FragmentManager fragmentManager, String tag, RecurrenceSetting recurrenceSetting) {
        RecurrencePicker recurrencePicker = (RecurrencePicker) fragmentManager.findFragmentByTag(tag);
        if (recurrencePicker == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_RECURRENCE_SETTING, recurrenceSetting);
            recurrencePicker = new RecurrencePicker();
            recurrencePicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(recurrencePicker, tag).commit();
        }
        return recurrencePicker;
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
            mRecurrenceSetting = savedInstanceState.getParcelable(SS_RECURRENCE_SETTING);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null && arguments.containsKey(ARG_RECURRENCE_SETTING)) {
                mRecurrenceSetting = arguments.getParcelable(ARG_RECURRENCE_SETTING);
            } else {
                throw new IllegalStateException("RecurrencePicker not initialized correctly. Please use RecurrencePicker.createPicker(...) instead.");
            }
        }
        mRecurrencePickerDialog = (RecurrencePickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mRecurrencePickerDialog == null) {
            mRecurrencePickerDialog = RecurrencePickerDialog.newInstance();
        }
        mRecurrencePickerDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_RECURRENCE_SETTING, mRecurrenceSetting);
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onRecurrenceSettingChanged(getTag(), mRecurrenceSetting);
        }
    }

    public RecurrenceSetting getCurrentSettings() {
        return mRecurrenceSetting;
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    public void showPicker() {
        mRecurrencePickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mRecurrenceSetting);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onRecurrenceSettingChanged(RecurrenceSetting recurrenceSetting) {
        mRecurrenceSetting = recurrenceSetting;
        fireCallbackSafely();
    }

    public interface Controller {

        void onRecurrenceSettingChanged(String tag, RecurrenceSetting recurrenceSetting);
    }
}