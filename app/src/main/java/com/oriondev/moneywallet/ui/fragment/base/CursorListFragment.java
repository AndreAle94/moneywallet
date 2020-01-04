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

package com.oriondev.moneywallet.ui.fragment.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.storage.preference.CurrentWalletController;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 11/02/18.
 */
public abstract class CursorListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>, CurrentWalletController {

    private static final int DEFAULT_LOADER_ID = 24;

    private AdvancedRecyclerView mAdvancedRecyclerView;
    private AbstractCursorAdapter mAbstractCursorAdapter;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBroadcastReceiver = PreferenceManager.registerCurrentWalletObserver(context, this);
    }

    @Override
    public void onDetach() {
        PreferenceManager.unregisterCurrentWalletObserver(getActivity(), mBroadcastReceiver);
        super.onDetach();
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAdvancedRecyclerView = new AdvancedRecyclerView(getActivity());
        onPrepareRecyclerView(mAdvancedRecyclerView);
        mAbstractCursorAdapter = onCreateAdapter();
        mAdvancedRecyclerView.setAdapter(mAbstractCursorAdapter);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
        return mAdvancedRecyclerView;
    }

    protected abstract void onPrepareRecyclerView(AdvancedRecyclerView recyclerView);

    protected abstract AbstractCursorAdapter onCreateAdapter();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mAbstractCursorAdapter.changeCursor(cursor);
        if (cursor != null && cursor.getCount() > 0) {
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.READY);
        } else {
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.EMPTY);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAbstractCursorAdapter.changeCursor(null);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.REFRESHING);
    }

    protected boolean shouldRefreshOnCurrentWalletChange() {
        return false;
    }

    @Override
    public void onCurrentWalletChanged(long walletId) {
        if (shouldRefreshOnCurrentWalletChange()) {
            getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
        }
    }
}