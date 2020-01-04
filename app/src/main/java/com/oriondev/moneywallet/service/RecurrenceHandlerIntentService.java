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

package com.oriondev.moneywallet.service;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.utils.DateUtils;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

import java.util.Date;

/**
 * Created by andrea on 11/11/18.
 */
public class RecurrenceHandlerIntentService extends JobIntentService {

    private static final int JOB_ID = 3564;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, RecurrenceHandlerIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        addMissingRecurrentTransactionOccurrences();
        addMissingRecurrentTransferOccurrences();
        RecurrenceBroadcastReceiver.scheduleRecurrenceTask(this);
    }

    private void addMissingRecurrentTransactionOccurrences() {
        Uri uri = DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS;
        String selection = Contract.RecurrentTransaction.NEXT_OCCURRENCE + " IS NOT NULL AND DATE(" + Contract.RecurrentTransaction.NEXT_OCCURRENCE + ") <= DATE('now', 'localtime')";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // get basic information about the recurrence entity
                long transactionId = cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.ID));
                String firstOccurrenceDateString = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.NEXT_OCCURRENCE));
                Date firstOccurrenceDate = DateUtils.getDateFromSQLDateString(firstOccurrenceDateString);
                DateTime currentDateTime = DateUtils.getFixedDateTime(new Date());
                DateTime startDateTime = DateUtils.getFixedDateTime(firstOccurrenceDate);
                DateTime lastOccurrence = DateUtils.getFixedDateTime(firstOccurrenceDate);
                DateTime nextOccurrence = null;
                RecurrenceRule recurrenceRule;
                try {
                    recurrenceRule = new RecurrenceRule(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.RULE)));
                    RecurrenceRuleIterator iterator = recurrenceRule.iterator(startDateTime);
                    while (iterator.hasNext()) {
                        DateTime nextInstance = iterator.nextDateTime();
                        if (!nextInstance.after(currentDateTime)) {
                            Date transactionDate = DateUtils.getFixedDate(nextInstance);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Transaction.MONEY, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.MONEY)));
                            contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(transactionDate));
                            contentValues.put(Contract.Transaction.DESCRIPTION, cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.DESCRIPTION)));
                            contentValues.put(Contract.Transaction.CATEGORY_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.CATEGORY_ID)));
                            contentValues.put(Contract.Transaction.DIRECTION, cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.DIRECTION)));
                            contentValues.put(Contract.Transaction.TYPE, Contract.TransactionType.STANDARD);
                            contentValues.put(Contract.Transaction.WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.WALLET_ID)));
                            contentValues.put(Contract.Transaction.NOTE, cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.NOTE)));
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ID))) {
                                contentValues.put(Contract.Transaction.PLACE_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ID)));
                            }
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_ID))) {
                                contentValues.put(Contract.Transaction.EVENT_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_ID)));
                            }
                            contentValues.put(Contract.Transaction.RECURRENCE_ID, transactionId);
                            contentValues.put(Contract.Transaction.CONFIRMED, cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.CONFIRMED)) == 1);
                            contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.COUNT_IN_TOTAL)) == 1);
                            getContentResolver().insert(DataContentProvider.CONTENT_TRANSACTIONS, contentValues);
                            lastOccurrence = nextInstance;
                        } else {
                            nextOccurrence = nextInstance;
                            break;
                        }
                    }
                } catch (InvalidRecurrenceRuleException ignore) {
                    // do nothing, next occurrence is still null so this recurrence will
                    // not be processed again in future
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.RecurrentTransaction.LAST_OCCURRENCE, DateUtils.getSQLDateString(DateUtils.getFixedDate(lastOccurrence)));
                contentValues.put(Contract.RecurrentTransaction.NEXT_OCCURRENCE, nextOccurrence != null ? DateUtils.getSQLDateString(DateUtils.getFixedDate(nextOccurrence)) : null);
                Uri contentUri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS, transactionId);
                getContentResolver().update(contentUri, contentValues, null, null);
            }
            cursor.close();
        }
    }

    private void addMissingRecurrentTransferOccurrences() {
        Uri uri = DataContentProvider.CONTENT_RECURRENT_TRANSFERS;
        String selection = Contract.RecurrentTransfer.NEXT_OCCURRENCE + " IS NOT NULL AND DATE(" + Contract.RecurrentTransfer.NEXT_OCCURRENCE + ") <= DATE('now', 'localtime')";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // get basic information about the recurrence entity
                long recurrenceId = cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.ID));
                String firstOccurrenceDateString = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.NEXT_OCCURRENCE));
                Date firstOccurrenceDate = DateUtils.getDateFromSQLDateString(firstOccurrenceDateString);
                DateTime currentDateTime = DateUtils.getFixedDateTime(new Date());
                DateTime startDateTime = DateUtils.getFixedDateTime(firstOccurrenceDate);
                DateTime lastOccurrence = DateUtils.getFixedDateTime(firstOccurrenceDate);
                DateTime nextOccurrence = null;
                RecurrenceRule recurrenceRule;
                try {
                    recurrenceRule = new RecurrenceRule(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.RULE)));
                    RecurrenceRuleIterator iterator = recurrenceRule.iterator(startDateTime);
                    while (iterator.hasNext()) {
                        DateTime nextInstance = iterator.nextDateTime();
                        if (!nextInstance.after(currentDateTime)) {
                            Date transferDate = DateUtils.getFixedDate(nextInstance);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Transfer.DESCRIPTION, cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.DESCRIPTION)));
                            contentValues.put(Contract.Transfer.DATE, DateUtils.getSQLDateTimeString(transferDate));
                            contentValues.put(Contract.Transfer.TRANSACTION_FROM_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_FROM_ID)));
                            contentValues.put(Contract.Transfer.TRANSACTION_TO_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_TO_ID)));
                            contentValues.put(Contract.Transfer.TRANSACTION_TAX_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_FROM_ID)));
                            contentValues.put(Contract.Transfer.TRANSACTION_FROM_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_FROM)));
                            contentValues.put(Contract.Transfer.TRANSACTION_TO_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_TO)));
                            contentValues.put(Contract.Transfer.TRANSACTION_TAX_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_TAX)));
                            contentValues.put(Contract.Transfer.NOTE, cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.NOTE)));
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransfer.PLACE_ID))) {
                                contentValues.put(Contract.Transfer.PLACE_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.PLACE_ID)));
                            }
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransfer.EVENT_ID))) {
                                contentValues.put(Contract.Transfer.EVENT_ID, cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.EVENT_ID)));
                            }
                            contentValues.put(Contract.Transfer.RECURRENCE_ID, recurrenceId);
                            contentValues.put(Contract.Transfer.CONFIRMED, cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransfer.CONFIRMED)) == 1);
                            contentValues.put(Contract.Transfer.COUNT_IN_TOTAL, cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransfer.COUNT_IN_TOTAL)) == 1);
                            getContentResolver().insert(DataContentProvider.CONTENT_TRANSFERS, contentValues);
                            lastOccurrence = nextInstance;
                        } else {
                            nextOccurrence = nextInstance;
                            break;
                        }
                    }
                } catch (InvalidRecurrenceRuleException ignore) {
                    // do nothing, next occurrence is still null so this recurrence will
                    // not be processed again in future
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.RecurrentTransfer.LAST_OCCURRENCE, DateUtils.getSQLDateString(DateUtils.getFixedDate(lastOccurrence)));
                contentValues.put(Contract.RecurrentTransfer.NEXT_OCCURRENCE, nextOccurrence != null ? DateUtils.getSQLDateString(DateUtils.getFixedDate(nextOccurrence)) : null);
                Uri contentUri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSFERS, recurrenceId);
                getContentResolver().update(contentUri, contentValues, null, null);
            }
            cursor.close();
        }
    }
}