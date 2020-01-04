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

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextUtils;

import androidx.test.platform.app.InstrumentationRegistry;

import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.utils.DateUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by andrea on 28/08/18.
 */
@LargeTest
public class SQLDatabaseTest {

    private Context mContext;
    private SQLDatabase mDatabase;

    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context baseContext = instrumentation.getTargetContext();
        mContext = new RenamingDelegatingContext(baseContext, "test.");
        // load existing databases and files if found
        if (mContext instanceof RenamingDelegatingContext) {
            ((RenamingDelegatingContext) mContext).makeExistingFilesAndDbsAccessible();
        }
        // remove existing database (if any) before starting the test
        mContext.deleteDatabase(SQLDatabase.DATABASE_NAME);
        // create a new database for testing purposes
        mDatabase = new SQLDatabase(mContext);
        mDatabase.setDeletedObjectCacheEnabled(false);
    }

    @After
    public void tearDown() {
        mDatabase.close();
        mContext.deleteDatabase(SQLDatabase.DATABASE_NAME);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// INTERNAL METHODS FOR TESTING ///////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    private String getObjectIds(Long[] ids) {
        if (ids != null && ids.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < ids.length; i++) {
                if (i != 0) {
                    builder.append(",");
                }
                builder.append(String.format(Locale.ENGLISH, "<%d>", ids[i]));
            }
            return builder.toString();
        }
        return null;
    }

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

    private int checkCursorMinSize(Cursor cursor, int minSize) {
        assertNotNull(cursor);
        int cursorSize = cursor.getCount();
        assertEquals(true, cursorSize >= minSize);
        cursor.close();
        return cursorSize;
    }

    private void checkCursorSize(Cursor cursor, int expectedSize) {
        assertNotNull(cursor);
        if (cursor.getCount() != expectedSize) {
            System.out.println("checkCursorSize is going to fail! printing cursor:");
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append(" | ");
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                rowBuilder.append(cursor.getColumnName(i));
                rowBuilder.append(" | ");
            }
            System.out.println(rowBuilder);
            while (cursor.moveToNext()) {
                rowBuilder = new StringBuilder();
                rowBuilder.append(" | ");
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    rowBuilder.append(cursor.getString(i));
                    rowBuilder.append(" | ");
                }
                System.out.println(rowBuilder);
            }
        }
        assertEquals(expectedSize, cursor.getCount());
        cursor.close();
    }

    private void checkNullable(Cursor cursor, Object value, int index) {
        assertEquals(value == null, cursor.isNull(index));
        if (!cursor.isNull(index)) {
            if (value instanceof Long) {
                assertEquals((long) value, cursor.getLong(index));
            } else if (value instanceof Integer) {
                assertEquals((int) value, cursor.getInt(index));
            } else if (value instanceof String) {
                assertEquals((String) value, cursor.getString(index));
            } else if (value instanceof Double) {
                assertEquals(value, cursor.getDouble(index));
            }
        }
    }

    private void checkWalletId(long id, String name, String icon, String currency, String note,
                               boolean countInTotal, long startMoney, long totalMoney, boolean archived,
                               String tag) {
        Cursor cursor = mDatabase.getWallet(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(id, cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID)));
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON)));
        assertEquals(currency, cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Wallet.NOTE)));
        assertEquals(countInTotal, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Wallet.COUNT_IN_TOTAL)));
        assertEquals(startMoney, cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY)));
        assertEquals(archived, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Wallet.ARCHIVED)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Wallet.TAG)));
        Money money = Money.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY)));
        assertEquals(totalMoney, money.getMoney(currency));
        cursor.close();
    }

    private long insertWallet(String name, String icon, String currency, String note,
                             boolean countInTotal, long startMoney, boolean archived,
                             String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Wallet.NAME, name);
        contentValues.put(Contract.Wallet.ICON, icon);
        contentValues.put(Contract.Wallet.CURRENCY, currency);
        contentValues.put(Contract.Wallet.NOTE, note);
        contentValues.put(Contract.Wallet.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Wallet.START_MONEY, startMoney);
        contentValues.put(Contract.Wallet.ARCHIVED, archived);
        contentValues.put(Contract.Wallet.TAG, tag);
        return mDatabase.insertWallet(contentValues);
    }

    private int updateWallet(long walletId, String name, String icon, String currency, String note,
                             boolean countInTotal, long startMoney, boolean archived,
                             String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Wallet.NAME, name);
        contentValues.put(Contract.Wallet.ICON, icon);
        contentValues.put(Contract.Wallet.CURRENCY, currency);
        contentValues.put(Contract.Wallet.NOTE, note);
        contentValues.put(Contract.Wallet.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Wallet.START_MONEY, startMoney);
        contentValues.put(Contract.Wallet.ARCHIVED, archived);
        contentValues.put(Contract.Wallet.TAG, tag);
        return mDatabase.updateWallet(walletId, contentValues);
    }

    private void checkCategoryId(long categoryId, String name, String icon, int type, Long parentId,
                                    boolean showReport, String tag) {
        Cursor cursor = mDatabase.getCategory(categoryId, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Category.ICON)));
        assertEquals(type, cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)));
        checkNullable(cursor, parentId, cursor.getColumnIndex(Contract.Category.PARENT));
        assertEquals(showReport, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Category.SHOW_REPORT)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Category.TAG)));
        cursor.close();
    }

    private long insertCategory(String name, String icon, int type, Long parentId,
                               boolean showReport, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Category.NAME, name);
        contentValues.put(Contract.Category.ICON, icon);
        contentValues.put(Contract.Category.TYPE, type);
        contentValues.put(Contract.Category.PARENT, parentId);
        contentValues.put(Contract.Category.SHOW_REPORT, showReport);
        contentValues.put(Contract.Category.TAG, tag);
        return mDatabase.insertCategory(contentValues);
    }

    private long getSystemCategory(String tag) {
        String[] projection = new String[] {Contract.Category.ID};
        String selection = Contract.Category.TAG + " = ?";
        String[] selectionArgs = new String[] {tag};
        Cursor cursor = mDatabase.getCategories(projection, selection, selectionArgs, null);
        assertNotNull(cursor);
        assertEquals(true, cursor.moveToFirst());
        long categoryId = cursor.getLong(cursor.getColumnIndex(Contract.Category.ID));
        cursor.close();
        return categoryId;
    }

    private int updateCategory(long categoryId, String name, String icon, int type, Long parentId,
                               boolean showReport, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Category.NAME, name);
        contentValues.put(Contract.Category.ICON, icon);
        contentValues.put(Contract.Category.TYPE, type);
        contentValues.put(Contract.Category.PARENT, parentId);
        contentValues.put(Contract.Category.SHOW_REPORT, showReport);
        contentValues.put(Contract.Category.TAG, tag);
        return mDatabase.updateCategory(categoryId, contentValues);
    }

    private void checkEventId(long id, String name, String icon, Date startDate, Date endDate,
                              String note, String tag) {
        Cursor cursor = mDatabase.getEvent(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Event.NAME)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Event.ICON)));
        assertEquals(DateUtils.getSQLDateString(startDate), cursor.getString(cursor.getColumnIndex(Contract.Event.START_DATE)));
        assertEquals(DateUtils.getSQLDateString(endDate), cursor.getString(cursor.getColumnIndex(Contract.Event.END_DATE)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Event.NOTE)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Event.TAG)));
        cursor.close();
    }

    private long insertEvent(String name, String icon, Date startDate, Date endDate,
                             String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Event.NAME, name);
        contentValues.put(Contract.Event.ICON, icon);
        contentValues.put(Contract.Event.START_DATE, DateUtils.getSQLDateString(startDate));
        contentValues.put(Contract.Event.END_DATE, DateUtils.getSQLDateString(endDate));
        contentValues.put(Contract.Event.NOTE, note);
        contentValues.put(Contract.Event.TAG, tag);
        return mDatabase.insertEvent(contentValues);
    }

    private int updateEvent(long id, String name, String icon, Date startDate, Date endDate,
                            String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Event.NAME, name);
        contentValues.put(Contract.Event.ICON, icon);
        contentValues.put(Contract.Event.START_DATE, DateUtils.getSQLDateString(startDate));
        contentValues.put(Contract.Event.END_DATE, DateUtils.getSQLDateString(endDate));
        contentValues.put(Contract.Event.NOTE, note);
        contentValues.put(Contract.Event.TAG, tag);
        return mDatabase.updateEvent(id, contentValues);
    }

    private void checkPlaceId(long id, String name, String icon, String address, Double latitude,
                              Double longitude, String tag) {
        Cursor cursor = mDatabase.getPlace(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Place.NAME)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Place.ICON)));
        assertEquals(address, cursor.getString(cursor.getColumnIndex(Contract.Place.ADDRESS)));
        checkNullable(cursor, latitude, cursor.getColumnIndex(Contract.Place.LATITUDE));
        checkNullable(cursor, longitude, cursor.getColumnIndex(Contract.Place.LONGITUDE));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Place.TAG)));
        cursor.close();
    }

    private long insertPlace(String name, String icon, String address, Double latitude,
                             Double longitude, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Place.NAME, name);
        contentValues.put(Contract.Place.ICON, icon);
        contentValues.put(Contract.Place.ADDRESS, address);
        contentValues.put(Contract.Place.LATITUDE, latitude);
        contentValues.put(Contract.Place.LONGITUDE, longitude);
        contentValues.put(Contract.Place.TAG, tag);
        return mDatabase.insertPlace(contentValues);
    }

    private int updatePlace(long id, String name, String icon, String address, Double latitude,
                            Double longitude, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Place.NAME, name);
        contentValues.put(Contract.Place.ICON, icon);
        contentValues.put(Contract.Place.ADDRESS, address);
        contentValues.put(Contract.Place.LATITUDE, latitude);
        contentValues.put(Contract.Place.LONGITUDE, longitude);
        contentValues.put(Contract.Place.TAG, tag);
        return mDatabase.updatePlace(id, contentValues);
    }

    private void checkPersonId(long id, String name, String icon, String note, String tag) {
        Cursor cursor = mDatabase.getPerson(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Person.NAME)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Person.ICON)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Person.NOTE)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Person.TAG)));
        cursor.close();
    }

    private long insertPerson(String name, String icon, String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Person.NAME, name);
        contentValues.put(Contract.Person.ICON, icon);
        contentValues.put(Contract.Person.NOTE, note);
        contentValues.put(Contract.Person.TAG, tag);
        return mDatabase.insertPerson(contentValues);
    }

    private int updatePerson(long id, String name, String icon, String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Person.NAME, name);
        contentValues.put(Contract.Person.ICON, icon);
        contentValues.put(Contract.Person.NOTE, note);
        contentValues.put(Contract.Person.TAG, tag);
        return mDatabase.updatePerson(id, contentValues);
    }

    private void checkAttachmentId(long id, String file, String name, String type,
                                   long size, String tag) {
        Cursor cursor = mDatabase.getAttachment(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(file, cursor.getString(cursor.getColumnIndex(Contract.Attachment.FILE)));
        assertEquals(name, cursor.getString(cursor.getColumnIndex(Contract.Attachment.NAME)));
        assertEquals(type, cursor.getString(cursor.getColumnIndex(Contract.Attachment.TYPE)));
        assertEquals(size, cursor.getLong(cursor.getColumnIndex(Contract.Attachment.SIZE)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Attachment.TAG)));
        cursor.close();
    }

    private long insertAttachment(String file, String name, String type, long size, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Attachment.FILE, file);
        contentValues.put(Contract.Attachment.NAME, name);
        contentValues.put(Contract.Attachment.TYPE, type);
        contentValues.put(Contract.Attachment.SIZE, size);
        contentValues.put(Contract.Attachment.TAG, tag);
        return mDatabase.insertAttachment(contentValues);
    }

    private int updateAttachment(long id, String file, String name, String type,
                                 long size, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Attachment.FILE, file);
        contentValues.put(Contract.Attachment.NAME, name);
        contentValues.put(Contract.Attachment.TYPE, type);
        contentValues.put(Contract.Attachment.SIZE, size);
        contentValues.put(Contract.Attachment.TAG, tag);
        return mDatabase.updateAttachment(id, contentValues);
    }

    private void checkDebtId(long id, int type, String icon, String description, Date date, Date exp,
                             long walletId, String note, Long placeId, long money, boolean archived,
                             Long[] peopleIds, String tag) {
        Cursor cursor = mDatabase.getDebt(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(type, cursor.getInt(cursor.getColumnIndex(Contract.Debt.TYPE)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Debt.ICON)));
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.Debt.DESCRIPTION)));
        assertEquals(DateUtils.getSQLDateString(date), cursor.getString(cursor.getColumnIndex(Contract.Debt.DATE)));
        checkNullable(cursor, exp != null ? DateUtils.getSQLDateString(exp) : null, cursor.getColumnIndex(Contract.Debt.EXPIRATION_DATE));
        assertEquals(walletId, cursor.getLong(cursor.getColumnIndex(Contract.Debt.WALLET_ID)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Debt.NOTE)));
        checkNullable(cursor, placeId, cursor.getColumnIndex(Contract.Debt.PLACE_ID));
        assertEquals(money, cursor.getLong(cursor.getColumnIndex(Contract.Debt.MONEY)));
        assertEquals(archived, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Debt.ARCHIVED)));
        assertEquals(getObjectIds(peopleIds), cursor.getString(cursor.getColumnIndex(Contract.Debt.PEOPLE_IDS)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Debt.TAG)));
        cursor.close();
    }

    private long insertDebt(int type, String icon, String description, Date date, Date exp,
                            long walletId, String note, Long placeId, long money, boolean archived,
                            Long[] peopleIds, String tag, boolean addMasterTransaction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Debt.TYPE, type);
        contentValues.put(Contract.Debt.ICON, icon);
        contentValues.put(Contract.Debt.DESCRIPTION, description);
        contentValues.put(Contract.Debt.DATE, DateUtils.getSQLDateString(date));
        contentValues.put(Contract.Debt.EXPIRATION_DATE, exp != null ? DateUtils.getSQLDateString(exp) : null);
        contentValues.put(Contract.Debt.WALLET_ID, walletId);
        contentValues.put(Contract.Debt.NOTE, note);
        contentValues.put(Contract.Debt.PLACE_ID, placeId);
        contentValues.put(Contract.Debt.MONEY, money);
        contentValues.put(Contract.Debt.ARCHIVED, archived);
        contentValues.put(Contract.Debt.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Debt.TAG, tag);
        contentValues.put(Contract.Debt.INSERT_MASTER_TRANSACTION, addMasterTransaction);
        return mDatabase.insertDebt(contentValues);
    }

    private int updateDebt(long id, int type, String icon, String description, Date date, Date exp,
                           long walletId, String note, Long placeId, long money, boolean archived,
                           Long[] peopleIds, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Debt.TYPE, type);
        contentValues.put(Contract.Debt.ICON, icon);
        contentValues.put(Contract.Debt.DESCRIPTION, description);
        contentValues.put(Contract.Debt.DATE, DateUtils.getSQLDateString(date));
        contentValues.put(Contract.Debt.EXPIRATION_DATE, exp != null ? DateUtils.getSQLDateString(exp) : null);
        contentValues.put(Contract.Debt.WALLET_ID, walletId);
        contentValues.put(Contract.Debt.NOTE, note);
        contentValues.put(Contract.Debt.PLACE_ID, placeId);
        contentValues.put(Contract.Debt.MONEY, money);
        contentValues.put(Contract.Debt.ARCHIVED, archived);
        contentValues.put(Contract.Debt.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Debt.TAG, tag);
        return mDatabase.updateDebt(id, contentValues);
    }

    private void checkBudgetId(long id, int type, Long categoryId, Date startDate, Date endDate,
                               long money, String currency, Long[] walletIds, String tag) {
        Cursor cursor = mDatabase.getBudget(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(type, cursor.getInt(cursor.getColumnIndex(Contract.Budget.TYPE)));
        checkNullable(cursor, categoryId, cursor.getColumnIndex(Contract.Budget.CATEGORY_ID));
        assertEquals(DateUtils.getSQLDateString(startDate), cursor.getString(cursor.getColumnIndex(Contract.Budget.START_DATE)));
        assertEquals(DateUtils.getSQLDateString(endDate), cursor.getString(cursor.getColumnIndex(Contract.Budget.END_DATE)));
        assertEquals(money, cursor.getLong(cursor.getColumnIndex(Contract.Budget.MONEY)));
        assertEquals(currency, cursor.getString(cursor.getColumnIndex(Contract.Budget.CURRENCY)));
        assertEquals(getObjectIds(walletIds), cursor.getString(cursor.getColumnIndex(Contract.Budget.WALLET_IDS)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Budget.TAG)));
        cursor.close();
    }

    private long insertBudget(int type, Long categoryId, Date startDate, Date endDate, long money,
                              String currency, Long[] walletIds, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Budget.TYPE, type);
        contentValues.put(Contract.Budget.CATEGORY_ID, categoryId);
        contentValues.put(Contract.Budget.START_DATE, DateUtils.getSQLDateString(startDate));
        contentValues.put(Contract.Budget.END_DATE, DateUtils.getSQLDateString(endDate));
        contentValues.put(Contract.Budget.MONEY, money);
        contentValues.put(Contract.Budget.CURRENCY, currency);
        System.out.println("[pre insert] " + Arrays.toString(walletIds));
        System.out.println("[post insert] " + getObjectIds(walletIds));
        contentValues.put(Contract.Budget.WALLET_IDS, getObjectIds(walletIds));
        contentValues.put(Contract.Budget.TAG, tag);
        return mDatabase.insertBudget(contentValues);
    }

    private int updateBudget(long id, int type, Long categoryId, Date startDate, Date endDate,
                             long money, String currency, Long[] walletIds, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Budget.TYPE, type);
        contentValues.put(Contract.Budget.CATEGORY_ID, categoryId);
        contentValues.put(Contract.Budget.START_DATE, DateUtils.getSQLDateString(startDate));
        contentValues.put(Contract.Budget.END_DATE, DateUtils.getSQLDateString(endDate));
        contentValues.put(Contract.Budget.MONEY, money);
        contentValues.put(Contract.Budget.CURRENCY, currency);
        contentValues.put(Contract.Budget.WALLET_IDS, getObjectIds(walletIds));
        contentValues.put(Contract.Budget.TAG, tag);
        return mDatabase.updateBudget(id, contentValues);
    }

    private void checkSavingId(long id, String description, String icon, long startMoney,
                               long endMoney, long walletId, Date exp, boolean completed,
                               String note, String tag) {
        Cursor cursor = mDatabase.getSaving(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.Saving.DESCRIPTION)));
        assertEquals(icon, cursor.getString(cursor.getColumnIndex(Contract.Saving.ICON)));
        assertEquals(startMoney, cursor.getLong(cursor.getColumnIndex(Contract.Saving.START_MONEY)));
        assertEquals(endMoney, cursor.getLong(cursor.getColumnIndex(Contract.Saving.END_MONEY)));
        assertEquals(walletId, cursor.getLong(cursor.getColumnIndex(Contract.Saving.WALLET_ID)));
        checkNullable(cursor, exp != null ? DateUtils.getSQLDateString(exp) : null, cursor.getColumnIndex(Contract.Saving.END_DATE));
        assertEquals(completed, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Saving.COMPLETE)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Saving.NOTE)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Saving.TAG)));
        cursor.close();
    }

    private long insertSaving(String description, String icon, long startMoney, long endMoney,
                              long walletId, Date exp, boolean completed, String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Saving.DESCRIPTION, description);
        contentValues.put(Contract.Saving.ICON, icon);
        contentValues.put(Contract.Saving.START_MONEY, startMoney);
        contentValues.put(Contract.Saving.END_MONEY, endMoney);
        contentValues.put(Contract.Saving.WALLET_ID, walletId);
        contentValues.put(Contract.Saving.END_DATE, exp != null ? DateUtils.getSQLDateString(exp) : null);
        contentValues.put(Contract.Saving.COMPLETE, completed);
        contentValues.put(Contract.Saving.NOTE, note);
        contentValues.put(Contract.Saving.TAG, tag);
        return mDatabase.insertSaving(contentValues);
    }

    private int updateSaving(long id, String description, String icon, long startMoney,
                             long endMoney, long walletId, Date exp, boolean completed,
                             String note, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Saving.DESCRIPTION, description);
        contentValues.put(Contract.Saving.ICON, icon);
        contentValues.put(Contract.Saving.START_MONEY, startMoney);
        contentValues.put(Contract.Saving.END_MONEY, endMoney);
        contentValues.put(Contract.Saving.WALLET_ID, walletId);
        contentValues.put(Contract.Saving.END_DATE, exp != null ? DateUtils.getSQLDateString(exp) : null);
        contentValues.put(Contract.Saving.COMPLETE, completed);
        contentValues.put(Contract.Saving.NOTE, note);
        contentValues.put(Contract.Saving.TAG, tag);
        return mDatabase.updateSaving(id, contentValues);
    }

    private void checkTransactionId(long id, long money, Date datetime, String description, long categoryId,
                                    int direction, int type, long walletId, Long placeId, String note,
                                    Long eventId, Long savingId, Long debtId, boolean confirmed,
                                    boolean countInTotal, Long[] peopleIds, String tag) {
        Cursor cursor = mDatabase.getTransaction(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(money, cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY)));
        assertEquals(DateUtils.getSQLDateTimeString(datetime), cursor.getString(cursor.getColumnIndex(Contract.Transaction.DATE)));
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.Transaction.DESCRIPTION)));
        assertEquals(categoryId, cursor.getLong(cursor.getColumnIndex(Contract.Transaction.CATEGORY_ID)));
        assertEquals(direction, cursor.getInt(cursor.getColumnIndex(Contract.Transaction.DIRECTION)));
        assertEquals(type, cursor.getInt(cursor.getColumnIndex(Contract.Transaction.TYPE)));
        assertEquals(walletId, cursor.getLong(cursor.getColumnIndex(Contract.Transaction.WALLET_ID)));
        checkNullable(cursor, placeId, cursor.getColumnIndex(Contract.Transaction.PLACE_ID));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Transaction.NOTE)));
        checkNullable(cursor, eventId, cursor.getColumnIndex(Contract.Transaction.EVENT_ID));
        checkNullable(cursor, savingId, cursor.getColumnIndex(Contract.Transaction.SAVING_ID));
        checkNullable(cursor, debtId, cursor.getColumnIndex(Contract.Transaction.DEBT_ID));
        assertEquals(confirmed, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Transaction.CONFIRMED)));
        assertEquals(countInTotal, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Transaction.COUNT_IN_TOTAL)));
        assertEquals(getObjectIds(peopleIds), cursor.getString(cursor.getColumnIndex(Contract.Transaction.PEOPLE_IDS)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Transaction.TAG)));
        cursor.close();
    }

    private long insertTransaction(long money, Date datetime, String description, long categoryId,
                                  int direction, int type, long walletId, Long placeId, String note,
                                  Long eventId, Long savingId, Long debtId, boolean confirmed,
                                  boolean countInTotal, Long[] peopleIds, Long[] attachmentIds,
                                  String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Transaction.MONEY, money);
        contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(datetime));
        contentValues.put(Contract.Transaction.DESCRIPTION, description);
        contentValues.put(Contract.Transaction.CATEGORY_ID, categoryId);
        contentValues.put(Contract.Transaction.DIRECTION, direction);
        contentValues.put(Contract.Transaction.TYPE, type);
        contentValues.put(Contract.Transaction.WALLET_ID, walletId);
        contentValues.put(Contract.Transaction.PLACE_ID, placeId);
        contentValues.put(Contract.Transaction.NOTE, note);
        contentValues.put(Contract.Transaction.EVENT_ID, eventId);
        contentValues.put(Contract.Transaction.SAVING_ID, savingId);
        contentValues.put(Contract.Transaction.DEBT_ID, debtId);
        contentValues.put(Contract.Transaction.CONFIRMED, confirmed);
        contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Transaction.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Transaction.ATTACHMENT_IDS, getObjectIds(attachmentIds));
        contentValues.put(Contract.Transaction.TAG, tag);
        return mDatabase.insertTransaction(contentValues);
    }

    private int updateTransaction(long id, long money, Date datetime, String description, long categoryId,
                                  int direction, int type, long walletId, Long placeId, String note,
                                  Long eventId, Long savingId, Long debtId, boolean confirmed,
                                  boolean countInTotal, Long[] peopleIds, Long[] attachmentIds,
                                  String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Transaction.MONEY, money);
        contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(datetime));
        contentValues.put(Contract.Transaction.DESCRIPTION, description);
        contentValues.put(Contract.Transaction.CATEGORY_ID, categoryId);
        contentValues.put(Contract.Transaction.DIRECTION, direction);
        contentValues.put(Contract.Transaction.TYPE, type);
        contentValues.put(Contract.Transaction.WALLET_ID, walletId);
        contentValues.put(Contract.Transaction.PLACE_ID, placeId);
        contentValues.put(Contract.Transaction.NOTE, note);
        contentValues.put(Contract.Transaction.EVENT_ID, eventId);
        contentValues.put(Contract.Transaction.SAVING_ID, savingId);
        contentValues.put(Contract.Transaction.DEBT_ID, debtId);
        contentValues.put(Contract.Transaction.CONFIRMED, confirmed);
        contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Transaction.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Transaction.ATTACHMENT_IDS, getObjectIds(attachmentIds));
        contentValues.put(Contract.Transaction.TAG, tag);
        return mDatabase.updateTransaction(id, contentValues);
    }

    private void checkTransferId(long id, String description, Date datetime, long walletFromId,
                                 long walletToId, Long walletTaxId, long moneyFrom,
                                 long moneyTo, long moneyTax, String note, Long placeId,
                                 Long eventId, boolean confirmed, boolean countInTotal,
                                 Long[] peopleIds, String tag) {
        Cursor cursor = mDatabase.getTransfer(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.Transfer.DESCRIPTION)));
        assertEquals(DateUtils.getSQLDateTimeString(datetime), cursor.getString(cursor.getColumnIndex(Contract.Transfer.DATE)));
        assertEquals(walletFromId, cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_ID)));
        assertEquals(walletToId, cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_ID)));
        checkNullable(cursor, walletTaxId, cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TAX_WALLET_ID));
        assertEquals(moneyFrom, cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_MONEY)));
        assertEquals(moneyTo, cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_MONEY)));
        assertEquals(moneyTax, cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TAX_MONEY)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.Transfer.NOTE)));
        checkNullable(cursor, placeId, cursor.getColumnIndex(Contract.Transfer.PLACE_ID));
        checkNullable(cursor, eventId, cursor.getColumnIndex(Contract.Transfer.EVENT_ID));
        assertEquals(confirmed, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Transfer.CONFIRMED)));
        assertEquals(countInTotal, 1 == cursor.getInt(cursor.getColumnIndex(Contract.Transfer.COUNT_IN_TOTAL)));
        assertEquals(getObjectIds(peopleIds), cursor.getString(cursor.getColumnIndex(Contract.Transfer.PEOPLE_IDS)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.Transfer.TAG)));
        cursor.close();
    }

    private long insertTransfer(String description, Date datetime, long walletFromId,
                                long walletToId, Long walletTaxId, long moneyFrom,
                                long moneyTo, long moneyTax, String note, Long placeId,
                                Long eventId, boolean confirmed, boolean countInTotal,
                                Long[] peopleIds, Long[] attachmentIds, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Transfer.DESCRIPTION, description);
        contentValues.put(Contract.Transfer.DATE, DateUtils.getSQLDateTimeString(datetime));
        contentValues.put(Contract.Transfer.TRANSACTION_FROM_WALLET_ID, walletFromId);
        contentValues.put(Contract.Transfer.TRANSACTION_TO_WALLET_ID, walletToId);
        contentValues.put(Contract.Transfer.TRANSACTION_TAX_WALLET_ID, walletTaxId);
        contentValues.put(Contract.Transfer.TRANSACTION_FROM_MONEY, moneyFrom);
        contentValues.put(Contract.Transfer.TRANSACTION_TO_MONEY, moneyTo);
        contentValues.put(Contract.Transfer.TRANSACTION_TAX_MONEY, moneyTax);
        contentValues.put(Contract.Transfer.NOTE, note);
        contentValues.put(Contract.Transfer.PLACE_ID, placeId);
        contentValues.put(Contract.Transfer.EVENT_ID, eventId);
        contentValues.put(Contract.Transfer.CONFIRMED, confirmed);
        contentValues.put(Contract.Transfer.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Transfer.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Transfer.ATTACHMENT_IDS, getObjectIds(attachmentIds));
        contentValues.put(Contract.Transfer.TAG, tag);
        return mDatabase.insertTransfer(contentValues);
    }

    private int updateTransfer(long id, String description, Date datetime, long walletFromId,
                               long walletToId, Long walletTaxId, long moneyFrom,
                               long moneyTo, long moneyTax, String note, Long placeId,
                               Long eventId, boolean confirmed, boolean countInTotal,
                               Long[] peopleIds, Long[] attachmentIds, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Transfer.DESCRIPTION, description);
        contentValues.put(Contract.Transfer.DATE, DateUtils.getSQLDateTimeString(datetime));
        contentValues.put(Contract.Transfer.TRANSACTION_FROM_WALLET_ID, walletFromId);
        contentValues.put(Contract.Transfer.TRANSACTION_TO_WALLET_ID, walletToId);
        contentValues.put(Contract.Transfer.TRANSACTION_TAX_WALLET_ID, walletTaxId);
        contentValues.put(Contract.Transfer.TRANSACTION_FROM_MONEY, moneyFrom);
        contentValues.put(Contract.Transfer.TRANSACTION_TO_MONEY, moneyTo);
        contentValues.put(Contract.Transfer.TRANSACTION_TAX_MONEY, moneyTax);
        contentValues.put(Contract.Transfer.NOTE, note);
        contentValues.put(Contract.Transfer.PLACE_ID, placeId);
        contentValues.put(Contract.Transfer.EVENT_ID, eventId);
        contentValues.put(Contract.Transfer.CONFIRMED, confirmed);
        contentValues.put(Contract.Transfer.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.Transfer.PEOPLE_IDS, getObjectIds(peopleIds));
        contentValues.put(Contract.Transfer.ATTACHMENT_IDS, getObjectIds(attachmentIds));
        contentValues.put(Contract.Transfer.TAG, tag);
        return mDatabase.updateTransfer(id, contentValues);
    }

    private void checkTransactionModelId(long id, long money, String description, long categoryId,
                                         int direction, long walletId, Long placeId, String note,
                                         Long eventId, boolean confirmed, boolean countInTotal,
                                         String tag) {
        Cursor cursor = mDatabase.getTransactionModel(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(money, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.MONEY)));
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.DESCRIPTION)));
        assertEquals(categoryId, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_ID)));
        assertEquals(direction, cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.DIRECTION)));
        assertEquals(walletId, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.WALLET_ID)));
        checkNullable(cursor, placeId, cursor.getColumnIndex(Contract.TransactionModel.PLACE_ID));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.NOTE)));
        checkNullable(cursor, eventId, cursor.getColumnIndex(Contract.TransactionModel.EVENT_ID));
        assertEquals(confirmed, 1 == cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.CONFIRMED)));
        assertEquals(countInTotal, 1 == cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.COUNT_IN_TOTAL)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.TAG)));
        cursor.close();
    }

    private long insertTransactionModel(long money, String description, long categoryId,
                                       int direction, long walletId, Long placeId, String note,
                                       Long eventId, boolean confirmed, boolean countInTotal,
                                       String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.TransactionModel.MONEY, money);
        contentValues.put(Contract.TransactionModel.DESCRIPTION, description);
        contentValues.put(Contract.TransactionModel.CATEGORY_ID, categoryId);
        contentValues.put(Contract.TransactionModel.DIRECTION, direction);
        contentValues.put(Contract.TransactionModel.WALLET_ID, walletId);
        contentValues.put(Contract.TransactionModel.PLACE_ID, placeId);
        contentValues.put(Contract.TransactionModel.EVENT_ID, eventId);
        contentValues.put(Contract.TransactionModel.NOTE, note);
        contentValues.put(Contract.TransactionModel.CONFIRMED, confirmed);
        contentValues.put(Contract.TransactionModel.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.TransactionModel.TAG, tag);
        return mDatabase.insertTransactionModel(contentValues);
    }

    private int updateTransactionModel(long id, long money, String description, long categoryId,
                                       int direction, long walletId, Long placeId, String note,
                                       Long eventId, boolean confirmed, boolean countInTotal,
                                       String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.TransactionModel.MONEY, money);
        contentValues.put(Contract.TransactionModel.DESCRIPTION, description);
        contentValues.put(Contract.TransactionModel.CATEGORY_ID, categoryId);
        contentValues.put(Contract.TransactionModel.DIRECTION, direction);
        contentValues.put(Contract.TransactionModel.WALLET_ID, walletId);
        contentValues.put(Contract.TransactionModel.PLACE_ID, placeId);
        contentValues.put(Contract.TransactionModel.EVENT_ID, eventId);
        contentValues.put(Contract.TransactionModel.NOTE, note);
        contentValues.put(Contract.TransactionModel.CONFIRMED, confirmed);
        contentValues.put(Contract.TransactionModel.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.TransactionModel.TAG, tag);
        return mDatabase.updateTransactionModel(id, contentValues);
    }

    private void checkTransferModelId(long id, String description, long walletFromId, long walletToId,
                                      long moneyFrom, long moneyTo, long moneyTax, String note,
                                      Long placeId, Long eventId, boolean confirmed,
                                      boolean countInTotal, String tag) {
        Cursor cursor = mDatabase.getTransferModel(id, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(description, cursor.getString(cursor.getColumnIndex(Contract.TransferModel.DESCRIPTION)));
        assertEquals(walletFromId, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_ID)));
        assertEquals(walletToId, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_ID)));
        assertEquals(moneyFrom, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_FROM)));
        assertEquals(moneyTo, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TO)));
        assertEquals(moneyTax, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TAX)));
        assertEquals(note, cursor.getString(cursor.getColumnIndex(Contract.TransferModel.NOTE)));
        checkNullable(cursor, placeId, cursor.getColumnIndex(Contract.TransferModel.PLACE_ID));
        checkNullable(cursor, eventId, cursor.getColumnIndex(Contract.TransferModel.EVENT_ID));
        assertEquals(confirmed, 1 == cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.CONFIRMED)));
        assertEquals(countInTotal, 1 == cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.COUNT_IN_TOTAL)));
        assertEquals(tag, cursor.getString(cursor.getColumnIndex(Contract.TransferModel.TAG)));
        cursor.close();
    }

    private long insertTransferModel(String description, long walletFromId, long walletToId,
                                     long moneyFrom, long moneyTo, long moneyTax, String note,
                                     Long placeId, Long eventId, boolean confirmed,
                                     boolean countInTotal, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.TransferModel.DESCRIPTION, description);
        contentValues.put(Contract.TransferModel.WALLET_FROM_ID, walletFromId);
        contentValues.put(Contract.TransferModel.WALLET_TO_ID, walletToId);
        contentValues.put(Contract.TransferModel.MONEY_FROM, moneyFrom);
        contentValues.put(Contract.TransferModel.MONEY_TO, moneyTo);
        contentValues.put(Contract.TransferModel.MONEY_TAX, moneyTax);
        contentValues.put(Contract.TransferModel.NOTE, note);
        contentValues.put(Contract.TransferModel.PLACE_ID, placeId);
        contentValues.put(Contract.TransferModel.EVENT_ID, eventId);
        contentValues.put(Contract.TransferModel.CONFIRMED, confirmed);
        contentValues.put(Contract.TransferModel.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.TransferModel.TAG, tag);
        return mDatabase.insertTransferModel(contentValues);
    }

    private int updateTransferModel(long id, String description, long walletFromId, long walletToId,
                                     long moneyFrom, long moneyTo, long moneyTax, String note,
                                     Long placeId, Long eventId, boolean confirmed,
                                     boolean countInTotal, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.TransferModel.DESCRIPTION, description);
        contentValues.put(Contract.TransferModel.WALLET_FROM_ID, walletFromId);
        contentValues.put(Contract.TransferModel.WALLET_TO_ID, walletToId);
        contentValues.put(Contract.TransferModel.WALLET_FROM_ID, walletFromId);
        contentValues.put(Contract.TransferModel.MONEY_FROM, moneyFrom);
        contentValues.put(Contract.TransferModel.MONEY_TO, moneyTo);
        contentValues.put(Contract.TransferModel.MONEY_TAX, moneyTax);
        contentValues.put(Contract.TransferModel.NOTE, note);
        contentValues.put(Contract.TransferModel.PLACE_ID, placeId);
        contentValues.put(Contract.TransferModel.EVENT_ID, eventId);
        contentValues.put(Contract.TransferModel.CONFIRMED, confirmed);
        contentValues.put(Contract.TransferModel.COUNT_IN_TOTAL, countInTotal);
        contentValues.put(Contract.TransferModel.TAG, tag);
        return mDatabase.updateTransferModel(id, contentValues);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// START THE TEST /////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void insertWallet() throws Exception {
        // insert 4 wallets and then query for each one and check the returned id
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "USD", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, false, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", false, 7500L, false, "tag-wallet-4");
        // now query each wallet and check that everything is ok
        checkWalletId(id1, "Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, 0L, false, "tag-wallet-1");
        checkWalletId(id2, "Test wallet 2", "encoded-icon-2", "USD", "note-wallet-2", true, 3000L, 0L, true, "tag-wallet-2");
        checkWalletId(id3, "Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, 0L, false, "tag-wallet-3");
        checkWalletId(id4, "Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", false, 7500L, 0L, false, "tag-wallet-4");
    }

    @Test
    public void updateWallet() throws Exception {
        // insert 4 wallets and then query for each one and check the returned id
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "USD", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, false, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", false, 7500L, false, "tag-wallet-4");
        // now modify each wallet
        assertEquals(1, updateWallet(id1, "Test wallet 1-edited", "encoded-icon-1-edited", "USD", "note-wallet-1-edited", false, 4000L, true, "tag-wallet-1-edited"));
        assertEquals(1, updateWallet(id2, "Test wallet 2-edited", "encoded-icon-2-edited", "EUR", "note-wallet-2-edited", true, 3500L, true, "tag-wallet-2-edited"));
        assertEquals(1, updateWallet(id3, "Test wallet 3-edited", "encoded-icon-3-edited", "USD", "note-wallet-3-edited", false, 500L, false, "tag-wallet-3-edited"));
        assertEquals(1, updateWallet(id4, "Test wallet 4-edited", "encoded-icon-4-edited", "EUR", "note-wallet-4-edited", true, 7800L, false, "tag-wallet-4-edited"));
        // now check that the values are changed
        checkWalletId(id1, "Test wallet 1-edited", "encoded-icon-1-edited", "USD", "note-wallet-1-edited", false, 4000L, 0L, true, "tag-wallet-1-edited");
        checkWalletId(id2, "Test wallet 2-edited", "encoded-icon-2-edited", "EUR", "note-wallet-2-edited", true, 3500L, 0L, true, "tag-wallet-2-edited");
        checkWalletId(id3, "Test wallet 3-edited", "encoded-icon-3-edited", "USD", "note-wallet-3-edited", false, 500L, 0L, false, "tag-wallet-3-edited");
        checkWalletId(id4, "Test wallet 4-edited", "encoded-icon-4-edited", "EUR", "note-wallet-4-edited", true, 7800L, 0L, false, "tag-wallet-4-edited");
    }

    @Test
    public void deleteWallet() throws Exception {
        // insert 4 wallets and then query for each one and check the returned id
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "USD", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, false, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", false, 7500L, false, "tag-wallet-4");
        // check that the returned cursor contains exactly 4 wallets
        checkCursorSize(mDatabase.getWallets(null, null, null, null), 4);
        // now remove two wallets
        assertEquals(1, mDatabase.deleteWallet(id2));
        assertEquals(1, mDatabase.deleteWallet(id3));
        // recheck the wallet count
        checkCursorSize(mDatabase.getWallets(null, null, null, null), 2);
    }

    @Test
    public void testEnsureDatabaseCleanAfterWalletDelete() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 5);
        Date endDate = calendar.getTime();
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 1, null, true, "category-tag-3");
        long id4 = insertTransaction(10, new Date(), "test", id3, Contract.Direction.EXPENSE, 0, id1, null, null, null, null, null, true, true, null, null, null);
        long id5 = insertTransactionModel(25, "desc", id3, Contract.Direction.INCOME, id1, null, null, null, true, true, null);
        long id6 = insertTransferModel("desc", id1, id2, 30L, 30L, 0L, "note", null, null, true, true, null);
        long id7 = insertSaving("desc", "encoded-icon", 0L, 100L, id1, null, false, null, null);
        long id8 = insertDebt(Contract.DebtType.DEBT.getValue(), "encoded-icon", "desc", new Date(), null, id1, null, null, 20, false, null, null, false);
        long id9 = insertBudget(Schema.BudgetType.CATEGORY, id3, startDate, endDate, 3000L, "EUR", new Long[] {id1, id2}, null);
        checkBudgetId(id9, Schema.BudgetType.CATEGORY, id3, startDate, endDate, 3000L, "EUR", new Long[] {id1, id2}, null);
        // now delete the wallet 1
        mDatabase.deleteWallet(id1);
        // check if everything has been deleted
        checkCursorSize(mDatabase.getTransaction(id4, null), 0);
        checkCursorSize(mDatabase.getTransactionModel(id5, null), 0);
        checkCursorSize(mDatabase.getTransferModel(id6, null), 0);
        checkCursorSize(mDatabase.getSaving(id7, null), 0);
        checkCursorSize(mDatabase.getDebt(id8, null), 0);
        checkBudgetId(id9, Schema.BudgetType.CATEGORY, id3, startDate, endDate, 3000L, "EUR", new Long[] {id2}, null);
    }

    @Test(expected = SQLiteDataException.class)
    public void deleteWalletInTransfer() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertTransfer("desc", new Date(), id1, id2, null, 10L, 10L, 0L, "note", null, null, true, true, null, null, "tag");
        // the wallet cannot be deleted because is used in the transfer 3
        mDatabase.deleteWallet(id2);
    }

    @Test
    public void insertTransaction() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 2", "encoded-icon-2", Contract.CategoryType.INCOME.getValue(), null, true, "tag-category-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id8 = insertTransaction(2000L, date, "desc", id2, Contract.Direction.INCOME, 0, id1, id3, "note", null, null, null, true, true, peopleIds, attachmentIds, "tag");
        checkTransactionId(id8, 2000L, date, "desc", id2, Contract.Direction.INCOME, 0, id1, id3, "note", null, null, null, true, true, peopleIds, "tag");
    }

    @Test
    public void updateTransaction() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 2", "encoded-icon-2", Contract.CategoryType.INCOME.getValue(), null, true, "tag-category-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id8 = insertTransaction(2000L, date, "desc", id2, Contract.Direction.INCOME, 0, id1, id3, "note", null, null, null, true, true, peopleIds, attachmentIds, "tag");
        // now modify the transaction
        long id9 = insertWallet("Test wallet 9", "encoded-icon-9", "USD", "note-wallet-9", false, 2000L, false, "tag-wallet-9");
        long id10 = insertCategory("Test category 10", "encoded-icon-10", Contract.CategoryType.EXPENSE.getValue(), null, true, "tag-category-10");
        long id11 = insertPerson("person-11", "encoded-icon-11", "note-11", "tag-11");
        peopleIds = new Long[] {id4, id5, id11};
        assertEquals(1, updateTransaction(id8, 4000L, date, "desc-edited", id10, Contract.Direction.EXPENSE, 0, id9, null, "note-edited", null, null, null, true, false, peopleIds, null, "tag-edited"));
        // now check if transaction has been properly edited
        checkTransactionId(id8, 4000L, date, "desc-edited", id10, Contract.Direction.EXPENSE, 0, id9, null, "note-edited", null, null, null, true, false, peopleIds, "tag-edited");
    }

    @Test
    public void deleteTransaction() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 2", "encoded-icon-2", Contract.CategoryType.INCOME.getValue(), null, true, "tag-category-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id8 = insertTransaction(2000L, date, "desc", id2, Contract.Direction.INCOME, 0, id1, id3, "note", null, null, null, true, true, peopleIds, attachmentIds, "tag");
        // check the transaction count
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 1);
        // now delete the transaction item
        mDatabase.deleteTransaction(id8);
        // recheck the transaction count
        checkCursorSize(mDatabase.getTransaction(id8, null), 0);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 0);
    }

    @Test
    public void insertTransfer() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        long id8 = insertEvent("event-8", "encoded-icon-8", new Date(), new Date(), "note-8", "tag-8");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id9 = insertTransfer("desc", date, id1, id2, id1, 10L, 10L, 4L, "note", id3, id8, true, true, peopleIds, attachmentIds, "tag");
        checkTransferId(id9, "desc", date, id1, id2, id1, 10L, 10L, 4L, "note", id3, id8, true, true, peopleIds, "tag");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 3);
    }

    @Test
    public void updateTransfer() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        long id8 = insertEvent("event-8", "encoded-icon-8", new Date(), new Date(), "note-8", "tag-8");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id9 = insertTransfer("desc", date, id1, id2, null, 10L, 10L, 0L, "note", id3, id8, true, true, peopleIds, attachmentIds, "tag");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 2);
        // now we update the transfer adding the tax
        long id10 = insertPerson("person-10", "encoded-icon-10", "note-10", "tag-10");
        long id11 = insertPerson("person-11", "encoded-icon-11", "note-11", "tag-11");
        peopleIds = new Long[] {id10, id11};
        assertEquals(1, updateTransfer(id9, "desc-edited", date, id1, id2, id1, 10L, 10L, 4L, "note-edited", id3, id8, false, false, peopleIds, null, "tag-edited"));
        checkTransferId(id9, "desc-edited", date, id1, id2, id1, 10L, 10L, 4L, "note-edited", id3, id8, false, false, peopleIds, "tag-edited");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 3);
        // now we change the source wallet
        long id12 = insertWallet("Test wallet 12", "encoded-icon-12", "EUR", "note-wallet-12", true, 22000L, false, "tag-wallet-12");
        assertEquals(1, updateTransfer(id9, "desc-edited", date, id12, id2, id12, 10L, 10L, 4L, "note-edited", id3, id8, false, false, peopleIds, null, "tag-edited"));
        checkTransferId(id9, "desc-edited", date, id12, id2, id12, 10L, 10L, 4L, "note-edited", id3, id8, false, false, peopleIds, "tag-edited");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 3);
        // now remove again the tax
        assertEquals(1, updateTransfer(id9, "desc-edited", date, id12, id2, null, 10L, 10L, 0L, "note-edited", id3, id8, false, false, peopleIds, null, "tag-edited"));
        checkTransferId(id9, "desc-edited", date, id12, id2, null, 10L, 10L, 0L, "note-edited", id3, id8, false, false, peopleIds, "tag-edited");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 2);
    }

    @Test
    public void deleteTransfer() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id7 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        long id8 = insertEvent("event-8", "encoded-icon-8", new Date(), new Date(), "note-8", "tag-8");
        Date date = new Date();
        Long[] peopleIds = new Long[] {id4, id5};
        Long[] attachmentIds = new Long[] {id6, id7};
        long id9 = insertTransfer("desc", date, id1, id2, id1, 10L, 10L, 2L, "note", id3, id8, true, true, peopleIds, attachmentIds, "tag");
        checkCursorSize(mDatabase.getTransfers(null, null, null, null), 1);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 3);
        // now delete the transfer
        mDatabase.deleteTransfer(id9);
        // recheck the table sizes
        checkCursorSize(mDatabase.getTransfer(id9, null), 0);
        checkCursorSize(mDatabase.getTransfers(null, null, null, null), 0);
        // also the transactions must be removed!!!
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 0);
    }

    @Test
    public void insertCategory() throws Exception {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 1, null, true, "category-tag-3");
        long id4 = insertCategory("category 4", "encoded-icon-4", 1, id3, true, "category-tag-4");
        long id5 = insertCategory("category 5", "encoded-icon-5", 1, null, true, "category-tag-5");
        checkCategoryId(id1, "category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        checkCategoryId(id2, "category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        checkCategoryId(id3, "category 3", "encoded-icon-3", 1, null, true, "category-tag-3");
        checkCategoryId(id4, "category 4", "encoded-icon-4", 1, id3, true, "category-tag-4");
        checkCategoryId(id5, "category 5", "encoded-icon-5", 1, null, true, "category-tag-5");
    }

    @Test(expected = SQLiteDataException.class)
    public void testInsertNestedCategories() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, id2, false, "category-tag-3");
    }

    @Test(expected = SQLiteDataException.class)
    public void testInsertInconsistentCategoryTree() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 1, id1, false, "category-tag-2");
    }

    @Test
    public void updateCategory() throws Exception {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 1, null, true, "category-tag-3");
        long id4 = insertCategory("category 4", "encoded-icon-4", 1, id3, true, "category-tag-4");
        long id5 = insertCategory("category 5", "encoded-icon-5", 1, null, true, "category-tag-5");
        // move category 5 as child of category 3 and edit category 2
        assertEquals(1, updateCategory(id2, "category 2-edited", "encoded-icon-2-edited", 0, null, true, "category-tag-2-edited"));
        assertEquals(1, updateCategory(id5, "category 5-edited", "encoded-icon-5-edited", 1, id3, false, "category-tag-5-edited"));
        // check that categories 1, 3 and 4 are not changed and category 2 and 5 are changed
        checkCategoryId(id1, "category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        checkCategoryId(id2, "category 2-edited", "encoded-icon-2-edited", 0, null, true, "category-tag-2-edited");
        checkCategoryId(id3, "category 3", "encoded-icon-3", 1, null, true, "category-tag-3");
        checkCategoryId(id4, "category 4", "encoded-icon-4", 1, id3, true, "category-tag-4");
        checkCategoryId(id5, "category 5-edited", "encoded-icon-5-edited", 1, id3, false, "category-tag-5-edited");
    }

    @Test(expected = SQLiteDataException.class)
    public void testUpdateNestedCategories1() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, null, false, "category-tag-3");
        // update category 1 to become children of category 3
        updateCategory(id1, "category 1", "encoded-icon-1", 0, id3, true, "category-tag-1");
    }

    @Test(expected = SQLiteDataException.class)
    public void testUpdateNestedCategories2() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, null, false, "category-tag-3");
        // update category 3 to become children of category 2
        updateCategory(id3, "category 3", "encoded-icon-3", 0, id2, false, "category-tag-3");
    }

    @Test(expected = SQLiteDataException.class)
    public void testUpdateInconsistentChildrenCategory() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, id1, false, "category-tag-3");
        // now change the type of the child category 3
        updateCategory(id3, "category 3", "encoded-icon-3", 1, id1, false, "category-tag-3");
    }

    @Test(expected = SQLiteDataException.class)
    public void testUpdateInconsistentParentCategory() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, id1, false, "category-tag-3");
        // now change the type of the parent category 1
        updateCategory(id1, "category 1", "encoded-icon-1", 1, null, true, "category-tag-1");
    }

    @Test
    public void deleteCategory() throws Exception {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        long id3 = insertCategory("category 3", "encoded-icon-3", 0, id1, false, "category-tag-3");
        // check cursor size
        int count = checkCursorMinSize(mDatabase.getCategories(null, null, null, null), 3);
        // now delete category 2 and 3
        assertEquals(1, mDatabase.deleteCategory(id2));
        assertEquals(1, mDatabase.deleteCategory(id3));
        // recheck cursor size
        int newCount = checkCursorMinSize(mDatabase.getCategories(null, null, null, null), 1);
        assertEquals(newCount, count - 2);
    }

    @Test(expected = SQLiteDataException.class)
    public void testDeleteCategoryWithChildren() {
        long id1 = insertCategory("category 1", "encoded-icon-1", 0, null, true, "category-tag-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, id1, false, "category-tag-2");
        // category 1 must not be removed because it has at least one child
        mDatabase.deleteCategory(id1);
    }

    @Test(expected = SQLiteDataException.class)
    public void testDeleteSystemCategory() {
        long categoryId = getSystemCategory(Contract.CategoryTag.TRANSFER);
        // a system category cannot be deleted
        mDatabase.deleteCategory(categoryId);
    }

    @Test(expected = SQLiteDataException.class)
    public void testDeleteCategoryUsedInTransaction() {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, null, true, "category-tag-2");
        long id3 = insertTransaction(2000L, new Date(), "description-3", id2, Contract.Direction.INCOME, 0, id1, null, "note-3", null, null, null, true, true, null, null, "tag-3");
        // the category cannot be deleted because it is used in transaction 3
        mDatabase.deleteCategory(id2);
    }

    @Test(expected = SQLiteDataException.class)
    public void testDeleteCategoryUsedInTransactionModel() {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, null, true, "category-tag-2");
        long id3 = insertTransactionModel(2000L, "description-3", id2, Contract.Direction.INCOME, id1, null, "note-3", null, true, true, "tag-3");
        // the category cannot be deleted because it is used in transaction-model 3
        mDatabase.deleteCategory(id2);
    }

    @Test(expected = SQLiteDataException.class)
    public void testDeleteCategoryUsedInBudget() {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("category 2", "encoded-icon-2", 0, null, true, "category-tag-2");
        long id3 = insertBudget(Schema.BudgetType.CATEGORY, id2, new Date(), new Date(), 3000L, "EUR", new Long[] {id1}, "tag");
        // the category cannot be deleted because it is used in budget 3
        mDatabase.deleteCategory(id2);
    }

    @Test
    public void insertDebt() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id3 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id4 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id5 = insertPerson("person-3", "encoded-icon-3", "note-3", "tag-3");
        Date date = new Date();
        Long[] peopleIds1 = new Long[] {id3, id4, id5};
        long id6 = insertDebt(Contract.DebtType.DEBT.getValue(), "encoded-icon-1", "desc-1", date, null, id1, "note-1", id2, 2000L, false, peopleIds1, "tag-1", false);
        checkDebtId(id6, Contract.DebtType.DEBT.getValue(), "encoded-icon-1", "desc-1", date, null, id1, "note-1", id2, 2000L, false, peopleIds1, "tag-1");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 0);
        long id7 = insertDebt(Contract.DebtType.CREDIT.getValue(), "encoded-icon-1", "desc-1", date, date, id1, "note-1", id2, 3000L, true, null, "tag-2", true);
        checkDebtId(id7, Contract.DebtType.CREDIT.getValue(), "encoded-icon-1", "desc-1", date, date, id1, "note-1", id2, 3000L, true, null, "tag-2");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 1);
    }

    @Test
    public void updateDebt() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id3 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id4 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id5 = insertPerson("person-3", "encoded-icon-3", "note-3", "tag-3");
        Date date = new Date();
        Long[] peopleIds1 = new Long[] {id3, id4};
        long id6 = insertDebt(Contract.DebtType.DEBT.getValue(), "encoded-icon-1", "desc-1", date, null, id1, "note-1", id2, 2000L, false, peopleIds1, "tag-1", false);
        long id7 = insertDebt(Contract.DebtType.CREDIT.getValue(), "encoded-icon-1", "desc-1", date, date, id1, "note-1", id2, 3000L, true, null, "tag-2", true);
        checkDebtId(id6, Contract.DebtType.DEBT.getValue(), "encoded-icon-1", "desc-1", date, null, id1, "note-1", id2, 2000L, false, peopleIds1, "tag-1");
        checkDebtId(id7, Contract.DebtType.CREDIT.getValue(), "encoded-icon-1", "desc-1", date, date, id1, "note-1", id2, 3000L, true, null, "tag-2");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 1);
        // now update both debts
        peopleIds1 = new Long[] {id4, id5};
        assertEquals(1, updateDebt(id6, Contract.DebtType.DEBT.getValue(), "encoded-icon-1-edited", "desc-1-edited", date, date, id1, "note-1-edited", id2, 3000L, true, peopleIds1, "tag-1-edited"));
        assertEquals(1, updateDebt(id7, Contract.DebtType.CREDIT.getValue(), "encoded-icon-2-edited", "desc-2-edited", date, null, id1, "note-1-edited", id2, 8000L, false, null, "tag-2-edited"));
        // now check that everything has been updated correctly
        checkDebtId(id6, Contract.DebtType.DEBT.getValue(), "encoded-icon-1-edited", "desc-1-edited", date, date, id1, "note-1-edited", id2, 3000L, true, peopleIds1, "tag-1-edited");
        checkDebtId(id7, Contract.DebtType.CREDIT.getValue(), "encoded-icon-2-edited", "desc-2-edited", date, null, id1, "note-1-edited", id2, 8000L, false, null, "tag-2-edited");
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 1);
    }

    @Test
    public void deleteDebt() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        long id3 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id4 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id5 = insertPerson("person-3", "encoded-icon-3", "note-3", "tag-3");
        long id6 = getSystemCategory(Contract.CategoryTag.PAID_DEBT);
        Date date = new Date();
        Long[] peopleIds1 = new Long[] {id3, id4};
        long id7 = insertDebt(Contract.DebtType.DEBT.getValue(), "encoded-icon-1", "desc-1", date, null, id1, "note-1", id2, 2000L, false, peopleIds1, "tag-1", true);
        long id8 = insertTransaction(10, date, "desc", id6, Contract.Direction.EXPENSE, Contract.TransactionType.DEBT, id1, null, null, null, null, id7, true, true, new Long[] {id5}, null, "tag");
        long id9 = insertTransaction(10, date, "desc", id6, Contract.Direction.EXPENSE, Contract.TransactionType.DEBT, id1, id2, null, null, null, id7, true, true, null, null, "tag");
        long id10 = insertTransaction(10, date, "desc", id6, Contract.Direction.EXPENSE, Contract.TransactionType.DEBT, id1, null, null, null, null, id7, true, true, new Long[] {id3, id4}, null, "tag");
        long id11 = insertTransaction(10, date, "desc", id6, Contract.Direction.EXPENSE, Contract.TransactionType.DEBT, id1, id2, null, null, null, id7, true, true, null, null, "tag");
        checkCursorSize(mDatabase.getDebts(null, null, null, null), 1);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 5);
        // now delete the debt and check if all the related transactions are removed
        assertEquals(1, mDatabase.deleteDebt(id7));
        checkCursorSize(mDatabase.getDebts(null, null, null, null), 0);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 0);
    }

    @Test
    public void insertBudget() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, true, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        long id5 = insertWallet("Test wallet 5", "encoded-icon-5", "USD", "note-wallet-5", true, 3500L, true, "tag-wallet-5");
        long id6 = insertCategory("Test category 1", "encoded-icon", 1, null, true, "category-tag");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds1 = new Long[] {id1, id3};
        Long[] walletIds2 = new Long[] {id1, id2, id3};
        Long[] walletIds3 = new Long[] {id4, id5};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds1, "tag-1");
        long id8 = insertBudget(Schema.BudgetType.EXPENSES, null, startDate, endDate, 10000L, "EUR", walletIds2, "tag-2");
        long id9 = insertBudget(Schema.BudgetType.CATEGORY, id6, startDate, endDate, 13000L, "USD", walletIds3, "tag-3");
        // now ensure everything is stored correctly
        checkBudgetId(id7, Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds1, "tag-1");
        checkBudgetId(id8, Schema.BudgetType.EXPENSES, null, startDate, endDate, 10000L, "EUR", walletIds2, "tag-2");
        checkBudgetId(id9, Schema.BudgetType.CATEGORY, id6, startDate, endDate, 13000L, "USD", walletIds3, "tag-3");
    }

    @Test(expected = SQLiteDataException.class)
    public void insertBudgetWithNoWallets() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", null, "tag-1");
    }

    @Test(expected = SQLiteDataException.class)
    public void insertBudgetWithNotConsistentWallets() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, true, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        long id5 = insertWallet("Test wallet 5", "encoded-icon-5", "USD", "note-wallet-5", true, 3500L, true, "tag-wallet-5");
        long id6 = insertCategory("Test category 1", "encoded-icon", 1, null, true, "category-tag");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds = new Long[] {id1, id3, id4};
        insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
    }

    @Test(expected = SQLiteDataException.class)
    public void insertBudgetWithInvalidWallet() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds = new Long[] {id1, id2, id1 + id2};
        insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
    }

    @Test
    public void updateBudget() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, true, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        long id5 = insertWallet("Test wallet 5", "encoded-icon-5", "USD", "note-wallet-5", true, 3500L, true, "tag-wallet-5");
        long id6 = insertCategory("Test category 1", "encoded-icon", 1, null, true, "category-tag");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds1 = new Long[] {id1, id3};
        Long[] walletIds2 = new Long[] {id1, id2, id3};
        Long[] walletIds3 = new Long[] {id4, id5};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds1, "tag-1");
        long id8 = insertBudget(Schema.BudgetType.EXPENSES, null, startDate, endDate, 10000L, "EUR", walletIds2, "tag-2");
        long id9 = insertBudget(Schema.BudgetType.CATEGORY, id6, startDate, endDate, 13000L, "USD", walletIds3, "tag-3");
        // now update budget 8 and 9
        walletIds2 = new Long[] {id5};
        walletIds3 = new Long[] {id4};
        calendar.add(Calendar.MONTH, -1);
        Date newStartDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 9);
        Date newEndDate = calendar.getTime();
        assertEquals(1, updateBudget(id7, Schema.BudgetType.CATEGORY, id6, startDate, endDate, 1000L, "EUR", walletIds1, "tag-1-edited"));
        assertEquals(1, updateBudget(id8, Schema.BudgetType.EXPENSES, null, startDate, endDate, 5000L, "USD", walletIds2, "tag-2-edited"));
        assertEquals(1, updateBudget(id9, Schema.BudgetType.CATEGORY, id6, newStartDate, newEndDate, 8000L, "USD", walletIds3, "tag-3-edited"));
        // now check that everything has been updated correctly
        checkBudgetId(id7, Schema.BudgetType.CATEGORY, id6, startDate, endDate, 1000L, "EUR", walletIds1, "tag-1-edited");
        checkBudgetId(id8, Schema.BudgetType.EXPENSES, null, startDate, endDate, 5000L, "USD", walletIds2, "tag-2-edited");
        checkBudgetId(id9, Schema.BudgetType.CATEGORY, id6, newStartDate, newEndDate, 8000L, "USD", walletIds3, "tag-3-edited");
    }

    @Test(expected = SQLiteDataException.class)
    public void updateBudgetWithNoWallets() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds = new Long[] {id1, id2};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
        // now update the budget with no wallets
        updateBudget(id7, Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", null, "tag-1");
    }

    @Test(expected = SQLiteDataException.class)
    public void updateBudgetWithNotConsistentWallets() {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds = new Long[] {id1, id2};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
        // now update the budget with no wallets
        walletIds = new Long[] {id1, id4};
        updateBudget(id7, Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
    }

    @Test(expected = SQLiteDataException.class)
    public void updateBudgetWithInvalidWallet() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds = new Long[] {id1, id2};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
        // now update the budget with no wallets
        walletIds = new Long[] {id1, id2, id1 + id2 + id4};
        updateBudget(id7, Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds, "tag-1");
    }

    @Test
    public void deleteBudget() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertWallet("Test wallet 3", "encoded-icon-3", "EUR", "note-wallet-3", true, 1000L, true, "tag-wallet-3");
        long id4 = insertWallet("Test wallet 4", "encoded-icon-4", "USD", "note-wallet-4", true, 2300L, true, "tag-wallet-4");
        long id5 = insertWallet("Test wallet 5", "encoded-icon-5", "USD", "note-wallet-5", true, 3500L, true, "tag-wallet-5");
        long id6 = insertCategory("Test category 1", "encoded-icon", 1, null, true, "category-tag");
        // test insert income budget
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 6);
        Date endDate = calendar.getTime();
        Long[] walletIds1 = new Long[] {id1, id3};
        Long[] walletIds2 = new Long[] {id1, id2, id3};
        Long[] walletIds3 = new Long[] {id4, id5};
        long id7 = insertBudget(Schema.BudgetType.INCOMES, null, startDate, endDate, 5000L, "EUR", walletIds1, "tag-1");
        long id8 = insertBudget(Schema.BudgetType.EXPENSES, null, startDate, endDate, 10000L, "EUR", walletIds2, "tag-2");
        long id9 = insertBudget(Schema.BudgetType.CATEGORY, id6, startDate, endDate, 13000L, "USD", walletIds3, "tag-3");
        // check budget count
        checkCursorSize(mDatabase.getBudgets(null, null, null, null), 3);
        // now delete all the budget one by one and check the budget count, than ensure all the
        // wallets and the category are not deleted
        assertEquals(1, mDatabase.deleteBudget(id7));
        checkCursorSize(mDatabase.getBudget(id7, null), 0);
        checkCursorSize(mDatabase.getBudgets(null, null, null, null), 2);
        assertEquals(1, mDatabase.deleteBudget(id8));
        checkCursorSize(mDatabase.getBudget(id8, null), 0);
        checkCursorSize(mDatabase.getBudgets(null, null, null, null), 1);
        assertEquals(1, mDatabase.deleteBudget(id9));
        checkCursorSize(mDatabase.getBudget(id9, null), 0);
        checkCursorSize(mDatabase.getBudgets(null, null, null, null), 0);
        checkCursorSize(mDatabase.getWallet(id1, null), 1);
        checkCursorSize(mDatabase.getWallet(id2, null), 1);
        checkCursorSize(mDatabase.getWallet(id3, null), 1);
        checkCursorSize(mDatabase.getWallet(id4, null), 1);
        checkCursorSize(mDatabase.getWallet(id5, null), 1);
        checkCursorSize(mDatabase.getCategory(id6, null), 1);
    }

    @Test
    public void insertSaving() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        Date exp = new Date();
        long id2 = insertSaving("desc-1", "encoded-icon", 0L, 10000L, id1, null, false, "note-1", "tag-1");
        long id3 = insertSaving("desc-2", "encoded-icon", 500L, 23000L, id1, exp, true, "note-2", "tag-2");
        checkSavingId(id2, "desc-1", "encoded-icon", 0L, 10000L, id1, null, false, "note-1", "tag-1");
        checkSavingId(id3, "desc-2", "encoded-icon", 500L, 23000L, id1, exp, true, "note-2", "tag-2");
    }

    @Test
    public void updateSaving() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        Date exp = new Date();
        long id2 = insertSaving("desc-1", "encoded-icon", 0L, 10000L, id1, null, false, "note-1", "tag-1");
        long id3 = insertSaving("desc-2", "encoded-icon", 500L, 23000L, id1, exp, true, "note-2", "tag-2");
        // now update the saving
        assertEquals(1, updateSaving(id2, "desc-1-edited", "encoded-icon-edited", 3L, 100L, id1, exp, true, "note-1-edited", "tag-1-edited"));
        assertEquals(1, updateSaving(id3, "desc-2-edited", "encoded-icon-edited", 50L, 73000L, id1, null, false, "note-2-edited", "tag-2-edited"));
        // now check that both savings have been successfully update
        checkSavingId(id2, "desc-1-edited", "encoded-icon-edited", 3L, 100L, id1, exp, true, "note-1-edited", "tag-1-edited");
        checkSavingId(id3, "desc-2-edited", "encoded-icon-edited", 50L, 73000L, id1, null, false, "note-2-edited", "tag-2-edited");
    }

    @Test
    public void deleteSaving() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        Date exp = new Date();
        long id2 = insertSaving("desc-1", "encoded-icon", 0L, 10000L, id1, null, false, "note-1", "tag-1");
        // we can now add some transactions to this saving
        long id3 = getSystemCategory(Contract.CategoryTag.SAVING_DEPOSIT);
        long id4 = getSystemCategory(Contract.CategoryTag.SAVING_WITHDRAW);
        long id5 = insertTransaction(10, new Date(), null, id3, Contract.Direction.EXPENSE, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        long id6 = insertTransaction(20, new Date(), null, id3, Contract.Direction.EXPENSE, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        long id7 = insertTransaction(30, new Date(), null, id4, Contract.Direction.INCOME, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        long id8 = insertTransaction(10, new Date(), null, id3, Contract.Direction.EXPENSE, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        long id9 = insertTransaction(20, new Date(), null, id3, Contract.Direction.EXPENSE, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        long id10 = insertTransaction(30, new Date(), null, id4, Contract.Direction.INCOME, Contract.TransactionType.SAVING, id1, null, null, null, id2, null, true, true, null, null, "tag");
        // now check the number of savings and transactions
        checkCursorSize(mDatabase.getSavings(null, null, null, null), 1);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 6);
        // now delete the saving
        assertEquals(1, mDatabase.deleteSaving(id2));
        // now ensure that all the transactions and the debt have been deleted
        checkCursorSize(mDatabase.getSavings(null, null, null, null), 0);
        checkCursorSize(mDatabase.getTransactions(null, null, null, null), 0);
    }

    @Test
    public void insertEvent() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -4);
        Date startDate = calendar.getTime();
        Date endDate = new Date();
        long id1 = insertEvent("Event 1", "encoded-icon-1", startDate, endDate, "note-1", "tag-1");
        long id2 = insertEvent("Event 2", "encoded-icon-2", startDate, endDate, "note-2", "tag-2");
        long id3 = insertEvent("Event 3", "encoded-icon-3", startDate, endDate, "note-3", "tag-3");
        checkEventId(id1, "Event 1", "encoded-icon-1", startDate, endDate, "note-1", "tag-1");
        checkEventId(id2, "Event 2", "encoded-icon-2",startDate, endDate, "note-2", "tag-2");
        checkEventId(id3, "Event 3", "encoded-icon-3", startDate, endDate, "note-3", "tag-3");
    }

    @Test
    public void updateEvent() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -4);
        Date startDate = calendar.getTime();
        Date endDate = new Date();
        long id1 = insertEvent("Event 1", "encoded-icon-1", startDate, endDate, "note-1", "tag-1");
        long id2 = insertEvent("Event 2", "encoded-icon-2", startDate, endDate, "note-2", "tag-2");
        long id3 = insertEvent("Event 3", "encoded-icon-3", startDate, endDate, "note-3", "tag-3");
        // update event 3
        calendar.add(Calendar.MONTH, 2);
        Date newStartDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 5);
        Date newEndDate = calendar.getTime();
        assertEquals(1, updateEvent(id3, "Event 3-edited", "encoded-icon-3-edited", newStartDate, newEndDate, "note-3-edited", "tag-3-edited"));
        // check
        checkEventId(id1, "Event 1", "encoded-icon-1", startDate, endDate, "note-1", "tag-1");
        checkEventId(id2, "Event 2", "encoded-icon-2",startDate, endDate, "note-2", "tag-2");
        checkEventId(id3, "Event 3-edited", "encoded-icon-3-edited", newStartDate, newEndDate, "note-3-edited", "tag-3-edited");
    }

    @Test
    public void deleteEvent() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -4);
        Date startDate = calendar.getTime();
        Date endDate = new Date();
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id3 = insertEvent("Event 1", "encoded-icon-1", startDate, endDate, "note-1", "tag-1");
        // add the event to all the possible items
        long id4 = insertTransaction(2000L, endDate, "desc", id2, Contract.Direction.INCOME, 0, id1, null, "note-1", id3, null, null, true, true, null, null, "tag-1");
        long id5 = insertTransactionModel(4503L, "desc", id2, Contract.Direction.INCOME, id1, null, "note", id3, true, true, "tag");
        long id6 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 2200L, false, "tag-wallet-2");
        long id7 = insertTransfer("desc", endDate, id1, id6, null, 4000L, 4000L, 0L, "note", null, id3, true, true, null, null, "tag-1");
        long id8 = insertTransferModel("desc", id1, id6, 4000L, 4000L, 0L, "note", null, id3, true, true, "tag-8");
        assertEquals(1, mDatabase.deleteEvent(id3));
        checkTransactionId(id4, 2000L, endDate, "desc", id2, Contract.Direction.INCOME, 0, id1, null, "note-1", null, null, null, true, true, null, "tag-1");
        checkTransactionModelId(id5, 4503L, "desc", id2, Contract.Direction.INCOME, id1, null, "note", null, true, true, "tag");
        checkTransferId(id7, "desc", endDate, id1, id6, null, 4000L, 4000L, 0L, "note", null, null, true, true, null, "tag-1");
        checkTransferModelId(id8, "desc", id1, id6, 4000L, 4000L, 0L, "note", null, null, true, true, "tag-8");
    }

    @Test
    public void insertTransactionModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id3 = insertTransactionModel(302, "desc-1", id2, Contract.Direction.INCOME, id1, null, "note-1", null, true, true, "tag-1");
        long id4 = insertTransactionModel(275, "desc-2", id2, Contract.Direction.INCOME, id1, null, "note-2", null, false, false, "tag-2");
        checkTransactionModelId(id3, 302, "desc-1", id2, Contract.Direction.INCOME, id1, null, "note-1", null, true, true, "tag-1");
        checkTransactionModelId(id4, 275, "desc-2", id2, Contract.Direction.INCOME, id1, null, "note-2", null, false, false, "tag-2");
    }

    @Test
    public void updateTransactionModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id3 = insertTransactionModel(302, "desc-1", id2, Contract.Direction.INCOME, id1, null, "note-1", null, true, true, "tag-1");
        long id4 = insertTransactionModel(275, "desc-2", id2, Contract.Direction.INCOME, id1, null, "note-2", null, false, false, "tag-2");
        // update transaction model 3
        assertEquals(1, updateTransactionModel(id3, 302, "desc-1-edited", id2, Contract.Direction.EXPENSE, id1, null, "note-1-edited", null, false, true, "tag-1-edited"));
        // check transaction models
        checkTransactionModelId(id3, 302, "desc-1-edited", id2, Contract.Direction.EXPENSE, id1, null, "note-1-edited", null, false, true, "tag-1-edited");
        checkTransactionModelId(id4, 275, "desc-2", id2, Contract.Direction.INCOME, id1, null, "note-2", null, false, false, "tag-2");
    }

    @Test
    public void deleteTransactionModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id3 = insertTransactionModel(302, "desc-1", id2, Contract.Direction.INCOME, id1, null, "note-1", null, true, true, "tag-1");
        long id4 = insertTransactionModel(275, "desc-2", id2, Contract.Direction.INCOME, id1, null, "note-2", null, false, false, "tag-2");
        // check the current count of transaction models
        checkCursorSize(mDatabase.getTransactionModels(null, null, null, null), 2);
        // now delete the two transaction models
        assertEquals(1, mDatabase.deleteTransactionModel(id3));
        assertEquals(1, mDatabase.deleteTransactionModel(id4));
        // recheck the current count of transaction models
        checkCursorSize(mDatabase.getTransactionModels(null, null, null, null), 0);
    }

    @Test
    public void insertTransferModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertTransferModel("desc-1", id1, id2, 10L, 10L, 0L, "note-1", null, null, true, true, "tag-1");
        long id4 = insertTransferModel("desc-2", id1, id2, 5L, 5L, 10L, "note-2", null, null, true, false, "tag-2");
        checkTransferModelId(id3, "desc-1", id1, id2, 10L, 10L, 0L, "note-1", null, null, true, true, "tag-1");
        checkTransferModelId(id4, "desc-2", id1, id2, 5L, 5L, 10L, "note-2", null, null, true, false, "tag-2");
    }

    @Test
    public void updateTransferModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertTransferModel("desc-1", id1, id2, 10L, 10L, 0L, "note-1", null, null, true, true, "tag-1");
        long id4 = insertTransferModel("desc-2", id1, id2, 5L, 5L, 10L, "note-2", null, null, true, false, "tag-2");
        // now update the transfer model 3
        assertEquals(1, updateTransferModel(id3, "desc-1-edited", id1, id2, 20L, 20L, 3L, "note-1-edited", null, null, false, true, "tag-1-edited"));
        // check the transfer models
        checkTransferModelId(id3, "desc-1-edited", id1, id2, 20L, 20L, 3L, "note-1-edited", null, null, false, true, "tag-1-edited");
        checkTransferModelId(id4, "desc-2", id1, id2, 5L, 5L, 10L, "note-2", null, null, true, false, "tag-2");
    }

    @Test
    public void deleteTransferModel() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertTransferModel("desc-1", id1, id2, 10L, 10L, 0L, "note-1", null, null, true, true, "tag-1");
        long id4 = insertTransferModel("desc-2", id1, id2, 5L, 5L, 10L, "note-2", null, null, true, false, "tag-2");
        // check the current count of transaction models
        checkCursorSize(mDatabase.getTransferModels(null, null, null, null), 2);
        // now delete the two transaction models
        assertEquals(1, mDatabase.deleteTransferModel(id3));
        assertEquals(1, mDatabase.deleteTransferModel(id4));
        // recheck the current count of transaction models
        checkCursorSize(mDatabase.getTransferModels(null, null, null, null), 0);
    }

    @Test
    public void insertPlace() throws Exception {
        long id1 = insertPlace("place-1", "encoded-icon-1", "fake-address-1", null, null, "tag-1");
        long id2 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        checkPlaceId(id1, "place-1", "encoded-icon-1", "fake-address-1", null, null, "tag-1");
        checkPlaceId(id2, "place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
    }

    @Test
    public void updatePlace() throws Exception {
        long id1 = insertPlace("place-1", "encoded-icon-1", "fake-address-1", null, null, "tag-1");
        long id2 = insertPlace("place-2", "encoded-icon-2", "fake-address-2", 7.3467, 8.364, "tag-2");
        // now update place 2
        assertEquals(1, updatePlace(id2, "place-2-edited", "encoded-icon-2-edited", "fake-address-2-edited", 2.354, 1.783, "tag-2-edited"));
        // check places
        checkPlaceId(id1, "place-1", "encoded-icon-1", "fake-address-1", null, null, "tag-1");
        checkPlaceId(id2, "place-2-edited", "encoded-icon-2-edited", "fake-address-2-edited", 2.354, 1.783, "tag-2-edited");
    }

    @Test
    public void deletePlace() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id4 = insertPlace("place-1", "encoded-icon-1", "fake-address-1", null, null, "tag-1");
        // now create one element for every item that can be linked to this place
        Date date = new Date();
        long id5 = insertDebt(Contract.DebtType.DEBT.getValue(), "icon", "desc", date, null, id1, null, id4, 10000L, false, null, null, false);
        long id6 = insertTransaction(10, date, "desc", id3, Contract.Direction.INCOME, 0, id1, id4, null, null, null, null, true, true, null, null, null);
        long id7 = insertTransfer("desc", date, id1, id2, null, 10, 10, 0, null, id4, null, true, true, null, null, null);
        long id8 = insertTransactionModel(10, "desc", id3, Contract.Direction.INCOME, id1, id4, null, null, true, true, null);
        long id9 = insertTransferModel("desc", id1, id2, 10, 10, 0, null, id4, null, true, true, null);
        // now delete the place
        assertEquals(1, mDatabase.deletePlace(id4));
        // check if items has no place as expected
        checkCursorSize(mDatabase.getPlaces(null, null, null, null), 0);
        checkDebtId(id5, Contract.DebtType.DEBT.getValue(), "icon", "desc", date, null, id1, null, null, 10000L, false, null, null);
        checkTransactionId(id6, 10, date, "desc", id3, Contract.Direction.INCOME, 0, id1, null, null, null, null, null, true, true, null, null);
        checkTransferId(id7, "desc", date, id1, id2, null, 10, 10, 0, null, null, null, true, true, null, null);
        checkTransactionModelId(id8, 10, "desc", id3, Contract.Direction.INCOME, id1, null, null, null, true, true, null);
        checkTransferModelId(id9, "desc", id1, id2, 10, 10, 0, null, null, null, true, true, null);
    }

    @Test
    public void insertPerson() throws Exception {
        long id1 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id2 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        checkPersonId(id1, "person-1", "encoded-icon-1", "note-1", "tag-1");
        checkPersonId(id2, "person-2", "encoded-icon-2", "note-2", "tag-2");
    }

    @Test
    public void updatePerson() throws Exception {
        long id1 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id2 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        // now update person 2
        assertEquals(1, updatePerson(id2, "person-2-edited", "encoded-icon-2-edited", "note-2-edited", "tag-2-edited"));
        // check people
        checkPersonId(id1, "person-1", "encoded-icon-1", "note-1", "tag-1");
        checkPersonId(id2, "person-2-edited", "encoded-icon-2-edited", "note-2-edited", "tag-2-edited");
    }

    @Test
    public void deletePerson() throws Exception {
        long id1 = insertWallet("Test wallet 1", "encoded-icon-1", "EUR", "note-wallet-1", true, 2000L, false, "tag-wallet-1");
        long id2 = insertWallet("Test wallet 2", "encoded-icon-2", "EUR", "note-wallet-2", true, 3000L, true, "tag-wallet-2");
        long id3 = insertCategory("Test category 1", "encoded-icon-1", 0, null, true, "tag-1");
        long id4 = insertPerson("person-1", "encoded-icon-1", "note-1", "tag-1");
        long id5 = insertPerson("person-2", "encoded-icon-2", "note-2", "tag-2");
        long id6 = insertPerson("person-3", "encoded-icon-3", "note-3", "tag-3");
        // now attach the person 5 to every item that supports it
        Date date = new Date();
        long id7 = insertDebt(Contract.DebtType.DEBT.getValue(), "icon", "desc", date, null, id1, null, null, 10000L, false, new Long[] {id5, id6}, null, false);
        long id8 = insertTransaction(10, date, "desc", id3, Contract.Direction.INCOME, 0, id1, null, null, null, null, null, true, true, new Long[] {id4, id5}, null, null);
        long id9 = insertTransfer("desc", date, id1, id2, null, 10, 10, 0, null, null, null, true, true, new Long[] {id5}, null, null);
        // now delete the person 5
        assertEquals(1, mDatabase.deletePerson(id5));
        checkCursorSize(mDatabase.getPeople(null, null, null, null), 2);
        // now check that every item has no more person 5 in list
        checkDebtId(id7, Contract.DebtType.DEBT.getValue(), "icon", "desc", date, null, id1, null, null, 10000L, false, new Long[] {id6}, null);
        checkTransactionId(id8, 10, date, "desc", id3, Contract.Direction.INCOME, 0, id1, null, null, null, null, null, true, true, new Long[] {id4}, null);
        checkTransferId(id9, "desc", date, id1, id2, null, 10, 10, 0, null, null, null, true, true, null, null);
    }

    @Test
    public void insertAttachment() throws Exception {
        long id1 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id2 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        checkAttachmentId(id1, "path1", "name-1", "mime-type-1", 90L, "tag-1");
        checkAttachmentId(id2, "path2", "name-2", "mime-type-2", 4560L, "tag-2");
    }

    @Test
    public void updateAttachment() throws Exception {
        long id1 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id2 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        // now update attachment 2
        assertEquals(1, updateAttachment(id2, "path2-edited", "name-2-edited", "mime-type-2-edited", 3647L, "tag-2-edited"));
        // check attachments
        checkAttachmentId(id1, "path1", "name-1", "mime-type-1", 90L, "tag-1");
        checkAttachmentId(id2, "path2-edited", "name-2-edited", "mime-type-2-edited", 3647L, "tag-2-edited");
    }

    @Test
    public void deleteAttachment() throws Exception {
        long id1 = insertAttachment("path1", "name-1", "mime-type-1", 90L, "tag-1");
        long id2 = insertAttachment("path2", "name-2", "mime-type-2", 4560L, "tag-2");
        checkCursorSize(mDatabase.getAttachments(null, null, null, null), 2);
        assertEquals(1, mDatabase.deleteAttachment(id1));
        checkCursorSize(mDatabase.getAttachments(null, null, null, null), 1);
        assertEquals(1, mDatabase.deleteAttachment(id2));
        checkCursorSize(mDatabase.getAttachments(null, null, null, null), 0);
    }

}