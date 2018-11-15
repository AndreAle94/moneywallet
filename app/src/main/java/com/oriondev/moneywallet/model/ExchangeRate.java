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

package com.oriondev.moneywallet.model;

import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andre on 24/03/2018.
 */
public class ExchangeRate {

    private final String mCurrency1;
    private final String mCurrency2;
    private final double mRate;
    private final long mTimestamp;

    public ExchangeRate(String currency1, String currency2, double rate, long timestamp) {
        mCurrency1 = currency1;
        mCurrency2 = currency2;
        mRate = rate;
        mTimestamp = timestamp;
    }

    public CurrencyUnit getCurrency1() {
        return CurrencyManager.getCurrency(mCurrency1);
    }

    public CurrencyUnit getCurrency2() {
        return CurrencyManager.getCurrency(mCurrency2);
    }

    public double getRate() {
        return mRate;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}