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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditBudgetActivity;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 03/04/18.
 */
public class BudgetItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BUDGET_LOADER_ID = 53320;
    private static final int WALLETS_LOADER_ID = 53321;

    private View mProgressLayout;
    private View mMainLayout;

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;

    private TextView mTypeTextView;
    private TextView mCategoryTextView;
    private TextView mStartDateTextView;
    private TextView mEndDateTextView;
    private TextView mWalletsTextView;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_money_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_budget_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mTypeTextView = view.findViewById(R.id.type_text_view);
        mCategoryTextView = view.findViewById(R.id.category_text_view);
        mStartDateTextView = view.findViewById(R.id.start_date_text_view);
        mEndDateTextView = view.findViewById(R.id.end_date_text_view);
        mWalletsTextView = view.findViewById(R.id.wallets_text_view);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_budget);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_list_edit_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_transaction_list:
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(TransactionListActivity.BUDGET_ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_edit_item:
                intent = new Intent(getActivity(), NewEditBudgetActivity.class);
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

    private void showDeleteDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_delete_budget)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_BUDGETS, getItemId());
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
        getLoaderManager().restartLoader(BUDGET_LOADER_ID, null, this);
        getLoaderManager().restartLoader(WALLETS_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mCurrencyTextView.setText(null);
            mMoneyTextView.setText(null);
            mTypeTextView.setText(null);
            mCategoryTextView.setText(null);
            mStartDateTextView.setText(null);
            mEndDateTextView.setText(null);
            mWalletsTextView.setText(null);
            mProgressLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_BUDGETS, getItemId());
            if (id == BUDGET_LOADER_ID) {
                String[] projection = new String[] {
                        Contract.Budget.ID,
                        Contract.Budget.CURRENCY,
                        Contract.Budget.MONEY,
                        Contract.Budget.TYPE,
                        Contract.Budget.CATEGORY_NAME,
                        Contract.Budget.START_DATE,
                        Contract.Budget.END_DATE
                };
                return new CursorLoader(activity, uri, projection, null, null, null);
            } else if (id == WALLETS_LOADER_ID) {
                String[] projection = new String[] {
                        "*"
                };
                return new CursorLoader(getActivity(), Uri.withAppendedPath(uri, "wallets"), projection, null, null, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == BUDGET_LOADER_ID) {
            if (cursor != null && cursor.moveToFirst()) {
                String iso = cursor.getString(cursor.getColumnIndex(Contract.Budget.CURRENCY));
                CurrencyUnit currency = CurrencyManager.getCurrency(iso);
                if (currency != null) {
                    mCurrencyTextView.setText(currency.getSymbol());
                } else {
                    mCurrencyTextView.setText("?");
                }
                long money = cursor.getLong(cursor.getColumnIndex(Contract.Budget.MONEY));
                mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
                Contract.BudgetType budgetType = Contract.BudgetType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Budget.TYPE)));
                if (budgetType != null) {
                    switch (budgetType) {
                        case INCOMES:
                            mTypeTextView.setText(R.string.hint_incomes);
                            mCategoryTextView.setVisibility(View.GONE);
                            break;
                        case EXPENSES:
                            mTypeTextView.setText(R.string.hint_expenses);
                            mCategoryTextView.setVisibility(View.GONE);
                            break;
                        case CATEGORY:
                            mTypeTextView.setText(R.string.hint_category);
                            mCategoryTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Budget.CATEGORY_NAME)));
                            mCategoryTextView.setVisibility(View.VISIBLE);
                            break;
                    }
                }
                Date startDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Budget.START_DATE)));
                Date endDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Budget.END_DATE)));
                DateFormatter.applyDate(mStartDateTextView, startDate);
                DateFormatter.applyDate(mEndDateTextView, endDate);
            } else {
                showItemId(0L);
            }
            setLoadingScreen(false);
        } else if (loader.getId() == WALLETS_LOADER_ID) {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                    if (i != 0) {
                        builder.append(", ");
                    }
                    String name = cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME));
                    builder.append(name);
                }
                mWalletsTextView.setText(builder);
                mWalletsTextView.setVisibility(View.VISIBLE);
            } else {
                mWalletsTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }
}