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
import com.oriondev.moneywallet.model.Group;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.storage.wrapper.TransferHeaderCursor;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.TransferCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.CursorListFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 03/03/18.
 */
public class TransferListFragment extends CursorListFragment implements TransferCursorAdapter.ActionListener {

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_transfer_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new TransferCursorAdapter(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = DataContentProvider.CONTENT_TRANSFERS;
            String selection;
            String[] arguments;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.Transfer.TRANSACTION_FROM_WALLET_COUNT_IN_TOTAL + " = 1 OR " +
                        Contract.Transfer.TRANSACTION_TO_WALLET_COUNT_IN_TOTAL + " = 1";
                arguments = null;
            } else {
                String walletId = String.valueOf(currentWallet);
                selection = Contract.Transfer.TRANSACTION_FROM_WALLET_ID + " = ? OR " +
                Contract.Transfer.TRANSACTION_TO_WALLET_ID + " = ?";
                arguments = new String[] {walletId, walletId};
            }
            selection += " AND DATETIME(" + Contract.Transfer.DATE + ") <= DATETIME('now', 'localtime')";
            String sortOrder = Contract.Transfer.DATE + " DESC";
            Group groupType = PreferenceManager.getCurrentGroupType();
            return new WrappedCursorLoader(activity, uri, null, selection, arguments,
                    sortOrder, groupType);
        }
        return null;
    }

    @Override
    public void onHeaderClick() {

    }

    @Override
    public void onTransferClick(long id) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(LocalAction.ACTION_ITEM_CLICK);
            intent.putExtra(Message.ITEM_ID, id);
            intent.putExtra(Message.ITEM_TYPE, Message.TYPE_TRANSFER);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    @Override
    protected boolean shouldRefreshOnCurrentWalletChange() {
        // this fragment content is dependant on the current
        // wallet when the loader is created, so the query
        // operation must be recreated from the beginning.
        return true;
    }

    private static class WrappedCursorLoader extends CursorLoader {

        private final Group mGroup;

        private WrappedCursorLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                                    @Nullable String[] selectionArgs, @Nullable String sortOrder, Group group) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mGroup = group;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();
            return new TransferHeaderCursor(cursor, mGroup);
        }
    }
}