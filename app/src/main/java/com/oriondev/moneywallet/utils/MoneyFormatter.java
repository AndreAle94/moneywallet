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

package com.oriondev.moneywallet.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.theme.ThemeEngine;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Created by andrea on 23/01/18.
 */
public class MoneyFormatter {

    private static final int DEFAULT_MIN_INTEGER_DIGITS = 1;
    private static final int DEFAULT_MIN_FRACTION_DIGITS_STANDARD = 2;
    private static final int DEFAULT_MAX_FRACTION_DIGITS_STANDARD = 2;
    private static final int DEFAULT_MIN_FRACTION_DIGITS_ROUNDING = 0;
    private static final int DEFAULT_MAX_FRACTION_DIGITS_ROUNDING = 0;
    private static final int DEFAULT_DIVIDER_POW = 2;

    private final static String EMPTY_TEXT = " --- ";
    private final static String DIVIDER_TEXT = " - ";

    private int mColorIn;
    private int mColorOut;
    private int mColorNeutral;
    private boolean mCurrencyEnabled;
    private boolean mGroupDigitEnabled;
    private boolean mRoundDecimalsEnabled;
    private boolean mShowSymbolEnabled;
    private NumberFormat mFormatter;

    public enum CurrencyMode {
        ALWAYS_SHOWN,
        ALWAYS_HIDDEN,
        USER_PREFERENCE
    }

    public enum TintMode {
        AUTO_DETECT,
        INCOME,
        EXPENSE
    }

    public enum FlowMode {
        FORCE_POSITIVE,
        FORCE_NEGATIVE,
        AUTO_DETECT
    }

    public static MoneyFormatter getInstance() {
        return new MoneyFormatter();
    }

    public static long normalize(long money, int decimals, int finalDecimals) {
        int offset = finalDecimals - decimals;
        return normalize(money, offset);
    }

    public static long normalize(long money, int decimalOffset) {
        double exponential = Math.pow(10d, decimalOffset);
        return (long) (money * exponential);
    }

    private MoneyFormatter() {
        mColorIn = PreferenceManager.getCurrentIncomeColor();
        mColorOut = PreferenceManager.getCurrentExpenseColor();
        mColorNeutral = ThemeEngine.getTheme().getTextColorPrimary();
        mCurrencyEnabled = PreferenceManager.isCurrencyEnabled();
        mGroupDigitEnabled = PreferenceManager.isGroupDigitEnabled();
        mRoundDecimalsEnabled = PreferenceManager.isRoundDecimalsEnabled();
        mShowSymbolEnabled = PreferenceManager.isShowPlusMinusSymbolEnabled();
        mFormatter = DecimalFormat.getInstance();
        mFormatter.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
    }

    public void setCurrencyEnabled(boolean enabled) {
        mCurrencyEnabled = enabled;
    }

    public void setGroupDigitEnabled(boolean enabled) {
        mGroupDigitEnabled = enabled;
    }

    public void setRoundDecimalsEnabled(boolean enabled) {
        mRoundDecimalsEnabled = enabled;
    }

    public void setShowSymbolEnabled(boolean enabled) {
        mShowSymbolEnabled = enabled;
    }

    public boolean isCurrencyEnabled() {
        return mCurrencyEnabled;
    }

    public boolean isGroupDigitEnabled() {
        return mGroupDigitEnabled;
    }

    public boolean isRoundDecimalsEnabled() {
        return mRoundDecimalsEnabled;
    }

    public boolean isShowSymbolEnabled() {
        return mShowSymbolEnabled;
    }

    public String getNotTintedString(Money money) {
        StringBuilder builder = new StringBuilder();
        int numberOfCurrencies = money.getNumberOfCurrencies();
        if (numberOfCurrencies == 0) {
            builder.append(EMPTY_TEXT);
        } else {
            CurrencyMode currencyMode = money.getNumberOfCurrencies() > 1 ? CurrencyMode.ALWAYS_SHOWN : CurrencyMode.USER_PREFERENCE;
            for (Map.Entry<String, Long> entry : money.getCurrencyMoneys().entrySet()) {
                if (builder.length() > 0) {
                    builder.append(DIVIDER_TEXT);
                }
                CurrencyUnit currencyUnit = CurrencyManager.getCurrency(entry.getKey());
                long currencyMoney = entry.getValue();
                builder.append(getNotTintedString(currencyUnit, currencyMoney, currencyMode, FlowMode.AUTO_DETECT));
            }
        }
        return builder.toString();
    }

