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

package com.oriondev.moneywallet.storage.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseLongArray;

import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * This class implements the default SQLite helper class of the Android Framework.
 * Here are handled all the raw operation with the database.
 */
/*package-local*/ class SQLDatabase extends SQLiteOpenHelper {

    /*package-local*/ static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 3;

    private static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys=ON";

    private final Context mContext;
    private boolean mCacheDeletedObjects;

    /*package-local*/ SQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mCacheDeletedObjects = true;
    }

    /**
     * The delete cache is used to correctly sync deleted objects with a remote backend.
     * If the sync is not enabled, the database will permanently remove deleted objects
     * and not simply flag them as deleted.
     * @param cacheEnabled true if cache is enabled, false otherwise.
     */
    /*package-local*/ void setDeletedObjectCacheEnabled(boolean cacheEnabled) {
        mCacheDeletedObjects = cacheEnabled;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create all tables
        db.execSQL(Schema.CREATE_TABLE_CURRENCY);
        db.execSQL(Schema.CREATE_TABLE_WALLET);
        db.execSQL(Schema.CREATE_TABLE_CATEGORY);
        db.execSQL(Schema.CREATE_TABLE_EVENT);
        db.execSQL(Schema.CREATE_TABLE_PLACE);
        db.execSQL(Schema.CREATE_TABLE_PERSON);
        db.execSQL(Schema.CREATE_TABLE_EVENT_PEOPLE);
        db.execSQL(Schema.CREATE_TABLE_DEBT);
        db.execSQL(Schema.CREATE_TABLE_DEBT_PEOPLE);
        db.execSQL(Schema.CREATE_TABLE_BUDGET);
        db.execSQL(Schema.CREATE_TABLE_BUDGET_WALLET);
        db.execSQL(Schema.CREATE_TABLE_SAVING);
        db.execSQL(Schema.CREATE_TABLE_TRANSACTION);
        db.execSQL(Schema.CREATE_TABLE_TRANSACTION_PEOPLE);
        db.execSQL(Schema.CREATE_TABLE_TRANSFER);
        db.execSQL(Schema.CREATE_TABLE_TRANSFER_PEOPLE);
        db.execSQL(Schema.CREATE_TABLE_TRANSACTION_MODEL);
        db.execSQL(Schema.CREATE_TABLE_TRANSFER_MODEL);
        db.execSQL(Schema.CREATE_TABLE_RECURRENT_TRANSACTION);
        db.execSQL(Schema.CREATE_TABLE_RECURRENT_TRANSFER);
        db.execSQL(Schema.CREATE_TABLE_ATTACHMENT);
        db.execSQL(Schema.CREATE_TABLE_TRANSACTION_ATTACHMENT);
        db.execSQL(Schema.CREATE_TABLE_TRANSFER_ATTACHMENT);
        // create all triggers to ensure data consistency
        // TODO [low] create triggers
        // insert default items
        addSystemCategories(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        } else {
            db.execSQL(ENABLE_FOREIGN_KEYS);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        // the patch is no more necessary to avoid bad behaviours when users
        // upgrade the app from the first release to the last one: if a user
        // encounter an issue with the currency decimals, he can adjust it
        // manually inside the app settings ('Utility -> Manage currencies')
        if (oldVersion < 2) {
            // we need to patch the money belonging to wallets where the currency
            // has a number of decimals different from 2 (we have made a mistake
            // during the upgrade from the legacy database: we forgot to normalize
            // where the decimals count is different from 2).
        }
        */
        if (oldVersion < 3) {
            // we need to add a new column to the wallet and the category table in order
            // to let the user sort the items inside these tables of the database.
            db.execSQL(Schema.CREATE_WALLET_INDEX_COLUMN);
            db.execSQL(Schema.CREATE_CATEGORY_INDEX_COLUMN);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Insert system categories inside the database. If a system category is already there,
     * the operation is aborted. This method must be called whenever a new database is created
     * or an update is performed in order to keep the system list updated.
     *
     * @param db instance of the database to write.
     */
    private void addSystemCategories(SQLiteDatabase db) {
        for (SystemCategory category : SystemCategory.mSystemCategories) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Schema.Category.NAME, category.getName(mContext));
            contentValues.put(Schema.Category.ICON, category.getIcon(mContext));
            contentValues.put(Schema.Category.TYPE, Contract.CategoryType.SYSTEM.getValue());
            contentValues.put(Schema.Category.SHOW_REPORT, true);
            contentValues.put(Schema.Category.INDEX, 0);
            contentValues.put(Schema.Category.TAG, category.getTag());
            contentValues.put(Schema.Category.UUID, category.getUUID());
            contentValues.put(Schema.Category.LAST_EDIT, System.currentTimeMillis());
            contentValues.put(Schema.Category.DELETED, false);
            db.insertWithOnConflict(Schema.Category.TABLE, null, contentValues, SQLiteDatabase.CONFLICT_ABORT);
        }
    }

    /**
     * This method is used internally to retrieve the id of a system category.
     *
     * @param tag of the system category to query.
     * @return the id of the system category if found, null otherwise.
     */
    private Long getSystemCategoryId(String tag) {
        String[] projection = new String[] {Schema.Category.ID};
        String selection = Schema.Category.TAG + " = ?";
        String[] selectionArgs = new String[] {tag};
        Cursor cursor = getCategories(projection, selection, selectionArgs, null);
        if (cursor != null) {
            Long categoryId = null;
            if (cursor.moveToFirst()) {
                categoryId = cursor.getLong(cursor.getColumnIndex(Schema.Category.ID));
            }
            cursor.close();
            return categoryId;
        }
        return null;
    }

    /**
     * This method is called by the content provider when the user is querying a specific currency
     * from the database.
     *
     * @param iso of the requested currency.
     * @param projection of the currency table.
     * @return the cursor that contains the requested data.
     */
    /*package-local*/ Cursor getCurrency(String iso, String[] projection) {
        String selection = Schema.Currency.ISO + " = ?";
        String[] selectionArgs = new String[]{iso};
        return getCurrencies(projection, selection, selectionArgs, null);
    }

    /**
     * Query for currencies stored inside the database.
     *
     * @param projection    of the currency table.
     * @param selection     of the required rows.
     * @param selectionArgs for the selection statements.
     * @param sortOrder     for the result cursor.
     * @return the cursor that contains the requested data.
     */
    /*package-local*/ Cursor getCurrencies(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Currency.ISO + " AS " + Contract.Currency.ISO + ", " +
                Schema.Currency.NAME + " AS " + Contract.Currency.NAME + ", " +
                Schema.Currency.SYMBOL + " AS " + Contract.Currency.SYMBOL + ", " +
                Schema.Currency.DECIMALS + " AS " + Contract.Currency.DECIMALS + ", " +
                Schema.Currency.FAVOURITE + " AS " + Contract.Currency.FAVOURITE +
                " FROM " + Schema.Currency.TABLE +
                " WHERE " + Schema.Currency.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new currency
     * into the database.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ String insertCurrency(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Currency.ISO, contentValues.getAsString(Contract.Currency.ISO));
        cv.put(Schema.Currency.NAME, contentValues.getAsString(Contract.Currency.NAME));
        cv.put(Schema.Currency.SYMBOL, contentValues.getAsString(Contract.Currency.SYMBOL));
        if (contentValues.containsKey(Schema.Currency.DECIMALS)) {
            int decimals = contentValues.getAsInteger(Contract.Currency.DECIMALS);
            if (decimals < 0) {
                decimals = 0;
            } else if (decimals > 8) {
                decimals = 8;
            }
            cv.put(Schema.Currency.DECIMALS, decimals);
        }
        if (contentValues.containsKey(Schema.Currency.FAVOURITE)) {
            cv.put(Schema.Currency.FAVOURITE, contentValues.getAsBoolean(Contract.Currency.FAVOURITE));
        }
        cv.put(Schema.Currency.UUID, "currency_" + contentValues.getAsString(Contract.Currency.ISO));
        cv.put(Schema.Currency.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Currency.DELETED, false);
        if (getWritableDatabase().insert(Schema.Currency.TABLE, null, cv) > 0) {
            return contentValues.getAsString(Contract.Currency.ISO);
        }
        return null;
    }

    /**
     * This method is called by the content provider when the user is updating an existing
     * currency in the database.
     *
     * @param iso id of the currency to update.
     * @param contentValues bundle that contains the data from the content provider.
     * @return the number of row updated inside the database.
     */
    /*package-local*/ int updateCurrency(String iso, ContentValues contentValues) {
        int oldDecimals = 0;
        String[] projection = new String[] {
                Schema.Currency.DECIMALS
        };
        String selection = Schema.Currency.ISO + " = ?";
        String[] selectionArgs = new String[] {iso};
        Cursor cursor = getReadableDatabase().query(Schema.Currency.TABLE, projection, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                oldDecimals = cursor.getInt(cursor.getColumnIndex(Schema.Currency.DECIMALS));
            }
            cursor.close();
        }
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Schema.Currency.NAME)) {
            cv.put(Schema.Currency.NAME, contentValues.getAsString(Contract.Currency.NAME));
        }
        if (contentValues.containsKey(Schema.Currency.SYMBOL)) {
            cv.put(Schema.Currency.SYMBOL, contentValues.getAsString(Contract.Currency.SYMBOL));
        }
        int newDecimals = oldDecimals;
        if (contentValues.containsKey(Schema.Currency.DECIMALS)) {
            newDecimals = contentValues.getAsInteger(Contract.Currency.DECIMALS);
            if (newDecimals < 0) {
                newDecimals = 0;
            } else if (newDecimals > 8) {
                newDecimals = 8;
            }
            cv.put(Schema.Currency.DECIMALS, newDecimals);
        }
        if (contentValues.containsKey(Schema.Currency.FAVOURITE)) {
            cv.put(Schema.Currency.FAVOURITE, contentValues.getAsBoolean(Contract.Currency.FAVOURITE));
        }
        cv.put(Schema.Currency.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Currency.ISO + " = ?";
        String[] whereArgs = new String[]{iso};
        int rows = getWritableDatabase().update(Schema.Currency.TABLE, cv, where, whereArgs);
        if (oldDecimals != newDecimals && contentValues.containsKey(Contract.Currency.FIX_MONEY_DECIMALS) && contentValues.getAsBoolean(Contract.Currency.FIX_MONEY_DECIMALS)) {
            int decimalOffset = newDecimals - oldDecimals;
            fixCurrencyAmounts(getWritableDatabase(), iso, decimalOffset);
        }
        return rows;
    }

    /**
     * This method is called by the content provider when the user is deleting a currency from the
     * database. The currency cannot be deleted if it is in use.
     *
     * @param iso of the currency to delete.
     * @return the number of rows affected by the deletion.
     * @throws SQLiteDataException if the currency is in use.
     */
    /*package-local*/ int deleteCurrency(String iso) {
        String[] projection = new String[] {
                Schema.Wallet.ID
        };
        String where = Schema.Wallet.CURRENCY + " = ?";
        String[] whereArgs = new String[]{iso};
        Cursor cursor = getReadableDatabase().query(Schema.Wallet.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long walletId = cursor.getLong(cursor.getColumnIndex(Schema.Wallet.ID));
                    throw new SQLiteDataException(Contract.ErrorCode.CURRENCY_IN_USE,
                            String.format(Locale.ENGLISH, "The currency (iso: %s) cannot be deleted because is in use in wallet (id: %d)", iso, walletId));
                }
            } finally {
                cursor.close();
            }
        }
        where = Schema.Currency.ISO + " = ?";
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Currency.DELETED, true);
            cv.put(Schema.Currency.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Currency.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Currency.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific wallet from
     * the database.
     *
     * @param id of the requested wallet.
     * @param projection of the wallet table.
     * @return the cursor that contains the requested data.
     */
    /*package-local*/ Cursor getWallet(long id, String[] projection) {
        String selection = Schema.Wallet.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getWallets(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the wallets from
     * the database.
     *
     * @param projection column names to include in the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments of the selection string.
     * @param sortOrder string that may contains column names to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getWallets(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Wallet.ID + " AS " + Contract.Wallet.ID + ", " +
                Schema.Wallet.NAME + " AS " + Contract.Wallet.NAME + ", " +
                Schema.Wallet.ICON + " AS " + Contract.Wallet.ICON + ", " +
                Schema.Wallet.CURRENCY + " AS " + Contract.Wallet.CURRENCY + ", " +
                Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Wallet.COUNT_IN_TOTAL + ", " +
                Schema.Wallet.START_MONEY + " AS " + Contract.Wallet.START_MONEY + ", " +
                Schema.Wallet.NOTE + " AS " + Contract.Wallet.NOTE + ", " +
                Schema.Wallet.ARCHIVED + " AS " + Contract.Wallet.ARCHIVED + ", " +
                Schema.Wallet.INDEX + " AS " + Contract.Wallet.INDEX + ", " +
                Schema.Wallet.TAG + " AS " + Contract.Wallet.TAG + ", " +
                "total_money AS " + Contract.Wallet.TOTAL_MONEY + " FROM " + Schema.Wallet.TABLE +
                " LEFT JOIN (SELECT " + Schema.Transaction.WALLET + " AS _wallet," +
                " SUM(((" + Schema.Transaction.DIRECTION + " * 2) - 1) * " + Schema.Transaction.MONEY +
                ") AS total_money FROM " + Schema.Transaction.TABLE + " AS t WHERE t."
                + Schema.Transaction.DELETED + " = 0 AND " + Schema.Transaction.CONFIRMED + " = 1 AND " +
                Schema.Transaction.COUNT_IN_TOTAL + " = 1 AND DATETIME(" + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') GROUP BY _wallet) ON " + Schema.Wallet.ID +
                " = _wallet WHERE " + Schema.Wallet.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new wallet into
     * the database.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertWallet(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Wallet.NAME, contentValues.getAsString(Contract.Wallet.NAME));
        cv.put(Schema.Wallet.ICON, contentValues.getAsString(Contract.Wallet.ICON));
        cv.put(Schema.Wallet.CURRENCY, contentValues.getAsString(Contract.Wallet.CURRENCY));
        cv.put(Schema.Wallet.START_MONEY, contentValues.getAsLong(Contract.Wallet.START_MONEY));
        cv.put(Schema.Wallet.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Wallet.COUNT_IN_TOTAL));
        cv.put(Schema.Wallet.NOTE, contentValues.getAsString(Contract.Wallet.NOTE));
        if (contentValues.containsKey(Contract.Wallet.ARCHIVED)) {
            cv.put(Schema.Wallet.ARCHIVED, contentValues.getAsBoolean(Contract.Wallet.ARCHIVED));
        }
        Integer index = contentValues.getAsInteger(Contract.Wallet.INDEX);
        cv.put(Schema.Wallet.INDEX, index != null ? index : 0);
        cv.put(Schema.Wallet.TAG, contentValues.getAsString(Contract.Wallet.TAG));
        cv.put(Schema.Wallet.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Wallet.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Wallet.DELETED, false);
        return getWritableDatabase().insert(Schema.Wallet.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing wallet in
     * the database.
     *
     * @param walletId id of the wallet to update.
     * @param contentValues bundle that contains the data from the content provider.
     * @return the number of row updated inside the database.
     */
    /*package-local*/ int updateWallet(long walletId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Contract.Wallet.NAME)) {
            cv.put(Schema.Wallet.NAME, contentValues.getAsString(Contract.Wallet.NAME));
        }
        if (contentValues.containsKey(Contract.Wallet.ICON)) {
            cv.put(Schema.Wallet.ICON, contentValues.getAsString(Contract.Wallet.ICON));
        }
        if (contentValues.containsKey(Contract.Wallet.CURRENCY)) {
            cv.put(Schema.Wallet.CURRENCY, contentValues.getAsString(Contract.Wallet.CURRENCY));
            // TODO: check if the wallet is updating the currency and is part of a budget (currency consistency)
        }
        if (contentValues.containsKey(Contract.Wallet.START_MONEY)) {
            cv.put(Schema.Wallet.START_MONEY, contentValues.getAsLong(Contract.Wallet.START_MONEY));
        }
        if (contentValues.containsKey(Contract.Wallet.COUNT_IN_TOTAL)) {
            cv.put(Schema.Wallet.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Wallet.COUNT_IN_TOTAL));
        }
        if (contentValues.containsKey(Contract.Wallet.NOTE)) {
            cv.put(Schema.Wallet.NOTE, contentValues.getAsString(Contract.Wallet.NOTE));
        }
        if (contentValues.containsKey(Contract.Wallet.ARCHIVED)) {
            cv.put(Schema.Wallet.ARCHIVED, contentValues.getAsBoolean(Contract.Wallet.ARCHIVED));
        }
        if (contentValues.containsKey(Contract.Wallet.INDEX)) {
            Integer index = contentValues.getAsInteger(Contract.Wallet.INDEX);
            cv.put(Schema.Wallet.INDEX, index != null ? index : 0);
        }
        if (contentValues.containsKey(Contract.Wallet.TAG)) {
            cv.put(Schema.Wallet.TAG, contentValues.getAsString(Contract.Wallet.TAG));
        }
        cv.put(Schema.Wallet.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Wallet.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(walletId)};
        return getWritableDatabase().update(Schema.Wallet.TABLE, cv, where, whereArgs);
    }

    /**
     * This method is called by the content provider when the user is deleting a wallet from the
     * database. All the related data will be removed. The wallet cannot be deleted if it is in use
     * in a transfer.
     *
     * @param walletId id of the wallet to delete.
     * @return the number of rows affected by the deletion.
     * @throws SQLiteDataException if the wallet is in use in a transfer.
     */
    /*package-local*/ int deleteWallet(long walletId) {
        // we must checks that the wallet is not used in a transfer
        String query = "SELECT " + Schema.Transfer.ID + " FROM " + Schema.Transfer.TABLE + " JOIN " +
                Schema.Transaction.TABLE + " AS t1 ON " + Schema.Transfer.TRANSACTION_FROM + " = t1." +
                Schema.Transaction.ID + " AND t1." + Schema.Transaction.DELETED + " = 0 JOIN " +
                Schema.Transaction.TABLE + " AS t2 ON " + Schema.Transfer.TRANSACTION_TO + " = t2." +
                Schema.Transaction.ID + " AND t2." + Schema.Transaction.DELETED + " = 0 LEFT JOIN " +
                Schema.Transaction.TABLE + " AS t3 ON " + Schema.Transfer.TRANSACTION_TAX + " = t3." +
                Schema.Transaction.ID + " AND t3." + Schema.Transaction.DELETED + " = 0 WHERE t1." +
                Schema.Transaction.WALLET + " = ? OR t2." + Schema.Transaction.WALLET + " = ? OR t3." +
                Schema.Transaction.WALLET + " = ?";
        String[] args = new String[] {String.valueOf(walletId), String.valueOf(walletId), String.valueOf(walletId)};
        Cursor cursor = getReadableDatabase().rawQuery(query, args);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long transferId = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.ID));
                    throw new SQLiteDataException(Contract.ErrorCode.WALLET_USED_IN_TRANSFER,
                            String.format(Locale.ENGLISH, "The wallet (id: %d) cannot be deleted because is in use in a transfer (id: %d)", walletId, transferId));
                }
            } finally {
                cursor.close();
            }
        }
        // if this line is reached it means that the wallet can be deleted
        // we can start to delete all the transactions related to this wallet
        String where = Schema.Transaction.WALLET + " = ?";
        String[] whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Transaction.DELETED, true);
            cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.Transaction.TABLE, where, whereArgs);
        }
        // now we can delete all the transaction models
        where = Schema.TransactionModel.WALLET + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransactionModel.DELETED, true);
            cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransactionModel.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransactionModel.TABLE, where, whereArgs);
        }
        // now we can delete all the transfer models
        where = Schema.TransferModel.WALLET_FROM + " = ? OR " + Schema.TransferModel.WALLET_TO + " = ?";
        whereArgs = new String[]{String.valueOf(walletId), String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransferModel.DELETED, true);
            cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransferModel.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransferModel.TABLE, where, whereArgs);
        }
        // now we can delete all the recurrent transactions
        where = Schema.RecurrentTransaction.WALLET + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.RecurrentTransaction.DELETED, true);
            cv.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.RecurrentTransaction.TABLE, where, whereArgs);
        }
        // now we can delete all the recurrent transfers
        where = Schema.RecurrentTransfer.WALLET_FROM + " = ? OR " + Schema.RecurrentTransfer.WALLET_TO + " = ?";
        whereArgs = new String[]{String.valueOf(walletId), String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.RecurrentTransfer.DELETED, true);
            cv.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.RecurrentTransfer.TABLE, where, whereArgs);
        }
        // now we can delete all the savings
        where = Schema.Saving.WALLET + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Saving.DELETED, true);
            cv.put(Schema.Saving.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.Saving.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.Saving.TABLE, where, whereArgs);
        }
        // now we can delete all the debts
        where = Schema.Debt.WALLET + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Debt.DELETED, true);
            cv.put(Schema.Debt.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.Debt.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.Debt.TABLE, where, whereArgs);
        }
        // now we can delete all the budget wallets
        where = Schema.BudgetWallet.WALLET + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.BudgetWallet.DELETED, true);
            cv.put(Schema.BudgetWallet.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.BudgetWallet.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.BudgetWallet.TABLE, where, whereArgs);
        }
        // TODO: [MEDIUM] A budget that has only this wallet must be removed
        // finally we can delete the wallet itself
        where = Schema.Wallet.ID + " = ?";
        whereArgs = new String[]{String.valueOf(walletId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Wallet.DELETED, true);
            cv.put(Schema.Wallet.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Wallet.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Wallet.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying the database for a
     * specific id.
     *
     * @param id of the transaction to retrieve.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one rows.
     */
    /*package-local*/ Cursor getTransaction(long id, String[] projection) {
        String selection = Schema.Transaction.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getTransactions(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying the database for all
     * the transactions.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransactions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "t." + Schema.Transaction.ID + " AS " + Contract.Transaction.ID + ", " +
                "t." + Schema.Transaction.MONEY + " AS " + Contract.Transaction.MONEY + ", " +
                "t." + Schema.Transaction.DATE + " AS " + Contract.Transaction.DATE + ", " +
                "t." + Schema.Transaction.DESCRIPTION + " AS " + Contract.Transaction.DESCRIPTION + ", " +
                "t." + Schema.Transaction.CATEGORY + " AS " + Contract.Transaction.CATEGORY_ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.Transaction.CATEGORY_NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.Transaction.CATEGORY_ICON + ", " +
                "c." + Schema.Category.PARENT + " AS " + Contract.Transaction.CATEGORY_PARENT_ID + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.Transaction.CATEGORY_TYPE + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.Transaction.CATEGORY_TAG + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.Transaction.CATEGORY_SHOW_REPORT + ", " +
                "t." + Schema.Transaction.DIRECTION + " AS " + Contract.Transaction.DIRECTION + ", " +
                "t." + Schema.Transaction.TYPE + " AS " + Contract.Transaction.TYPE + ", " +
                "t." + Schema.Transaction.WALLET + " AS " + Contract.Transaction.WALLET_ID + ", " +
                "w." + Schema.Wallet.NAME + " AS " + Contract.Transaction.WALLET_NAME + ", " +
                "w." + Schema.Wallet.ICON + " AS " + Contract.Transaction.WALLET_ICON + ", " +
                "w." + Schema.Wallet.CURRENCY + " AS " + Contract.Transaction.WALLET_CURRENCY + ", " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Transaction.WALLET_COUNT_IN_TOTAL + ", " +
                "w." + Schema.Wallet.ARCHIVED + " AS " + Contract.Transaction.WALLET_ARCHIVED + ", " +
                "w." + Schema.Wallet.TAG + " AS " + Contract.Transaction.WALLET_TAG + ", " +
                "t." + Schema.Transaction.PLACE + " AS " + Contract.Transaction.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.Transaction.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.Transaction.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.Transaction.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.Transaction.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.Transaction.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.Transaction.PLACE_TAG + ", " +
                "t." + Schema.Transaction.EVENT + " AS " + Contract.Transaction.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.Transaction.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.Transaction.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.Transaction.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.Transaction.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.Transaction.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.Transaction.EVENT_TAG + ", " +
                "t." + Schema.Transaction.NOTE + " AS " + Contract.Transaction.NOTE + ", " +
                "t." + Schema.Transaction.DEBT + " AS " + Contract.Transaction.DEBT_ID + ", " +
                "t." + Schema.Transaction.SAVING + " AS " + Contract.Transaction.SAVING_ID + ", " +
                "t." + Schema.Transaction.RECURRENCE + " AS " + Contract.Transaction.RECURRENCE_ID + ", " +
                "t." + Schema.Transaction.CONFIRMED + " AS " + Contract.Transaction.CONFIRMED + ", " +
                "t." + Schema.Transaction.COUNT_IN_TOTAL + " AS " + Contract.Transaction.COUNT_IN_TOTAL + ", " +
                "t." + Schema.Transaction.TAG + " AS " + Contract.Transaction.TAG + ", " +
                "GROUP_CONCAT('<' || pe." + Schema.Person.ID + " || '>') AS " + Contract.Transaction.PEOPLE_IDS + " " +
                "FROM " + Schema.Transaction.TABLE + " AS t LEFT JOIN " + Schema.Category.TABLE +
                " AS c ON t." + Schema.Transaction.CATEGORY + " = c." + Schema.Category.ID + " AND c." +
                Schema.Category.DELETED + " = 0 JOIN " + Schema.Wallet.TABLE + " AS w ON t." +
                Schema.Transaction.WALLET + " = w." + Schema.Wallet.ID + " AND w." +
                Schema.Wallet.DELETED + " = 0 LEFT JOIN " + Schema.TransactionPeople.TABLE + " AS tp ON t." +
                Schema.Transaction.ID + " = tp." + Schema.TransactionPeople.TRANSACTION + " AND tp." +
                Schema.TransactionPeople.DELETED + " = 0 LEFT JOIN " + Schema.Person.TABLE + " AS pe ON tp." +
                Schema.TransactionPeople.PERSON + " = pe." + Schema.Person.ID + " AND pe." +
                Schema.Person.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON t." +
                Schema.Transaction.PLACE + " = " + Schema.Place.ID + " AND p." + Schema.Place.DELETED +
                " = 0 LEFT JOIN " + Schema.Event.TABLE + " AS e ON t." + Schema.Transaction.EVENT +
                " = e." + Schema.Event.ID + " AND e." + Schema.Event.DELETED + " = 0 WHERE t." +
                Schema.Transaction.DELETED + " = 0 GROUP BY t." + Schema.Transaction.ID;
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new transaction
     * inside the database.
     *
     * @param contentValues object which contains all the column values.
     * @return the id of the created transaction if success, -1 if failure.
     */
    /*package-local*/ long insertTransaction(ContentValues contentValues) {
        // if this is a transaction linked to a recurrence, we need to correctly calculate
        // the uuid of the transaction to be uniquely identified across different devices
        String transactionUUID = UUID.randomUUID().toString();
        long lastEdit = System.currentTimeMillis();
        if (contentValues.containsKey(Contract.Transaction.RECURRENCE_ID)) {
            long recurrenceId = contentValues.getAsLong(Contract.Transaction.RECURRENCE_ID);
            String table = Schema.RecurrentTransaction.TABLE;
            String[] projection = new String[] {Schema.RecurrentTransaction.UUID};
            String selection = Schema.RecurrentTransaction.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(recurrenceId)};
            Cursor cursor = getReadableDatabase().query(table, projection, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String recurrenceUUID = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.UUID));
                    Date date = DateUtils.getDateFromSQLDateTimeString(contentValues.getAsString(Contract.Transaction.DATE));
                    transactionUUID = getRecurrentItemUUID(recurrenceUUID, date);
                    lastEdit = date.getTime();
                }
                cursor.close();
            }
        }
        // insert the body of the transaction inside the database
        ContentValues cv = new ContentValues();
        cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transaction.MONEY));
        cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transaction.DATE));
        cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transaction.DESCRIPTION));
        cv.put(Schema.Transaction.CATEGORY, contentValues.getAsLong(Contract.Transaction.CATEGORY_ID));
        cv.put(Schema.Transaction.DIRECTION, contentValues.getAsInteger(Contract.Transaction.DIRECTION));
        cv.put(Schema.Transaction.TYPE, contentValues.getAsInteger(Contract.Transaction.TYPE));
        cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transaction.WALLET_ID));
        cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transaction.PLACE_ID));
        cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transaction.NOTE));
        cv.put(Schema.Transaction.SAVING, contentValues.getAsLong(Contract.Transaction.SAVING_ID));
        cv.put(Schema.Transaction.DEBT, contentValues.getAsLong(Contract.Transaction.DEBT_ID));
        cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transaction.EVENT_ID));
        cv.put(Schema.Transaction.RECURRENCE, contentValues.getAsLong(Contract.Transaction.RECURRENCE_ID));
        cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transaction.CONFIRMED));
        cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transaction.COUNT_IN_TOTAL));
        cv.put(Schema.Transaction.TAG, contentValues.getAsString(Contract.Transaction.TAG));
        cv.put(Schema.Transaction.UUID, transactionUUID);
        cv.put(Schema.Transaction.LAST_EDIT, lastEdit);
        cv.put(Schema.Transaction.DELETED, false);
        long transactionId = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cv);
        if (transactionId > 0L) {
            // now we need to parse the content values string that may contains the ids of the people
            long[] peopleIds = parseIds(contentValues.getAsString(Contract.Transaction.PEOPLE_IDS));
            if (peopleIds != null) {
                for (long personId : peopleIds) {
                    ContentValues cvp = new ContentValues();
                    cvp.put(Schema.TransactionPeople.TRANSACTION, transactionId);
                    cvp.put(Schema.TransactionPeople.PERSON, personId);
                    cvp.put(Schema.TransactionPeople.UUID, UUID.randomUUID().toString());
                    cvp.put(Schema.TransactionPeople.LAST_EDIT, System.currentTimeMillis());
                    cvp.put(Schema.TransactionPeople.DELETED, false);
                    getWritableDatabase().insert(Schema.TransactionPeople.TABLE, null, cvp);
                }
            }
            // we need to do the same for the attachments
            long[] attachmentsIds = parseIds(contentValues.getAsString(Contract.Transaction.ATTACHMENT_IDS));
            if (attachmentsIds != null) {
                for (long attachmentId : attachmentsIds) {
                    ContentValues cva = new ContentValues();
                    cva.put(Schema.TransactionAttachment.TRANSACTION, transactionId);
                    cva.put(Schema.TransactionAttachment.ATTACHMENT, attachmentId);
                    cva.put(Schema.TransactionAttachment.UUID, UUID.randomUUID().toString());
                    cva.put(Schema.TransactionAttachment.LAST_EDIT, System.currentTimeMillis());
                    cva.put(Schema.TransactionAttachment.DELETED, false);
                    getWritableDatabase().insert(Schema.TransactionAttachment.TABLE, null, cva);
                }
            }
        }
        return transactionId;
    }

    /**
     * This method is called by the content provider when the user is querying all the attachments
     * of a specific transaction.
     *
     * @param id of the transaction.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransactionAttachments(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "a." + Schema.Attachment.ID + " AS " + Contract.Attachment.ID + ", " +
                "a." + Schema.Attachment.FILE + " AS " + Contract.Attachment.FILE + ", " +
                "a." + Schema.Attachment.NAME + " AS " + Contract.Attachment.NAME + ", " +
                "a." + Schema.Attachment.TYPE + " AS " + Contract.Attachment.TYPE + ", " +
                "a." + Schema.Attachment.SIZE + " AS " + Contract.Attachment.SIZE + ", " +
                "a." + Schema.Attachment.TAG + " AS " + Contract.Attachment.TAG + " " +
                "FROM " + Schema.TransactionAttachment.TABLE + " AS ta JOIN " + Schema.Attachment.TABLE + " AS a " +
                "ON ta." + Schema.TransactionAttachment.ATTACHMENT + " = a." + Schema.Attachment.ID + " AND ta." +
                Schema.TransactionAttachment.DELETED + " = 0 AND a." + Schema.Attachment.DELETED + " = 0 AND " +
                Schema.TransactionAttachment.TRANSACTION + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the people
     * of a specific transaction.
     *
     * @param id of the transaction.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransactionPeople(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Person.ID + " AS " + Contract.Person.ID + ", " +
                Schema.Person.NAME + " AS " + Contract.Person.NAME + ", " +
                Schema.Person.ICON + " AS " + Contract.Person.ICON + ", " +
                Schema.Person.NOTE + " AS " + Contract.Person.NOTE + ", " +
                Schema.Person.TAG + " AS " + Contract.Person.TAG + " " +
                "FROM " + Schema.TransactionPeople.TABLE + " AS dp JOIN " + Schema.Person.TABLE + " AS p " +
                "ON dp." + Schema.TransactionPeople.PERSON + " = p." + Schema.Person.ID + " AND dp." +
                Schema.TransactionPeople.DELETED + " = 0 AND p." + Schema.Person.DELETED + " = 0 AND " +
                Schema.TransactionPeople.TRANSACTION + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is updating a specific transaction.
     * A transfer transaction cannot be updated directly, it must be updated updating the related
     * transfer item.
     *
     * @param transactionId id of the transaction to update.
     * @param contentValues set of column values to update.
     * @return the number of rows affected by the update.
     * @throws SQLiteDataException if the transaction is part of a transfer.
     */
    /*package-local*/ int updateTransaction(long transactionId, ContentValues contentValues) {
        // we must checks that the transaction is not part of a transfer
        String[] projection = new String[]{Schema.Transfer.ID};
        String where = Schema.Transfer.TRANSACTION_FROM + " = ? OR " + Schema.Transfer.TRANSACTION_TO +
                " = ? OR " + Schema.Transfer.TRANSACTION_TAX + " = ? AND " + Schema.Transfer.DELETED + " = 0";
        String[] whereArgs = new String[]{String.valueOf(transactionId), String.valueOf(transactionId), String.valueOf(transactionId)};
        Cursor cursor = getReadableDatabase().query(Schema.Transfer.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long transferId = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.ID));
                    throw new SQLiteDataException(Contract.ErrorCode.TRANSACTION_USED_IN_TRANSFER,
                            String.format(Locale.ENGLISH, "The transaction (id: %d) cannot be updated because it is part of a transfer (id: %d)", transactionId, transferId));
                }
            } finally {
                cursor.close();
            }
        }
        // if this line is reached it means that the transaction can be updated
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Contract.Transaction.MONEY)) {
            cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transaction.MONEY));
        }
        if (contentValues.containsKey(Contract.Transaction.DATE)) {
            cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transaction.DATE));
        }
        if (contentValues.containsKey(Contract.Transaction.DESCRIPTION)) {
            cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transaction.DESCRIPTION));
        }
        if (contentValues.containsKey(Contract.Transaction.CATEGORY_ID)) {
            cv.put(Schema.Transaction.CATEGORY, contentValues.getAsLong(Contract.Transaction.CATEGORY_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.DIRECTION)) {
            cv.put(Schema.Transaction.DIRECTION, contentValues.getAsInteger(Contract.Transaction.DIRECTION));
        }
        if (contentValues.containsKey(Contract.Transaction.WALLET_ID)) {
            cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transaction.WALLET_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.PLACE_ID)) {
            cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transaction.PLACE_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.NOTE)) {
            cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transaction.NOTE));
        }
        if (contentValues.containsKey(Contract.Transaction.SAVING_ID)) {
            cv.put(Schema.Transaction.SAVING, contentValues.getAsLong(Contract.Transaction.SAVING_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.DEBT_ID)) {
            cv.put(Schema.Transaction.DEBT, contentValues.getAsLong(Contract.Transaction.DEBT_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.EVENT_ID)) {
            cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transaction.EVENT_ID));
        }
        if (contentValues.containsKey(Contract.Transaction.CONFIRMED)) {
            cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transaction.CONFIRMED));
        }
        if (contentValues.containsKey(Contract.Transaction.COUNT_IN_TOTAL)) {
            cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transaction.COUNT_IN_TOTAL));
        }
        if (contentValues.containsKey(Contract.Transaction.TAG)) {
            cv.put(Schema.Transaction.TAG, contentValues.getAsString(Contract.Transaction.TAG));
        }
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Transaction.ID + " = ?";
        whereArgs = new String[]{String.valueOf(transactionId)};
        int rows = getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        if (rows > 0) {
            // In this case we have to check if the new provided people ids is changed. We could
            // query the current list, compute two sets containing the items to flag as deleted and
            // and the items to add but it is not enough fast. We can flag all the current items as
            // deleted and then add all the ids as new items but checking for conflict. In case of
            // conflict (same <transactionId,personId> tuple) we update the deleted flag only.
            if (contentValues.containsKey(Contract.Transaction.PEOPLE_IDS)) {
                cv = new ContentValues();
                cv.put(Schema.TransactionPeople.DELETED, true);
                where = Schema.TransactionPeople.TRANSACTION + " = ?";
                whereArgs = new String[]{String.valueOf(transactionId)};
                getWritableDatabase().update(Schema.TransactionPeople.TABLE, cv, where, whereArgs);
                // All the current people associated with this debt are flagged as deleted, now it's
                // time to add (checking for conflicts) the new ids.
                long[] peopleIds = parseIds(contentValues.getAsString(Contract.Transaction.PEOPLE_IDS));
                if (peopleIds != null) {
                    for (long personId : peopleIds) {
                        cv = new ContentValues();
                        cv.put(Schema.TransactionPeople.TRANSACTION, transactionId);
                        cv.put(Schema.TransactionPeople.PERSON, personId);
                        cv.put(Schema.TransactionPeople.UUID, UUID.randomUUID().toString());
                        cv.put(Schema.TransactionPeople.LAST_EDIT, System.currentTimeMillis());
                        cv.put(Schema.TransactionPeople.DELETED, false);
                        long newId = getWritableDatabase().insertWithOnConflict(Schema.TransactionPeople.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                        if (newId == -1L) {
                            // In this case the tuple <transactionId,personId> already exists inside the table!
                            // We have to simply update the deleted flag and the update timestamp.
                            cv.clear();
                            cv.put(Schema.TransactionPeople.LAST_EDIT, System.currentTimeMillis());
                            cv.put(Schema.TransactionPeople.DELETED, false);
                            where = Schema.TransactionPeople.TRANSACTION + " = ? AND " + Schema.TransactionPeople.PERSON + " = ?";
                            whereArgs = new String[]{String.valueOf(transactionId), String.valueOf(personId)};
                            getWritableDatabase().update(Schema.TransactionPeople.TABLE, cv, where, whereArgs);
                        }
                    }
                }
            }
            // we need to do the same for the attachments. the file itself is deleted by the
            // attachmentPicker as soon as it is no more required when the update is performed.
            if (contentValues.containsKey(Contract.Transaction.ATTACHMENT_IDS)) {
                cv = new ContentValues();
                cv.put(Schema.TransactionAttachment.DELETED, true);
                where = Schema.TransactionAttachment.TRANSACTION + " = ?";
                whereArgs = new String[]{String.valueOf(transactionId)};
                getWritableDatabase().update(Schema.TransactionAttachment.TABLE, cv, where, whereArgs);
                // reinsert or simply flag as not deleted the already existing attachment links
                long[] attachmentIds = parseIds(contentValues.getAsString(Contract.Transaction.ATTACHMENT_IDS));
                if (attachmentIds != null) {
                    for (long attachmentId : attachmentIds) {
                        cv = new ContentValues();
                        cv.put(Schema.TransactionAttachment.TRANSACTION, transactionId);
                        cv.put(Schema.TransactionAttachment.ATTACHMENT, attachmentId);
                        cv.put(Schema.TransactionAttachment.UUID, UUID.randomUUID().toString());
                        cv.put(Schema.TransactionAttachment.LAST_EDIT, System.currentTimeMillis());
                        cv.put(Schema.TransactionAttachment.DELETED, false);
                        long newId = getWritableDatabase().insertWithOnConflict(Schema.TransactionAttachment.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                        if (newId == -1L) {
                            // In this case the tuple <transactionId,attachmentId> already exists inside the table!
                            // We have to simply update the deleted flag and the update timestamp.
                            cv.clear();
                            cv.put(Schema.TransactionAttachment.LAST_EDIT, System.currentTimeMillis());
                            cv.put(Schema.TransactionAttachment.DELETED, false);
                            where = Schema.TransactionAttachment.TRANSACTION + " = ? AND " + Schema.TransactionAttachment.ATTACHMENT + " = ?";
                            whereArgs = new String[]{String.valueOf(transactionId), String.valueOf(attachmentId)};
                            getWritableDatabase().update(Schema.TransactionAttachment.TABLE, cv, where, whereArgs);
                        }
                    }
                }
            }
            if (!mCacheDeletedObjects) {
                // remove all deleted TransactionPeople
                where = Schema.TransactionPeople.TRANSACTION + " = ? AND " + Schema.TransactionPeople.DELETED + " = 1";
                whereArgs = new String[]{String.valueOf(transactionId)};
                getWritableDatabase().delete(Schema.TransactionPeople.TABLE, where, whereArgs);
                // remove all deleted TransactionAttachment
                where = Schema.TransactionAttachment.TRANSACTION + " = ? AND " + Schema.TransactionAttachment.DELETED + " = 1";
                whereArgs = new String[]{String.valueOf(transactionId)};
                getWritableDatabase().delete(Schema.TransactionAttachment.TABLE, where, whereArgs);
            }
        }
        return rows;
    }

    /**
     * Delete a transaction from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted. The transaction is NOT removed if part of a transfer.
     * @param transactionId id of the transaction to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     * @throws SQLiteDataException if the transaction is part of a transfer.
     */
    /*package-local*/ int deleteTransaction(long transactionId) {
        // we must checks that the transaction is not part of a transfer
        String[] projection = new String[]{Schema.Transfer.ID};
        String where = Schema.Transfer.TRANSACTION_FROM + " = ? OR " + Schema.Transfer.TRANSACTION_TO +
                " = ? OR " + Schema.Transfer.TRANSACTION_TAX + " = ? AND " + Schema.Transfer.DELETED + " = 0";
        String[] whereArgs = new String[]{String.valueOf(transactionId), String.valueOf(transactionId), String.valueOf(transactionId)};
        Cursor cursor = getReadableDatabase().query(Schema.Transfer.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long transferId = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.ID));
                    throw new SQLiteDataException(Contract.ErrorCode.TRANSACTION_USED_IN_TRANSFER,
                            String.format(Locale.ENGLISH, "The transaction (id: %d) cannot be deleted because it is part of a transfer (id: %d)", transactionId, transferId));
                }
            } finally {
                cursor.close();
            }
        }
        // if this line is reached it means that the transaction can be deleted
        return deleteTransactionItem(transactionId);
    }

    /**
     * This method is used internally to remove a transaction and all the related data. No checks
     * will be done to detect if this transaction is part of a transfer.
     *
     * @param transactionId id of the transaction to remove.
     * @return the number of row affected.
     */
    private int deleteTransactionItem(long transactionId) {
        // remove all TransactionPeople
        String where = Schema.TransactionPeople.TRANSACTION + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransactionPeople.DELETED, true);
            cv.put(Schema.TransactionPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransactionPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransactionPeople.TABLE, where, whereArgs);
        }
        // now remove all TransactionAttachment
        where = Schema.TransactionAttachment.TRANSACTION + " = ?";
        whereArgs = new String[]{String.valueOf(transactionId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransactionAttachment.DELETED, true);
            cv.put(Schema.TransactionAttachment.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransactionAttachment.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransactionAttachment.TABLE, where, whereArgs);
        }
        // finally we can remove the transfer item
        where = Schema.Transaction.ID + " = ?";
        whereArgs = new String[]{String.valueOf(transactionId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Transaction.DELETED, true);
            cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Transaction.TABLE, where, whereArgs);
        }
    }

    private int deleteTransactionItems(Cursor cursor) {
        // iterate the cursor and cache the ids of the transaction to remove
        List<Long> cache = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                cache.add(cursor.getLong(cursor.getColumnIndex(Contract.Transaction.ID)));
            }
        } finally {
            cursor.close();
        }
        // now iterate the cache and delete the transactions
        int rows = 0;
        for (Long transactionId : cache) {
            rows += deleteTransaction(transactionId);
        }
        return rows;
    }

    /**
     * This method is called by the content provider when the user is querying for a specific transfer
     * id from the database.
     *
     * @param id of the transfer.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getTransfer(long id, String[] projection) {
        String selection = Schema.Transfer.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getTransfers(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the transfers from
     * the database.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransfers(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "t." + Schema.Transfer.ID + " AS " + Contract.Transfer.ID + ", " +
                "t." + Schema.Transfer.DESCRIPTION + " AS " + Contract.Transfer.DESCRIPTION + ", " +
                "t." + Schema.Transfer.DATE + " AS " + Contract.Transfer.DATE + ", " +
                "t." + Schema.Transfer.TRANSACTION_FROM + " AS " + Contract.Transfer.TRANSACTION_FROM_ID + ", " +
                "t1." + Schema.Transaction.MONEY + " AS " + Contract.Transfer.TRANSACTION_FROM_MONEY + ", " +
                "w1." + Schema.Wallet.ID + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_ID + ", " +
                "w1." + Schema.Wallet.NAME + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_NAME + ", " +
                "w1." + Schema.Wallet.ICON + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_ICON + ", " +
                "w1." + Schema.Wallet.CURRENCY + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_CURRENCY + ", " +
                "w1." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_COUNT_IN_TOTAL + ", " +
                "w1." + Schema.Wallet.ARCHIVED + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_ARCHIVED + ", " +
                "w1." + Schema.Wallet.TAG + " AS " + Contract.Transfer.TRANSACTION_FROM_WALLET_TAG + ", " +
                "t1." + Schema.Transaction.TAG + " AS " + Contract.Transfer.TRANSACTION_FROM_TAG + ", " +
                "t." + Schema.Transfer.TRANSACTION_TO + " AS " + Contract.Transfer.TRANSACTION_TO_ID + ", " +
                "t2." + Schema.Transaction.MONEY + " AS " + Contract.Transfer.TRANSACTION_TO_MONEY + ", " +
                "w2." + Schema.Wallet.ID + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_ID + ", " +
                "w2." + Schema.Wallet.NAME + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_NAME + ", " +
                "w2." + Schema.Wallet.ICON + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_ICON + ", " +
                "w2." + Schema.Wallet.CURRENCY + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_CURRENCY + ", " +
                "w2." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_COUNT_IN_TOTAL + ", " +
                "w2." + Schema.Wallet.ARCHIVED + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_ARCHIVED + ", " +
                "w2." + Schema.Wallet.TAG + " AS " + Contract.Transfer.TRANSACTION_TO_WALLET_TAG + ", " +
                "t2." + Schema.Transaction.TAG + " AS " + Contract.Transfer.TRANSACTION_TO_TAG + ", " +
                "t." + Schema.Transfer.TRANSACTION_TAX + " AS " + Contract.Transfer.TRANSACTION_TAX_ID + ", " +
                "t3." + Schema.Transaction.MONEY + " AS " + Contract.Transfer.TRANSACTION_TAX_MONEY + ", " +
                "w3." + Schema.Wallet.ID + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_ID + ", " +
                "w3." + Schema.Wallet.NAME + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_NAME + ", " +
                "w3." + Schema.Wallet.CURRENCY + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_CURRENCY + ", " +
                "w3." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_COUNT_IN_TOTAL + ", " +
                "w3." + Schema.Wallet.ARCHIVED + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_ARCHIVED + ", " +
                "w3." + Schema.Wallet.TAG + " AS " + Contract.Transfer.TRANSACTION_TAX_WALLET_TAG + ", " +
                "t3." + Schema.Transaction.TAG + " AS " + Contract.Transfer.TRANSACTION_TAX_TAG + ", " +
                "t." + Schema.Transfer.NOTE + " AS " + Contract.Transfer.NOTE + ", " +
                "t." + Schema.Transfer.PLACE + " AS " + Contract.Transfer.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.Transfer.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.Transfer.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.Transfer.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.Transfer.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.Transfer.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.Transfer.PLACE_TAG + ", " +
                "t." + Schema.Transfer.EVENT + " AS " + Contract.Transfer.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.Transfer.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.Transfer.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.Transfer.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.Transfer.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.Transfer.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.Transfer.EVENT_TAG + ", " +
                "t." + Schema.Transfer.RECURRENCE + " AS " + Contract.Transfer.RECURRENCE_ID + ", " +
                "t." + Schema.Transfer.CONFIRMED + " AS " + Contract.Transfer.CONFIRMED + ", " +
                "t." + Schema.Transfer.COUNT_IN_TOTAL + " AS " + Contract.Transfer.COUNT_IN_TOTAL + ", " +
                "t." + Schema.Transfer.TAG + " AS " + Contract.Transfer.TAG + ", " +
                "GROUP_CONCAT('<' || pe." + Schema.Person.ID + " || '>') AS " + Contract.Transfer.PEOPLE_IDS + " " +
                "FROM transfers AS t \n" +
                "JOIN transactions AS t1 ON transfer_transaction_from = t1.transaction_id AND t1.deleted = 0 \n" +
                "JOIN wallets AS w1 ON t1.transaction_wallet = w1.wallet_id AND w1.deleted = 0\n" +
                "JOIN transactions AS t2 ON transfer_transaction_to = t2.transaction_id AND t2.deleted = 0\n" +
                "JOIN wallets AS w2 ON t2.transaction_wallet = w2.wallet_id AND w2.deleted = 0\n" +
                "LEFT JOIN transactions AS t3 ON transfer_transaction_tax = t3.transaction_id AND t3.deleted = 0\n" +
                "LEFT JOIN wallets AS w3 ON t3.transaction_wallet = w3.wallet_id AND w3.deleted = 0\n" +
                "LEFT JOIN transfer_people AS tp ON t.transfer_id = tp._transfer AND tp.deleted = 0\n" +
                "LEFT JOIN people AS pe ON tp._person = pe.person_id AND pe.deleted = 0\n" +
                "LEFT JOIN places AS p ON transfer_place = place_id AND p.deleted = 0 \n" +
                "LEFT JOIN events AS e ON transfer_event = event_id AND e.deleted = 0 \n" +
                "WHERE t.deleted = 0\n" +
                "GROUP BY t.transfer_id";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is used internally to obtain the ids of the transactions that are part of the
     * given transfer id.
     *
     * @param transferId id of the transfer.
     * @return an array of length 3 that contains the ids of the requested transaction. All the cells
     * are null if the transfer not exists. The first cell contains the id of the transaction_from,
     * the second cell contains the id of the transaction_to and the third cell contains the id of
     * the transaction_tax if provided, null otherwise.
     */
    private Long[] getTransferTransactionIds(long transferId) {
        Long[] transactionIds = new Long[3];
        String[] projection = new String[] {
                Schema.Transfer.TRANSACTION_FROM,
                Schema.Transfer.TRANSACTION_TO,
                Schema.Transfer.TRANSACTION_TAX
        };
        String selection = Schema.Transfer.ID + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(transferId)};
        Cursor cursor = getTransfers(projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                transactionIds[0] = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_FROM));
                transactionIds[1] = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TO));
                if (!cursor.isNull(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TAX))) {
                    transactionIds[2] = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TAX));
                } else {
                    transactionIds[2] = null;
                }
            }
            cursor.close();
        }
        return transactionIds;
    }

    /**
     * This method is called by the content provider when the user is querying all the attachments of
     * a given transfer.
     *
     * @param id of the transfer.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransferAttachments(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "a." + Schema.Attachment.ID + " AS " + Contract.Attachment.ID + ", " +
                "a." + Schema.Attachment.FILE + " AS " + Contract.Attachment.FILE + ", " +
                "a." + Schema.Attachment.NAME + " AS " + Contract.Attachment.NAME + ", " +
                "a." + Schema.Attachment.TYPE + " AS " + Contract.Attachment.TYPE + ", " +
                "a." + Schema.Attachment.SIZE + " AS " + Contract.Attachment.SIZE + ", " +
                "a." + Schema.Attachment.TAG + " AS " + Contract.Attachment.TAG + " " +
                "FROM " + Schema.TransferAttachment.TABLE + " AS ta JOIN " + Schema.Attachment.TABLE + " AS a " +
                "ON ta." + Schema.TransferAttachment.ATTACHMENT + " = a." + Schema.Attachment.ID + " AND ta." +
                Schema.TransferAttachment.DELETED + " = 0 AND a." + Schema.Attachment.DELETED + " = 0 AND " +
                Schema.TransferAttachment.TRANSFER + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the people of
     * a given transfer.
     *
     * @param id of the transfer.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransferPeople(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Person.ID + " AS " + Contract.Person.ID + ", " +
                Schema.Person.NAME + " AS " + Contract.Person.NAME + ", " +
                Schema.Person.ICON + " AS " + Contract.Person.ICON + ", " +
                Schema.Person.NOTE + " AS " + Contract.Person.NOTE + ", " +
                Schema.Person.TAG + " AS " + Contract.Person.TAG + " " +
                "FROM " + Schema.TransferPeople.TABLE + " AS dp JOIN " + Schema.Person.TABLE + " AS p " +
                "ON dp." + Schema.TransferPeople.PERSON + " = p." + Schema.Person.ID + " AND dp." +
                Schema.TransferPeople.DELETED + " = 0 AND p." + Schema.Person.DELETED + " = 0 AND " +
                Schema.TransferPeople.TRANSFER + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new transfer inside
     * the database.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertTransfer(ContentValues contentValues) {
        Long transferCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER);
        Long transferTaxCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER_TAX);
        // if this is a transaction linked to a recurrence, we need to correctly calculate
        // the uuid of the transaction to be uniquely identified across different devices
        String transferUUID = UUID.randomUUID().toString();
        long lastEdit = System.currentTimeMillis();
        if (contentValues.containsKey(Contract.Transfer.RECURRENCE_ID)) {
            long recurrenceId = contentValues.getAsLong(Contract.Transfer.RECURRENCE_ID);
            String table = Schema.RecurrentTransfer.TABLE;
            String[] projection = new String[] {Schema.RecurrentTransfer.UUID};
            String selection = Schema.RecurrentTransfer.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(recurrenceId)};
            Cursor cursor = getReadableDatabase().query(table, projection, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String recurrenceUUID = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.UUID));
                    Date date = DateUtils.getDateFromSQLDateTimeString(contentValues.getAsString(Contract.Transfer.DATE));
                    transferUUID = getRecurrentItemUUID(recurrenceUUID, date);
                    lastEdit = date.getTime();
                }
                cursor.close();
            }
        }
        // source transaction
        ContentValues cv = new ContentValues();
        cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_FROM_MONEY));
        cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transaction.CATEGORY, transferCategoryId);
        cv.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
        cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
        cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_FROM_WALLET_ID));
        cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.putNull(Schema.Transaction.SAVING);
        cv.putNull(Schema.Transaction.DEBT);
        cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        cv.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Transaction.DELETED, false);
        long transaction1 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cv);
        // destination transaction
        cv = new ContentValues();
        cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TO_MONEY));
        cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transaction.CATEGORY, transferCategoryId);
        cv.put(Schema.Transaction.DIRECTION, Contract.Direction.INCOME);
        cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
        cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TO_WALLET_ID));
        cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.putNull(Schema.Transaction.SAVING);
        cv.putNull(Schema.Transaction.DEBT);
        cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        cv.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Transaction.DELETED, false);
        long transaction2 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cv);
        // tax transaction
        Long transaction3 = null;
        if (contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY) != 0L) {
            cv = new ContentValues();
            cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY));
            cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
            cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
            cv.put(Schema.Transaction.CATEGORY, transferTaxCategoryId);
            cv.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
            cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
            cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_WALLET_ID));
            cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
            cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
            cv.putNull(Schema.Transaction.SAVING);
            cv.putNull(Schema.Transaction.DEBT);
            cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
            cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
            cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
            cv.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
            cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
            cv.put(Schema.Transaction.DELETED, false);
            transaction3 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cv);
        }
        // now we can add the transfer tuple
        cv = new ContentValues();
        cv.put(Schema.Transfer.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transfer.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transfer.TRANSACTION_FROM, transaction1);
        cv.put(Schema.Transfer.TRANSACTION_TO, transaction2);
        cv.put(Schema.Transfer.TRANSACTION_TAX, transaction3);
        cv.put(Schema.Transfer.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.put(Schema.Transfer.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transfer.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transfer.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transfer.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        cv.put(Schema.Transfer.TAG, contentValues.getAsString(Contract.Transfer.TAG));
        cv.put(Schema.Transfer.UUID, transferUUID);
        cv.put(Schema.Transfer.LAST_EDIT, lastEdit);
        cv.put(Schema.Transfer.DELETED, false);
        long transferId = getWritableDatabase().insert(Schema.Transfer.TABLE, null, cv);
        if (transferId > 0) {
            long[] peopleIds = parseIds(contentValues.getAsString(Contract.Transfer.PEOPLE_IDS));
            if (peopleIds != null) {
                for (long personId : peopleIds) {
                    ContentValues cvp = new ContentValues();
                    cvp.put(Schema.TransferPeople.TRANSFER, transferId);
                    cvp.put(Schema.TransferPeople.PERSON, personId);
                    cvp.put(Schema.TransferPeople.UUID, UUID.randomUUID().toString());
                    cvp.put(Schema.TransferPeople.LAST_EDIT, System.currentTimeMillis());
                    cvp.put(Schema.TransferPeople.DELETED, false);
                    getWritableDatabase().insert(Schema.TransferPeople.TABLE, null, cvp);
                }
            }
            // we need to do the same for the attachments
            long[] attachmentsIds = parseIds(contentValues.getAsString(Contract.Transfer.ATTACHMENT_IDS));
            if (attachmentsIds != null) {
                for (long attachmentId : attachmentsIds) {
                    ContentValues cva = new ContentValues();
                    cva.put(Schema.TransferAttachment.TRANSFER, transferId);
                    cva.put(Schema.TransferAttachment.ATTACHMENT, attachmentId);
                    cva.put(Schema.TransferAttachment.UUID, UUID.randomUUID().toString());
                    cva.put(Schema.TransferAttachment.LAST_EDIT, System.currentTimeMillis());
                    cva.put(Schema.TransferAttachment.DELETED, false);
                    getWritableDatabase().insert(Schema.TransferAttachment.TABLE, null, cva);
                }
            }
        }
        return transferId;
    }

    /**
     * This method is called by the content provider when the user is updating an existing transfer
     * from the database.
     *
     * @param transferId id of the transfer to update.
     * @param contentValues bundle that contains the values to update.
     * @return the the number of row affected by the update.
     */
    /*package-local*/ int updateTransfer(long transferId, ContentValues contentValues) {
        Long transferCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER);
        Long transferTaxCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER_TAX);
        // first of all we need to retrieve the transfer and the id of every transaction
        Long[] transactionIds = getTransferTransactionIds(transferId);
        // source transaction
        ContentValues cv = new ContentValues();
        cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_FROM_MONEY));
        cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transaction.CATEGORY, transferCategoryId);
        cv.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
        cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
        cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_FROM_WALLET_ID));
        cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.putNull(Schema.Transaction.SAVING);
        cv.putNull(Schema.Transaction.DEBT);
        cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Transaction.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transactionIds[0])};
        getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        // destination transaction
        cv = new ContentValues();
        cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TO_MONEY));
        cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transaction.CATEGORY, transferCategoryId);
        cv.put(Schema.Transaction.DIRECTION, Contract.Direction.INCOME);
        cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
        cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TO_WALLET_ID));
        cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.putNull(Schema.Transaction.SAVING);
        cv.putNull(Schema.Transaction.DEBT);
        cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Transaction.ID + " = ?";
        whereArgs = new String[]{String.valueOf(transactionIds[1])};
        getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        // tax transaction: handle all possible cases
        if (transactionIds[2] != null) {
            if (contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY) != 0L) {
                cv = new ContentValues();
                cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY));
                cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
                cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
                cv.put(Schema.Transaction.CATEGORY, transferTaxCategoryId);
                cv.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
                cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
                cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_WALLET_ID));
                cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
                cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
                cv.putNull(Schema.Transaction.SAVING);
                cv.putNull(Schema.Transaction.DEBT);
                cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
                cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
                cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
                cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                where = Schema.Transaction.ID + " = ?";
                whereArgs = new String[] {String.valueOf(transactionIds[2])};
                getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
            } else {
                where = Schema.Transaction.ID + " = ?";
                whereArgs = new String[] {String.valueOf(transactionIds[2])};
                if (mCacheDeletedObjects) {
                    cv = new ContentValues();
                    cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                    cv.put(Schema.Transaction.DELETED, true);
                    getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
                } else {
                    getWritableDatabase().delete(Schema.Transaction.TABLE, where, whereArgs);
                }
                transactionIds[2] = null;
            }
        } else if (contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY) != 0L) {
            cv = new ContentValues();
            cv.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_MONEY));
            cv.put(Schema.Transaction.DATE, contentValues.getAsString(Contract.Transfer.DATE));
            cv.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
            cv.put(Schema.Transaction.CATEGORY, transferTaxCategoryId);
            cv.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
            cv.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
            cv.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.Transfer.TRANSACTION_TAX_WALLET_ID));
            cv.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
            cv.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
            cv.putNull(Schema.Transaction.SAVING);
            cv.putNull(Schema.Transaction.DEBT);
            cv.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
            cv.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
            cv.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
            cv.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
            cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
            cv.put(Schema.Transaction.DELETED, false);
            transactionIds[2] = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cv);
        }
        // update transfer body
        cv = new ContentValues();
        cv.put(Schema.Transfer.DESCRIPTION, contentValues.getAsString(Contract.Transfer.DESCRIPTION));
        cv.put(Schema.Transfer.DATE, contentValues.getAsString(Contract.Transfer.DATE));
        cv.put(Schema.Transfer.TRANSACTION_FROM, transactionIds[0]);
        cv.put(Schema.Transfer.TRANSACTION_TO, transactionIds[1]);
        cv.put(Schema.Transfer.TRANSACTION_TAX, transactionIds[2]);
        cv.put(Schema.Transfer.NOTE, contentValues.getAsString(Contract.Transfer.NOTE));
        cv.put(Schema.Transfer.PLACE, contentValues.getAsLong(Contract.Transfer.PLACE_ID));
        cv.put(Schema.Transfer.EVENT, contentValues.getAsLong(Contract.Transfer.EVENT_ID));
        cv.put(Schema.Transfer.CONFIRMED, contentValues.getAsBoolean(Contract.Transfer.CONFIRMED));
        cv.put(Schema.Transfer.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.Transfer.COUNT_IN_TOTAL));
        if (contentValues.containsKey(Schema.Transfer.TAG)) {
            cv.put(Schema.Transfer.TAG, contentValues.getAsString(Contract.Transfer.TAG));
        }
        cv.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Transfer.ID + " = ?";
        whereArgs = new String[]{String.valueOf(transferId)};
        int rows = getWritableDatabase().update(Schema.Transfer.TABLE, cv, where, whereArgs);
        if (rows > 0) {
            // In this case we have to check if the new provided people ids is changed. We could
            // query the current list, compute two sets containing the items to flag as deleted and
            // and the items to add but it is not enough fast. We can flag all the current items as
            // deleted and then add all the ids as new items but checking for conflict. In case of
            // conflict (same <debtId,personId> tuple) we update the deleted flag only.
            cv = new ContentValues();
            cv.put(Schema.TransferPeople.DELETED, true);
            where = Schema.TransferPeople.TRANSFER + " = ?";
            whereArgs = new String[]{String.valueOf(transferId)};
            getWritableDatabase().update(Schema.TransferPeople.TABLE, cv, where, whereArgs);
            // All the current people associated with this debt are flagged as deleted, now it's
            // time to add (checking for conflicts) the new ids.
            long[] peopleIds = parseIds(contentValues.getAsString(Contract.Transfer.PEOPLE_IDS));
            if (peopleIds != null) {
                for (long personId : peopleIds) {
                    cv = new ContentValues();
                    cv.put(Schema.TransferPeople.TRANSFER, transferId);
                    cv.put(Schema.TransferPeople.PERSON, personId);
                    cv.put(Schema.TransferPeople.UUID, UUID.randomUUID().toString());
                    cv.put(Schema.TransferPeople.LAST_EDIT, System.currentTimeMillis());
                    cv.put(Schema.TransferPeople.DELETED, false);
                    long newId = getWritableDatabase().insertWithOnConflict(Schema.TransferPeople.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                    if (newId == -1L) {
                        // In this case the tuple <transferId,personId> already exists inside the table!
                        // We have to simply update the deleted flag and the update timestamp.
                        cv.clear();
                        cv.put(Schema.TransferPeople.LAST_EDIT, System.currentTimeMillis());
                        cv.put(Schema.TransferPeople.DELETED, false);
                        where = Schema.TransferPeople.TRANSFER + " = ? AND " + Schema.TransferPeople.PERSON + " = ?";
                        whereArgs = new String[]{String.valueOf(transferId), String.valueOf(personId)};
                        getWritableDatabase().update(Schema.TransferPeople.TABLE, cv, where, whereArgs);
                    }
                }
            }
            // we need to do the same for the attachments. the file itself is deleted by the
            // attachmentPicker as soon as it is no more required when the update is performed.
            cv = new ContentValues();
            cv.put(Schema.TransferAttachment.DELETED, true);
            where = Schema.TransferAttachment.TRANSFER + " = ?";
            whereArgs = new String[]{String.valueOf(transferId)};
            getWritableDatabase().update(Schema.TransferAttachment.TABLE, cv, where, whereArgs);
            // reinsert or simply flag as not deleted the already existing attachment links
            long[] attachmentIds = parseIds(contentValues.getAsString(Contract.Transfer.ATTACHMENT_IDS));
            if (attachmentIds != null) {
                for (long attachmentId : attachmentIds) {
                    cv = new ContentValues();
                    cv.put(Schema.TransferAttachment.TRANSFER, transferId);
                    cv.put(Schema.TransferAttachment.ATTACHMENT, attachmentId);
                    cv.put(Schema.TransferAttachment.UUID, UUID.randomUUID().toString());
                    cv.put(Schema.TransferAttachment.LAST_EDIT, System.currentTimeMillis());
                    cv.put(Schema.TransferAttachment.DELETED, false);
                    long newId = getWritableDatabase().insertWithOnConflict(Schema.TransferAttachment.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                    if (newId == -1L) {
                        // In this case the tuple <transferId,attachmentId> already exists inside the table!
                        // We have to simply update the deleted flag and the update timestamp.
                        cv.clear();
                        cv.put(Schema.TransferAttachment.LAST_EDIT, System.currentTimeMillis());
                        cv.put(Schema.TransferAttachment.DELETED, false);
                        where = Schema.TransferAttachment.TRANSFER + " = ? AND " + Schema.TransferAttachment.ATTACHMENT + " = ?";
                        whereArgs = new String[]{String.valueOf(transferId), String.valueOf(attachmentId)};
                        getWritableDatabase().update(Schema.TransferAttachment.TABLE, cv, where, whereArgs);
                    }
                }
            }
            if (!mCacheDeletedObjects) {
                // remove all deleted TransactionPeople
                where = Schema.TransferPeople.TRANSFER + " = ? AND " + Schema.TransferPeople.DELETED + " = 1";
                whereArgs = new String[]{String.valueOf(transferId)};
                getWritableDatabase().delete(Schema.TransferPeople.TABLE, where, whereArgs);
                // remove all deleted TransactionAttachment
                where = Schema.TransferAttachment.TRANSFER + " = ? AND " + Schema.TransferAttachment.DELETED + " = 1";
                whereArgs = new String[]{String.valueOf(transferId)};
                getWritableDatabase().delete(Schema.TransferAttachment.TABLE, where, whereArgs);
            }
        }
        return rows;
    }

    /**
     * This method is called by the content provider when the user is deleting a transfer.
     *
     * @param transferId id of the transfer to delete.
     * @return the number of row affected.
     */
    /*package-local*/ int deleteTransfer(long transferId) {
        // obtain the transaction ids related to the transfer
        for (Long transactionId : getTransferTransactionIds(transferId)) {
            if (transactionId != null) {
                deleteTransactionItem(transactionId);
            }
        }
        // now remove all TransferPeople
        String where = Schema.TransferPeople.TRANSFER + " = ?";
        String[] whereArgs = new String[]{String.valueOf(transferId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransferPeople.DELETED, true);
            cv.put(Schema.TransferPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransferPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransferPeople.TABLE, where, whereArgs);
        }
        // now remove all TransferAttachment
        where = Schema.TransferAttachment.TRANSFER + " = ?";
        whereArgs = new String[]{String.valueOf(transferId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransferAttachment.DELETED, true);
            cv.put(Schema.TransferAttachment.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransferAttachment.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransferAttachment.TABLE, where, whereArgs);
        }
        // finally we can remove the transfer item
        where = Schema.Transfer.ID + " = ?";
        whereArgs = new String[]{String.valueOf(transferId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Transfer.DELETED, true);
            cv.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Transfer.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Transfer.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific category
     * from the database.
     *
     * @param id of the category.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getCategory(long id, String[] projection) {
        String selection = Schema.Category.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getCategories(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the categories
     * from the database.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "c." + Schema.Category.ID + " AS " + Contract.Category.ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.Category.NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.Category.ICON + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.Category.TYPE + ", " +
                "c." + Schema.Category.PARENT + " AS " + Contract.Category.PARENT + ", " +
                "p." + Schema.Category.NAME + " AS " + Contract.Category.PARENT_NAME + ", " +
                "p." + Schema.Category.ICON + " AS " + Contract.Category.PARENT_ICON + ", " +
                "p." + Schema.Category.TYPE + " AS " + Contract.Category.PARENT_TYPE + ", " +
                "p." + Schema.Category.SHOW_REPORT + " AS " + Contract.Category.PARENT_SHOW_REPORT + ", " +
                "p." + Schema.Category.TAG + " AS " + Contract.Category.PARENT_TAG + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.Category.TAG + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.Category.SHOW_REPORT + ", " +
                "c." + Schema.Category.INDEX + " AS " + Contract.Category.INDEX + ", " +
                "IFNULL(p." + Schema.Category.ID + ", c." + Schema.Category.ID + ") AS " + Contract.Category.GROUP_ID + ", " +
                "IFNULL(p." + Schema.Category.NAME + ", c." + Schema.Category.NAME + ") AS " + Contract.Category.GROUP_NAME + ", " +
                "IFNULL(p." + Schema.Category.INDEX + ", c." + Schema.Category.INDEX + ") AS " + Contract.Category.GROUP_INDEX + " " +
                "FROM " + Schema.Category.TABLE + " AS c LEFT JOIN " + Schema.Category.TABLE +
                " AS p ON c." + Schema.Category.PARENT + " = p." + Schema.Category.ID + " AND p." +
                Schema.Category.DELETED + " = 0 WHERE c." + Schema.Category.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given category.
     *
     * @param categoryId id of the category.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getCategoryTransactions(long categoryId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String _selection = "(" + Contract.Transaction.CATEGORY_ID + " = ? OR " +
                Contract.Transaction.CATEGORY_PARENT_ID + " = ?)";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 2];
        _selectionArgs[0] = String.valueOf(categoryId);
        _selectionArgs[1] = String.valueOf(categoryId);
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 2, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new category.
     * Some data consistency check is done before the insertion. If an error is detected an exception
     * is thrown with an {@link com.oriondev.moneywallet.storage.database.Contract.ErrorCode}.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     * @throws SQLiteDataException if the category cannot be inserted.
     */
    /*package-local*/ long insertCategory(ContentValues contentValues) {
        // before inserting the category, checks if it is valid
        if (contentValues.containsKey(Contract.Category.PARENT)) {
            // query the parent category
            String[] projection = new String[] {Schema.Category.ID, Schema.Category.TYPE, Schema.Category.PARENT};
            String where = Schema.Category.ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(contentValues.getAsLong(Contract.Category.PARENT))};
            Cursor cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        // check if category that is set has parent is already a child category
                        if (cursor.getLong(cursor.getColumnIndex(Schema.Category.PARENT)) > 0L) {
                            throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_HIERARCHY_NOT_SUPPORTED,
                                    "The category cannot be inserted because nested relations are not supported");
                        }
                        // check if parent category is consistent with the type of the category
                        if (cursor.getInt(cursor.getColumnIndex(Schema.Category.TYPE)) != contentValues.getAsInteger(Contract.Category.TYPE)) {
                            throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_NOT_CONSISTENT,
                                    "The category cannot be inserted because is not consistent with the parent category");
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        // if this line is reached it means that the category can be inserted
        ContentValues cv = new ContentValues();
        cv.put(Schema.Category.NAME, contentValues.getAsString(Contract.Category.NAME));
        cv.put(Schema.Category.ICON, contentValues.getAsString(Contract.Category.ICON));
        cv.put(Schema.Category.TYPE, contentValues.getAsInteger(Contract.Category.TYPE));
        cv.put(Schema.Category.PARENT, contentValues.getAsLong(Contract.Category.PARENT));
        cv.put(Schema.Category.TAG, contentValues.getAsString(Contract.Category.TAG));
        cv.put(Schema.Category.SHOW_REPORT, contentValues.getAsBoolean(Contract.Category.SHOW_REPORT));
        Integer index = contentValues.getAsInteger(Contract.Category.INDEX);
        cv.put(Schema.Category.INDEX, index != null ? index : 0);
        cv.put(Schema.Category.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Category.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Category.DELETED, false);
        return getWritableDatabase().insert(Schema.Category.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing category.
     * Some data consistency check is done before the update. If an error is detected an exception
     * is thrown with an {@link com.oriondev.moneywallet.storage.database.Contract.ErrorCode}.
     *
     * @param categoryId id of the category to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected by the update.
     * @throws SQLiteDataException if the category cannot be updated.
     */
    /*package-local*/ int updateCategory(long categoryId, ContentValues contentValues) {
        // check if category is a system category: only the flag SHOW_REPORT can be changed
        boolean isSystemCategory = false;
        boolean isChildCategory = false;
        String[] projection = new String[] {Schema.Category.TYPE, Schema.Category.PARENT};
        String where = Schema.Category.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(categoryId)};
        Cursor cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (cursor.getInt(cursor.getColumnIndex(Schema.Category.TYPE)) == Contract.CategoryType.SYSTEM.getValue()) {
                        isSystemCategory = true;
                    }
                    if (!cursor.isNull(cursor.getColumnIndex(Schema.Category.PARENT))) {
                        isChildCategory = true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        boolean isValidParentSet = contentValues.containsKey(Contract.Category.PARENT) && contentValues.getAsLong(Contract.Category.PARENT) != null;
        if (isValidParentSet) {
            // check if category that has a parent id set has already some children
            projection = new String[] {Schema.Category.ID};
            where = Schema.Category.PARENT + " = ?";
            whereArgs = new String[]{String.valueOf(categoryId)};
            cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_HIERARCHY_NOT_SUPPORTED,
                                String.format(Locale.ENGLISH, "The category (id: %d) cannot be updated because nested relations are not supported", categoryId));
                    }
                } finally {
                    cursor.close();
                }
            }
            // query the parent category
            projection = new String[] {Schema.Category.ID, Schema.Category.TYPE, Schema.Category.PARENT};
            where = Schema.Category.ID + " = ?";
            whereArgs = new String[]{String.valueOf(contentValues.getAsLong(Contract.Category.PARENT))};
            cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        // check if category that is set has parent is already a child category
                        if (cursor.getLong(cursor.getColumnIndex(Schema.Category.PARENT)) > 0L) {
                            throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_HIERARCHY_NOT_SUPPORTED,
                                    String.format(Locale.ENGLISH, "The category (id: %d) cannot be updated because nested relations are not supported", categoryId));
                        }
                        // check if parent category is consistent with the type of the category
                        if (cursor.getInt(cursor.getColumnIndex(Schema.Category.TYPE)) != contentValues.getAsInteger(Contract.Category.TYPE)) {
                            throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_NOT_CONSISTENT,
                                    String.format(Locale.ENGLISH, "The category (id: %d) cannot be updated because is not consistent with the parent category", categoryId));
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } else {
            // check if category has some children and if the type is consistent
            if (contentValues.containsKey(Contract.Category.TYPE)) {
                projection = new String[] {Schema.Category.ID};
                where = Schema.Category.PARENT + " = ? AND " + Schema.Category.TYPE + " != ?";
                whereArgs = new String[]{String.valueOf(categoryId), String.valueOf(contentValues.getAsInteger(Contract.Category.TYPE))};
                cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_NOT_CONSISTENT,
                                    String.format(Locale.ENGLISH, "The category (id: %d) cannot be updated because is not consistent with the children categories", categoryId));
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
        // update the category
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Contract.Category.NAME)) {
            cv.put(Schema.Category.NAME, contentValues.getAsString(Contract.Category.NAME));
        }
        if (contentValues.containsKey(Contract.Category.ICON)) {
            cv.put(Schema.Category.ICON, contentValues.getAsString(Contract.Category.ICON));
        }
        if (!isSystemCategory) {
            if (contentValues.containsKey(Contract.Category.TYPE)) {
                int type = contentValues.getAsInteger(Contract.Category.TYPE);
                if (type != Schema.CategoryType.SYSTEM) {
                    cv.put(Schema.Category.TYPE, type);
                }
            }
            if (contentValues.containsKey(Contract.Category.PARENT)) {
                cv.put(Schema.Category.PARENT, contentValues.getAsLong(Contract.Category.PARENT));
            }
            if (contentValues.containsKey(Contract.Category.TAG)) {
                cv.put(Schema.Category.TAG, contentValues.getAsString(Contract.Category.TAG));
            }
        }
        if (contentValues.containsKey(Contract.Category.SHOW_REPORT)) {
            cv.put(Schema.Category.SHOW_REPORT, contentValues.getAsBoolean(Contract.Category.SHOW_REPORT));
        }
        if (contentValues.containsKey(Contract.Category.INDEX)) {
            int index = contentValues.getAsInteger(Contract.Category.INDEX);
            if (isChildCategory || isValidParentSet) {
                // child categories should have index set always to 0 to prevent undesired
                // order between subcategories of the same group.
                index = 0;
            }
            cv.put(Schema.Category.INDEX, index);
        } else {
            if (isChildCategory || isValidParentSet) {
                // for the same reason, if the category is a child category or it is going to
                // be updated as child category, force the index field to 0.
                cv.put(Schema.Category.INDEX, 0);
            }
        }
        cv.put(Schema.Category.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Category.ID + " = ?";
        whereArgs = new String[]{String.valueOf(categoryId)};
        int rows = getWritableDatabase().update(Schema.Category.TABLE, cv, where, whereArgs);
        // if the category type has changed, all the transactions made with this category must be
        // updated using the direction of the updated category
        if (!isSystemCategory && contentValues.containsKey(Contract.Category.TYPE)) {
            int type = contentValues.getAsInteger(Contract.Category.TYPE);
            if (type != Schema.CategoryType.SYSTEM) {
                where = Schema.Transaction.CATEGORY + " = ?";
                whereArgs = new String[]{String.valueOf(categoryId)};
                cv = new ContentValues();
                cv.put(Schema.Transaction.DIRECTION, type == Schema.CategoryType.INCOME ?
                                        Schema.Direction.INCOME : Schema.Direction.EXPENSE);
                cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
            }
        }
        // return the number of rows updated only inside the category table
        return rows;
    }

    /**
     * This method is called by the content provider when the user is deleting an existing category.
     *
     * @param categoryId id of the category to delete.
     * @return the number of row affected.
     * @throws SQLiteDataException if the category cannot be deleted.
     */
    /*package-local*/ int deleteCategory(long categoryId) {
        // check if category has some sub-category
        String[] projection = new String[] {Schema.Category.ID};
        String where = Schema.Category.PARENT + " = ? AND " + Schema.Category.DELETED + " = 0";
        String[] whereArgs = new String[]{String.valueOf(categoryId)};
        Cursor cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_HAS_CHILDREN,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is parent of %d categories", categoryId, cursor.getCount()));
                }
            } finally {
                cursor.close();
            }
        }
        // check if category is a system category
        projection = new String[] {Schema.Category.TYPE};
        where = Schema.Category.ID + " = ?";
        whereArgs = new String[]{String.valueOf(categoryId)};
        cursor = getReadableDatabase().query(Schema.Category.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(Schema.Category.TYPE)) == Contract.CategoryType.SYSTEM.getValue()) {
                    throw new SQLiteDataException(Contract.ErrorCode.SYSTEM_CATEGORY_NOT_MODIFIABLE,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is a system category", categoryId));
                }
            } finally {
                cursor.close();
            }
        }
        // check if category is in use in some transaction
        projection = new String[] {Schema.Transaction.ID};
        where = Schema.Transaction.CATEGORY + " = ? AND " + Schema.Transaction.DELETED + " = 0";
        whereArgs = new String[]{String.valueOf(categoryId)};
        cursor = getReadableDatabase().query(Schema.Transaction.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_IN_USE,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is in use in %d transactions", categoryId, cursor.getCount()));
                }
            } finally {
                cursor.close();
            }
        }
        // check if category is in use in some transaction model
        projection = new String[] {Schema.TransactionModel.ID};
        where = Schema.TransactionModel.CATEGORY + " = ? AND " + Schema.TransactionModel.DELETED + " = 0";
        whereArgs = new String[]{String.valueOf(categoryId)};
        cursor = getReadableDatabase().query(Schema.TransactionModel.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_IN_USE,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is in use in %d transaction-models", categoryId, cursor.getCount()));
                }
            } finally {
                cursor.close();
            }
        }
        // check if category is in use in some recurrent transaction
        projection = new String[] {Schema.RecurrentTransaction.ID};
        where = Schema.RecurrentTransaction.CATEGORY + " = ? AND " + Schema.RecurrentTransaction.DELETED + " = 0";
        whereArgs = new String[]{String.valueOf(categoryId)};
        cursor = getReadableDatabase().query(Schema.RecurrentTransaction.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_IN_USE,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is in use in %d recurrent-transactions", categoryId, cursor.getCount()));
                }
            } finally {
                cursor.close();
            }
        }
        // check if category is in use in some budget
        projection = new String[] {Schema.Budget.ID};
        where = Schema.Budget.CATEGORY + " = ? AND " + Schema.Budget.DELETED + " = 0";
        whereArgs = new String[]{String.valueOf(categoryId)};
        cursor = getReadableDatabase().query(Schema.Budget.TABLE, projection, where, whereArgs, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    throw new SQLiteDataException(Contract.ErrorCode.CATEGORY_IN_USE,
                            String.format(Locale.ENGLISH, "The category (id: %d) cannot be deleted because it is in use in %d budgets", categoryId, cursor.getCount()));
                }
            } finally {
                cursor.close();
            }
        }
        // if this line has been reached, the category can be removed
        where = Schema.Category.ID + " = ?";
        whereArgs = new String[]{String.valueOf(categoryId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Category.DELETED, true);
            cv.put(Schema.Category.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Category.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Category.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific debt.
     *
     * @param id of the debt.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getDebt(long id, String[] projection) {
        String selection = Schema.Debt.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getDebts(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the debts.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getDebts(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Debt.ID + " AS " + Contract.Debt.ID + ", " +
                Schema.Debt.TYPE + " AS " + Contract.Debt.TYPE + ", " +
                Schema.Debt.ICON + " AS " + Contract.Debt.ICON + ", " +
                Schema.Debt.DESCRIPTION + " AS " + Contract.Debt.DESCRIPTION + ", " +
                Schema.Debt.DATE + " AS " + Contract.Debt.DATE + ", " +
                Schema.Debt.EXPIRATION_DATE + " AS " + Contract.Debt.EXPIRATION_DATE + ", " +
                Schema.Debt.WALLET + " AS " + Contract.Debt.WALLET_ID + ", " +
                Schema.Wallet.NAME + " AS " + Contract.Debt.WALLET_NAME + ", " +
                Schema.Wallet.ICON + " AS " + Contract.Debt.WALLET_ICON + ", " +
                Schema.Wallet.CURRENCY + " AS " + Contract.Debt.WALLET_CURRENCY + ", " +
                Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Debt.WALLET_COUNT_IN_TOTAL + ", " +
                Schema.Wallet.ARCHIVED + " AS " + Contract.Debt.WALLET_ARCHIVED + ", " +
                Schema.Wallet.TAG + " AS " + Contract.Debt.WALLET_TAG + ", " +
                Schema.Debt.NOTE + " AS " + Contract.Debt.NOTE + ", " +
                Schema.Debt.PLACE + " AS " + Contract.Debt.PLACE_ID + ", " +
                Schema.Place.NAME + " AS " + Contract.Debt.PLACE_NAME + ", " +
                Schema.Place.ICON + " AS " + Contract.Debt.PLACE_ICON + ", " +
                Schema.Place.ADDRESS + " AS " + Contract.Debt.PLACE_ADDRESS + ", " +
                Schema.Place.LATITUDE + " AS " + Contract.Debt.PLACE_LATITUDE + ", " +
                Schema.Place.LONGITUDE + " AS " + Contract.Debt.PLACE_LONGITUDE + ", " +
                Schema.Place.TAG + " AS " + Contract.Debt.PLACE_TAG + ", " +
                Schema.Debt.MONEY + " AS " + Contract.Debt.MONEY + ", " +
                Schema.Debt.ARCHIVED + " AS " + Contract.Debt.ARCHIVED + ", " +
                Schema.Debt.TAG + " AS " + Contract.Debt.TAG + ", " +
                "_debt_progress AS " + Contract.Debt.PROGRESS + ",\n" +
                "GROUP_CONCAT('<' || _person_id || '>') AS " + Contract.Debt.PEOPLE_IDS + "\n" +
                "FROM (\n" +
                "\tSELECT d.*, w.*, p.*,\n" +
                "\tt._debt_progress,\n" +
                "\tpe.person_id AS _person_id\n" +
                "\tFROM debts AS d\n" +
                "\t\tLEFT JOIN debt_people AS dp ON d.debt_id = dp._debt AND dp.deleted = 0\n" +
                "\t\tLEFT JOIN people AS pe ON dp._person = pe.person_id AND pe.deleted = 0\n" +
                "\t\tJOIN wallets AS w ON d.debt_wallet = w.wallet_id AND w.deleted = 0\n" +
                "\t\tLEFT JOIN places AS p ON debt_place = p.place_id AND p.deleted = 0\n" +
                "\t\tLEFT JOIN (\n" +
                "\t\t\tSELECT transaction_debt AS _debt_id,\n" +
                "\t\t\tSUM(((transaction_direction * 2) - 1) * transaction_money) AS _debt_progress\n" +
                "\t\t\tFROM transactions AS t\n" +
                "\t\t\tLEFT JOIN categories AS c ON t.transaction_category = c.category_id AND c.deleted = 0\n" +
                "\t\t\tWHERE t.deleted = 0 AND t.transaction_confirmed = 1 AND (\n" +
                "\t\t\tc.category_tag = 'system::paid_debt' OR \n" +
                "\t\t\tc.category_tag = 'system::paid_credit'\n" +
                "\t\t\t) AND DATETIME(t.transaction_date) <= DATETIME('now', 'localtime')\n" +
                "\t\t\tGROUP BY _debt_id\n" +
                "\t\t) AS t ON t._debt_id = d.debt_id\n" +
                "\t\tWHERE d.deleted = 0\n" +
                ")\n" +
                "GROUP BY debt_id";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the people
     * related to a given debt.
     *
     * @param id of the debt.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getDebtPeople(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Person.ID + " AS " + Contract.Person.ID + ", " +
                Schema.Person.NAME + " AS " + Contract.Person.NAME + ", " +
                Schema.Person.ICON + " AS " + Contract.Person.ICON + ", " +
                Schema.Person.NOTE + " AS " + Contract.Person.NOTE + ", " +
                Schema.Person.TAG + " AS " + Contract.Person.TAG + " " +
                "FROM " + Schema.DebtPeople.TABLE + " AS dp JOIN " + Schema.Person.TABLE + " AS p " +
                "ON dp." + Schema.DebtPeople.PERSON + " = p." + Schema.Person.ID + " AND dp." +
                Schema.DebtPeople.DELETED + " = 0 AND p." + Schema.Person.DELETED + " = 0 AND " +
                Schema.DebtPeople.DEBT + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given debt.
     *
     * @param debtId id of the debt.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getDebtTransactions(long debtId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String _selection = Schema.Transaction.DEBT + " = ?";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 1];
        _selectionArgs[0] = String.valueOf(debtId);
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 1, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new debt.
     *
     * contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertDebt(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Debt.TYPE, contentValues.getAsInteger(Contract.Debt.TYPE));
        cv.put(Schema.Debt.ICON, contentValues.getAsString(Contract.Debt.ICON));
        cv.put(Schema.Debt.DESCRIPTION, contentValues.getAsString(Contract.Debt.DESCRIPTION));
        cv.put(Schema.Debt.DATE, contentValues.getAsString(Contract.Debt.DATE));
        cv.put(Schema.Debt.EXPIRATION_DATE, contentValues.getAsString(Contract.Debt.EXPIRATION_DATE));
        cv.put(Schema.Debt.WALLET, contentValues.getAsLong(Contract.Debt.WALLET_ID));
        cv.put(Schema.Debt.NOTE, contentValues.getAsString(Contract.Debt.NOTE));
        cv.put(Schema.Debt.PLACE, contentValues.getAsLong(Contract.Debt.PLACE_ID));
        cv.put(Schema.Debt.MONEY, contentValues.getAsLong(Contract.Debt.MONEY));
        cv.put(Schema.Debt.ARCHIVED, contentValues.getAsBoolean(Contract.Debt.ARCHIVED));
        cv.put(Schema.Debt.TAG, contentValues.getAsString(Contract.Debt.TAG));
        cv.put(Schema.Debt.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Debt.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Debt.DELETED, false);
        long debtId = getWritableDatabase().insert(Schema.Debt.TABLE, null, cv);
        if (debtId > 0L) {
            // now we need to parse the content values string that may contains the ids of the people
            long[] peopleIds = parseIds(contentValues.getAsString(Contract.Debt.PEOPLE_IDS));
            if (peopleIds != null) {
                for (long personId : peopleIds) {
                    ContentValues cvp = new ContentValues();
                    cvp.put(Schema.DebtPeople.DEBT, debtId);
                    cvp.put(Schema.DebtPeople.PERSON, personId);
                    cvp.put(Schema.DebtPeople.UUID, UUID.randomUUID().toString());
                    cvp.put(Schema.DebtPeople.LAST_EDIT, System.currentTimeMillis());
                    cvp.put(Schema.DebtPeople.DELETED, false);
                    getWritableDatabase().insert(Schema.DebtPeople.TABLE, null, cvp);
                }
            }
            // now check if the user want to add a master transaction for this debt
            if (contentValues.containsKey(Contract.Debt.INSERT_MASTER_TRANSACTION)
                    && contentValues.getAsBoolean(Contract.Debt.INSERT_MASTER_TRANSACTION)) {
                cv = new ContentValues();
                Contract.DebtType type = Contract.DebtType.fromValue(contentValues.getAsInteger(Contract.Debt.TYPE));
                cv.put(Contract.Transaction.MONEY, contentValues.getAsLong(Contract.Debt.MONEY));
                cv.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(System.currentTimeMillis()));
                cv.put(Contract.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Debt.DESCRIPTION));
                cv.put(Contract.Transaction.CATEGORY_ID, getSystemCategoryId((type == Contract.DebtType.DEBT) ? Schema.CategoryTag.DEBT : Schema.CategoryTag.CREDIT));
                cv.put(Contract.Transaction.DIRECTION, (type == Contract.DebtType.CREDIT) ? Contract.Direction.EXPENSE : Contract.Direction.INCOME);
                cv.put(Contract.Transaction.TYPE, Contract.TransactionType.DEBT);
                cv.put(Contract.Transaction.WALLET_ID, contentValues.containsKey(Contract.Debt.WALLET_ID) ? contentValues.getAsLong(Contract.Debt.WALLET_ID) : null);
                cv.put(Contract.Transaction.PLACE_ID, contentValues.containsKey(Contract.Debt.PLACE_ID) ? contentValues.getAsLong(Contract.Debt.PLACE_ID) : null);
                cv.put(Contract.Transaction.NOTE, contentValues.getAsString(Contract.Debt.NOTE));
                cv.putNull(Contract.Transaction.EVENT_ID);
                cv.putNull(Contract.Transaction.SAVING_ID);
                cv.put(Contract.Transaction.DEBT_ID, debtId);
                cv.put(Contract.Transaction.CONFIRMED, true);
                cv.put(Contract.Transaction.COUNT_IN_TOTAL, true);
                cv.put(Contract.Transaction.PEOPLE_IDS, contentValues.getAsString(Contract.Debt.PEOPLE_IDS));
                cv.putNull(Contract.Transaction.ATTACHMENT_IDS);
                insertTransaction(cv);
            }
        }
        return debtId;
    }

    /**
     * This method is called by the content provider when the user is updating an existing debt.
     *
     * @param debtId id of the debt to update.
     * @param contentValues bundle that contains the data from the content provider.
     * @return the number of row affected.
     */
    /*package-local*/ int updateDebt(long debtId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Contract.Debt.TYPE)) {
            cv.put(Schema.Debt.TYPE, contentValues.getAsInteger(Contract.Debt.TYPE));
        }
        if (contentValues.containsKey(Contract.Debt.ICON)) {
            cv.put(Schema.Debt.ICON, contentValues.getAsString(Contract.Debt.ICON));
        }
        if (contentValues.containsKey(Contract.Debt.DESCRIPTION)) {
            cv.put(Schema.Debt.DESCRIPTION, contentValues.getAsString(Contract.Debt.DESCRIPTION));
        }
        if (contentValues.containsKey(Contract.Debt.DATE)) {
            cv.put(Schema.Debt.DATE, contentValues.getAsString(Contract.Debt.DATE));
        }
        if (contentValues.containsKey(Contract.Debt.EXPIRATION_DATE)) {
            cv.put(Schema.Debt.EXPIRATION_DATE, contentValues.getAsString(Contract.Debt.EXPIRATION_DATE));
        }
        if (contentValues.containsKey(Contract.Debt.WALLET_ID)) {
            cv.put(Schema.Debt.WALLET, contentValues.getAsLong(Contract.Debt.WALLET_ID));
        }
        if (contentValues.containsKey(Contract.Debt.NOTE)) {
            cv.put(Schema.Debt.NOTE, contentValues.getAsString(Contract.Debt.NOTE));
        }
        if (contentValues.containsKey(Contract.Debt.PLACE_ID)) {
            cv.put(Schema.Debt.PLACE, contentValues.getAsLong(Contract.Debt.PLACE_ID));
        }
        if (contentValues.containsKey(Contract.Debt.MONEY)) {
            cv.put(Schema.Debt.MONEY, contentValues.getAsLong(Contract.Debt.MONEY));
        }
        if (contentValues.containsKey(Contract.Debt.ARCHIVED)) {
            cv.put(Schema.Debt.ARCHIVED, contentValues.getAsBoolean(Contract.Debt.ARCHIVED));
        }
        if (contentValues.containsKey(Contract.Debt.TAG)) {
            cv.put(Schema.Debt.TAG, contentValues.getAsString(Contract.Debt.TAG));
        }
        cv.put(Schema.Debt.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Debt.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(debtId)};
        int rows = getWritableDatabase().update(Schema.Debt.TABLE, cv, where, whereArgs);
        if (rows > 0) {
            // In this case we have to check if the new provided people ids is changed. We could
            // query the current list, compute two sets containing the items to flag as deleted and
            // and the items to add but it is not enough fast. We can flag all the current items as
            // deleted and then add all the ids as new items but checking for conflict. In case of
            // conflict (same <debtId,personId> tuple) we update the deleted flag only.
            if (contentValues.containsKey(Contract.Debt.PEOPLE_IDS)) {
                cv = new ContentValues();
                cv.put(Schema.DebtPeople.DELETED, true);
                where = Schema.DebtPeople.DEBT + " = ?";
                whereArgs = new String[]{String.valueOf(debtId)};
                getWritableDatabase().update(Schema.DebtPeople.TABLE, cv, where, whereArgs);
                // All the current people associated with this debt are flagged as deleted, now it's
                // time to add (checking for conflicts) the new ids.
                long[] peopleIds = parseIds(contentValues.getAsString(Contract.Debt.PEOPLE_IDS));
                if (peopleIds != null) {
                    for (long personId : peopleIds) {
                        cv.clear();
                        cv.put(Schema.DebtPeople.DEBT, debtId);
                        cv.put(Schema.DebtPeople.PERSON, personId);
                        cv.put(Schema.DebtPeople.UUID, UUID.randomUUID().toString());
                        cv.put(Schema.DebtPeople.LAST_EDIT, System.currentTimeMillis());
                        cv.put(Schema.DebtPeople.DELETED, false);
                        long newId = getWritableDatabase().insertWithOnConflict(Schema.DebtPeople.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                        if (newId == -1L) {
                            // In this case the tuple <debtId,personId> already exists inside the table!
                            // We have to simply update the deleted flag and the update timestamp.
                            cv.clear();
                            cv.put(Schema.DebtPeople.LAST_EDIT, System.currentTimeMillis());
                            cv.put(Schema.DebtPeople.DELETED, false);
                            where = Schema.DebtPeople.DEBT + " = ? AND " + Schema.DebtPeople.PERSON + " = ?";
                            whereArgs = new String[]{String.valueOf(debtId), String.valueOf(personId)};
                            getWritableDatabase().update(Schema.DebtPeople.TABLE, cv, where, whereArgs);
                        }
                    }
                }
                if (!mCacheDeletedObjects) {
                    // remove all deleted DebtPeople
                    where = Schema.DebtPeople.DEBT + " = ? AND " + Schema.DebtPeople.DELETED + " = 1";
                    whereArgs = new String[]{String.valueOf(debtId)};
                    getWritableDatabase().delete(Schema.DebtPeople.TABLE, where, whereArgs);
                }
            }
            // check if exists a master transaction for this debt and update it
            Long debtCategoryId = getSystemCategoryId(Schema.CategoryTag.DEBT);
            Long creditCategoryId = getSystemCategoryId(Schema.CategoryTag.CREDIT);
            String[] projection = new String[] {
                    Contract.Transaction.ID
            };
            String selection = Contract.Transaction.TYPE + " = ? AND " +
                    Contract.Transaction.DEBT_ID + " = ? AND (" +
                    Contract.Transaction.CATEGORY_ID + " = ? OR " +
                    Contract.Transaction.CATEGORY_ID + " = ?)";
            String[] selectionArgs = new String[] {
                    String.valueOf(Contract.TransactionType.DEBT),
                    String.valueOf(debtId),
                    String.valueOf(debtCategoryId),
                    String.valueOf(creditCategoryId)
            };
            Cursor cursor = getTransactions(projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    long transactionId = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.ID));
                    cv = new ContentValues();
                    if (contentValues.containsKey(Contract.Debt.MONEY)) {
                        cv.put(Contract.Transaction.MONEY, contentValues.getAsLong(Contract.Debt.MONEY));
                    }
                    if (contentValues.containsKey(Contract.Debt.DESCRIPTION)) {
                        cv.put(Contract.Transaction.DESCRIPTION, contentValues.getAsString(Contract.Debt.DESCRIPTION));
                    }
                    if (contentValues.containsKey(Contract.Debt.TYPE)) {
                        Contract.DebtType type = Contract.DebtType.fromValue(contentValues.getAsInteger(Contract.Debt.TYPE));
                        if (type == Contract.DebtType.CREDIT) {
                            cv.put(Contract.Transaction.DIRECTION, Contract.Direction.EXPENSE);
                            cv.put(Contract.Transaction.CATEGORY_ID, creditCategoryId);
                        } else if (type == Contract.DebtType.DEBT) {
                            cv.put(Contract.Transaction.DIRECTION, Contract.Direction.INCOME);
                            cv.put(Contract.Transaction.CATEGORY_ID, debtCategoryId);
                        }
                    }
                    if (contentValues.containsKey(Contract.Debt.WALLET_ID)) {
                        cv.put(Contract.Transaction.WALLET_ID, contentValues.getAsLong(Contract.Debt.WALLET_ID));
                    }
                    if (contentValues.containsKey(Contract.Debt.PLACE_ID)) {
                        cv.put(Contract.Transaction.PLACE_ID, contentValues.getAsLong(Contract.Debt.PLACE_ID));
                    }
                    if (contentValues.containsKey(Contract.Debt.NOTE)) {
                        cv.put(Contract.Transaction.NOTE, contentValues.getAsString(Contract.Debt.NOTE));
                    }
                    if (contentValues.containsKey(Contract.Debt.PEOPLE_IDS)) {
                        cv.put(Contract.Transaction.PEOPLE_IDS, contentValues.getAsString(Contract.Debt.PEOPLE_IDS));
                    }
                    updateTransaction(transactionId, cv);
                }
                cursor.close();
            }
        }
        return rows;
    }

    /**
     * Delete a debt from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param debtId id of the debt to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteDebt(long debtId) {
        // remove all the transactions of this debt
        deleteTransactionItems(getDebtTransactions(debtId, new String[] {Contract.Transaction.ID}, null, null, null));
        // remove all DebtPeople
        String where = Schema.DebtPeople.DEBT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(debtId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.DebtPeople.DELETED, true);
            cv.put(Schema.DebtPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.DebtPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.DebtPeople.TABLE, where, whereArgs);
        }
        // remove the debt item
        where = Schema.Debt.ID + " = ?";
        whereArgs = new String[]{String.valueOf(debtId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Debt.DELETED, true);
            cv.put(Schema.Debt.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Debt.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Debt.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific budget.
     *
     * @param id of the budget.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getBudget(long id, String[] projection) {
        String selection = Schema.Budget.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getBudgets(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the budgets.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getBudgets(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // The query can be modified to return the 'progress' column as a concatenation of currency
        // and sum of money for that currency that is useful if we decide to let the user set wallets
        // with different currencies inside the same budget.
        // In this case the budget has for example a target of 'EUR 5000' but the progress column
        // can contains something like this: 'EUR 2050, USD 1000'. How do we handle this? We need to
        // convert all the other currencies to the target one to check the percentage?
        // The percentage will than vary in future when the exchange rates varies because we have no
        // way to persist them somewhere. The simpler solution is to avoid to have different currencies
        // inside the same budget. If we found a simple solution for this in future we only need to
        // modify the part of the query where we sum the transaction amounts using the already provided
        // '_wallet_currency' column internal to the query (at the moment unused).
        String subQuery = "SELECT " +
                "b." + Schema.Budget.ID + " AS " + Contract.Budget.ID + ", " +
                "b." + Schema.Budget.TYPE + " AS " + Contract.Budget.TYPE + ", " +
                "b." + Schema.Budget.CATEGORY + " AS " + Contract.Budget.CATEGORY_ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.Budget.CATEGORY_NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.Budget.CATEGORY_ICON + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.Budget.CATEGORY_TYPE + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.Budget.CATEGORY_SHOW_REPORT + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.Budget.CATEGORY_TAG + ", " +
                "b." + Schema.Budget.START_DATE + " AS " + Contract.Budget.START_DATE + ", " +
                "b." + Schema.Budget.END_DATE + " AS " + Contract.Budget.END_DATE + ", " +
                "b." + Schema.Budget.MONEY + " AS " + Contract.Budget.MONEY + ", " +
                "b." + Schema.Budget.CURRENCY + " AS " + Contract.Budget.CURRENCY + ", " +
                "b." + Schema.Budget.TAG + " AS " + Contract.Budget.TAG + ", " +
                "SUM(_progress) AS " + Contract.Budget.PROGRESS + "," +
                "GROUP_CONCAT('<' || _wallet_id || '>') AS " + Contract.Budget.WALLET_IDS + "," +
                "MAX(_wallet_total) AS " + Contract.Budget.HAS_WALLET_IN_TOTAL + " FROM(" +
                // query all budgets of type 0
                "SELECT b.*, SUM(t." + Schema.Transaction.MONEY + ") AS _progress " +
                "FROM(SELECT b.*, w." + Schema.Wallet.ID + " AS _wallet_id, " +
                "w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " +
                Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON " +
                "bw._budget = b." + Schema.Budget.ID + " JOIN " + Schema.Wallet.TABLE + " AS w ON " +
                "bw._wallet = w." + Schema.Wallet.ID + " WHERE bw. " + Schema.BudgetWallet.DELETED +
                " = 0 AND b." + Schema.Budget.DELETED + " = 0 AND w." + Schema.Wallet.DELETED +
                " = 0 AND b." + Schema.Budget.TYPE + " = " + Schema.BudgetType.EXPENSES +
                " ) AS b LEFT JOIN " + Schema.Transaction.TABLE + " AS t ON b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND t." + Schema.Transaction.DELETED + " = 0 AND t." +
                Schema.Transaction.DIRECTION + " = 0 AND DATETIME(t." + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') AND DATE(t." + Schema.Transaction.DATE +
                ") >= DATE(b." + Schema.Budget.START_DATE + ") AND DATE(t." + Schema.Transaction.DATE +
                ") <=  DATE(b." + Schema.Budget.END_DATE + ") GROUP BY b." + Schema.Budget.ID +
                ", _wallet_id UNION " +
                // query all budgets of type 1
                "SELECT b.*, SUM(t." + Schema.Transaction.MONEY + ") AS _progress " +
                "FROM(SELECT b.*, w." + Schema.Wallet.ID + " AS _wallet_id, " +
                "w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " +
                Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON " +
                "bw._budget = b." + Schema.Budget.ID + " JOIN " + Schema.Wallet.TABLE + " AS w ON " +
                "bw._wallet = w." + Schema.Wallet.ID + " WHERE bw. " + Schema.BudgetWallet.DELETED +
                " = 0 AND b." + Schema.Budget.DELETED + " = 0 AND w." + Schema.Wallet.DELETED +
                " = 0 AND b." + Schema.Budget.TYPE + " = " + Schema.BudgetType.INCOMES +
                " ) AS b LEFT JOIN " + Schema.Transaction.TABLE + " AS t ON b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND t." + Schema.Transaction.DELETED + " = 0 AND t." +
                Schema.Transaction.DIRECTION + " = 1 AND DATETIME(t." + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') AND DATE(t." + Schema.Transaction.DATE +
                ") >= DATE(b." + Schema.Budget.START_DATE + ") AND DATE(t." +
                Schema.Transaction.DATE + ") <= DATE(b." + Schema.Budget.END_DATE + ") GROUP BY b."
                + Schema.Budget.ID + ", _wallet_id UNION " +
                // query all budgets of type 2
                "SELECT b.*, SUM(((t." + Schema.Transaction.DIRECTION + " * 2) - 1) * t." +
                Schema.Transaction.MONEY + ") AS _progress FROM(SELECT b.*, w." + Schema.Wallet.ID +
                " AS _wallet_id, w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, w." +
                Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " + Schema.BudgetWallet.TABLE +
                " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON bw._budget = b." + Schema.Budget.ID +
                " JOIN " + Schema.Wallet.TABLE + " AS w ON bw._wallet = w." + Schema.Wallet.ID +
                " WHERE bw. " + Schema.BudgetWallet.DELETED + " = 0 AND b." + Schema.Budget.DELETED +
                " = 0 AND w." + Schema.Wallet.DELETED + " = 0 AND b." + Schema.Budget.TYPE + " = " +
                Schema.BudgetType.CATEGORY + ") AS b LEFT JOIN (SELECT tr.*, tc." +
                Schema.Category.PARENT + " AS _parent_category FROM " + Schema.Transaction.TABLE +
                " AS tr JOIN " + Schema.Category.TABLE + " AS tc ON tr." + Schema.Transaction.CATEGORY +
                " = tc." + Schema.Category.ID + " WHERE tr." + Schema.Transaction.DELETED + " = 0 " +
                " AND tc." + Schema.Category.DELETED + " = 0) AS t ON (b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND DATETIME(t." + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') AND DATE(t." + Schema.Transaction.DATE +
                ") >= DATE(b." + Schema.Budget.START_DATE + ") AND DATE(t." +
                Schema.Transaction.DATE + ") <=  DATE(b." + Schema.Budget.END_DATE + ") AND (b."
                + Schema.Budget.CATEGORY + " = t." + Schema.Transaction.CATEGORY + " OR b." +
                Schema.Budget.CATEGORY + " = t._parent_category)) GROUP BY b." + Schema.Budget.ID +
                ", _wallet_id) AS b LEFT JOIN " + Schema.Category.TABLE + " AS c ON b." +
                Schema.Budget.CATEGORY + " = c." + Schema.Category.ID + " AND c." +
                Schema.Category.DELETED + " = 0 GROUP BY b." + Schema.Budget.ID;
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the wallets
     * related to a given budget.
     *
     * @param id of the budget.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getBudgetWallets(long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Wallet.ID + " AS " + Contract.Wallet.ID + ", " +
                Schema.Wallet.NAME + " AS " + Contract.Wallet.NAME + ", " +
                Schema.Wallet.ICON + " AS " + Contract.Wallet.ICON + ", " +
                Schema.Wallet.CURRENCY + " AS " + Contract.Wallet.CURRENCY + ", " +
                Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Wallet.COUNT_IN_TOTAL + ", " +
                Schema.Wallet.START_MONEY + " AS " + Contract.Wallet.START_MONEY + ", " +
                Schema.Wallet.ARCHIVED + " AS " + Contract.Wallet.ARCHIVED + ", " +
                Schema.Wallet.TAG + " AS " + Contract.Wallet.TAG + " " +
                "FROM " + Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Wallet.TABLE + " AS w " +
                "ON bw." + Schema.BudgetWallet.WALLET + " = w." + Schema.Wallet.ID + " AND bw." +
                Schema.BudgetWallet.DELETED + " = 0 AND w." + Schema.Wallet.DELETED + " = 0 AND " +
                Schema.BudgetWallet.BUDGET + " = " + String.valueOf(id);
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given budget.
     *
     * @param budgetId id of the budget.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getBudgetTransactions(long budgetId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "t." + Schema.Transaction.ID + " AS " + Contract.Transaction.ID + ", " +
                "t." + Schema.Transaction.MONEY + " AS " + Contract.Transaction.MONEY + ", " +
                "t." + Schema.Transaction.DATE + " AS " + Contract.Transaction.DATE + ", " +
                "t." + Schema.Transaction.DESCRIPTION + " AS " + Contract.Transaction.DESCRIPTION + ", " +
                "t." + Schema.Transaction.CATEGORY + " AS " + Contract.Transaction.CATEGORY_ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.Transaction.CATEGORY_NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.Transaction.CATEGORY_ICON + ", " +
                "c." + Schema.Category.PARENT + " AS " + Contract.Transaction.CATEGORY_PARENT_ID + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.Transaction.CATEGORY_TYPE + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.Transaction.CATEGORY_TAG + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.Transaction.CATEGORY_SHOW_REPORT + ", " +
                "t." + Schema.Transaction.DIRECTION + " AS " + Contract.Transaction.DIRECTION + ", " +
                "t." + Schema.Transaction.TYPE + " AS " + Contract.Transaction.TYPE + ", " +
                "t." + Schema.Transaction.WALLET + " AS " + Contract.Transaction.WALLET_ID + ", " +
                "w." + Schema.Wallet.NAME + " AS " + Contract.Transaction.WALLET_NAME + ", " +
                "w." + Schema.Wallet.ICON + " AS " + Contract.Transaction.WALLET_ICON + ", " +
                "w." + Schema.Wallet.CURRENCY + " AS " + Contract.Transaction.WALLET_CURRENCY + ", " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Transaction.WALLET_COUNT_IN_TOTAL + ", " +
                "w." + Schema.Wallet.ARCHIVED + " AS " + Contract.Transaction.WALLET_ARCHIVED + ", " +
                "w." + Schema.Wallet.TAG + " AS " + Contract.Transaction.WALLET_TAG + ", " +
                "t." + Schema.Transaction.PLACE + " AS " + Contract.Transaction.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.Transaction.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.Transaction.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.Transaction.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.Transaction.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.Transaction.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.Transaction.PLACE_TAG + ", " +
                "t." + Schema.Transaction.EVENT + " AS " + Contract.Transaction.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.Transaction.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.Transaction.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.Transaction.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.Transaction.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.Transaction.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.Transaction.EVENT_TAG + ", " +
                "t." + Schema.Transaction.NOTE + " AS " + Contract.Transaction.NOTE + ", " +
                "t." + Schema.Transaction.DEBT + " AS " + Contract.Transaction.DEBT_ID + ", " +
                "t." + Schema.Transaction.SAVING + " AS " + Contract.Transaction.SAVING_ID + ", " +
                "t." + Schema.Transaction.CONFIRMED + " AS " + Contract.Transaction.CONFIRMED + ", " +
                "t." + Schema.Transaction.COUNT_IN_TOTAL + " AS " + Contract.Transaction.COUNT_IN_TOTAL + ", " +
                "t." + Schema.Transaction.TAG + " AS " + Contract.Transaction.TAG + ", " +
                "GROUP_CONCAT('<' || pe." + Schema.Person.ID + " || '>') AS " + Contract.Transaction.PEOPLE_IDS + " " +
                "FROM (" +
                // case budget of type 0
                "SELECT t.* FROM(SELECT b.*, w." + Schema.Wallet.ID + " AS _wallet_id, " +
                "w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " +
                Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON " +
                "bw._budget = b." + Schema.Budget.ID + " JOIN " + Schema.Wallet.TABLE + " AS w ON " +
                "bw._wallet = w." + Schema.Wallet.ID + " WHERE bw. " + Schema.BudgetWallet.DELETED +
                " = 0 AND b." + Schema.Budget.DELETED + " = 0 AND w." + Schema.Wallet.DELETED +
                " = 0 AND b." + Schema.Budget.TYPE + " = " + Schema.BudgetType.EXPENSES +
                " ) AS b LEFT JOIN " + Schema.Transaction.TABLE + " AS t ON b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND t." + Schema.Transaction.DELETED + " = 0 AND t." +
                Schema.Transaction.DIRECTION + " = 0 AND DATETIME(t." + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') AND DATE(t." + Schema.Transaction.DATE +
                ") >= DATE(b." + Schema.Budget.START_DATE + ") AND DATE(t." + Schema.Transaction.DATE +
                ") <= DATE(b." + Schema.Budget.END_DATE + ") WHERE b." + Schema.Budget.ID + " = " +
                String.valueOf(budgetId) + " UNION " +
                // case budget of type 1
                "SELECT t.* FROM(SELECT b.*, w." + Schema.Wallet.ID + " AS _wallet_id, " +
                "w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " +
                Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON " +
                "bw._budget = b." + Schema.Budget.ID + " JOIN " + Schema.Wallet.TABLE + " AS w ON " +
                "bw._wallet = w." + Schema.Wallet.ID + " WHERE bw. " + Schema.BudgetWallet.DELETED +
                " = 0 AND b." + Schema.Budget.DELETED + " = 0 AND w." + Schema.Wallet.DELETED +
                " = 0 AND b." + Schema.Budget.TYPE + " = " + Schema.BudgetType.INCOMES +
                " ) AS b LEFT JOIN " + Schema.Transaction.TABLE + " AS t ON b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND t." + Schema.Transaction.DELETED + " = 0 AND t." +
                Schema.Transaction.DIRECTION + " = 1 AND DATETIME(t." + Schema.Transaction.DATE +
                ") <= DATETIME('now', 'localtime') AND DATE(t." + Schema.Transaction.DATE +
                ") >= DATE(b." + Schema.Budget.START_DATE + ") AND DATE(t." +
                Schema.Transaction.DATE + ") <= DATE(b." + Schema.Budget.END_DATE + ") WHERE b."
                + Schema.Budget.ID + " = " + String.valueOf(budgetId) + " UNION " +
                // case budget of type 2
                "SELECT t.* FROM(SELECT b.*, w." + Schema.Wallet.ID + " AS _wallet_id, " +
                "w." + Schema.Wallet.CURRENCY + " AS _wallet_currency, " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS _wallet_total FROM " +
                Schema.BudgetWallet.TABLE + " AS bw JOIN " + Schema.Budget.TABLE + " AS b ON " +
                "bw._budget = b." + Schema.Budget.ID + " JOIN " + Schema.Wallet.TABLE + " AS w ON " +
                "bw._wallet = w." + Schema.Wallet.ID + " WHERE bw. " + Schema.BudgetWallet.DELETED +
                " = 0 AND b." + Schema.Budget.DELETED + " = 0 AND w." + Schema.Wallet.DELETED +
                " = 0 AND b." + Schema.Budget.TYPE + " = " + Schema.BudgetType.CATEGORY +
                ") AS b LEFT JOIN " + Schema.Transaction.TABLE + " AS t ON b._wallet_id = t." +
                Schema.Transaction.WALLET + " AND t." + Schema.Transaction.DELETED + " = 0 JOIN " +
                Schema.Category.TABLE + " AS tc ON t." + Schema.Transaction.CATEGORY + " = tc." +
                Schema.Category.ID + " AND tc." + Schema.Category.DELETED + " = 0 WHERE (b." +
                Schema.Budget.CATEGORY + " = " + Schema.Transaction.CATEGORY + " OR b." +
                Schema.Budget.CATEGORY + " = tc."+ Schema.Category.PARENT + ") " +
                "AND DATETIME(t." + Schema.Transaction.DATE + ") <= DATETIME('now', 'localtime') " +
                "AND DATE(t." + Schema.Transaction.DATE + ") >= DATE(b." +
                Schema.Budget.START_DATE + ") AND DATE(t." + Schema.Transaction.DATE +
                ") <=  DATE(b." + Schema.Budget.END_DATE + ") " + "AND b." + Schema.Budget.ID +
                " = " + String.valueOf(budgetId) + ") AS t LEFT JOIN " + Schema.Category.TABLE +
                " AS c ON t." + Schema.Transaction.CATEGORY + " = c." + Schema.Category.ID + " AND c." +
                Schema.Category.DELETED + " = 0 JOIN " + Schema.Wallet.TABLE + " AS w ON t." +
                Schema.Transaction.WALLET + " = w." + Schema.Wallet.ID + " AND w." +
                Schema.Wallet.DELETED + " = 0 LEFT JOIN " + Schema.TransactionPeople.TABLE + " AS tp ON t." +
                Schema.Transaction.ID + " = tp." + Schema.TransactionPeople.TRANSACTION + " AND tp." +
                Schema.TransactionPeople.DELETED + " = 0 LEFT JOIN " + Schema.Person.TABLE + " AS pe ON tp." +
                Schema.TransactionPeople.PERSON + " = pe." + Schema.Person.ID + " AND pe." +
                Schema.Person.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON t." +
                Schema.Transaction.PLACE + " = " + Schema.Place.ID + " AND p." + Schema.Place.DELETED +
                " = 0 LEFT JOIN " + Schema.Event.TABLE + " AS e ON t." + Schema.Transaction.EVENT +
                " = e." + Schema.Event.ID + " AND e." + Schema.Event.DELETED + " = 0 WHERE t." +
                Schema.Transaction.DELETED + " = 0 GROUP BY t." + Schema.Transaction.ID;
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new budget.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertBudget(ContentValues contentValues) {
        long[] walletIds = parseIds(contentValues.getAsString(Contract.Budget.WALLET_IDS));
        if (walletIds == null || walletIds.length == 0) {
            throw new SQLiteDataException(Contract.ErrorCode.WALLETS_NOT_FOUND, "No wallet id provided");
        }
        checkWalletsConsistency(walletIds);
        ContentValues cv = new ContentValues();
        cv.put(Schema.Budget.TYPE, contentValues.getAsInteger(Contract.Budget.TYPE));
        cv.put(Schema.Budget.CATEGORY, contentValues.getAsLong(Contract.Budget.CATEGORY_ID));
        cv.put(Schema.Budget.START_DATE, contentValues.getAsString(Contract.Budget.START_DATE));
        cv.put(Schema.Budget.END_DATE, contentValues.getAsString(Contract.Budget.END_DATE));
        cv.put(Schema.Budget.MONEY, contentValues.getAsLong(Contract.Budget.MONEY));
        cv.put(Schema.Budget.CURRENCY, contentValues.getAsString(Contract.Budget.CURRENCY));
        cv.put(Schema.Budget.TAG, contentValues.getAsString(Contract.Budget.TAG));
        cv.put(Schema.Budget.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Budget.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Budget.DELETED, false);
        long budgetId = getWritableDatabase().insert(Schema.Budget.TABLE, null, cv);
        if (budgetId > 0) {
            // now insert a tuple for every wallet associated with this budget
            for (long walletId : walletIds) {
                ContentValues cvw = new ContentValues();
                cvw.put(Schema.BudgetWallet.BUDGET, budgetId);
                cvw.put(Schema.BudgetWallet.WALLET, walletId);
                cvw.put(Schema.BudgetWallet.UUID, UUID.randomUUID().toString());
                cvw.put(Schema.BudgetWallet.LAST_EDIT, System.currentTimeMillis());
                cvw.put(Schema.BudgetWallet.DELETED, false);
                getWritableDatabase().insert(Schema.BudgetWallet.TABLE, null, cvw);
            }
        }
        return budgetId;
    }

    /**
     * This method is called by the content provider when the user is updating an existing budget.
     *
     * @param budgetId id of the budget to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateBudget(long budgetId, ContentValues contentValues) {
        long[] walletIds = parseIds(contentValues.getAsString(Contract.Budget.WALLET_IDS));
        if (walletIds == null || walletIds.length == 0) {
            throw new SQLiteDataException(Contract.ErrorCode.WALLETS_NOT_FOUND, "No wallet id provided");
        }
        checkWalletsConsistency(walletIds);
        ContentValues cv = new ContentValues();
        cv.put(Schema.Budget.TYPE, contentValues.getAsInteger(Contract.Budget.TYPE));
        cv.put(Schema.Budget.CATEGORY, contentValues.getAsLong(Contract.Budget.CATEGORY_ID));
        cv.put(Schema.Budget.START_DATE, contentValues.getAsString(Contract.Budget.START_DATE));
        cv.put(Schema.Budget.END_DATE, contentValues.getAsString(Contract.Budget.END_DATE));
        cv.put(Schema.Budget.MONEY, contentValues.getAsLong(Contract.Budget.MONEY));
        cv.put(Schema.Budget.CURRENCY, contentValues.getAsString(Contract.Budget.CURRENCY));
        if (contentValues.containsKey(Contract.Budget.TAG)) {
            cv.put(Schema.Budget.TAG, contentValues.getAsString(Contract.Budget.TAG));
        }
        cv.put(Schema.Budget.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Budget.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(budgetId)};
        int rows = getWritableDatabase().update(Schema.Budget.TABLE, cv, where, whereArgs);
        if (rows > 0) {
            // In this case we have to check if the new provided wallet ids is changed. We could
            // query the current list, compute two sets containing the items to flag as deleted and
            // and the items to add but it is not enough fast. We can flag all the current items as
            // deleted and then add all the ids as new items but checking for conflict. In case of
            // conflict (same <budgetId,walletId> tuple) we update the deleted flag only.
            cv = new ContentValues();
            cv.put(Schema.BudgetWallet.DELETED, true);
            where = Schema.BudgetWallet.BUDGET + " = ?";
            whereArgs = new String[]{String.valueOf(budgetId)};
            getWritableDatabase().update(Schema.BudgetWallet.TABLE, cv, where, whereArgs);
            // All the current wallets associated with this budget are flagged as deleted, now it's
            // time to add (checking for conflicts) the new ids.
            for (long walletId : walletIds) {
                cv = new ContentValues();
                cv.put(Schema.BudgetWallet.BUDGET, budgetId);
                cv.put(Schema.BudgetWallet.WALLET, walletId);
                cv.put(Schema.BudgetWallet.UUID, UUID.randomUUID().toString());
                cv.put(Schema.BudgetWallet.LAST_EDIT, System.currentTimeMillis());
                cv.put(Schema.BudgetWallet.DELETED, false);
                long newId = getWritableDatabase().insertWithOnConflict(Schema.BudgetWallet.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                if (newId == -1L) {
                    // In this case the tuple <budgetId,walletId> already exists inside the table!
                    // We have to simply update the deleted flag and the update timestamp.
                    cv = new ContentValues();
                    cv.put(Schema.BudgetWallet.LAST_EDIT, System.currentTimeMillis());
                    cv.put(Schema.BudgetWallet.DELETED, false);
                    where = Schema.BudgetWallet.BUDGET + " = ? AND " + Schema.BudgetWallet.WALLET + " = ?";
                    whereArgs = new String[]{String.valueOf(budgetId), String.valueOf(walletId)};
                    getWritableDatabase().update(Schema.BudgetWallet.TABLE, cv, where, whereArgs);
                }
            }
        }
        return rows;
    }

    /**
     * This method is used internally to check if the given array of wallet ids is consistent.
     * If two or more different currencies are found, an exception is thrown.
     * @param walletIds array of wallet id.
     * @throws SQLiteDataException if one of the id is not found or wallets are not consistent.
     */
    private void checkWalletsConsistency(long[] walletIds) {
        String savedCurrency = null;
        String[] projection = new String[] {
                Contract.Wallet.CURRENCY
        };
        for (long walletId : walletIds) {
            Cursor cursor = getWallet(walletId, projection);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String currency = cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY));
                    if (savedCurrency != null) {
                        if (!TextUtils.equals(savedCurrency, currency)) {
                            String message = String.format(Locale.ENGLISH, "Wallet currency is not consistent (found %s and %s)", savedCurrency, currency);
                            throw new SQLiteDataException(Contract.ErrorCode.WALLETS_NOT_CONSISTENT, message);
                        }
                    } else {
                        savedCurrency = currency;
                    }
                } else {
                    String message = String.format(Locale.ENGLISH, "Wallet (id: %d) not found", walletId);
                    throw new SQLiteDataException(Contract.ErrorCode.WALLETS_NOT_FOUND, message);
                }
            }
        }
    }

    /**
     * Delete a budget from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param budgetId id of the budget to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteBudget(long budgetId) {
        // remove all BudgetWallet
        String where = Schema.BudgetWallet.BUDGET + " = ?";
        String[] whereArgs = new String[]{String.valueOf(budgetId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.BudgetWallet.DELETED, true);
            cv.put(Schema.BudgetWallet.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.BudgetWallet.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.BudgetWallet.TABLE, where, whereArgs);
        }
        // remove the debt item
        where = Schema.Budget.ID + " = ?";
        whereArgs = new String[]{String.valueOf(budgetId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Budget.DELETED, true);
            cv.put(Schema.Budget.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Budget.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Budget.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific saving.
     *
     * @param id of the saving.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getSaving(long id, String[] projection) {
        String selection = Schema.Saving.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getSavings(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the savings.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getSavings(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Saving.ID + " AS " + Contract.Saving.ID + ", " +
                Schema.Saving.DESCRIPTION + " AS " + Contract.Saving.DESCRIPTION + ", " +
                Schema.Saving.ICON + " AS " + Contract.Saving.ICON + ", " +
                Schema.Saving.START_MONEY + " AS " + Contract.Saving.START_MONEY + ", " +
                Schema.Saving.END_MONEY + " AS " + Contract.Saving.END_MONEY + ", " +
                Schema.Saving.WALLET + " AS " + Contract.Saving.WALLET_ID + ", " +
                Schema.Wallet.NAME + " AS " + Contract.Saving.WALLET_NAME + ", " +
                Schema.Wallet.ICON + " AS " + Contract.Saving.WALLET_ICON + ", " +
                Schema.Wallet.CURRENCY + " AS " + Contract.Saving.WALLET_CURRENCY + ", " +
                Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.Saving.WALLET_COUNT_IN_TOTAL + ", " +
                Schema.Wallet.ARCHIVED + " AS " + Contract.Saving.WALLET_ARCHIVED + ", " +
                Schema.Wallet.TAG + " AS " + Contract.Saving.WALLET_TAG + ", " +
                Schema.Saving.END_DATE + " AS " + Contract.Saving.END_DATE + ", " +
                Schema.Saving.COMPLETE + " AS " + Contract.Saving.COMPLETE + ", " +
                Schema.Saving.NOTE + " AS " + Contract.Saving.NOTE + ", " +
                Schema.Saving.TAG + " AS " + Contract.Saving.TAG + ", " +
                "_progress AS " + Contract.Saving.PROGRESS + " FROM " + Schema.Saving.TABLE +
                " AS s LEFT JOIN " + Schema.Wallet.TABLE + " ON " + Schema.Saving.WALLET + " = " +
                Schema.Wallet.ID + " LEFT JOIN (SELECT " + Schema.Transaction.SAVING + " AS _saving, " +
                " SUM(((" + Schema.Transaction.DIRECTION + " * -2) + 1) * " + Schema.Transaction.MONEY +
                ") AS _progress FROM " + Schema.Transaction.TABLE + " AS j LEFT JOIN " + Schema.Category.TABLE +
                " ON " + Schema.Transaction.CATEGORY + " = " + Schema.Category.ID + " WHERE j." +
                Schema.Transaction.DELETED + " = 0 AND " + Schema.Transaction.CONFIRMED +
                " = 1 AND (" + Schema.Category.TAG + " = '" + Schema.CategoryTag.SAVING_DEPOSIT + "' OR " +
                Schema.Category.TAG + " = '" + Schema.CategoryTag.SAVING_WITHDRAW + "') AND DATETIME(" +
                Schema.Transaction.DATE + ") <= DATETIME('now', 'localtime') GROUP BY _saving) ON " +
                Schema.Saving.ID + " = _saving WHERE s." + Schema.Saving.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given saving.
     *
     * @param savingId id of the saving.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getSavingTransactions(long savingId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String _selection = Schema.Transaction.SAVING + " = ?";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 1];
        _selectionArgs[0] = String.valueOf(savingId);
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 1, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new saving.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertSaving(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Saving.DESCRIPTION, contentValues.getAsString(Contract.Saving.DESCRIPTION));
        cv.put(Schema.Saving.ICON, contentValues.getAsString(Contract.Saving.ICON));
        cv.put(Schema.Saving.START_MONEY, contentValues.getAsLong(Contract.Saving.START_MONEY));
        cv.put(Schema.Saving.END_MONEY, contentValues.getAsLong(Contract.Saving.END_MONEY));
        cv.put(Schema.Saving.WALLET, contentValues.getAsLong(Contract.Saving.WALLET_ID));
        cv.put(Schema.Saving.END_DATE, contentValues.getAsString(Contract.Saving.END_DATE));
        cv.put(Schema.Saving.COMPLETE, contentValues.getAsBoolean(Contract.Saving.COMPLETE));
        cv.put(Schema.Saving.NOTE, contentValues.getAsString(Contract.Saving.NOTE));
        cv.put(Schema.Saving.TAG, contentValues.getAsString(Contract.Saving.TAG));
        cv.put(Schema.Saving.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Saving.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Saving.DELETED, false);
        return getWritableDatabase().insert(Schema.Saving.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing saving.
     *
     * @param savingId id of the saving to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateSaving(long savingId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        if (contentValues.containsKey(Contract.Saving.COMPLETE)) {
            cv.put(Schema.Saving.COMPLETE, contentValues.getAsBoolean(Contract.Saving.COMPLETE));
        }
        if (contentValues.containsKey(Contract.Saving.DESCRIPTION)) {
            cv.put(Schema.Saving.DESCRIPTION, contentValues.getAsString(Contract.Saving.DESCRIPTION));
        }
        if (contentValues.containsKey(Contract.Saving.ICON)) {
            cv.put(Schema.Saving.ICON, contentValues.getAsString(Contract.Saving.ICON));
        }
        if (contentValues.containsKey(Contract.Saving.START_MONEY)) {
            cv.put(Schema.Saving.START_MONEY, contentValues.getAsLong(Contract.Saving.START_MONEY));
        }
        if (contentValues.containsKey(Contract.Saving.END_MONEY)) {
            cv.put(Schema.Saving.END_MONEY, contentValues.getAsLong(Contract.Saving.END_MONEY));
        }
        if (contentValues.containsKey(Contract.Saving.WALLET_ID)) {
            cv.put(Schema.Saving.WALLET, contentValues.getAsLong(Contract.Saving.WALLET_ID));
        }
        if (contentValues.containsKey(Contract.Saving.END_DATE)) {
            cv.put(Schema.Saving.END_DATE, contentValues.getAsString(Contract.Saving.END_DATE));
        }
        if (contentValues.containsKey(Contract.Saving.NOTE)) {
            cv.put(Schema.Saving.NOTE, contentValues.getAsString(Contract.Saving.NOTE));
        }
        if (contentValues.containsKey(Contract.Saving.TAG)) {
            cv.put(Schema.Saving.TAG, contentValues.getAsString(Contract.Saving.TAG));
        }
        cv.put(Schema.Saving.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Saving.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(savingId)};
        return getWritableDatabase().update(Schema.Saving.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a saving from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param savingId id of the saving to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteSaving(long savingId) {
        // remove all the transactions of this saving
        deleteTransactionItems(getSavingTransactions(savingId, new String[] {Contract.Transaction.ID}, null, null, null));
        // delete the saving item
        String where = Schema.Saving.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(savingId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Saving.DELETED, true);
            cv.put(Schema.Saving.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Saving.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Saving.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific event.
     *
     * @param id of the event.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getEvent(long id, String[] projection) {
        String selection = Schema.Event.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getEvents(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the events.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getEvents(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Event.ID + " AS " + Contract.Event.ID + ", " +
                Schema.Event.NAME + " AS " + Contract.Event.NAME + ", " +
                Schema.Event.ICON + " AS " + Contract.Event.ICON + ", " +
                Schema.Event.NOTE + " AS " + Contract.Event.NOTE + ", " +
                Schema.Event.START_DATE + " AS " + Contract.Event.START_DATE + ", " +
                Schema.Event.END_DATE + " AS " + Contract.Event.END_DATE + ", " +
                Schema.Event.TAG + " AS " + Contract.Event.TAG + ", " +
                "_progress AS " + Contract.Event.PROGRESS + " FROM " + Schema.Event.TABLE + " AS e" +
                " LEFT JOIN (SELECT *, GROUP_CONCAT(wallet_currency || ' ' || _currency_money) AS " +
                "_progress FROM (SELECT * , SUM (((" + Schema.Transaction.DIRECTION + " * 2) - 1) * " +
                Schema.Transaction.MONEY + ") AS _currency_money FROM " + Schema.Transaction.TABLE +
                " AS t JOIN " + Schema.Wallet.TABLE + " AS w ON " + Schema.Transaction.WALLET + " = " +
                Schema.Wallet.ID + " WHERE " + Schema.Transaction.EVENT + " IS NOT NULL AND " +
                "DATETIME(" + Schema.Transaction.DATE + ") <= DATETIME('now', 'localtime')" +
                " AND " + Schema.Transaction.CONFIRMED + " = 1 AND t." + Schema.Transaction.DELETED +
                " = 0 AND w." + Schema.Wallet.DELETED + " = 0 GROUP BY " + Schema.Transaction.EVENT +
                ", wallet_currency) GROUP BY " + Schema.Transaction.EVENT + ") ON " + Schema.Event.ID +
                " = " + Schema.Transaction.EVENT + " WHERE e." + Schema.Event.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given event.
     *
     * @param eventId id of the event.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getEventTransactions(long eventId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String _selection = Schema.Transaction.EVENT + " = ?";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 1];
        _selectionArgs[0] = String.valueOf(eventId);
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 1, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new event.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertEvent(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Event.NAME, contentValues.getAsString(Contract.Event.NAME));
        cv.put(Schema.Event.ICON, contentValues.getAsString(Contract.Event.ICON));
        cv.put(Schema.Event.NOTE, contentValues.getAsString(Contract.Event.NOTE));
        cv.put(Schema.Event.START_DATE, contentValues.getAsString(Contract.Event.START_DATE));
        cv.put(Schema.Event.END_DATE, contentValues.getAsString(Contract.Event.END_DATE));
        cv.put(Schema.Event.TAG, contentValues.getAsString(Contract.Event.TAG));
        cv.put(Schema.Event.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Event.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Event.DELETED, false);
        return getWritableDatabase().insert(Schema.Event.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing event.
     *
     * @param eventId id of the event to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateEvent(long eventId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Event.NAME, contentValues.getAsString(Contract.Event.NAME));
        cv.put(Schema.Event.ICON, contentValues.getAsString(Contract.Event.ICON));
        cv.put(Schema.Event.NOTE, contentValues.getAsString(Contract.Event.NOTE));
        cv.put(Schema.Event.START_DATE, contentValues.getAsString(Contract.Event.START_DATE));
        cv.put(Schema.Event.END_DATE, contentValues.getAsString(Contract.Event.END_DATE));
        if (contentValues.containsKey(Contract.Event.TAG)) {
            cv.put(Schema.Event.TAG, contentValues.getAsString(Contract.Event.TAG));
        }
        cv.put(Schema.Event.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Event.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(eventId)};
        return getWritableDatabase().update(Schema.Event.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a event from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param eventId id of the event to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteEvent(long eventId) {
        // remove the event flag from all the transactions
        ContentValues cv = new ContentValues();
        cv.putNull(Schema.Transaction.EVENT);
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Transaction.EVENT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        // remove the event flag from all the transaction models
        cv = new ContentValues();
        cv.putNull(Schema.TransactionModel.EVENT);
        cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
        where = Schema.TransactionModel.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.TransactionModel.TABLE, cv, where, whereArgs);
        // remove the event flag from all the transfers
        cv = new ContentValues();
        cv.putNull(Schema.Transfer.EVENT);
        cv.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Transfer.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.Transfer.TABLE, cv, where, whereArgs);
        // remove the event flag from all the transfer models
        cv = new ContentValues();
        cv.putNull(Schema.TransferModel.EVENT);
        cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
        where = Schema.TransferModel.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.TransferModel.TABLE, cv, where, whereArgs);
        // remove the event flag from all the recurrent transactions
        cv = new ContentValues();
        cv.putNull(Schema.RecurrentTransaction.EVENT);
        cv.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
        where = Schema.RecurrentTransaction.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, cv, where, whereArgs);
        // remove the event flag from all the recurrent transfers
        cv = new ContentValues();
        cv.putNull(Schema.RecurrentTransfer.EVENT);
        cv.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
        where = Schema.RecurrentTransfer.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, cv, where, whereArgs);
        // remove all EventPeople
        where = Schema.EventPeople.EVENT + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        if (mCacheDeletedObjects) {
            cv = new ContentValues();
            cv.put(Schema.EventPeople.DELETED, true);
            cv.put(Schema.EventPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.EventPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.EventPeople.TABLE, where, whereArgs);
        }
        // finally remove the event item
        where = Schema.Event.ID + " = ?";
        whereArgs = new String[]{String.valueOf(eventId)};
        if (mCacheDeletedObjects) {
            cv = new ContentValues();
            cv.put(Schema.Event.DELETED, true);
            cv.put(Schema.Event.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Event.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Event.TABLE, where, whereArgs);
        }
    }

    /*package-local*/ Cursor getRecurrentTransaction(long transactionId, String[] projection) {
        String selection = Schema.RecurrentTransaction.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(transactionId)};
        return getRecurrentTransactions(projection, selection, selectionArgs, null);
    }

    /*package-local*/ Cursor getRecurrentTransactions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "rt." + Schema.RecurrentTransaction.ID + " AS " + Contract.RecurrentTransaction.ID + ", " +
                "rt." + Schema.RecurrentTransaction.MONEY + " AS " + Contract.RecurrentTransaction.MONEY + ", " +
                "rt." + Schema.RecurrentTransaction.DESCRIPTION + " AS " + Contract.RecurrentTransaction.DESCRIPTION + ", " +
                "rt." + Schema.RecurrentTransaction.CATEGORY + " AS " + Contract.RecurrentTransaction.CATEGORY_ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.RecurrentTransaction.CATEGORY_NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.RecurrentTransaction.CATEGORY_ICON + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.RecurrentTransaction.CATEGORY_TYPE + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.RecurrentTransaction.CATEGORY_SHOW_REPORT + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.RecurrentTransaction.CATEGORY_TAG + ", " +
                "rt." + Schema.RecurrentTransaction.DIRECTION + " AS " + Contract.RecurrentTransaction.DIRECTION + ", " +
                "rt." + Schema.RecurrentTransaction.WALLET + " AS " + Contract.RecurrentTransaction.WALLET_ID + ", " +
                "w." + Schema.Wallet.NAME + " AS " + Contract.RecurrentTransaction.WALLET_NAME + ", " +
                "w." + Schema.Wallet.ICON + " AS " + Contract.RecurrentTransaction.WALLET_ICON + ", " +
                "w." + Schema.Wallet.CURRENCY + " AS " + Contract.RecurrentTransaction.WALLET_CURRENCY + ", " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.RecurrentTransaction.WALLET_COUNT_IN_TOTAL + ", " +
                "w." + Schema.Wallet.ARCHIVED + " AS " + Contract.RecurrentTransaction.WALLET_ARCHIVED + ", " +
                "w." + Schema.Wallet.TAG + " AS " + Contract.RecurrentTransaction.WALLET_TAG + ", " +
                "rt." + Schema.RecurrentTransaction.PLACE + " AS " + Contract.RecurrentTransaction.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.RecurrentTransaction.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.RecurrentTransaction.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.RecurrentTransaction.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.RecurrentTransaction.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.RecurrentTransaction.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.RecurrentTransaction.PLACE_TAG + ", " +
                "rt." + Schema.RecurrentTransaction.NOTE + " AS " + Contract.RecurrentTransaction.NOTE + ", " +
                "rt." + Schema.RecurrentTransaction.EVENT + " AS " + Contract.RecurrentTransaction.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.RecurrentTransaction.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.RecurrentTransaction.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.RecurrentTransaction.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.RecurrentTransaction.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.RecurrentTransaction.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.RecurrentTransaction.EVENT_TAG + ", " +
                "rt." + Schema.RecurrentTransaction.CONFIRMED + " AS " + Contract.RecurrentTransaction.CONFIRMED + ", " +
                "rt." + Schema.RecurrentTransaction.COUNT_IN_TOTAL + " AS " + Contract.RecurrentTransaction.COUNT_IN_TOTAL + ", " +
                "rt." + Schema.RecurrentTransaction.START_DATE + " AS " + Contract.RecurrentTransaction.START_DATE + ", " +
                "rt." + Schema.RecurrentTransaction.LAST_OCCURRENCE + " AS " + Contract.RecurrentTransaction.LAST_OCCURRENCE + ", " +
                "rt." + Schema.RecurrentTransaction.NEXT_OCCURRENCE + " AS " + Contract.RecurrentTransaction.NEXT_OCCURRENCE + ", " +
                "rt." + Schema.RecurrentTransaction.RULE + " AS " + Contract.RecurrentTransaction.RULE + ", " +
                "rt." + Schema.RecurrentTransaction.TAG + " AS " + Contract.RecurrentTransaction.TAG + " " +
                "FROM " + Schema.RecurrentTransaction.TABLE + " AS rt JOIN " + Schema.Wallet.TABLE +
                " AS w ON rt." + Schema.RecurrentTransaction.WALLET + " = w." + Schema.Wallet.ID + " AND w." +
                Schema.Wallet.DELETED + " = 0 JOIN " + Schema.Category.TABLE + " AS c ON rt." +
                Schema.RecurrentTransaction.CATEGORY + " = c." + Schema.Category.ID + " AND c." +
                Schema.Category.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON rt." +
                Schema.RecurrentTransaction.PLACE + " = p." + Schema.Place.ID + " AND p." +
                Schema.Place.ID + " = 0 LEFT JOIN " + Schema.Event.TABLE + " AS e ON rt." +
                Schema.RecurrentTransaction.EVENT + " = e." + Schema.Event.ID + " AND e." +
                Schema.Event.DELETED + " = 0 WHERE rt." + Schema.RecurrentTransaction.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new recurring
     * transaction. This method will also detect if the recurring range is already started and it
     * will directly add all the missing transactions.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertRecurrentTransaction(ContentValues contentValues) {
        RecurrenceRule recurrenceRule;
        try {
            recurrenceRule = new RecurrenceRule(contentValues.getAsString(Contract.RecurrentTransaction.RULE));
        } catch (InvalidRecurrenceRuleException e) {
            throw new SQLiteDataException(Contract.ErrorCode.INVALID_RECURRENCE_RULE, e.getMessage());
        }
        String uuid = UUID.randomUUID().toString();
        ContentValues cv = new ContentValues();
        cv.put(Schema.RecurrentTransaction.MONEY, contentValues.getAsLong(Contract.RecurrentTransaction.MONEY));
        cv.put(Schema.RecurrentTransaction.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransaction.DESCRIPTION));
        cv.put(Schema.RecurrentTransaction.CATEGORY, contentValues.getAsLong(Contract.RecurrentTransaction.CATEGORY_ID));
        cv.put(Schema.RecurrentTransaction.DIRECTION, contentValues.getAsInteger(Contract.RecurrentTransaction.DIRECTION));
        cv.put(Schema.RecurrentTransaction.WALLET, contentValues.getAsLong(Contract.RecurrentTransaction.WALLET_ID));
        cv.put(Schema.RecurrentTransaction.PLACE, contentValues.getAsLong(Contract.RecurrentTransaction.PLACE_ID));
        cv.put(Schema.RecurrentTransaction.EVENT, contentValues.getAsLong(Contract.RecurrentTransaction.EVENT_ID));
        cv.put(Schema.RecurrentTransaction.NOTE, contentValues.getAsString(Contract.RecurrentTransaction.NOTE));
        cv.put(Schema.RecurrentTransaction.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransaction.CONFIRMED));
        cv.put(Schema.RecurrentTransaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransaction.COUNT_IN_TOTAL));
        cv.put(Schema.RecurrentTransaction.START_DATE, contentValues.getAsString(Contract.RecurrentTransaction.START_DATE));
        cv.put(Schema.RecurrentTransaction.LAST_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransaction.START_DATE));
        cv.put(Schema.RecurrentTransaction.RULE, contentValues.getAsString(Contract.RecurrentTransaction.RULE));
        cv.put(Schema.RecurrentTransaction.TAG, contentValues.getAsString(Contract.RecurrentTransaction.TAG));
        cv.put(Schema.RecurrentTransaction.UUID, uuid);
        cv.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.RecurrentTransaction.DELETED, false);
        long id = getWritableDatabase().insert(Schema.RecurrentTransaction.TABLE, null, cv);
        if (id > 0L) {
            // iterate from the starting date until the current date to check and insert some
            // transaction if already occurred and to correctly calculate the next occurrence
            String startDateString = contentValues.getAsString(Contract.RecurrentTransaction.START_DATE);
            Date startDate = DateUtils.getDateFromSQLDateString(startDateString);
            DateTime currentDateTime = DateUtils.getFixedDateTime(new Date());
            DateTime startDateTime = DateUtils.getFixedDateTime(startDate);
            DateTime lastOccurrence = DateUtils.getFixedDateTime(startDate);
            DateTime nextOccurrence = null;
            RecurrenceRuleIterator iterator = recurrenceRule.iterator(startDateTime);
            while (iterator.hasNext()) {
                DateTime nextInstance = iterator.nextDateTime();
                if (!nextInstance.after(currentDateTime)) {
                    Date transactionDate = DateUtils.getFixedDate(nextInstance);
                    ContentValues cvt = new ContentValues();
                    cvt.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.RecurrentTransaction.MONEY));
                    cvt.put(Schema.Transaction.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                    cvt.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransaction.DESCRIPTION));
                    cvt.put(Schema.Transaction.CATEGORY, contentValues.getAsLong(Contract.RecurrentTransaction.CATEGORY_ID));
                    cvt.put(Schema.Transaction.DIRECTION, contentValues.getAsInteger(Contract.RecurrentTransaction.DIRECTION));
                    cvt.put(Schema.Transaction.TYPE, Contract.TransactionType.STANDARD);
                    cvt.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.RecurrentTransaction.WALLET_ID));
                    cvt.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.RecurrentTransaction.PLACE_ID));
                    cvt.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.RecurrentTransaction.NOTE));
                    cvt.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.RecurrentTransaction.EVENT_ID));
                    cvt.put(Schema.Transaction.RECURRENCE, id);
                    cvt.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransaction.CONFIRMED));
                    cvt.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransaction.COUNT_IN_TOTAL));
                    cvt.put(Schema.Transaction.UUID, getRecurrentItemUUID(uuid, transactionDate));
                    cvt.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                    cvt.put(Schema.Transaction.DELETED, false);
                    getWritableDatabase().insert(Schema.Transaction.TABLE, null, cvt);
                    lastOccurrence = nextInstance;
                } else {
                    nextOccurrence = nextInstance;
                    break;
                }
            }
            // update the last_occurrence and next_occurrence field using the two calculated values
            // in order to correctly use the recurrence task to add transactions on the fly
            ContentValues cvu = new ContentValues();
            cvu.put(Schema.RecurrentTransaction.LAST_OCCURRENCE, DateUtils.getSQLDateString(DateUtils.getFixedDate(lastOccurrence)));
            cvu.put(Schema.RecurrentTransaction.NEXT_OCCURRENCE, nextOccurrence != null ? DateUtils.getSQLDateString(DateUtils.getFixedDate(nextOccurrence)) : null);
            String selection = Schema.RecurrentTransaction.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(id)};
            getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, cvu, selection, selectionArgs);
        }
        return id;
    }

    /*package-local*/ int updateRecurrentTransaction(long transactionId, ContentValues contentValues) {
        if (contentValues.containsKey(Contract.RecurrentTransaction.LAST_OCCURRENCE) && contentValues.containsKey(Contract.RecurrentTransaction.NEXT_OCCURRENCE)) {
            // in this case, the only thing to do is to update these values inside the database
            ContentValues cv = new ContentValues();
            cv.put(Schema.RecurrentTransaction.LAST_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransaction.LAST_OCCURRENCE));
            cv.put(Schema.RecurrentTransaction.NEXT_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransaction.NEXT_OCCURRENCE));
            cv.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
            String where = Schema.RecurrentTransaction.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(transactionId)};
            return getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, cv, where, selectionArgs);
        }
        return 0;
    }

    /**
     * Delete a recurrent transaction from the database. If the 'mCacheDeletedObjects' flag is
     * enabled the data is not removed but simply flagged as deleted. All the related transactions
     * are updated with the removal of the reference to this recurring transaction.
     *
     * @param transactionId id of the recurring transaction to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteRecurrentTransaction(long transactionId) {
        // the first step is to remove the reference to the recurrent
        // transaction from all the related transactions
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(Schema.Transaction.RECURRENCE);
        contentValues.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        String selection = Schema.Transaction.RECURRENCE + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(transactionId)};
        getWritableDatabase().update(Schema.Transaction.TABLE, contentValues, selection, selectionArgs);
        // now is possible to remove the recurrent transaction itself
        selection = Schema.RecurrentTransaction.ID + " = ?";
        selectionArgs = new String[]{String.valueOf(transactionId)};
        if (mCacheDeletedObjects) {
            contentValues = new ContentValues();
            contentValues.put(Schema.RecurrentTransaction.DELETED, true);
            contentValues.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, contentValues, selection, selectionArgs);
        } else {
            return getWritableDatabase().delete(Schema.RecurrentTransaction.TABLE, selection, selectionArgs);
        }
    }

    /*package-local*/ Cursor getRecurrentTransfer(long transferId, String[] projection) {
        String selection = Schema.RecurrentTransfer.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(transferId)};
        return getRecurrentTransfers(projection, selection, selectionArgs, null);
    }

    /*package-local*/ Cursor getRecurrentTransfers(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "rt." + Schema.RecurrentTransfer.ID + " AS " + Contract.RecurrentTransfer.ID + ", " +
                "rt." + Schema.RecurrentTransfer.DESCRIPTION + " AS " + Contract.RecurrentTransfer.DESCRIPTION + ", " +
                "rt." + Schema.RecurrentTransfer.WALLET_FROM + " AS " + Contract.RecurrentTransfer.WALLET_FROM_ID + ", " +
                "wf." + Schema.Wallet.NAME + " AS " + Contract.RecurrentTransfer.WALLET_FROM_NAME + ", " +
                "wf." + Schema.Wallet.ICON + " AS " + Contract.RecurrentTransfer.WALLET_FROM_ICON + ", " +
                "wf." + Schema.Wallet.CURRENCY + " AS " + Contract.RecurrentTransfer.WALLET_FROM_CURRENCY + ", " +
                "wf." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.RecurrentTransfer.WALLET_FROM_COUNT_IN_TOTAL + ", " +
                "wf." + Schema.Wallet.ARCHIVED + " AS " + Contract.RecurrentTransfer.WALLET_FROM_ARCHIVED + ", " +
                "wf." + Schema.Wallet.TAG + " AS " + Contract.RecurrentTransfer.WALLET_FROM_TAG + ", " +
                "rt." + Schema.RecurrentTransfer.WALLET_TO + " AS " + Contract.RecurrentTransfer.WALLET_TO_ID + ", " +
                "wt." + Schema.Wallet.NAME + " AS " + Contract.RecurrentTransfer.WALLET_TO_NAME + ", " +
                "wt." + Schema.Wallet.ICON + " AS " + Contract.RecurrentTransfer.WALLET_TO_ICON + ", " +
                "wt." + Schema.Wallet.CURRENCY + " AS " + Contract.RecurrentTransfer.WALLET_TO_CURRENCY + ", " +
                "wt." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.RecurrentTransfer.WALLET_TO_COUNT_IN_TOTAL + ", " +
                "wt." + Schema.Wallet.ARCHIVED + " AS " + Contract.RecurrentTransfer.WALLET_TO_ARCHIVED + ", " +
                "wt." + Schema.Wallet.TAG + " AS " + Contract.RecurrentTransfer.WALLET_TO_TAG + ", " +
                "rt." + Schema.RecurrentTransfer.MONEY_FROM + " AS " + Contract.RecurrentTransfer.MONEY_FROM + ", " +
                "rt." + Schema.RecurrentTransfer.MONEY_TO + " AS " + Contract.RecurrentTransfer.MONEY_TO + ", " +
                "rt." + Schema.RecurrentTransfer.MONEY_TAX + " AS " + Contract.RecurrentTransfer.MONEY_TAX + ", " +
                "rt." + Schema.RecurrentTransfer.NOTE + " AS " + Contract.RecurrentTransfer.NOTE + ", " +
                "rt." + Schema.RecurrentTransfer.EVENT + " AS " + Contract.RecurrentTransfer.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.RecurrentTransfer.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.RecurrentTransfer.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.RecurrentTransfer.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.RecurrentTransfer.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.RecurrentTransfer.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.RecurrentTransfer.EVENT_TAG + ", " +
                "rt." + Schema.RecurrentTransfer.PLACE + " AS " + Contract.RecurrentTransfer.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.RecurrentTransfer.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.RecurrentTransfer.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.RecurrentTransfer.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.RecurrentTransfer.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.RecurrentTransfer.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.RecurrentTransfer.PLACE_TAG + ", " +
                "rt." + Schema.RecurrentTransfer.CONFIRMED + " AS " + Contract.RecurrentTransfer.CONFIRMED + ", " +
                "rt." + Schema.RecurrentTransfer.COUNT_IN_TOTAL + " AS " + Contract.RecurrentTransfer.COUNT_IN_TOTAL + ", " +
                "rt." + Schema.RecurrentTransfer.START_DATE + " AS " + Contract.RecurrentTransfer.START_DATE + ", " +
                "rt." + Schema.RecurrentTransfer.LAST_OCCURRENCE + " AS " + Contract.RecurrentTransfer.LAST_OCCURRENCE + ", " +
                "rt." + Schema.RecurrentTransfer.NEXT_OCCURRENCE + " AS " + Contract.RecurrentTransfer.NEXT_OCCURRENCE + ", " +
                "rt." + Schema.RecurrentTransfer.RULE + " AS " + Contract.RecurrentTransfer.RULE + ", " +
                "rt." + Schema.RecurrentTransfer.TAG + " AS " + Contract.RecurrentTransfer.TAG + " " +
                "FROM " + Schema.RecurrentTransfer.TABLE + " AS rt JOIN " + Schema.Wallet.TABLE + " AS " +
                "wf ON rt." + Schema.RecurrentTransfer.WALLET_FROM + " = wf." + Schema.Wallet.ID +
                " AND wf." + Schema.Wallet.DELETED + " = 0 JOIN " + Schema.Wallet.TABLE + " AS " +
                "wt ON rt." + Schema.RecurrentTransfer.WALLET_TO + " = wt." + Schema.Wallet.ID +
                " AND wt." + Schema.Wallet.DELETED + " = 0 LEFT JOIN " + Schema.Event.TABLE +
                " AS e ON rt." + Schema.RecurrentTransfer.EVENT + " = e." + Schema.Event.ID + " AND e." +
                Schema.Event.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON rt." +
                Schema.RecurrentTransfer.PLACE + " = p." + Schema.Place.ID + " AND p." +
                Schema.Place.DELETED + " = 0 WHERE rt." + Schema.RecurrentTransfer.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new recurring
     * transfer. This method will also detect if the recurring range is already started and it
     * will directly add all the missing transfers.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertRecurrentTransfer(ContentValues contentValues) {
        RecurrenceRule recurrenceRule;
        try {
            recurrenceRule = new RecurrenceRule(contentValues.getAsString(Contract.RecurrentTransfer.RULE));
        } catch (InvalidRecurrenceRuleException e) {
            throw new SQLiteDataException(Contract.ErrorCode.INVALID_RECURRENCE_RULE, e.getMessage());
        }
        String uuid = UUID.randomUUID().toString();
        ContentValues cv = new ContentValues();
        cv.put(Schema.RecurrentTransfer.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransfer.DESCRIPTION));
        cv.put(Schema.RecurrentTransfer.WALLET_FROM, contentValues.getAsLong(Contract.RecurrentTransfer.WALLET_FROM_ID));
        cv.put(Schema.RecurrentTransfer.WALLET_TO, contentValues.getAsLong(Contract.RecurrentTransfer.WALLET_TO_ID));
        cv.put(Schema.RecurrentTransfer.MONEY_FROM, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_FROM));
        cv.put(Schema.RecurrentTransfer.MONEY_TO, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_TO));
        cv.put(Schema.RecurrentTransfer.MONEY_TAX, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_TAX));
        cv.put(Schema.RecurrentTransfer.NOTE, contentValues.getAsString(Contract.RecurrentTransfer.NOTE));
        cv.put(Schema.RecurrentTransfer.EVENT, contentValues.getAsLong(Contract.RecurrentTransfer.EVENT_ID));
        cv.put(Schema.RecurrentTransfer.PLACE, contentValues.getAsLong(Contract.RecurrentTransfer.PLACE_ID));
        cv.put(Schema.RecurrentTransfer.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransfer.CONFIRMED));
        cv.put(Schema.RecurrentTransfer.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransfer.COUNT_IN_TOTAL));
        cv.put(Schema.RecurrentTransfer.START_DATE, contentValues.getAsString(Contract.RecurrentTransfer.START_DATE));
        cv.put(Schema.RecurrentTransfer.LAST_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransfer.START_DATE));
        cv.put(Schema.RecurrentTransfer.RULE, contentValues.getAsString(Contract.RecurrentTransfer.RULE));
        cv.put(Schema.RecurrentTransfer.TAG, contentValues.getAsString(Contract.RecurrentTransfer.TAG));
        cv.put(Schema.RecurrentTransfer.UUID, uuid);
        cv.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.RecurrentTransfer.DELETED, false);
        long id = getWritableDatabase().insert(Schema.RecurrentTransfer.TABLE, null, cv);
        if (id > 0L) {
            // iterate from the starting date until the current date to check and insert some
            // transfer if already occurred and to correctly calculate the next occurrence
            String startDateString = contentValues.getAsString(Contract.RecurrentTransfer.START_DATE);
            Date startDate = DateUtils.getDateFromSQLDateString(startDateString);
            DateTime currentDateTime = DateUtils.getFixedDateTime(new Date());
            DateTime startDateTime = DateUtils.getFixedDateTime(startDate);
            DateTime lastOccurrence = DateUtils.getFixedDateTime(startDate);
            DateTime nextOccurrence = null;
            RecurrenceRuleIterator iterator = recurrenceRule.iterator(startDateTime);
            Long transferCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER);
            Long transferTaxCategoryId = getSystemCategoryId(Schema.CategoryTag.TRANSFER_TAX);
            while (iterator.hasNext()) {
                DateTime nextInstance = iterator.nextDateTime();
                if (!nextInstance.after(currentDateTime)) {
                    Date transactionDate = DateUtils.getFixedDate(nextInstance);
                    // insert source transaction
                    ContentValues cvt = new ContentValues();
                    cvt.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_FROM));
                    cvt.put(Schema.Transaction.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                    cvt.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransfer.DESCRIPTION));
                    cvt.put(Schema.Transaction.CATEGORY, transferCategoryId);
                    cvt.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
                    cvt.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
                    cvt.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.RecurrentTransfer.WALLET_FROM_ID));
                    cvt.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.RecurrentTransfer.PLACE_ID));
                    cvt.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.RecurrentTransfer.NOTE));
                    cvt.putNull(Schema.Transaction.SAVING);
                    cvt.putNull(Schema.Transaction.DEBT);
                    cvt.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.RecurrentTransfer.EVENT_ID));
                    cvt.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransfer.CONFIRMED));
                    cvt.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransfer.COUNT_IN_TOTAL));
                    cvt.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
                    cvt.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                    cvt.put(Schema.Transaction.DELETED, false);
                    long transaction1 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cvt);
                    // insert destination transaction
                    cvt = new ContentValues();
                    cvt.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_TO));
                    cvt.put(Schema.Transaction.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                    cvt.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransfer.DESCRIPTION));
                    cvt.put(Schema.Transaction.CATEGORY, transferCategoryId);
                    cvt.put(Schema.Transaction.DIRECTION, Contract.Direction.INCOME);
                    cvt.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
                    cvt.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.RecurrentTransfer.WALLET_TO_ID));
                    cvt.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.RecurrentTransfer.PLACE_ID));
                    cvt.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.RecurrentTransfer.NOTE));
                    cvt.putNull(Schema.Transaction.SAVING);
                    cvt.putNull(Schema.Transaction.DEBT);
                    cvt.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.RecurrentTransfer.EVENT_ID));
                    cvt.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransfer.CONFIRMED));
                    cvt.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransfer.COUNT_IN_TOTAL));
                    cvt.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
                    cvt.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                    cvt.put(Schema.Transaction.DELETED, false);
                    long transaction2 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cvt);
                    // tax transaction
                    Long transaction3 = null;
                    if (contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_TAX) != 0L) {
                        cvt = new ContentValues();
                        cvt.put(Schema.Transaction.MONEY, contentValues.getAsLong(Contract.RecurrentTransfer.MONEY_TAX));
                        cvt.put(Schema.Transaction.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                        cvt.put(Schema.Transaction.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransfer.DESCRIPTION));
                        cvt.put(Schema.Transaction.CATEGORY, transferTaxCategoryId);
                        cvt.put(Schema.Transaction.DIRECTION, Contract.Direction.EXPENSE);
                        cvt.put(Schema.Transaction.TYPE, Contract.TransactionType.TRANSFER);
                        cvt.put(Schema.Transaction.WALLET, contentValues.getAsLong(Contract.RecurrentTransfer.WALLET_FROM_ID));
                        cvt.put(Schema.Transaction.PLACE, contentValues.getAsLong(Contract.RecurrentTransfer.PLACE_ID));
                        cvt.put(Schema.Transaction.NOTE, contentValues.getAsString(Contract.RecurrentTransfer.NOTE));
                        cvt.putNull(Schema.Transaction.SAVING);
                        cvt.putNull(Schema.Transaction.DEBT);
                        cvt.put(Schema.Transaction.EVENT, contentValues.getAsLong(Contract.RecurrentTransfer.EVENT_ID));
                        cvt.put(Schema.Transaction.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransfer.CONFIRMED));
                        cvt.put(Schema.Transaction.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransfer.COUNT_IN_TOTAL));
                        cvt.put(Schema.Transaction.UUID, UUID.randomUUID().toString());
                        cvt.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
                        cvt.put(Schema.Transaction.DELETED, false);
                        transaction3 = getWritableDatabase().insert(Schema.Transaction.TABLE, null, cvt);
                    }
                    // now we can add the transfer tuple
                    cvt = new ContentValues();
                    cvt.put(Schema.Transfer.DESCRIPTION, contentValues.getAsString(Contract.RecurrentTransfer.DESCRIPTION));
                    cvt.put(Schema.Transfer.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                    cvt.put(Schema.Transfer.TRANSACTION_FROM, transaction1);
                    cvt.put(Schema.Transfer.TRANSACTION_TO, transaction2);
                    cvt.put(Schema.Transfer.TRANSACTION_TAX, transaction3);
                    cvt.put(Schema.Transfer.NOTE, contentValues.getAsString(Contract.RecurrentTransfer.NOTE));
                    cvt.put(Schema.Transfer.PLACE, contentValues.getAsLong(Contract.RecurrentTransfer.PLACE_ID));
                    cvt.put(Schema.Transfer.EVENT, contentValues.getAsLong(Contract.RecurrentTransfer.EVENT_ID));
                    cvt.put(Schema.Transfer.CONFIRMED, contentValues.getAsBoolean(Contract.RecurrentTransfer.CONFIRMED));
                    cvt.put(Schema.Transfer.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.RecurrentTransfer.COUNT_IN_TOTAL));
                    cvt.put(Schema.Transfer.TAG, contentValues.getAsString(Contract.RecurrentTransfer.TAG));
                    cvt.put(Schema.Transfer.UUID, getRecurrentItemUUID(uuid, transactionDate));
                    cvt.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
                    cvt.put(Schema.Transfer.DELETED, false);
                    getWritableDatabase().insert(Schema.Transfer.TABLE, null, cvt);
                    lastOccurrence = nextInstance;
                } else {
                    nextOccurrence = nextInstance;
                    break;
                }
            }
            // update the last_occurrence and next_occurrence field using the two calculated values
            // in order to correctly use the recurrence task to add transfers on the fly
            ContentValues cvu = new ContentValues();
            cvu.put(Schema.RecurrentTransfer.LAST_OCCURRENCE, DateUtils.getSQLDateString(DateUtils.getFixedDate(lastOccurrence)));
            cvu.put(Schema.RecurrentTransfer.NEXT_OCCURRENCE, nextOccurrence != null ? DateUtils.getSQLDateString(DateUtils.getFixedDate(nextOccurrence)) : null);
            String selection = Schema.RecurrentTransfer.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(id)};
            getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, cvu, selection, selectionArgs);
        }
        return id;
    }

    /*package-local*/ int updateRecurrentTransfer(long transferId, ContentValues contentValues) {
        if (contentValues.containsKey(Contract.RecurrentTransfer.LAST_OCCURRENCE) && contentValues.containsKey(Contract.RecurrentTransfer.NEXT_OCCURRENCE)) {
            // in this case, the only thing to do is to update these values inside the database
            ContentValues cv = new ContentValues();
            cv.put(Schema.RecurrentTransfer.LAST_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransfer.LAST_OCCURRENCE));
            cv.put(Schema.RecurrentTransfer.NEXT_OCCURRENCE, contentValues.getAsString(Contract.RecurrentTransfer.NEXT_OCCURRENCE));
            cv.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
            String where = Schema.RecurrentTransfer.ID + " = ?";
            String[] selectionArgs = new String[] {String.valueOf(transferId)};
            return getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, cv, where, selectionArgs);
        }
        return 0;
    }

    /**
     * Delete a recurrent transfer from the database. If the 'mCacheDeletedObjects' flag is
     * enabled the data is not removed but simply flagged as deleted. All the related transfers
     * are updated with the removal of the reference to this recurring transfer.
     *
     * @param transferId id of the recurring transfer to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteRecurrentTransfer(long transferId) {
        // the first step is to remove the reference to the recurrent
        // transfer from all the related transfers
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(Schema.Transfer.RECURRENCE);
        contentValues.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
        String selection = Schema.Transfer.RECURRENCE + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(transferId)};
        getWritableDatabase().update(Schema.Transfer.TABLE, contentValues, selection, selectionArgs);
        // now is possible to remove the recurrent transfer itself
        selection = Schema.RecurrentTransfer.ID + " = ?";
        selectionArgs = new String[]{String.valueOf(transferId)};
        if (mCacheDeletedObjects) {
            contentValues = new ContentValues();
            contentValues.put(Schema.RecurrentTransfer.DELETED, true);
            contentValues.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, contentValues, selection, selectionArgs);
        } else {
            return getWritableDatabase().delete(Schema.RecurrentTransfer.TABLE, selection, selectionArgs);
        }
    }

    /*package-local*/ long insertRecurrentTransferOccurrence(long transferId, ContentValues contentValues) {
        return -1L;
    }

    /**
     * This method is called by the content provider when the user is querying a specific
     * transaction model.
     *
     * @param modelId id of the transaction model.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getTransactionModel(long modelId, String[] projection) {
        String selection = Schema.TransactionModel.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(modelId)};
        return getTransactionModels(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the transaction
     * models.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransactionModels(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "tm." + Schema.TransactionModel.ID + " AS " + Contract.TransactionModel.ID + ", " +
                "tm." + Schema.TransactionModel.MONEY + " AS " + Contract.TransactionModel.MONEY + ", " +
                "tm." + Schema.TransactionModel.DESCRIPTION + " AS " + Contract.TransactionModel.DESCRIPTION + ", " +
                "tm." + Schema.TransactionModel.CATEGORY + " AS " + Contract.TransactionModel.CATEGORY_ID + ", " +
                "c." + Schema.Category.NAME + " AS " + Contract.TransactionModel.CATEGORY_NAME + ", " +
                "c." + Schema.Category.ICON + " AS " + Contract.TransactionModel.CATEGORY_ICON + ", " +
                "c." + Schema.Category.TYPE + " AS " + Contract.TransactionModel.CATEGORY_TYPE + ", " +
                "c." + Schema.Category.SHOW_REPORT + " AS " + Contract.TransactionModel.CATEGORY_SHOW_REPORT + ", " +
                "c." + Schema.Category.TAG + " AS " + Contract.TransactionModel.CATEGORY_TAG + ", " +
                "tm." + Schema.TransactionModel.DIRECTION + " AS " + Contract.TransactionModel.DIRECTION + ", " +
                "tm." + Schema.TransactionModel.WALLET + " AS " + Contract.TransactionModel.WALLET_ID + ", " +
                "w." + Schema.Wallet.NAME + " AS " + Contract.TransactionModel.WALLET_NAME + ", " +
                "w." + Schema.Wallet.ICON + " AS " + Contract.TransactionModel.WALLET_ICON + ", " +
                "w." + Schema.Wallet.CURRENCY + " AS " + Contract.TransactionModel.WALLET_CURRENCY + ", " +
                "w." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.TransactionModel.WALLET_COUNT_IN_TOTAL + ", " +
                "w." + Schema.Wallet.ARCHIVED + " AS " + Contract.TransactionModel.WALLET_ARCHIVED + ", " +
                "w." + Schema.Wallet.TAG + " AS " + Contract.TransactionModel.WALLET_TAG + ", " +
                "tm." + Schema.TransactionModel.PLACE + " AS " + Contract.TransactionModel.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.TransactionModel.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.TransactionModel.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.TransactionModel.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.TransactionModel.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.TransactionModel.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.TransactionModel.PLACE_TAG + ", " +
                "tm." + Schema.TransactionModel.NOTE + " AS " + Contract.TransactionModel.NOTE + ", " +
                "tm." + Schema.TransactionModel.EVENT + " AS " + Contract.TransactionModel.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.TransactionModel.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.TransactionModel.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.TransactionModel.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.TransactionModel.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.TransactionModel.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.TransactionModel.EVENT_TAG + ", " +
                "tm." + Schema.TransactionModel.CONFIRMED + " AS " + Contract.TransactionModel.CONFIRMED + ", " +
                "tm." + Schema.TransactionModel.COUNT_IN_TOTAL + " AS " + Contract.TransactionModel.COUNT_IN_TOTAL + ", " +
                "tm." + Schema.TransactionModel.TAG + " AS " + Contract.TransactionModel.TAG + " " +
                "FROM " + Schema.TransactionModel.TABLE + " AS tm JOIN " + Schema.Wallet.TABLE +
                " AS w ON tm." + Schema.TransactionModel.WALLET + " = w." + Schema.Wallet.ID + " AND w." +
                Schema.Wallet.DELETED + " = 0 JOIN " + Schema.Category.TABLE + " AS c ON tm." +
                Schema.TransactionModel.CATEGORY + " = c." + Schema.Category.ID + " AND c." +
                Schema.Category.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON tm." +
                Schema.TransactionModel.PLACE + " = p." + Schema.Place.ID + " AND p." +
                Schema.Place.ID + " = 0 LEFT JOIN " + Schema.Event.TABLE + " AS e ON tm." +
                Schema.TransactionModel.EVENT + " = e." + Schema.Event.ID + " AND e." +
                Schema.Event.DELETED + " = 0 WHERE tm." + Schema.TransactionModel.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new transaction
     * model.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertTransactionModel(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.TransactionModel.MONEY, contentValues.getAsLong(Contract.TransactionModel.MONEY));
        cv.put(Schema.TransactionModel.DESCRIPTION, contentValues.getAsString(Contract.TransactionModel.DESCRIPTION));
        cv.put(Schema.TransactionModel.CATEGORY, contentValues.getAsLong(Contract.TransactionModel.CATEGORY_ID));
        cv.put(Schema.TransactionModel.DIRECTION, contentValues.getAsInteger(Contract.TransactionModel.DIRECTION));
        cv.put(Schema.TransactionModel.WALLET, contentValues.getAsLong(Contract.TransactionModel.WALLET_ID));
        cv.put(Schema.TransactionModel.PLACE, contentValues.getAsLong(Contract.TransactionModel.PLACE_ID));
        cv.put(Schema.TransactionModel.NOTE, contentValues.getAsString(Contract.TransactionModel.NOTE));
        cv.put(Schema.TransactionModel.EVENT, contentValues.getAsLong(Contract.TransactionModel.EVENT_ID));
        cv.put(Schema.TransactionModel.CONFIRMED, contentValues.getAsBoolean(Contract.TransactionModel.CONFIRMED));
        cv.put(Schema.TransactionModel.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.TransactionModel.COUNT_IN_TOTAL));
        cv.put(Schema.TransactionModel.TAG, contentValues.getAsString(Contract.TransactionModel.TAG));
        cv.put(Schema.TransactionModel.UUID, UUID.randomUUID().toString());
        cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.TransactionModel.DELETED, false);
        return getWritableDatabase().insert(Schema.TransactionModel.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing
     * transaction model.
     *
     * @param modelId id of the transaction model to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateTransactionModel(long modelId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.TransactionModel.MONEY, contentValues.getAsLong(Contract.TransactionModel.MONEY));
        cv.put(Schema.TransactionModel.DESCRIPTION, contentValues.getAsString(Contract.TransactionModel.DESCRIPTION));
        cv.put(Schema.TransactionModel.CATEGORY, contentValues.getAsLong(Contract.TransactionModel.CATEGORY_ID));
        cv.put(Schema.TransactionModel.DIRECTION, contentValues.getAsInteger(Contract.TransactionModel.DIRECTION));
        cv.put(Schema.TransactionModel.WALLET, contentValues.getAsLong(Contract.TransactionModel.WALLET_ID));
        cv.put(Schema.TransactionModel.PLACE, contentValues.getAsLong(Contract.TransactionModel.PLACE_ID));
        cv.put(Schema.TransactionModel.NOTE, contentValues.getAsString(Contract.TransactionModel.NOTE));
        cv.put(Schema.TransactionModel.EVENT, contentValues.getAsLong(Contract.TransactionModel.EVENT_ID));
        cv.put(Schema.TransactionModel.CONFIRMED, contentValues.getAsBoolean(Contract.TransactionModel.CONFIRMED));
        cv.put(Schema.TransactionModel.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.TransactionModel.COUNT_IN_TOTAL));
        if (contentValues.containsKey(Contract.TransactionModel.TAG)) {
            cv.put(Schema.TransactionModel.TAG, contentValues.getAsString(Contract.TransactionModel.TAG));
        }
        cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.TransactionModel.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(modelId)};
        return getWritableDatabase().update(Schema.TransactionModel.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a transaction model from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param modelId id of the transaction model to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteTransactionModel(long modelId) {
        String where = Schema.TransactionModel.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(modelId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransactionModel.DELETED, true);
            cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.TransactionModel.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.TransactionModel.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific transfer
     * model.
     *
     * @param modelId id of the transfer model.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getTransferModel(long modelId, String[] projection) {
        String selection = Schema.TransferModel.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(modelId)};
        return getTransferModels(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the transfer
     * models.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getTransferModels(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                "tm." + Schema.TransferModel.ID + " AS " + Contract.TransferModel.ID + ", " +
                "tm." + Schema.TransferModel.DESCRIPTION + " AS " + Contract.TransferModel.DESCRIPTION + ", " +
                "tm." + Schema.TransferModel.WALLET_FROM + " AS " + Contract.TransferModel.WALLET_FROM_ID + ", " +
                "wf." + Schema.Wallet.NAME + " AS " + Contract.TransferModel.WALLET_FROM_NAME + ", " +
                "wf." + Schema.Wallet.ICON + " AS " + Contract.TransferModel.WALLET_FROM_ICON + ", " +
                "wf." + Schema.Wallet.CURRENCY + " AS " + Contract.TransferModel.WALLET_FROM_CURRENCY + ", " +
                "wf." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.TransferModel.WALLET_FROM_COUNT_IN_TOTAL + ", " +
                "wf." + Schema.Wallet.ARCHIVED + " AS " + Contract.TransferModel.WALLET_FROM_ARCHIVED + ", " +
                "wf." + Schema.Wallet.TAG + " AS " + Contract.TransferModel.WALLET_FROM_TAG + ", " +
                "tm." + Schema.TransferModel.WALLET_TO + " AS " + Contract.TransferModel.WALLET_TO_ID + ", " +
                "wt." + Schema.Wallet.NAME + " AS " + Contract.TransferModel.WALLET_TO_NAME + ", " +
                "wt." + Schema.Wallet.ICON + " AS " + Contract.TransferModel.WALLET_TO_ICON + ", " +
                "wt." + Schema.Wallet.CURRENCY + " AS " + Contract.TransferModel.WALLET_TO_CURRENCY + ", " +
                "wt." + Schema.Wallet.COUNT_IN_TOTAL + " AS " + Contract.TransferModel.WALLET_TO_COUNT_IN_TOTAL + ", " +
                "wt." + Schema.Wallet.ARCHIVED + " AS " + Contract.TransferModel.WALLET_TO_ARCHIVED + ", " +
                "wt." + Schema.Wallet.TAG + " AS " + Contract.TransferModel.WALLET_TO_TAG + ", " +
                "tm." + Schema.TransferModel.MONEY_FROM + " AS " + Contract.TransferModel.MONEY_FROM + ", " +
                "tm." + Schema.TransferModel.MONEY_TO + " AS " + Contract.TransferModel.MONEY_TO + ", " +
                "tm." + Schema.TransferModel.MONEY_TAX + " AS " + Contract.TransferModel.MONEY_TAX + ", " +
                "tm." + Schema.TransferModel.NOTE + " AS " + Contract.TransferModel.NOTE + ", " +
                "tm." + Schema.TransferModel.EVENT + " AS " + Contract.TransferModel.EVENT_ID + ", " +
                "e." + Schema.Event.NAME + " AS " + Contract.TransferModel.EVENT_NAME + ", " +
                "e." + Schema.Event.ICON + " AS " + Contract.TransferModel.EVENT_ICON + ", " +
                "e." + Schema.Event.NOTE + " AS " + Contract.TransferModel.EVENT_NOTE + ", " +
                "e." + Schema.Event.START_DATE + " AS " + Contract.TransferModel.EVENT_START_DATE + ", " +
                "e." + Schema.Event.END_DATE + " AS " + Contract.TransferModel.EVENT_END_DATE + ", " +
                "e." + Schema.Event.TAG + " AS " + Contract.TransferModel.EVENT_TAG + ", " +
                "tm." + Schema.TransferModel.PLACE + " AS " + Contract.TransferModel.PLACE_ID + ", " +
                "p." + Schema.Place.NAME + " AS " + Contract.TransferModel.PLACE_NAME + ", " +
                "p." + Schema.Place.ICON + " AS " + Contract.TransferModel.PLACE_ICON + ", " +
                "p." + Schema.Place.ADDRESS + " AS " + Contract.TransferModel.PLACE_ADDRESS + ", " +
                "p." + Schema.Place.LATITUDE + " AS " + Contract.TransferModel.PLACE_LATITUDE + ", " +
                "p." + Schema.Place.LONGITUDE + " AS " + Contract.TransferModel.PLACE_LONGITUDE + ", " +
                "p." + Schema.Place.TAG + " AS " + Contract.TransferModel.PLACE_TAG + ", " +
                "tm." + Schema.TransferModel.CONFIRMED + " AS " + Contract.TransferModel.CONFIRMED + ", " +
                "tm." + Schema.TransferModel.COUNT_IN_TOTAL + " AS " + Contract.TransferModel.COUNT_IN_TOTAL + ", " +
                "tm." + Schema.TransferModel.TAG + " AS " + Contract.TransferModel.TAG + " " +
                "FROM " + Schema.TransferModel.TABLE + " AS tm JOIN " + Schema.Wallet.TABLE + " AS " +
                "wf ON tm." + Schema.TransferModel.WALLET_FROM + " = wf." + Schema.Wallet.ID +
                " AND wf." + Schema.Wallet.DELETED + " = 0 JOIN " + Schema.Wallet.TABLE + " AS " +
                "wt ON tm." + Schema.TransferModel.WALLET_TO + " = wt." + Schema.Wallet.ID +
                " AND wt." + Schema.Wallet.DELETED + " = 0 LEFT JOIN " + Schema.Event.TABLE +
                " AS e ON tm." + Schema.TransferModel.EVENT + " = e." + Schema.Event.ID + " AND e." +
                Schema.Event.DELETED + " = 0 LEFT JOIN " + Schema.Place.TABLE + " AS p ON tm." +
                Schema.TransferModel.PLACE + " = p." + Schema.Place.ID + " AND p." +
                Schema.Place.DELETED + " = 0 WHERE tm." + Schema.TransferModel.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new transfer model.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertTransferModel(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.TransferModel.DESCRIPTION, contentValues.getAsString(Contract.TransferModel.DESCRIPTION));
        cv.put(Schema.TransferModel.WALLET_FROM, contentValues.getAsLong(Contract.TransferModel.WALLET_FROM_ID));
        cv.put(Schema.TransferModel.WALLET_TO, contentValues.getAsLong(Contract.TransferModel.WALLET_TO_ID));
        cv.put(Schema.TransferModel.MONEY_FROM, contentValues.getAsLong(Contract.TransferModel.MONEY_FROM));
        cv.put(Schema.TransferModel.MONEY_TO, contentValues.getAsLong(Contract.TransferModel.MONEY_TO));
        cv.put(Schema.TransferModel.MONEY_TAX, contentValues.getAsLong(Contract.TransferModel.MONEY_TAX));
        cv.put(Schema.TransferModel.NOTE, contentValues.getAsString(Contract.TransferModel.NOTE));
        cv.put(Schema.TransferModel.EVENT, contentValues.getAsLong(Contract.TransferModel.EVENT_ID));
        cv.put(Schema.TransferModel.PLACE, contentValues.getAsLong(Contract.TransferModel.PLACE_ID));
        cv.put(Schema.TransferModel.CONFIRMED, contentValues.getAsBoolean(Contract.TransferModel.CONFIRMED));
        cv.put(Schema.TransferModel.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.TransferModel.COUNT_IN_TOTAL));
        cv.put(Schema.TransferModel.TAG, contentValues.getAsString(Contract.TransferModel.TAG));
        cv.put(Schema.TransferModel.UUID, UUID.randomUUID().toString());
        cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.TransferModel.DELETED, false);
        return getWritableDatabase().insert(Schema.TransferModel.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing transfer
     * model.
     *
     * @param modelId id of the transfer model to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateTransferModel(long modelId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.TransferModel.DESCRIPTION, contentValues.getAsString(Contract.TransferModel.DESCRIPTION));
        cv.put(Schema.TransferModel.WALLET_FROM, contentValues.getAsLong(Contract.TransferModel.WALLET_FROM_ID));
        cv.put(Schema.TransferModel.WALLET_TO, contentValues.getAsLong(Contract.TransferModel.WALLET_TO_ID));
        cv.put(Schema.TransferModel.MONEY_FROM, contentValues.getAsLong(Contract.TransferModel.MONEY_FROM));
        cv.put(Schema.TransferModel.MONEY_TO, contentValues.getAsLong(Contract.TransferModel.MONEY_TO));
        cv.put(Schema.TransferModel.MONEY_TAX, contentValues.getAsLong(Contract.TransferModel.MONEY_TAX));
        cv.put(Schema.TransferModel.NOTE, contentValues.getAsString(Contract.TransferModel.NOTE));
        cv.put(Schema.TransferModel.EVENT, contentValues.getAsLong(Contract.TransferModel.EVENT_ID));
        cv.put(Schema.TransferModel.PLACE, contentValues.getAsLong(Contract.TransferModel.PLACE_ID));
        cv.put(Schema.TransferModel.CONFIRMED, contentValues.getAsBoolean(Contract.TransferModel.CONFIRMED));
        cv.put(Schema.TransferModel.COUNT_IN_TOTAL, contentValues.getAsBoolean(Contract.TransferModel.COUNT_IN_TOTAL));
        if (contentValues.containsKey(Contract.TransferModel.TAG)) {
            cv.put(Schema.TransferModel.TAG, contentValues.getAsString(Contract.TransferModel.TAG));
        }
        cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.TransferModel.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(modelId)};
        return getWritableDatabase().update(Schema.TransferModel.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a transfer model from the database. If the 'mCacheDeletedObjects' flag is enabled the
     * data is not removed but simply flagged as deleted.
     *
     * @param modelId id of the transfer model to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteTransferModel(long modelId) {
        String where = Schema.TransferModel.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(modelId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransferModel.DELETED, true);
            cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.TransferModel.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.TransferModel.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific place.
     *
     * @param placeId id of the place.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getPlace(long placeId, String[] projection) {
        String selection = Schema.Place.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(placeId)};
        return getPlaces(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the places.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getPlaces(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Place.ID + " AS " + Contract.Place.ID + ", " +
                Schema.Place.NAME + " AS " + Contract.Place.NAME + ", " +
                Schema.Place.ICON + " AS " + Contract.Place.ICON + ", " +
                Schema.Place.ADDRESS + " AS " + Contract.Place.ADDRESS + ", " +
                Schema.Place.LATITUDE + " AS " + Contract.Place.LATITUDE + ", " +
                Schema.Place.LONGITUDE + " AS " + Contract.Place.LONGITUDE + ", " +
                Schema.Place.TAG + " AS " + Contract.Place.TAG + " FROM " +
                Schema.Place.TABLE + " WHERE " + Schema.Place.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given place.
     *
     * @param placeId id of the place.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getPlaceTransactions(long placeId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String _selection = Schema.Transaction.PLACE + " = ?";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 1];
        _selectionArgs[0] = String.valueOf(placeId);
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 1, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new place.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertPlace(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Place.NAME, contentValues.getAsString(Contract.Place.NAME));
        cv.put(Schema.Place.ICON, contentValues.getAsString(Contract.Place.ICON));
        cv.put(Schema.Place.ADDRESS, contentValues.getAsString(Contract.Place.ADDRESS));
        cv.put(Schema.Place.LATITUDE, contentValues.getAsString(Contract.Place.LATITUDE));
        cv.put(Schema.Place.LONGITUDE, contentValues.getAsString(Contract.Place.LONGITUDE));
        cv.put(Schema.Place.TAG, contentValues.getAsString(Contract.Place.TAG));
        cv.put(Schema.Place.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Place.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Place.DELETED, false);
        return getWritableDatabase().insert(Schema.Place.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing place.
     *
     * @param placeId id of the place to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updatePlace(long placeId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Place.NAME, contentValues.getAsString(Contract.Place.NAME));
        cv.put(Schema.Place.ICON, contentValues.getAsString(Contract.Place.ICON));
        cv.put(Schema.Place.ADDRESS, contentValues.getAsString(Contract.Place.ADDRESS));
        cv.put(Schema.Place.LATITUDE, contentValues.getAsString(Contract.Place.LATITUDE));
        cv.put(Schema.Place.LONGITUDE, contentValues.getAsString(Contract.Place.LONGITUDE));
        if (contentValues.containsKey(Contract.Place.TAG)) {
            cv.put(Schema.Place.TAG, contentValues.getAsString(Contract.Place.TAG));
        }
        cv.put(Schema.Place.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Place.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(placeId)};
        return getWritableDatabase().update(Schema.Place.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a place from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param placeId id of the place to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deletePlace(long placeId) {
        // remove the place flag from all the transactions
        ContentValues cv = new ContentValues();
        cv.putNull(Schema.Transaction.PLACE);
        cv.put(Schema.Transaction.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Transaction.PLACE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.Transaction.TABLE, cv, where, whereArgs);
        // remove the place flag from all the transfers
        cv = new ContentValues();
        cv.putNull(Schema.Transfer.PLACE);
        cv.put(Schema.Transfer.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Transfer.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.Transfer.TABLE, cv, where, whereArgs);
        // remove the place flag from all the debts
        cv = new ContentValues();
        cv.putNull(Schema.Debt.PLACE);
        cv.put(Schema.Debt.LAST_EDIT, System.currentTimeMillis());
        where = Schema.Debt.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.Debt.TABLE, cv, where, whereArgs);
        // remove the place flag from all the transaction models
        cv = new ContentValues();
        cv.putNull(Schema.TransactionModel.PLACE);
        cv.put(Schema.TransactionModel.LAST_EDIT, System.currentTimeMillis());
        where = Schema.TransactionModel.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.TransactionModel.TABLE, cv, where, whereArgs);
        // remove the place flag from all the transfer models
        cv = new ContentValues();
        cv.putNull(Schema.TransferModel.PLACE);
        cv.put(Schema.TransferModel.LAST_EDIT, System.currentTimeMillis());
        where = Schema.TransferModel.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.TransferModel.TABLE, cv, where, whereArgs);
        // remove the place flag from all the recurrent transactions
        cv = new ContentValues();
        cv.putNull(Schema.RecurrentTransaction.PLACE);
        cv.put(Schema.RecurrentTransaction.LAST_EDIT, System.currentTimeMillis());
        where = Schema.RecurrentTransaction.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.RecurrentTransaction.TABLE, cv, where, whereArgs);
        // remove the place flag from all the recurrent transfers
        cv = new ContentValues();
        cv.putNull(Schema.RecurrentTransfer.PLACE);
        cv.put(Schema.RecurrentTransfer.LAST_EDIT, System.currentTimeMillis());
        where = Schema.RecurrentTransfer.PLACE + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        getWritableDatabase().update(Schema.RecurrentTransfer.TABLE, cv, where, whereArgs);
        // finally remove the place item
        where = Schema.Place.ID + " = ?";
        whereArgs = new String[]{String.valueOf(placeId)};
        if (mCacheDeletedObjects) {
            cv = new ContentValues();
            cv.put(Schema.Place.DELETED, true);
            cv.put(Schema.Place.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Place.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Place.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific person.
     *
     * @param personId id of the person.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getPerson(long personId, String[] projection) {
        String selection = Schema.Person.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(personId)};
        return getPeople(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the people.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getPeople(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Person.ID + " AS " + Contract.Person.ID + ", " +
                Schema.Person.NAME + " AS " + Contract.Person.NAME + ", " +
                Schema.Person.ICON + " AS " + Contract.Person.ICON + ", " +
                Schema.Person.NOTE + " AS " + Contract.Person.NOTE + ", " +
                Schema.Person.TAG + " AS " + Contract.Person.TAG + " " +
                "FROM " + Schema.Person.TABLE + " WHERE " + Schema.Person.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is querying all the transactions
     * related to a given person.
     *
     * @param personId id of the person.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getPeopleTransactions(long personId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // this query make a simple lookup over PEOPLE_IDS column and looks for <id> inside it
        // TODO: [LOW] Maybe try to optimize this query and replace the LIKE operator with a WHERE
        String _selection = Contract.Transaction.PEOPLE_IDS + " LIKE ?";
        if (!TextUtils.isEmpty(selection)) {
            _selection += " AND " + selection;
        }
        int size = selectionArgs != null ? selectionArgs.length : 0;
        String[] _selectionArgs = new String[size + 1];
        _selectionArgs[0] = "%<" + String.valueOf(personId) + ">%";
        if (selectionArgs != null) {
            System.arraycopy(selectionArgs, 0, _selectionArgs, 1, size);
        }
        return getTransactions(projection, _selection, _selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new person.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertPerson(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Person.NAME, contentValues.getAsString(Contract.Person.NAME));
        cv.put(Schema.Person.ICON, contentValues.getAsString(Contract.Person.ICON));
        cv.put(Schema.Person.NOTE, contentValues.getAsString(Contract.Person.NOTE));
        cv.put(Schema.Person.TAG, contentValues.getAsString(Contract.Person.TAG));
        cv.put(Schema.Person.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Person.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Person.DELETED, false);
        return getWritableDatabase().insert(Schema.Person.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing person.
     *
     * @param personId id of the person to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updatePerson(long personId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Person.NAME, contentValues.getAsString(Contract.Person.NAME));
        cv.put(Schema.Person.ICON, contentValues.getAsString(Contract.Person.ICON));
        cv.put(Schema.Person.NOTE, contentValues.getAsString(Contract.Person.NOTE));
        if (contentValues.containsKey(Contract.Person.TAG)) {
            cv.put(Schema.Person.TAG, contentValues.getAsString(Contract.Person.TAG));
        }
        cv.put(Schema.Person.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Person.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(personId)};
        return getWritableDatabase().update(Schema.Person.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete a person from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param personId id of the person to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deletePerson(long personId) {
        // remove all the TransactionPeople
        String where = Schema.TransactionPeople.PERSON + " = ?";
        String[] whereArgs = new String[]{String.valueOf(personId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransactionPeople.DELETED, true);
            cv.put(Schema.TransactionPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransactionPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransactionPeople.TABLE, where, whereArgs);
        }
        // remove all the TransferPeople
        where = Schema.TransferPeople.PERSON + " = ?";
        whereArgs = new String[]{String.valueOf(personId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.TransferPeople.DELETED, true);
            cv.put(Schema.TransferPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.TransferPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.TransferPeople.TABLE, where, whereArgs);
        }
        // remove all the EventPeople
        where = Schema.EventPeople.PERSON + " = ?";
        whereArgs = new String[]{String.valueOf(personId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.EventPeople.DELETED, true);
            cv.put(Schema.EventPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.EventPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.EventPeople.TABLE, where, whereArgs);
        }
        // remove all the DebtPeople
        where = Schema.DebtPeople.PERSON + " = ?";
        whereArgs = new String[]{String.valueOf(personId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.DebtPeople.DELETED, true);
            cv.put(Schema.DebtPeople.LAST_EDIT, System.currentTimeMillis());
            getWritableDatabase().update(Schema.DebtPeople.TABLE, cv, where, whereArgs);
        } else {
            getWritableDatabase().delete(Schema.DebtPeople.TABLE, where, whereArgs);
        }
        // finally remove the person item
        where = Schema.Person.ID + " = ?";
        whereArgs = new String[]{String.valueOf(personId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Person.DELETED, true);
            cv.put(Schema.Person.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Person.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Person.TABLE, where, whereArgs);
        }
    }

    /**
     * This method is called by the content provider when the user is querying a specific attachment.
     *
     * @param attachmentId id of the attachment.
     * @param projection column names that are requested to be part of the cursor.
     * @return a cursor with zero or one row.
     */
    /*package-local*/ Cursor getAttachment(long attachmentId, String[] projection) {
        String selection = Schema.Attachment.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(attachmentId)};
        return getAttachments(projection, selection, selectionArgs, null);
    }

    /**
     * This method is called by the content provider when the user is querying all the attachments.
     *
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    /*package-local*/ Cursor getAttachments(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String subQuery = "SELECT " +
                Schema.Attachment.ID + " AS " + Contract.Attachment.ID + ", " +
                Schema.Attachment.FILE + " AS " + Contract.Attachment.FILE + ", " +
                Schema.Attachment.NAME + " AS " + Contract.Attachment.NAME + ", " +
                Schema.Attachment.TYPE + " AS " + Contract.Attachment.TYPE + ", " +
                Schema.Attachment.SIZE + " AS " + Contract.Attachment.SIZE + ", " +
                Schema.Attachment.TAG + " AS " + Contract.Attachment.TAG + " " +
                "FROM " + Schema.Attachment.TABLE + " WHERE " + Schema.Attachment.DELETED + " = 0";
        return queryFrom(subQuery, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method is called by the content provider when the user is inserting a new attachment.
     *
     * @param contentValues bundle that contains the data from the content provider.
     * @return the id of the new item if inserted, -1 if an error occurs.
     */
    /*package-local*/ long insertAttachment(ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Attachment.FILE, contentValues.getAsString(Contract.Attachment.FILE));
        cv.put(Schema.Attachment.NAME, contentValues.getAsString(Contract.Attachment.NAME));
        cv.put(Schema.Attachment.TYPE, contentValues.getAsString(Contract.Attachment.TYPE));
        cv.put(Schema.Attachment.SIZE, contentValues.getAsLong(Contract.Attachment.SIZE));
        cv.put(Schema.Attachment.TAG, contentValues.getAsString(Contract.Attachment.TAG));
        cv.put(Schema.Attachment.UUID, UUID.randomUUID().toString());
        cv.put(Schema.Attachment.LAST_EDIT, System.currentTimeMillis());
        cv.put(Schema.Attachment.DELETED, false);
        return getWritableDatabase().insert(Schema.Attachment.TABLE, null, cv);
    }

    /**
     * This method is called by the content provider when the user is updating an existing attachment.
     *
     * @param attachmentId id of the attachment to update.
     * @param contentValues bundle that contains the values to update.
     * @return the number of row affected.
     */
    /*package-local*/ int updateAttachment(long attachmentId, ContentValues contentValues) {
        ContentValues cv = new ContentValues();
        cv.put(Schema.Attachment.FILE, contentValues.getAsString(Contract.Attachment.FILE));
        cv.put(Schema.Attachment.NAME, contentValues.getAsString(Contract.Attachment.NAME));
        cv.put(Schema.Attachment.TYPE, contentValues.getAsString(Contract.Attachment.TYPE));
        cv.put(Schema.Attachment.SIZE, contentValues.getAsLong(Contract.Attachment.SIZE));
        if (contentValues.containsKey(Contract.Attachment.TAG)) {
            cv.put(Schema.Attachment.TAG, contentValues.getAsString(Contract.Attachment.TAG));
        }
        cv.put(Schema.Attachment.LAST_EDIT, System.currentTimeMillis());
        String where = Schema.Attachment.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(attachmentId)};
        return getWritableDatabase().update(Schema.Attachment.TABLE, cv, where, whereArgs);
    }

    /**
     * Delete an attachment from the database. If the 'mCacheDeletedObjects' flag is enabled the data
     * is not removed but simply flagged as deleted.
     *
     * @param attachmentId id of the attachment to remove.
     * @return the number of rows affected by the deletion (must be 1 for success).
     */
    /*package-local*/ int deleteAttachment(long attachmentId) {
        String where = Schema.Attachment.ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(attachmentId)};
        if (mCacheDeletedObjects) {
            ContentValues cv = new ContentValues();
            cv.put(Schema.Attachment.DELETED, true);
            cv.put(Schema.Attachment.LAST_EDIT, System.currentTimeMillis());
            return getWritableDatabase().update(Schema.Attachment.TABLE, cv, where, whereArgs);
        } else {
            return getWritableDatabase().delete(Schema.Attachment.TABLE, where, whereArgs);
        }
    }

    /**
     * This is an internal method used to prepare a query over an existing sub query as table.
     *
     * @param subQuery query that acts as a table.
     * @param projection column names that are requested to be part of the cursor.
     * @param selection string that may contains additional filters for the query.
     * @param selectionArgs string array that may contains the arguments for the selection string.
     * @param sortOrder string that may contains column name to use to sort the cursor.
     * @return a cursor with zero or more rows.
     */
    private Cursor queryFrom(String subQuery, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getReadableDatabase().query("(" + subQuery + ")", projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * This is an internal method used to parse a string composed by zero, one or more ids.
     * For example a well formatted string like '<1>,<3>,<5>' can be parsed to obtain an array with
     * ids: 1, 3, 5.
     *
     * @param list well formatted string.
     * @return an array of ids as long.
     * @throws SQLiteException if the string is not well formatted.
     */
    private long[] parseIds(String list) {
        if (!TextUtils.isEmpty(list)) {
            System.out.println(list);
            String[] encodedIds = list.split(",");
            long[] ids = new long[encodedIds.length];
            for (int i = 0; i < encodedIds.length; i++) {
                String encodedId = encodedIds[i];
                if (encodedId.startsWith("<") && encodedId.endsWith(">")) {
                    ids[i] = Long.parseLong(encodedId.substring(1, encodedId.length() - 1));
                } else {
                    String message = "The ids column not follow the pattern at index %d. Content: %s";
                    throw new SQLiteException(String.format(Locale.ENGLISH, message, i, list));
                }
            }
            return ids;
        }
        return null;
    }

    /**
     * This is an internal method used to uniquely identify an occurrence of a recurring event
     * across different devices. This is not needed today but, if server sync will be introduced
     * in future, this will be necessary to avoid that, different devices adds the same transaction
     * using different UUIDs (this will lead to multiple instances of the same occurrence).
     * @param recurrenceUUID uuid of the recurrence entity.
     * @param date of the occurrence.
     * @return a uniquely identified UUID to use when inserting the occurrence.
     */
    private String getRecurrentItemUUID(String recurrenceUUID, Date date) {
        return String.format("%s:%s", recurrenceUUID, DateUtils.getSQLDateString(date));
    }

    /**
     * This method will update all the amounts of a given currency using the provided multiplicand
     * @param db instance of a writable database
     * @param iso of the currency to update
     * @param decimalOffset offset of the decimals position
     */
    private void fixCurrencyAmounts(SQLiteDatabase db, String iso, int decimalOffset) {
        if (decimalOffset != 0) {
            List<Long> walletIds = new ArrayList<>();
            // the first step consists into searching for all the wallets that are using this
            // currency and to collect their id inside a list. To speedup the process, we can
            // update immediately the start money value of each wallet.
            String[] projections = new String[] {
                    Contract.Wallet.ID,
                    Contract.Wallet.START_MONEY
            };
            String selection = Contract.Wallet.CURRENCY + " = ?";
            String[] selectionArgs = new String[] {iso};
            Cursor cursor = getWallets(projections, selection, selectionArgs, null);
            if (cursor != null) {
                int indexId = cursor.getColumnIndex(Contract.Wallet.ID);
                int indexStartMoney = cursor.getColumnIndex(Contract.Wallet.START_MONEY);
                while (cursor.moveToNext()) {
                    long walletId = cursor.getLong(indexId);
                    long startMoney = cursor.getLong(indexStartMoney);
                    // calculate the fixed value and update the item inside the database
                    long fixedStartMoney = MoneyFormatter.normalize(startMoney, decimalOffset);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Schema.Wallet.START_MONEY, fixedStartMoney);
                    String whereClause = Schema.Wallet.ID + " = ?";
                    String[] whereArgs = new String[] {String.valueOf(walletId)};
                    db.update(Schema.Wallet.TABLE, contentValues, whereClause, whereArgs);
                    // cache the wallet id inside the local list
                    walletIds.add(walletId);
                }
                cursor.close();
            }
            // now that we have collected all the wallet ids, we can start to iterate them and
            // update all the items that are linked within this wallet id
            for (Long walletId : walletIds) {
                // fix the debt table
                String[] projection = new String[] {
                        Schema.Debt.ID,
                        Schema.Debt.MONEY
                };
                selection = Schema.Debt.WALLET + " = ?";
                selectionArgs = new String[] {String.valueOf(walletId)};
                cursor = db.query(Schema.Debt.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.Debt.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.Debt.MONEY));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.Debt.MONEY, fixedMoney);
                        String whereClause = Schema.Debt.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.Debt.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the saving table
                projection = new String[] {
                        Schema.Saving.ID,
                        Schema.Saving.START_MONEY,
                        Schema.Saving.END_MONEY
                };
                selection = Schema.Saving.WALLET + " = ?";
                cursor = db.query(Schema.Saving.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.Saving.ID));
                        long startMoney = cursor.getLong(cursor.getColumnIndex(Schema.Saving.START_MONEY));
                        long endMoney = cursor.getLong(cursor.getColumnIndex(Schema.Saving.END_MONEY));
                        long fixedStartMoney = MoneyFormatter.normalize(startMoney, decimalOffset);
                        long fixedEndMoney = MoneyFormatter.normalize(endMoney, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.Saving.START_MONEY, fixedStartMoney);
                        contentValues.put(Schema.Saving.END_MONEY, fixedEndMoney);
                        String whereClause = Schema.Saving.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.Saving.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the transaction table
                projection = new String[] {
                        Schema.Transaction.ID,
                        Schema.Transaction.MONEY
                };
                selection = Schema.Transaction.WALLET + " = ?";
                cursor = db.query(Schema.Transaction.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.Transaction.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.Transaction.MONEY));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.Transaction.MONEY, fixedMoney);
                        String whereClause = Schema.Transaction.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.Transaction.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the transaction model table
                projection = new String[] {
                        Schema.TransactionModel.ID,
                        Schema.TransactionModel.MONEY
                };
                selection = Schema.TransactionModel.WALLET + " = ?";
                cursor = db.query(Schema.TransactionModel.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.MONEY));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.TransactionModel.MONEY, fixedMoney);
                        String whereClause = Schema.TransactionModel.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.TransactionModel.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the recurrent transfer table (FROM)
                projection = new String[] {
                        Schema.TransferModel.ID,
                        Schema.TransferModel.MONEY_FROM,
                        Schema.TransferModel.MONEY_TAX
                };
                selection = Schema.TransferModel.WALLET_FROM + " = ?";
                cursor = db.query(Schema.TransferModel.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_FROM));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.TransferModel.MONEY_FROM, fixedMoney);
                        // check if also the tax should be updated
                        if (!cursor.isNull(cursor.getColumnIndex(Schema.TransferModel.MONEY_TAX))) {
                            long moneyTax = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_TAX));
                            if (moneyTax > 0L) {
                                long fixedMoneyTax = MoneyFormatter.normalize(moneyTax, decimalOffset);
                                contentValues.put(Schema.TransferModel.MONEY_TAX, fixedMoneyTax);
                            }
                        }
                        String whereClause = Schema.TransferModel.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.TransferModel.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the recurrent transfer table (TO)
                projection = new String[] {
                        Schema.TransferModel.ID,
                        Schema.TransferModel.MONEY_TO
                };
                selection = Schema.TransferModel.WALLET_TO + " = ?";
                cursor = db.query(Schema.TransferModel.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_TO));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.TransferModel.MONEY_TO, fixedMoney);
                        String whereClause = Schema.TransferModel.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.TransferModel.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the recurrent transaction table
                projection = new String[] {
                        Schema.RecurrentTransaction.ID,
                        Schema.RecurrentTransaction.MONEY
                };
                selection = Schema.RecurrentTransaction.WALLET + " = ?";
                cursor = db.query(Schema.RecurrentTransaction.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.MONEY));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.RecurrentTransaction.MONEY, fixedMoney);
                        String whereClause = Schema.RecurrentTransaction.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.RecurrentTransaction.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the recurrent transfer table (FROM)
                projection = new String[] {
                        Schema.RecurrentTransfer.ID,
                        Schema.RecurrentTransfer.MONEY_FROM,
                        Schema.RecurrentTransfer.MONEY_TAX
                };
                selection = Schema.RecurrentTransfer.WALLET_FROM + " = ?";
                cursor = db.query(Schema.RecurrentTransfer.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_FROM));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.RecurrentTransfer.MONEY_FROM, fixedMoney);
                        // check if also the tax should be updated
                        if (!cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_TAX))) {
                            long moneyTax = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_TAX));
                            if (moneyTax > 0L) {
                                long fixedMoneyTax = MoneyFormatter.normalize(moneyTax, decimalOffset);
                                contentValues.put(Schema.RecurrentTransfer.MONEY_TAX, fixedMoneyTax);
                            }
                        }
                        String whereClause = Schema.RecurrentTransfer.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.RecurrentTransfer.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
                // fix the recurrent transfer table (TO)
                projection = new String[] {
                        Schema.RecurrentTransfer.ID,
                        Schema.RecurrentTransfer.MONEY_TO
                };
                selection = Schema.RecurrentTransfer.WALLET_TO + " = ?";
                cursor = db.query(Schema.RecurrentTransfer.TABLE, projection, selection, selectionArgs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // extract the current info from the table row
                        long id = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.ID));
                        long money = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_TO));
                        long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                        // update the row with the fixed money
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Schema.RecurrentTransfer.MONEY_TO, fixedMoney);
                        String whereClause = Schema.RecurrentTransfer.ID + " = ?";
                        String[] whereArgs = new String[] {String.valueOf(id)};
                        db.update(Schema.RecurrentTransfer.TABLE, contentValues, whereClause, whereArgs);
                    }
                    cursor.close();
                }
            }
            // fix the budget table (this must be done differently)
            String[] projection = new String[] {
                    Schema.Budget.ID,
                    Schema.Budget.MONEY
            };
            selection = Schema.Budget.CURRENCY + " = ?";
            selectionArgs = new String[] {iso};
            cursor = db.query(Schema.Budget.TABLE, projection, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // extract the current info from the table row
                    long id = cursor.getLong(cursor.getColumnIndex(Schema.Budget.ID));
                    long money = cursor.getLong(cursor.getColumnIndex(Schema.Budget.MONEY));
                    long fixedMoney = MoneyFormatter.normalize(money, decimalOffset);
                    // update the row with the fixed money
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Schema.Budget.MONEY, fixedMoney);
                    String whereClause = Schema.Budget.ID + " = ?";
                    String[] whereArgs = new String[] {String.valueOf(id)};
                    db.update(Schema.Budget.TABLE, contentValues, whereClause, whereArgs);
                }
                cursor.close();
            }
        }
    }
}