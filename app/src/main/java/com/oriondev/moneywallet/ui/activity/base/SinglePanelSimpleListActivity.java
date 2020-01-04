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

package com.oriondev.moneywallet.ui.activity.base;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 09/02/18.
 */
public abstract class SinglePanelSimpleListActivity extends SinglePanelActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final int DEFAULT_LOADER_ID = 23;

    private AdvancedRecyclerView mAdvancedRecyclerView;
    private AbstractCursorAdapter mAbstractCursorAdapter;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_activity_single_panel_body_list, parent, true);
        mAdvancedRecyclerView = view.findViewById(R.id.advanced_recycler_view);
        onPrepareRecyclerView(mAdvancedRecyclerView);
        mAbstractCursorAdapter = onCreateAdapter();
        mAdvancedRecyclerView.setAdapter(mAbstractCursorAdapter);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        getSupportLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
    }

    protected abstract void onPrepareRecyclerView(AdvancedRecyclerView recyclerView);

    protected abstract AbstractCursorAdapter onCreateAdapter();

    protected AdvancedRecyclerView getAdvancedRecyclerView() {
        return mAdvancedRecyclerView;
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
        getSupportLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.REFRESHING);
    }
}