    public String getNotTintedString(CurrencyUnit currencyUnit, long money) {
        return getNotTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, FlowMode.AUTO_DETECT);
    }

    public String getNotTintedString(CurrencyUnit currencyUnit, long money, CurrencyMode currencyMode) {
        return getNotTintedString(currencyUnit, money, currencyMode, FlowMode.AUTO_DETECT);
    }

    private String getNotTintedString(CurrencyUnit currencyUnit, long money, CurrencyMode currencyMode, FlowMode flowMode) {
        StringBuilder builder = new StringBuilder();
        double divider;
        if (currencyUnit != null) {
            mFormatter.setMinimumFractionDigits(currencyUnit.getDecimals());
            mFormatter.setMaximumFractionDigits(currencyUnit.getDecimals());
            divider = Math.pow(10, currencyUnit.getDecimals());
            if (currencyMode == CurrencyMode.ALWAYS_SHOWN || (currencyMode == CurrencyMode.USER_PREFERENCE && mCurrencyEnabled)) {
                builder.append(currencyUnit.getSymbol());
                builder.append(" ");
            }
        } else {
            mFormatter.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS_STANDARD);
            mFormatter.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS_STANDARD);
            divider = Math.pow(10, DEFAULT_DIVIDER_POW);
        }
        mFormatter.setGroupingUsed(mGroupDigitEnabled);
        if (mRoundDecimalsEnabled) {
            mFormatter.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS_ROUNDING);
            mFormatter.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS_ROUNDING);
            mFormatter.setRoundingMode(RoundingMode.HALF_UP);
        }
        if (flowMode == FlowMode.FORCE_NEGATIVE || (flowMode == FlowMode.AUTO_DETECT && money < 0L)) {
            builder.append("-");
        } else if (mShowSymbolEnabled) {
            builder.append("+");
        }
        builder.append(mFormatter.format((double) Math.abs(money) / divider));
        return builder.toString();
    }

    public SpannableStringBuilder getTintedString(Money money) {
        return getTintedString(money, TintMode.AUTO_DETECT);
    }

    public SpannableStringBuilder getTintedString(Money money, TintMode tintMode) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int numberOfCurrencies = money.getNumberOfCurrencies();
        if (numberOfCurrencies == 0) {
            SpannableString emptyString = new SpannableString(EMPTY_TEXT);
            emptyString.setSpan(new ForegroundColorSpan(mColorNeutral), 0, emptyString.length(), 0);
            builder.append(emptyString);
        } else {
            CurrencyMode currencyMode = money.getNumberOfCurrencies() > 1 ? CurrencyMode.ALWAYS_SHOWN : CurrencyMode.USER_PREFERENCE;
            for (Map.Entry<String, Long> entry : money.getCurrencyMoneys().entrySet()) {
                if (builder.length() > 0) {
                    SpannableString divider = new SpannableString(DIVIDER_TEXT);
                    divider.setSpan(new ForegroundColorSpan(mColorNeutral), 0, divider.length(), 0);
                    builder.append(divider);
                }
                CurrencyUnit currencyUnit = CurrencyManager.getCurrency(entry.getKey());
                long currencyMoney = entry.getValue();
                builder.append(getTintedString(currencyUnit, currencyMoney, currencyMode, tintMode));
            }
        }
        return builder;
    }

    public SpannableString getTintedString(CurrencyUnit currencyUnit, long money) {
        return getTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, TintMode.AUTO_DETECT);
    }

    public SpannableString getTintedString(CurrencyUnit currencyUnit, long money, CurrencyMode currencyMode) {
        return getTintedString(currencyUnit, money, currencyMode, TintMode.AUTO_DETECT);
    }

    private SpannableString getTintedString(CurrencyUnit currencyUnit, long money, CurrencyMode currencyMode, TintMode tintMode) {
        FlowMode flowMode = mShowSymbolEnabled ? getFlowModeFromTintMode(tintMode) : FlowMode.FORCE_POSITIVE;
        String notTinted = getNotTintedString(currencyUnit, money, currencyMode, flowMode);
        SpannableString spannableString = new SpannableString(notTinted);
        if (tintMode == TintMode.AUTO_DETECT) {
            spannableString.setSpan(new ForegroundColorSpan(money < 0L ? mColorOut : mColorIn), 0, notTinted.length(), 0);
        } else if (tintMode == TintMode.INCOME) {
            spannableString.setSpan(new ForegroundColorSpan(mColorIn), 0, notTinted.length(), 0);
        } else if (tintMode == TintMode.EXPENSE) {
            spannableString.setSpan(new ForegroundColorSpan(mColorOut), 0, notTinted.length(), 0);
        }
        return spannableString;
    }

    private FlowMode getFlowModeFromTintMode(TintMode tintMode) {
        switch (tintMode) {
            case INCOME:
                return FlowMode.FORCE_POSITIVE;
            case EXPENSE:
                return FlowMode.FORCE_NEGATIVE;
        }
        return FlowMode.AUTO_DETECT;
    }

    public void applyNotTinted(TextView textView, Money money) {
        textView.setText(getNotTintedString(money));
    }

    public void applyNotTinted(TextView textView, CurrencyUnit currencyUnit, long money) {
        textView.setText(getNotTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, FlowMode.AUTO_DETECT));
    }

    public void applyTinted(TextView textView, Money money) {
        textView.setText(getTintedString(money), TextView.BufferType.SPANNABLE);
    }

    public void applyTinted(TextView textView, CurrencyUnit currencyUnit, long money) {
        textView.setText(getTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, TintMode.AUTO_DETECT), TextView.BufferType.SPANNABLE);
    }

    public void applyTintedIncome(TextView textView, Money money) {
        textView.setText(getTintedString(money, TintMode.INCOME), TextView.BufferType.SPANNABLE);
    }

    public void applyTintedIncome(TextView textView, CurrencyUnit currencyUnit, long money) {
        textView.setText(getTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, TintMode.INCOME), TextView.BufferType.SPANNABLE);
    }

    public void applyTintedExpense(TextView textView, Money money) {
        textView.setText(getTintedString(money, TintMode.EXPENSE), TextView.BufferType.SPANNABLE);
    }

    public void applyTintedExpense(TextView textView, CurrencyUnit currencyUnit, long money) {
        textView.setText(getTintedString(currencyUnit, money, CurrencyMode.USER_PREFERENCE, TintMode.EXPENSE), TextView.BufferType.SPANNABLE);
    }
}