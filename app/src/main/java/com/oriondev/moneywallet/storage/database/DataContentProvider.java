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

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;

import java.util.List;

/**
 * Created by andrea on 17/01/18.
 */
public class DataContentProvider extends ContentProvider {

    private static final boolean IS_REMOTE_SYNC_ENABLED = false;

    /*package-local*/ static final String AUTHORITY = "com.oriondev.moneywallet.storage.data";

    public static final Uri CONTENT_CURRENCIES = Uri.parse("content://" + AUTHORITY + "/currencies");
    public static final Uri CONTENT_WALLETS = Uri.parse("content://" + AUTHORITY + "/wallets");
    public static final Uri CONTENT_TRANSACTIONS = Uri.parse("content://" + AUTHORITY + "/transactions");
    public static final Uri CONTENT_TRANSFERS = Uri.parse("content://" + AUTHORITY + "/transfers");
    public static final Uri CONTENT_CATEGORIES = Uri.parse("content://" + AUTHORITY + "/categories");
    public static final Uri CONTENT_DEBTS = Uri.parse("content://" + AUTHORITY + "/debts");
    public static final Uri CONTENT_BUDGETS = Uri.parse("content://" + AUTHORITY + "/budgets");
    public static final Uri CONTENT_SAVINGS = Uri.parse("content://" + AUTHORITY + "/savings");
    public static final Uri CONTENT_EVENTS = Uri.parse("content://" + AUTHORITY + "/events");
    public static final Uri CONTENT_RECURRENT_TRANSACTIONS = Uri.parse("content://" + AUTHORITY + "/recurrences/transactions");
    public static final Uri CONTENT_RECURRENT_TRANSFERS = Uri.parse("content://" + AUTHORITY + "/recurrences/transfers");
    public static final Uri CONTENT_TRANSACTION_MODELS = Uri.parse("content://" + AUTHORITY + "/models/transactions");
    public static final Uri CONTENT_TRANSFER_MODELS = Uri.parse("content://" + AUTHORITY + "/models/transfers");
    public static final Uri CONTENT_PLACES = Uri.parse("content://" + AUTHORITY + "/places");
    public static final Uri CONTENT_PEOPLE = Uri.parse("content://" + AUTHORITY + "/people");
    public static final Uri CONTENT_ATTACHMENTS = Uri.parse("content://" + AUTHORITY + "/attachments");

    private static final int CURRENCY_LIST = 1;
    private static final int WALLET_LIST = 2;
    private static final int TRANSACTION_LIST = 3;
    private static final int TRANSFER_LIST = 4;
    private static final int CATEGORY_LIST = 5;
    private static final int DEBT_LIST = 6;
    private static final int BUDGET_LIST = 7;
    private static final int SAVING_LIST = 8;
    private static final int EVENT_LIST = 9;
    private static final int RECURRENT_TRANSACTION_LIST = 10;
    private static final int RECURRENT_TRANSFER_LIST = 11;
    private static final int TRANSACTION_MODEL_LIST = 12;
    private static final int TRANSFER_MODEL_LIST = 13;
    private static final int PLACE_LIST = 14;
    private static final int PERSON_LIST = 15;
    private static final int ATTACHMENT_LIST = 16;
    private static final int ATTACHMENT_ITEM = 17;

    private static final int WALLET_ITEM = 18;
    private static final int TRANSACTION_ITEM = 19;
    private static final int TRANSFER_ITEM = 20;
    private static final int CATEGORY_ITEM = 21;
    private static final int DEBT_ITEM = 22;
    private static final int BUDGET_ITEM = 23;
    private static final int SAVING_ITEM = 24;
    private static final int EVENT_ITEM = 25;
    private static final int RECURRENT_TRANSACTION_ITEM = 26;
    private static final int RECURRENT_TRANSFER_ITEM = 27;
    private static final int TRANSACTION_MODEL_ITEM = 28;
    private static final int TRANSFER_MODEL_ITEM = 29;
    private static final int PLACE_ITEM = 30;
    private static final int PERSON_ITEM = 31;

    private static final int TRANSACTION_ATTACHMENTS = 32;
    private static final int TRANSACTION_PEOPLE = 33;
    private static final int TRANSFER_ATTACHMENTS = 34;
    private static final int TRANSFER_PEOPLE = 35;
    private static final int DEBT_PEOPLE = 36;
    private static final int BUDGET_WALLETS = 37;

