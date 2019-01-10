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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

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

/**
 * Created by andrea on 28/10/18.
 */
public class SQLDatabaseExporter {

    public static Currency getCurrency(Cursor cursor) {
        Currency object = new Currency();
        object.mIso = cursor.getString(cursor.getColumnIndex(Schema.Currency.ISO));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Currency.NAME));
        object.mSymbol = cursor.getString(cursor.getColumnIndex(Schema.Currency.SYMBOL));
        object.mDecimals = cursor.getInt(cursor.getColumnIndex(Schema.Currency.DECIMALS));
        object.mFavourite = cursor.getInt(cursor.getColumnIndex(Schema.Currency.FAVOURITE)) == 1;
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Currency.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Currency.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Currency.DELETED)) == 1;
        return object;
    }

    public static Wallet getWallet(Cursor cursor) {
        Wallet object = new Wallet();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Wallet.ID));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Wallet.NAME));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Wallet.ICON));
        object.mCurrency = cursor.getString(cursor.getColumnIndex(Schema.Wallet.CURRENCY));
        object.mStartMoney = cursor.getLong(cursor.getColumnIndex(Schema.Wallet.START_MONEY));
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.Wallet.COUNT_IN_TOTAL)) == 1;
        object.mArchived = cursor.getInt(cursor.getColumnIndex(Schema.Wallet.ARCHIVED)) == 1;
        object.mIndex = cursor.getInt(cursor.getColumnIndex(Schema.Wallet.INDEX));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Wallet.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Wallet.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Wallet.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Wallet.DELETED)) == 1;
        return object;
    }

    public static Category getCategory(Cursor cursor) {
        Category object = new Category();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Category.ID));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Category.NAME));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Category.ICON));
        object.mType = cursor.getInt(cursor.getColumnIndex(Schema.Category.TYPE));
        object.mParent = cursor.isNull(cursor.getColumnIndex(Schema.Category.PARENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Category.PARENT));
        object.mShowReport = cursor.getInt(cursor.getColumnIndex(Schema.Category.SHOW_REPORT)) == 1;
        object.mIndex = cursor.getInt(cursor.getColumnIndex(Schema.Category.INDEX));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Category.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Category.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Category.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Category.DELETED)) == 1;
        return object;
    }

    public static Event getEvent(Cursor cursor) {
        Event object = new Event();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Event.ID));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Event.NAME));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Event.ICON));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Event.NOTE));
        object.mStartDate = cursor.getString(cursor.getColumnIndex(Schema.Event.START_DATE));
        object.mEndDate = cursor.getString(cursor.getColumnIndex(Schema.Event.END_DATE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Event.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Event.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Event.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Event.DELETED)) == 1;
        return object;
    }

    public static Place getPlace(Cursor cursor) {
        Place object = new Place();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Place.ID));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Place.NAME));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Place.ICON));
        object.mAddress = cursor.getString(cursor.getColumnIndex(Schema.Place.ADDRESS));
        if (!cursor.isNull(cursor.getColumnIndex(Schema.Place.LATITUDE)) && !cursor.isNull(cursor.getColumnIndex(Schema.Place.LONGITUDE))) {
            object.mLatitude = cursor.getDouble(cursor.getColumnIndex(Schema.Place.LATITUDE));
            object.mLongitude = cursor.getDouble(cursor.getColumnIndex(Schema.Place.LONGITUDE));
        } else {
            object.mLatitude = null;
            object.mLongitude = null;
        }
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Place.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Place.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Place.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Place.DELETED)) == 1;
        return object;
    }

    public static Person getPerson(Cursor cursor) {
        Person object = new Person();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Person.ID));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Person.NAME));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Person.ICON));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Person.NOTE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Person.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Person.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Person.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Person.DELETED)) == 1;
        return object;
    }

    public static EventPerson getEventPerson(Cursor cursor) {
        EventPerson object = new EventPerson();
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.EventPeople.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.EventPeople.EVENT));
        object.mPerson = cursor.isNull(cursor.getColumnIndex(Schema.EventPeople.PERSON)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.EventPeople.PERSON));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.EventPeople.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.EventPeople.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.EventPeople.DELETED)) == 1;
        return object;
    }

    public static Debt getDebt(Cursor cursor) {
        Debt object = new Debt();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Debt.ID));
        object.mType = cursor.getInt(cursor.getColumnIndex(Schema.Debt.TYPE));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Debt.ICON));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.Debt.DESCRIPTION));
        object.mDate = cursor.getString(cursor.getColumnIndex(Schema.Debt.DATE));
        object.mExpirationDate = cursor.getString(cursor.getColumnIndex(Schema.Debt.EXPIRATION_DATE));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.Debt.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Debt.WALLET));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Debt.NOTE));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.Debt.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Debt.PLACE));
        object.mMoney = cursor.getLong(cursor.getColumnIndex(Schema.Debt.MONEY));
        object.mArchived = cursor.getInt(cursor.getColumnIndex(Schema.Debt.ARCHIVED)) == 1;
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Debt.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Debt.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Debt.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Debt.DELETED)) == 1;
        return object;
    }

    public static DebtPerson getDebtPerson(Cursor cursor) {
        DebtPerson object = new DebtPerson();
        object.mDebt = cursor.isNull(cursor.getColumnIndex(Schema.DebtPeople.DEBT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.DebtPeople.DEBT));
        object.mPerson = cursor.isNull(cursor.getColumnIndex(Schema.DebtPeople.PERSON)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.DebtPeople.PERSON));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.DebtPeople.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.DebtPeople.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.DebtPeople.DELETED)) == 1;
        return object;
    }

    public static Budget getBudget(Cursor cursor) {
        Budget object = new Budget();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Budget.ID));
        object.mType = cursor.getInt(cursor.getColumnIndex(Schema.Budget.TYPE));
        object.mCategory = cursor.isNull(cursor.getColumnIndex(Schema.Budget.CATEGORY)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Budget.CATEGORY));
        object.mStartDate = cursor.getString(cursor.getColumnIndex(Schema.Budget.START_DATE));
        object.mEndDate = cursor.getString(cursor.getColumnIndex(Schema.Budget.END_DATE));
        object.mMoney = cursor.getLong(cursor.getColumnIndex(Schema.Budget.MONEY));
        object.mCurrency = cursor.getString(cursor.getColumnIndex(Schema.Budget.CURRENCY));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Budget.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Budget.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Budget.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Budget.DELETED)) == 1;
        return object;
    }

    public static BudgetWallet getBudgetWallet(Cursor cursor) {
        BudgetWallet object = new BudgetWallet();
        object.mBudget = cursor.isNull(cursor.getColumnIndex(Schema.BudgetWallet.BUDGET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.BudgetWallet.BUDGET));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.BudgetWallet.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.BudgetWallet.WALLET));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.BudgetWallet.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.BudgetWallet.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.BudgetWallet.DELETED)) == 1;
        return object;
    }

    public static Saving getSaving(Cursor cursor) {
        Saving object = new Saving();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Saving.ID));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.Saving.DESCRIPTION));
        object.mIcon = cursor.getString(cursor.getColumnIndex(Schema.Saving.ICON));
        object.mStartMoney = cursor.getLong(cursor.getColumnIndex(Schema.Saving.START_MONEY));
        object.mEndMoney = cursor.getLong(cursor.getColumnIndex(Schema.Saving.END_MONEY));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.Saving.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Saving.WALLET));
        object.mEndDate = cursor.getString(cursor.getColumnIndex(Schema.Saving.END_DATE));
        object.mComplete = cursor.getInt(cursor.getColumnIndex(Schema.Saving.COMPLETE)) == 1;
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Saving.NOTE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Saving.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Saving.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Saving.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Saving.DELETED)) == 1;
        return object;
    }

    public static RecurrentTransaction getRecurrentTransaction(Cursor cursor) {
        RecurrentTransaction object = new RecurrentTransaction();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.ID));
        object.mMoney = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.MONEY));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.DESCRIPTION));
        object.mCategory = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransaction.CATEGORY)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.CATEGORY));
        object.mDirection = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransaction.DIRECTION));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransaction.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.WALLET));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransaction.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.PLACE));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.NOTE));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransaction.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.EVENT));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransaction.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransaction.COUNT_IN_TOTAL)) == 1;
        object.mStartDate = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.START_DATE));
        object.mLastOccurrence = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.LAST_OCCURRENCE));
        object.mNextOccurrence = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.NEXT_OCCURRENCE));
        object.mRule = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.RULE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransaction.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransaction.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransaction.DELETED)) == 1;
        return object;
    }

    public static RecurrentTransfer getRecurrentTransfer(Cursor cursor) {
        RecurrentTransfer object = new RecurrentTransfer();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.ID));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.DESCRIPTION));
        object.mFromWallet = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransfer.WALLET_FROM)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.WALLET_FROM));
        object.mToWallet = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransfer.WALLET_TO)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.WALLET_TO));
        object.mFromMoney = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_FROM));
        object.mToMoney = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_TO));
        object.mTaxMoney = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.MONEY_TAX));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.NOTE));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransfer.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.PLACE));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.RecurrentTransfer.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.EVENT));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransfer.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransfer.COUNT_IN_TOTAL)) == 1;
        object.mStartDate = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.START_DATE));
        object.mLastOccurrence = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.LAST_OCCURRENCE));
        object.mNextOccurrence = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.NEXT_OCCURRENCE));
        object.mRule = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.RULE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.RecurrentTransfer.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.RecurrentTransfer.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.RecurrentTransfer.DELETED)) == 1;
        return object;
    }

    public static Transaction getTransaction(Cursor cursor) {
        Transaction object = new Transaction();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Transaction.ID));
        object.mMoney = cursor.getLong(cursor.getColumnIndex(Schema.Transaction.MONEY));
        object.mDate = cursor.getString(cursor.getColumnIndex(Schema.Transaction.DATE));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.Transaction.DESCRIPTION));
        object.mCategory = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.CATEGORY)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.CATEGORY));
        object.mDirection = cursor.getInt(cursor.getColumnIndex(Schema.Transaction.DIRECTION));
        object.mType = cursor.getInt(cursor.getColumnIndex(Schema.Transaction.TYPE));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.WALLET));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.PLACE));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Transaction.NOTE));
        object.mSaving = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.SAVING)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.SAVING));
        object.mDebt = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.DEBT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.DEBT));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.EVENT));
        object.mRecurrence = cursor.isNull(cursor.getColumnIndex(Schema.Transaction.RECURRENCE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transaction.RECURRENCE));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.Transaction.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.Transaction.COUNT_IN_TOTAL)) == 1;
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Transaction.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Transaction.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Transaction.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Transaction.DELETED)) == 1;
        return object;
    }

    public static TransactionPerson getTransactionPerson(Cursor cursor) {
        TransactionPerson object = new TransactionPerson();
        object.mTransaction = cursor.isNull(cursor.getColumnIndex(Schema.TransactionPeople.TRANSACTION)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionPeople.TRANSACTION));
        object.mPerson = cursor.isNull(cursor.getColumnIndex(Schema.TransactionPeople.PERSON)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionPeople.PERSON));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransactionPeople.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransactionPeople.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransactionPeople.DELETED)) == 1;
        return object;
    }

    public static TransactionModel getTransactionModel(Cursor cursor) {
        TransactionModel object = new TransactionModel();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.ID));
        object.mMoney = cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.MONEY));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.TransactionModel.DESCRIPTION));
        object.mCategory = cursor.isNull(cursor.getColumnIndex(Schema.TransactionModel.CATEGORY)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.CATEGORY));
        object.mDirection = cursor.getInt(cursor.getColumnIndex(Schema.TransactionModel.DIRECTION));
        object.mWallet = cursor.isNull(cursor.getColumnIndex(Schema.TransactionModel.WALLET)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.WALLET));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.TransactionModel.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.PLACE));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.TransactionModel.NOTE));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.TransactionModel.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.EVENT));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.TransactionModel.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.TransactionModel.COUNT_IN_TOTAL)) == 1;
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.TransactionModel.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransactionModel.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransactionModel.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransactionModel.DELETED)) == 1;
        return object;
    }

    public static Transfer getTransfer(Cursor cursor) {
        Transfer object = new Transfer();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.ID));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.Transfer.DESCRIPTION));
        object.mDate = cursor.getString(cursor.getColumnIndex(Schema.Transfer.DATE));
        object.mTransactionFrom = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_FROM)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_FROM));
        object.mTransactionTo = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TO)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TO));
        object.mTransactionTax = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TAX)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.TRANSACTION_TAX));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.Transfer.NOTE));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.PLACE));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.EVENT));
        object.mRecurrence = cursor.isNull(cursor.getColumnIndex(Schema.Transfer.RECURRENCE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.Transfer.RECURRENCE));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.Transfer.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.Transfer.COUNT_IN_TOTAL)) == 1;
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Transfer.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Transfer.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Transfer.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Transfer.DELETED)) == 1;
        return object;
    }

    public static TransferPerson getTransferPerson(Cursor cursor) {
        TransferPerson object = new TransferPerson();
        object.mTransfer = cursor.isNull(cursor.getColumnIndex(Schema.TransferPeople.TRANSFER)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferPeople.TRANSFER));
        object.mPerson = cursor.isNull(cursor.getColumnIndex(Schema.TransferPeople.PERSON)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferPeople.PERSON));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransferPeople.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransferPeople.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransferPeople.DELETED)) == 1;
        return object;
    }

    public static TransferModel getTransferModel(Cursor cursor) {
        TransferModel object = new TransferModel();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.ID));
        object.mDescription = cursor.getString(cursor.getColumnIndex(Schema.TransferModel.DESCRIPTION));
        object.mFromWallet = cursor.isNull(cursor.getColumnIndex(Schema.TransferModel.WALLET_FROM)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.WALLET_FROM));
        object.mToWallet = cursor.isNull(cursor.getColumnIndex(Schema.TransferModel.WALLET_TO)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.WALLET_TO));
        object.mFromMoney = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_FROM));
        object.mToMoney = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_TO));
        object.mTaxMoney = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.MONEY_TAX));
        object.mNote = cursor.getString(cursor.getColumnIndex(Schema.TransferModel.NOTE));
        object.mPlace = cursor.isNull(cursor.getColumnIndex(Schema.TransferModel.PLACE)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.PLACE));
        object.mEvent = cursor.isNull(cursor.getColumnIndex(Schema.TransferModel.EVENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.EVENT));
        object.mConfirmed = cursor.getInt(cursor.getColumnIndex(Schema.TransferModel.CONFIRMED)) == 1;
        object.mCountInTotal = cursor.getInt(cursor.getColumnIndex(Schema.TransferModel.COUNT_IN_TOTAL)) == 1;
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.TransferModel.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransferModel.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransferModel.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransferModel.DELETED)) == 1;
        return object;
    }

    public static Attachment getAttachment(Cursor cursor) {
        Attachment object = new Attachment();
        object.mId = cursor.getLong(cursor.getColumnIndex(Schema.Attachment.ID));
        object.mFile = cursor.getString(cursor.getColumnIndex(Schema.Attachment.FILE));
        object.mName = cursor.getString(cursor.getColumnIndex(Schema.Attachment.NAME));
        object.mType = cursor.getString(cursor.getColumnIndex(Schema.Attachment.TYPE));
        object.mSize = cursor.getLong(cursor.getColumnIndex(Schema.Attachment.SIZE));
        object.mTag = cursor.getString(cursor.getColumnIndex(Schema.Attachment.TAG));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.Attachment.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.Attachment.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.Attachment.DELETED)) == 1;
        return object;
    }

    public static TransactionAttachment getTransactionAttachment(Cursor cursor) {
        TransactionAttachment object = new TransactionAttachment();
        object.mTransaction = cursor.isNull(cursor.getColumnIndex(Schema.TransactionAttachment.TRANSACTION)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionAttachment.TRANSACTION));
        object.mAttachment = cursor.isNull(cursor.getColumnIndex(Schema.TransactionAttachment.ATTACHMENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransactionAttachment.ATTACHMENT));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransactionAttachment.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransactionAttachment.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransactionAttachment.DELETED)) == 1;
        return object;
    }

    public static TransferAttachment getTransferAttachment(Cursor cursor) {
        TransferAttachment object = new TransferAttachment();
        object.mTransfer = cursor.isNull(cursor.getColumnIndex(Schema.TransferAttachment.TRANSFER)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferAttachment.TRANSFER));
        object.mAttachment = cursor.isNull(cursor.getColumnIndex(Schema.TransferAttachment.ATTACHMENT)) ? null : cursor.getLong(cursor.getColumnIndex(Schema.TransferAttachment.ATTACHMENT));
        object.mUUID = cursor.getString(cursor.getColumnIndex(Schema.TransferAttachment.UUID));
        object.mLastEdit = cursor.getLong(cursor.getColumnIndex(Schema.TransferAttachment.LAST_EDIT));
        object.mDeleted = cursor.getInt(cursor.getColumnIndex(Schema.TransferAttachment.DELETED)) == 1;
        return object;
    }

    public static Cursor getAllCurrencies(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_CURRENCIES;
        String selection = Schema.Currency.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllWallets(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_WALLETS;
        String selection = Schema.Wallet.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllCategories(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_CATEGORIES;
        String selection = Schema.Category.DELETED + " = 0";
        String sortOrder = Schema.Category.PARENT + " ASC";
        return contentResolver.query(uri, null, selection, null, sortOrder);
    }

    public static Cursor getAllEvents(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_EVENTS;
        String selection = Schema.Event.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllPlaces(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_PLACES;
        String selection = Schema.Place.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllPeople(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_PEOPLE;
        String selection = Schema.Person.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllEventPeople(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_EVENT_PEOPLE;
        String selection = Schema.EventPeople.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllDebt(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_DEBT;
        String selection = Schema.Debt.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllDebtPeople(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_DEBT_PEOPLE;
        String selection = Schema.DebtPeople.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllBudget(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_BUDGET;
        String selection = Schema.Budget.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllBudgetWallets(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_BUDGET_WALLET;
        String selection = Schema.BudgetWallet.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllSavings(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_SAVING;
        String selection = Schema.Saving.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllRecurrentTransactions(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_RECURRENT_TRANSACTION;
        String selection = Schema.RecurrentTransaction.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllRecurrentTransfers(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_RECURRENT_TRANSFER;
        String selection = Schema.RecurrentTransfer.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

    public static Cursor getAllTransactions(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION;
        String selection = Schema.Transaction.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransactionPeople(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_PEOPLE;
        String selection = Schema.TransactionPeople.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransactionModels(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_MODEL;
        String selection = Schema.TransactionModel.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransfers(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSFER;
        String selection = Schema.Transfer.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransferPeople(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_PEOPLE;
        String selection = Schema.TransferPeople.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransferModels(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_MODEL;
        String selection = Schema.TransferModel.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllAttachments(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_ATTACHMENT;
        String selection = Schema.Attachment.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransactionAttachments(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_ATTACHMENT;
        String selection = Schema.TransactionAttachment.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }


    public static Cursor getAllTransferAttachments(ContentResolver contentResolver) {
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_ATTACHMENT;
        String selection = Schema.TransferAttachment.DELETED + " = 0";
        return contentResolver.query(uri, null, selection, null, null);
    }

}