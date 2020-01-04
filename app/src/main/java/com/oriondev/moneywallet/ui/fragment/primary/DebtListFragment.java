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

package com.oriondev.moneywallet.ui.fragment.primary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.Message;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.storage.wrapper.DebtHeaderCursor;
import com.oriondev.moneywallet.ui.activity.NewEditTransactionActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.DebtCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.CursorListFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 05/03/18.
 */

public class DebtListFragment extends CursorListFragment implements DebtCursorAdapter.ActionListener {

    private static final String ARG_DEBT_TYPE = "DebtListFragment::Arguments::DebtType";

    public static DebtListFragment newInstance(Contract.DebtType debtType) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_DEBT_TYPE, debtType);
        DebtListFragment fragment = new DebtListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_debt_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new DebtCursorAdapter(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity != null && arguments != null) {
            Contract.DebtType debtType = (Contract.DebtType) arguments.getSerializable(ARG_DEBT_TYPE);
            if (debtType == null) {
                return null;
            }
            Uri uri = DataContentProvider.CONTENT_DEBTS;
            String[] projection = new String[] {
                    Contract.Debt.ID,
                    Contract.Debt.TYPE,
                    Contract.Debt.ICON,
                    Contract.Debt.DESCRIPTION,
                    Contract.Debt.MONEY,
                    Contract.Debt.PROGRESS,
                    Contract.Debt.WALLET_CURRENCY,
                    Contract.Debt.EXPIRATION_DATE,
                    Contract.Debt.ARCHIVED,
                    Contract.Debt.PLACE_ID,
                    Contract.Debt.PLACE_NAME
            };
            String selection;
            String[] selectionArgs;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.Debt.WALLET_COUNT_IN_TOTAL + " = 1";
                selectionArgs = null;
            } else {
                selection = Contract.Debt.WALLET_ID + " = ?";
                selectionArgs = new String[] {String.valueOf(currentWallet)};
            }
            selection += " AND " + Contract.Debt.TYPE + " = " + String.valueOf(debtType.getValue());
            String sortOrder = Contract.Debt.ARCHIVED + " ASC, " + Contract.Debt.DATE + " DESC";
            return new WrappedCursorLoader(activity, uri, projection, selection, selectionArgs, sortOrder, debtType);
        }
        return null;
    }

    private static class WrappedCursorLoader extends CursorLoader {

        private final Contract.DebtType mDebtType;

        private WrappedCursorLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                                    @Nullable String[] selectionArgs, @Nullable String sortOrder, Contract.DebtType debtType) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mDebtType = debtType;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();
            return new DebtHeaderCursor(cursor, mDebtType);
        }
    }

    @Override
    public void onDebtClick(long id) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(LocalAction.ACTION_ITEM_CLICK);
            intent.putExtra(Message.ITEM_ID, id);
            intent.putExtra(Message.ITEM_TYPE, Message.TYPE_DEBT);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    @Override
    public void onPayClick(long id) {
        Intent intent = new Intent(getActivity(), NewEditTransactionActivity.class);
        intent.putExtra(NewEditTransactionActivity.TYPE, NewEditTransactionActivity.TYPE_DEBT);
        intent.putExtra(NewEditTransactionActivity.DEBT_ID, id);
        intent.putExtra(NewEditTransactionActivity.DEBT_ACTION, NewEditTransactionActivity.DEBT_PAY);
        startActivity(intent);
    }

    @Override
    public void onReceiveClick(long id) {
        Intent intent = new Intent(getActivity(), NewEditTransactionActivity.class);
        intent.putExtra(NewEditTransactionActivity.TYPE, NewEditTransactionActivity.TYPE_DEBT);
        intent.putExtra(NewEditTransactionActivity.DEBT_ID, id);
        intent.putExtra(NewEditTransactionActivity.DEBT_ACTION, NewEditTransactionActivity.DEBT_RECEIVE);
        startActivity(intent);
    }

    @Override
    protected boolean shouldRefreshOnCurrentWalletChange() {
        // this fragment content is dependant on the current
        // wallet when the loader is created, so the query
        // operation must be recreated from the beginning.
        return true;
    }
}