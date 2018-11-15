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

package com.oriondev.moneywallet.ui.fragment.secondary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.RecurrenceSetting;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;

import java.util.Date;

/**
 * Created by andrea on 04/11/18.
 */
public class RecurrentTransferItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RECURRENT_TRANSFER_LOADER_ID = 57326;

    private View mProgressLayout;
    private View mMainLayout;

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;

    private TextView mDescriptionTextView;
    private TextView mWalletFromTextView;
    private TextView mWalletToTextView;
    private TextView mTaxTextView;
    private TextView mRecurrenceTextView;
    private TextView mEventTextView;
    private TextView mPlaceTextView;
    private TextView mNoteTextView;
    private CheckBox mConfirmedCheckBox;
    private CheckBox mCountInTotalCheckBox;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_money_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_recurrent_transfer_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mDescriptionTextView = view.findViewById(R.id.description_text_view);
        mWalletFromTextView = view.findViewById(R.id.wallet_from_text_view);
        mWalletToTextView = view.findViewById(R.id.wallet_to_text_view);
        mTaxTextView = view.findViewById(R.id.tax_text_view);
        mRecurrenceTextView = view.findViewById(R.id.recurrence_text_view);
        mEventTextView = view.findViewById(R.id.event_text_view);
        mPlaceTextView = view.findViewById(R.id.place_text_view);
        mNoteTextView = view.findViewById(R.id.note_text_view);
        mConfirmedCheckBox = view.findViewById(R.id.confirmed_checkbox);
        mCountInTotalCheckBox = view.findViewById(R.id.count_in_total_checkbox);
    }

    @Override
    protected String getTitle() {
        return  getString(R.string.title_fragment_item_recurrence);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_item:
                showDeleteDialog(getActivity());
                break;
        }
        return false;
    }

    private void showDeleteDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_delete_recurrent_transfer)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSFERS, getItemId());
                            ContentResolver contentResolver = activity.getContentResolver();
                            contentResolver.delete(uri, null, null);
                            RecurrenceBroadcastReceiver.scheduleRecurrenceTask(activity);
                            navigateBackSafely();
                            showItemId(0L);
                        }
                    }

                })
                .show();
    }

    @Override
    protected void onShowItemId(long itemId) {
        setLoadingScreen(true);
        getLoaderManager().restartLoader(RECURRENT_TRANSFER_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mMoneyTextView.setText(null);
            mCurrencyTextView.setText(null);
            mDescriptionTextView.setText(null);
            mWalletFromTextView.setText(null);
            mWalletToTextView.setText(null);
            mTaxTextView.setText(null);
            mRecurrenceTextView.setText(null);
            mEventTextView.setText(null);
            mPlaceTextView.setText(null);
            mNoteTextView.setText(null);
            mConfirmedCheckBox.setText(null);
            mCountInTotalCheckBox.setText(null);
            mProgressLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSFERS, getItemId());
            String[] projection = new String[] {
                    "*"
            };
            return new CursorLoader(getActivity(), uri, projection, null, null, null);
        }
        throw new IllegalStateException("Activity is null");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            String iso = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_FROM_CURRENCY));
            CurrencyUnit currency = CurrencyManager.getCurrency(iso);
            if (currency != null) {
                mCurrencyTextView.setText(currency.getSymbol());
            } else {
                mCurrencyTextView.setText("?");
            }
            long money = cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_FROM));
            mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
            String description = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.DESCRIPTION));
            if (!TextUtils.isEmpty(description)) {
                mDescriptionTextView.setText(description);
                mDescriptionTextView.setVisibility(View.VISIBLE);
            } else {
                mDescriptionTextView.setVisibility(View.GONE);
            }
            String walletFrom = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_FROM_NAME));
            String walletTo = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.WALLET_TO_NAME));
            mWalletFromTextView.setText(walletFrom);
            mWalletToTextView.setText(walletTo);
            if (cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_TAX)) > 0L) {
                long tax = cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransfer.MONEY_TAX));
                mTaxTextView.setText(mMoneyFormatter.getNotTintedString(currency, tax));
                mTaxTextView.setVisibility(View.VISIBLE);
            } else {
                mTaxTextView.setVisibility(View.GONE);
            }
            try {
                Date startDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.START_DATE)));
                String rule = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.RULE));
                RecurrenceSetting recurrenceSetting = RecurrenceSetting.build(startDate, rule);
                mRecurrenceTextView.setText(recurrenceSetting.getUserReadableString(getActivity()));
            } catch (InvalidRecurrenceRuleException e) {
                mRecurrenceTextView.setText(R.string.hint_recurrence_invalid);
            }
            String event = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.EVENT_NAME));
            if (!TextUtils.isEmpty(event)) {
                mEventTextView.setText(event);
                mEventTextView.setVisibility(View.VISIBLE);
            } else {
                mEventTextView.setVisibility(View.GONE);
            }
            String place = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.PLACE_NAME));
            if (!TextUtils.isEmpty(place)) {
                mPlaceTextView.setText(place);
                mPlaceTextView.setVisibility(View.VISIBLE);
            } else {
                mPlaceTextView.setVisibility(View.GONE);
            }
            String note = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransfer.NOTE));
            if (!TextUtils.isEmpty(note)) {
                mNoteTextView.setText(note);
                mNoteTextView.setVisibility(View.VISIBLE);
            } else {
                mNoteTextView.setVisibility(View.GONE);
            }
            mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransfer.CONFIRMED)) == 1);
            mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransfer.COUNT_IN_TOTAL)) == 1);
        } else {
            showItemId(0L);
        }
        setLoadingScreen(false);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // nothing to release
    }
}