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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 09/02/18.
 */

public abstract class MultiPanelViewPagerFragment extends MultiPanelAppBarFragment implements ViewPager.OnPageChangeListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreatePrimaryAppBar(LayoutInflater inflater, @NonNull ViewGroup primaryAppBar, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_multi_panel_view_pager_app_bar, primaryAppBar, true);
        mTabLayout = view.findViewById(R.id.tab_layout);
    }

    @Override
    protected void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_multi_panel_view_pager_body, primaryPanel, true);
        mViewPager = view.findViewById(R.id.view_pager);
        // setup the view pager and the tab layout
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(onCreatePagerAdapter(getChildFragmentManager()));
        mViewPager.addOnPageChangeListener(this);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState) {

    }

    @NonNull
    protected abstract PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager);

    protected int getViewPagerPosition() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(this);
    }
}