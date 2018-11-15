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

package com.oriondev.moneywallet.ui.fragment.multipanel;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditWalletActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.WalletCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelAppBarItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.WalletItemFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 24/01/18.
 */
public class WalletMultiPanelFragment extends MultiPanelAppBarItemFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, WalletCursorAdapter.WalletActionListener {

    private static final String SECONDARY_FRAGMENT_TAG = "WalletMultiPanelFragment::Tag::SecondaryPanelFragment";

    private final static int LOADER_ID = 0;

    private TextView mTotalMoneyTextView;
    private AdvancedRecyclerView mAdvancedRecyclerView;

    private WalletCursorAdapter mAdapter;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreatePrimaryAppBar(LayoutInflater inflater, @NonNull ViewGroup primaryAppBarContainer, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_wallet_list_app_bar, primaryAppBarContainer, true);
        mTotalMoneyTextView = view.findViewById(R.id.total_money_text_view);
    }

    @Override
    protected void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState) {
        // inflate the layout and obtain all the view references
        View view = inflater.inflate(R.layout.view_wallet_list, primaryPanel, true);
        mAdvancedRecyclerView = view.findViewById(R.id.advanced_recycler_view);
        // setup all the views
        mAdapter = new WalletCursorAdapter(this);
        mAdvancedRecyclerView.setAdapter(mAdapter);
        mAdvancedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdvancedRecyclerView.setEmptyText(R.string.message_no_wallet_found);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        // attach a background loader to retrieve data from the content provider
        getLoaderManager().initLoader(LOADER_ID, null, this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
    }

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel() {
        return new WalletItemFragment();
    }

    @Override
    protected String getSecondaryFragmentTag() {
        return SECONDARY_FRAGMENT_TAG;
    }

    @Override
    @StringRes
    protected int getTitleRes() {
        return R.string.action_manage_wallets;
    }

    @Override
    protected void onFloatingActionButtonClick() {
        Intent intent = new Intent(getActivity(), NewEditWalletActivity.class);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getActivity();
        if (context != null) {
            Uri uri = DataContentProvider.CONTENT_WALLETS;
            String[] projection = new String[] {
                    Contract.Wallet.ID,
                    Contract.Wallet.NAME,
                    Contract.Wallet.ICON,
                    Contract.Wallet.CURRENCY,
                    Contract.Wallet.START_MONEY,
                    Contract.Wallet.COUNT_IN_TOTAL,
                    Contract.Wallet.TOTAL_MONEY
            };
            String sortOrder = Contract.Wallet.NAME;
            return new CursorLoader(context, uri, projection, null, null, sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            Money money = new Money();
            int indexCurrency = cursor.getColumnIndex(Contract.Wallet.CURRENCY);
            int indexInTotal = cursor.getColumnIndex(Contract.Wallet.COUNT_IN_TOTAL);
            int indexWalletInitial = cursor.getColumnIndex(Contract.Wallet.START_MONEY);
            int indexWalletTotal = cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (cursor.getInt(indexInTotal) == 1) {
                    String currency = cursor.getString(indexCurrency);
                    long amount = cursor.getLong(indexWalletInitial) + cursor.getLong(indexWalletTotal);
                    money.addMoney(currency, amount);
                }
            }
            mMoneyFormatter.applyNotTinted(mTotalMoneyTextView, money);
        }
        mAdapter.changeCursor(cursor);
        if (cursor != null && cursor.getCount() > 0) {
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.READY);
        } else {
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.EMPTY);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.REFRESHING);
    }

    @Override
    public void onWalletClick(long id) {
        showItemId(id);
        showSecondaryPanel();
    }
}