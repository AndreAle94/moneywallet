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

import com.oriondev.moneywallet.model.DataFormat;
import com.oriondev.moneywallet.ui.fragment.dialog.DataFormatPickerDialog;

/**
 * Created by andrea on 12/03/18.
 */
public class ImportExportFormatPicker extends Fragment implements DataFormatPickerDialog.Callback {

    private static final String SS_CURRENT_FORMAT = "ImportExportFormatPicker::SavedState::CurrentFormat";

    private Controller mController;

    private DataFormat mCurrentFormat;

    private DataFormatPickerDialog mDataFormatPickerDialog;

    public static ImportExportFormatPicker createPicker(FragmentManager fragmentManager, String tag) {
        ImportExportFormatPicker budgetTypePicker = (ImportExportFormatPicker) fragmentManager.findFragmentByTag(tag);
        if (budgetTypePicker == null) {
            budgetTypePicker = new ImportExportFormatPicker();
            fragmentManager.beginTransaction().add(budgetTypePicker, tag).commit();
        }
        return budgetTypePicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentFormat = (DataFormat) savedInstanceState.getSerializable(SS_CURRENT_FORMAT);
        } else {
            mCurrentFormat = null;
        }
        mDataFormatPickerDialog = (DataFormatPickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mDataFormatPickerDialog == null) {
            mDataFormatPickerDialog = DataFormatPickerDialog.newInstance();
        }
        mDataFormatPickerDialog.setCallback(this);
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onFormatChanged(getTag(), mCurrentFormat);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_CURRENT_FORMAT, mCurrentFormat);
    }

    public boolean isSelected() {
        return mCurrentFormat != null;
    }

    public void setCurrentFormat(DataFormat dataFormat) {
        mCurrentFormat = dataFormat;
        fireCallbackSafely();
    }

    public DataFormat getCurrentFormat() {
        return mCurrentFormat;
    }

    public void showPicker(DataFormat[] dataFormats) {
        int index = -1;
        for (int i = 0; i < dataFormats.length; i++) {
            if (dataFormats[i] == mCurrentFormat) {
                index = i;
            }
        }
        mDataFormatPickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), dataFormats, index);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onDataFormatSelected(DataFormat dataFormat) {
        mCurrentFormat = dataFormat;
        fireCallbackSafely();
    }

    public interface Controller {

        void onFormatChanged(String tag, DataFormat format);
    }
}