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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.ExchangeRate;
import com.oriondev.moneywallet.storage.cache.ExchangeRateCache;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class act as a proxy on the top of the content provider.
 * It is responsible to cache the currency list for a faster access at runtime and manage all
 * the related operations (like handle currency rates).
 */
public class CurrencyManager {

    private static final Object CACHE_MUTEX = new Object();

    private static CurrencyManager mInstance;

    public static void initialize(Context context) {
        if (mInstance == null) {
            mInstance = new CurrencyManager(context);
        }
    }

    private final ExchangeRateCache mExchangeRateCache;
    private final Map<String, CurrencyUnit> mCurrencyCache;

    private CurrencyManager(Context context) {
        mExchangeRateCache = new ExchangeRateCache(context);
        mCurrencyCache = new HashMap<>();
        loadCurrencies(context);
    }

    private void loadCurrencies(Context context) {
        loadUserCurrencies(context);
        if (mCurrencyCache.isEmpty()) {
            System.out.println("[CurrencyManager] No currency found. Loading default currencies from the assets...");
            loadDefaultCurrencies(context);
        }
    }

    public static void invalidateCache(Context context) {
        synchronized (CACHE_MUTEX) {
            System.out.println("[CurrencyManager] Invalidating cache...");
            mInstance.mCurrencyCache.clear();
            mInstance.loadCurrencies(context);
        }
    }

    /**
     * This method will force reload all currencies from the database inside the currency manager.
     * A call to this method is very expensive because it is an I/O operation on the main thread.
     * @param context of the application.
     */
    private void loadUserCurrencies(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] projections = new String[] {
                Contract.Currency.ISO,
                Contract.Currency.NAME,
                Contract.Currency.SYMBOL,
                Contract.Currency.DECIMALS
        };
        Cursor cursor = contentResolver.query(DataContentProvider.CONTENT_CURRENCIES, projections, null, null, null);
        if (cursor != null) {
            int indexIso = cursor.getColumnIndex(Contract.Currency.ISO);
            int indexName = cursor.getColumnIndex(Contract.Currency.NAME);
            int indexSymbol = cursor.getColumnIndex(Contract.Currency.SYMBOL);
            int indexDecimals = cursor.getColumnIndex(Contract.Currency.DECIMALS);
            while (cursor.moveToNext()) {
                CurrencyUnit currencyUnit = new CurrencyUnit(
                        cursor.getString(indexIso),
                        cursor.getString(indexName),
                        cursor.getString(indexSymbol),
                        cursor.getInt(indexDecimals));
                mCurrencyCache.put(currencyUnit.getIso(), currencyUnit);
            }
            cursor.close();
        }
    }

    private void loadDefaultCurrencies(Context context) {
        try {
            // open assets file and load all the default currencies into a JSONArray
            StringBuilder jsonBuilder = new StringBuilder();
            InputStream inputStream = context.getAssets().open("resources/currencies.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONArray array = new JSONArray(jsonBuilder.toString());
            // for each currency, load it into cache and store a copy inside the database
            ContentResolver contentResolver = context.getContentResolver();
            for (int i = 0; i < array.length(); i++) {
                JSONObject currency = array.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.Currency.ISO, currency.getString("code"));
                contentValues.put(Contract.Currency.NAME, currency.getString("name"));
                contentValues.put(Contract.Currency.SYMBOL, currency.optString("symbol", null));
                contentValues.put(Contract.Currency.DECIMALS, currency.optInt("decimals", 2));
                contentResolver.insert(DataContentProvider.CONTENT_CURRENCIES, contentValues);
                // directly store the currency inside the local cache
                CurrencyUnit currencyUnit = new CurrencyUnit(
                        contentValues.getAsString(Contract.Currency.ISO),
                        contentValues.getAsString(Contract.Currency.NAME),
                        contentValues.getAsString(Contract.Currency.SYMBOL),
                        contentValues.getAsInteger(Contract.Currency.DECIMALS)
                );
                mCurrencyCache.put(currencyUnit.getIso(), currencyUnit);
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Exception while reading currencies file from assets: " + e.getMessage());
        }
    }

    /**
     * Obtain the currency object from the iso code.
     * @param iso of the currency to obtain.
     * @return the currency object if the iso code is found.
     */
    public static CurrencyUnit getCurrency(String iso) {
        synchronized (CACHE_MUTEX) {
            return mInstance.mCurrencyCache.get(iso);
        }
    }

    public static Collection<CurrencyUnit> getCurrencies() {
        synchronized (CACHE_MUTEX) {
            return mInstance.mCurrencyCache.values();
        }
    }

    public static ExchangeRate getExchangeRate(CurrencyUnit currency1, CurrencyUnit currency2) {
        return mInstance.mExchangeRateCache.getExchangeRate(currency1.getIso(), currency2.getIso());
    }

    /**
     * Obtain the currency of the current locale used by the application.
     * If for example the user is using the Italian locale, the EUR currency will be returned.
     * @return the current currency.
     */
    public static CurrencyUnit getDefaultCurrency() {
        synchronized (CACHE_MUTEX) {
            Locale locale = Locale.getDefault();
            Currency currency = Currency.getInstance(locale);
            return getCurrency(currency.getCurrencyCode());
        }
    }

    public static ExchangeRateCache getExchangeRateCache() {
        return mInstance.mExchangeRateCache;
    }
}