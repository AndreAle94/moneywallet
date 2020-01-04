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

package com.oriondev.moneywallet.ui.adapter.pager;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.PeriodDetailFlowData;
import com.oriondev.moneywallet.ui.view.chart.PieChart;

/**
 * Created by andrea on 14/08/18.
 */
public class PieChartViewPagerAdapter extends PagerAdapter {

    private PeriodDetailFlowData mData;

    public PieChartViewPagerAdapter() {
        // empty constructor
    }

    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_pie_chart_item, container, false);
        PieChart pieChart = view.findViewById(R.id.pie_chart_view);
        /*
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setTouchEnabled(false);
        if (mData != null && mData.getChartCount() > 0) {
            pieChart.setData(mData.getChartData(position));
        } else {
            pieChart.setData(null);
        }*/
        if (mData != null && mData.getChartCount() > 0) {
            pieChart.setChartData(mData.getChartData(position));
        } else {
            pieChart.setChartData(null);
        }
        container.addView(view);
        return view;
    }

    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mData != null ? mData.getChartCount() : 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void setData(PeriodDetailFlowData data) {
        mData = data;
        notifyDataSetChanged();
    }
}