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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditSavingActivity;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 03/04/18.
 */
public class SavingItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SAVING_LOADER_ID = 53324;

    private View mProgressLayout;
    private View mMainLayout;

    private ImageView mAvatarImageView;
    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;

    private TextView mDescriptionTextView;
    private TextView mStartMoneyTextView;
    private TextView mExpirationDateTextView;
    private TextView mWalletTextView;
    private TextView mNoteTextView;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_icon_money_item, parent, true);
        mAvatarImageView = view.findViewById(R.id.avatar_image_view);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_saving_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mDescriptionTextView = view.findViewById(R.id.description_text_view);
        mStartMoneyTextView = view.findViewById(R.id.start_money_text_view);
        mExpirationDateTextView = view.findViewById(R.id.expiration_date_text_view);
        mWalletTextView = view.findViewById(R.id.wallet_text_view);
        mNoteTextView = view.findViewById(R.id.note_text_view);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_saving);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_list_archive_edit_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_transaction_list:
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(TransactionListActivity.SAVING_ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_archive_item:
                showArchiveDialog(getActivity());
                break;
            case R.id.action_unarchive_item:
                showUnarchiveDialog(getActivity());
                break;
            case R.id.action_edit_item:
                intent = new Intent(getActivity(), NewEditSavingActivity.class);
                intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.EDIT_ITEM);
                intent.putExtra(NewEditItemActivity.ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_delete_item:
                showDeleteDialog(getActivity());
                break;
        }
        return false;
    }

    private void showArchiveDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_action_archive_saving)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Saving.COMPLETE, true);
                            int rows = activity.getContentResolver().update(uri, contentValues, null, null);
                            if (rows > 0) {
                                setMenuItemVisibility(R.id.action_archive_item, false);
                                setMenuItemVisibility(R.id.action_unarchive_item, true);
                            }
                        }
                    }

                })
                .show();
    }

    private void showUnarchiveDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_action_unarchive_saving)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Saving.COMPLETE, false);
                            int rows = activity.getContentResolver().update(uri, contentValues, null, null);
                            if (rows > 0) {
                                setMenuItemVisibility(R.id.action_archive_item, true);
                                setMenuItemVisibility(R.id.action_unarchive_item, false);
                            }
                        }
                    }

                })
                .show();
    }

    private void showDeleteDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_delete_saving)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
                            ContentResolver contentResolver = activity.getContentResolver();
                            contentResolver.delete(uri, null, null);
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
        getLoaderManager().restartLoader(SAVING_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {

            mProgressLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
            setMenuItemVisibility(R.id.action_archive_item, false);
            setMenuItemVisibility(R.id.action_unarchive_item, false);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, getItemId());
            String[] projection = new String[] {
                    Contract.Saving.ID,
                    Contract.Saving.ICON,
                    Contract.Saving.WALLET_CURRENCY,
                    Contract.Saving.DESCRIPTION,
                    Contract.Saving.START_MONEY,
                    Contract.Saving.END_MONEY,
                    Contract.Saving.END_DATE,
                    Contract.Saving.WALLET_NAME,
                    Contract.Saving.NOTE,
                    Contract.Saving.COMPLETE
            };
            return new CursorLoader(activity, uri, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == SAVING_LOADER_ID) {
            if (cursor != null && cursor.moveToFirst()) {
                Icon icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Saving.ICON)));
                IconLoader.loadInto(icon, mAvatarImageView);
                String iso = cursor.getString(cursor.getColumnIndex(Contract.Saving.WALLET_CURRENCY));
                CurrencyUnit currency = CurrencyManager.getCurrency(iso);
                if (currency != null) {
                    mCurrencyTextView.setText(currency.getSymbol());
                } else {
                    mCurrencyTextView.setText("?");
                }
                long money = cursor.getLong(cursor.getColumnIndex(Contract.Saving.END_MONEY));
                mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
                String description = cursor.getString(cursor.getColumnIndex(Contract.Saving.DESCRIPTION));
                if (!TextUtils.isEmpty(description)) {
                    mDescriptionTextView.setText(description);
                    mDescriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    mDescriptionTextView.setVisibility(View.GONE);
                }
                long startMoney = cursor.getLong(cursor.getColumnIndex(Contract.Saving.START_MONEY));
                mStartMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, startMoney, MoneyFormatter.CurrencyMode.ALWAYS_SHOWN));
                String expirationDate = cursor.getString(cursor.getColumnIndex(Contract.Saving.END_DATE));
                if (!TextUtils.isEmpty(expirationDate)) {
                    DateFormatter.applyDate(mExpirationDateTextView, DateUtils.getDateFromSQLDateString(expirationDate));
                    mExpirationDateTextView.setVisibility(View.VISIBLE);
                } else {
                    mExpirationDateTextView.setVisibility(View.GONE);
                }
                String wallet = cursor.getString(cursor.getColumnIndex(Contract.Saving.WALLET_NAME));
                mWalletTextView.setText(wallet);
                String note = cursor.getString(cursor.getColumnIndex(Contract.Saving.NOTE));
                if (!TextUtils.isEmpty(note)) {
                    mNoteTextView.setText(note);
                    mNoteTextView.setVisibility(View.VISIBLE);
                } else {
                    mNoteTextView.setVisibility(View.GONE);
                }
                if (cursor.getInt(cursor.getColumnIndex(Contract.Saving.COMPLETE)) == 1) {
                    setMenuItemVisibility(R.id.action_archive_item, false);
                    setMenuItemVisibility(R.id.action_unarchive_item, true);
                } else {
                    setMenuItemVisibility(R.id.action_archive_item, true);
                    setMenuItemVisibility(R.id.action_unarchive_item, false);
                }
            } else {
                showItemId(0L);
            }
            setLoadingScreen(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }
}