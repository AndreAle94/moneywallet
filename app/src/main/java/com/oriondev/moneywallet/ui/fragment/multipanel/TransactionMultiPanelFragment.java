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
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Group;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.CurrentWalletController;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.storage.wrapper.TransactionHeaderCursor;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.TransactionCursorAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelCursorListItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.TransactionItemFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.utils.DateUtils;

import java.util.Date;

/**
 * Created by andrea on 08/04/18.
 */
public class TransactionMultiPanelFragment extends MultiPanelCursorListItemFragment implements TransactionCursorAdapter.ActionListener, CurrentWalletController {

    private static final String FILTER_TYPE = "TransactionMultiPanelFragment::Arguments::FilterType";
    private static final String FILTER_ID = "TransactionMultiPanelFragment::Arguments::FilterId";
    private static final String FILTER_START_DATE = "TransactionMultiPanelFragment::Arguments::FilterStartDate";
    private static final String FILTER_END_DATE = "TransactionMultiPanelFragment::Arguments::FilterEndDate";

    private static final String SECONDARY_PANEL_TAG = "TransactionMultiPanelFragment::Tag::SecondaryPanel";

    public enum FilterType {
        CATEGORY,
        DEBT,
        BUDGET,
        SAVING,
        EVENT,
        PLACE,
        PERSON
    }

    public static TransactionMultiPanelFragment newInstance(FilterType type, long id, Date startDate, Date endDate) {
        TransactionMultiPanelFragment fragment = new TransactionMultiPanelFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(FILTER_TYPE, type);
        arguments.putLong(FILTER_ID, id);
        arguments.putSerializable(FILTER_START_DATE, startDate);
        arguments.putSerializable(FILTER_END_DATE, endDate);
        fragment.setArguments(arguments);
        return fragment;
    }

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

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel() {
        return new TransactionItemFragment();
    }

    @Override
    protected String getSecondaryFragmentTag() {
        return SECONDARY_PANEL_TAG;
    }

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyText(R.string.message_no_transaction_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new TransactionCursorAdapter(this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity != null && arguments != null) {
            Uri uri = DataContentProvider.CONTENT_TRANSACTIONS;
            FilterType type = (FilterType) arguments.getSerializable(FILTER_TYPE);
            long itemId = arguments.getLong(FILTER_ID);
            Date startDate = (Date) arguments.getSerializable(FILTER_START_DATE);
            Date endDate = (Date) arguments.getSerializable(FILTER_END_DATE);
            if (type != null) {
                switch (type) {
                    case CATEGORY:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, itemId);
                        break;
                    case DEBT:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_DEBTS, itemId);
                        break;
                    case BUDGET:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_BUDGETS, itemId);
                        break;
                    case SAVING:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_SAVINGS, itemId);
                        break;
                    case EVENT:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_EVENTS, itemId);
                        break;
                    case PLACE:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PLACES, itemId);
                        break;
                    case PERSON:
                        uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PEOPLE, itemId);
                        break;
                }
                uri = Uri.withAppendedPath(uri, "transactions");
            }
            String selection;
            String[] selectionArgs;
            long currentWallet = PreferenceManager.getCurrentWallet();
            if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                selection = Contract.Transaction.WALLET_COUNT_IN_TOTAL + " = 1";
                selectionArgs = null;
            } else {
                selection = Contract.Transaction.WALLET_ID + " = ?";
                selectionArgs = new String[] {String.valueOf(currentWallet)};
            }
            selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('now', 'localtime')";
            if (startDate != null) {
                selection += " AND DATETIME(" + Contract.Transaction.DATE + ") >= DATETIME('" + DateUtils.getSQLDateTimeString(startDate) + "')";
            }
            if (endDate != null) {
                selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('" + DateUtils.getSQLDateTimeString(endDate) + "')";
            }
            String sortOrder = Contract.Transaction.DATE + " DESC";
            Group groupType = PreferenceManager.getCurrentGroupType();
            return new WrappedCursorLoader(activity, uri, null, selection, selectionArgs, sortOrder, groupType, startDate, endDate);
        }
        return null;
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_transaction;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public void onHeaderClick(Date startDate, Date endDate) {

    }

    @Override
    public void onTransactionClick(long id) {
        showItemId(id);
        showSecondaryPanel();
    }

    @Override
    public void onCurrentWalletChanged(long walletId) {
        recreateLoader();
    }

    private static class WrappedCursorLoader extends CursorLoader {

        private final Group mGroup;
        private final Date mStartDate;
        private final Date mEndDate;

        private WrappedCursorLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                                    @Nullable String[] selectionArgs, @Nullable String sortOrder, Group group, Date startDate, Date endDate) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mGroup = group;
            mStartDate = startDate;
            mEndDate = endDate;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = super.loadInBackground();
            return new TransactionHeaderCursor(cursor, mGroup, mStartDate, mEndDate);
        }
    }
}