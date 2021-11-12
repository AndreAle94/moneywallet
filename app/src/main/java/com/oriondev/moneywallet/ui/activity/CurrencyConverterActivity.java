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

package com.oriondev.moneywallet.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.ExchangeRate;
import com.oriondev.moneywallet.picker.CurrencyPicker;
import com.oriondev.moneywallet.service.AbstractCurrencyRateDownloadIntentService;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.math.BigDecimal;

/**
 * Created by andre on 23/03/2018.
 */
public class CurrencyConverterActivity extends SinglePanelActivity implements View.OnClickListener, CurrencyPicker.Controller {

    private static final String TAG_CURRENCY_FROM_PICKER = "CurrencyConverterActivity::Tag::CurrencyFrom";
    private static final String TAG_CURRENCY_TO_PICKER = "CurrencyConverterActivity::Tag::CurrencyTo";
    private static final String SS_MONEY = "CurrencyConverterActivity::SavedState::money";

    private static final String EUR = "EUR";
    private static final String USD = "USD";

    private ImageView mImageCurrencyFrom;
    private ImageView mImageCurrencyTo;
    private TextView mTextCurrencyFrom;
    private TextView mTextCurrencyTo;
    private TextView mTextMoneyFrom;
    private TextView mTextMoneyTo;

    private CurrencyPicker mCurrencyFromPicker;
    private CurrencyPicker mCurrencyToPicker;

