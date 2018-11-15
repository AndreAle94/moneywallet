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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This content provider exposes the full SQLiteDatabase structure.
 * The goal is to make the full tables available to backup and sync components.
 */
public class SyncContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.oriondev.moneywallet.storage.sync";

    public static final Uri ACTION_RECREATE_DATABASE = Uri.parse("content://" + AUTHORITY + "/database/recreate");

    public static final Uri CONTENT_CURRENCIES = Uri.parse("content://" + AUTHORITY + "/currencies");
    public static final Uri CONTENT_WALLETS = Uri.parse("content://" + AUTHORITY + "/wallets");
    public static final Uri CONTENT_CATEGORIES = Uri.parse("content://" + AUTHORITY + "/categories");
    public static final Uri CONTENT_EVENTS = Uri.parse("content://" + AUTHORITY + "/events");
    public static final Uri CONTENT_PLACES = Uri.parse("content://" + AUTHORITY + "/places");
    public static final Uri CONTENT_PEOPLE = Uri.parse("content://" + AUTHORITY + "/people");
    public static final Uri CONTENT_EVENT_PEOPLE = Uri.parse("content://" + AUTHORITY + "/event_people");
    public static final Uri CONTENT_DEBT = Uri.parse("content://" + AUTHORITY + "/debts");
    public static final Uri CONTENT_DEBT_PEOPLE = Uri.parse("content://" + AUTHORITY + "/debt_people");
    public static final Uri CONTENT_BUDGET = Uri.parse("content://" + AUTHORITY + "/budget");
    public static final Uri CONTENT_BUDGET_WALLET = Uri.parse("content://" + AUTHORITY + "/budget_wallets");
    public static final Uri CONTENT_SAVING = Uri.parse("content://" + AUTHORITY + "/savings");
    public static final Uri CONTENT_RECURRENT_TRANSACTION = Uri.parse("content://" + AUTHORITY + "/recurrent_transactions");
    public static final Uri CONTENT_RECURRENT_TRANSFER = Uri.parse("content://" + AUTHORITY + "/recurrent_transfers");
    public static final Uri CONTENT_TRANSACTION = Uri.parse("content://" + AUTHORITY + "/transactions");
    public static final Uri CONTENT_TRANSACTION_PEOPLE = Uri.parse("content://" + AUTHORITY + "/transaction_people");
    public static final Uri CONTENT_TRANSACTION_MODEL = Uri.parse("content://" + AUTHORITY + "/transaction_model");
    public static final Uri CONTENT_TRANSFER = Uri.parse("content://" + AUTHORITY + "/transfers");
    public static final Uri CONTENT_TRANSFER_PEOPLE = Uri.parse("content://" + AUTHORITY + "/transfer_people");
    public static final Uri CONTENT_TRANSFER_MODEL = Uri.parse("content://" + AUTHORITY + "/transfer_model");
    public static final Uri CONTENT_ATTACHMENT = Uri.parse("content://" + AUTHORITY + "/attachments");
    public static final Uri CONTENT_TRANSACTION_ATTACHMENT = Uri.parse("content://" + AUTHORITY + "/transaction_attachments");
    public static final Uri CONTENT_TRANSFER_ATTACHMENT = Uri.parse("content://" + AUTHORITY + "/transfer_attachments");

    private static final int TABLE_CURRENCIES = 1;
    private static final int TABLE_WALLETS = 2;
    private static final int TABLE_CATEGORIES = 3;
    private static final int TABLE_EVENTS = 4;
    private static final int TABLE_PLACES = 5;
    private static final int TABLE_PEOPLE = 6;
    private static final int TABLE_EVENT_PEOPLE = 7;
    private static final int TABLE_DEBTS = 8;
    private static final int TABLE_DEBT_PEOPLE = 9;
    private static final int TABLE_BUDGETS = 10;
    private static final int TABLE_BUDGET_WALLETS = 11;
    private static final int TABLE_SAVINGS = 12;
    private static final int TABLE_RECURRENT_TRANSACTIONS = 13;
    private static final int TABLE_RECURRENT_TRANSFERS = 14;
    private static final int TABLE_TRANSACTIONS = 15;
    private static final int TABLE_TRANSACTION_PEOPLE = 16;
    private static final int TABLE_TRANSACTION_MODELS = 17;
    private static final int TABLE_TRANSFERS = 18;
    private static final int TABLE_TRANSFER_PEOPLE = 19;
    private static final int TABLE_TRANSFER_MODELS = 20;
    private static final int TABLE_ATTACHMENTS = 21;
    private static final int TABLE_TRANSACTION_ATTACHMENTS = 22;
    private static final int TABLE_TRANSFER_ATTACHMENTS = 23;

    private static final UriMatcher mUriMatcher = createUriMatcher();

    private static UriMatcher createUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "currencies", TABLE_CURRENCIES);
        matcher.addURI(AUTHORITY, "wallets", TABLE_WALLETS);
        matcher.addURI(AUTHORITY, "categories", TABLE_CATEGORIES);
        matcher.addURI(AUTHORITY, "events", TABLE_EVENTS);
        matcher.addURI(AUTHORITY, "places", TABLE_PLACES);
        matcher.addURI(AUTHORITY, "people", TABLE_PEOPLE);
        matcher.addURI(AUTHORITY, "event_people", TABLE_EVENT_PEOPLE);
        matcher.addURI(AUTHORITY, "debts", TABLE_DEBTS);
        matcher.addURI(AUTHORITY, "debt_people", TABLE_DEBT_PEOPLE);
        matcher.addURI(AUTHORITY, "budget", TABLE_BUDGETS);
        matcher.addURI(AUTHORITY, "budget_wallets", TABLE_BUDGET_WALLETS);
        matcher.addURI(AUTHORITY, "savings", TABLE_SAVINGS);
        matcher.addURI(AUTHORITY, "recurrent_transactions", TABLE_RECURRENT_TRANSACTIONS);
        matcher.addURI(AUTHORITY, "recurrent_transfers", TABLE_RECURRENT_TRANSFERS);
        matcher.addURI(AUTHORITY, "transactions", TABLE_TRANSACTIONS);
        matcher.addURI(AUTHORITY, "transaction_people", TABLE_TRANSACTION_PEOPLE);
        matcher.addURI(AUTHORITY, "transaction_model", TABLE_TRANSACTION_MODELS);
        matcher.addURI(AUTHORITY, "transfers", TABLE_TRANSFERS);
        matcher.addURI(AUTHORITY, "transfer_people", TABLE_TRANSFER_PEOPLE);
        matcher.addURI(AUTHORITY, "transfer_model", TABLE_TRANSFER_MODELS);
        matcher.addURI(AUTHORITY, "attachments", TABLE_ATTACHMENTS);
        matcher.addURI(AUTHORITY, "transaction_attachments", TABLE_TRANSACTION_ATTACHMENTS);
        matcher.addURI(AUTHORITY, "transfer_attachments", TABLE_TRANSFER_ATTACHMENTS);
        return matcher;
    }

    private SQLDatabase mDatabase;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDatabase = new SQLDatabase(context);
        return true;
    }

    private String getTable(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case TABLE_CURRENCIES:
                return Schema.Currency.TABLE;
            case TABLE_WALLETS:
                return Schema.Wallet.TABLE;
            case TABLE_CATEGORIES:
                return Schema.Category.TABLE;
            case TABLE_EVENTS:
                return Schema.Event.TABLE;
            case TABLE_PLACES:
                return Schema.Place.TABLE;
            case TABLE_PEOPLE:
                return Schema.Person.TABLE;
            case TABLE_EVENT_PEOPLE:
                return Schema.EventPeople.TABLE;
            case TABLE_DEBTS:
                return Schema.Debt.TABLE;
            case TABLE_DEBT_PEOPLE:
                return Schema.DebtPeople.TABLE;
            case TABLE_BUDGETS:
                return Schema.Budget.TABLE;
            case TABLE_BUDGET_WALLETS:
                return Schema.BudgetWallet.TABLE;
            case TABLE_SAVINGS:
                return Schema.Saving.TABLE;
            case TABLE_RECURRENT_TRANSACTIONS:
                return Schema.RecurrentTransaction.TABLE;
            case TABLE_RECURRENT_TRANSFERS:
                return Schema.RecurrentTransfer.TABLE;
            case TABLE_TRANSACTIONS:
                return Schema.Transaction.TABLE;
            case TABLE_TRANSACTION_PEOPLE:
                return Schema.TransactionPeople.TABLE;
            case TABLE_TRANSACTION_MODELS:
                return Schema.TransactionModel.TABLE;
            case TABLE_TRANSFERS:
                return Schema.Transfer.TABLE;
            case TABLE_TRANSFER_PEOPLE:
                return Schema.TransferPeople.TABLE;
            case TABLE_TRANSFER_MODELS:
                return Schema.TransferModel.TABLE;
            case TABLE_ATTACHMENTS:
                return Schema.Attachment.TABLE;
            case TABLE_TRANSACTION_ATTACHMENTS:
                return Schema.TransactionAttachment.TABLE;
            case TABLE_TRANSFER_ATTACHMENTS:
                return Schema.TransferAttachment.TABLE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (uri.equals(ACTION_RECREATE_DATABASE)) {
            // This is necessary when the database is restored because the old reference to the
            // SQLDatabase object is pointing internally to the old file, so an attempt to write
            // to it will irremediably fail. This uri exposes to the application a way to relink
            // the object to the new database (to create).
            mDatabase = new SQLDatabase(getContext());
        } else {
            String table = getTable(uri);
            if (table != null) {
                return mDatabase.getReadableDatabase().query(table, projection, selection, selectionArgs, null, null, sortOrder);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = getTable(uri);
        if (table != null) {
            long id = mDatabase.getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = getTable(uri);
        if (table != null) {
            return mDatabase.getWritableDatabase().delete(table, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = getTable(uri);
        if (table != null) {
            return mDatabase.getWritableDatabase().update(table, values, selection, selectionArgs);
        }
        return 0;
    }
}