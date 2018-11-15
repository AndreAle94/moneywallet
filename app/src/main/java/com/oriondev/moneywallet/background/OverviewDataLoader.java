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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.model.OverviewData;
import com.oriondev.moneywallet.model.OverviewSetting;
import com.oriondev.moneywallet.model.PeriodMoney;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrea on 17/08/18.
 */
public class OverviewDataLoader extends AbstractGenericLoader<OverviewData> {

    private final static int[] COLOR_PALETTE = new int[] {
            Color.parseColor("#D32F2F"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#FDD835"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#039BE5"),
            Color.parseColor("#673AB7")
    };

    private final OverviewSetting mOverviewSetting;

    public OverviewDataLoader(Context context, OverviewSetting overviewSetting) {
        super(context);
        mOverviewSetting = overviewSetting;
    }

    @Override
    public OverviewData loadInBackground() {
        Money totalNetIncomes = new Money();
        List<PeriodMoney> periodMoneyList = new ArrayList<>();
        Uri uri = DataContentProvider.CONTENT_TRANSACTIONS;
        String[] projection = new String[] {
                Contract.Transaction.DATE,
                Contract.Transaction.DIRECTION,
                Contract.Transaction.WALLET_CURRENCY,
                Contract.Transaction.MONEY
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
        selection += " AND " + Contract.Transaction.CATEGORY_SHOW_REPORT + " = '1'";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('now', 'localtime')";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") >= DATETIME('" + DateUtils.getSQLDateTimeString(mOverviewSetting.getStartDate()) + "')";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('" + DateUtils.getSQLDateTimeString(mOverviewSetting.getEndDate()) + "')";
        switch (mOverviewSetting.getType()) {
            case CASH_FLOW:
                switch (mOverviewSetting.getCashFlow()) {
                    case INCOMES:
                        selection += " AND " + Contract.Transaction.DIRECTION + " = ?";
                        selectionArgs = appendSelectionArgs(selectionArgs, String.valueOf(Contract.Direction.INCOME));
                        break;
                    case EXPENSES:
                        selection += " AND " + Contract.Transaction.DIRECTION + " = ?";
                        selectionArgs = appendSelectionArgs(selectionArgs, String.valueOf(Contract.Direction.EXPENSE));
                        break;
                }
                break;
            case CATEGORY:
                selection += " AND (" + Contract.Transaction.CATEGORY_ID + " = ? OR " + Contract.Transaction.CATEGORY_PARENT_ID + " = ?)";
                selectionArgs = appendSelectionArgs(selectionArgs, String.valueOf(mOverviewSetting.getCategoryId()), String.valueOf(mOverviewSetting.getCategoryId()));
                break;
        }
        String sortOrder = Contract.Transaction.DATE + " ASC";
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            PeriodMoney currentPeriod = null;
            while (isAnotherPeriodNeeded(currentPeriod)) {
                currentPeriod = getNextPeriod(currentPeriod);
                if (!cursor.isAfterLast()) {
                    do {
                        Date date = DateUtils.getDateFromSQLDateTimeString(cursor.getString(cursor.getColumnIndex(Contract.Transaction.DATE)));
                        if (belongToPeriod(currentPeriod, date)) {
                            int direction = cursor.getInt(cursor.getColumnIndex(Contract.Transaction.DIRECTION));
                            String currency = cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY));
                            long money = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY));
                            if (direction == Contract.Direction.INCOME) {
                                currentPeriod.addIncome(currency, money);
                                totalNetIncomes.addMoney(currency, money);
                            } else if (direction == Contract.Direction.EXPENSE) {
                                currentPeriod.addExpense(currency, money);
                                totalNetIncomes.removeMoney(currency, money);
                            }
                        } else {
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                periodMoneyList.add(currentPeriod);
            }
            cursor.close();
        }
        // generate chart data
        List<IBarDataSet> barDataSets = new ArrayList<>();
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        List<IRadarDataSet> radarDataSets = new ArrayList<>();
        for (String currency : totalNetIncomes.getCurrencies()) {
            // for each currency we have to iterate the list of period money and generate
            // a valid chart data set for each chart types.
            List<BarEntry> barEntryList = new ArrayList<>();
            List<Entry> lineEntryList = new ArrayList<>();
            List<RadarEntry> radarEntryList = new ArrayList<>();
            // iterate period money items
            CurrencyUnit currencyUnit = CurrencyManager.getCurrency(currency);
            double divider = Math.pow(10, currencyUnit.getDecimals());
            for (int i = 0; i < periodMoneyList.size(); i++) {
                PeriodMoney periodMoney = periodMoneyList.get(i);
                long money = periodMoney.getNetIncomes().getMoney(currency);
                float value = (float) ((double) money / divider);
                barEntryList.add(new BarEntry(i, value));
                lineEntryList.add(new Entry(i, value));
                radarEntryList.add(new RadarEntry(value));
            }
            // generate one data set for each chart
            BarDataSet barDataSet = new BarDataSet(barEntryList, currency);
            barDataSet.setColor(COLOR_PALETTE[barDataSets.size() % COLOR_PALETTE.length]);
            barDataSets.add(barDataSet);
            LineDataSet lineDataSet = new LineDataSet(lineEntryList, currency);
            lineDataSet.setColor(COLOR_PALETTE[barDataSets.size() % COLOR_PALETTE.length]);
            lineDataSet.setCircleColor(COLOR_PALETTE[barDataSets.size() % COLOR_PALETTE.length]);
            lineDataSet.setCircleRadius(0.3f);
            lineDataSet.setLineWidth(1f);
            lineDataSets.add(lineDataSet);
            RadarDataSet radarDataSet = new RadarDataSet(radarEntryList, currency);
            radarDataSet.setColor(COLOR_PALETTE[barDataSets.size() % COLOR_PALETTE.length]);
            radarDataSet.setFillColor(COLOR_PALETTE[barDataSets.size() % COLOR_PALETTE.length]);
            radarDataSet.setDrawFilled(true);
            radarDataSets.add(radarDataSet);
        }
        // buildMaterialDialog the bar data and personalize it
        BarData barData = null;
        if (!barDataSets.isEmpty()) {
            barData = new BarData(barDataSets);
            int groupSize = barData.getDataSetCount();
            if (groupSize > 1) {
                barData.setBarWidth(0.8f / groupSize);
                barData.groupBars(0, 0.08f, 0.12f / groupSize);
            }
        }
        LineData lineData = null;
        if (!lineDataSets.isEmpty()) {
            lineData = new LineData(lineDataSets);
        }
        // the chart library has a bug when the radar data set is empty
        RadarData radarData = null;
        if (!radarDataSets.isEmpty()) {
            radarData = new RadarData(radarDataSets);
        }
        // return generated data
        return new OverviewData(barData, lineData, radarData, periodMoneyList);
    }

    private String[] appendSelectionArgs(String[] oldArgs, String... newArgs) {
        String[] finalArgs = new String[(oldArgs != null ? oldArgs.length : 0) + newArgs.length];
        if (oldArgs != null) {
            System.arraycopy(oldArgs, 0, finalArgs, 0, oldArgs.length);
        }
        System.arraycopy(newArgs, 0, finalArgs, finalArgs.length - newArgs.length, newArgs.length);
        return finalArgs;
    }

    /**
     * This method is used to determine if another period must be added to the period list.
     * @param periodMoney is the last period analyzed.
     * @return true if the end date has not already been reached.
     */
    private boolean isAnotherPeriodNeeded(PeriodMoney periodMoney) {
        return periodMoney == null || DateUtils.isBefore(periodMoney.getEndDate(), mOverviewSetting.getEndDate());
    }

    private PeriodMoney getNextPeriod(PeriodMoney lastPeriod) {
        Calendar startCalendar = Calendar.getInstance();
        if (lastPeriod != null) {
            startCalendar.setTime(lastPeriod.getEndDate());
            startCalendar.add(Calendar.MILLISECOND, 1);
        } else {
            startCalendar.setTime(mOverviewSetting.getStartDate());
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
        }
        Date startDate = startCalendar.getTime();
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(startDate);
        switch (mOverviewSetting.getGroupType()) {
            case YEARLY:
                // the end date is the last day of the same year of the start date
                endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
                endCalendar.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case MONTHLY:
                // the end date is generally the last day of the same month of the start month
                // but the user could have specified a different 'first day of month' so the
                // calculation is a bit more tricky:
                // step 1: we check if the start day is after the first day of month
                int firstDayOfMonth = PreferenceManager.getFirstDayOfMonth();
                int currentDayOfMonth = endCalendar.get(Calendar.DAY_OF_MONTH);
                if (currentDayOfMonth >= firstDayOfMonth) {
                    // step 2: we set as 1 the current day of the same month
                    endCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    // step 3: we move to the next month
                    endCalendar.add(Calendar.MONTH, 1);
                }
                // step 4: we move to the first day of month and then we move one day first
                endCalendar.set(Calendar.DAY_OF_MONTH, firstDayOfMonth);
                endCalendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case WEEKLY:
                // we can check which day of week is the start date and count how many days are
                // needed to reach the last day of the week that can also be personalized by the
                // user so the calculation is a bit more tricky:
                // step 1: obtain the day of week of the start day and the user preferred one
                int firstDayOfWeek = PreferenceManager.getFirstDayOfWeek();
                int currentDayOfWeek = endCalendar.get(Calendar.DAY_OF_WEEK);
                // step 2: calculate how many days are needed to reach the last day of week
                int offset = firstDayOfWeek - currentDayOfWeek;
                if (offset <= 0) {
                    offset += 7;
                }
                int requiredDays = offset - 1;
                // step 3: add those days to the current start date
                endCalendar.add(Calendar.DAY_OF_MONTH, requiredDays);
                break;
            case DAILY:
                // no special calculation is needed
                break;
        }
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);
        if (endCalendar.getTimeInMillis() > mOverviewSetting.getEndDate().getTime()) {
            endCalendar.setTime(mOverviewSetting.getEndDate());
        }
        return new PeriodMoney(startDate, endCalendar.getTime());
    }

    private boolean belongToPeriod(PeriodMoney periodMoney, Date date) {
        return DateUtils.isAfterEqual(date, periodMoney.getStartDate()) && DateUtils.isBeforeEqual(date, periodMoney.getEndDate());
    }
}