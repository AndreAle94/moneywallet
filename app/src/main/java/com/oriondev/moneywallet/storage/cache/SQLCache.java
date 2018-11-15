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

package com.oriondev.moneywallet.storage.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andre on 24/03/2018.
 */
/*package-local*/ class SQLCache extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cache.db";
    private static final int DATABASE_VERSION = 1;

    /*package-local*/ static final class ExchangeRateT {
        private static final String TABLE = "exchange_rates";
        /*package-local*/ static final String CURRENCY_ISO = "exchange_currency_iso";
        /*package-local*/ static final String RATE = "exchange_rate";
        /*package-local*/ static final String TIMESTAMP = "exchange_timestamp";
    }

    private static final String CREATE_TABLE_EXCHANGE_RATE = "CREATE TABLE " + ExchangeRateT.TABLE + " (" +
            ExchangeRateT.CURRENCY_ISO + " TEXT PRIMARY KEY, " +
            ExchangeRateT.RATE + " REAL NOT NULL, " +
            ExchangeRateT.TIMESTAMP + " INTEGER NOT NULL" +
            ")";

    /*package-local*/ SQLCache(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EXCHANGE_RATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing -> this is just a cache
    }

    /*package-local*/ Cursor getExchangeRates(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getReadableDatabase().query(ExchangeRateT.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    /*package-local*/ void insertOrUpdateExchangeRate(ContentValues contentValues) {
        getWritableDatabase().insertWithOnConflict(ExchangeRateT.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }
}