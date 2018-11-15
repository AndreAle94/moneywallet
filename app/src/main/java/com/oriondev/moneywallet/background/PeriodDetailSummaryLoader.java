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
import android.net.Uri;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.model.PeriodDetailSummaryData;
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
 * Created by andrea on 14/08/18.
 */
public class PeriodDetailSummaryLoader extends AbstractGenericLoader<PeriodDetailSummaryData> {

    public static final int GROUP_BY_YEAR = 1;
    public static final int GROUP_BY_MONTH = 2;
    public static final int GROUP_BY_WEEK = 3;
    public static final int GROUP_BY_DAY = 4;
    public static final int GROUP_BY_HOUR = 5;

    private final Date mStartDate;
    private final Date mEndDate;
    private final int mGroupType;

    public PeriodDetailSummaryLoader(Context context, Date startDate, Date endDate, int groupType) {
        super(context);
        mStartDate = startDate;
        mEndDate = endDate;
        mGroupType = groupType;
    }

    @Override
    public PeriodDetailSummaryData loadInBackground() {
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
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('now', 'localtime')";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") >= DATETIME('" + DateUtils.getSQLDateTimeString(mStartDate) + "')";
        selection += " AND DATETIME(" + Contract.Transaction.DATE + ") <= DATETIME('" + DateUtils.getSQLDateTimeString(mEndDate) + "')";
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
        // the total net income contains all the available currencies
        List<BarData> barDataList = new ArrayList<>();
        List<CurrencyUnit> barDataCurrencies = new ArrayList<>();
        for (String currency : totalNetIncomes.getCurrencies()) {
            // for each currency we have to iterate the period money items and generate
            // the chart data set composed by two sub data sets:
            CurrencyUnit currencyUnit = CurrencyManager.getCurrency(currency);
            List<BarEntry> incomeBarEntries = new ArrayList<>();
            List<BarEntry> expenseBarEntries = new ArrayList<>();
            for (int i = 0; i < periodMoneyList.size(); i++) {
                PeriodMoney periodMoney = periodMoneyList.get(i);
                long incomes = periodMoney.getIncomes().getMoney(currency);
                incomeBarEntries.add(new BarEntry(i, incomes));
                long expenses = periodMoney.getExpenses().getMoney(currency);
                expenseBarEntries.add(new BarEntry(i, expenses));
            }
            BarDataSet incomeDataSet = new BarDataSet(incomeBarEntries, getContext().getString(R.string.hint_incomes));
            BarDataSet expenseDataSet = new BarDataSet(expenseBarEntries, getContext().getString(R.string.hint_expenses));
            incomeDataSet.setColor(PreferenceManager.getCurrentIncomeColor());
            expenseDataSet.setColor(PreferenceManager.getCurrentExpenseColor());
            barDataList.add(new BarData(incomeDataSet, expenseDataSet));
            barDataCurrencies.add(currencyUnit);
        }
        return new PeriodDetailSummaryData(totalNetIncomes, barDataList, barDataCurrencies, periodMoneyList);
    }

    /**
     * This method is used to determine if another period must be added to the period list.
     * @param periodMoney is the last period analyzed.
     * @return true if the end date has not already been reached.
     */
    private boolean isAnotherPeriodNeeded(PeriodMoney periodMoney) {
        return periodMoney == null || DateUtils.isBefore(periodMoney.getEndDate(), mEndDate);
    }

    private PeriodMoney getNextPeriod(PeriodMoney lastPeriod) {
        Calendar startCalendar = Calendar.getInstance();
        if (lastPeriod != null) {
            startCalendar.setTime(lastPeriod.getEndDate());
            startCalendar.add(Calendar.MILLISECOND, 1);
        } else {
            startCalendar.setTime(mStartDate);
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
        }
        Date startDate = startCalendar.getTime();
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(startDate);
        switch (mGroupType) {
            case GROUP_BY_YEAR:
                // the end date is the last day of the same year of the start date
                endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
                endCalendar.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case GROUP_BY_MONTH:
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
            case GROUP_BY_WEEK:
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
            case GROUP_BY_DAY:
                // no special calculation is needed
                break;
            case GROUP_BY_HOUR:
                // no special calculation is needed
                break;
        }
        if (mGroupType != GROUP_BY_HOUR) {
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        }
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);
        if (endCalendar.getTimeInMillis() > mEndDate.getTime()) {
            endCalendar.setTime(mEndDate);
        }
        return new PeriodMoney(startDate, endCalendar.getTime());
    }

    private boolean belongToPeriod(PeriodMoney periodMoney, Date date) {
        return DateUtils.isAfterEqual(date, periodMoney.getStartDate()) && DateUtils.isBeforeEqual(date, periodMoney.getEndDate());
    }
}