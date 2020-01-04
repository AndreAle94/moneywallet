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

package com.oriondev.moneywallet.storage.database.legacy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import androidx.collection.LongSparseArray;
import android.text.TextUtils;

import com.oriondev.moneywallet.model.ColorIcon;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.RecurrenceSetting;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.DatabaseImporter;
import com.oriondev.moneywallet.storage.database.ImportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.model.Attachment;
import com.oriondev.moneywallet.storage.database.model.Budget;
import com.oriondev.moneywallet.storage.database.model.BudgetWallet;
import com.oriondev.moneywallet.storage.database.model.Category;
import com.oriondev.moneywallet.storage.database.model.Debt;
import com.oriondev.moneywallet.storage.database.model.Event;
import com.oriondev.moneywallet.storage.database.model.Place;
import com.oriondev.moneywallet.storage.database.model.RecurrentTransaction;
import com.oriondev.moneywallet.storage.database.model.Saving;
import com.oriondev.moneywallet.storage.database.model.Transaction;
import com.oriondev.moneywallet.storage.database.model.Transfer;
import com.oriondev.moneywallet.storage.database.model.Wallet;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.MoneyFormatter;
import com.oriondev.moneywallet.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class is an implementation of the DatabaseImporter interface and is used by
 * LegacyEditionImporter to correctly upgrade the internal database when switching from
 * a legacy edition of the application to one of the latest releases, and by
 * LegacyBackupImporter when a legacy backup has been selected to be restored.
 */
public class LegacyDatabaseImporter implements DatabaseImporter {

    private static final int LEGACY_DECIMALS = 2;

    private final SQLiteDatabase mDatabase;

    private final LongSparseArray<Long> mCacheWallet = new LongSparseArray<>();
    private final LongSparseArray<String> mCacheWalletCurrency = new LongSparseArray<>();
    private final LongSparseArray<Long> mCacheCategory = new LongSparseArray<>();
    private final LongSparseArray<String> mCacheCategoryTag = new LongSparseArray<>();
    private final LongSparseArray<Long> mCacheEvent = new LongSparseArray<>();
    private final Map<String, Long> mCachePlace = new HashMap<>();
    private final LongSparseArray<Long> mCacheDebt = new LongSparseArray<>();
    private final LongSparseArray<Long> mCacheSaving = new LongSparseArray<>();
    private final LongSparseArray<Long> mCacheRecurrences = new LongSparseArray<>();
    private final Map<String, Transfer> mCacheTransfer = new HashMap<>();
    private final Set<String> mCacheTransaction = new HashSet<>();

