package com.oriondev.moneywallet.storage.database.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.oriondev.moneywallet.model.ColorIcon;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrea on 23/12/18.
 */
public abstract class AbstractDataImporter {

    private final Context mContext;

    public AbstractDataImporter(Context context, File file) throws IOException {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    public abstract void importData() throws IOException;

    protected void insertTransaction(String wallet, CurrencyUnit currencyUnit, String category,
                                     Date datetime, Long money, int direction, String description,
                                     String event, String place, String people, String note) {
        ContentResolver contentResolver = getContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        // the first step consists in checking if a wallet with the same name and currency already
        // exists in the database, if not, create it and keep the id as reference
        contentValues.put(Contract.Transaction.WALLET_ID, getOrCreateWallet(contentResolver, wallet, currencyUnit));
        // the second step consists in checking if a category with the same name and direction
        // already exists in the database, if not, create it and keep the id as reference
        contentValues.put(Contract.Transaction.CATEGORY_ID, getOrCreateCategory(contentResolver, category, direction));
        // the third step consists in adding the datetime, the money and the direction, the
        // description and the note related to this transaction
        contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(datetime));
        contentValues.put(Contract.Transaction.DIRECTION, direction);
        contentValues.put(Contract.Transaction.MONEY, money);
        contentValues.put(Contract.Transaction.DESCRIPTION, description);
        contentValues.put(Contract.Transaction.NOTE, note);
        // the fourth step consists in checking if the event has been provided,
        // if not, simply ignore it because we cannot know the date range
        contentValues.put(Contract.Transaction.EVENT_ID, getEvent(contentResolver, event));
        // the fifth step consists in checking if a place with the same name already
        // exists in the database, if not, create it and keep the id as reference
        contentValues.put(Contract.Transaction.PLACE_ID, getOrCreatePlace(contentResolver, place));
        // the sixth step consists in checking if a set of people with the same name already
        // exists in the database, if not, create them and keep the ids as reference
        contentValues.put(Contract.Transaction.PEOPLE_IDS, getOrCreatePeople(contentResolver, people));
        // the last step consists in inserting the entity inside the database
        contentValues.put(Contract.Transaction.TYPE, Contract.TransactionType.STANDARD);
        contentValues.put(Contract.Transaction.CONFIRMED, true);
        contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, true);
        contentResolver.insert(DataContentProvider.CONTENT_TRANSACTIONS, contentValues);
    }

    private long getOrCreateWallet(ContentResolver contentResolver, String name, CurrencyUnit currencyUnit) {
        Uri uri = DataContentProvider.CONTENT_WALLETS;
        String[] projection = new String[] {Contract.Wallet.ID};
        String selection = Contract.Wallet.NAME + " = ? AND " + Contract.Wallet.CURRENCY + " = ?";
        String[] selectionArgs = new String[] {name, currencyUnit.getIso()};
        String sortOrder = Contract.Wallet.ID + " DESC";
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID));
                }
            } finally {
                cursor.close();
            }
        }
        // if we reached this line, the wallet does not exists and we have to create it
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Wallet.NAME, name);
        contentValues.put(Contract.Wallet.ICON, generateRandomIcon(name));
        contentValues.put(Contract.Wallet.CURRENCY, currencyUnit.getIso());
        contentValues.put(Contract.Wallet.COUNT_IN_TOTAL, true);
        contentValues.put(Contract.Wallet.START_MONEY, 0L);
        contentValues.put(Contract.Wallet.ARCHIVED, false);
        Uri result = contentResolver.insert(uri, contentValues);
        if (result == null) {
            throw new RuntimeException("Failed to create the new wallet");
        }
        return ContentUris.parseId(result);
    }

    private long getOrCreateCategory(ContentResolver contentResolver, String name, int direction) {
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        Contract.CategoryType type = direction == Contract.Direction.INCOME ? Contract.CategoryType.INCOME : Contract.CategoryType.EXPENSE;
        String[] projection = new String[] {Contract.Category.ID};
        String selection = Contract.Category.NAME + " = ? AND (" + Contract.Category.TYPE + " = ? OR " + Contract.Category.TYPE + " = ?)";
        String[] selectionArgs = new String[] {name,
                String.valueOf(type.getValue()),
                String.valueOf(Contract.CategoryType.SYSTEM)
        };
        String sortOrder = Contract.Category.ID + " DESC";
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(Contract.Category.ID));
                }
            } finally {
                cursor.close();
            }
        }
        // if we reached this line, the category does not exists and we have to create it
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Category.NAME, name);
        contentValues.put(Contract.Category.ICON, generateRandomIcon(name));
        contentValues.put(Contract.Category.TYPE, type.getValue());
        contentValues.putNull(Contract.Category.PARENT);
        contentValues.put(Contract.Category.SHOW_REPORT, true);
        Uri result = contentResolver.insert(uri, contentValues);
        if (result == null) {
            throw new RuntimeException("Failed to create the new category");
        }
        return ContentUris.parseId(result);
    }

    private Long getEvent(ContentResolver contentResolver, String name) {
        if (!TextUtils.isEmpty(name)) {
            Uri uri = DataContentProvider.CONTENT_EVENTS;
            String[] projection = new String[] {Contract.Event.ID};
            String selection = Contract.Event.NAME + " = ?";
            String[] selectionArgs = new String[] {name};
            String sortOrder = Contract.Event.ID + " DESC";
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getLong(cursor.getColumnIndex(Contract.Event.ID));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }

    private Long getOrCreatePlace(ContentResolver contentResolver, String name) {
        if (!TextUtils.isEmpty(name)) {
            Uri uri = DataContentProvider.CONTENT_PLACES;
            String[] projection = new String[] {Contract.Place.ID};
            String selection = Contract.Place.NAME + " = ?";
            String[] selectionArgs = new String[] {name};
            String sortOrder = Contract.Place.ID + " DESC";
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getLong(cursor.getColumnIndex(Contract.Place.ID));
                    }
                } finally {
                    cursor.close();
                }
            }
            // if we reached this line, the place does not exists and we have to create it
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Place.NAME, name);
            contentValues.put(Contract.Place.ICON, generateRandomIcon(name));
            contentValues.putNull(Contract.Place.ADDRESS);
            contentValues.putNull(Contract.Place.LATITUDE);
            contentValues.putNull(Contract.Place.LONGITUDE);
            Uri result = contentResolver.insert(uri, contentValues);
            if (result == null) {
                throw new RuntimeException("Failed to create the new place");
            }
            return ContentUris.parseId(result);
        }
        return null;
    }

    private String getOrCreatePeople(ContentResolver contentResolver, String people) {
        if (!TextUtils.isEmpty(people)) {
            List<Long> peopleIds = new ArrayList<>();
            for (String person : people.split(",")) {
                String name = person.trim();
                if (!TextUtils.isEmpty(name)) {
                    boolean personFound = false;
                    Uri uri = DataContentProvider.CONTENT_PEOPLE;
                    String[] projection = new String[] {Contract.Person.ID};
                    String selection = Contract.Person.NAME + " = ?";
                    String[] selectionArgs = new String[] {name};
                    String sortOrder = Contract.Person.ID + " DESC";
                    Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                personFound = true;
                                peopleIds.add(cursor.getLong(cursor.getColumnIndex(Contract.Person.ID)));
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                    if (!personFound) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Contract.Person.NAME, name);
                        contentValues.put(Contract.Person.ICON, name);
                        contentValues.putNull(Contract.Person.NOTE);
                        Uri result = contentResolver.insert(uri, contentValues);
                        if (result == null) {
                            throw new RuntimeException("Failed to create the new person");
                        }
                        peopleIds.add(ContentUris.parseId(result));
                    }
                }
            }
            if (!peopleIds.isEmpty()) {
                StringBuilder peopleIdBuilder = new StringBuilder();
                for (int i = 0; i < peopleIds.size(); i++) {
                    if (i != 0) {
                        peopleIdBuilder.append(",");
                    }
                    peopleIdBuilder.append(String.format(Locale.ENGLISH, "<%d>", peopleIds.get(i)));
                }
                return peopleIdBuilder.toString();
            }
        }
        return null;
    }

    private String generateRandomIcon(String name) {
        int randomColor = Utils.getRandomMDColor();
        String iconText = IconPicker.getColorIconString(name);
        Icon icon = new ColorIcon(randomColor, iconText);
        return icon.toString();
    }

    public abstract void close() throws IOException;
}