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

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 06/03/18.
 */
public class NewEditSavingActivity extends NewEditItemActivity implements IconPicker.Controller,
                                                                        MoneyPicker.Controller,
                                                                        DateTimePicker.Controller,
                                                                        WalletPicker.SingleWalletController {

    private static final String TAG_ICON_PICKER = "NewEditSavingActivity::Tag::IconPicker";
    private static final String TAG_MONEY_PICKER = "NewEditSavingActivity::Tag::MoneyPicker";
    private static final String TAG_START_MONEY_PICKER = "NewEditSavingActivity::Tag::StartMoneyPicker";
    private static final String TAG_EXP_DATE_PICKER = "NewEditSavingActivity::Tag::ExpDatePicker";
    private static final String TAG_WALLET_PICKER = "NewEditSavingActivity::Tag::WalletPicker";

    private ImageView mAvatarImageView;
    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private MaterialEditText mDescriptionEditText;
    private MaterialEditText mStartMoneyEditText;
    private MaterialEditText mExpirationDateEditText;
    private MaterialEditText mWalletEditText;
    private MaterialEditText mNoteEditText;

    private IconPicker mIconPicker;
    private MoneyPicker mMoneyPicker;
    private MoneyPicker mStartMoneyPicker;
    private DateTimePicker mExpDatePicker;
    private WalletPicker mWalletPicker;

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
        View view = inflater.inflate(R.layout.layout_panel_new_edit_saving, parent, true);
        mDescriptionEditText = view.findViewById(R.id.description_edit_text);
        mStartMoneyEditText = view.findViewById(R.id.start_money_edit_text);
        mExpirationDateEditText = view.findViewById(R.id.expiration_date_edit_text);
        mWalletEditText = view.findViewById(R.id.wallet_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        // disable unused edit text
        mStartMoneyEditText.setTextViewMode(true);
        mExpirationDateEditText.setTextViewMode(true);
        mWalletEditText.setTextViewMode(true);
        // add validators
        mDescriptionEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_missing_description));
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
        mStartMoneyEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartMoneyPicker.showPicker();
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
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        Icon icon = null;
        long money = 0L;
        long startMoney = 0L;
        Date expirationDate = null;
        Wallet wallet = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                String[] projection = new String[] {
                        Contract.Saving.DESCRIPTION,
                        Contract.Saving.ICON,
                        Contract.Saving.START_MONEY,
                        Contract.Saving.END_MONEY,
                        Contract.Saving.WALLET_ID,
                        Contract.Saving.WALLET_NAME,
                        Contract.Saving.WALLET_ICON,
                        Contract.Saving.WALLET_CURRENCY,
                        Contract.Saving.END_DATE,
                        Contract.Saving.NOTE
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Saving.DESCRIPTION)));
                        icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Saving.ICON)));
                        startMoney = cursor.getLong(cursor.getColumnIndex(Contract.Saving.START_MONEY));
                        money = cursor.getLong(cursor.getColumnIndex(Contract.Saving.END_MONEY));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Saving.END_DATE))) {
                            expirationDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Saving.END_DATE)));
                        }
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Saving.NOTE)));
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
            } else {
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
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, money);
        mStartMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_START_MONEY_PICKER, null, startMoney);
        mExpDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_EXP_DATE_PICKER, expirationDate);
        mWalletPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_PICKER, wallet);
        // configure pickers
        mIconPicker.listenOn(mDescriptionEditText);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_saving;
            case EDIT_ITEM:
                return R.string.title_activity_edit_saving;
            default:
                return -1;
        }
    }

    private boolean validate() {
        return mDescriptionEditText.validate() && mWalletEditText.validate();
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Saving.DESCRIPTION, mDescriptionEditText.getTextAsString());
            contentValues.put(Contract.Saving.ICON, mIconPicker.getCurrentIcon().toString());
            contentValues.put(Contract.Saving.START_MONEY, mStartMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Saving.END_MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Saving.WALLET_ID, mWalletPicker.getCurrentWallet().getId());
            contentValues.put(Contract.Saving.END_DATE, mExpDatePicker.isSelected() ? DateUtils.getSQLDateString(mExpDatePicker.getCurrentDateTime()) : null);
            contentValues.put(Contract.Saving.COMPLETE, false);
            contentValues.put(Contract.Saving.NOTE, mNoteEditText.getTextAsString());
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_SAVINGS, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mAvatarImageView);
    }

    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        switch (tag) {
            case TAG_MONEY_PICKER:
                mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
                if (currency != null) {
                    mCurrencyTextView.setText(currency.getSymbol());
                } else {
                    mCurrencyTextView.setText("?");
                }
                break;
            case TAG_START_MONEY_PICKER:
                mMoneyFormatter.applyNotTinted(mStartMoneyEditText, currency, money);
                break;
        }
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        if (date != null) {
            DateFormatter.applyDate(mExpirationDateEditText, date);
        } else {
            mExpirationDateEditText.setText(null);
        }
    }

    @Override
    public void onWalletChanged(String tag, Wallet wallet) {
        if (wallet != null) {
            mWalletEditText.setText(wallet.getName());
            mMoneyPicker.setCurrency(wallet.getCurrency());
            mStartMoneyPicker.setCurrency(wallet.getCurrency());
        } else {
            mWalletEditText.setText(null);
            mMoneyPicker.setCurrency(null);
            mStartMoneyPicker.setCurrency(null);
        }
    }
}