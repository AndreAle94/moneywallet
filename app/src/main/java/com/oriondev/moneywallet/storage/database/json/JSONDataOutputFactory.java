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

import com.oriondev.moneywallet.storage.database.model.Attachment;
import com.oriondev.moneywallet.storage.database.model.Budget;
import com.oriondev.moneywallet.storage.database.model.BudgetWallet;
import com.oriondev.moneywallet.storage.database.model.Category;
import com.oriondev.moneywallet.storage.database.model.Currency;
import com.oriondev.moneywallet.storage.database.model.Debt;
import com.oriondev.moneywallet.storage.database.model.DebtPerson;
import com.oriondev.moneywallet.storage.database.model.Event;
import com.oriondev.moneywallet.storage.database.model.EventPerson;
import com.oriondev.moneywallet.storage.database.model.Person;
import com.oriondev.moneywallet.storage.database.model.Place;
import com.oriondev.moneywallet.storage.database.model.RecurrentTransaction;
import com.oriondev.moneywallet.storage.database.model.RecurrentTransfer;
import com.oriondev.moneywallet.storage.database.model.Saving;
import com.oriondev.moneywallet.storage.database.model.Transaction;
import com.oriondev.moneywallet.storage.database.model.TransactionAttachment;
import com.oriondev.moneywallet.storage.database.model.TransactionModel;
import com.oriondev.moneywallet.storage.database.model.TransactionPerson;
import com.oriondev.moneywallet.storage.database.model.Transfer;
import com.oriondev.moneywallet.storage.database.model.TransferAttachment;
import com.oriondev.moneywallet.storage.database.model.TransferModel;
import com.oriondev.moneywallet.storage.database.model.TransferPerson;
import com.oriondev.moneywallet.storage.database.model.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by andrea on 28/10/18.
 */
