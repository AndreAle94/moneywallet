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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.ExchangeRate;
import com.oriondev.moneywallet.service.AbstractCurrencyRateDownloadIntentService;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andre on 19/03/2018.
 */
public class CurrencyConverterDialog extends DialogFragment {

    private static final String SS_CURRENCY_1 = "CurrencyConverterDialog::SavedState::Currency1";
    private static final String SS_CURRENCY_2 = "CurrencyConverterDialog::SavedState::Currency2";
    private static final String SS_RATE = "CurrencyConverterDialog::SavedState::Rate";

    public static CurrencyConverterDialog newInstance() {
        return new CurrencyConverterDialog();
    }

    private Callback mCallback;

    private CurrencyUnit mCurrency1;
    private CurrencyUnit mCurrency2;
    private double mExchangeRate;

    private EditText mExchangeRateEditText;
    private ImageView mRefreshButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter(LocalAction.ACTION_EXCHANGE_RATES_UPDATED);
        broadcastManager.registerReceiver(mExchangeRateUpdateBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
            broadcastManager.unregisterReceiver(mExchangeRateUpdateBroadcastReceiver);
        }
        super.onDetach();
    }

    private BroadcastReceiver mExchangeRateUpdateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocalAction.ACTION_EXCHANGE_RATES_UPDATED.equals(intent.getAction())) {
                ExchangeRate exchangeRate = CurrencyManager.getExchangeRate(mCurrency1, mCurrency2);
                if (exchangeRate != null) {
                    mExchangeRate = exchangeRate.getRate();
                    updateDisplay();
                    mRefreshButton.setClickable(true);
                }
            }
        }

    };

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mCurrency1 = savedInstanceState.getParcelable(SS_CURRENCY_1);
            mCurrency2 = savedInstanceState.getParcelable(SS_CURRENCY_2);
            mExchangeRate = savedInstanceState.getDouble(SS_RATE);
        }
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.title_currency_exchange_rate)
                .customView(R.layout.dialog_currency_exchange_rate, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            mExchangeRate = Double.parseDouble(mExchangeRateEditText.getText().toString());
                        } catch (NumberFormatException ignore) {}
                        if (mCallback != null) {
                            mCallback.onExchangeRateChanged(mExchangeRate);
                        }
                    }

                })
                .build();
        View customView = dialog.getCustomView();
        if (customView != null) {
            mExchangeRateEditText = customView.findViewById(R.id.exchange_rate_edit_text);
            mRefreshButton = customView.findViewById(R.id.refresh_button);
            updateDisplay();
            mRefreshButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    Intent intent = AbstractCurrencyRateDownloadIntentService.buildIntent(activity);
                    activity.startService(intent);
                    // temporary disable the refresh button until the load is completed
                    // to avoid launching multiple serial services
                    mRefreshButton.setClickable(false);
                }

            });
        }
        return dialog;
    }

    private void updateDisplay() {
        mExchangeRateEditText.setText(String.valueOf(mExchangeRate));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENCY_1, mCurrency1);
        outState.putParcelable(SS_CURRENCY_2, mCurrency2);
        outState.putDouble(SS_RATE, mExchangeRate);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, CurrencyUnit currency1, CurrencyUnit currency2, double exchangeRate) {
        mCurrency1 = currency1;
        mCurrency2 = currency2;
        mExchangeRate = exchangeRate;
        show(fragmentManager, tag);
    }

    public interface Callback {

        void onExchangeRateChanged(double exchangeRate);
    }
}