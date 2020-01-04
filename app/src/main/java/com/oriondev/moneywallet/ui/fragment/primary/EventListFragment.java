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
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.EventCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.CursorListFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

/**
 * Created by andrea on 03/03/18.
 */
public class EventListFragment extends CursorListFragment implements EventCursorAdapter.ActionListener {

    private static final String ARG_EVENT_COMPLETED = "EventListFragment::Arguments::EventCompleted";

    public static EventListFragment newInstance(boolean completed) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_EVENT_COMPLETED, completed);
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_event_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new EventCursorAdapter(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity != null && arguments != null) {
            // unpack the argument bundle
            boolean completed = arguments.getBoolean(ARG_EVENT_COMPLETED, true);
            // query the content provider
            Uri uri = DataContentProvider.CONTENT_EVENTS;
            String[] projection = new String[] {
                    Contract.Event.ID,
                    Contract.Event.ICON,
                    Contract.Event.NAME,
                    Contract.Event.END_DATE,
                    Contract.Event.PROGRESS
            };
            String selection;
            String sortOrder;
            if (completed) {
                selection = "DATE(" + Contract.Event.END_DATE + ") < DATE('now')";
                sortOrder = Contract.Event.END_DATE + " DESC";
            } else {
                selection = "DATE(" + Contract.Event.END_DATE + ") >= DATE('now')";
                sortOrder = Contract.Event.END_DATE + " ASC";
            }
            return new CursorLoader(activity, uri, projection, selection, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onEventClick(long id) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(LocalAction.ACTION_ITEM_CLICK);
            intent.putExtra(Message.ITEM_ID, id);
            intent.putExtra(Message.ITEM_TYPE, Message.TYPE_EVENT);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }
}