    private static final int CATEGORY_TRANSACTION_LIST = 38;
    private static final int DEBT_TRANSACTION_LIST = 39;
    private static final int BUDGET_TRANSACTION_LIST = 40;
    private static final int SAVING_TRANSACTION_LIST = 41;
    private static final int EVENT_TRANSACTION_LIST = 42;
    private static final int PLACE_TRANSACTION_LIST = 43;
    private static final int PERSON_TRANSACTION_LIST = 44;

    private static final UriMatcher mUriMatcher = createUriMatcher();

    private static UriMatcher createUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "currencies", CURRENCY_LIST);
        matcher.addURI(AUTHORITY, "wallets", WALLET_LIST);
        matcher.addURI(AUTHORITY, "wallets/#", WALLET_ITEM);
        matcher.addURI(AUTHORITY, "transactions", TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "transactions/#", TRANSACTION_ITEM);
        matcher.addURI(AUTHORITY, "transactions/#/attachments", TRANSACTION_ATTACHMENTS);
        matcher.addURI(AUTHORITY, "transactions/#/people", TRANSACTION_PEOPLE);
        matcher.addURI(AUTHORITY, "transfers", TRANSFER_LIST);
        matcher.addURI(AUTHORITY, "transfers/#", TRANSFER_ITEM);
        matcher.addURI(AUTHORITY, "transfers/#/attachments", TRANSFER_ATTACHMENTS);
        matcher.addURI(AUTHORITY, "transfers/#/people", TRANSFER_PEOPLE);
        matcher.addURI(AUTHORITY, "categories", CATEGORY_LIST);
        matcher.addURI(AUTHORITY, "categories/#", CATEGORY_ITEM);
        matcher.addURI(AUTHORITY, "categories/#/transactions", CATEGORY_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "debts", DEBT_LIST);
        matcher.addURI(AUTHORITY, "debts/#", DEBT_ITEM);
        matcher.addURI(AUTHORITY, "debts/#/people", DEBT_PEOPLE);
        matcher.addURI(AUTHORITY, "debts/#/transactions", DEBT_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "budgets", BUDGET_LIST);
        matcher.addURI(AUTHORITY, "budgets/#", BUDGET_ITEM);
        matcher.addURI(AUTHORITY, "budgets/#/wallets", BUDGET_WALLETS);
        matcher.addURI(AUTHORITY, "budgets/#/transactions", BUDGET_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "savings", SAVING_LIST);
        matcher.addURI(AUTHORITY, "savings/#", SAVING_ITEM);
        matcher.addURI(AUTHORITY, "savings/#/transactions", SAVING_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "events", EVENT_LIST);
        matcher.addURI(AUTHORITY, "events/#", EVENT_ITEM);
        matcher.addURI(AUTHORITY, "events/#/transactions", EVENT_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "recurrences/transactions", RECURRENT_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "recurrences/transactions/#", RECURRENT_TRANSACTION_ITEM);
        matcher.addURI(AUTHORITY, "recurrences/transfers", RECURRENT_TRANSFER_LIST);
        matcher.addURI(AUTHORITY, "recurrences/transfers/#", RECURRENT_TRANSFER_ITEM);
        matcher.addURI(AUTHORITY, "models/transactions", TRANSACTION_MODEL_LIST);
        matcher.addURI(AUTHORITY, "models/transactions/#", TRANSACTION_MODEL_ITEM);
        matcher.addURI(AUTHORITY, "models/transfers", TRANSFER_MODEL_LIST);
        matcher.addURI(AUTHORITY, "models/transfers/#", TRANSFER_MODEL_ITEM);
        matcher.addURI(AUTHORITY, "places", PLACE_LIST);
        matcher.addURI(AUTHORITY, "places/#", PLACE_ITEM);
        matcher.addURI(AUTHORITY, "places/#/transactions", PLACE_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "people", PERSON_LIST);
        matcher.addURI(AUTHORITY, "people/#", PERSON_ITEM);
        matcher.addURI(AUTHORITY, "people/#/transactions", PERSON_TRANSACTION_LIST);
        matcher.addURI(AUTHORITY, "attachments", ATTACHMENT_LIST);
        matcher.addURI(AUTHORITY, "attachments/#", ATTACHMENT_ITEM);
        return matcher;
    }

    private SQLDatabase mDatabase;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        initializeDatabase(context);
        setupReceiver(context);
        return true;
    }

    private void setupReceiver(Context context) {
        IntentFilter filter = new IntentFilter(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, filter);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case CURRENCY_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getCurrencies(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_CURRENCIES);
                break;
            case WALLET_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getWallets(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case WALLET_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getWallet(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransactions(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSACTION_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransaction(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSACTION_ATTACHMENTS:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransactionAttachments(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSACTION_PEOPLE:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransactionPeople(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                break;
            case TRANSFER_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransfers(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSFER_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransfer(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSFER_ATTACHMENTS:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransferAttachments(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case TRANSFER_PEOPLE:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransferPeople(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                break;
            case CATEGORY_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getCategories(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                break;
            case CATEGORY_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getCategory(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                break;
            case CATEGORY_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getCategoryTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                break;
            case DEBT_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getDebts(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case DEBT_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getDebt(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case DEBT_PEOPLE:
                cursor = new MultiUriCursorWrapper(mDatabase.getDebtPeople(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                break;
            case DEBT_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getDebtTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case BUDGET_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getBudgets(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_BUDGETS);
                break;
            case BUDGET_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getBudget(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case BUDGET_WALLETS:
                cursor = new MultiUriCursorWrapper(mDatabase.getBudgetWallets(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_BUDGETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                break;
            case BUDGET_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getBudgetTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_BUDGETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case SAVING_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getSavings(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_SAVINGS);
                break;
            case SAVING_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getSaving(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case SAVING_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getSavingTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_SAVINGS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case EVENT_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getEvents(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                break;
            case EVENT_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getEvent(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case EVENT_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getEventTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case RECURRENT_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getRecurrentTransactions(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTION_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_RECURRENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case RECURRENT_TRANSACTION_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getRecurrentTransaction(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTION_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_RECURRENT_TRANSACTIONS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case RECURRENT_TRANSFER_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getRecurrentTransfers(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFER_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_RECURRENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case RECURRENT_TRANSFER_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getRecurrentTransfer(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFER_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_RECURRENT_TRANSFERS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case TRANSACTION_MODEL_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransactionModels(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTION_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case TRANSACTION_MODEL_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransactionModel(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case TRANSFER_MODEL_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransferModels(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSFER_MODELS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case TRANSFER_MODEL_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getTransferModel(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                cursor.setNotificationUri(getContentResolver(), CONTENT_WALLETS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_CATEGORIES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_DEBTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_EVENTS);
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case PLACE_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getPlaces(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                break;
            case PLACE_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getPlace(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                break;
            case PLACE_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getPlaceTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_PLACES);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case PERSON_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getPeople(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                break;
            case PERSON_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getPerson(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                break;
            case PERSON_TRANSACTION_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getPeopleTransactions(parseIdAtIndex(uri, 1), projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_PEOPLE);
                cursor.setNotificationUri(getContentResolver(), CONTENT_TRANSACTIONS);
                break;
            case ATTACHMENT_LIST:
                cursor = new MultiUriCursorWrapper(mDatabase.getAttachments(projection, selection, selectionArgs, sortOrder));
                cursor.setNotificationUri(getContentResolver(), CONTENT_ATTACHMENTS);
                break;
            case ATTACHMENT_ITEM:
                cursor = new MultiUriCursorWrapper(mDatabase.getAttachment(ContentUris.parseId(uri), projection));
                cursor.setNotificationUri(getContentResolver(), uri);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case CURRENCY_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.currency";
            case WALLET_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.wallet";
            case WALLET_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.wallet";
            case TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case TRANSACTION_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.transaction";
            case TRANSACTION_ATTACHMENTS:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.attachments";
            case TRANSACTION_PEOPLE:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.person";
            case TRANSFER_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transfer";
            case TRANSFER_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.transfer";
            case TRANSFER_ATTACHMENTS:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.attachments";
            case TRANSFER_PEOPLE:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.person";
            case CATEGORY_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.category";
            case CATEGORY_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.category";
            case CATEGORY_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case DEBT_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.debt";
            case DEBT_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.debt";
            case DEBT_PEOPLE:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.person";
            case DEBT_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case BUDGET_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.budget";
            case BUDGET_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.budget";
            case BUDGET_WALLETS:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.wallet";
            case BUDGET_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case SAVING_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.saving";
            case SAVING_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.saving";
            case SAVING_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case EVENT_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.event";
            case EVENT_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.event";
            case EVENT_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case RECURRENT_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.recurrence.transaction";
            case RECURRENT_TRANSACTION_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.recurrence.transaction";
            case RECURRENT_TRANSFER_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.recurrence.transfer";
            case RECURRENT_TRANSFER_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.recurrence.transfer";
            case TRANSACTION_MODEL_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.model.transaction";
            case TRANSACTION_MODEL_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.model.transaction";
            case TRANSFER_MODEL_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.model.transfer";
            case TRANSFER_MODEL_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.model.transfer";
            case PLACE_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.place";
            case PLACE_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.place";
            case PLACE_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case PERSON_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.person";
            case PERSON_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.person";
            case PERSON_TRANSACTION_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.transaction";
            case ATTACHMENT_LIST:
                return "vnd.android.cursor.dir/vnd.com.oriondev.moneywallet.storage.attachment";
            case ATTACHMENT_ITEM:
                return "vnd.android.cursor.item/vnd.com.oriondev.moneywallet.storage.attachment";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        long objectId = 0L;
        switch (mUriMatcher.match(uri)) {
            case CURRENCY_LIST:
                throw new RuntimeException("User cannot add extra currencies by now");
            case WALLET_LIST:
                objectId = mDatabase.insertWallet(contentValues);
                break;
            case TRANSACTION_LIST:
                objectId = mDatabase.insertTransaction(contentValues);
                break;
            case TRANSFER_LIST:
                objectId = mDatabase.insertTransfer(contentValues);
                break;
            case CATEGORY_LIST:
                objectId = mDatabase.insertCategory(contentValues);
                break;
            case DEBT_LIST:
                objectId = mDatabase.insertDebt(contentValues);
                break;
            case BUDGET_LIST:
                objectId = mDatabase.insertBudget(contentValues);
                break;
            case SAVING_LIST:
                objectId = mDatabase.insertSaving(contentValues);
                break;
            case EVENT_LIST:
                objectId = mDatabase.insertEvent(contentValues);
                break;
            case RECURRENT_TRANSACTION_LIST:
                objectId = mDatabase.insertRecurrentTransaction(contentValues);
                break;
            case RECURRENT_TRANSFER_LIST:
                objectId = mDatabase.insertRecurrentTransfer(contentValues);
                break;
            case TRANSACTION_MODEL_LIST:
                objectId = mDatabase.insertTransactionModel(contentValues);
                break;
            case TRANSFER_MODEL_LIST:
                objectId = mDatabase.insertTransferModel(contentValues);
                break;
            case PLACE_LIST:
                objectId = mDatabase.insertPlace(contentValues);
                break;
            case PERSON_LIST:
                objectId = mDatabase.insertPerson(contentValues);
                break;
            case ATTACHMENT_LIST:
                objectId = mDatabase.insertAttachment(contentValues);
                break;
        }
        if (objectId > 0L) {
            ContentResolver contentResolver = getContentResolver();
            Uri objectUri = ContentUris.withAppendedId(uri, objectId);
            if (contentResolver != null) {
                contentResolver.notifyChange(objectUri, null);
            }
            return objectUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        Uri notifyUri = null;
        switch (mUriMatcher.match(uri)) {
            case WALLET_ITEM:
                notifyUri = DataContentProvider.CONTENT_WALLETS;
                result = mDatabase.deleteWallet(ContentUris.parseId(uri));
                break;
            case TRANSACTION_ITEM:
                notifyUri = DataContentProvider.CONTENT_TRANSACTIONS;
                result = mDatabase.deleteTransaction(ContentUris.parseId(uri));
                break;
            case TRANSFER_ITEM:
                notifyUri = DataContentProvider.CONTENT_TRANSFERS;
                result = mDatabase.deleteTransfer(ContentUris.parseId(uri));
                break;
            case CATEGORY_ITEM:
                notifyUri = DataContentProvider.CONTENT_CATEGORIES;
                result = mDatabase.deleteCategory(ContentUris.parseId(uri));
                break;
            case DEBT_ITEM:
                notifyUri = DataContentProvider.CONTENT_DEBTS;
                result = mDatabase.deleteDebt(ContentUris.parseId(uri));
                break;
            case BUDGET_ITEM:
                notifyUri = DataContentProvider.CONTENT_BUDGETS;
                result = mDatabase.deleteBudget(ContentUris.parseId(uri));
                break;
            case SAVING_ITEM:
                notifyUri = DataContentProvider.CONTENT_SAVINGS;
                result = mDatabase.deleteSaving(ContentUris.parseId(uri));
                break;
            case EVENT_ITEM:
                notifyUri = DataContentProvider.CONTENT_EVENTS;
                result = mDatabase.deleteEvent(ContentUris.parseId(uri));
                break;
            case RECURRENT_TRANSACTION_ITEM:
                notifyUri = DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS;
                result = mDatabase.deleteRecurrentTransaction(ContentUris.parseId(uri));
                break;
            case RECURRENT_TRANSFER_ITEM:
                notifyUri = DataContentProvider.CONTENT_RECURRENT_TRANSFERS;
                result = mDatabase.deleteRecurrentTransfer(ContentUris.parseId(uri));
                break;
            case TRANSACTION_MODEL_ITEM:
                notifyUri = DataContentProvider.CONTENT_TRANSACTION_MODELS;
                result = mDatabase.deleteTransactionModel(ContentUris.parseId(uri));
                break;
            case TRANSFER_MODEL_ITEM:
                notifyUri = DataContentProvider.CONTENT_TRANSFER_MODELS;
                result = mDatabase.deleteTransferModel(ContentUris.parseId(uri));
                break;
            case PLACE_ITEM:
                notifyUri = DataContentProvider.CONTENT_PLACES;
                result = mDatabase.deletePlace(ContentUris.parseId(uri));
                break;
            case PERSON_ITEM:
                notifyUri = DataContentProvider.CONTENT_PEOPLE;
                result = mDatabase.deletePerson(ContentUris.parseId(uri));
                break;
            case ATTACHMENT_ITEM:
                notifyUri = DataContentProvider.CONTENT_ATTACHMENTS;
                result = mDatabase.deleteAttachment(ContentUris.parseId(uri));
                break;
        }
        ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null && result > 0) {
            contentResolver.notifyChange(notifyUri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result = 0;
        switch (mUriMatcher.match(uri)) {
            case WALLET_ITEM:
                result = mDatabase.updateWallet(ContentUris.parseId(uri), values);
                break;
            case TRANSACTION_ITEM:
                result = mDatabase.updateTransaction(ContentUris.parseId(uri), values);
                break;
            case TRANSFER_ITEM:
                result = mDatabase.updateTransfer(ContentUris.parseId(uri), values);
                break;
            case CATEGORY_ITEM:
                result = mDatabase.updateCategory(ContentUris.parseId(uri), values);
                break;
            case DEBT_ITEM:
                result = mDatabase.updateDebt(ContentUris.parseId(uri), values);
                break;
            case BUDGET_ITEM:
                result = mDatabase.updateBudget(ContentUris.parseId(uri), values);
                break;
            case SAVING_ITEM:
                result = mDatabase.updateSaving(ContentUris.parseId(uri), values);
                break;
            case EVENT_ITEM:
                result = mDatabase.updateEvent(ContentUris.parseId(uri), values);
                break;
            case RECURRENT_TRANSACTION_ITEM:
                result = mDatabase.updateRecurrentTransaction(ContentUris.parseId(uri), values);
                break;
            case RECURRENT_TRANSFER_ITEM:
                result = mDatabase.updateRecurrentTransfer(ContentUris.parseId(uri), values);
                break;
            case TRANSACTION_MODEL_ITEM:
                result = mDatabase.updateTransactionModel(ContentUris.parseId(uri), values);
                break;
            case TRANSFER_MODEL_ITEM:
                result = mDatabase.updateTransferModel(ContentUris.parseId(uri), values);
                break;
            case PLACE_ITEM:
                result = mDatabase.updatePlace(ContentUris.parseId(uri), values);
                break;
            case PERSON_ITEM:
                result = mDatabase.updatePerson(ContentUris.parseId(uri), values);
                break;
        }
        if (result > 0) {
            ContentResolver contentResolver = getContentResolver();
            if (contentResolver != null) {
                contentResolver.notifyChange(uri, null);
            }
        }
        return result;
    }

    private ContentResolver getContentResolver() {
        Context context = getContext();
        return context != null ? context.getContentResolver() : null;
    }

    /**
     * Parse an id from a uri starting from the end of the string.
     * @param uri to parse from.
     * @param index of the id starting from the end.
     * @return the parsed id.
     */
    private long parseIdAtIndex(Uri uri, int index) {
        List<String> segments = uri.getPathSegments();
        int fixedIndex = segments.size() - index;
        return Long.parseLong(segments.get(fixedIndex - 1));
    }

    private void initializeDatabase(Context context) {
        if (mDatabase != null) {
            mDatabase.close();
        }
        mDatabase = new SQLDatabase(context);
        mDatabase.setDeletedObjectCacheEnabled(IS_REMOTE_SYNC_ENABLED);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int action = intent.getIntExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, 0);
                if (action == AbstractBackupHandlerIntentService.ACTION_RESTORE) {
                    initializeDatabase(context);
                    PreferenceManager.setCurrentWallet(context, PreferenceManager.NO_CURRENT_WALLET);
                }
            }
        }

    };
}