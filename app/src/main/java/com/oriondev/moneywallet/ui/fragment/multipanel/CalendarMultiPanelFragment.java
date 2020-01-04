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

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.TransactionCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelAppBarItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.TransactionItemFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.ui.view.calendar.MonthView;
import com.oriondev.moneywallet.ui.view.calendar.OnDateSelectedListener;
import com.oriondev.moneywallet.ui.view.calendar.TimelineView;
import com.oriondev.moneywallet.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 06/04/18.
 */
public class CalendarMultiPanelFragment extends MultiPanelAppBarItemFragment implements MonthView.OnMonthSelectedListener, OnDateSelectedListener, SwipeRefreshLayout.OnRefreshListener, TransactionCursorAdapter.ActionListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SECONDARY_PANEL_FRAGMENT_TAG = "CalendarMultiPanelFragment::Tag::TransactionItemFragment";

    private static final String ARG_SELECTED_YEAR = "CalendarMultiPanelFragment::Arguments::Year";
    private static final String ARG_SELECTED_MONTH = "CalendarMultiPanelFragment::Arguments::Month";
    private static final String ARG_SELECTED_DAY = "CalendarMultiPanelFragment::Arguments::Day";

    private static final int DEFAULT_LOADER_ID = 4834;

    private MonthView mMonthView;
    private TimelineView mTimelineView;
    private AdvancedRecyclerView mAdvancedRecyclerView;
    private AbstractCursorAdapter mAbstractCursorAdapter;

    @Override
    protected View onInflateRootLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_panel_appbar_without_scroll, container, false);
    }

    @Override
    protected void onCreatePrimaryAppBar(LayoutInflater inflater, @NonNull ViewGroup primaryAppBar, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_calendar_app_bar_layout, primaryAppBar, true);
        mMonthView = view.findViewById(R.id.month_view);
        mMonthView.setFirstDate(1900, Calendar.JANUARY);
        mMonthView.setOnMonthSelectedListener(this);
    }

    @Override
    protected void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_calendar_primary_panel, primaryPanel, true);
        mTimelineView = view.findViewById(R.id.timeline_view);
        mAdvancedRecyclerView = view.findViewById(R.id.advanced_recycler_view);
        mTimelineView.setFirstDate(1900, Calendar.JANUARY, 1);
        mTimelineView.setLastDate(2100, Calendar.DECEMBER, 31);
        mTimelineView.setOnDateSelectedListener(this);
        mAbstractCursorAdapter = new TransactionCursorAdapter(this);
        mAdvancedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdvancedRecyclerView.setEmptyText(R.string.message_no_transaction_found);
        mAdvancedRecyclerView.setAdapter(mAbstractCursorAdapter);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
        Calendar calendar = Calendar.getInstance();
        mTimelineView.setSelectedDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel() {
        return new TransactionItemFragment();
    }

    @Override
    protected String getSecondaryFragmentTag() {
        return SECONDARY_PANEL_FRAGMENT_TAG;
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_activity_calendar;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public void onMonthSelected(int year, int month, int index) {
        mTimelineView.setSelectedDate(year, month, 1);
    }

    @Override
    public void onDateSelected(int year, int month, int day, int index) {
        mMonthView.setSelectedMonth(year, month, false, true);
        loadTransactions(year, month, day);
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
    }

    @Override
    public void onHeaderClick(Date startDate, Date endDate) {
        // never used here
    }

    @Override
    public void onTransactionClick(long id) {
        showItemId(id);
        showSecondaryPanel();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null && args != null) {
            Date date = DateUtils.getDate(
                    args.getInt(ARG_SELECTED_YEAR),
                    args.getInt(ARG_SELECTED_MONTH),
                    args.getInt(ARG_SELECTED_DAY)
            );
            Uri uri = DataContentProvider.CONTENT_TRANSACTIONS;
            String selection;
            String[] arguments;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.Transaction.WALLET_COUNT_IN_TOTAL + " = 1";
                arguments = null;
            } else {
                selection = Contract.Transaction.WALLET_ID + " = ?";
                arguments = new String[] {String.valueOf(currentWallet)};
            }
            selection += " AND DATE(" + Contract.Transaction.DATE + ") == DATE('" + DateUtils.getSQLDateString(date) + "')";
            System.out.println(selection);
            String sortOrder = Contract.Transaction.DATE + " DESC";
            return new CursorLoader(activity, uri, null, selection, arguments, sortOrder);
        }
        return null;
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
        loadTransactions(
                mTimelineView.getSelectedYear(),
                mTimelineView.getSelectedMonth(),
                mTimelineView.getSelectedDay()
        );
        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.REFRESHING);
    }

    private void loadTransactions(int year, int month, int day) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_SELECTED_YEAR, year);
        arguments.putInt(ARG_SELECTED_MONTH, month);
        arguments.putInt(ARG_SELECTED_DAY, day);
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, arguments, this);
    }
}