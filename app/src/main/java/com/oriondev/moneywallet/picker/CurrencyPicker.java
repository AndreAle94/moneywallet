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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.ui.activity.CurrencyListActivity;
import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andrea on 02/02/18.
 */
public class CurrencyPicker extends Fragment {

    private static final String SS_CURRENT_CURRENCY = "CurrencyPicker::SavedState::CurrentCurrency";
    private static final String ARG_DEFAULT_CURRENCY = "CurrencyPicker::Arguments::DefaultCurrency";

    private static final int REQUEST_CURRENCY_PICKER = 56;

    private Controller mController;

    private CurrencyUnit mCurrentCurrency;

    public static CurrencyPicker createPicker(FragmentManager fragmentManager, String tag) {
        return createPicker(fragmentManager, tag, CurrencyManager.getDefaultCurrency());
    }

    public static CurrencyPicker createPicker(FragmentManager fragmentManager, String tag, CurrencyUnit defaultCurrency) {
        CurrencyPicker currencyPicker = (CurrencyPicker) fragmentManager.findFragmentByTag(tag);
        if (currencyPicker == null) {
            currencyPicker = new CurrencyPicker();
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_CURRENCY, defaultCurrency);
            currencyPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(currencyPicker, tag).commit();
        }
        return currencyPicker;
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
            mCurrentCurrency = savedInstanceState.getParcelable(SS_CURRENT_CURRENCY);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentCurrency = arguments.getParcelable(ARG_DEFAULT_CURRENCY);
            } else {
                mCurrentCurrency = CurrencyManager.getDefaultCurrency();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onCurrencyChanged(getTag(), mCurrentCurrency);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_CURRENCY, mCurrentCurrency);
    }

    public void setCurrency(CurrencyUnit currency) {
        mCurrentCurrency = currency;
        fireCallbackSafely();
    }

    public boolean isSelected() {
        return mCurrentCurrency != null;
    }

    public CurrencyUnit getCurrentCurrency() {
        return mCurrentCurrency;
    }

    public void showPicker() {
        Intent intent = new Intent(getActivity(), CurrencyListActivity.class);
        intent.putExtra(CurrencyListActivity.ACTIVITY_MODE, CurrencyListActivity.CURRENCY_PICKER);
        startActivityForResult(intent, REQUEST_CURRENCY_PICKER);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CURRENCY_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentCurrency = intent.getParcelableExtra(CurrencyListActivity.RESULT_CURRENCY);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public interface Controller {

        void onCurrencyChanged(String tag, CurrencyUnit currency);
    }
}