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

package com.oriondev.moneywallet.ui.view.calendar;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oriondev.moneywallet.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimelineView extends RecyclerView {

    private static final String TAG = "TimelineView";

    private static final String[] WEEK_DAYS = DateFormatSymbols.getInstance().getShortWeekdays();

    private final Calendar calendar = Calendar.getInstance(Locale.getDefault());

    private TimelineAdapter adapter;
    private LinearLayoutManager layoutManager;
    private OnDateSelectedListener onDateSelectedListener;
    private MonthView.DateLabelAdapter dateLabelAdapter;

    private int startYear = 1970, startMonth = 0, startDay = 1;
    private int selectedYear, selectedMonth, selectedDay;
    private int selectedPosition = 1;
    private int dayCount = Integer.MAX_VALUE;

    // Day letter
    private int lblDayColor;
    // Day number label
    private int lblDateColor, lblDateSelectedColor;

    public TimelineView(Context context) {
        super(context);
        init();
    }

    public TimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimelineView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        setSelectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        resetCalendar();

        setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        adapter = new TimelineAdapter();
        setLayoutManager(layoutManager);
        setAdapter(adapter);
    }

    private void resetCalendar() {
        calendar.set(startYear, startMonth, startDay, 1, 0, 0);
    }

    private void onDateSelected(int position, int year, int month, int day) {
        if (position == selectedPosition) {
            centerOnPosition(selectedPosition);
            return;
        }
        int oldPosition = selectedPosition;
        selectedPosition = position;
        this.selectedYear = year;
        this.selectedMonth = month;
        this.selectedDay = day;
        if (adapter != null && layoutManager != null) {
            adapter.notifyItemChanged(oldPosition);
            adapter.notifyItemChanged(position);
            centerOnPosition(selectedPosition);
            if (onDateSelectedListener != null) {
                onDateSelectedListener.onDateSelected(selectedYear, selectedMonth, selectedDay, selectedPosition);
            }
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    centerOnPosition(selectedPosition);
                }
            });
        }
    }

    public void centerOnPosition(int position) {
        if (getChildCount() == 0) {
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!isLaidOut()) {
                return;
            }
        }
        // Animate scroll
        int offset = getMeasuredWidth() / 2 - getChildAt(0).getMeasuredWidth() / 2;
        layoutManager.scrollToPositionWithOffset(position, offset);
    }

    public void centerOnSelection() {
        centerOnPosition(selectedPosition);
    }

    public void setSelectedPosition(int position) {
        resetCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, position);
        onDateSelected(position, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void setSelectedDate(int year, int month, int day) {
        if (year == startYear && month == startMonth && day < startDay) {
            day = startDay;
        }

        // Get new selected dayOfYear
        calendar.set(year, month, day, 1, 0, 0);
        final int newDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        final long newTimestamp = calendar.getTimeInMillis();

        // Get current selected dayOfYear
        calendar.set(selectedYear, selectedMonth, selectedDay, 1, 0, 0);
        final int oldDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        final long oldTimestamp = calendar.getTimeInMillis();

        int dayDifference;
        if (year == selectedYear) {
            dayDifference = newDayOfYear - oldDayOfYear;
        } else {
            // Lazy...
            int dayDifferenceApprox = (int) ((newTimestamp - oldTimestamp) / TimeUnit.DAYS.toMillis(1));
            calendar.add(Calendar.DAY_OF_YEAR, dayDifferenceApprox);
            dayDifference = dayDifferenceApprox + (newDayOfYear - calendar.get(Calendar.DAY_OF_YEAR));
        }

        onDateSelected(selectedPosition + dayDifference, year, month, day);
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public int getSelectedMonth() {
        return selectedMonth;
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }

    public void setDateLabelAdapter(@Nullable MonthView.DateLabelAdapter dateLabelAdapter) {
        this.dateLabelAdapter = dateLabelAdapter;
    }

    public void setDayLabelColor(int lblDayColor) {
        this.lblDayColor = lblDayColor;
    }

    public void setDateLabelColor(int lblDateColor) {
        this.lblDateColor = lblDateColor;
    }

    public void setDateLabelSelectedColor(int lblDateSelectedColor) {
        this.lblDateSelectedColor = lblDateSelectedColor;
    }

    public int getLblDateColor() {
        return lblDateColor;
    }

    public void setLblDateColor(int lblDateColor) {
        this.lblDateColor = lblDateColor;
    }

    public int getLblDateSelectedColor() {
        return lblDateSelectedColor;
    }

    public void setLblDateSelectedColor(int lblDateSelectedColor) {
        this.lblDateSelectedColor = lblDateSelectedColor;
    }

    public int getLblDayColor() {
        return lblDayColor;
    }

    public void setLblDayColor(int lblDayColor) {
        this.lblDayColor = lblDayColor;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setFirstDate(int startYear, int startMonth, int startDay) {
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;

        selectedYear = startYear;
        selectedMonth = startMonth;
        selectedDay = startDay;
        selectedPosition = 0;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setLastDate(int endYear, int endMonth, int endDay) {
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(startYear, startMonth, startDay);
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(endYear, endMonth, endDay);

        // TODO: might now work for summer time...
        int dayDiff = (int) TimeUnit.DAYS.convert(lastDate.getTimeInMillis() - firstDate.getTimeInMillis(),
                TimeUnit.MILLISECONDS);

        setDayCount(dayDiff + 1);
    }

    void setDayCount(int dayCount) {
        if (this.dayCount == dayCount) {
            return;
        }

        this.dayCount = dayCount;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private class TimelineAdapter extends RecyclerView.Adapter<TimelineViewHolder> {

        TimelineAdapter() {

        }

        @Override
        public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.view_mti_item_day, parent, false);
            return new TimelineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TimelineViewHolder holder, int position) {
            resetCalendar();
            calendar.add(Calendar.DAY_OF_YEAR, position);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            boolean isToday = DateUtils.isToday(calendar.getTimeInMillis());
            holder.bind(position, year, month, day, dayOfWeek,position == selectedPosition, isToday);
        }

        @Override
        public int getItemCount() {
            return dayCount;
        }
    }

    private class TimelineViewHolder extends RecyclerView.ViewHolder {

        private final TextView lblDay;
        private final TextView lblDate;

        private int position;
        private int year, month, day;

        TimelineViewHolder(View root) {
            super(root);

            lblDay = root.findViewById(R.id.mti_timeline_lbl_day);
            lblDate = root.findViewById(R.id.mti_timeline_lbl_date);

            lblDay.setTextColor(lblDayColor);
            lblDate.setTextColor(lblDateColor);

            root.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    onDateSelected(position, year, month, day);
                }

            });
        }

        void bind(int position, int year, int month, int day, int dayOfWeek, boolean selected, boolean isToday) {
            this.position = position;
            this.year = year;
            this.month = month;
            this.day = day;
            lblDay.setText(WEEK_DAYS[dayOfWeek].toUpperCase(Locale.US));
            lblDate.setText(String.valueOf(day));
            // lblDate.setBackgroundResource(selected ? R.drawable.mti_bg_lbl_date_selected : (isToday ? R.drawable.mti_bg_lbl_date_today : 0));
            lblDate.setTextColor(selected || isToday ? lblDateSelectedColor : lblDateColor);
        }
    }
}