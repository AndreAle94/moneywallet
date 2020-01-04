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

import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.fragment.dialog.BudgetTypePickerDialog;

/**
 * Created by andrea on 12/03/18.
 */
public class BudgetTypePicker extends Fragment implements BudgetTypePickerDialog.Callback {

    private static final String SS_CURRENT_TYPE = "BudgetTypePicker::SavedState::CurrentType";

    private static final String ARG_DEFAULT_TYPE = "BudgetTypePicker::Arguments::DefaultType";

    private Controller mController;

    private Contract.BudgetType mCurrentType;

    private BudgetTypePickerDialog mBudgetTypePickerDialog;

    public static BudgetTypePicker createPicker(FragmentManager fragmentManager, String tag, Contract.BudgetType defaultType) {
        BudgetTypePicker budgetTypePicker = (BudgetTypePicker) fragmentManager.findFragmentByTag(tag);
        if (budgetTypePicker == null) {
            budgetTypePicker = new BudgetTypePicker();
            Bundle arguments = new Bundle();
            arguments.putSerializable(ARG_DEFAULT_TYPE, defaultType);
            budgetTypePicker.setArguments(arguments);
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
            mCurrentType = (Contract.BudgetType) savedInstanceState.getSerializable(SS_CURRENT_TYPE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentType = (Contract.BudgetType) arguments.getSerializable(ARG_DEFAULT_TYPE);
            } else {
                mCurrentType = Contract.BudgetType.EXPENSES;
            }
        }
        mBudgetTypePickerDialog = (BudgetTypePickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mBudgetTypePickerDialog == null) {
            mBudgetTypePickerDialog = BudgetTypePickerDialog.newInstance();
        }
        mBudgetTypePickerDialog.setCallback(this);
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
            mController.onTypeChanged(getTag(), mCurrentType);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_CURRENT_TYPE, mCurrentType);
    }

    public boolean isSelected() {
        return mCurrentType != null;
    }

    public Contract.BudgetType getCurrentType() {
        return mCurrentType;
    }

    public void showPicker() {
        mBudgetTypePickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentType);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onBudgetTypeSelected(Contract.BudgetType budgetType) {
        mCurrentType = budgetType;
        fireCallbackSafely();
    }

    public interface Controller {

        void onTypeChanged(String tag, Contract.BudgetType type);
    }
}