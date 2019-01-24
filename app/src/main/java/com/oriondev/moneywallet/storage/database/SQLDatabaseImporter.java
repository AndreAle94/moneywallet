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
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

import com.oriondev.moneywallet.storage.database.model.*;

/**
 * Created by andrea on 27/10/18.
 */
public class SQLDatabaseImporter {

    public static final String DATABASE_NAME = SQLDatabase.DATABASE_NAME;

    public static long insert(ContentResolver contentResolver, Currency currency) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Currency.ISO, currency.mIso);
        contentValues.put(Schema.Currency.NAME, currency.mName);
        contentValues.put(Schema.Currency.SYMBOL, currency.mSymbol);
        contentValues.put(Schema.Currency.DECIMALS, currency.mDecimals);
        contentValues.put(Schema.Currency.FAVOURITE, currency.mFavourite);
        contentValues.put(Schema.Currency.UUID, currency.mUUID);
        contentValues.put(Schema.Currency.LAST_EDIT, currency.mLastEdit);
        contentValues.put(Schema.Currency.DELETED, currency.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_CURRENCIES;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Wallet wallet) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Wallet.NAME, wallet.mName);
        contentValues.put(Schema.Wallet.ICON, wallet.mIcon);
        contentValues.put(Schema.Wallet.CURRENCY, wallet.mCurrency);
        contentValues.put(Schema.Wallet.START_MONEY, wallet.mStartMoney);
        contentValues.put(Schema.Wallet.COUNT_IN_TOTAL, wallet.mCountInTotal);
        contentValues.put(Schema.Wallet.ARCHIVED, wallet.mArchived);
        contentValues.put(Schema.Wallet.NOTE, wallet.mNote);
        contentValues.put(Schema.Wallet.INDEX, wallet.mIndex);
        contentValues.put(Schema.Wallet.TAG, wallet.mTag);
        contentValues.put(Schema.Wallet.UUID, wallet.mUUID);
        contentValues.put(Schema.Wallet.LAST_EDIT, wallet.mLastEdit);
        contentValues.put(Schema.Wallet.DELETED, wallet.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_WALLETS;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Category.NAME, category.mName);
        contentValues.put(Schema.Category.ICON, category.mIcon);
        contentValues.put(Schema.Category.TYPE, category.mType);
        contentValues.put(Schema.Category.PARENT, category.mParent);
        contentValues.put(Schema.Category.SHOW_REPORT, category.mShowReport);
        contentValues.put(Schema.Category.INDEX, category.mIndex);
        contentValues.put(Schema.Category.TAG, category.mTag);
        contentValues.put(Schema.Category.UUID, category.mUUID);
        contentValues.put(Schema.Category.LAST_EDIT, category.mLastEdit);
        contentValues.put(Schema.Category.DELETED, category.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_CATEGORIES;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Event event) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Event.NAME, event.mName);
        contentValues.put(Schema.Event.ICON, event.mIcon);
        contentValues.put(Schema.Event.NOTE, event.mNote);
        contentValues.put(Schema.Event.START_DATE, event.mStartDate);
        contentValues.put(Schema.Event.END_DATE, event.mEndDate);
        contentValues.put(Schema.Event.TAG, event.mTag);
        contentValues.put(Schema.Event.UUID, event.mUUID);
        contentValues.put(Schema.Event.LAST_EDIT, event.mLastEdit);
        contentValues.put(Schema.Event.DELETED, event.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_EVENTS;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Place place) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Place.NAME, place.mName);
        contentValues.put(Schema.Place.ICON, place.mIcon);
        contentValues.put(Schema.Place.ADDRESS, place.mAddress);
        contentValues.put(Schema.Place.LATITUDE, place.mLatitude);
        contentValues.put(Schema.Place.LONGITUDE, place.mLongitude);
        contentValues.put(Schema.Place.TAG, place.mTag);
        contentValues.put(Schema.Place.UUID, place.mUUID);
        contentValues.put(Schema.Place.LAST_EDIT, place.mLastEdit);
        contentValues.put(Schema.Place.DELETED, place.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_PLACES;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Person person) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Person.NAME, person.mName);
        contentValues.put(Schema.Person.ICON, person.mIcon);
        contentValues.put(Schema.Person.NOTE, person.mNote);
        contentValues.put(Schema.Person.TAG, person.mTag);
        contentValues.put(Schema.Person.UUID, person.mUUID);
        contentValues.put(Schema.Person.LAST_EDIT, person.mLastEdit);
        contentValues.put(Schema.Person.DELETED, person.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_PEOPLE;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, EventPerson eventPerson) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.EventPeople.EVENT, eventPerson.mEvent);
        contentValues.put(Schema.EventPeople.PERSON, eventPerson.mPerson);
        contentValues.put(Schema.EventPeople.UUID, eventPerson.mUUID);
        contentValues.put(Schema.EventPeople.LAST_EDIT, eventPerson.mLastEdit);
        contentValues.put(Schema.EventPeople.DELETED, eventPerson.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_EVENT_PEOPLE;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Debt debt) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Debt.TYPE, debt.mType);
        contentValues.put(Schema.Debt.ICON, debt.mIcon);
        contentValues.put(Schema.Debt.DESCRIPTION, debt.mDescription);
        contentValues.put(Schema.Debt.DATE, debt.mDate);
        contentValues.put(Schema.Debt.EXPIRATION_DATE, debt.mExpirationDate);
        contentValues.put(Schema.Debt.WALLET, debt.mWallet);
        contentValues.put(Schema.Debt.NOTE, debt.mNote);
        contentValues.put(Schema.Debt.PLACE, debt.mPlace);
        contentValues.put(Schema.Debt.MONEY, debt.mMoney);
        contentValues.put(Schema.Debt.ARCHIVED, debt.mArchived);
        contentValues.put(Schema.Debt.TAG, debt.mTag);
        contentValues.put(Schema.Debt.UUID, debt.mUUID);
        contentValues.put(Schema.Debt.LAST_EDIT, debt.mLastEdit);
        contentValues.put(Schema.Debt.DELETED, debt.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_DEBT;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, DebtPerson debtPerson) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.DebtPeople.DEBT, debtPerson.mDebt);
        contentValues.put(Schema.DebtPeople.PERSON, debtPerson.mPerson);
        contentValues.put(Schema.DebtPeople.UUID, debtPerson.mUUID);
        contentValues.put(Schema.DebtPeople.LAST_EDIT, debtPerson.mLastEdit);
        contentValues.put(Schema.DebtPeople.DELETED, debtPerson.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_DEBT_PEOPLE;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Budget budget) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Budget.TYPE, budget.mType);
        contentValues.put(Schema.Budget.CATEGORY, budget.mCategory);
        contentValues.put(Schema.Budget.START_DATE, budget.mStartDate);
        contentValues.put(Schema.Budget.END_DATE, budget.mEndDate);
        contentValues.put(Schema.Budget.MONEY, budget.mMoney);
        contentValues.put(Schema.Budget.CURRENCY, budget.mCurrency);
        contentValues.put(Schema.Budget.TAG, budget.mTag);
        contentValues.put(Schema.Budget.UUID, budget.mUUID);
        contentValues.put(Schema.Budget.LAST_EDIT, budget.mLastEdit);
        contentValues.put(Schema.Budget.DELETED, budget.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_BUDGET;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, BudgetWallet budgetWallet) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.BudgetWallet.BUDGET, budgetWallet.mBudget);
        contentValues.put(Schema.BudgetWallet.WALLET, budgetWallet.mWallet);
        contentValues.put(Schema.BudgetWallet.UUID, budgetWallet.mUUID);
        contentValues.put(Schema.BudgetWallet.LAST_EDIT, budgetWallet.mLastEdit);
        contentValues.put(Schema.BudgetWallet.DELETED, budgetWallet.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_BUDGET_WALLET;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Saving saving) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Saving.DESCRIPTION, saving.mDescription);
        contentValues.put(Schema.Saving.ICON, saving.mIcon);
        contentValues.put(Schema.Saving.START_MONEY, saving.mStartMoney);
        contentValues.put(Schema.Saving.END_MONEY, saving.mEndMoney);
        contentValues.put(Schema.Saving.WALLET, saving.mWallet);
        contentValues.put(Schema.Saving.END_DATE, saving.mEndDate);
        contentValues.put(Schema.Saving.COMPLETE, saving.mComplete);
        contentValues.put(Schema.Saving.NOTE, saving.mNote);
        contentValues.put(Schema.Saving.TAG, saving.mTag);
        contentValues.put(Schema.Saving.UUID, saving.mUUID);
        contentValues.put(Schema.Saving.LAST_EDIT, saving.mLastEdit);
        contentValues.put(Schema.Saving.DELETED, saving.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_SAVING;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, RecurrentTransaction recurrentTransaction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.RecurrentTransaction.MONEY, recurrentTransaction.mMoney);
        contentValues.put(Schema.RecurrentTransaction.DESCRIPTION, recurrentTransaction.mDescription);
        contentValues.put(Schema.RecurrentTransaction.CATEGORY, recurrentTransaction.mCategory);
        contentValues.put(Schema.RecurrentTransaction.DIRECTION, recurrentTransaction.mDirection);
        contentValues.put(Schema.RecurrentTransaction.WALLET, recurrentTransaction.mWallet);
        contentValues.put(Schema.RecurrentTransaction.PLACE, recurrentTransaction.mPlace);
        contentValues.put(Schema.RecurrentTransaction.NOTE, recurrentTransaction.mNote);
        contentValues.put(Schema.RecurrentTransaction.EVENT, recurrentTransaction.mEvent);
        contentValues.put(Schema.RecurrentTransaction.CONFIRMED, recurrentTransaction.mConfirmed);
        contentValues.put(Schema.RecurrentTransaction.COUNT_IN_TOTAL, recurrentTransaction.mCountInTotal);
        contentValues.put(Schema.RecurrentTransaction.START_DATE, recurrentTransaction.mStartDate);
        contentValues.put(Schema.RecurrentTransaction.LAST_OCCURRENCE, recurrentTransaction.mLastOccurrence);
        contentValues.put(Schema.RecurrentTransaction.NEXT_OCCURRENCE, recurrentTransaction.mNextOccurrence);
        contentValues.put(Schema.RecurrentTransaction.RULE, recurrentTransaction.mRule);
        contentValues.put(Schema.RecurrentTransaction.TAG, recurrentTransaction.mTag);
        contentValues.put(Schema.RecurrentTransaction.UUID, recurrentTransaction.mUUID);
        contentValues.put(Schema.RecurrentTransaction.LAST_EDIT, recurrentTransaction.mLastEdit);
        contentValues.put(Schema.RecurrentTransaction.DELETED, recurrentTransaction.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_RECURRENT_TRANSACTION;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, RecurrentTransfer recurrentTransfer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.RecurrentTransfer.DESCRIPTION, recurrentTransfer.mDescription);
        contentValues.put(Schema.RecurrentTransfer.WALLET_FROM, recurrentTransfer.mFromWallet);
        contentValues.put(Schema.RecurrentTransfer.WALLET_TO, recurrentTransfer.mToWallet);
        contentValues.put(Schema.RecurrentTransfer.MONEY_FROM, recurrentTransfer.mFromMoney);
        contentValues.put(Schema.RecurrentTransfer.MONEY_TO, recurrentTransfer.mToMoney);
        contentValues.put(Schema.RecurrentTransfer.MONEY_TAX, recurrentTransfer.mTaxMoney);
        contentValues.put(Schema.RecurrentTransfer.NOTE, recurrentTransfer.mNote);
        contentValues.put(Schema.RecurrentTransfer.EVENT, recurrentTransfer.mEvent);
        contentValues.put(Schema.RecurrentTransfer.PLACE, recurrentTransfer.mPlace);
        contentValues.put(Schema.RecurrentTransfer.CONFIRMED, recurrentTransfer.mConfirmed);
        contentValues.put(Schema.RecurrentTransfer.COUNT_IN_TOTAL, recurrentTransfer.mCountInTotal);
        contentValues.put(Schema.RecurrentTransfer.START_DATE, recurrentTransfer.mStartDate);
        contentValues.put(Schema.RecurrentTransfer.LAST_OCCURRENCE, recurrentTransfer.mLastOccurrence);
        contentValues.put(Schema.RecurrentTransfer.NEXT_OCCURRENCE, recurrentTransfer.mNextOccurrence);
        contentValues.put(Schema.RecurrentTransfer.RULE, recurrentTransfer.mRule);
        contentValues.put(Schema.RecurrentTransfer.TAG, recurrentTransfer.mTag);
        contentValues.put(Schema.RecurrentTransfer.UUID, recurrentTransfer.mUUID);
        contentValues.put(Schema.RecurrentTransfer.LAST_EDIT, recurrentTransfer.mLastEdit);
        contentValues.put(Schema.RecurrentTransfer.DELETED, recurrentTransfer.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_RECURRENT_TRANSFER;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Transaction transaction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Transaction.MONEY, transaction.mMoney);
        contentValues.put(Schema.Transaction.DATE, transaction.mDate);
        contentValues.put(Schema.Transaction.DESCRIPTION, transaction.mDescription);
        contentValues.put(Schema.Transaction.CATEGORY, transaction.mCategory);
        contentValues.put(Schema.Transaction.DIRECTION, transaction.mDirection);
        contentValues.put(Schema.Transaction.TYPE, transaction.mType);
        contentValues.put(Schema.Transaction.WALLET, transaction.mWallet);
        contentValues.put(Schema.Transaction.PLACE, transaction.mPlace);
        contentValues.put(Schema.Transaction.NOTE, transaction.mNote);
        contentValues.put(Schema.Transaction.SAVING, transaction.mSaving);
        contentValues.put(Schema.Transaction.DEBT, transaction.mDebt);
        contentValues.put(Schema.Transaction.EVENT, transaction.mEvent);
        contentValues.put(Schema.Transaction.RECURRENCE, transaction.mRecurrence);
        contentValues.put(Schema.Transaction.CONFIRMED, transaction.mConfirmed);
        contentValues.put(Schema.Transaction.COUNT_IN_TOTAL, transaction.mCountInTotal);
        contentValues.put(Schema.Transaction.TAG, transaction.mTag);
        contentValues.put(Schema.Transaction.UUID, transaction.mUUID);
        contentValues.put(Schema.Transaction.LAST_EDIT, transaction.mLastEdit);
        contentValues.put(Schema.Transaction.DELETED, transaction.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransactionPerson transactionPeople) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransactionPeople.TRANSACTION, transactionPeople.mTransaction);
        contentValues.put(Schema.TransactionPeople.PERSON, transactionPeople.mPerson);
        contentValues.put(Schema.TransactionPeople.UUID, transactionPeople.mUUID);
        contentValues.put(Schema.TransactionPeople.LAST_EDIT, transactionPeople.mLastEdit);
        contentValues.put(Schema.TransactionPeople.DELETED, transactionPeople.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_PEOPLE;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransactionModel transactionModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransactionModel.MONEY, transactionModel.mMoney);
        contentValues.put(Schema.TransactionModel.DESCRIPTION, transactionModel.mDescription);
        contentValues.put(Schema.TransactionModel.CATEGORY, transactionModel.mCategory);
        contentValues.put(Schema.TransactionModel.DIRECTION, transactionModel.mDirection);
        contentValues.put(Schema.TransactionModel.WALLET, transactionModel.mWallet);
        contentValues.put(Schema.TransactionModel.PLACE, transactionModel.mPlace);
        contentValues.put(Schema.TransactionModel.NOTE, transactionModel.mNote);
        contentValues.put(Schema.TransactionModel.EVENT, transactionModel.mEvent);
        contentValues.put(Schema.TransactionModel.CONFIRMED, transactionModel.mConfirmed);
        contentValues.put(Schema.TransactionModel.COUNT_IN_TOTAL, transactionModel.mCountInTotal);
        contentValues.put(Schema.TransactionModel.TAG, transactionModel.mTag);
        contentValues.put(Schema.TransactionModel.UUID, transactionModel.mUUID);
        contentValues.put(Schema.TransactionModel.LAST_EDIT, transactionModel.mLastEdit);
        contentValues.put(Schema.TransactionModel.DELETED, transactionModel.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_MODEL;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Transfer transfers) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Transfer.DESCRIPTION, transfers.mDescription);
        contentValues.put(Schema.Transfer.DATE, transfers.mDate);
        contentValues.put(Schema.Transfer.TRANSACTION_FROM, transfers.mTransactionFrom);
        contentValues.put(Schema.Transfer.TRANSACTION_TO, transfers.mTransactionTo);
        contentValues.put(Schema.Transfer.TRANSACTION_TAX, transfers.mTransactionTax);
        contentValues.put(Schema.Transfer.NOTE, transfers.mNote);
        contentValues.put(Schema.Transfer.PLACE, transfers.mPlace);
        contentValues.put(Schema.Transfer.EVENT, transfers.mEvent);
        contentValues.put(Schema.Transfer.RECURRENCE, transfers.mRecurrence);
        contentValues.put(Schema.Transfer.CONFIRMED, transfers.mConfirmed);
        contentValues.put(Schema.Transfer.COUNT_IN_TOTAL, transfers.mCountInTotal);
        contentValues.put(Schema.Transfer.TAG, transfers.mTag);
        contentValues.put(Schema.Transfer.UUID, transfers.mUUID);
        contentValues.put(Schema.Transfer.LAST_EDIT, transfers.mLastEdit);
        contentValues.put(Schema.Transfer.DELETED, transfers.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSFER;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransferPerson transferPerson) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransferPeople.TRANSFER, transferPerson.mTransfer);
        contentValues.put(Schema.TransferPeople.PERSON, transferPerson.mPerson);
        contentValues.put(Schema.TransferPeople.UUID, transferPerson.mUUID);
        contentValues.put(Schema.TransferPeople.LAST_EDIT, transferPerson.mLastEdit);
        contentValues.put(Schema.TransferPeople.DELETED, transferPerson.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_PEOPLE;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransferModel transferModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransferModel.DESCRIPTION, transferModel.mDescription);
        contentValues.put(Schema.TransferModel.WALLET_FROM, transferModel.mFromWallet);
        contentValues.put(Schema.TransferModel.WALLET_TO, transferModel.mToWallet);
        contentValues.put(Schema.TransferModel.MONEY_FROM, transferModel.mFromMoney);
        contentValues.put(Schema.TransferModel.MONEY_TO, transferModel.mToMoney);
        contentValues.put(Schema.TransferModel.MONEY_TAX, transferModel.mTaxMoney);
        contentValues.put(Schema.TransferModel.NOTE, transferModel.mNote);
        contentValues.put(Schema.TransferModel.EVENT, transferModel.mEvent);
        contentValues.put(Schema.TransferModel.PLACE, transferModel.mPlace);
        contentValues.put(Schema.TransferModel.CONFIRMED, transferModel.mConfirmed);
        contentValues.put(Schema.TransferModel.COUNT_IN_TOTAL, transferModel.mCountInTotal);
        contentValues.put(Schema.TransferModel.TAG, transferModel.mTag);
        contentValues.put(Schema.TransferModel.UUID, transferModel.mUUID);
        contentValues.put(Schema.TransferModel.LAST_EDIT, transferModel.mLastEdit);
        contentValues.put(Schema.TransferModel.DELETED, transferModel.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_MODEL;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, Attachment attachment) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.Attachment.FILE, attachment.mFile);
        contentValues.put(Schema.Attachment.NAME, attachment.mName);
        contentValues.put(Schema.Attachment.TYPE, attachment.mType);
        contentValues.put(Schema.Attachment.SIZE, attachment.mSize);
        contentValues.put(Schema.Attachment.TAG, attachment.mTag);
        contentValues.put(Schema.Attachment.UUID, attachment.mUUID);
        contentValues.put(Schema.Attachment.LAST_EDIT, attachment.mLastEdit);
        contentValues.put(Schema.Attachment.DELETED, attachment.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_ATTACHMENT;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransactionAttachment transactionAttachment) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransactionAttachment.TRANSACTION, transactionAttachment.mTransaction);
        contentValues.put(Schema.TransactionAttachment.ATTACHMENT, transactionAttachment.mAttachment);
        contentValues.put(Schema.TransactionAttachment.UUID, transactionAttachment.mUUID);
        contentValues.put(Schema.TransactionAttachment.LAST_EDIT, transactionAttachment.mLastEdit);
        contentValues.put(Schema.TransactionAttachment.DELETED, transactionAttachment.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSACTION_ATTACHMENT;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }

    public static long insert(ContentResolver contentResolver, TransferAttachment transferAttachment) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.TransferAttachment.TRANSFER, transferAttachment.mTransfer);
        contentValues.put(Schema.TransferAttachment.ATTACHMENT, transferAttachment.mAttachment);
        contentValues.put(Schema.TransferAttachment.UUID, transferAttachment.mUUID);
        contentValues.put(Schema.TransferAttachment.LAST_EDIT, transferAttachment.mLastEdit);
        contentValues.put(Schema.TransferAttachment.DELETED, transferAttachment.mDeleted);
        Uri uri = SyncContentProvider.CONTENT_TRANSFER_ATTACHMENT;
        uri = contentResolver.insert(uri, contentValues);
        return ContentUris.parseId(uri);
    }
}