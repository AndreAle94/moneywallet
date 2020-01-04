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
import com.oriondev.moneywallet.ui.activity.CalculatorActivity;
import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andrea on 02/02/18.
 */
public class MoneyPicker extends Fragment {

    private static final String SS_CURRENT_CURRENCY = "MoneyPicker::SavedState::CurrentCurrency";
    private static final String SS_CURRENT_MONEY = "MoneyPicker::SavedState::CurrentMoney";
    private static final String ARG_DEFAULT_CURRENCY = "MoneyPicker::Arguments::DefaultCurrency";
    private static final String ARG_DEFAULT_MONEY = "MoneyPicker::Arguments::DefaultMoney";

    private static final int REQUEST_MONEY_PICKER = 4546;

    private Controller mController;

    private CurrencyUnit mCurrentCurrency;
    private long mCurrentMoney;

    public static MoneyPicker createPicker(FragmentManager fragmentManager, String tag, CurrencyUnit currency, long money) {
        MoneyPicker moneyPicker = (MoneyPicker) fragmentManager.findFragmentByTag(tag);
        if (moneyPicker == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_CURRENCY, currency);
            arguments.putLong(ARG_DEFAULT_MONEY, money);
            moneyPicker = new MoneyPicker();
            moneyPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(moneyPicker, tag).commit();
        }
        return moneyPicker;
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
            mCurrentMoney = savedInstanceState.getLong(SS_CURRENT_MONEY);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentCurrency = arguments.getParcelable(ARG_DEFAULT_CURRENCY);
                mCurrentMoney = arguments.getLong(ARG_DEFAULT_MONEY);
            } else {
                mCurrentCurrency = CurrencyManager.getDefaultCurrency();
                mCurrentMoney = 0L;
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
            mController.onMoneyChanged(getTag(), mCurrentCurrency, mCurrentMoney);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_CURRENCY, mCurrentCurrency);
        outState.putLong(SS_CURRENT_MONEY, mCurrentMoney);
    }

    public void setCurrency(CurrencyUnit currency) {
        mCurrentCurrency = currency;
        fireCallbackSafely();
    }

    public void setMoney(long money) {
        mCurrentMoney = money;
        fireCallbackSafely();
    }

    public void setCurrencyAndMoney(CurrencyUnit currency, long money) {
        mCurrentCurrency = currency;
        mCurrentMoney = money;
        fireCallbackSafely();
    }

    public CurrencyUnit getCurrentCurrency() {
        return mCurrentCurrency;
    }

    public long getCurrentMoney() {
        return mCurrentMoney;
    }

    public void showPicker() {
        // TODO let the user choice in preferences if start the calculator or the bottom sheet keypad
        /*
        Activity activity = getActivity();
        if (activity != null) {
            new MaterialDialog.Builder(activity)
                    .title("Enter money")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input("money", null, new MaterialDialog.InputCallback() {

                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            try {
                                mCurrentMoney = Long.parseLong(input.toString());
                                fireCallbackSafely();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                    }).show();
        }*/
        Intent intent = new Intent(getActivity(), CalculatorActivity.class);
        intent.putExtra(CalculatorActivity.ACTIVITY_MODE, CalculatorActivity.MODE_KEYPAD);
        intent.putExtra(CalculatorActivity.CURRENCY, mCurrentCurrency);
        intent.putExtra(CalculatorActivity.MONEY, mCurrentMoney);
        startActivityForResult(intent, REQUEST_MONEY_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MONEY_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentMoney = data.getLongExtra(CalculatorActivity.MONEY, mCurrentMoney);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    public interface Controller {

        void onMoneyChanged(String tag, CurrencyUnit currency, long money);
    }
}