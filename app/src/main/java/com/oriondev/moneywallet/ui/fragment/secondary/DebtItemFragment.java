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
import com.oriondev.moneywallet.ui.activity.NewEditDebtActivity;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 03/04/18.
 */
public class DebtItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DEBT_LOADER_ID = 53321;
    private static final int PEOPLE_LOADER_ID = 53322;

    private View mProgressLayout;
    private View mMainLayout;

    private ImageView mAvatarImageView;
    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;

    private TextView mDescriptionTextView;
    private TextView mDateTextView;
    private TextView mExpirationDateTextView;
    private TextView mWalletTextView;
    private TextView mPeopleTextView;
    private TextView mPlaceTextView;
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
        View view = inflater.inflate(R.layout.layout_panel_show_debt_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mDescriptionTextView = view.findViewById(R.id.description_text_view);
        mDateTextView = view.findViewById(R.id.date_text_view);
        mExpirationDateTextView = view.findViewById(R.id.expiration_date_text_view);
        mWalletTextView = view.findViewById(R.id.wallet_text_view);
        mPeopleTextView = view.findViewById(R.id.people_text_view);
        mPlaceTextView = view.findViewById(R.id.place_text_view);
        mNoteTextView = view.findViewById(R.id.note_text_view);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_debt);
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
                intent.putExtra(TransactionListActivity.DEBT_ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_archive_item:
                showArchiveDialog(getActivity());
                break;
            case R.id.action_unarchive_item:
                showUnarchiveDialog(getActivity());
                break;
            case R.id.action_edit_item:
                intent = new Intent(getActivity(), NewEditDebtActivity.class);
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
                .content(R.string.message_action_archive_debt)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Debt.ARCHIVED, true);
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
                .content(R.string.message_action_unarchive_debt)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Contract.Debt.ARCHIVED, false);
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
                .content(R.string.message_delete_debt)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
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
        getLoaderManager().restartLoader(DEBT_LOADER_ID, null, this);
        getLoaderManager().restartLoader(PEOPLE_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mAvatarImageView.setImageDrawable(null);
            mCurrencyTextView.setText(null);
            mMoneyTextView.setText(null);
            mDescriptionTextView.setText(null);
            mDateTextView.setText(null);
            mExpirationDateTextView.setText(null);
            mWalletTextView.setText(null);
            mPeopleTextView.setText(null);
            mPlaceTextView.setText(null);
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
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, getItemId());
            if (id == DEBT_LOADER_ID) {
                String[] projection = new String[] {
                        Contract.Debt.ID,
                        Contract.Debt.ICON,
                        Contract.Debt.WALLET_CURRENCY,
                        Contract.Debt.MONEY,
                        Contract.Debt.DESCRIPTION,
                        Contract.Debt.DATE,
                        Contract.Debt.EXPIRATION_DATE,
                        Contract.Debt.WALLET_NAME,
                        Contract.Debt.PLACE_NAME,
                        Contract.Debt.NOTE,
                        Contract.Debt.ARCHIVED
                };
                return new CursorLoader(activity, uri, projection, null, null, null);
            } else if (id == PEOPLE_LOADER_ID) {
                String[] projection = new String[] {
                        "*"
                };
                return new CursorLoader(getActivity(), Uri.withAppendedPath(uri, "people"), projection, null, null, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == DEBT_LOADER_ID) {
            if (cursor != null && cursor.moveToFirst()) {
                Icon icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Debt.ICON)));
                IconLoader.loadInto(icon, mAvatarImageView);
                String iso = cursor.getString(cursor.getColumnIndex(Contract.Debt.WALLET_CURRENCY));
                CurrencyUnit currency = CurrencyManager.getCurrency(iso);
                if (currency != null) {
                    mCurrencyTextView.setText(currency.getSymbol());
                } else {
                    mCurrencyTextView.setText("?");
                }
                long money = cursor.getLong(cursor.getColumnIndex(Contract.Debt.MONEY));
                mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
                String description = cursor.getString(cursor.getColumnIndex(Contract.Debt.DESCRIPTION));
                if (!TextUtils.isEmpty(description)) {
                    mDescriptionTextView.setText(description);
                    mDescriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    mDescriptionTextView.setVisibility(View.GONE);
                }
                Date date = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Debt.DATE)));
                DateFormatter.applyDate(mDateTextView, date);
                String expirationDate = cursor.getString(cursor.getColumnIndex(Contract.Debt.EXPIRATION_DATE));
                if (!TextUtils.isEmpty(expirationDate)) {
                    DateFormatter.applyDate(mExpirationDateTextView, DateUtils.getDateFromSQLDateString(expirationDate));
                    mExpirationDateTextView.setVisibility(View.VISIBLE);
                } else {
                    mExpirationDateTextView.setVisibility(View.GONE);
                }
                String wallet = cursor.getString(cursor.getColumnIndex(Contract.Debt.WALLET_NAME));
                mWalletTextView.setText(wallet);
                String place = cursor.getString(cursor.getColumnIndex(Contract.Debt.PLACE_NAME));
                if (!TextUtils.isEmpty(place)) {
                    mPlaceTextView.setText(place);
                    mPlaceTextView.setVisibility(View.VISIBLE);
                } else {
                    mPlaceTextView.setVisibility(View.GONE);
                }
                String note = cursor.getString(cursor.getColumnIndex(Contract.Debt.NOTE));
                if (!TextUtils.isEmpty(note)) {
                    mNoteTextView.setText(note);
                    mNoteTextView.setVisibility(View.VISIBLE);
                } else {
                    mNoteTextView.setVisibility(View.GONE);
                }
                if (cursor.getInt(cursor.getColumnIndex(Contract.Debt.ARCHIVED)) == 1) {
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
        } else if (loader.getId() == PEOPLE_LOADER_ID) {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                    if (i != 0) {
                        builder.append(", ");
                    }
                    String name = cursor.getString(cursor.getColumnIndex(Contract.Person.NAME));
                    builder.append(name);
                }
                mPeopleTextView.setText(builder);
                mPeopleTextView.setVisibility(View.VISIBLE);
            } else {
                mPeopleTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }
}