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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.oriondev.moneywallet.ui.activity.NewEditTransactionActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.SavingCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.CursorListFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 03/03/18.
 */
public class SavingListFragment extends CursorListFragment implements SavingCursorAdapter.ActionListener {

    private static final String ARG_SAVING_COMPLETED = "SavingListFragment::Arguments::SavingCompleted";

    public static SavingListFragment newInstance(boolean completed) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_SAVING_COMPLETED, completed);
        SavingListFragment fragment = new SavingListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_saving_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new SavingCursorAdapter(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity != null && arguments != null) {
            // unpack the argument bundle
            boolean completed = arguments.getBoolean(ARG_SAVING_COMPLETED, true);
            // query the content provider
            Uri uri = DataContentProvider.CONTENT_SAVINGS;
            String[] projection = new String[] {
                    Contract.Saving.ID,
                    Contract.Saving.ICON,
                    Contract.Saving.DESCRIPTION,
                    Contract.Saving.COMPLETE,
                    Contract.Saving.START_MONEY,
                    Contract.Saving.END_MONEY,
                    Contract.Saving.WALLET_CURRENCY,
                    Contract.Saving.PROGRESS
            };
            String selection;
            String[] selectionArgs;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.Saving.WALLET_COUNT_IN_TOTAL + " = 1 AND " + Contract.Saving.COMPLETE + " = ?";
                selectionArgs = new String[] {String.valueOf(completed ? 1 : 0)};
            } else {
                selection = Contract.Saving.WALLET_ID + " = ? AND " + Contract.Saving.COMPLETE + " = ?";
                selectionArgs = new String[] {String.valueOf(currentWallet), String.valueOf(completed ? 1 : 0)};
            }
            String sortOrder = Contract.Saving.ID + " DESC";
            return new CursorLoader(activity, uri, projection, selection, selectionArgs, sortOrder);
        }
        return null;
    }

    @Override
    public void onSavingClick(long id) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(LocalAction.ACTION_ITEM_CLICK);
            intent.putExtra(Message.ITEM_ID, id);
            intent.putExtra(Message.ITEM_TYPE, Message.TYPE_SAVING);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    @Override
    public void onWithdrawEverything(long id) {
        Intent intent = new Intent(getActivity(), NewEditTransactionActivity.class);
        intent.putExtra(NewEditTransactionActivity.TYPE, NewEditTransactionActivity.TYPE_SAVING);
        intent.putExtra(NewEditTransactionActivity.SAVING_ID, id);
        intent.putExtra(NewEditTransactionActivity.SAVING_ACTION, NewEditTransactionActivity.SAVING_WITHDRAW_EVERYTHING);
        startActivity(intent);
    }

    @Override
    public void onWithdraw(long id) {
        Intent intent = new Intent(getActivity(), NewEditTransactionActivity.class);
        intent.putExtra(NewEditTransactionActivity.TYPE, NewEditTransactionActivity.TYPE_SAVING);
        intent.putExtra(NewEditTransactionActivity.SAVING_ID, id);
        intent.putExtra(NewEditTransactionActivity.SAVING_ACTION, NewEditTransactionActivity.SAVING_WITHDRAW);
        startActivity(intent);
    }

    @Override
    public void onDeposit(long id) {
        Intent intent = new Intent(getActivity(), NewEditTransactionActivity.class);
        intent.putExtra(NewEditTransactionActivity.TYPE, NewEditTransactionActivity.TYPE_SAVING);
        intent.putExtra(NewEditTransactionActivity.SAVING_ID, id);
        intent.putExtra(NewEditTransactionActivity.SAVING_ACTION, NewEditTransactionActivity.SAVING_DEPOSIT);
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