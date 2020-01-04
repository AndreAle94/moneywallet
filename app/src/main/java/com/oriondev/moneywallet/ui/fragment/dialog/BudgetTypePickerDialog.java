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

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.adapter.recycler.BudgetTypeSelectorAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 24/03/2018.
 */
public class BudgetTypePickerDialog extends DialogFragment implements BudgetTypeSelectorAdapter.Controller {

    private static final String SS_SELECTED_BUDGET_TYPE = "BudgetTypePickerDialog::SavedState::CurrentBudgetType";

    public static BudgetTypePickerDialog newInstance() {
        return new BudgetTypePickerDialog();
    }

    private Callback mCallback;
    private Contract.BudgetType mBudgetType;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mBudgetType = (Contract.BudgetType) savedInstanceState.getSerializable(SS_SELECTED_BUDGET_TYPE);
        }
        return ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_budget_type_picker_title)
                .adapter(new BudgetTypeSelectorAdapter(this), null)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_SELECTED_BUDGET_TYPE, mBudgetType);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, Contract.BudgetType budgetType) {
        mBudgetType = budgetType;
        show(fragmentManager, tag);
    }

    @Override
    public void onBudgetTypeSelected(Contract.BudgetType budgetType) {
        mCallback.onBudgetTypeSelected(budgetType);
        dismiss();
    }

    @Override
    public boolean isBudgetTypeSelected(Contract.BudgetType budgetType) {
        return mBudgetType == budgetType;
    }

    public interface Callback {

        void onBudgetTypeSelected(Contract.BudgetType budgetType);
    }
}