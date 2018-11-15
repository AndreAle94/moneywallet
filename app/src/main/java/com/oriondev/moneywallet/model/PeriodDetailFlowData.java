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

import com.oriondev.moneywallet.ui.view.chart.PieData;

import java.util.List;

/**
 * Created by andrea on 13/08/18.
 */
public class PeriodDetailFlowData {

    private final Money mTotalMoney;
    // private final List<PieData> mPieDataList;
    private final List<PieData> mPieDataList;
    private final List<CategoryMoney> mCategoryMoneyList;

    public PeriodDetailFlowData(Money totalMoney, List<PieData> pieDataList, List<CategoryMoney> categoryMoneyList) {
        mTotalMoney = totalMoney;
        mPieDataList = pieDataList;
        mCategoryMoneyList = categoryMoneyList;
    }

    public Money getTotalMoney() {
        return mTotalMoney;
    }

    public PieData getChartData(int index) {
        return mPieDataList.get(index);
    }

    public int getChartCount() {
        return mPieDataList.size();
    }

    public CategoryMoney getCategory(int index) {
        return mCategoryMoneyList.get(index);
    }

    public int getCategoryCount() {
        return mCategoryMoneyList.size();
    }
}