    public LegacyDatabaseImporter(File legacyDatabase) throws ImportException {
        if (!legacyDatabase.exists()) {
            throw new ImportException("Legacy database not exists");
        }
        String databasePath = legacyDatabase.getPath();
        int flags = SQLiteDatabase.OPEN_READONLY;
        try {
            mDatabase = SQLiteDatabase.openDatabase(databasePath, null, flags);
        } catch (SQLiteException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importHeader() throws ImportException {
        // no header to check :(
    }

    @Override
    public void importCurrencies(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importWallets(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Wallet.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Wallet.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Wallet wallet = new Wallet();
                wallet.mName = getStringSafely(cursor, LegacyDatabaseSchema.Wallet.NAME);
                wallet.mIcon = getIconSafely(cursor, LegacyDatabaseSchema.Wallet.ICON, wallet.mName);
                wallet.mCurrency = getStringSafely(cursor, LegacyDatabaseSchema.Wallet.CURRENCY_ISO);
                wallet.mCountInTotal = getBooleanSafely(cursor, LegacyDatabaseSchema.Wallet.IN_TOTAL);
                wallet.mStartMoney = normalize(wallet.mCurrency, getLongSafely(cursor, LegacyDatabaseSchema.Wallet.INITIAL));
                wallet.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Wallet.UUID);
                wallet.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Wallet.DELETED);
                wallet.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Wallet.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, wallet);
                    // cache the old id and the new id to reconstruct the relationships
                    long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Wallet.ID);
                    mCacheWallet.put(legacyId, id);
                    mCacheWalletCurrency.put(legacyId, wallet.mCurrency);
                } catch (Exception ignore) {
                    // if an exception occur, the wallet id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importCategories(ContentResolver contentResolver) throws ImportException {
        // firstly, query only the categories that have no parent id set
        String selection = LegacyDatabaseSchema.Category.DELETED + " = 0 AND (" +
                LegacyDatabaseSchema.Category.PARENT + " IS NULL OR " +
                LegacyDatabaseSchema.Category.PARENT + " <= 0)";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Category.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            // populate map to easily detect a system category
            LongSparseArray<String> systemCategoryMap = new LongSparseArray<>();
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_CREDIT, Contract.CategoryTag.CREDIT);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_CREDIT_PAID, Contract.CategoryTag.PAID_CREDIT);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_DEBT, Contract.CategoryTag.DEBT);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_DEBT_PAID, Contract.CategoryTag.PAID_DEBT);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_TRANSFER, Contract.CategoryTag.TRANSFER);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_TRANSFER_TAX, Contract.CategoryTag.TRANSFER_TAX);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_OPERATION_TAX, Contract.CategoryTag.TAX);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_SAVING_IN, Contract.CategoryTag.SAVING_DEPOSIT);
            systemCategoryMap.put(LegacyDatabaseSchema.INDEX_HIDDEN_CATEGORY_SAVING_OUT, Contract.CategoryTag.SAVING_WITHDRAW);
            // iterate between the rows returned by the query
            while (cursor.moveToNext()) {
                long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Category.ID);
                long parentId = getIntSafely(cursor, LegacyDatabaseSchema.Category.PARENT);
                if (parentId < 0 && systemCategoryMap.get(parentId) != null) {
                    // this is a system category, we have to link it to the new system category
                    String categoryTag = systemCategoryMap.get(parentId);
                    long id = getSystemCategoryId(contentResolver, categoryTag);
                    mCacheCategory.put(legacyId, id);
                    mCacheCategoryTag.put(legacyId, categoryTag);
                } else {
                    // this is a standard category, just insert it and link the id in the cache
                    Category category = new Category();
                    category.mName = getStringSafely(cursor, LegacyDatabaseSchema.Category.NAME);
                    category.mIcon = getIconSafely(cursor, LegacyDatabaseSchema.Category.ICON, category.mName);
                    category.mType = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.IS_IN) ? Contract.CategoryType.INCOME.getValue() : Contract.CategoryType.EXPENSE.getValue();
                    category.mShowReport = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.REPORT);
                    category.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Category.UUID);
                    category.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.DELETED);
                    category.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Category.LAST_EDIT);
                    try {
                        long id = SQLDatabaseImporter.insert(contentResolver, category);
                        mCacheCategory.put(legacyId, id);
                    } catch (Exception ignore) {
                        // if an exception occur, the category id is not inserted in the cache
                        // and all the related stuff will be discarded during the migration
                    }
                }
            }
            cursor.close();
        }
        // now, query all the categories that have a parent set
        selection = LegacyDatabaseSchema.Category.DELETED + " = 0 AND " +
                LegacyDatabaseSchema.Category.PARENT + " > 0";
        cursor = mDatabase.query(LegacyDatabaseSchema.Category.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Category category = new Category();
                category.mName = getStringSafely(cursor, LegacyDatabaseSchema.Category.NAME);
                category.mIcon = getIconSafely(cursor, LegacyDatabaseSchema.Category.ICON, category.mName);
                category.mType = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.IS_IN) ? Contract.CategoryType.INCOME.getValue() : Contract.CategoryType.EXPENSE.getValue();
                category.mParent = mCacheCategory.get(getLongSafely(cursor, LegacyDatabaseSchema.Category.PARENT));
                category.mShowReport = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.REPORT);
                category.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Category.UUID);
                category.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Category.DELETED);
                category.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Category.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, category);
                    long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Category.ID);
                    mCacheCategory.put(legacyId, id);
                } catch (Exception ignore) {
                    // if an exception occur, the category id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    private long getSystemCategoryId(ContentResolver contentResolver, String tag) throws ImportException {
        long systemCategoryId = -1L;
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        String[] projection = new String[] {Contract.Category.ID};
        String selection = Contract.Category.TAG + " = ?";
        String[] selectionArgs = new String[] {tag};
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                systemCategoryId = cursor.getLong(cursor.getColumnIndex(Contract.Category.ID));
            }
            cursor.close();
        }
        if (systemCategoryId == -1L) {
            throw new ImportException("Failed to link a legacy system category in the new database");
        }
        return systemCategoryId;
    }

    @Override
    public void importEvents(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Event.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Event.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Event event = new Event();
                event.mName = getStringSafely(cursor, LegacyDatabaseSchema.Event.NAME);
                event.mIcon = getIconSafely(cursor, LegacyDatabaseSchema.Event.ICON, event.mName);
                event.mNote = getStringSafely(cursor, LegacyDatabaseSchema.Event.NOTE);
                event.mStartDate = getStringSafely(cursor, LegacyDatabaseSchema.Event.DATE_FROM);
                event.mEndDate = getStringSafely(cursor, LegacyDatabaseSchema.Event.DATE_TO);
                event.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Event.UUID);
                event.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Event.DELETED);
                event.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Event.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, event);
                    long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Event.ID);
                    mCacheEvent.put(legacyId, id);
                } catch (Exception ignore) {
                    // if an exception occur, the event id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importPlaces(ContentResolver contentResolver) throws ImportException {
        // The legacy database does not have a separate table for places: it saves the
        // place entity as a string directly inside the other tables (transactions,
        // recurrences and debts). We have to query these table to retrieve all the
        // places that have been added (we use the place name as key so, if multiple
        // places with the same name were found, we insert only one place in the table).
        String rawQuery = "SELECT place_name, place_latitude, place_longitude FROM (" +
                "SELECT transaction_place AS place_name, " +
                "transaction_latitude AS place_latitude, " +
                "transaction_longitude AS place_longitude " +
                "FROM transaction_table " +
                "WHERE transaction_deleted = 0 AND transaction_place != '' " +
                "UNION SELECT recurring_place AS place_name, " +
                "recurring_latitude AS place_latitude, " +
                "recurring_longitude AS place_longitude " +
                "FROM recurring_table " +
                "WHERE recurring_deleted = 0 AND recurring_place != '' " +
                "UNION SELECT debt_place AS place_name, " +
                "NULL AS place_latitude, " +
                "NULL AS place_longitude " +
                "FROM debt_table " +
                "WHERE debt_deleted = 0 AND debt_place != '' " +
                ") GROUP BY place_name";
        Cursor cursor = mDatabase.rawQuery(rawQuery, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Place place = new Place();
                place.mName = getStringSafely(cursor, "place_name");
                place.mIcon = getIconSafely(cursor, null, place.mName);
                place.mLatitude = getDoubleObjSafely(cursor, "place_latitude");
                place.mLongitude = getDoubleObjSafely(cursor, "place_longitude");
                place.mUUID = UUID.randomUUID().toString();
                place.mLastEdit = System.currentTimeMillis();
                place.mDeleted = false;
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, place);
                    mCachePlace.put(place.mName, id);
                } catch (Exception ignore) {
                    // if an exception occur, the place id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importPeople(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importEventPeople(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importDebts(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Debt.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Debt.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Debt debt = new Debt();
                debt.mType = getIntSafely(cursor, LegacyDatabaseSchema.Debt.TYPE) == LegacyDatabaseSchema.TYPE_DEBT ? Contract.DebtType.DEBT.getValue() : Contract.DebtType.CREDIT.getValue();
                debt.mDescription = getStringSafely(cursor, LegacyDatabaseSchema.Debt.DESCRIPTION);
                debt.mIcon = getIconSafely(cursor, null, debt.mDescription);
                debt.mDate = getStringSafely(cursor, LegacyDatabaseSchema.Debt.DATE);
                debt.mExpirationDate = getStringSafely(cursor, LegacyDatabaseSchema.Debt.EXPIRATION_DATE);
                debt.mWallet = mCacheWallet.get(getLongSafely(cursor, LegacyDatabaseSchema.Debt.WALLET));
                debt.mNote = getStringSafely(cursor, LegacyDatabaseSchema.Debt.NOTE);
                debt.mPlace = mCachePlace.get(getStringSafely(cursor, LegacyDatabaseSchema.Debt.PLACE));
                debt.mMoney = normalize(
                        mCacheWalletCurrency.get(getLongSafely(cursor, LegacyDatabaseSchema.Debt.WALLET)),
                        getLongSafely(cursor, LegacyDatabaseSchema.Debt.IMPORT)
                );
                debt.mArchived = false;
                debt.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Debt.UUID);
                debt.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Debt.DELETED);
                debt.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Debt.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, debt);
                    long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Debt.ID);
                    mCacheDebt.put(legacyId, id);
                } catch (Exception ignore) {
                    // if an exception occur, the debt id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importDebtPeople(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importBudgets(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Budget.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Budget.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String walletIds = getStringSafely(cursor, LegacyDatabaseSchema.Budget.WALLETS);
                Long[] legacyIds = decodeWallets(walletIds);
                if (legacyIds != null && legacyIds.length > 0) {
                    // reconstruct the budget item starting from the legacy copy
                    Budget budget = new Budget();
                    int legacyType = getIntSafely(cursor, LegacyDatabaseSchema.Budget.TYPE);
                    if (legacyType == LegacyDatabaseSchema.TYPE_BUDGET_CATEGORY) {
                        budget.mType = Contract.BudgetType.CATEGORY.getValue();
                        budget.mCategory = mCacheCategory.get(getLongSafely(cursor, LegacyDatabaseSchema.Budget.CATEGORY_OR_FLOW));
                    } else if (legacyType == LegacyDatabaseSchema.TYPE_BUDGET_CASH_FLOW) {
                        int legacyFlow = getIntSafely(cursor, LegacyDatabaseSchema.Budget.CATEGORY_OR_FLOW);
                        if (legacyFlow == LegacyDatabaseSchema.BUDGET_INFLOW) {
                            budget.mType = Contract.BudgetType.INCOMES.getValue();
                        } else if (legacyFlow == LegacyDatabaseSchema.BUDGET_OUTFLOW) {
                            budget.mType = Contract.BudgetType.EXPENSES.getValue();
                        } else {
                            throw new ImportException("Invalid budget flow type (" + legacyFlow + ")");
                        }
                        budget.mCategory = null;
                    } else {
                        throw new ImportException("Invalid budget type (" + legacyType + ")");
                    }
                    budget.mStartDate = getStringSafely(cursor, LegacyDatabaseSchema.Budget.DATE_FROM);
                    budget.mEndDate = getStringSafely(cursor, LegacyDatabaseSchema.Budget.DATE_TO);
                    budget.mCurrency = mCacheWalletCurrency.get(legacyIds[0]);
                    budget.mMoney = normalize(budget.mCurrency, getLongSafely(cursor, LegacyDatabaseSchema.Budget.MAX));
                    budget.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Budget.UUID);
                    budget.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Budget.DELETED);
                    budget.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Budget.LAST_EDIT);
                    try {
                        long id = SQLDatabaseImporter.insert(contentResolver, budget);
                        // for each linked wallet, insert a link in the new database
                        for (Long legacyId : legacyIds) {
                            BudgetWallet budgetWallet = new BudgetWallet();
                            budgetWallet.mBudget = id;
                            budgetWallet.mWallet = mCacheWallet.get(legacyId);
                            budgetWallet.mUUID = UUID.randomUUID().toString();
                            budgetWallet.mLastEdit = System.currentTimeMillis();
                            budgetWallet.mDeleted = false;
                            SQLDatabaseImporter.insert(contentResolver, budgetWallet);
                        }
                    } catch (Exception ignore) {
                        // if an exception occur, the budget is not inserted in the database
                        // and all the related budget-wallet entities are not created
                    }
                }
            }
            cursor.close();
        }
    }

    private Long[] decodeWallets(String stringToDecode) {
        if (stringToDecode != null) {
            String[] parts = stringToDecode.split(String.valueOf(';'));
            List<Long> walletIds = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                try {
                    long walletId = Long.parseLong(parts[i]);
                    if (mCacheWallet.get(walletId) != null) {
                        walletIds.add(walletId);
                    }
                } catch (NumberFormatException ignore) {
                    // malformed wallet string!
                }
            }
            // convert list to array
            Long[] answer = new Long[walletIds.size()];
            for (int i = 0; i < answer.length; i++) {
                answer[i] = walletIds.get(i);
            }
            return answer;
        }
        return null;
    }

    @Override
    public void importBudgetWallets(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importSavings(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Saving.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Saving.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Saving saving = new Saving();
                saving.mDescription = getStringSafely(cursor, LegacyDatabaseSchema.Saving.DESCRIPTION);
                saving.mIcon = getIconSafely(cursor, null, saving.mDescription);
                saving.mStartMoney = normalize(
                        mCacheWalletCurrency.get(getLongSafely(cursor, LegacyDatabaseSchema.Saving.WALLET)),
                        getLongSafely(cursor, LegacyDatabaseSchema.Saving.INITIAL)
                );
                saving.mEndMoney = normalize(
                        mCacheWalletCurrency.get(getLongSafely(cursor, LegacyDatabaseSchema.Saving.WALLET)),
                        getLongSafely(cursor, LegacyDatabaseSchema.Saving.TARGET)
                );
                saving.mWallet = mCacheWallet.get(getLongSafely(cursor, LegacyDatabaseSchema.Saving.WALLET));
                saving.mEndDate = getStringSafely(cursor, LegacyDatabaseSchema.Saving.END_DATE);
                saving.mComplete = getBooleanSafely(cursor, LegacyDatabaseSchema.Saving.COMPLETE);
                saving.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Saving.UUID);
                saving.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Saving.DELETED);
                saving.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Saving.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, saving);
                    long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Saving.ID);
                    mCacheSaving.put(legacyId, id);
                } catch (Exception ignore) {
                    // if an exception occur, the saving id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importRecurrentTransactions(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Recurrence.DELETED + " = 0";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Recurrence.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                RecurrentTransaction transaction = new RecurrentTransaction();
                long legacyId = getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.ID);
                transaction.mMoney = normalize(
                        mCacheWalletCurrency.get(getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.WALLET)),
                        getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.IMPORT)
                );
                transaction.mDescription = getStringSafely(cursor, LegacyDatabaseSchema.Recurrence.DESCRIPTION);
                transaction.mCategory = mCacheCategory.get(getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.CATEGORY));
                transaction.mDirection = getBooleanSafely(cursor, LegacyDatabaseSchema.Recurrence.IS_IN) ? Contract.Direction.INCOME : Contract.Direction.EXPENSE;
                transaction.mWallet = mCacheWallet.get(getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.WALLET));
                transaction.mPlace = mCachePlace.get(getStringSafely(cursor, LegacyDatabaseSchema.Recurrence.PLACE));
                transaction.mNote = getStringSafely(cursor, LegacyDatabaseSchema.Recurrence.NOTE);
                transaction.mConfirmed = true;
                transaction.mCountInTotal = true;
                String encodedInfo = getStringSafely(cursor, LegacyDatabaseSchema.Recurrence.ENCODED_INFO);
                RecurrenceSetting recurrenceSetting = parseRecurrenceSetting(encodedInfo);
                transaction.mStartDate = DateUtils.getSQLDateString(recurrenceSetting.getStartDate());
                transaction.mLastOccurrence = getRecurrenceLastOccurrence(legacyId, transaction.mStartDate);
                Date nextOccurrence = recurrenceSetting.getNextOccurrence(DateUtils.getDateFromSQLDateString(transaction.mLastOccurrence));
                transaction.mNextOccurrence = nextOccurrence != null ? DateUtils.getSQLDateString(nextOccurrence) : null;
                transaction.mRule = recurrenceSetting.getRule();
                transaction.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Recurrence.UUID);
                transaction.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Recurrence.DELETED);
                transaction.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Recurrence.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, transaction);
                    mCacheRecurrences.put(legacyId, id);
                } catch (Exception ignore) {
                    // if an exception occur, the recurrence id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    private RecurrenceSetting parseRecurrenceSetting(String encodedInfo) {
        String[] rules = encodedInfo.split(";");
        int recurrenceType = RecurrenceSetting.TYPE_DAILY;
        switch (Integer.parseInt(rules[0])) {
            case 0:
                recurrenceType = RecurrenceSetting.TYPE_DAILY;
                break;
            case 1:
                recurrenceType = RecurrenceSetting.TYPE_WEEKLY;
                break;
            case 2:
                recurrenceType = RecurrenceSetting.TYPE_MONTHLY;
                break;
            case 3:
                recurrenceType = RecurrenceSetting.TYPE_YEARLY;
                break;
        }
        int offset = Integer.parseInt(rules[1]);
        int monthRule = Integer.parseInt(rules[2]);
        boolean[] weekDays = new boolean[7];
        weekDays[0] = rules[3].contains("0");
        weekDays[1] = rules[3].contains("1");
        weekDays[2] = rules[3].contains("2");
        weekDays[3] = rules[3].contains("3");
        weekDays[4] = rules[3].contains("4");
        weekDays[5] = rules[3].contains("5");
        weekDays[6] = rules[3].contains("6");
        int endType = RecurrenceSetting.END_FOREVER;
        switch (Integer.parseInt(rules[4])) {
            case 0:
                endType = RecurrenceSetting.END_UNTIL;
                break;
            case 1:
                endType = RecurrenceSetting.END_FOR;
                break;
        }
        int occurrences = Integer.parseInt(rules[5]);
        Date startDate = DateUtils.getDateFromSQLDateString(rules[6]);
        Date endDate = DateUtils.getDateFromSQLDateString(rules[7]);
        // reconstruct the rule object
        RecurrenceSetting.Builder builder = new RecurrenceSetting.Builder(startDate, recurrenceType);
        builder.setOffset(offset);
        if (recurrenceType == RecurrenceSetting.TYPE_WEEKLY) {
            builder.setRepeatWeekDay(weekDays);
        } else if (recurrenceType == RecurrenceSetting.TYPE_MONTHLY) {
            builder.setRepeatSameMonthDay();
        }
        switch (endType) {
            case RecurrenceSetting.END_UNTIL:
                builder.setEndUntil(endDate);
                break;
            case RecurrenceSetting.END_FOR:
                builder.setEndFor(occurrences);
                break;
        }
        return builder.build();
    }

    private String getRecurrenceLastOccurrence(long id, String startDate) {
        // we need to query the maximum value of the transaction date where the date
        // is previous or equals today
        String rawQuery = "SELECT MAX(" + LegacyDatabaseSchema.Transaction.DATE + ") FROM " +
                LegacyDatabaseSchema.Transaction.TABLE + " WHERE DATE(" +
                LegacyDatabaseSchema.Transaction.DATE + ") <= DATE('now', 'localtime') AND " +
                LegacyDatabaseSchema.Transaction.RECURRENCE + " = ? AND " +
                LegacyDatabaseSchema.Transaction.DELETED + " = 0";
        String[] selectionArgs = new String[] {String.valueOf(id)};
        Cursor cursor = mDatabase.rawQuery(rawQuery, selectionArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String date = cursor.getString(0);
                if (!TextUtils.isEmpty(date)) {
                    return date;
                }
            }
            cursor.close();
        }
        return startDate;
    }

    @Override
    public void importRecurrentTransfers(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransactions(ContentResolver contentResolver) throws ImportException {
        String selection = LegacyDatabaseSchema.Transaction.DELETED + " = 0 AND DATETIME(" +
                LegacyDatabaseSchema.Transaction.DATE + ") <= DATETIME('now', 'localtime')";
        Cursor cursor = mDatabase.query(LegacyDatabaseSchema.Transaction.TABLE, null, selection, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Transaction transaction = new Transaction();
                transaction.mMoney = normalize(
                        mCacheWalletCurrency.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.WALLET)),
                        getLongSafely(cursor, LegacyDatabaseSchema.Transaction.IMPORT)
                );
                transaction.mDate = getDateTimeSafely(cursor, LegacyDatabaseSchema.Transaction.DATE);
                transaction.mDescription = getStringSafely(cursor, LegacyDatabaseSchema.Transaction.DESCRIPTION);
                transaction.mCategory = mCacheCategory.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.CATEGORY));
                transaction.mDirection = getBooleanSafely(cursor, LegacyDatabaseSchema.Transaction.IS_IN) ? Contract.Direction.INCOME : Contract.Direction.EXPENSE;
                transaction.mWallet = mCacheWallet.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.WALLET));
                transaction.mPlace = mCachePlace.get(getStringSafely(cursor, LegacyDatabaseSchema.Transaction.PLACE));
                transaction.mNote = getStringSafely(cursor, LegacyDatabaseSchema.Transaction.NOTE);
                transaction.mEvent = mCacheEvent.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.EVENT));
                transaction.mRecurrence = mCacheRecurrences.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.RECURRENCE));
                transaction.mConfirmed = true;
                transaction.mCountInTotal = true;
                if (!cursor.isNull(cursor.getColumnIndex(LegacyDatabaseSchema.Transaction.DEBT))) {
                    transaction.mDebt = mCacheDebt.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.DEBT));
                    if (transaction.mDebt != null) {
                        transaction.mType = Contract.TransactionType.DEBT;
                    } else {
                        transaction.mType = Contract.TransactionType.STANDARD;
                    }
                } else if (!cursor.isNull(cursor.getColumnIndex(LegacyDatabaseSchema.Transaction.SAVING))) {
                    transaction.mSaving = mCacheSaving.get(getLongSafely(cursor, LegacyDatabaseSchema.Transaction.SAVING));
                    if (transaction.mSaving != null) {
                        transaction.mType = Contract.TransactionType.SAVING;
                    } else {
                        transaction.mType = Contract.TransactionType.STANDARD;
                    }
                } else if (!cursor.isNull(cursor.getColumnIndex(LegacyDatabaseSchema.Transaction.TRANSFER))) {
                    transaction.mType = Contract.TransactionType.TRANSFER;
                } else {
                    transaction.mType = Contract.TransactionType.STANDARD;
                }
                transaction.mUUID = getStringSafely(cursor, LegacyDatabaseSchema.Transaction.UUID);
                transaction.mDeleted = getBooleanSafely(cursor, LegacyDatabaseSchema.Transaction.DELETED);
                transaction.mLastEdit = getLongSafely(cursor, LegacyDatabaseSchema.Transaction.LAST_EDIT);
                try {
                    long id = SQLDatabaseImporter.insert(contentResolver, transaction);
                    mCacheTransaction.add(transaction.mUUID);
                    // handle the case of a transfer transaction
                    if (transaction.mType == Contract.TransactionType.TRANSFER) {
                        String transferId = getStringSafely(cursor, LegacyDatabaseSchema.Transaction.TRANSFER);
                        Transfer transfer = mCacheTransfer.get(transferId);
                        if (transfer == null) {
                            transfer = new Transfer();
                        }
                        if (transaction.mDirection == Contract.Direction.INCOME) {
                            transfer.mTransactionTo = id;
                        } else if (transaction.mDirection == Contract.Direction.EXPENSE) {
                            long legacyCategoryId = getLongSafely(cursor, LegacyDatabaseSchema.Transaction.CATEGORY);
                            String categoryTag = mCacheCategoryTag.get(legacyCategoryId);
                            if (Contract.CategoryTag.TRANSFER_TAX.equals(categoryTag)) {
                                transfer.mTransactionTax = id;
                            } else if (Contract.CategoryTag.TRANSFER.equals(categoryTag)) {
                                transfer.mTransactionFrom = id;
                                transfer.mDescription = transaction.mDescription;
                                transfer.mDate = transaction.mDate;
                                transfer.mNote = transaction.mNote;
                                transfer.mPlace = transaction.mPlace;
                                transfer.mEvent = transaction.mEvent;
                                transfer.mConfirmed = true;
                                transfer.mCountInTotal = true;
                                transfer.mUUID = UUID.randomUUID().toString();
                                transfer.mDeleted = false;
                                transfer.mLastEdit = System.currentTimeMillis();
                            }
                        }
                        mCacheTransfer.put(transferId, transfer);
                    }
                } catch (Exception ignore) {
                    // if an exception occur, the transaction id is not inserted in the cache
                    // and all the related stuff will be discarded during the migration
                }
            }
            cursor.close();
        }
    }

    @Override
    public void importTransactionPeople(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransactionModels(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransfers(ContentResolver contentResolver) throws ImportException {
        for (Transfer transfer : mCacheTransfer.values()) {
            if (transfer.mTransactionFrom != null && transfer.mTransactionTo != null) {
                try {
                    SQLDatabaseImporter.insert(contentResolver, transfer);
                } catch (Exception ignore) {
                    // if an exception occur, the transfer has not been correctly inserted and the
                    // related transactions are registered as standard transactions
                }
            }
        }
    }

    @Override
    public void importTransferPeople(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransferModels(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importAttachments(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransactionAttachments(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    @Override
    public void importTransferAttachments(ContentResolver contentResolver) throws ImportException {
        // not supported in legacy database
    }

    public String getAttachmentId(String legacyIdentifier) throws ImportException {
        return mCacheTransaction.contains(legacyIdentifier) ? UUID.randomUUID().toString() : null;
    }

    public void importAttachment(ContentResolver contentResolver, String file, long size) throws ImportException {
        Attachment attachment = new Attachment();
        attachment.mFile = file;
        attachment.mName = file;
        attachment.mType = "image/*";
        attachment.mSize = size;
        attachment.mUUID = UUID.randomUUID().toString();
        attachment.mLastEdit = System.currentTimeMillis();
        attachment.mDeleted = false;
        try {
            SQLDatabaseImporter.insert(contentResolver, attachment);
        } catch (Exception ignore) {
            // simply ignore this case
        }
    }

    private String getIconSafely(Cursor cursor, String columnName, String name) {
        /*
        if (columnName != null) {
            int index = cursor.getColumnIndex(columnName);
            if (index >= 0) {
                String legacyIcon = cursor.getString(index);
                Icon icon = LegacyIconMapper.getLegacyIcon(legacyIcon, name);
                if (icon == null) {
                    icon = new ColorIcon(Utils.getRandomMDColor(), IconPicker.getColorIconString(name));
                }
                return icon.toString();
            }
        }*/
        int randomColor = Utils.getRandomMDColor();
        String iconText = IconPicker.getColorIconString(name);
        Icon icon = new ColorIcon(randomColor, iconText);
        return icon.toString();
    }

    private String getDateTimeSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0) {
            return cursor.getString(index) + " 00:00:00";
        }
        return null;
    }

    private String getStringSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0) {
            return cursor.getString(index);
        }
        return null;
    }

    private int getIntSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0) {
            return cursor.getInt(index);
        }
        return 0;
    }

    private long getLongSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0) {
            return cursor.getLong(index);
        }
        return 0L;
    }

    private boolean getBooleanSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 && cursor.getInt(index) == 1;
    }

    private Double getDoubleObjSafely(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0 && !cursor.isNull(index)) {
            return cursor.getDouble(index);
        }
        return null;
    }

    @Override
    public void close() throws ImportException {
        mDatabase.close();
    }

    private static long normalize(String currency, long money) {
        CurrencyUnit currencyUnit = CurrencyManager.getCurrency(currency);
        if (currencyUnit != null && currencyUnit.getDecimals() != LEGACY_DECIMALS) {
            return MoneyFormatter.normalize(money, LEGACY_DECIMALS, currencyUnit.getDecimals());
        }
        return money;
    }
}