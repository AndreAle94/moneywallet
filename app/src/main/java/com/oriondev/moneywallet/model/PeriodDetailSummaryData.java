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

package com.oriondev.moneywallet.model;

import com.github.mikephil.charting.data.BarData;

import java.util.List;

/**
 * Created by andrea on 14/08/18.
 */
public class PeriodDetailSummaryData {

    private final Money mNetIncomes;
    private final List<BarData> mBarDataList;
    private final List<CurrencyUnit> mBarDataCurrencies;
    private final List<PeriodMoney> mPeriodList;

    public PeriodDetailSummaryData(Money netIncomes, List<BarData> barDataList, List<CurrencyUnit> currencyUnitList, List<PeriodMoney> periodMoneyList) {
        mNetIncomes = netIncomes;
        mBarDataList = barDataList;
        mBarDataCurrencies = currencyUnitList;
        mPeriodList = periodMoneyList;
    }

    public Money getNetIncomes() {
        return mNetIncomes;
    }

    public BarData getChartData(int index) {
        return mBarDataList.get(index);
    }

    public CurrencyUnit getChartCurrency(int index) {
        return mBarDataCurrencies.get(index);
    }

    public int getChartCount() {
        return mBarDataList.size();
    }

    public PeriodMoney getPeriodMoney(int index) {
        return mPeriodList.get(index);
    }

    public int getPeriodCount() {
        return mPeriodList.size();
    }
}