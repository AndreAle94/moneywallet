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

package com.oriondev.moneywallet.ui.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Attachment;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.model.Person;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.AttachmentPicker;
import com.oriondev.moneywallet.picker.CategoryPicker;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.EventPicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.picker.PersonPicker;
import com.oriondev.moneywallet.picker.PlacePicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.AttachmentView;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by andrea on 06/03/18.
 */
public class NewEditTransactionActivity extends NewEditItemActivity implements MoneyPicker.Controller,
                                                                            CategoryPicker.Controller,
                                                                            DateTimePicker.Controller,
                                                                            WalletPicker.SingleWalletController,
                                                                            EventPicker.Controller,
                                                                            PersonPicker.Controller,
                                                                            PlacePicker.Controller,
                                                                            AttachmentPicker.Controller, AttachmentView.Controller {

    public static final String TYPE = "NewEditTransactionActivity::Type";
    public static final String DEBT_ID = "NewEditTransactionActivity::DebtId";
    public static final String DEBT_ACTION = "NewEditTransactionActivity::DebtAction";
    public static final String SAVING_ID = "NewEditTransactionActivity::SavingId";
    public static final String SAVING_ACTION = "NewEditTransactionActivity::SavingAction";
    public static final String MODEL_ID = "NewEditTransactionActivity::ModelId";

    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_TRANSFER = 1;
    public static final int TYPE_DEBT = 2;
    public static final int TYPE_SAVING = 3;
    public static final int TYPE_MODEL = 4;

    public static final int DEBT_PAY = 1;
    public static final int DEBT_RECEIVE = 2;

    public static final int SAVING_DEPOSIT = 1;
    public static final int SAVING_WITHDRAW = 2;
    public static final int SAVING_WITHDRAW_EVERYTHING = 3;

    private static final String TAG_MONEY_PICKER = "NewEditTransactionActivity::Tag::MoneyPicker";
    private static final String TAG_CATEGORY_PICKER = "NewEditTransactionActivity::Tag::CategoryPicker";
    private static final String TAG_DATETIME_PICKER = "NewEditTransactionActivity::Tag::DateTimePicker";
    private static final String TAG_WALLET_PICKER = "NewEditTransactionActivity::Tag::WalletPicker";
    private static final String TAG_EVENT_PICKER = "NewEditTransactionActivity::Tag::EventPicker";
    private static final String TAG_PLACE_PICKER = "NewEditTransactionActivity::Tag::PlacePicker";
    private static final String TAG_PERSON_PICKER = "NewEditTransactionActivity::Tag::PersonPicker";
    private static final String TAG_ATTACHMENT_PICKER = "NewEditTransactionActivity::Tag::AttachmentPicker";

    private static final String SS_TYPE = "NewEditTransactionActivity::SavedState::Type";
    private static final String SS_DEBT_ID = "NewEditTransactionActivity::SavedState::DebtId";
    private static final String SS_SAVING_ID = "NewEditTransactionActivity::SavedState::SavingId";
    private static final String SS_SAVING_COMPLETED = "NewEditTransactionActivity::SavedState::SavingCompleted";

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private MaterialEditText mDescriptionEditText;
    private MaterialEditText mCategoryEditText;
    private MaterialEditText mDateEditText;
    private MaterialEditText mTimeEditText;
    private MaterialEditText mWalletEditText;
    private MaterialEditText mEventEditText;
    private MaterialEditText mPeopleEditText;
    private MaterialEditText mPlaceEditText;
    private MaterialEditText mNoteEditText;
    private CheckBox mConfirmedCheckBox;
    private CheckBox mCountInTotalCheckBox;
    private AttachmentView mAttachmentView;

    private MoneyPicker mMoneyPicker;
    private CategoryPicker mCategoryPicker;
    private DateTimePicker mDateTimePicker;
    private WalletPicker mWalletPicker;
    private EventPicker mEventPicker;
    private PlacePicker mPlacePicker;
    private PersonPicker mPersonPicker;
    private AttachmentPicker mAttachmentPicker;

    private int mType;
    private Long mDebtId = null;
    private Long mSavingId = null;
    private boolean mSavingCompleted = false;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_money_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
        // attach a listener to the views
        mMoneyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_transaction, parent, true);
        mDescriptionEditText = view.findViewById(R.id.description_edit_text);
        mCategoryEditText = view.findViewById(R.id.category_edit_text);
        mDateEditText = view.findViewById(R.id.date_edit_text);
        mTimeEditText = view.findViewById(R.id.time_edit_text);
        mWalletEditText = view.findViewById(R.id.wallet_edit_text);
        mEventEditText = view.findViewById(R.id.event_edit_text);
        mPeopleEditText = view.findViewById(R.id.people_edit_text);
        mPlaceEditText = view.findViewById(R.id.place_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        mConfirmedCheckBox = view.findViewById(R.id.confirmed_checkbox);
        mCountInTotalCheckBox = view.findViewById(R.id.count_in_total_checkbox);
        mAttachmentView = view.findViewById(R.id.attachment_view);
        // disable unused edit texts
        mCategoryEditText.setTextViewMode(true);
        mDateEditText.setTextViewMode(true);
        mTimeEditText.setTextViewMode(true);
        mWalletEditText.setTextViewMode(true);
        mEventEditText.setTextViewMode(true);
        mPeopleEditText.setTextViewMode(true);
        mPlaceEditText.setTextViewMode(true);
        // add validators
        mDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_date);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mDateTimePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mCategoryEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_category);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mCategoryPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_wallet);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        // attach listeners
        mCategoryEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategoryPicker.showPicker();
            }

        });
        mDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDateTimePicker.showDatePicker();
            }

        });
        mTimeEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDateTimePicker.showTimePicker();
            }

        });
        mWalletEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletPicker.showSingleWalletPicker();
            }

        });
        mEventEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEventPicker.showPicker(mDateTimePicker.getCurrentDateTime());
            }

        });
        mEventEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mEventPicker.setCurrentEvent(null);
                return false;
            }

        });
        mPeopleEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPersonPicker.showPicker();
            }

        });
        mPeopleEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mPersonPicker.setPeople(null);
                return false;
            }

        });
        mPlaceEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPlacePicker.showPicker();
            }

        });
        mPlaceEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mPlacePicker.setCurrentPlace(null);
                return false;
            }

        });
        mAttachmentView.setController(this);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        long money = 0L;
        Category category = null;
        Date datetime = null;
        Wallet wallet = null;
        Event event = null;
        Person[] people = null;
        Place place = null;
        ArrayList<Attachment> attachments = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSACTIONS, getItemId());
                String[] projection = new String[] {
                        Contract.Transaction.MONEY,
                        Contract.Transaction.DATE,
                        Contract.Transaction.DESCRIPTION,
                        Contract.Transaction.CATEGORY_ID,
                        Contract.Transaction.CATEGORY_NAME,
                        Contract.Transaction.CATEGORY_ICON,
                        Contract.Transaction.CATEGORY_TYPE,
                        Contract.Transaction.CATEGORY_TAG,
                        Contract.Transaction.CATEGORY_SHOW_REPORT,
                        Contract.Transaction.TYPE,
                        Contract.Transaction.WALLET_ID,
                        Contract.Transaction.WALLET_NAME,
                        Contract.Transaction.WALLET_ICON,
                        Contract.Transaction.WALLET_CURRENCY,
                        Contract.Transaction.PLACE_ID,
                        Contract.Transaction.PLACE_NAME,
                        Contract.Transaction.PLACE_ICON,
                        Contract.Transaction.PLACE_ADDRESS,
                        Contract.Transaction.PLACE_LATITUDE,
                        Contract.Transaction.PLACE_LONGITUDE,
                        Contract.Transaction.NOTE,
                        Contract.Transaction.EVENT_ID,
                        Contract.Transaction.EVENT_NAME,
                        Contract.Transaction.EVENT_ICON,
                        Contract.Transaction.EVENT_NOTE,
                        Contract.Transaction.EVENT_START_DATE,
                        Contract.Transaction.EVENT_END_DATE,
                        Contract.Transaction.SAVING_ID,
                        Contract.Transaction.DEBT_ID,
                        Contract.Transaction.CONFIRMED,
                        Contract.Transaction.COUNT_IN_TOTAL
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        money = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY));
                        datetime = DateUtils.getDateFromSQLDateTimeString(cursor.getString(cursor.getColumnIndex(Contract.Transaction.DATE)));
                        mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Transaction.DESCRIPTION)));
                        category = new Category(
                                cursor.getLong(cursor.getColumnIndex(Contract.Transaction.CATEGORY_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.Transaction.CATEGORY_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transaction.CATEGORY_ICON))),
                                Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Transaction.CATEGORY_TYPE))),
                                cursor.getString(cursor.getColumnIndex(Contract.Transaction.CATEGORY_TAG))
                        );
                        mType = cursor.getInt(cursor.getColumnIndex(Contract.Transaction.TYPE));
                        wallet = new Wallet(
                                cursor.getLong(cursor.getColumnIndex(Contract.Transaction.WALLET_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_ICON))),
                                CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY))),
                                0L,0L
                        );
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transaction.PLACE_ID))) {
                            place = new Place(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Transaction.PLACE_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transaction.PLACE_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transaction.PLACE_ICON))),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transaction.PLACE_ADDRESS)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.Transaction.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Transaction.PLACE_LATITUDE)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.Transaction.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Transaction.PLACE_LONGITUDE))
                            );
                        }
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Transaction.NOTE)));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transaction.EVENT_ID))) {
                            event = new Event(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Transaction.EVENT_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_ICON))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_START_DATE))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_END_DATE)))
                            );
                        }
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transaction.DEBT_ID))) {
                            mDebtId = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.DEBT_ID));
                        }
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transaction.SAVING_ID))) {
                            mSavingId = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.SAVING_ID));
                        }
                        mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Transaction.CONFIRMED)) == 1);
                        mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Transaction.COUNT_IN_TOTAL)) == 1);
                    }
                    cursor.close();
                }
                // before continuing, check if the transaction is part of a transfer
                if (mType == TYPE_TRANSFER) {
                    uri = DataContentProvider.CONTENT_TRANSFERS;
                    projection = new String[] {Contract.Transfer.ID};
                    String selection = Contract.Transfer.TRANSACTION_FROM_ID + " = ? OR " +
                                       Contract.Transfer.TRANSACTION_TO_ID + " = ? OR " +
                                       Contract.Transfer.TRANSACTION_TAX_ID + " = ?";
                    String[] selectionArgs = new String[] {String.valueOf(getItemId()),
                            String.valueOf(getItemId()), String.valueOf(getItemId())};
                    cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                long transferId = cursor.getLong(cursor.getColumnIndex(Contract.Transfer.ID));
                                Intent intent = new Intent(this, NewEditTransferActivity.class);
                                intent.putExtra(NewEditTransferActivity.MODE, Mode.EDIT_ITEM);
                                intent.putExtra(NewEditTransferActivity.ID, transferId);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                }
                // the previous cursor contains only a column with the list of ids of linked people.
                // we need instead to buildMaterialDialog the full person object so we must perform a separated
                // query to the database to obtain the full cursor.
                Uri peopleUri = Uri.withAppendedPath(uri, "people");
                projection = new String[] {
                        Contract.Person.ID,
                        Contract.Person.NAME,
                        Contract.Person.ICON
                };
                cursor = contentResolver.query(peopleUri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        people = new Person[cursor.getCount()];
                        for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                            people[i] = new Person(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Person.ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Person.NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Person.ICON)))
                            );
                        }
                    }
                    cursor.close();
                }
                // load all attachments
                attachments = new ArrayList<>();
                Uri attachmentsUri = Uri.withAppendedPath(uri, "attachments");
                projection = new String[] {
                        Contract.Attachment.ID,
                        Contract.Attachment.FILE,
                        Contract.Attachment.NAME,
                        Contract.Attachment.TYPE,
                        Contract.Attachment.SIZE
                };
                cursor = contentResolver.query(attachmentsUri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                            Attachment attachment = new Attachment(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Attachment.ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Attachment.FILE)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Attachment.NAME)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Attachment.TYPE)),
                                    cursor.getLong(cursor.getColumnIndex(Contract.Attachment.SIZE))
                            );
                            attachments.add(attachment);
                        }
                    }
                    cursor.close();
                }
            } else {
                Intent intent = getIntent();
                mType = intent.getIntExtra(TYPE, TYPE_STANDARD);
                if (mType == TYPE_STANDARD) {
                    String[] projection = new String[] {
                            Contract.Wallet.ID,
                            Contract.Wallet.NAME,
                            Contract.Wallet.ICON,
                            Contract.Wallet.CURRENCY,
                            Contract.Wallet.START_MONEY,
                            Contract.Wallet.TOTAL_MONEY
                    };
                    long currentWallet = PreferenceManager.getCurrentWallet();
                    Cursor cursor;
                    if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                        Uri uri = DataContentProvider.CONTENT_WALLETS;
                        cursor = contentResolver.query(uri, projection, null, null, null);
                    } else {
                        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, currentWallet);
                        cursor = contentResolver.query(uri, projection, null, null, null);
                    }
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            wallet = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY))),
                                    cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY)),
                                    cursor.getLong(cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY))
                            );
                        }
                        cursor.close();
                    }
                } else if (mType == TYPE_TRANSFER) {
                    // In this case the activity has been launched to insert a new transfer so we
                    // have to simply start the correct activity and finish the current one.
                    startActivity(new Intent(this, NewEditTransferActivity.class));
                    finish();
                } else if (mType == TYPE_DEBT) {
                    mDebtId = intent.getLongExtra(DEBT_ID, 0L);
                    Contract.DebtType debtType = null;
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, mDebtId);
                    String[] projection = new String[] {
                            Contract.Debt.TYPE,
                            Contract.Debt.WALLET_ID,
                            Contract.Debt.WALLET_NAME,
                            Contract.Debt.WALLET_ICON,
                            Contract.Debt.WALLET_CURRENCY,
                            Contract.Debt.PLACE_ID,
                            Contract.Debt.PLACE_NAME,
                            Contract.Debt.PLACE_ICON,
                            Contract.Debt.PLACE_ADDRESS,
                            Contract.Debt.PLACE_LATITUDE,
                            Contract.Debt.PLACE_LONGITUDE,
                    };
                    Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            debtType = Contract.DebtType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Debt.TYPE)));
                            wallet = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Debt.WALLET_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Debt.WALLET_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Debt.WALLET_ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Debt.WALLET_CURRENCY))),
                                    0L,0L
                            );
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.Debt.PLACE_ID))) {
                                place = new Place(
                                        cursor.getLong(cursor.getColumnIndex(Contract.Debt.PLACE_ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.Debt.PLACE_NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Debt.PLACE_ICON))),
                                        cursor.getString(cursor.getColumnIndex(Contract.Debt.PLACE_ADDRESS)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.Debt.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Debt.PLACE_LATITUDE)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.Debt.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Debt.PLACE_LONGITUDE))
                                );
                            }
                        }
                        cursor.close();
                    }
                    // the previous cursor contains only a column with the list of ids of linked people.
                    // we need instead to buildMaterialDialog the full person object so we must perform a separated
                    // query to the database to obtain the full cursor.
                    uri = Uri.withAppendedPath(uri, "people");
                    projection = new String[] {
                            Contract.Person.ID,
                            Contract.Person.NAME,
                            Contract.Person.ICON
                    };
                    cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            people = new Person[cursor.getCount()];
                            for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                                people[i] = new Person(
                                        cursor.getLong(cursor.getColumnIndex(Contract.Person.ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.Person.NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Person.ICON)))
                                );
                            }
                        }
                        cursor.close();
                    }
                    // load the category associated with this debt
                    uri = DataContentProvider.CONTENT_CATEGORIES;
                    projection = new String[] {
                            Contract.Category.ID,
                            Contract.Category.NAME,
                            Contract.Category.ICON,
                            Contract.Category.TYPE,
                            Contract.Category.TAG
                    };
                    String where = Contract.Category.TAG + " = ?";
                    String[] whereArgs = new String[1];
                    if (debtType == null) {
                        switch (intent.getIntExtra(DEBT_ACTION, 0)) {
                            case DEBT_PAY:
                                debtType = Contract.DebtType.DEBT;
                                break;
                            case DEBT_RECEIVE:
                                debtType = Contract.DebtType.CREDIT;
                                break;
                        }
                    }
                    if (debtType != null) {
                        switch (debtType) {
                            case DEBT:
                                whereArgs[0] = Contract.CategoryTag.PAID_DEBT;
                                break;
                            case CREDIT:
                                whereArgs[0] = Contract.CategoryTag.PAID_CREDIT;
                                break;
                        }
                        cursor = contentResolver.query(uri, projection, where, whereArgs, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                category = new Category(
                                        cursor.getLong(cursor.getColumnIndex(Contract.Category.ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON))),
                                        Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE))),
                                        cursor.getString(cursor.getColumnIndex(Contract.Category.TAG))
                                );
                            }
                            cursor.close();
                        }
                    }
                } else if (mType == TYPE_SAVING) {
                    mSavingId = intent.getLongExtra(SAVING_ID, 0L);
                    long savingMoney = 0L;
                    long savingProgress = 0L;
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                    String[] projection = new String[] {
                            Contract.Saving.END_MONEY,
                            Contract.Saving.WALLET_ID,
                            Contract.Saving.WALLET_NAME,
                            Contract.Saving.WALLET_ICON,
                            Contract.Saving.WALLET_CURRENCY
                    };
                    Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            savingMoney = cursor.getLong(cursor.getColumnIndex(Contract.Saving.END_MONEY));
                            wallet = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Saving.WALLET_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Saving.WALLET_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Saving.WALLET_ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Saving.WALLET_CURRENCY))),
                                    0L, 0L
                            );
                        }
                        cursor.close();
                    }
                    uri = DataContentProvider.CONTENT_CATEGORIES;
                    projection = new String[] {
                            Contract.Category.ID,
                            Contract.Category.NAME,
                            Contract.Category.ICON,
                            Contract.Category.TYPE,
                            Contract.Category.TAG
                    };
                    String selection = Contract.Category.TAG + " = ?";
                    String[] selectionArgs = new String[1];
                    int action = intent.getIntExtra(SAVING_ACTION, 0);
                    switch (action) {
                        case SAVING_DEPOSIT:
                            selectionArgs[0] = Contract.CategoryTag.SAVING_DEPOSIT;
                            break;
                        case SAVING_WITHDRAW_EVERYTHING:
                            money = savingMoney;
                            mSavingCompleted = true;
                        case SAVING_WITHDRAW:
                            selectionArgs[0] = Contract.CategoryTag.SAVING_WITHDRAW;
                            break;
                    }
                    cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            category = new Category(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Category.ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON))),
                                    Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE))),
                                    cursor.getString(cursor.getColumnIndex(Contract.Category.TAG))
                            );
                        }
                        cursor.close();
                    }
                } else if (mType == TYPE_MODEL) {
                    long modelId = intent.getLongExtra(MODEL_ID, 0L);
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSACTION_MODELS, modelId);
                    String[] projection = new String[] {
                            Contract.TransactionModel.MONEY,
                            Contract.TransactionModel.DESCRIPTION,
                            Contract.TransactionModel.CATEGORY_ID,
                            Contract.TransactionModel.CATEGORY_NAME,
                            Contract.TransactionModel.CATEGORY_ICON,
                            Contract.TransactionModel.CATEGORY_TYPE,
                            Contract.TransactionModel.CATEGORY_SHOW_REPORT,
                            Contract.TransactionModel.DIRECTION,
                            Contract.TransactionModel.WALLET_ID,
                            Contract.TransactionModel.WALLET_NAME,
                            Contract.TransactionModel.WALLET_ICON,
                            Contract.TransactionModel.WALLET_CURRENCY,
                            Contract.TransactionModel.PLACE_ID,
                            Contract.TransactionModel.PLACE_NAME,
                            Contract.TransactionModel.PLACE_ICON,
                            Contract.TransactionModel.PLACE_ADDRESS,
                            Contract.TransactionModel.PLACE_LATITUDE,
                            Contract.TransactionModel.PLACE_LONGITUDE,
                            Contract.TransactionModel.NOTE,
                            Contract.TransactionModel.EVENT_ID,
                            Contract.TransactionModel.EVENT_NAME,
                            Contract.TransactionModel.EVENT_ICON,
                            Contract.TransactionModel.EVENT_START_DATE,
                            Contract.TransactionModel.EVENT_END_DATE,
                            Contract.TransactionModel.CONFIRMED,
                            Contract.TransactionModel.COUNT_IN_TOTAL
                    };
                    Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            money = cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.MONEY));
                            mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.DESCRIPTION)));
                            category = new Category(
                                    cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_ICON))),
                                    Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_TYPE)))
                            );
                            wallet = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.WALLET_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.WALLET_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.WALLET_ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.WALLET_CURRENCY))),
                                    0L, 0L
                            );
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ID))) {
                                place = new Place(
                                        cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.PLACE_NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ICON))),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ADDRESS)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.TransactionModel.PLACE_LATITUDE)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.TransactionModel.PLACE_LONGITUDE))
                                );
                            }
                            mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.NOTE)));
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.EVENT_ID))) {
                                event = new Event(
                                        cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.EVENT_ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.EVENT_NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.EVENT_ICON))),
                                        DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.EVENT_START_DATE))),
                                        DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.EVENT_END_DATE)))
                                );
                            }
                            mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.CONFIRMED)) == 1);
                            mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.COUNT_IN_TOTAL)) == 1);
                        }
                        cursor.close();
                    }
                }
                datetime = new Date();
            }
        } else {
            mType = savedInstanceState.getInt(SS_TYPE, TYPE_STANDARD);
            mDebtId = savedInstanceState.containsKey(SS_DEBT_ID) ? savedInstanceState.getLong(SS_DEBT_ID) : null;
            mSavingId = savedInstanceState.containsKey(SS_SAVING_ID) ? savedInstanceState.getLong(SS_SAVING_ID) : null;
            mSavingCompleted = savedInstanceState.getBoolean(SS_SAVING_COMPLETED, false);
        }
        // depending on the type we must hide pickers that are now allowed to be changed
        switch (mType) {
            case TYPE_DEBT:
            case TYPE_SAVING:
                mCategoryEditText.setVisibility(View.GONE);
                break;
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, money);
        mCategoryPicker = CategoryPicker.createPicker(fragmentManager, TAG_CATEGORY_PICKER, category);
        mDateTimePicker = DateTimePicker.createPicker(fragmentManager, TAG_DATETIME_PICKER, datetime);
        mWalletPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_PICKER, wallet);
        mEventPicker = EventPicker.createPicker(fragmentManager, TAG_EVENT_PICKER, event);
        mPersonPicker = PersonPicker.createPicker(fragmentManager, TAG_PERSON_PICKER, people);
        mPlacePicker = PlacePicker.createPicker(fragmentManager, TAG_PLACE_PICKER, place);
        mAttachmentPicker = AttachmentPicker.createPicker(fragmentManager, TAG_ATTACHMENT_PICKER, attachments);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_TYPE, mType);
        if (mDebtId != null) {
            outState.putLong(SS_DEBT_ID, mDebtId);
        }
        if (mSavingId != null) {
            outState.putLong(SS_SAVING_ID, mSavingId);
        }
        outState.putBoolean(SS_SAVING_COMPLETED, mSavingCompleted);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_transaction;
            case EDIT_ITEM:
                return R.string.title_activity_edit_transaction;
            default:
                return -1;
        }
    }

    @Override
    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_new_edit_item_with_attachment;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_attach_file:
                mAttachmentPicker.showPicker();
                return false;
            default:
                return super.onMenuItemClick(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAttachmentPicker.cleanUp(true);
    }

    private boolean validate() {
        if (mDateEditText.validate() && mCategoryEditText.validate() && mWalletEditText.validate()) {
            if (mAttachmentPicker.areAllAttachmentsReady()) {
                return true;
            } else {
                // TODO show error: wait for attachment load competition
            }
        }
        return false;
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Transaction.MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(mDateTimePicker.getCurrentDateTime()));
            contentValues.put(Contract.Transaction.DESCRIPTION, mDescriptionEditText.getTextAsString());
            contentValues.put(Contract.Transaction.CATEGORY_ID, mCategoryPicker.getCurrentCategory().getId());
            contentValues.put(Contract.Transaction.DIRECTION, mCategoryPicker.getCurrentCategory().getDirection());
            contentValues.put(Contract.Transaction.TYPE, mType);
            contentValues.put(Contract.Transaction.WALLET_ID, mWalletPicker.getCurrentWallet().getId());
            contentValues.put(Contract.Transaction.PLACE_ID, mPlacePicker.isSelected() ? mPlacePicker.getCurrentPlace().getId() : null);
            contentValues.put(Contract.Transaction.NOTE, mNoteEditText.getTextAsString());
            contentValues.put(Contract.Transaction.EVENT_ID, mEventPicker.isSelected() ? mEventPicker.getCurrentEvent().getId() : null);
            contentValues.put(Contract.Transaction.SAVING_ID, mSavingId);
            contentValues.put(Contract.Transaction.DEBT_ID, mDebtId);
            contentValues.put(Contract.Transaction.CONFIRMED, mConfirmedCheckBox.isChecked());
            contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, mCountInTotalCheckBox.isChecked());
            contentValues.put(Contract.Transaction.PEOPLE_IDS, Contract.getObjectIds(mPersonPicker.getCurrentPeople()));
            contentValues.put(Contract.Transaction.ATTACHMENT_IDS, Contract.getObjectIds(mAttachmentPicker.getCurrentAttachments()));
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_TRANSACTIONS, contentValues);
                    if (mSavingId != null && mSavingCompleted) {
                        setSavingCompleted(contentResolver, mSavingId);
                    }
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSACTIONS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            mAttachmentPicker.cleanUp(false);
            setResult(RESULT_OK);
            finish();
        }
    }

    private void setSavingCompleted(ContentResolver contentResolver, long savingId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Saving.COMPLETE, true);
        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, savingId);
        contentResolver.update(uri, contentValues, null, null);
    }

    public static Uri insertTransactionFromModel(Context context, long modelId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSACTION_MODELS, modelId);
        String[] projection = new String[] {
                Contract.TransactionModel.MONEY,
                Contract.TransactionModel.DESCRIPTION,
                Contract.TransactionModel.CATEGORY_ID,
                Contract.TransactionModel.DIRECTION,
                Contract.TransactionModel.WALLET_ID,
                Contract.TransactionModel.PLACE_ID,
                Contract.TransactionModel.NOTE,
                Contract.TransactionModel.EVENT_ID,
                Contract.TransactionModel.CONFIRMED,
                Contract.TransactionModel.COUNT_IN_TOTAL
        };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            Uri resultUri = null;
            if (cursor.moveToFirst()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.Transaction.MONEY, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.MONEY)));
                contentValues.put(Contract.Transaction.DATE, DateUtils.getSQLDateTimeString(new Date()));
                contentValues.put(Contract.Transaction.DESCRIPTION, cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.DESCRIPTION)));
                contentValues.put(Contract.Transaction.CATEGORY_ID, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.CATEGORY_ID)));
                contentValues.put(Contract.Transaction.DIRECTION, cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.DIRECTION)));
                contentValues.put(Contract.Transaction.TYPE, Contract.TransactionType.STANDARD);
                contentValues.put(Contract.Transaction.WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.WALLET_ID)));
                contentValues.put(Contract.Transaction.PLACE_ID, cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ID)) ? null : cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.PLACE_ID)));
                contentValues.put(Contract.Transaction.NOTE, cursor.getString(cursor.getColumnIndex(Contract.TransactionModel.NOTE)));
                contentValues.put(Contract.Transaction.EVENT_ID, cursor.isNull(cursor.getColumnIndex(Contract.TransactionModel.EVENT_ID)) ? null : cursor.getLong(cursor.getColumnIndex(Contract.TransactionModel.EVENT_ID)));
                contentValues.put(Contract.Transaction.CONFIRMED, cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.CONFIRMED)));
                contentValues.put(Contract.Transaction.COUNT_IN_TOTAL, cursor.getInt(cursor.getColumnIndex(Contract.TransactionModel.COUNT_IN_TOTAL)));
                resultUri = contentResolver.insert(DataContentProvider.CONTENT_TRANSACTIONS, contentValues);
            }
            cursor.close();
            return resultUri;
        }
        return null;
    }

    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        if (currency != null) {
            mCurrencyTextView.setText(currency.getSymbol());
        } else {
            mCurrencyTextView.setText("?");
        }
        mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
    }

    @Override
    public void onCategoryChanged(String tag, Category category) {
        if (category != null) {
            mCategoryEditText.setText(category.getName());
        } else {
            mCategoryEditText.setText(null);
        }
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        if (date != null) {
            DateFormatter.applyDate(mDateEditText, date);
            DateFormatter.applyTime(mTimeEditText, date);
        } else {
            mDateEditText.setText(null);
            mTimeEditText.setText(null);
        }
    }

    @Override
    public void onWalletChanged(String tag, Wallet wallet) {
        if (wallet != null) {
            mWalletEditText.setText(wallet.getName());
            mMoneyPicker.setCurrency(wallet.getCurrency());
        } else {
            mWalletEditText.setText(null);
            mMoneyPicker.setCurrency(null);
        }
    }

    @Override
    public void onEventChanged(String tag, Event event) {
        if (event != null) {
            mEventEditText.setText(event.getName());
        } else {
            mEventEditText.setText(null);
        }
    }

    @Override
    public void onPeopleChanged(String tag, Person[] people) {
        if (people != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < people.length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(people[i].getName());
            }
            mPeopleEditText.setText(builder);
        } else {
            mPeopleEditText.setText(null);
        }
    }

    @Override
    public void onPlaceChanged(String tag, Place place) {
        if (place != null) {
            mPlaceEditText.setText(place.getName());
        } else {
            mPlaceEditText.setText(null);
        }
    }

    @Override
    public void onAttachmentListChanged(List<Attachment> attachments) {
        mAttachmentView.setVisibility(attachments.isEmpty() ? View.GONE : View.VISIBLE);
        mAttachmentView.setAttachments(attachments);
    }

    @Override
    public void onAttachmentClick(Attachment attachment) {
        Attachment.openAttachment(this, attachment);
    }

    @Override
    public void onAttachmentDelete(Attachment attachment) {
        mAttachmentPicker.remove(attachment);
    }
}