/*package-local*/ class JSONDataOutputFactory {

    private final HashMap<Long, String> mCacheWallets = new HashMap<>();
    private final HashMap<Long, String> mCacheCategories = new HashMap<>();
    private final HashMap<Long, String> mCacheEvents = new HashMap<>();
    private final HashMap<Long, String> mCachePlaces = new HashMap<>();
    private final HashMap<Long, String> mCachePeople = new HashMap<>();
    private final HashMap<Long, String> mCacheDebts = new HashMap<>();
    private final HashMap<Long, String> mCacheBudgets = new HashMap<>();
    private final HashMap<Long, String> mCacheSavings = new HashMap<>();
    private final HashMap<Long, String> mCacheRecurrentTransactions = new HashMap<>();
    private final HashMap<Long, String> mCacheRecurrentTransfers = new HashMap<>();
    private final HashMap<Long, String> mCacheTransactions = new HashMap<>();
    private final HashMap<Long, String> mCacheTransfers = new HashMap<>();
    private final HashMap<Long, String> mCacheAttachments = new HashMap<>();

    /*package-local*/ JSONObject getObject(Currency currency) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Currency.ISO, currency.mIso);
        object.put(JSONDatabase.Currency.NAME, currency.mName);
        object.put(JSONDatabase.Currency.SYMBOL, currency.mSymbol);
        object.put(JSONDatabase.Currency.DECIMALS, currency.mDecimals);
        object.put(JSONDatabase.Currency.FAVOURITE, currency.mFavourite);
        object.put(JSONDatabase.Currency.LAST_EDIT, currency.mLastEdit);
        object.put(JSONDatabase.Currency.DELETED, currency.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Wallet wallet) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Wallet.NAME, wallet.mName);
        object.put(JSONDatabase.Wallet.ICON, wallet.mIcon);
        object.put(JSONDatabase.Wallet.CURRENCY, wallet.mCurrency);
        object.put(JSONDatabase.Wallet.START_MONEY, wallet.mStartMoney);
        object.put(JSONDatabase.Wallet.COUNT_IN_TOTAL, wallet.mCountInTotal);
        object.put(JSONDatabase.Wallet.ARCHIVED, wallet.mArchived);
        object.put(JSONDatabase.Wallet.NOTE, wallet.mNote);
        object.put(JSONDatabase.Wallet.TAG, wallet.mTag);
        object.put(JSONDatabase.Wallet.ID, wallet.mUUID);
        object.put(JSONDatabase.Wallet.INDEX, wallet.mIndex);
        object.put(JSONDatabase.Wallet.LAST_EDIT, wallet.mLastEdit);
        object.put(JSONDatabase.Wallet.DELETED, wallet.mDeleted);
        mCacheWallets.put(wallet.mId, wallet.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(Category category) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Category.NAME, category.mName);
        object.put(JSONDatabase.Category.ICON, category.mIcon);
        object.put(JSONDatabase.Category.TYPE, category.mType);
        object.put(JSONDatabase.Category.PARENT, mCacheCategories.get(category.mParent));
        object.put(JSONDatabase.Category.TAG, category.mTag);
        object.put(JSONDatabase.Category.SHOW_REPORT, category.mShowReport);
        object.put(JSONDatabase.Category.INDEX, category.mIndex);
        object.put(JSONDatabase.Category.ID, category.mUUID);
        object.put(JSONDatabase.Category.LAST_EDIT, category.mLastEdit);
        object.put(JSONDatabase.Category.DELETED, category.mDeleted);
        mCacheCategories.put(category.mId, category.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(Event event) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Event.NAME, event.mName);
        object.put(JSONDatabase.Event.ICON, event.mIcon);
        object.put(JSONDatabase.Event.NOTE, event.mNote);
        object.put(JSONDatabase.Event.START_DATE, event.mStartDate);
        object.put(JSONDatabase.Event.END_DATE, event.mEndDate);
        object.put(JSONDatabase.Event.TAG, event.mTag);
        object.put(JSONDatabase.Event.ID, event.mUUID);
        object.put(JSONDatabase.Event.LAST_EDIT, event.mLastEdit);
        object.put(JSONDatabase.Event.DELETED, event.mDeleted);
        mCacheEvents.put(event.mId, event.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(Place place) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Place.NAME, place.mName);
        object.put(JSONDatabase.Place.ICON, place.mIcon);
        object.put(JSONDatabase.Place.ADDRESS, place.mAddress);
        object.put(JSONDatabase.Place.LATITUDE, place.mLatitude);
        object.put(JSONDatabase.Place.LONGITUDE, place.mLongitude);
        object.put(JSONDatabase.Place.TAG, place.mTag);
        object.put(JSONDatabase.Place.ID, place.mUUID);
        object.put(JSONDatabase.Place.LAST_EDIT, place.mLastEdit);
        object.put(JSONDatabase.Place.DELETED, place.mDeleted);
        mCachePlaces.put(place.mId, place.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(Person person) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Person.NAME, person.mName);
        object.put(JSONDatabase.Person.ICON, person.mIcon);
        object.put(JSONDatabase.Person.NOTE, person.mNote);
        object.put(JSONDatabase.Person.TAG, person.mTag);
        object.put(JSONDatabase.Person.ID, person.mUUID);
        object.put(JSONDatabase.Person.LAST_EDIT, person.mLastEdit);
        object.put(JSONDatabase.Person.DELETED, person.mDeleted);
        mCachePeople.put(person.mId, person.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(EventPerson eventPerson) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.EventPeople.EVENT, mCacheEvents.get(eventPerson.mEvent));
        object.put(JSONDatabase.EventPeople.PERSON, mCachePeople.get(eventPerson.mPerson));
        object.put(JSONDatabase.EventPeople.ID, eventPerson.mUUID);
        object.put(JSONDatabase.EventPeople.LAST_EDIT, eventPerson.mLastEdit);
        object.put(JSONDatabase.EventPeople.DELETED, eventPerson.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Debt debt) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Debt.TYPE, debt.mType);
        object.put(JSONDatabase.Debt.ICON, debt.mIcon);
        object.put(JSONDatabase.Debt.DESCRIPTION, debt.mDescription);
        object.put(JSONDatabase.Debt.DATE, debt.mDate);
        object.put(JSONDatabase.Debt.EXPIRATION_DATE, debt.mExpirationDate);
        object.put(JSONDatabase.Debt.WALLET, mCacheWallets.get(debt.mWallet));
        object.put(JSONDatabase.Debt.NOTE, debt.mNote);
        object.put(JSONDatabase.Debt.PLACE, mCachePlaces.get(debt.mPlace));
        object.put(JSONDatabase.Debt.MONEY, debt.mMoney);
        object.put(JSONDatabase.Debt.ARCHIVED, debt.mArchived);
        object.put(JSONDatabase.Debt.TAG, debt.mTag);
        object.put(JSONDatabase.Debt.ID, debt.mUUID);
        object.put(JSONDatabase.Debt.LAST_EDIT, debt.mLastEdit);
        object.put(JSONDatabase.Debt.DELETED, debt.mDeleted);
        mCacheDebts.put(debt.mId, debt.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(DebtPerson debtPerson) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.DebtPeople.DEBT, mCacheDebts.get(debtPerson.mDebt));
        object.put(JSONDatabase.DebtPeople.PERSON, mCachePeople.get(debtPerson.mPerson));
        object.put(JSONDatabase.DebtPeople.ID, debtPerson.mUUID);
        object.put(JSONDatabase.DebtPeople.LAST_EDIT, debtPerson.mLastEdit);
        object.put(JSONDatabase.DebtPeople.DELETED, debtPerson.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Budget budget) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Budget.TYPE, budget.mType);
        object.put(JSONDatabase.Budget.CATEGORY, mCacheCategories.get(budget.mCategory));
        object.put(JSONDatabase.Budget.START_DATE, budget.mStartDate);
        object.put(JSONDatabase.Budget.END_DATE, budget.mEndDate);
        object.put(JSONDatabase.Budget.MONEY, budget.mMoney);
        object.put(JSONDatabase.Budget.CURRENCY, budget.mCurrency);
        object.put(JSONDatabase.Budget.TAG, budget.mTag);
        object.put(JSONDatabase.Budget.ID, budget.mUUID);
        object.put(JSONDatabase.Budget.LAST_EDIT, budget.mLastEdit);
        object.put(JSONDatabase.Budget.DELETED, budget.mDeleted);
        mCacheBudgets.put(budget.mId, budget.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(BudgetWallet budgetWallet) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.BudgetWallet.BUDGET, mCacheBudgets.get(budgetWallet.mBudget));
        object.put(JSONDatabase.BudgetWallet.WALLET, mCacheWallets.get(budgetWallet.mWallet));
        object.put(JSONDatabase.BudgetWallet.ID, budgetWallet.mUUID);
        object.put(JSONDatabase.BudgetWallet.LAST_EDIT, budgetWallet.mLastEdit);
        object.put(JSONDatabase.BudgetWallet.DELETED, budgetWallet.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Saving saving) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Saving.DESCRIPTION, saving.mDescription);
        object.put(JSONDatabase.Saving.ICON, saving.mIcon);
        object.put(JSONDatabase.Saving.START_MONEY, saving.mStartMoney);
        object.put(JSONDatabase.Saving.END_MONEY, saving.mEndMoney);
        object.put(JSONDatabase.Saving.WALLET, mCacheWallets.get(saving.mWallet));
        object.put(JSONDatabase.Saving.END_DATE, saving.mEndDate);
        object.put(JSONDatabase.Saving.COMPLETE, saving.mComplete);
        object.put(JSONDatabase.Saving.NOTE, saving.mNote);
        object.put(JSONDatabase.Saving.TAG, saving.mTag);
        object.put(JSONDatabase.Saving.ID, saving.mUUID);
        object.put(JSONDatabase.Saving.LAST_EDIT, saving.mLastEdit);
        object.put(JSONDatabase.Saving.DELETED, saving.mDeleted);
        mCacheSavings.put(saving.mId, saving.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(RecurrentTransaction recurrentTransaction) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.RecurrentTransaction.MONEY, recurrentTransaction.mMoney);
        object.put(JSONDatabase.RecurrentTransaction.DESCRIPTION, recurrentTransaction.mDescription);
        object.put(JSONDatabase.RecurrentTransaction.CATEGORY, mCacheCategories.get(recurrentTransaction.mCategory));
        object.put(JSONDatabase.RecurrentTransaction.DIRECTION, recurrentTransaction.mDirection);
        object.put(JSONDatabase.RecurrentTransaction.WALLET, mCacheWallets.get(recurrentTransaction.mWallet));
        object.put(JSONDatabase.RecurrentTransaction.PLACE, mCachePlaces.get(recurrentTransaction.mPlace));
        object.put(JSONDatabase.RecurrentTransaction.NOTE, recurrentTransaction.mNote);
        object.put(JSONDatabase.RecurrentTransaction.EVENT, mCacheEvents.get(recurrentTransaction.mEvent));
        object.put(JSONDatabase.RecurrentTransaction.CONFIRMED, recurrentTransaction.mConfirmed);
        object.put(JSONDatabase.RecurrentTransaction.COUNT_IN_TOTAL, recurrentTransaction.mCountInTotal);
        object.put(JSONDatabase.RecurrentTransaction.START_DATE, recurrentTransaction.mStartDate);
        object.put(JSONDatabase.RecurrentTransaction.LAST_OCCURRENCE, recurrentTransaction.mLastOccurrence);
        object.put(JSONDatabase.RecurrentTransaction.NEXT_OCCURRENCE, recurrentTransaction.mNextOccurrence);
        object.put(JSONDatabase.RecurrentTransaction.RULE, recurrentTransaction.mRule);
        object.put(JSONDatabase.RecurrentTransaction.TAG, recurrentTransaction.mTag);
        object.put(JSONDatabase.RecurrentTransaction.ID, recurrentTransaction.mUUID);
        object.put(JSONDatabase.RecurrentTransaction.LAST_EDIT, recurrentTransaction.mLastEdit);
        object.put(JSONDatabase.RecurrentTransaction.DELETED, recurrentTransaction.mDeleted);
        mCacheRecurrentTransactions.put(recurrentTransaction.mId, recurrentTransaction.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(RecurrentTransfer recurrentTransfer) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.RecurrentTransfer.DESCRIPTION, recurrentTransfer.mDescription);
        object.put(JSONDatabase.RecurrentTransfer.WALLET_FROM, mCacheWallets.get(recurrentTransfer.mFromWallet));
        object.put(JSONDatabase.RecurrentTransfer.WALLET_TO, mCacheWallets.get(recurrentTransfer.mToWallet));
        object.put(JSONDatabase.RecurrentTransfer.MONEY_FROM, recurrentTransfer.mFromMoney);
        object.put(JSONDatabase.RecurrentTransfer.MONEY_TO, recurrentTransfer.mToMoney);
        object.put(JSONDatabase.RecurrentTransfer.MONEY_TAX, recurrentTransfer.mTaxMoney);
        object.put(JSONDatabase.RecurrentTransfer.NOTE, recurrentTransfer.mNote);
        object.put(JSONDatabase.RecurrentTransfer.EVENT, mCacheEvents.get(recurrentTransfer.mEvent));
        object.put(JSONDatabase.RecurrentTransfer.PLACE, mCachePlaces.get(recurrentTransfer.mPlace));
        object.put(JSONDatabase.RecurrentTransfer.CONFIRMED, recurrentTransfer.mConfirmed);
        object.put(JSONDatabase.RecurrentTransfer.COUNT_IN_TOTAL, recurrentTransfer.mCountInTotal);
        object.put(JSONDatabase.RecurrentTransfer.START_DATE, recurrentTransfer.mStartDate);
        object.put(JSONDatabase.RecurrentTransfer.LAST_OCCURRENCE, recurrentTransfer.mLastOccurrence);
        object.put(JSONDatabase.RecurrentTransfer.NEXT_OCCURRENCE, recurrentTransfer.mNextOccurrence);
        object.put(JSONDatabase.RecurrentTransfer.RULE, recurrentTransfer.mRule);
        object.put(JSONDatabase.RecurrentTransfer.TAG, recurrentTransfer.mTag);
        object.put(JSONDatabase.RecurrentTransfer.ID, recurrentTransfer.mUUID);
        object.put(JSONDatabase.RecurrentTransfer.LAST_EDIT, recurrentTransfer.mLastEdit);
        object.put(JSONDatabase.RecurrentTransfer.DELETED, recurrentTransfer.mDeleted);
        mCacheRecurrentTransfers.put(recurrentTransfer.mId, recurrentTransfer.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(Transaction transaction) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Transaction.MONEY, transaction.mMoney);
        object.put(JSONDatabase.Transaction.DATE, transaction.mDate);
        object.put(JSONDatabase.Transaction.DESCRIPTION, transaction.mDescription);
        object.put(JSONDatabase.Transaction.CATEGORY, mCacheCategories.get(transaction.mCategory));
        object.put(JSONDatabase.Transaction.DIRECTION, transaction.mDirection);
        object.put(JSONDatabase.Transaction.TYPE, transaction.mType);
        object.put(JSONDatabase.Transaction.WALLET, mCacheWallets.get(transaction.mWallet));
        object.put(JSONDatabase.Transaction.PLACE, mCachePlaces.get(transaction.mPlace));
        object.put(JSONDatabase.Transaction.NOTE, transaction.mNote);
        object.put(JSONDatabase.Transaction.SAVING, mCacheSavings.get(transaction.mSaving));
        object.put(JSONDatabase.Transaction.DEBT, mCacheDebts.get(transaction.mDebt));
        object.put(JSONDatabase.Transaction.EVENT, mCacheEvents.get(transaction.mEvent));
        object.put(JSONDatabase.Transaction.RECURRENCE, mCacheRecurrentTransactions.get(transaction.mRecurrence));
        object.put(JSONDatabase.Transaction.CONFIRMED, transaction.mConfirmed);
        object.put(JSONDatabase.Transaction.COUNT_IN_TOTAL, transaction.mCountInTotal);
        object.put(JSONDatabase.Transaction.TAG, transaction.mTag);
        object.put(JSONDatabase.Transaction.ID, transaction.mUUID);
        object.put(JSONDatabase.Transaction.LAST_EDIT, transaction.mLastEdit);
        object.put(JSONDatabase.Transaction.DELETED, transaction.mDeleted);
        mCacheTransactions.put(transaction.mId, transaction.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransactionPerson transactionPerson) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransactionPeople.TRANSACTION, mCacheTransactions.get(transactionPerson.mTransaction));
        object.put(JSONDatabase.TransactionPeople.PERSON, mCachePeople.get(transactionPerson.mPerson));
        object.put(JSONDatabase.TransactionPeople.ID, transactionPerson.mUUID);
        object.put(JSONDatabase.TransactionPeople.LAST_EDIT, transactionPerson.mLastEdit);
        object.put(JSONDatabase.TransactionPeople.DELETED, transactionPerson.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransactionModel transactionModel) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransactionModel.MONEY, transactionModel.mMoney);
        object.put(JSONDatabase.TransactionModel.DESCRIPTION, transactionModel.mDescription);
        object.put(JSONDatabase.TransactionModel.CATEGORY, mCacheCategories.get(transactionModel.mCategory));
        object.put(JSONDatabase.TransactionModel.DIRECTION, transactionModel.mDirection);
        object.put(JSONDatabase.TransactionModel.WALLET, mCacheWallets.get(transactionModel.mWallet));
        object.put(JSONDatabase.TransactionModel.PLACE, mCachePlaces.get(transactionModel.mPlace));
        object.put(JSONDatabase.TransactionModel.NOTE, transactionModel.mNote);
        object.put(JSONDatabase.TransactionModel.EVENT, mCacheEvents.get(transactionModel.mEvent));
        object.put(JSONDatabase.TransactionModel.CONFIRMED, transactionModel.mConfirmed);
        object.put(JSONDatabase.TransactionModel.COUNT_IN_TOTAL, transactionModel.mCountInTotal);
        object.put(JSONDatabase.TransactionModel.TAG, transactionModel.mTag);
        object.put(JSONDatabase.TransactionModel.ID, transactionModel.mUUID);
        object.put(JSONDatabase.TransactionModel.LAST_EDIT, transactionModel.mLastEdit);
        object.put(JSONDatabase.TransactionModel.DELETED, transactionModel.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Transfer transfer) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Transfer.DESCRIPTION, transfer.mDescription);
        object.put(JSONDatabase.Transfer.DATE, transfer.mDate);
        object.put(JSONDatabase.Transfer.FROM, mCacheTransactions.get(transfer.mTransactionFrom));
        object.put(JSONDatabase.Transfer.TO, mCacheTransactions.get(transfer.mTransactionTo));
        object.put(JSONDatabase.Transfer.TAX, mCacheTransactions.get(transfer.mTransactionTax));
        object.put(JSONDatabase.Transfer.NOTE, transfer.mNote);
        object.put(JSONDatabase.Transfer.PLACE, mCachePlaces.get(transfer.mPlace));
        object.put(JSONDatabase.Transfer.EVENT, mCacheEvents.get(transfer.mEvent));
        object.put(JSONDatabase.Transfer.RECURRENCE, mCacheRecurrentTransfers.get(transfer.mRecurrence));
        object.put(JSONDatabase.Transfer.CONFIRMED, transfer.mConfirmed);
        object.put(JSONDatabase.Transfer.COUNT_IN_TOTAL, transfer.mCountInTotal);
        object.put(JSONDatabase.Transfer.TAG, transfer.mTag);
        object.put(JSONDatabase.Transfer.ID, transfer.mUUID);
        object.put(JSONDatabase.Transfer.LAST_EDIT, transfer.mLastEdit);
        object.put(JSONDatabase.Transfer.DELETED, transfer.mDeleted);
        mCacheTransfers.put(transfer.mId, transfer.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransferPerson transferPerson) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransferPeople.TRANSFER, mCacheTransfers.get(transferPerson.mTransfer));
        object.put(JSONDatabase.TransferPeople.PERSON, mCachePeople.get(transferPerson.mPerson));
        object.put(JSONDatabase.TransferPeople.ID, transferPerson.mUUID);
        object.put(JSONDatabase.TransferPeople.LAST_EDIT, transferPerson.mLastEdit);
        object.put(JSONDatabase.TransferPeople.DELETED, transferPerson.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransferModel transferModel) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransferModel.DESCRIPTION, transferModel.mDescription);
        object.put(JSONDatabase.TransferModel.WALLET_FROM, mCacheWallets.get(transferModel.mFromWallet));
        object.put(JSONDatabase.TransferModel.WALLET_TO, mCacheWallets.get(transferModel.mToWallet));
        object.put(JSONDatabase.TransferModel.MONEY_FROM, transferModel.mFromMoney);
        object.put(JSONDatabase.TransferModel.MONEY_TO, transferModel.mToMoney);
        object.put(JSONDatabase.TransferModel.MONEY_TAX, transferModel.mTaxMoney);
        object.put(JSONDatabase.TransferModel.NOTE, transferModel.mNote);
        object.put(JSONDatabase.TransferModel.EVENT, mCacheEvents.get(transferModel.mEvent));
        object.put(JSONDatabase.TransferModel.PLACE, mCachePlaces.get(transferModel.mPlace));
        object.put(JSONDatabase.TransferModel.CONFIRMED, transferModel.mConfirmed);
        object.put(JSONDatabase.TransferModel.COUNT_IN_TOTAL, transferModel.mCountInTotal);
        object.put(JSONDatabase.TransferModel.TAG, transferModel.mTag);
        object.put(JSONDatabase.TransferModel.ID, transferModel.mUUID);
        object.put(JSONDatabase.TransferModel.LAST_EDIT, transferModel.mLastEdit);
        object.put(JSONDatabase.TransferModel.DELETED, transferModel.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(Attachment attachment) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.Attachment.FILE, attachment.mFile);
        object.put(JSONDatabase.Attachment.NAME, attachment.mName);
        object.put(JSONDatabase.Attachment.TYPE, attachment.mType);
        object.put(JSONDatabase.Attachment.SIZE, attachment.mSize);
        object.put(JSONDatabase.Attachment.TAG, attachment.mTag);
        object.put(JSONDatabase.Attachment.ID, attachment.mUUID);
        object.put(JSONDatabase.Attachment.LAST_EDIT, attachment.mLastEdit);
        object.put(JSONDatabase.Attachment.DELETED, attachment.mDeleted);
        mCacheAttachments.put(attachment.mId, attachment.mUUID);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransactionAttachment transactionAttachment) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransactionAttachment.TRANSACTION, mCacheTransactions.get(transactionAttachment.mTransaction));
        object.put(JSONDatabase.TransactionAttachment.ATTACHMENT, mCacheAttachments.get(transactionAttachment.mAttachment));
        object.put(JSONDatabase.TransactionAttachment.ID, transactionAttachment.mUUID);
        object.put(JSONDatabase.TransactionAttachment.LAST_EDIT, transactionAttachment.mLastEdit);
        object.put(JSONDatabase.TransactionAttachment.DELETED, transactionAttachment.mDeleted);
        return object;
    }

    /*package-local*/ JSONObject getObject(TransferAttachment transferAttachment) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSONDatabase.TransferAttachment.TRANSFER, mCacheTransfers.get(transferAttachment.mTransfer));
        object.put(JSONDatabase.TransferAttachment.ATTACHMENT, mCacheAttachments.get(transferAttachment.mAttachment));
        object.put(JSONDatabase.TransferAttachment.ID, transferAttachment.mUUID);
        object.put(JSONDatabase.TransferAttachment.LAST_EDIT, transferAttachment.mLastEdit);
        object.put(JSONDatabase.TransferAttachment.DELETED, transferAttachment.mDeleted);
        return object;
    }
}