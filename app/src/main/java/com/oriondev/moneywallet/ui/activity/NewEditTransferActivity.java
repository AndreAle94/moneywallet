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

import android.content.ActivityNotFoundException;
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
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.model.Person;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.AttachmentPicker;
import com.oriondev.moneywallet.picker.CurrencyConverterPicker;
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
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrea on 16/03/18.
 */
public class NewEditTransferActivity extends NewEditItemActivity  implements CurrencyConverterPicker.Controller,
                                                                            MoneyPicker.Controller,
                                                                            DateTimePicker.Controller,
                                                                            WalletPicker.SingleWalletController,
                                                                            EventPicker.Controller,
                                                                            PersonPicker.Controller,
                                                                            PlacePicker.Controller,
                                                                            AttachmentPicker.Controller, AttachmentView.Controller {

    public static final String TYPE = "NewEditTransferActivity::Type";
    public static final String MODEL_ID = "NewEditTransferActivity::ModelId";

    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_MODEL = 1;

    private static final String TAG_CONVERTER_PICKER = "NewEditTransferModelActivity::Tag::ConverterPicker";
    private static final String TAG_MONEY_PICKER = "NewEditTransferModelActivity::Tag::MoneyPicker";
    private static final String TAG_DATETIME_PICKER = "NewEditTransferModelActivity::Tag::DateTimePicker";
    private static final String TAG_WALLET_FROM_PICKER = "NewEditTransferModelActivity::Tag::WalletFromPicker";
    private static final String TAG_WALLET_TO_PICKER = "NewEditTransferModelActivity::Tag::WalletToPicker";
    private static final String TAG_TAX_PICKER = "NewEditTransferModelActivity::Tag::TaxPicker";
    private static final String TAG_EVENT_PICKER = "NewEditTransferModelActivity::Tag::EventPicker";
    private static final String TAG_PLACE_PICKER = "NewEditTransferModelActivity::Tag::PlacePicker";
    private static final String TAG_PERSON_PICKER = "NewEditTransferModelActivity::Tag::PersonPicker";
    private static final String TAG_ATTACHMENT_PICKER = "NewEditTransferModelActivity::Tag::AttachmentPicker";

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private TextView mExchangeRateTextView;
    private TextView mSecondaryMoneyTextView;
    private MaterialEditText mDescriptionEditText;
    private MaterialEditText mDateEditText;
    private MaterialEditText mTimeEditText;
    private MaterialEditText mWalletFromEditText;
    private MaterialEditText mWalletToEditText;
    private MaterialEditText mTaxEditText;
    private MaterialEditText mEventEditText;
    private MaterialEditText mPeopleEditText;
    private MaterialEditText mPlaceEditText;
    private MaterialEditText mNoteEditText;
    private CheckBox mConfirmedCheckBox;
    private CheckBox mCountInTotalCheckBox;
    private AttachmentView mAttachmentView;

    private CurrencyConverterPicker mConverterPicker;
    private MoneyPicker mMoneyPicker;
    private DateTimePicker mDateTimePicker;
    private WalletPicker mWalletFromPicker;
    private WalletPicker mWalletToPicker;
    private MoneyPicker mTaxPicker;
    private EventPicker mEventPicker;
    private PlacePicker mPlacePicker;
    private PersonPicker mPersonPicker;
    private AttachmentPicker mAttachmentPicker;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_money_multiple_currencies_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
        mExchangeRateTextView = view.findViewById(R.id.exchange_rate_text_view);
        mSecondaryMoneyTextView = view.findViewById(R.id.secondary_money_text_view);
        // attach a listener to the views
        mMoneyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
        mExchangeRateTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mConverterPicker.showPicker(mMoneyPicker.getCurrentMoney());
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_transfer, parent, true);
        mDescriptionEditText = view.findViewById(R.id.description_edit_text);
        mDateEditText = view.findViewById(R.id.date_edit_text);
        mTimeEditText = view.findViewById(R.id.time_edit_text);
        mWalletFromEditText = view.findViewById(R.id.wallet_from_edit_text);
        mWalletToEditText = view.findViewById(R.id.wallet_to_edit_text);
        mTaxEditText = view.findViewById(R.id.money_tax_edit_text);
        mEventEditText = view.findViewById(R.id.event_edit_text);
        mPeopleEditText = view.findViewById(R.id.people_edit_text);
        mPlaceEditText = view.findViewById(R.id.place_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        mConfirmedCheckBox = view.findViewById(R.id.confirmed_checkbox);
        mCountInTotalCheckBox = view.findViewById(R.id.count_in_total_checkbox);
        mAttachmentView = view.findViewById(R.id.attachment_view);
        // disable unused edit texts
        mDateEditText.setTextViewMode(true);
        mTimeEditText.setTextViewMode(true);
        mWalletFromEditText.setTextViewMode(true);
        mWalletToEditText.setTextViewMode(true);
        mTaxEditText.setTextViewMode(true);
        mEventEditText.setTextViewMode(true);
        mPeopleEditText.setTextViewMode(true);
        mPlaceEditText.setTextViewMode(true);
        // add validators
        mWalletFromEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_wallet);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletFromPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletToEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_wallet);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletToPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        // setup listeners
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
        mWalletFromEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletFromPicker.showSingleWalletPicker();
            }

        });
        mWalletToEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletToPicker.showSingleWalletPicker();
            }

        });
        mTaxEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTaxPicker.showPicker();
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
        long moneyFrom = 0L;
        double conversionRate = 0D;
        long tax = 0L;
        Date datetime = null;
        Wallet walletFrom = null;
        Wallet walletTo = null;
        Event event = null;
        Place place = null;
        Person[] people = null;
        ArrayList<Attachment> attachments = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSFERS, getItemId());
                String[] projection = new String[] {
                        Contract.Transfer.DESCRIPTION,
                        Contract.Transfer.DATE,
                        Contract.Transfer.TRANSACTION_FROM_WALLET_ID,
                        Contract.Transfer.TRANSACTION_FROM_WALLET_NAME,
                        Contract.Transfer.TRANSACTION_FROM_WALLET_ICON,
                        Contract.Transfer.TRANSACTION_FROM_WALLET_CURRENCY,
                        Contract.Transfer.TRANSACTION_TO_WALLET_ID,
                        Contract.Transfer.TRANSACTION_TO_WALLET_NAME,
                        Contract.Transfer.TRANSACTION_TO_WALLET_ICON,
                        Contract.Transfer.TRANSACTION_TO_WALLET_CURRENCY,
                        Contract.Transfer.TRANSACTION_FROM_MONEY,
                        Contract.Transfer.TRANSACTION_TO_MONEY,
                        Contract.Transfer.TRANSACTION_TAX_MONEY,
                        Contract.Transfer.NOTE,
                        Contract.Transfer.EVENT_ID,
                        Contract.Transfer.EVENT_NAME,
                        Contract.Transfer.EVENT_ICON,
                        Contract.Transfer.EVENT_START_DATE,
                        Contract.Transfer.EVENT_END_DATE,
                        Contract.Transfer.PLACE_ID,
                        Contract.Transfer.PLACE_NAME,
                        Contract.Transfer.PLACE_ICON,
                        Contract.Transfer.PLACE_ADDRESS,
                        Contract.Transfer.PLACE_LATITUDE,
                        Contract.Transfer.PLACE_LONGITUDE,
                        Contract.Transfer.CONFIRMED,
                        Contract.Transfer.COUNT_IN_TOTAL
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Transfer.DESCRIPTION)));
                        datetime = DateUtils.getDateFromSQLDateTimeString(cursor.getString(cursor.getColumnIndex(Contract.Transfer.DATE)));
                        walletFrom = new Wallet(
                                cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_ICON))),
                                CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_CURRENCY))),
                                0L, 0L
                        );
                        walletTo = new Wallet(
                                cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_ICON))),
                                CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_CURRENCY))),
                                0L, 0L
                        );
                        moneyFrom = cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_MONEY));
                        long moneyTo = cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_MONEY));
                        tax = cursor.getLong(cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TAX_MONEY));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transfer.PLACE_ID))) {
                            place = new Place(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Transfer.PLACE_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transfer.PLACE_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transfer.PLACE_ICON))),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transfer.PLACE_ADDRESS)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.Transfer.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Transfer.PLACE_LATITUDE)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.Transfer.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.Transfer.PLACE_LONGITUDE))
                            );
                        }
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Transfer.NOTE)));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Transfer.EVENT_ID))) {
                            event = new Event(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Transfer.EVENT_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Transfer.EVENT_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Transfer.EVENT_ICON))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Transfer.EVENT_START_DATE))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Transfer.EVENT_END_DATE)))
                            );
                        }
                        conversionRate = (double) moneyTo / moneyFrom;
                        mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Transfer.CONFIRMED)) == 1);
                        mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Transfer.COUNT_IN_TOTAL)) == 1);
                    }
                    cursor.close();
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
                if (intent.getIntExtra(TYPE, TYPE_STANDARD) == TYPE_MODEL) {
                    long modelId = intent.getLongExtra(MODEL_ID, 0L);
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSFER_MODELS, modelId);
                    String[] projection = new String[] {
                            Contract.TransferModel.DESCRIPTION,
                            Contract.TransferModel.WALLET_FROM_ID,
                            Contract.TransferModel.WALLET_FROM_NAME,
                            Contract.TransferModel.WALLET_FROM_ICON,
                            Contract.TransferModel.WALLET_FROM_CURRENCY,
                            Contract.TransferModel.WALLET_TO_ID,
                            Contract.TransferModel.WALLET_TO_ID,
                            Contract.TransferModel.WALLET_TO_NAME,
                            Contract.TransferModel.WALLET_TO_ICON,
                            Contract.TransferModel.WALLET_TO_CURRENCY,
                            Contract.TransferModel.MONEY_FROM,
                            Contract.TransferModel.MONEY_TO,
                            Contract.TransferModel.MONEY_TAX,
                            Contract.TransferModel.NOTE,
                            Contract.TransferModel.EVENT_ID,
                            Contract.TransferModel.EVENT_NAME,
                            Contract.TransferModel.EVENT_ICON,
                            Contract.TransferModel.EVENT_START_DATE,
                            Contract.TransferModel.EVENT_END_DATE,
                            Contract.TransferModel.PLACE_ID,
                            Contract.TransferModel.PLACE_NAME,
                            Contract.TransferModel.PLACE_ICON,
                            Contract.TransferModel.PLACE_ADDRESS,
                            Contract.TransferModel.PLACE_LATITUDE,
                            Contract.TransferModel.PLACE_LONGITUDE,
                            Contract.TransferModel.CONFIRMED,
                            Contract.TransferModel.COUNT_IN_TOTAL
                    };
                    Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.DESCRIPTION)));
                            walletFrom = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_CURRENCY))),
                                    0L, 0L
                            );
                            walletTo = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_CURRENCY))),
                                    0L, 0L
                            );
                            moneyFrom = cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_FROM));
                            long moneyTo = cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TO));
                            tax = cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TAX));
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.PLACE_ID))) {
                                place = new Place(
                                        cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.PLACE_ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransferModel.PLACE_NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.PLACE_ICON))),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransferModel.PLACE_ADDRESS)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.TransferModel.PLACE_LATITUDE)),
                                        cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.TransferModel.PLACE_LONGITUDE))
                                );
                            }
                            mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.NOTE)));
                            if (!cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.EVENT_ID))) {
                                event = new Event(
                                        cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.EVENT_ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.TransferModel.EVENT_NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.EVENT_ICON))),
                                        DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.EVENT_START_DATE))),
                                        DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.TransferModel.EVENT_END_DATE)))
                                );
                            }
                            conversionRate = (double) moneyTo / moneyFrom;
                            mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.CONFIRMED)) == 1);
                            mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.COUNT_IN_TOTAL)) == 1);
                        }
                        cursor.close();
                    }
                } else {
                    String[] projection = new String[]{
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
                            walletFrom = new Wallet(
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
                }
                datetime = new Date();
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mConverterPicker = CurrencyConverterPicker.createPicker(fragmentManager, TAG_CONVERTER_PICKER, null, null, conversionRate);
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, moneyFrom);
        mTaxPicker = MoneyPicker.createPicker(fragmentManager, TAG_TAX_PICKER, null, tax);
        mDateTimePicker = DateTimePicker.createPicker(fragmentManager, TAG_DATETIME_PICKER, datetime);
        mWalletFromPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_FROM_PICKER, walletFrom);
        mWalletToPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_TO_PICKER, walletTo);
        mEventPicker = EventPicker.createPicker(fragmentManager, TAG_EVENT_PICKER, event);
        mPersonPicker = PersonPicker.createPicker(fragmentManager, TAG_PERSON_PICKER, people);
        mPlacePicker = PlacePicker.createPicker(fragmentManager, TAG_PLACE_PICKER, place);
        mAttachmentPicker = AttachmentPicker.createPicker(fragmentManager, TAG_ATTACHMENT_PICKER, attachments);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_transfer;
            case EDIT_ITEM:
                return R.string.title_activity_edit_transfer;
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
        if (mWalletFromEditText.validate() && mWalletToEditText.validate() && mConverterPicker.isReady()) {
            if (mAttachmentPicker.areAllAttachmentsReady()) {
                return true;
            } else {
                // TODO show error attachments not ready
            }
        }
        return false;
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Transfer.DESCRIPTION, mDescriptionEditText.getTextAsString());
            contentValues.put(Contract.Transfer.DATE, DateUtils.getSQLDateTimeString(mDateTimePicker.getCurrentDateTime()));
            contentValues.put(Contract.Transfer.TRANSACTION_FROM_WALLET_ID, mWalletFromPicker.getCurrentWallet().getId());
            contentValues.put(Contract.Transfer.TRANSACTION_TO_WALLET_ID, mWalletToPicker.getCurrentWallet().getId());
            contentValues.put(Contract.Transfer.TRANSACTION_TAX_WALLET_ID, mWalletFromPicker.getCurrentWallet().getId());
            contentValues.put(Contract.Transfer.TRANSACTION_FROM_MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Transfer.TRANSACTION_TO_MONEY, mConverterPicker.convert(mMoneyPicker.getCurrentMoney()));
            contentValues.put(Contract.Transfer.TRANSACTION_TAX_MONEY, mTaxPicker.getCurrentMoney());
            contentValues.put(Contract.Transfer.NOTE, mNoteEditText.getTextAsString());
            contentValues.put(Contract.Transfer.PLACE_ID, mPlacePicker.isSelected() ? mPlacePicker.getCurrentPlace().getId() : null);
            contentValues.put(Contract.Transfer.EVENT_ID, mEventPicker.isSelected() ? mEventPicker.getCurrentEvent().getId() : null);
            contentValues.put(Contract.Transfer.CONFIRMED, mConfirmedCheckBox.isChecked());
            contentValues.put(Contract.Transfer.COUNT_IN_TOTAL, mCountInTotalCheckBox.isChecked());
            contentValues.put(Contract.Transfer.PEOPLE_IDS, Contract.getObjectIds(mPersonPicker.getCurrentPeople()));
            contentValues.put(Contract.Transfer.ATTACHMENT_IDS, Contract.getObjectIds(mAttachmentPicker.getCurrentAttachments()));
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_TRANSFERS, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSFERS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            mAttachmentPicker.cleanUp(false);
            setResult(RESULT_OK);
            finish();
        }
    }

    public static Uri insertTransferFromModel(Context context, long modelId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_TRANSFER_MODELS, modelId);
        String[] projection = new String[] {
                Contract.TransferModel.DESCRIPTION,
                Contract.TransferModel.WALLET_FROM_ID,
                Contract.TransferModel.WALLET_TO_ID,
                Contract.TransferModel.MONEY_FROM,
                Contract.TransferModel.MONEY_TO,
                Contract.TransferModel.MONEY_TAX,
                Contract.TransferModel.NOTE,
                Contract.TransferModel.EVENT_ID,
                Contract.TransferModel.PLACE_ID,
                Contract.TransferModel.CONFIRMED,
                Contract.TransferModel.COUNT_IN_TOTAL
        };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            Uri resultUri = null;
            if (cursor.moveToFirst()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.Transfer.DESCRIPTION, cursor.getString(cursor.getColumnIndex(Contract.TransferModel.DESCRIPTION)));
                contentValues.put(Contract.Transfer.DATE, DateUtils.getSQLDateTimeString(new Date()));
                contentValues.put(Contract.Transfer.TRANSACTION_FROM_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_ID)));
                contentValues.put(Contract.Transfer.TRANSACTION_TO_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_ID)));
                contentValues.put(Contract.Transfer.TRANSACTION_TAX_WALLET_ID, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_ID)));
                contentValues.put(Contract.Transfer.TRANSACTION_FROM_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_FROM)));
                contentValues.put(Contract.Transfer.TRANSACTION_TO_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TO)));
                contentValues.put(Contract.Transfer.TRANSACTION_TAX_MONEY, cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.MONEY_TAX)));
                contentValues.put(Contract.Transfer.NOTE, cursor.getString(cursor.getColumnIndex(Contract.TransferModel.NOTE)));
                contentValues.put(Contract.Transfer.PLACE_ID, cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.PLACE_ID)) ? null : cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.PLACE_ID)));
                contentValues.put(Contract.Transfer.EVENT_ID, cursor.isNull(cursor.getColumnIndex(Contract.TransferModel.EVENT_ID)) ? null : cursor.getLong(cursor.getColumnIndex(Contract.TransferModel.EVENT_ID)));
                contentValues.put(Contract.Transfer.CONFIRMED, cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.CONFIRMED)));
                contentValues.put(Contract.Transfer.COUNT_IN_TOTAL, cursor.getInt(cursor.getColumnIndex(Contract.TransferModel.COUNT_IN_TOTAL)));
                resultUri = contentResolver.insert(DataContentProvider.CONTENT_TRANSFERS, contentValues);
            }
            cursor.close();
            return resultUri;
        }
        return null;
    }

    @Override
    public void onConversionRateChanged(String tag, CurrencyUnit currencyUnit1, CurrencyUnit currencyUnit2, Double conversionRate) {
        if (currencyUnit1 != null && currencyUnit2 != null && !currencyUnit1.equals(currencyUnit2)) {
            // we have two different currencies, its time to show the secondary currency line and do
            // a conversion on the fly with the provided conversion rate.
            mExchangeRateTextView.setText(String.format(Locale.getDefault(), "%s ➡ %s: %.2f", currencyUnit1.getSymbol(), currencyUnit2.getSymbol(), conversionRate));
            long convertedAmount = mConverterPicker.convert(mMoneyPicker.getCurrentMoney());
            mMoneyFormatter.applyNotTinted(mSecondaryMoneyTextView, currencyUnit2, convertedAmount);
            mExchangeRateTextView.setVisibility(View.VISIBLE);
            mSecondaryMoneyTextView.setVisibility(View.VISIBLE);
        } else {
            mExchangeRateTextView.setVisibility(View.GONE);
            mSecondaryMoneyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        switch (tag) {
            case TAG_MONEY_PICKER:
                if (currency != null) {
                    mCurrencyTextView.setText(currency.getSymbol());
                } else {
                    mCurrencyTextView.setText("?");
                }
                mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
                mConverterPicker.notifyMoneyChanged();
                break;
            case TAG_TAX_PICKER:
                mMoneyFormatter.applyNotTinted(mTaxEditText, currency, money);
                break;
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
        switch (tag) {
            case TAG_WALLET_FROM_PICKER:
                if (wallet != null) {
                    mWalletFromEditText.setText(wallet.getName());
                    mMoneyPicker.setCurrency(wallet.getCurrency());
                    mTaxPicker.setCurrency(wallet.getCurrency());
                    mConverterPicker.setCurrency1(wallet.getCurrency());
                } else {
                    mWalletFromEditText.setText(null);
                    mMoneyPicker.setCurrency(null);
                    mTaxPicker.setCurrency(null);
                    mConverterPicker.setCurrency1(null);
                }
                break;
            case TAG_WALLET_TO_PICKER:
                if (wallet != null) {
                    mWalletToEditText.setText(wallet.getName());
                    mConverterPicker.setCurrency2(wallet.getCurrency());
                } else {
                    mWalletToEditText.setText(null);
                    mConverterPicker.setCurrency2(null);
                }
                break;
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
        try {
            startActivity(attachment.getActionViewIntent(this));
        } catch (ActivityNotFoundException e) {
            ThemedDialog.buildMaterialDialog(this)
                    .title(R.string.title_error)
                    .content(R.string.message_error_activity_not_found)
                    .positiveText(android.R.string.ok)
                    .show();
        }
    }

    @Override
    public void onAttachmentDelete(Attachment attachment) {
        mAttachmentPicker.remove(attachment);
    }
}