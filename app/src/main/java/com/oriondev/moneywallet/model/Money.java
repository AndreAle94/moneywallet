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

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by andrea on 23/01/18.
 */
public class Money {

    private Map<String, Long> mCurrencies;

    public static Money empty() {
        return new Money();
    }

    public Money() {
        mCurrencies = new HashMap<>();
    }

    public Money(String currency, long money) {
        mCurrencies = new HashMap<>();
        mCurrencies.put(currency, money);
    }

    public void addMoney(String currency, long money) {
        long total = money;
        if (mCurrencies.containsKey(currency)) {
            total += mCurrencies.get(currency);
        }
        mCurrencies.put(currency, total);
    }

    public void addMoney(Money money) {
        if (money != null) {
            for (Map.Entry<String, Long> currency : money.mCurrencies.entrySet()) {
                addMoney(currency.getKey(), currency.getValue());
            }
        }
    }

    public void removeMoney(String currency, long money) {
        long total = -money;
        if (mCurrencies.containsKey(currency)) {
            total += mCurrencies.get(currency);
        }
        mCurrencies.put(currency, total);
    }

    public void removeMoney(Money money) {
        if (money != null) {
            for (Map.Entry<String, Long> currency : money.mCurrencies.entrySet()) {
                removeMoney(currency.getKey(), currency.getValue());
            }
        }
    }

    public long getMoney(String currency) {
        Long money = mCurrencies.get(currency);
        return money != null ? money : 0;
    }

    public Map<String, Long> getCurrencyMoneys() {
        return mCurrencies;
    }

    public Set<String> getCurrencies() {
        return mCurrencies.keySet();
    }

    public int getNumberOfCurrencies() {
        return mCurrencies.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : mCurrencies.entrySet()) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(String.format(Locale.ENGLISH, "%s %d", entry.getKey(), entry.getValue()));
        }
        return builder.toString();
    }

    public static Money parse(String string) {
        Money money = new Money();
        if (!TextUtils.isEmpty(string)) {
            String[] currencies = string.split(",");
            for (String currency : currencies) {
                String[] parts = currency.split(" ");
                money.addMoney(parts[0], Long.parseLong(parts[1]));
            }
        }
        return money;
    }
}