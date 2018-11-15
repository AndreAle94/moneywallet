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

package com.oriondev.moneywallet.background;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.CategoryMoney;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.model.PeriodDetailFlowData;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.chart.PieData;
import com.oriondev.moneywallet.ui.view.chart.PieSlice;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrea on 13/08/18.
 */
public class PeriodDetailFlowLoader extends AbstractGenericLoader<PeriodDetailFlowData> {

    /*
    private static final int[] mColors = new int[] {
            Color.rgb(204, 198, 24),
            Color.rgb(229, 163, 25),
            Color.rgb(232, 111, 40),
            Color.rgb(212, 75, 145),
            Color.rgb(117, 96, 165),
            Color.rgb(54, 142, 92),
            Color.rgb(129, 191, 22),
            Color.rgb(224, 184, 26),
            Color.rgb(229, 138, 24),
            Color.rgb(235, 89, 92),
            Color.rgb(167, 78, 160),
            Color.rgb(66, 117, 138),
            Color.rgb(85, 169, 48)
    };*/

    private final Date mStartDate;
    private final Date mEndDate;
    private final boolean mIncomes;

    public PeriodDetailFlowLoader(Context context, Date startDate, Date endDate, boolean incomes) {
        super(context);
        mStartDate = startDate;
        mEndDate = endDate;
        mIncomes = incomes;
    }

    @Override @SuppressLint("UseSparseArrays")
    public PeriodDetailFlowData loadInBackground() {
        Money totalMoney = new Money();
        Map<CurrencyUnit, PieData> pieDataSets = new HashMap<>();
        Map<Long, CategoryMoney> categoryMoneyMap = new HashMap<>();
        // load from content resolver
        Map<Long, Category> categoryCache = loadCategoryCache();
        Uri uri = DataContentProvider.CONTENT_TRANSACTIONS;
        String[] projection = new String[] {
                Contract.Transaction.CATEGORY_ID,
                Contract.Transaction.CATEGORY_PARENT_ID,
                Contract.Transaction.MONEY,
                Contract.Transaction.WALLET_CURRENCY
        };
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
        selection += " AND " + Contract.Transaction.CONFIRMED + " = '1' AND " + Contract.Transaction.COUNT_IN_TOTAL + " = '1'";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('now', 'localtime')";
        selection += " AND " + Contract.Transaction.DIRECTION + " = " + (mIncomes ? Contract.Direction.INCOME : Contract.Direction.EXPENSE);
        if (mStartDate != null) {
            selection += " AND DATETIME(" + Contract.Transaction.DATE + ") >= DATETIME('" + DateUtils.getSQLDateTimeString(mStartDate) + "')";
        }
        if (mEndDate != null) {
            selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('" + DateUtils.getSQLDateTimeString(mEndDate) + "')";
        }
        String sortOrder = Contract.Transaction.CATEGORY_ID;
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long categoryId;
                    if (cursor.isNull(cursor.getColumnIndex(Contract.Transaction.CATEGORY_PARENT_ID))) {
                        categoryId = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.CATEGORY_ID));
                    } else {
                        categoryId = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.CATEGORY_PARENT_ID));
                    }
                    long money = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY));
                    String iso = cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY));
                    if (categoryMoneyMap.containsKey(categoryId)) {
                        CategoryMoney categoryMoney = categoryMoneyMap.get(categoryId);
                        categoryMoney.getMoney().addMoney(iso, money);
                    } else {
                        Category category = categoryCache.get(categoryId);
                        if (category != null) {
                            // if category is null it means that the category must not be showed
                            // inside the reports
                            CategoryMoney categoryMoney = new CategoryMoney(
                                    categoryId,
                                    category.getName(),
                                    category.getIcon(),
                                    new Money(iso, money)
                            );
                            categoryMoneyMap.put(categoryId, categoryMoney);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        // now we have all the necessary data stored inside the map, we can iterate all the
        // category and fill the chart data and the total money item
        List<CategoryMoney> categoryMoneyList = new ArrayList<>();
        for (CategoryMoney categoryMoney : categoryMoneyMap.values()) {
            Money money = categoryMoney.getMoney();
            totalMoney.addMoney(money);
            for (Map.Entry<String, Long> entry : money.getCurrencyMoneys().entrySet()) {
                CurrencyUnit currency = CurrencyManager.getCurrency(entry.getKey());
                if (pieDataSets.containsKey(currency)) {
                    PieData pieData = pieDataSets.get(currency);
                    pieData.add(new PieSlice(categoryMoney.getName(), entry.getValue(), categoryMoney.getIcon().getDrawable(getContext())));
                } else {
                    PieData pieData = new PieData();
                    //entries.add(new PieEntry(entry.getValue(), categoryMoney.getName(), categoryMoney.getIcon().getDrawable(getContext())));
                    pieData.add(new PieSlice(categoryMoney.getName(), entry.getValue(), categoryMoney.getIcon().getDrawable(getContext())));
                    pieDataSets.put(currency, pieData);
                }
                // --->
                /* === USE THIS CODE IF MPAndroidChart library is used ===
                currency = CurrencyManager.getCurrency("USD");
                if (pieDataSets.containsKey(currency)) {
                    List<PieEntry> entries = pieDataSets.get(currency);
                    entries.add(new PieEntry(entry.getValue(), categoryMoney.getName()));
                } else {
                    List<PieEntry> entries = new ArrayList<>();
                    entries.add(new PieEntry(entry.getValue(), categoryMoney.getName()));
                    pieDataSets.put(currency, entries);
                }*/
                // <---
            }
            categoryMoneyList.add(categoryMoney);
        }
        // buildMaterialDialog the return object
        /*
        List<PieData> pieDataList = new ArrayList<>();
        for (Map.Entry<CurrencyUnit, List<PieEntry>> entry : pieDataSets.entrySet()) {
            String name = entry.getKey().getName();
            PieDataSet pieDataSet = new PieDataSet(entry.getValue(), name);
            pieDataSet.setColors(mColors);
            pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            pieDataList.add(new PieData(pieDataSet));
        }*/
        List<PieData> pieDataList = new ArrayList<>();
        for (Map.Entry<CurrencyUnit, PieData> entry : pieDataSets.entrySet()) {
            pieDataList.add(entry.getValue());
        }
        return new PeriodDetailFlowData(totalMoney, pieDataList, categoryMoneyList);
    }

    @SuppressLint("UseSparseArrays")
    private Map<Long, Category> loadCategoryCache() {
        Map<Long, Category> cache = new HashMap<>();
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        String[] projection = new String[] {
                Contract.Category.ID,
                Contract.Category.NAME,
                Contract.Category.ICON,
                Contract.Category.TYPE
        };
        String selection = Contract.Category.PARENT + " IS NULL AND " +
                Contract.Category.SHOW_REPORT + " = '1'";
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long categoryId = cursor.getLong(cursor.getColumnIndex(Contract.Category.ID));
                    Category category = new Category(categoryId,
                            cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)),
                            IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON))),
                            Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)))
                    );
                    cache.put(categoryId, category);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cache;
    }
}