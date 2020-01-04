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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.Person;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.picker.PersonPicker;
import com.oriondev.moneywallet.picker.PlacePicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 06/03/18.
 */
public class NewEditDebtActivity extends NewEditItemActivity implements IconPicker.Controller,
                                                                        MoneyPicker.Controller,
                                                                        DateTimePicker.Controller,
                                                                        WalletPicker.SingleWalletController,
                                                                        PersonPicker.Controller,
                                                                        PlacePicker.Controller {

    private static final String TAG_ICON_PICKER = "NewEditDebtActivity::Tag::IconPicker";
    private static final String TAG_MONEY_PICKER = "NewEditDebtActivity::Tag::MoneyPicker";
    private static final String TAG_DATE_PICKER = "NewEditDebtActivity::Tag::DatePicker";
    private static final String TAG_EXP_DATE_PICKER = "NewEditDebtActivity::Tag::ExpDatePicker";
    private static final String TAG_WALLET_PICKER = "NewEditDebtActivity::Tag::WalletPicker";
    private static final String TAG_PERSON_PICKER = "NewEditDebtActivity::Tag::PersonPicker";
    private static final String TAG_PLACE_PICKER = "NewEditDebtActivity::Tag::PlacePicker";

    public static final String TYPE = "NewEditDebtActivity::Argument::Type";
    public static final String SS_TYPE = "NewEditDebtActivity::SavedState::Type";

    private ImageView mAvatarImageView;
    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private MaterialEditText mDescriptionEditText;
    private MaterialEditText mDateEditText;
    private MaterialEditText mExpirationDateEditText;
    private MaterialEditText mWalletEditText;
    private MaterialEditText mPeopleEditText;
    private MaterialEditText mPlaceEditText;
    private MaterialEditText mNoteEditText;

    private IconPicker mIconPicker;
    private MoneyPicker mMoneyPicker;
    private DateTimePicker mDatePicker;
    private DateTimePicker mExpDatePicker;
    private WalletPicker mWalletPicker;
    private PersonPicker mPersonPicker;
    private PlacePicker mPlacePicker;

    private Contract.DebtType mDebtType;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_icon_money_item, parent, true);
        mAvatarImageView = view.findViewById(R.id.avatar_image_view);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
        // attach a listener to the views
        mAvatarImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mIconPicker.showPicker();
            }

        });
        mMoneyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_debt, parent, true);
        mDescriptionEditText = view.findViewById(R.id.description_edit_text);
        mDateEditText = view.findViewById(R.id.date_edit_text);
        mExpirationDateEditText = view.findViewById(R.id.expiration_date_edit_text);
        mWalletEditText = view.findViewById(R.id.wallet_edit_text);
        mPeopleEditText = view.findViewById(R.id.people_edit_text);
        mPlaceEditText = view.findViewById(R.id.place_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        // disable unused edit text
        mDateEditText.setTextViewMode(true);
        mExpirationDateEditText.setTextViewMode(true);
        mWalletEditText.setTextViewMode(true);
        mPeopleEditText.setTextViewMode(true);
        mPlaceEditText.setTextViewMode(true);
        // setup validators
        mDescriptionEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_missing_description));
        mDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_date);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mDatePicker.isSelected();
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
        // setup listeners
        mDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDatePicker.showDatePicker();
            }

        });
        mExpirationDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mExpDatePicker.showDatePicker();
            }

        });
        mExpirationDateEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mExpDatePicker.setCurrentDateTime(null);
                return true;
            }

        });
        mWalletEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletPicker.showSingleWalletPicker();
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
                return true;
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
                return true;
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        Icon icon = null;
        long money = 0L;
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        Date expirationDate = null;
        Wallet wallet = null;
        Person[] people = null;
        Place place = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
                String[] projection = new String[] {
                        Contract.Debt.ICON,
                        Contract.Debt.DESCRIPTION,
                        Contract.Debt.TYPE,
                        Contract.Debt.MONEY,
                        Contract.Debt.DATE,
                        Contract.Debt.EXPIRATION_DATE,
                        Contract.Debt.WALLET_ID,
                        Contract.Debt.WALLET_NAME,
                        Contract.Debt.WALLET_ICON,
                        Contract.Debt.WALLET_CURRENCY,
                        Contract.Debt.NOTE,
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
                        mDebtType = Contract.DebtType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Debt.TYPE)));
                        mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Debt.DESCRIPTION)));
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Debt.NOTE)));
                        icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Debt.ICON)));
                        money = cursor.getLong(cursor.getColumnIndex(Contract.Debt.MONEY));
                        date = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Debt.DATE)));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Debt.EXPIRATION_DATE))) {
                            expirationDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Debt.EXPIRATION_DATE)));
                        }
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
            } else {
                mDebtType = (Contract.DebtType) getIntent().getSerializableExtra(TYPE);
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
            }
        } else {
            mDebtType = (Contract.DebtType) savedInstanceState.getSerializable(SS_TYPE);
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, money);
        mDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_DATE_PICKER, date);
        mExpDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_EXP_DATE_PICKER, expirationDate);
        mWalletPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_PICKER, wallet);
        mPersonPicker = PersonPicker.createPicker(fragmentManager, TAG_PERSON_PICKER, people);
        mPlacePicker = PlacePicker.createPicker(fragmentManager, TAG_PLACE_PICKER, place);
        // configure pickers
        mIconPicker.listenOn(mDescriptionEditText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_TYPE, mDebtType);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_debt;
            case EDIT_ITEM:
                return R.string.title_activity_edit_debt;
            default:
                return -1;
        }
    }

    private boolean validate() {
        return mDescriptionEditText.validate() && mDescriptionEditText.validate() && mWalletEditText.validate();
    }

    @Override
    protected void onSaveChanges(final Mode mode) {
        if (validate()) {
            if (mode == Mode.NEW_ITEM) {
                ThemedDialog.buildMaterialDialog(this)
                        .title(R.string.title_debt_insert_master_transaction)
                        .content(R.string.message_debt_insert_master_transaction)
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
                        .onAny(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                updateDatabase(mode, which == DialogAction.POSITIVE);
                                setResult(Activity.RESULT_OK);
                                finish();
                            }

                        })
                        .show();
            } else {
                updateDatabase(mode, false);
            }

        }
    }

    private void updateDatabase(Mode mode, boolean addMasterTransaction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Debt.TYPE, mDebtType.getValue());
        contentValues.put(Contract.Debt.ICON, mIconPicker.getCurrentIcon().toString());
        contentValues.put(Contract.Debt.DESCRIPTION, mDescriptionEditText.getTextAsString());
        contentValues.put(Contract.Debt.DATE, DateUtils.getSQLDateString(mDatePicker.getCurrentDateTime()));
        contentValues.put(Contract.Debt.EXPIRATION_DATE, mExpDatePicker.isSelected() ? DateUtils.getSQLDateString(mExpDatePicker.getCurrentDateTime()) : null);
        contentValues.put(Contract.Debt.WALLET_ID, mWalletPicker.getCurrentWallet().getId());
        contentValues.put(Contract.Debt.NOTE, mNoteEditText.getTextAsString());
        contentValues.put(Contract.Debt.PLACE_ID, mPlacePicker.isSelected() ? mPlacePicker.getCurrentPlace().getId() : null);
        contentValues.put(Contract.Debt.MONEY, mMoneyPicker.getCurrentMoney());
        contentValues.put(Contract.Debt.ARCHIVED, false);
        contentValues.put(Contract.Debt.PEOPLE_IDS, Contract.getObjectIds(mPersonPicker.getCurrentPeople()));
        contentValues.put(Contract.Debt.INSERT_MASTER_TRANSACTION, addMasterTransaction);
        ContentResolver contentResolver = getContentResolver();
        switch (mode) {
            case NEW_ITEM:
                contentResolver.insert(DataContentProvider.CONTENT_DEBTS, contentValues);
                break;
            case EDIT_ITEM:
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
                contentResolver.update(uri, contentValues, null, null);
                break;
        }
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mAvatarImageView);
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
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_DATE_PICKER:
                DateFormatter.applyDate(mDateEditText, date);
                break;
            case TAG_EXP_DATE_PICKER:
                if (date != null) {
                    DateFormatter.applyDate(mExpirationDateEditText, date);
                } else {
                    mExpirationDateEditText.setText(null);
                }
                break;
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
}