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
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.CurrencyPicker;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * This class is buildMaterialDialog on top of {@link NewEditItemActivity} and let the user to create a new wallet
 * or edit an existing one inside the database storage through the use of the {@link DataContentProvider}.
 * The activity uses the following pickers to retrieve information from the user:
 * - {@link IconPicker} to create or select an icon for the wallet.
 * - {@link CurrencyPicker} to select the currency of the wallet.
 * - {@link MoneyPicker} to insert the starting amount of money for the wallet.
 */
public class NewEditWalletActivity extends NewEditItemActivity implements IconPicker.Controller, CurrencyPicker.Controller, MoneyPicker.Controller {

    private static final String TAG_ICON_PICKER = "NewEditWalletActivity::Tag::IconPicker";
    private static final String TAG_CURRENCY_PICKER = "NewEditWalletActivity::Tag::CurrencyPicker";
    private static final String TAG_MONEY_PICKER = "NewEditWalletActivity::Tag::MoneyPicker";

    private ImageView mIconView;
    private MaterialEditText mNameEditText;
    private MaterialEditText mCurrencyEditText;
    private MaterialEditText mStartMoneyEditText;
    private MaterialEditText mNoteEditText;
    private CheckBox mNotExcludeTotalCheckBox;

    private IconPicker mIconPicker;
    private CurrencyPicker mCurrencyPicker;
    private MoneyPicker mMoneyPicker;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_icon_name_item, parent, true);
        mIconView = view.findViewById(R.id.icon_image_view);
        mNameEditText = view.findViewById(R.id.name_edit_text);
        mNameEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_name_not_valid));
        // attach a listener to the views
        mIconView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mIconPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_wallet, parent, true);
        mCurrencyEditText = view.findViewById(R.id.currency_edit_text);
        mStartMoneyEditText = view.findViewById(R.id.start_money_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        mNotExcludeTotalCheckBox = view.findViewById(R.id.not_exclude_total_check_box);
        mCurrencyEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_currency_not_valid);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mCurrencyPicker.getCurrentCurrency() != null;
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        // disable edit text capabilities when not needed
        mCurrencyEditText.setTextViewMode(true);
        mStartMoneyEditText.setTextViewMode(true);
        // attach a click listener to the picker views
        mCurrencyEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mCurrencyPicker.showPicker();
            }

        });
        mStartMoneyEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        // initialize or load all item parameters
        String name = null;
        Icon icon = null;
        CurrencyUnit currencyUnit = CurrencyManager.getDefaultCurrency();
        long startMoney = 0L;
        boolean countInTotal = true;
        // load item state
        if (savedInstanceState == null && getMode() == Mode.EDIT_ITEM) {
            // retrieve the item from the content provider
            boolean loadComplete = false;
            String[] projection = new String[] {
                    Contract.Wallet.NAME,
                    Contract.Wallet.ICON,
                    Contract.Wallet.CURRENCY,
                    Contract.Wallet.START_MONEY,
                    Contract.Wallet.COUNT_IN_TOTAL,
                    Contract.Wallet.NOTE
            };
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.moveToFirst()) {
                    mNameEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)));
                    icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON)));
                    currencyUnit = CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY)));
                    startMoney = cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY));
                    mNotExcludeTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Wallet.COUNT_IN_TOTAL)) == 1);
                    mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Wallet.NOTE)));
                    loadComplete = true;
                }
                cursor.close();
            }
            if (!loadComplete) {
                // the id has not been found in the content provider, this can be a bug inside the
                // application logic or an attempt from another component to let the user modify
                // a wallet that not exists or has been deleted. Simply log it and close the activity.
                // TODO log this as 'probably runtime bug'
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        mCurrencyPicker = CurrencyPicker.createPicker(fragmentManager, TAG_CURRENCY_PICKER, currencyUnit);
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, currencyUnit, startMoney);
        // configure pickers
        mIconPicker.listenOn(mNameEditText);
    }

    @Override
    @StringRes
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_wallet;
            case EDIT_ITEM:
                return R.string.title_activity_edit_wallet;
            default:
                return -1;
        }
    }

    /**
     * This method checks if everything has been correctly provided by the user.
     * It is responsible to show error messages if something is wrong or missing.
     * @return true if everything is ok, false otherwise.
     */
    private boolean validate() {
        return mNameEditText.validate() && mCurrencyEditText.validate();
    }

    /**
     * This method is called by the superclass whenever the user clicks on the toolbar icon to save
     * the changes to the current wallet.
     * @param mode current mode of the activity.
     */
    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Wallet.NAME, mNameEditText.getTextAsString());
            contentValues.put(Contract.Wallet.ICON, mIconPicker.getCurrentIcon().toString());
            contentValues.put(Contract.Wallet.CURRENCY, mCurrencyPicker.getCurrentCurrency().getIso());
            contentValues.put(Contract.Wallet.START_MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Wallet.COUNT_IN_TOTAL, mNotExcludeTotalCheckBox.isChecked());
            contentValues.put(Contract.Wallet.NOTE, mNoteEditText.getTextAsString());
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_WALLETS, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * This method is called whenever the {@link #mIconPicker} detects a change of the
     * selected icon. It is automatically triggered when the picker is firstly
     * created or restored from a fragment recreation.
     * @param tag of the picker that called this method.
     * @param icon current icon set for this picker.
     */
    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mIconView);
    }

    /**
     * This method is called whenever the {@link #mCurrencyPicker} detects a change of the
     * selected currency. It is automatically triggered when the picker is firstly
     * created or restored from a fragment recreation.
     * @param tag of the picker that called this method.
     * @param currency current currency set for this picker.
     */
    @Override
    public void onCurrencyChanged(String tag, CurrencyUnit currency) {
        if (currency != null) {
            mCurrencyEditText.setText(currency.getName());
        } else {
            mCurrencyEditText.setText(null);
        }
        mMoneyPicker.setCurrency(currency);
    }

    /**
     * This method is called whenever the {@link #mMoneyPicker} detects a change of the
     * currency or the current money. It is automatically triggered when the picker is firstly
     * created or restored from a fragment recreation.
     * @param tag of the picker that called this method.
     * @param currency current currency set for this picker.
     * @param money current money selected for this picker.
     */
    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        mMoneyFormatter.applyNotTinted(mStartMoneyEditText, currency, money);
    }
}