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

package com.oriondev.moneywallet.ui.fragment.singlepanel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.background.OverviewDataLoader;
import com.oriondev.moneywallet.model.OverviewData;
import com.oriondev.moneywallet.model.OverviewSetting;
import com.oriondev.moneywallet.model.PeriodMoney;
import com.oriondev.moneywallet.picker.OverviewSettingPicker;
import com.oriondev.moneywallet.storage.preference.CurrentWalletController;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.PeriodDetailActivity;
import com.oriondev.moneywallet.ui.adapter.pager.OverviewChartViewPagerAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.OverviewItemAdapter;
import com.oriondev.moneywallet.ui.fragment.base.SinglePanelFragment;

/**
 * Created by andrea on 17/08/18.
 */
public class OverviewSinglePanelFragment extends SinglePanelFragment implements OverviewSettingPicker.Controller, LoaderManager.LoaderCallbacks<OverviewData>, OverviewItemAdapter.Controller, CurrentWalletController {

    private static final int LOADER_OVERVIEW_DATA = 34848;

    private static final String TAG_SETTING_PICKER = "OverviewSinglePanelFragment::Tag::OverviewSettingPicker";

    private OverviewChartViewPagerAdapter mViewPagerAdapter;
    private OverviewItemAdapter mRecyclerViewAdapter;

    private OverviewSettingPicker mOverviewSettingPicker;

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
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_overview, parent, true);
        ViewPager viewPager = view.findViewById(R.id.chart_view_pager);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // initialize adapters
        mViewPagerAdapter = new OverviewChartViewPagerAdapter();
        mRecyclerViewAdapter = new OverviewItemAdapter(this);
        // attach adapters
        viewPager.setAdapter(mViewPagerAdapter);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        // initialize picker
        FragmentManager fragmentManager = getChildFragmentManager();
        mOverviewSettingPicker = OverviewSettingPicker.createPicker(fragmentManager, TAG_SETTING_PICKER);
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_overview;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_overview_fragment;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.advanced_settings) {
            mOverviewSettingPicker.showPicker();
        }
        return false;
    }

    @Override
    public void onOverviewSettingChanged(String tag, OverviewSetting overviewSetting) {
        getLoaderManager().restartLoader(LOADER_OVERVIEW_DATA, null, this);
    }

    @NonNull
    @Override
    public Loader<OverviewData> onCreateLoader(int id, Bundle args) {
        OverviewSetting setting = mOverviewSettingPicker.getCurrentSettings();
        return new OverviewDataLoader(getActivity(), setting);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<OverviewData> loader, OverviewData data) {
        mViewPagerAdapter.setData(data);
        mRecyclerViewAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<OverviewData> loader) {
        // nothing to release
    }

    @Override
    public void onPeriodClick(PeriodMoney periodMoney) {
        Intent intent = new Intent(getActivity(), PeriodDetailActivity.class);
        intent.putExtra(PeriodDetailActivity.START_DATE, periodMoney.getStartDate());
        intent.putExtra(PeriodDetailActivity.END_DATE, periodMoney.getEndDate());
        startActivity(intent);
    }

    @Override
    public void onCurrentWalletChanged(long walletId) {
        getLoaderManager().restartLoader(LOADER_OVERVIEW_DATA, null, this);
    }
}