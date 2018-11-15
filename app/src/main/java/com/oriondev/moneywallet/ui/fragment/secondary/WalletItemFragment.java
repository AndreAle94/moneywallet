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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.SQLiteDataException;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditWalletActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 03/04/18.
 */
public class WalletItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int WALLET_LOADER_ID = 246390;

    private View mProgressLayout;
    private View mMainLayout;

    private ImageView mAvatarImageView;
    private TextView mNameTextView;
    private TextView mCurrencyTextView;
    private TextView mStartMoneyTextView;
    private TextView mNoteTextView;
    private CheckBox mNotExcludeTotalCheckBox;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_icon_name_item, parent, true);
        mAvatarImageView = view.findViewById(R.id.avatar_image_view);
        mNameTextView = view.findViewById(R.id.name_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_wallet_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mStartMoneyTextView = view.findViewById(R.id.start_money_text_view);
        mNoteTextView = view.findViewById(R.id.note_text_view);
        mNotExcludeTotalCheckBox = view.findViewById(R.id.not_exclude_total_check_box);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_wallet);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_archive_edit_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_archive_item:
                showArchiveDialog(getActivity());
                break;
            case R.id.action_unarchive_item:
                showUnarchiveDialog(getActivity());
                break;
            case R.id.action_edit_item:
                Intent intent = new Intent(getActivity(), NewEditWalletActivity.class);
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
                .content(R.string.message_action_archive_wallet)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Wallet.ARCHIVED, true);
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
                .content(R.string.message_action_unarchive_wallet)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Wallet.ARCHIVED, false);
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
                .content(R.string.message_delete_wallet)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            try {
                                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
                                ContentResolver contentResolver = activity.getContentResolver();
                                contentResolver.delete(uri, null, null);
                                navigateBackSafely();
                                showItemId(0L);
                            } catch (SQLiteDataException e) {
                                if (e.getErrorCode() == Contract.ErrorCode.WALLET_USED_IN_TRANSFER) {
                                    ThemedDialog.buildMaterialDialog(activity)
                                            .title(R.string.title_error)
                                            .content(R.string.message_error_delete_wallet_of_transfer)
                                            .positiveText(android.R.string.ok)
                                            .show();
                                }
                            }
                        }
                    }

                })
                .show();
    }

    @Override
    protected void onShowItemId(long itemId) {
        setLoadingScreen(true);
        getLoaderManager().restartLoader(WALLET_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mAvatarImageView.setImageDrawable(null);
            mNameTextView.setText(null);
            mCurrencyTextView.setText(null);
            mStartMoneyTextView.setText(null);
            mNoteTextView.setText(null);
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
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, getItemId());
            String[] projection = new String[] {
                    Contract.Wallet.NAME,
                    Contract.Wallet.ICON,
                    Contract.Wallet.CURRENCY,
                    Contract.Wallet.START_MONEY,
                    Contract.Wallet.NOTE,
                    Contract.Wallet.COUNT_IN_TOTAL,
                    Contract.Wallet.ARCHIVED
            };
            return new CursorLoader(getActivity(), uri, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            Icon icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON)));
            IconLoader.loadInto(icon, mAvatarImageView);
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)));
            CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY)));
            mCurrencyTextView.setText(currency.getName());
            long startMoney = cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY));
            mMoneyFormatter.applyNotTinted(mStartMoneyTextView, currency, startMoney);
            String note = cursor.getString(cursor.getColumnIndex(Contract.Wallet.NOTE));
            if (!TextUtils.isEmpty(note)) {
                mNoteTextView.setText(note);
                mNoteTextView.setVisibility(View.VISIBLE);
            } else {
                mNoteTextView.setVisibility(View.GONE);
            }
            if (cursor.getInt(cursor.getColumnIndex(Contract.Wallet.ARCHIVED)) == 1) {
                setMenuItemVisibility(R.id.action_archive_item, false);
                setMenuItemVisibility(R.id.action_unarchive_item, true);
            } else {
                setMenuItemVisibility(R.id.action_archive_item, true);
                setMenuItemVisibility(R.id.action_unarchive_item, false);
            }
            mNotExcludeTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.Wallet.COUNT_IN_TOTAL)) == 1);
        } else {
            showItemId(0L);
        }
        setLoadingScreen(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }
}