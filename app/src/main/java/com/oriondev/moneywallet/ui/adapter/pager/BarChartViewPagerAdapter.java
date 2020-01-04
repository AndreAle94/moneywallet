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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.PeriodDetailSummaryData;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 16/08/18.
 */
public class BarChartViewPagerAdapter  extends PagerAdapter {

    private PeriodDetailSummaryData mData;

    public BarChartViewPagerAdapter() {
        // empty constructor
    }

    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_bar_chart_item, container, false);
        BarChart barChart = view.findViewById(R.id.bar_chart_view);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawMarkers(true);
        if (mData != null && mData.getChartCount() > 0) {
            BarData barData = mData.getChartData(position);
            CurrencyUnit currencyUnit = mData.getChartCurrency(position);
            barChart.setData(barData);
            XAxis xAxis = barChart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(true);
            xAxis.setAxisMinimum(0f);
            xAxis.setAxisMaximum(mData.getPeriodCount());
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return String.valueOf((int) value + 1);
                }

            });
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setValueFormatter(new IMoneyFormatter(currencyUnit));
            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(35f);
            leftAxis.setAxisMinimum(0f);
            barChart.getAxisRight().setEnabled(false);
            barData.setBarWidth(0.4f);
            barData.setHighlightEnabled(false);
            barData.setValueTextColor(xAxis.getTextColor());
            barData.setValueFormatter(new IMoneyFormatter(currencyUnit));
            barChart.groupBars(0, 0.08f, 0.06f);
        } else {
            barChart.setData(null);
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

    public void setData(PeriodDetailSummaryData data) {
        mData = data;
        notifyDataSetChanged();
    }

    private static class IMoneyFormatter implements IAxisValueFormatter, IValueFormatter {

        private final CurrencyUnit mCurrencyUnit;
        private final MoneyFormatter mMoneyFormatter;

        private IMoneyFormatter(CurrencyUnit currencyUnit) {
            mCurrencyUnit = currencyUnit;
            mMoneyFormatter = MoneyFormatter.getInstance();
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mMoneyFormatter.getNotTintedString(mCurrencyUnit, (long) value);
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mMoneyFormatter.getNotTintedString(mCurrencyUnit, (long) value);
        }
    }
}