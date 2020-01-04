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

package com.oriondev.moneywallet.utils;

import android.content.Context;
import androidx.annotation.StringRes;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.oriondev.moneywallet.storage.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andrea on 07/03/18.
 */

public class DateFormatter {

    public static final String DATE_PATTERN_0 = "EEEE dd MMMM yyyy";
    public static final String DATE_PATTERN_1 = "EEEE dd MMM yyyy";
    public static final String DATE_PATTERN_2 = "EEE dd MMM yyyy";
    public static final String DATE_PATTERN_3 = "dd MMM yyyy";
    public static final String DATE_PATTERN_4 = "EEE dd/MM/yyyy";
    public static final String DATE_PATTERN_5 = "dd/MM/yyyy";
    public static final String DATE_PATTERN_6 = "yyyy/MM/dd";
    public static final String DATE_PATTERN_7 = "MM/dd/yyyy";
    public static final String DATE_PATTERN_8 = "EEE MM/dd/yyyy";

    private static final String[][] DATE_FORMATS = new String[][] {
            new String[] {"EEEE dd MMMM yyyy", "HH:mm", "EEEE dd MMMM yyyy, HH:mm"},
            new String[] {"EEEE dd MMM yyyy", "HH:mm", "EEEE dd MMM yyyy, HH:mm"},
            new String[] {"EEE dd MMM yyyy", "HH:mm", "EEE dd MMM yyyy, HH:mm"},
            new String[] {"dd MMM yyyy", "HH:mm", "dd MMM yyyy, HH:mm"},
            new String[] {"EEE dd/MM/yyyy", "HH:mm", "EEE dd/MM/yyyy, HH:mm"},
            new String[] {"dd/MM/yyyy", "HH:mm", "dd/MM/yyyy, HH:mm"},
            new String[] {"yyyy/MM/dd", "HH:mm", "yyyy/MM/dd, HH:mm"},
            new String[] {"MM/dd/yyyy", "HH:mm", "MM/dd/yyyy, HH:mm"},
            new String[] {"EEE MM/dd/yyyy", "HH:mm", "EEE MM/dd/yyyy, HH:mm"}
    };

    public static void applyDate(TextView textView, Date date) {
        textView.setText(getFormattedDate(date));
    }

    public static void applyTime(TextView textView, Date date) {
        textView.setText(getFormattedTime(date));
    }

    public static String getDateFromToday(Date date) {
        // TODO: find a way to represent dates in a relative way
        return getFormattedDateTime(date);
    }

    public static void applyDateFromToday(TextView textView, Date date, @StringRes int header) {
        // TODO: find a way to represent dates in a relative way
        Context context = textView.getContext();
        String base = context.getString(header);
        String formatted = getFormattedDate(date);
        textView.setText(String.format(base, formatted));
    }

    public static void applyDateTime(TextView textView, Date date) {
        textView.setText(getFormattedDateTime(date));
    }

    public static String getFormattedDate(Date date) {
        return getFormattedDate(date, PreferenceManager.getCurrentDateFormatIndex());
    }

    public static String getFormattedDate(Date date, int index) {
        return getUserDateFormat(index).format(date);
    }

    public static String getFormattedTime(Date date) {
        return getFormattedTime(date, PreferenceManager.getCurrentDateFormatIndex());
    }

    public static String getFormattedTime(Date date, int index) {
        return getUserTimeFormat(index).format(date);
    }

    public static String getFormattedDateTime(Date date) {
        return getFormattedDateTime(date, PreferenceManager.getCurrentDateFormatIndex());
    }

    public static String getFormattedDateTime(Date date, int index) {
        return getUserDateTimeFormat(index).format(date);
    }

    public static String getDateRange(Context context, Date start, Date end) {
        long startMillis = start.getTime();
        long endMillis = end.getTime();
        int flags = DateUtils.FORMAT_SHOW_DATE;
        return DateUtils.formatDateRange(context, startMillis, endMillis, flags);
    }

    public static void applyDateRange(TextView textView, Date start, Date end) {
        Context context = textView.getContext();
        textView.setText(getDateRange(context, start, end));
    }

    public static String getTimeRange(Context context, Date start, Date end) {
        long startMillis = start.getTime();
        long endMillis = end.getTime();
        int flags = DateUtils.FORMAT_SHOW_TIME;
        return DateUtils.formatDateRange(context, startMillis, endMillis, flags);
    }

    public static void applyTimeRange(TextView textView, Date start, Date end) {
        Context context = textView.getContext();
        textView.setText(getTimeRange(context, start, end));
    }

    private static DateFormat getUserDateFormat(int index) {
        int safeIndex = 0;
        if (index >= 0 && index < DATE_FORMATS.length) {
            safeIndex = index;
        }
        return new SimpleDateFormat(DATE_FORMATS[safeIndex][0], Locale.getDefault());
    }

    private static DateFormat getUserTimeFormat(int index) {
        int safeIndex = 0;
        if (index >= 0 && index < DATE_FORMATS.length) {
            safeIndex = index;
        }
        return new SimpleDateFormat(DATE_FORMATS[safeIndex][1], Locale.getDefault());
    }

    private static DateFormat getUserDateTimeFormat(int index) {
        int safeIndex = 0;
        if (index >= 0 && index < DATE_FORMATS.length) {
            safeIndex = index;
        }
        return new SimpleDateFormat(DATE_FORMATS[safeIndex][2], Locale.getDefault());
    }
}