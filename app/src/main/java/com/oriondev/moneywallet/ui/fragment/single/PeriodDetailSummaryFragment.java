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

package com.oriondev.moneywallet.ui.fragment.single;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itsronald.widget.ViewPagerIndicator;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.background.PeriodDetailSummaryLoader;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.model.PeriodDetailSummaryData;
import com.oriondev.moneywallet.model.PeriodMoney;
import com.oriondev.moneywallet.storage.preference.CurrentWalletController;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.adapter.pager.BarChartViewPagerAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.PeriodDetailSummaryAdapter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 01/05/18.
 */
public class PeriodDetailSummaryFragment extends Fragment implements PeriodDetailSummaryAdapter.Controller, LoaderManager.LoaderCallbacks<PeriodDetailSummaryData>,CurrentWalletController {

    private static final int LOADER_FRAGMENT_DATA = 3650;

    private static final String ARG_START_DATE = "PeriodDetailSummaryFragment::Arguments::StartDate";
    private static final String ARG_END_DATE = "PeriodDetailSummaryFragment::Arguments::EndDate";

    public static PeriodDetailSummaryFragment newInstance(Date start, Date end) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_START_DATE, start);
        arguments.putSerializable(ARG_END_DATE, end);
        PeriodDetailSummaryFragment fragment = new PeriodDetailSummaryFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private TextView mHeaderLeftTextView;
    private TextView mHeaderRightTextView;
    private RecyclerView mRecyclerView;

    private BarChartViewPagerAdapter mBarChartViewPagerAdapter;
    private ViewPagerIndicator mChartViewPagerIndicator;
    private PeriodDetailSummaryAdapter mRecyclerViewAdapter;

    private Date mStartDate;
    private Date mEndDate;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

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
        View view = inflater.inflate(R.layout.layout_panel_period_detail, container, false);
        mHeaderLeftTextView = view.findViewById(R.id.left_text_view);
        mHeaderRightTextView = view.findViewById(R.id.right_text_view);
        ViewPager barChartViewPager = view.findViewById(R.id.chart_view_pager);
        mChartViewPagerIndicator = view.findViewById(R.id.view_pager_indicator);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        // unpack the arguments bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mStartDate = (Date) arguments.getSerializable(ARG_START_DATE);
            mEndDate = (Date) arguments.getSerializable(ARG_END_DATE);
        } else {
            throw new IllegalStateException("The fragment must be initialized using the newInstance() method");
        }
        // create the adapter
        mBarChartViewPagerAdapter = new BarChartViewPagerAdapter();
        barChartViewPager.setAdapter(mBarChartViewPagerAdapter);
        mRecyclerViewAdapter = new PeriodDetailSummaryAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        // return the view
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mHeaderLeftTextView.setText(R.string.hint_net_incomes);
        getLoaderManager().restartLoader(LOADER_FRAGMENT_DATA, null, this);
    }

    @Override
    public void onPeriodClick(PeriodMoney periodMoney) {
        Intent intent = new Intent(getActivity(), TransactionListActivity.class);
        intent.putExtra(TransactionListActivity.START_DATE, periodMoney.getStartDate());
        intent.putExtra(TransactionListActivity.END_DATE, periodMoney.getEndDate());
        startActivity(intent);
    }

    @Override
    public Loader<PeriodDetailSummaryData> onCreateLoader(int id, Bundle args) {
        // TODO: [low] in future we can let the user choice the group type from the UI
        // but now it is not possible, just calculate which is the better strategy
        // depending on the date range
        int groupType;
        int daysBetween = DateUtils.getDaysBetween(mStartDate, mEndDate);
        if (daysBetween == 0) {
            groupType = PeriodDetailSummaryLoader.GROUP_BY_HOUR;
        } else if (daysBetween > 0 && daysBetween <= 7) {
            groupType = PeriodDetailSummaryLoader.GROUP_BY_DAY;
        } else if (daysBetween > 7 && daysBetween <= 31) {
            groupType = PeriodDetailSummaryLoader.GROUP_BY_WEEK;
        } else if (daysBetween > 31 && daysBetween <= 365) {
            groupType = PeriodDetailSummaryLoader.GROUP_BY_MONTH;
        } else {
            groupType = PeriodDetailSummaryLoader.GROUP_BY_YEAR;
        }
        return new PeriodDetailSummaryLoader(getActivity(), mStartDate, mEndDate, groupType);
    }

    @Override
    public void onLoadFinished(Loader<PeriodDetailSummaryData> loader, PeriodDetailSummaryData data) {
        if (data != null) {
            mMoneyFormatter.applyTinted(mHeaderRightTextView, data.getNetIncomes());
            mBarChartViewPagerAdapter.setData(data);
            mRecyclerViewAdapter.setData(data);
            mChartViewPagerIndicator.setVisibility(data.getChartCount() > 1 ? View.VISIBLE : View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mMoneyFormatter.applyNotTinted(mHeaderRightTextView, Money.empty());
            mBarChartViewPagerAdapter.setData(null);
            mRecyclerViewAdapter.setData(null);
            mChartViewPagerIndicator.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<PeriodDetailSummaryData> loader) {
        // nothing to release
    }

    @Override
    public void onCurrentWalletChanged(long walletId) {
        getLoaderManager().restartLoader(LOADER_FRAGMENT_DATA, null, this);
    }
}