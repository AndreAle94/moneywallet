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

package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditEventActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.EventSelectorCursorAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.DateUtils;

import java.util.Date;

/**
 * Created by andre on 19/03/2018.
 */
public class EventPickerDialog extends DialogFragment implements EventSelectorCursorAdapter.Controller, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SS_SELECTED_EVENT = "EventPickerDialog::SavedState::SelectedEvent";
    private static final String SS_FILTER_DATE = "EventPickerDialog::SavedState::FilterDate";

    private static final int DEFAULT_LOADER_ID = 1;

    public static EventPickerDialog newInstance() {
        return new EventPickerDialog();
    }

    private Event mEvent;
    private Date mFilterDate;

    private Callback mCallback;

    private RecyclerView mRecyclerView;
    private TextView mMessageTextView;

    private EventSelectorCursorAdapter mCursorAdapter;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mEvent = savedInstanceState.getParcelable(SS_SELECTED_EVENT);
            mFilterDate = (Date) savedInstanceState.getSerializable(SS_FILTER_DATE);
        }
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_event_picker_title)
                .positiveText(R.string.action_new)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.dialog_advanced_list, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(getActivity(), NewEditEventActivity.class));
                    }

                })
                .build();
        mCursorAdapter = new EventSelectorCursorAdapter(this);
        View view = dialog.getCustomView();
        if (view != null) {
            mRecyclerView = view.findViewById(R.id.recycler_view);
            mMessageTextView = view.findViewById(R.id.message_text_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.setAdapter(mCursorAdapter);
            mMessageTextView.setText(R.string.message_no_event_found);
        }
        mRecyclerView.setVisibility(View.GONE);
        mMessageTextView.setVisibility(View.GONE);
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_SELECTED_EVENT, mEvent);
        outState.putSerializable(SS_FILTER_DATE, mFilterDate);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, Event event, Date filterDate) {
        mEvent = event;
        mFilterDate = filterDate;
        show(fragmentManager, tag);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = DataContentProvider.CONTENT_EVENTS;
            String[] projection = new String[]{
                    Contract.Event.ID,
                    Contract.Event.NAME,
                    Contract.Event.ICON,
                    Contract.Event.START_DATE,
                    Contract.Event.END_DATE
            };
            String where = null;
            String[] whereArgs = null;
            if (mFilterDate != null) {
                where = "DATE(?) BETWEEN DATE(" + Contract.Event.START_DATE + ") AND (" +
                        Contract.Event.END_DATE + ")";
                whereArgs = new String[] {DateUtils.getSQLDateString(mFilterDate)};
            }
            String sortOrder = Contract.Event.NAME;
            return new CursorLoader(activity, uri, projection, where, whereArgs, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        if (data != null && data.getCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mMessageTextView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onEventClick(Event event) {
        mCallback.onEventSelected(event);
        dismiss();
    }

    @Override
    public boolean isEventSelected(long id) {
        return mEvent != null && mEvent.getId() == id;
    }

    public interface Callback {

        void onEventSelected(Event event);
    }
}