    private MoneyFormatter mMoneyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter(LocalAction.ACTION_EXCHANGE_RATES_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        mMoneyFormatter = MoneyFormatter.getInstance();
        mMoneyFormatter.setShowSymbolEnabled(false);
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_currency_converter;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_rate:
                startService(AbstractCurrencyRateDownloadIntentService.buildIntent(this));
                break;
        }
        return false;
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_currency_converter, parent, true);
        mImageCurrencyFrom = view.findViewById(R.id.image_currency_from);
        mImageCurrencyTo = view.findViewById(R.id.image_currency_to);
        mTextCurrencyFrom = view.findViewById(R.id.text_currency_from);
        mTextCurrencyTo = view.findViewById(R.id.text_currency_to);
        mTextMoneyFrom = view.findViewById(R.id.money_currency_from);
        mTextMoneyTo = view.findViewById(R.id.money_currency_to);
        findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CurrencyUnit temp = mCurrencyFromPicker.getCurrentCurrency();
                mCurrencyFromPicker.setCurrency(mCurrencyToPicker.getCurrentCurrency());
                mCurrencyToPicker.setCurrency(temp);
                makeConversion(mTextMoneyFrom.getText().toString());
            }

        });
        findViewById(R.id.keyboard_button_0).setOnClickListener(this);
        findViewById(R.id.keyboard_button_1).setOnClickListener(this);
        findViewById(R.id.keyboard_button_2).setOnClickListener(this);
        findViewById(R.id.keyboard_button_3).setOnClickListener(this);
        findViewById(R.id.keyboard_button_4).setOnClickListener(this);
        findViewById(R.id.keyboard_button_5).setOnClickListener(this);
        findViewById(R.id.keyboard_button_6).setOnClickListener(this);
        findViewById(R.id.keyboard_button_7).setOnClickListener(this);
        findViewById(R.id.keyboard_button_8).setOnClickListener(this);
        findViewById(R.id.keyboard_button_9).setOnClickListener(this);
        findViewById(R.id.keyboard_button_point).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String decimalSeparator = ".";
                String currentText = mTextMoneyFrom.getText().toString();
                if (!TextUtils.isEmpty(currentText) && !currentText.contains(decimalSeparator)) {
                    currentText = currentText + decimalSeparator;
                    mTextMoneyFrom.setText(currentText);
                }
            }

        });
        findViewById(R.id.keyboard_button_cancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String currentText = mTextMoneyFrom.getText().toString();
                if (currentText.length() > 1) {
                    currentText = currentText.substring(0, currentText.length() - 1);
                } else {
                    currentText = "0";
                }
                mTextMoneyFrom.setText(currentText);
                makeConversion(currentText);
            }

        });
        findViewById(R.id.keyboard_button_cancel).setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                mTextMoneyFrom.setText("0");
                makeConversion("0");
                return false;
            }

        });
        findViewById(R.id.currency_from).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mCurrencyFromPicker.showPicker();
            }

        });
        findViewById(R.id.currency_to).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mCurrencyToPicker.showPicker();
            }

        });
        CurrencyUnit currency1 = getCurrency(true);
        CurrencyUnit currency2 = getCurrency(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mCurrencyFromPicker = CurrencyPicker.createPicker(fragmentManager, TAG_CURRENCY_FROM_PICKER, currency1);
        mCurrencyToPicker = CurrencyPicker.createPicker(fragmentManager, TAG_CURRENCY_TO_PICKER, currency2);
        mTextMoneyFrom.setText(savedInstanceState != null ? savedInstanceState.getString(SS_MONEY) : "0");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SS_MONEY, mTextMoneyFrom.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private CurrencyUnit getCurrency(boolean primary) {
        CurrencyUnit currencyUnit;
        if (primary) {
            currencyUnit = CurrencyManager.getCurrency(PreferenceManager.getCurrencyConverterLastCurrency1());
        } else {
            currencyUnit = CurrencyManager.getCurrency(PreferenceManager.getCurrencyConverterLastCurrency2());
        }
        if (currencyUnit == null) {
            // if no default currency is found, return the system default currency
            return getDefaultCurrencyUnit(primary);
        }
        return currencyUnit;
    }

    private CurrencyUnit getDefaultCurrencyUnit(boolean primary) {
        CurrencyUnit currencyUnit = CurrencyManager.getDefaultCurrency();
        if (!primary) {
            return currencyUnit.getIso().equals(EUR) ?
                    CurrencyManager.getCurrency(USD) : CurrencyManager.getCurrency(EUR);
        }
        return currencyUnit;
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_currency_converter;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    private void makeConversion(String text) {
        ExchangeRate exchangeRate = CurrencyManager.getExchangeRate(
                mCurrencyFromPicker.getCurrentCurrency(),
                mCurrencyToPicker.getCurrentCurrency()
        );
        if (exchangeRate != null) {
            CurrencyUnit currencyUnit = mCurrencyToPicker.getCurrentCurrency();
            long money = new BigDecimal(text)
                    .multiply(BigDecimal.valueOf(exchangeRate.getRate()))
                    .movePointRight(currencyUnit.getDecimals())
                    .longValue();
            mTextMoneyTo.setText(mMoneyFormatter.getNotTintedString(currencyUnit, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
        } else {
            mTextMoneyTo.setText(R.string.hint_unknown);
        }
    }

    @Override
    public void onClick(View view) {
        String currentText = mTextMoneyFrom.getText().toString();
        String button = ((Button) view).getText().toString();
        if (currentText.equals("0")) {
            currentText = button;
        } else {
            currentText = currentText + button;
        }
        try {
            makeConversion(currentText);
            mTextMoneyFrom.setText(currentText);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCurrencyChanged(String tag, CurrencyUnit currency) {
        switch (tag) {
            case TAG_CURRENCY_FROM_PICKER:
                loadCurrencyFlag(mImageCurrencyFrom, currency.getIso());
                mTextCurrencyFrom.setText(currency.getIso());
                PreferenceManager.setCurrencyConverterLastCurrency1(currency.getIso());
                break;
            case TAG_CURRENCY_TO_PICKER:
                loadCurrencyFlag(mImageCurrencyTo, currency.getIso());
                mTextCurrencyTo.setText(currency.getIso());
                PreferenceManager.setCurrencyConverterLastCurrency2(currency.getIso());
                break;
        }
        if (mCurrencyFromPicker.isSelected() && mCurrencyToPicker.isSelected()) {
            makeConversion(mTextMoneyFrom.getText().toString());
        }
    }

    private void loadCurrencyFlag(ImageView imageView, String iso) {
        int flag = CurrencyUnit.getCurrencyFlag(this, iso);
        if (flag > 0) {
            imageView.setImageDrawable(null);
            Glide.with(this)
                    .load(flag)
                    .into(imageView);
        } else {
            imageView.setImageDrawable(CurrencyUnit.getCurrencyDrawable(iso));
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCurrencyFromPicker.isSelected() && mCurrencyToPicker.isSelected()) {
                makeConversion(mTextMoneyFrom.getText().toString());
            }
        }

    };
}