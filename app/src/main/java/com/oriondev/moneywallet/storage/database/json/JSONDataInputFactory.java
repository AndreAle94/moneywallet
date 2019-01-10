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

package com.oriondev.moneywallet.storage.database.json;

import com.oriondev.moneywallet.storage.database.model.*;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class acts as object factory and mapping between the JSONObjects and
 * the native database objects. It also uses an internal cache to correctly
 * handle the mapping between the UUID that are used as identifiers in the
 * json file and native longs that are used as id inside the database.
 */
/*package-local*/ class JSONDataInputFactory {

    private final Map<String, Long> mCacheWallets = new HashMap<>();
    private final Map<String, Long> mCacheCategories = new HashMap<>();
    private final Map<String, Long> mCacheEvents = new HashMap<>();
    private final Map<String, Long> mCachePlaces = new HashMap<>();
    private final Map<String, Long> mCachePeople = new HashMap<>();
    private final Map<String, Long> mCacheDebts = new HashMap<>();
    private final Map<String, Long> mCacheBudgets = new HashMap<>();
    private final Map<String, Long> mCacheSavings = new HashMap<>();
    private final Map<String, Long> mCacheRecurrentTransactions = new HashMap<>();
    private final Map<String, Long> mCacheRecurrentTransfers = new HashMap<>();
    private final Map<String, Long> mCacheTransactions = new HashMap<>();
    private final Map<String, Long> mCacheTransfers = new HashMap<>();
    private final Map<String, Long> mCacheAttachments = new HashMap<>();

    /*package-local*/ void cacheWallet(String uuid, long id) {
        mCacheWallets.put(uuid, id);
    }

    /*package-local*/ void cacheCategory(String uuid, long id) {
        mCacheCategories.put(uuid, id);
    }

    /*package-local*/ void cacheEvent(String uuid, long id) {
        mCacheEvents.put(uuid, id);
    }

    /*package-local*/ void cachePlace(String uuid, long id) {
        mCachePlaces.put(uuid, id);
    }

    /*package-local*/ void cachePerson(String uuid, long id) {
        mCachePeople.put(uuid, id);
    }

    /*package-local*/ void cacheDebt(String uuid, long id) {
        mCacheDebts.put(uuid, id);
    }

    /*package-local*/ void cacheBudget(String uuid, long id) {
        mCacheBudgets.put(uuid, id);
    }

    /*package-local*/ void cacheSaving(String uuid, long id) {
        mCacheSavings.put(uuid, id);
    }

    /*package-local*/ void cacheRecurrentTransaction(String uuid, long id) {
        mCacheRecurrentTransactions.put(uuid, id);
    }

    /*package-local*/ void cacheRecurrentTransfer(String uuid, long id) {
        mCacheRecurrentTransfers.put(uuid, id);
    }

    /*package-local*/ void cacheTransaction(String uuid, long id) {
        mCacheTransactions.put(uuid, id);
    }

    /*package-local*/ void cacheTransfer(String uuid, long id) {
        mCacheTransfers.put(uuid, id);
    }

    /*package-local*/ void cacheAttachment(String uuid, long id) {
        mCacheAttachments.put(uuid, id);
    }

    /*package-local*/ Currency getCurrency(JSONObject object) {
        Currency currency = new Currency();
        currency.mIso = object.optString(JSONDatabase.Currency.ISO, null);
        currency.mName = object.optString(JSONDatabase.Currency.NAME, null);
        currency.mSymbol = object.optString(JSONDatabase.Currency.SYMBOL, null);
        currency.mDecimals = object.optInt(JSONDatabase.Currency.DECIMALS, 2);
        currency.mFavourite = object.optBoolean(JSONDatabase.Currency.FAVOURITE, false);
        currency.mUUID = "currency_" + object.optString(JSONDatabase.Currency.ISO, null);
        currency.mLastEdit = object.optLong(JSONDatabase.Currency.LAST_EDIT, 0L);
        currency.mDeleted = object.optBoolean(JSONDatabase.Currency.DELETED, false);
        return currency;
    }

    /*package-local*/ Wallet getWallet(JSONObject object) {
        Wallet wallet = new Wallet();
        wallet.mName = object.optString(JSONDatabase.Wallet.NAME, null);
        wallet.mIcon = object.optString(JSONDatabase.Wallet.ICON, null);
        wallet.mCurrency = object.optString(JSONDatabase.Wallet.CURRENCY, null);
        wallet.mStartMoney = object.optLong(JSONDatabase.Wallet.START_MONEY, 0L);
        wallet.mCountInTotal = object.optBoolean(JSONDatabase.Wallet.COUNT_IN_TOTAL, true);
        wallet.mArchived = object.optBoolean(JSONDatabase.Wallet.ARCHIVED, false);
        wallet.mNote = object.optString(JSONDatabase.Wallet.NOTE, null);
        wallet.mTag = object.optString(JSONDatabase.Wallet.TAG, null);
        wallet.mIndex = object.optInt(JSONDatabase.Wallet.INDEX, 0);
        wallet.mUUID = object.optString(JSONDatabase.Wallet.ID, null);
        wallet.mLastEdit = object.optLong(JSONDatabase.Wallet.LAST_EDIT, 0L);
        wallet.mDeleted = object.optBoolean(JSONDatabase.Wallet.DELETED, false);
        return wallet;
    }

    /*package-local*/ Category getCategory(JSONObject object) {
        Category category = new Category();
        category.mName = object.optString(JSONDatabase.Category.NAME, null);
        category.mIcon = object.optString(JSONDatabase.Category.ICON, null);
        category.mType = object.optInt(JSONDatabase.Category.TYPE, 0);
        category.mParent = mCacheCategories.get(object.optString(JSONDatabase.Category.PARENT, null));
        category.mTag = object.optString(JSONDatabase.Category.TAG, null);
        category.mShowReport = object.optBoolean(JSONDatabase.Category.SHOW_REPORT, true);
        category.mIndex = object.optInt(JSONDatabase.Category.INDEX, 0);
        category.mUUID = object.optString(JSONDatabase.Category.ID, null);
        category.mLastEdit = object.optLong(JSONDatabase.Category.LAST_EDIT, 0L);
        category.mDeleted = object.optBoolean(JSONDatabase.Category.DELETED, false);
        return category;
    }

    /*package-local*/ Event getEvent(JSONObject object) {
        Event event = new Event();
        event.mName = object.optString(JSONDatabase.Event.NAME, null);
        event.mIcon = object.optString(JSONDatabase.Event.ICON, null);
        event.mNote = object.optString(JSONDatabase.Event.NOTE, null);
        event.mStartDate = object.optString(JSONDatabase.Event.START_DATE, null);
        event.mEndDate = object.optString(JSONDatabase.Event.END_DATE, null);
        event.mTag = object.optString(JSONDatabase.Event.TAG, null);
        event.mUUID = object.optString(JSONDatabase.Event.ID, null);
        event.mLastEdit = object.optLong(JSONDatabase.Event.LAST_EDIT, 0L);
        event.mDeleted = object.optBoolean(JSONDatabase.Event.DELETED, false);
        return event;
    }

    /*package-local*/ Place getPlace(JSONObject object) {
        Place place = new Place();
        place.mName = object.optString(JSONDatabase.Place.NAME, null);
        place.mIcon = object.optString(JSONDatabase.Place.ICON, null);
        place.mAddress = object.optString(JSONDatabase.Place.ADDRESS, null);
        place.mLatitude = object.has(JSONDatabase.Place.LATITUDE) ? object.optDouble(JSONDatabase.Place.LATITUDE, 0d) : null;
        place.mLongitude = object.has(JSONDatabase.Place.LONGITUDE) ? object.optDouble(JSONDatabase.Place.LONGITUDE, 0d) : null;
        place.mTag = object.optString(JSONDatabase.Place.TAG, null);
        place.mUUID = object.optString(JSONDatabase.Place.ID, null);
        place.mLastEdit = object.optLong(JSONDatabase.Place.LAST_EDIT, 0L);
        place.mDeleted = object.optBoolean(JSONDatabase.Place.DELETED, false);
        return place;
    }

    /*package-local*/ Person getPerson(JSONObject object) {
        Person person = new Person();
        person.mName = object.optString(JSONDatabase.Person.NAME, null);
        person.mIcon = object.optString(JSONDatabase.Person.ICON, null);
        person.mNote = object.optString(JSONDatabase.Person.NOTE, null);
        person.mTag = object.optString(JSONDatabase.Person.TAG, null);
        person.mUUID = object.optString(JSONDatabase.Person.ID, null);
        person.mLastEdit = object.optLong(JSONDatabase.Person.LAST_EDIT, 0L);
        person.mDeleted = object.optBoolean(JSONDatabase.Person.DELETED, false);
        return person;
    }

    /*package-local*/ EventPerson getEventPerson(JSONObject object) {
        EventPerson eventPerson = new EventPerson();
        eventPerson.mEvent = mCacheEvents.get(object.optString(JSONDatabase.EventPeople.EVENT, null));
        eventPerson.mPerson = mCachePeople.get(object.optString(JSONDatabase.EventPeople.PERSON, null));
        eventPerson.mUUID = object.optString(JSONDatabase.EventPeople.ID, null);
        eventPerson.mLastEdit = object.optLong(JSONDatabase.EventPeople.LAST_EDIT, 0L);
        eventPerson.mDeleted = object.optBoolean(JSONDatabase.EventPeople.DELETED, false);
        return eventPerson;
    }

    /*package-local*/ Debt getDebt(JSONObject object) {
        Debt debt = new Debt();
        debt.mType = object.optInt(JSONDatabase.Debt.TYPE, 0);
        debt.mIcon = object.optString(JSONDatabase.Debt.ICON, null);
        debt.mDescription = object.optString(JSONDatabase.Debt.DESCRIPTION, null);
        debt.mDate = object.optString(JSONDatabase.Debt.DATE, null);
        debt.mExpirationDate = object.optString(JSONDatabase.Debt.EXPIRATION_DATE, null);
        debt.mWallet = mCacheWallets.get(object.optString(JSONDatabase.Debt.WALLET, null));
        debt.mNote = object.optString(JSONDatabase.Debt.NOTE, null);
        debt.mPlace = mCachePlaces.get(object.optString(JSONDatabase.Debt.PLACE, null));
        debt.mMoney = object.optLong(JSONDatabase.Debt.MONEY, 0L);
        debt.mArchived = object.optBoolean(JSONDatabase.Debt.ARCHIVED, false);
        debt.mTag = object.optString(JSONDatabase.Debt.TAG, null);
        debt.mUUID = object.optString(JSONDatabase.Debt.ID, null);
        debt.mLastEdit = object.optLong(JSONDatabase.Debt.LAST_EDIT, 0L);
        debt.mDeleted = object.optBoolean(JSONDatabase.Debt.DELETED, false);
        return debt;
    }

    /*package-local*/ DebtPerson getDebtPerson(JSONObject object) {
        DebtPerson debtPerson = new DebtPerson();
        debtPerson.mDebt = mCacheDebts.get(object.optString(JSONDatabase.DebtPeople.DEBT, null));
        debtPerson.mPerson = mCachePeople.get(object.optString(JSONDatabase.DebtPeople.PERSON, null));
        debtPerson.mUUID = object.optString(JSONDatabase.DebtPeople.ID, null);
        debtPerson.mLastEdit = object.optLong(JSONDatabase.DebtPeople.LAST_EDIT, 0L);
        debtPerson.mDeleted = object.optBoolean(JSONDatabase.DebtPeople.DELETED, false);
        return debtPerson;
    }

    /*package-local*/ Budget getBudget(JSONObject object) {
        Budget budget = new Budget();
        budget.mType = object.optInt(JSONDatabase.Budget.TYPE, 0);
        budget.mCategory = mCacheCategories.get(object.optString(JSONDatabase.Budget.CATEGORY, null));
        budget.mStartDate = object.optString(JSONDatabase.Budget.START_DATE, null);
        budget.mEndDate = object.optString(JSONDatabase.Budget.END_DATE, null);
        budget.mMoney = object.optLong(JSONDatabase.Budget.MONEY, 0L);
        budget.mCurrency = object.optString(JSONDatabase.Budget.CURRENCY, null);
        budget.mTag = object.optString(JSONDatabase.Budget.TAG, null);
        budget.mUUID = object.optString(JSONDatabase.Budget.ID, null);
        budget.mLastEdit = object.optLong(JSONDatabase.Budget.LAST_EDIT, 0L);
        budget.mDeleted = object.optBoolean(JSONDatabase.Budget.DELETED, false);
        return budget;
    }

    /*package-local*/ BudgetWallet getBudgetWallet(JSONObject object) {
        BudgetWallet budgetWallet = new BudgetWallet();
        budgetWallet.mBudget = mCacheBudgets.get(object.optString(JSONDatabase.BudgetWallet.BUDGET, null));
        budgetWallet.mWallet = mCacheWallets.get(object.optString(JSONDatabase.BudgetWallet.WALLET, null));
        budgetWallet.mUUID = object.optString(JSONDatabase.BudgetWallet.ID, null);
        budgetWallet.mLastEdit = object.optLong(JSONDatabase.BudgetWallet.LAST_EDIT, 0L);
        budgetWallet.mDeleted = object.optBoolean(JSONDatabase.BudgetWallet.DELETED, false);
        return budgetWallet;
    }

    /*package-local*/ Saving getSaving(JSONObject object) {
        Saving saving = new Saving();
        saving.mDescription = object.optString(JSONDatabase.Saving.DESCRIPTION, null);
        saving.mIcon = object.optString(JSONDatabase.Saving.ICON, null);
        saving.mStartMoney = object.optLong(JSONDatabase.Saving.START_MONEY, 0L);
        saving.mEndMoney = object.optLong(JSONDatabase.Saving.END_MONEY, 0L);
        saving.mWallet = mCacheWallets.get(object.optString(JSONDatabase.Saving.WALLET, null));
        saving.mEndDate = object.optString(JSONDatabase.Saving.END_DATE, null);
        saving.mComplete = object.optBoolean(JSONDatabase.Saving.COMPLETE, false);
        saving.mNote = object.optString(JSONDatabase.Saving.NOTE, null);
        saving.mTag = object.optString(JSONDatabase.Saving.TAG, null);
        saving.mUUID = object.optString(JSONDatabase.Saving.ID, null);
        saving.mLastEdit = object.optLong(JSONDatabase.Saving.LAST_EDIT, 0L);
        saving.mDeleted = object.optBoolean(JSONDatabase.Saving.DELETED, false);
        return saving;
    }

    /*package-local*/ RecurrentTransaction getRecurrentTransaction(JSONObject object) {
        RecurrentTransaction transaction = new RecurrentTransaction();
        transaction.mMoney = object.optLong(JSONDatabase.RecurrentTransaction.MONEY, 0L);
        transaction.mDescription = object.optString(JSONDatabase.RecurrentTransaction.DESCRIPTION, null);
        transaction.mCategory = mCacheCategories.get(object.optString(JSONDatabase.RecurrentTransaction.CATEGORY, null));
        transaction.mDirection = object.optInt(JSONDatabase.RecurrentTransaction.DIRECTION, 0);
        transaction.mWallet = mCacheWallets.get(object.optString(JSONDatabase.RecurrentTransaction.WALLET, null));
        transaction.mPlace = mCachePlaces.get(object.optString(JSONDatabase.RecurrentTransaction.PLACE, null));
        transaction.mNote = object.optString(JSONDatabase.RecurrentTransaction.NOTE, null);
        transaction.mEvent = mCacheEvents.get(object.optString(JSONDatabase.RecurrentTransaction.EVENT, null));
        transaction.mConfirmed = object.optBoolean(JSONDatabase.RecurrentTransaction.CONFIRMED, true);
        transaction.mCountInTotal = object.optBoolean(JSONDatabase.RecurrentTransaction.COUNT_IN_TOTAL, true);
        transaction.mStartDate = object.optString(JSONDatabase.RecurrentTransaction.START_DATE, null);
        transaction.mLastOccurrence = object.optString(JSONDatabase.RecurrentTransaction.LAST_OCCURRENCE, null);
        transaction.mNextOccurrence = object.optString(JSONDatabase.RecurrentTransaction.NEXT_OCCURRENCE, null);
        transaction.mRule = object.optString(JSONDatabase.RecurrentTransaction.RULE, null);
        transaction.mTag = object.optString(JSONDatabase.RecurrentTransaction.TAG, null);
        transaction.mUUID = object.optString(JSONDatabase.RecurrentTransaction.ID, null);
        transaction.mLastEdit = object.optLong(JSONDatabase.RecurrentTransaction.LAST_EDIT, 0L);
        transaction.mDeleted = object.optBoolean(JSONDatabase.RecurrentTransaction.DELETED, false);
        return transaction;
    }

    /*package-local*/ RecurrentTransfer getRecurrentTransfer(JSONObject object) {
        RecurrentTransfer transfer = new RecurrentTransfer();
        transfer.mDescription = object.optString(JSONDatabase.RecurrentTransfer.DESCRIPTION, null);
        transfer.mFromWallet = mCacheWallets.get(object.optString(JSONDatabase.RecurrentTransfer.WALLET_FROM, null));
        transfer.mToWallet = mCacheWallets.get(object.optString(JSONDatabase.RecurrentTransfer.WALLET_TO, null));
        transfer.mFromMoney = object.optLong(JSONDatabase.RecurrentTransfer.MONEY_FROM, 0L);
        transfer.mToMoney = object.optLong(JSONDatabase.RecurrentTransfer.MONEY_TO, 0L);
        transfer.mTaxMoney = object.optLong(JSONDatabase.RecurrentTransfer.MONEY_TAX, 0L);
        transfer.mNote = object.optString(JSONDatabase.RecurrentTransfer.NOTE, null);
        transfer.mEvent = mCacheEvents.get(object.optString(JSONDatabase.RecurrentTransfer.EVENT, null));
        transfer.mPlace = mCachePlaces.get(object.optString(JSONDatabase.RecurrentTransfer.PLACE, null));
        transfer.mConfirmed = object.optBoolean(JSONDatabase.RecurrentTransfer.CONFIRMED, true);
        transfer.mCountInTotal = object.optBoolean(JSONDatabase.RecurrentTransfer.COUNT_IN_TOTAL, true);
        transfer.mStartDate = object.optString(JSONDatabase.RecurrentTransfer.START_DATE, null);
        transfer.mLastOccurrence = object.optString(JSONDatabase.RecurrentTransfer.LAST_OCCURRENCE, null);
        transfer.mNextOccurrence = object.optString(JSONDatabase.RecurrentTransfer.NEXT_OCCURRENCE, null);
        transfer.mRule = object.optString(JSONDatabase.RecurrentTransfer.RULE, null);
        transfer.mTag = object.optString(JSONDatabase.RecurrentTransfer.TAG, null);
        transfer.mUUID = object.optString(JSONDatabase.RecurrentTransfer.ID, null);
        transfer.mLastEdit = object.optLong(JSONDatabase.RecurrentTransfer.LAST_EDIT, 0L);
        transfer.mDeleted = object.optBoolean(JSONDatabase.RecurrentTransfer.DELETED, false);
        return transfer;
    }

    /*package-local*/ Transaction getTransaction(JSONObject object) {
        Transaction transaction = new Transaction();
        transaction.mMoney = object.optLong(JSONDatabase.Transaction.MONEY, 0L);
        transaction.mDate = object.optString(JSONDatabase.Transaction.DATE, null);
        transaction.mDescription = object.optString(JSONDatabase.Transaction.DESCRIPTION, null);
        transaction.mCategory = mCacheCategories.get(object.optString(JSONDatabase.Transaction.CATEGORY, null));
        transaction.mDirection = object.optInt(JSONDatabase.Transaction.DIRECTION, 0);
        transaction.mType = object.optInt(JSONDatabase.Transaction.TYPE, 0);
        transaction.mWallet = mCacheWallets.get(object.optString(JSONDatabase.Transaction.WALLET, null));
        transaction.mPlace = mCachePlaces.get(object.optString(JSONDatabase.Transaction.PLACE, null));
        transaction.mNote = object.optString(JSONDatabase.Transaction.NOTE, null);
        transaction.mSaving = mCacheSavings.get(object.optString(JSONDatabase.Transaction.SAVING, null));
        transaction.mDebt = mCacheDebts.get(object.optString(JSONDatabase.Transaction.DEBT, null));
        transaction.mEvent = mCacheEvents.get(object.optString(JSONDatabase.Transaction.EVENT, null));
        transaction.mRecurrence = mCacheRecurrentTransactions.get(object.optString(JSONDatabase.Transaction.RECURRENCE, null));
        transaction.mConfirmed = object.optBoolean(JSONDatabase.Transaction.CONFIRMED, true);
        transaction.mCountInTotal = object.optBoolean(JSONDatabase.Transaction.COUNT_IN_TOTAL, true);
        transaction.mTag = object.optString(JSONDatabase.Transaction.TAG, null);
        transaction.mUUID = object.optString(JSONDatabase.Transaction.ID, null);
        transaction.mLastEdit = object.optLong(JSONDatabase.Transaction.LAST_EDIT, 0L);
        transaction.mDeleted = object.optBoolean(JSONDatabase.Transaction.DELETED, false);
        return transaction;
    }

    /*package-local*/ TransactionPerson getTransactionPerson(JSONObject object) {
        TransactionPerson transactionPerson = new TransactionPerson();
        transactionPerson.mTransaction = mCacheTransactions.get(object.optString(JSONDatabase.TransactionPeople.TRANSACTION, null));
        transactionPerson.mPerson = mCachePeople.get(object.optString(JSONDatabase.TransactionPeople.PERSON, null));
        transactionPerson.mUUID = object.optString(JSONDatabase.TransactionPeople.ID, null);
        transactionPerson.mLastEdit = object.optLong(JSONDatabase.TransactionPeople.LAST_EDIT, 0L);
        transactionPerson.mDeleted = object.optBoolean(JSONDatabase.TransactionPeople.DELETED, false);
        return transactionPerson;
    }

    /*package-local*/ TransactionModel getTransactionModel(JSONObject object) {
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.mMoney = object.optLong(JSONDatabase.TransactionModel.MONEY, 0L);
        transactionModel.mDescription = object.optString(JSONDatabase.TransactionModel.DESCRIPTION, null);
        transactionModel.mCategory = mCacheCategories.get(object.optString(JSONDatabase.TransactionModel.CATEGORY, null));
        transactionModel.mDirection = object.optInt(JSONDatabase.TransactionModel.DIRECTION, 0);
        transactionModel.mWallet = mCacheWallets.get(object.optString(JSONDatabase.TransactionModel.WALLET, null));
        transactionModel.mPlace = mCachePlaces.get(object.optString(JSONDatabase.TransactionModel.PLACE, null));
        transactionModel.mNote = object.optString(JSONDatabase.TransactionModel.NOTE, null);
        transactionModel.mEvent = mCacheEvents.get(object.optString(JSONDatabase.TransactionModel.EVENT, null));
        transactionModel.mConfirmed = object.optBoolean(JSONDatabase.TransactionModel.CONFIRMED, true);
        transactionModel.mCountInTotal = object.optBoolean(JSONDatabase.TransactionModel.COUNT_IN_TOTAL, true);
        transactionModel.mTag = object.optString(JSONDatabase.TransactionModel.TAG, null);
        transactionModel.mUUID = object.optString(JSONDatabase.TransactionModel.ID, null);
        transactionModel.mLastEdit = object.optLong(JSONDatabase.TransactionModel.LAST_EDIT, 0L);
        transactionModel.mDeleted = object.optBoolean(JSONDatabase.TransactionModel.DELETED, false);
        return transactionModel;
    }

    /*package-local*/ Transfer getTransfer(JSONObject object) {
        Transfer transfer = new Transfer();
        transfer.mDescription = object.optString(JSONDatabase.Transfer.DESCRIPTION, null);
        transfer.mDate = object.optString(JSONDatabase.Transfer.DATE, null);
        transfer.mTransactionFrom = mCacheTransactions.get(object.optString(JSONDatabase.Transfer.FROM, null));
        transfer.mTransactionTo = mCacheTransactions.get(object.optString(JSONDatabase.Transfer.TO, null));
        transfer.mTransactionTax = mCacheTransactions.get(object.optString(JSONDatabase.Transfer.TAX, null));
        transfer.mNote = object.optString(JSONDatabase.Transfer.NOTE, null);
        transfer.mPlace = mCachePlaces.get(object.optString(JSONDatabase.Transfer.PLACE, null));
        transfer.mEvent = mCacheEvents.get(object.optString(JSONDatabase.Transfer.EVENT, null));
        transfer.mRecurrence = mCacheRecurrentTransfers.get(object.optString(JSONDatabase.Transfer.RECURRENCE, null));
        transfer.mConfirmed = object.optBoolean(JSONDatabase.Transfer.CONFIRMED, true);
        transfer.mCountInTotal = object.optBoolean(JSONDatabase.Transfer.COUNT_IN_TOTAL, true);
        transfer.mTag = object.optString(JSONDatabase.Transfer.TAG, null);
        transfer.mUUID = object.optString(JSONDatabase.Transfer.ID, null);
        transfer.mLastEdit = object.optLong(JSONDatabase.Transfer.LAST_EDIT, 0L);
        transfer.mDeleted = object.optBoolean(JSONDatabase.Transfer.DELETED, false);
        return transfer;
    }

    /*package-local*/ TransferPerson getTransferPerson(JSONObject object) {
        TransferPerson transferPerson = new TransferPerson();
        transferPerson.mTransfer = mCacheTransfers.get(object.optString(JSONDatabase.TransferPeople.TRANSFER, null));
        transferPerson.mPerson = mCachePeople.get(object.optString(JSONDatabase.TransferPeople.PERSON, null));
        transferPerson.mUUID = object.optString(JSONDatabase.TransferPeople.ID, null);
        transferPerson.mLastEdit = object.optLong(JSONDatabase.TransferPeople.LAST_EDIT, 0L);
        transferPerson.mDeleted = object.optBoolean(JSONDatabase.TransferPeople.DELETED, false);
        return transferPerson;
    }

    /*package-local*/ TransferModel getTransferModel(JSONObject object) {
        TransferModel transferModel = new TransferModel();
        transferModel.mDescription = object.optString(JSONDatabase.TransferModel.DESCRIPTION, null);
        transferModel.mFromWallet = mCacheWallets.get(object.optString(JSONDatabase.TransferModel.WALLET_FROM, null));
        transferModel.mToWallet = mCacheWallets.get(object.optString(JSONDatabase.TransferModel.WALLET_TO, null));
        transferModel.mFromMoney = object.optLong(JSONDatabase.TransferModel.MONEY_FROM, 0L);
        transferModel.mToMoney = object.optLong(JSONDatabase.TransferModel.MONEY_TO, 0L);
        transferModel.mTaxMoney = object.optLong(JSONDatabase.TransferModel.MONEY_TAX, 0L);
        transferModel.mNote = object.optString(JSONDatabase.TransferModel.NOTE, null);
        transferModel.mEvent = mCacheEvents.get(object.optString(JSONDatabase.TransferModel.EVENT, null));
        transferModel.mPlace = mCachePlaces.get(object.optString(JSONDatabase.TransferModel.PLACE, null));
        transferModel.mConfirmed = object.optBoolean(JSONDatabase.TransferModel.CONFIRMED, true);
        transferModel.mCountInTotal = object.optBoolean(JSONDatabase.TransferModel.COUNT_IN_TOTAL, true);
        transferModel.mTag = object.optString(JSONDatabase.TransferModel.TAG, null);
        transferModel.mUUID = object.optString(JSONDatabase.TransferModel.ID, null);
        transferModel.mLastEdit = object.optLong(JSONDatabase.TransferModel.LAST_EDIT, 0L);
        transferModel.mDeleted = object.optBoolean(JSONDatabase.TransferModel.DELETED, false);
        return transferModel;
    }

    /*package-local*/ Attachment getAttachment(JSONObject object) {
        Attachment attachment = new Attachment();
        attachment.mFile = object.optString(JSONDatabase.Attachment.FILE, null);
        attachment.mName = object.optString(JSONDatabase.Attachment.NAME, null);
        attachment.mType = object.optString(JSONDatabase.Attachment.TYPE, null);
        attachment.mSize = object.optLong(JSONDatabase.Attachment.SIZE, 0L);
        attachment.mTag = object.optString(JSONDatabase.Attachment.TAG, null);
        attachment.mUUID = object.optString(JSONDatabase.Attachment.ID, null);
        attachment.mLastEdit = object.optLong(JSONDatabase.Attachment.LAST_EDIT, 0L);
        attachment.mDeleted = object.optBoolean(JSONDatabase.Attachment.DELETED, false);
        return attachment;
    }

    /*package-local*/ TransactionAttachment getTransactionAttachment(JSONObject object) {
        TransactionAttachment transactionAttachment = new TransactionAttachment();
        transactionAttachment.mTransaction = mCacheTransactions.get(object.optString(JSONDatabase.TransactionAttachment.TRANSACTION, null));
        transactionAttachment.mAttachment = mCacheAttachments.get(object.optString(JSONDatabase.TransactionAttachment.ATTACHMENT, null));
        transactionAttachment.mUUID = object.optString(JSONDatabase.TransactionAttachment.ID, null);
        transactionAttachment.mLastEdit = object.optLong(JSONDatabase.TransactionAttachment.LAST_EDIT, 0L);
        transactionAttachment.mDeleted = object.optBoolean(JSONDatabase.TransactionAttachment.DELETED, false);
        return transactionAttachment;
    }

    /*package-local*/ TransferAttachment getTransferAttachment(JSONObject object) {
        TransferAttachment transferAttachment = new TransferAttachment();
        transferAttachment.mTransfer = mCacheTransfers.get(object.optString(JSONDatabase.TransferAttachment.TRANSFER, null));
        transferAttachment.mAttachment = mCacheAttachments.get(object.optString(JSONDatabase.TransferAttachment.ATTACHMENT, null));
        transferAttachment.mUUID = object.optString(JSONDatabase.TransferAttachment.ID, null);
        transferAttachment.mLastEdit = object.optLong(JSONDatabase.TransferAttachment.LAST_EDIT, 0L);
        transferAttachment.mDeleted = object.optBoolean(JSONDatabase.TransferAttachment.DELETED, false);
        return transferAttachment;
    }
}