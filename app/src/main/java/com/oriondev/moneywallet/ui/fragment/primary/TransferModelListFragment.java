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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditTransferActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.TransferModelCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.CursorListFragment;
import com.oriondev.moneywallet.ui.fragment.multipanel.ModelMultiPanelViewPagerFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 14/03/18.
 */
public class TransferModelListFragment extends CursorListFragment implements TransferModelCursorAdapter.ActionListener {

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_model_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new TransferModelCursorAdapter(this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = DataContentProvider.CONTENT_TRANSFER_MODELS;
            String[] projection = new String[] {
                    Contract.TransferModel.ID,
                    Contract.TransferModel.WALLET_FROM_NAME,
                    Contract.TransferModel.WALLET_FROM_CURRENCY,
                    Contract.TransferModel.WALLET_TO_NAME,
                    Contract.TransferModel.WALLET_TO_CURRENCY,
                    Contract.TransferModel.WALLET_TO_ICON,
                    Contract.TransferModel.MONEY_FROM,
                    Contract.TransferModel.MONEY_TO,
                    Contract.TransferModel.MONEY_TAX
            };
            String selection;
            String[] selectionArgs;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.TransferModel.WALLET_FROM_COUNT_IN_TOTAL + " = 1 OR " +
                            Contract.TransferModel.WALLET_TO_COUNT_IN_TOTAL + " = 1";
                selectionArgs = null;
            } else {
                selection = Contract.TransferModel.WALLET_FROM_ID + " = ? OR " + Contract.TransferModel.WALLET_TO_ID + " = ?";
                selectionArgs = new String[] {String.valueOf(currentWallet), String.valueOf(currentWallet)};
            }
            return new CursorLoader(activity, uri, projection, selection, selectionArgs, null);
        } else {
            throw new IllegalStateException("Activity is null");
        }
    }

    @Override
    public void onModelClick(long modelId) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(LocalAction.ACTION_ITEM_CLICK);
            intent.putExtra(Message.ITEM_ID, modelId);
            intent.putExtra(Message.ITEM_TYPE, Message.TYPE_TRANSFER_MODEL);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    @Override
    public void onModelAddClick(long modelId) {
        Uri uri = NewEditTransferActivity.insertTransferFromModel(getActivity(), modelId);
        if (uri != null) {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ModelMultiPanelViewPagerFragment) {
                ((ModelMultiPanelViewPagerFragment) parentFragment).onTransferAdded(uri);
            }
        } else {
            // TODO: alert user of the failure.
        }
    }

    @Override
    public void onModelEditClick(long modelId) {
        Intent intent = new Intent(getActivity(), NewEditTransferActivity.class);
        intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.NEW_ITEM);
        intent.putExtra(NewEditTransferActivity.TYPE, NewEditTransferActivity.TYPE_MODEL);
        intent.putExtra(NewEditTransferActivity.MODEL_ID, modelId);
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