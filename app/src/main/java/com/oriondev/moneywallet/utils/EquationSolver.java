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

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import com.oriondev.moneywallet.model.CurrencyUnit;

import java.math.BigDecimal;

/**
 * Created by andrea on 31/03/18.
 */
public class EquationSolver {

    private static final String SS_FIRST_NUMBER = "EquationSolver::SavedState::FirstNumber";
    private static final String SS_SECOND_NUMBER = "EquationSolver::SavedState::SecondNumber";
    private static final String SS_OPERATION_NUMBER = "EquationSolver::SavedState::Operation";
    private static final String SS_CURRENCY = "EquationSolver::SavedState::Currency";

    private final Controller mController;

    @VisibleForTesting
    /*package-local*/ String mFirstNumber;
    private String mSecondNumber;
    private Operation mOperation;
    @VisibleForTesting
    /*package-local*/ CurrencyUnit mCurrency;

    public EquationSolver(Bundle savedInstanceState, Controller controller) {
        mController = controller;
        if (savedInstanceState != null) {
            mFirstNumber = savedInstanceState.getString(SS_FIRST_NUMBER, "0");
            mSecondNumber = savedInstanceState.getString(SS_SECOND_NUMBER, null);
            mOperation = (Operation) savedInstanceState.getSerializable(SS_OPERATION_NUMBER);
            mCurrency = savedInstanceState.getParcelable(SS_CURRENCY);
        } else {
            mFirstNumber = "0";
            mSecondNumber = null;
            mOperation = null;
            mCurrency = null;
        }
        updateDisplaySafely();
    }

    public void setValue(CurrencyUnit currency, long money) {
        if (currency != null && money != 0L && currency.hasDecimals()) {
            mFirstNumber = String.valueOf(money / Math.pow(10, currency.getDecimals()));
        } else {
            mFirstNumber = String.valueOf(money);
        }
        mSecondNumber = null;
        mOperation = null;
        mCurrency = currency;
        updateDisplaySafely();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SS_FIRST_NUMBER, mFirstNumber);
        outState.putString(SS_SECOND_NUMBER, mSecondNumber);
        outState.putSerializable(SS_OPERATION_NUMBER, mOperation);
    }

    public void clear() {
        mFirstNumber = "0";
        mSecondNumber = null;
        mOperation = null;
        updateDisplaySafely();
    }

    public void cancel() {
        if (mSecondNumber != null && !mSecondNumber.isEmpty()) {
            mSecondNumber = mSecondNumber.substring(0, mSecondNumber.length() - 1);
        } else if (mOperation != null) {
            mOperation = null;
        } else if (mFirstNumber != null && !mFirstNumber.isEmpty()) {
            mFirstNumber = mFirstNumber.substring(0, mFirstNumber.length() - 1);
            if (mFirstNumber.isEmpty()) {
                mFirstNumber = "0";
            }
        }
        updateDisplaySafely();
    }

    public void appendOperation(Operation operation) {
        if (mOperation == null || execute(false)) {
            mOperation = operation;
            updateDisplaySafely();
        }
    }

    public void appendPoint() {
        String number = mOperation == null ? mFirstNumber : mSecondNumber;
        if (number == null || number.isEmpty() || number.equals("0")) {
            number = "0.";
        } else if (!number.contains(".")) {
            number += ".";
        }
        mFirstNumber = mOperation == null ? number : mFirstNumber;
        mSecondNumber = mOperation == null ? null : number;
        updateDisplaySafely();
    }

    public void appendNumber(String digit) {
        String number = mOperation == null ? mFirstNumber : mSecondNumber;
        if (number == null || number.isEmpty() || number.equals("0")) {
            number = digit.equals("000") ? "0" : digit;
        } else {
            number += digit;
        }
        mFirstNumber = mOperation == null ? number : mFirstNumber;
        mSecondNumber = mOperation == null ? null : number;
        updateDisplaySafely();
    }

    public boolean isPendingOperation() {
        return mOperation != null;
    }

    public boolean execute(boolean fireCallback) {
        BigDecimal first = parseNumber(mFirstNumber);
        BigDecimal second = parseNumber(mSecondNumber);
        try {
            BigDecimal result;
            switch (mOperation) {
                case ADDITION:
                    result = first.add(second);
                    break;
                case SUBTRACTION:
                    result = first.subtract(second);
                    break;
                case MULTIPLICATION:
                    result = first.multiply(second);
                    break;
                case DIVISION:
                    result = first.divide(second,BigDecimal.ROUND_HALF_EVEN);
                    break;
                default:
                    result = BigDecimal.valueOf(0);
                    break;
            }
            mFirstNumber = String.valueOf(result);
            if (mFirstNumber.endsWith(".0")) {
                mFirstNumber = mFirstNumber.substring(0, mFirstNumber.length() - 2);
            }
            mSecondNumber = null;
            mOperation = null;
            if (fireCallback) {
                updateDisplaySafely();
            }
            return true;
        } catch (ArithmeticException ignore) {
            ignore.printStackTrace();
        }
        return false;
    }

    public long getResult() {
        if (isPendingOperation() && !execute(false)) {
            // Error occurred during calculation
            return 0L;
        }
        BigDecimal parsedNumber = parseNumber(mFirstNumber);
        if (mCurrency != null) {
            return parsedNumber.movePointRight(mCurrency.getDecimals()).longValue();
        }
        return parsedNumber.longValue();
    }

    private BigDecimal parseNumber(String number) {
        String safe = number == null || number.isEmpty() ? "0" : number;
        if (safe.endsWith(".")) {
            safe = safe.substring(0, safe.length() - 1);
        }
        try {
            return new BigDecimal(safe);
        } catch (NumberFormatException ignore) {}
        return BigDecimal.valueOf(0);
    }

    public enum Operation {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }

    private void updateDisplaySafely() {
        if (mController != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(mFirstNumber);
            if (mOperation != null) {
                switch (mOperation) {
                    case ADDITION:
                        builder.append(" + ");
                        break;
                    case SUBTRACTION:
                        builder.append(" − ");
                        break;
                    case MULTIPLICATION:
                        builder.append(" × ");
                        break;
                    case DIVISION:
                        builder.append(" ÷ ");
                        break;
                }
                if (mSecondNumber != null) {
                    builder.append(mSecondNumber);
                }
            }
            mController.onUpdateDisplay(builder.toString());
        }
    }

    public interface Controller {

        void onUpdateDisplay(String text);
    }
}