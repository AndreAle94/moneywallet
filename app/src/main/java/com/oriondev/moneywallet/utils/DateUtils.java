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

import org.dmfs.rfc5545.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andrea on 03/03/18.
 */

public class DateUtils {

    private static final String SQL_DATE = "yyyy-MM-dd";
    private static final String SQL_DATETIME = "yyyy-MM-dd HH:mm:ss";
    private static final String FILENAME_DATETIME = "yyyy-MM-dd_HH-mm-ss";

    public static Date getDateFromSQLDateString(String date) {
        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE, Locale.ENGLISH);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getDateFromSQLDateTimeString(String dateTime) {
        DateFormat dateFormat = new SimpleDateFormat(SQL_DATETIME, Locale.ENGLISH);
        try {
            return dateFormat.parse(dateTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDateTimeString(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return format.format(date);
    }

    public static String getSQLDateString(Date date) {
        DateFormat format = new SimpleDateFormat(SQL_DATE, Locale.ENGLISH);
        return format.format(date);
    }

    public static String getSQLDateTimeString(Date date) {
        DateFormat format = new SimpleDateFormat(SQL_DATETIME, Locale.ENGLISH);
        return format.format(date);
    }

    public static String getSQLDateTimeString(long millis) {
        DateFormat format = new SimpleDateFormat(SQL_DATETIME, Locale.ENGLISH);
        return format.format(new Date(millis));
    }

    public static String getFilenameDateTimeString(Date date) {
        DateFormat format = new SimpleDateFormat(FILENAME_DATETIME, Locale.ENGLISH);
        return format.format(date);
    }

    public static Date setTime(Date date, int hour, int minute, int second, int millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return setTime(calendar, hour, minute, second, millis);
    }

    public static Date setTime(Calendar calendar, int hour, int minute, int second, int millis) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    public static Date setDayOfMonth(Calendar calendar, int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    public static Date setMonth(Calendar calendar, int month) {
        calendar.set(Calendar.MONTH, month);
        return calendar.getTime();
    }

    public static Date setDayAndMonth(Calendar calendar, int day, int month) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        return calendar.getTime();
    }

    public static Date getFirstDateOfWeek(Calendar calendar) {
        return getFirstDateOfWeek(calendar, calendar.getFirstDayOfWeek());
    }

    public static Date getFirstDateOfWeek(Calendar calendar, int firstDayOfWeek) {
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (currentDayOfWeek < firstDayOfWeek) {
            // move to week before
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
        calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        return calendar.getTime();
    }

    public static Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static Date addDays(Calendar calendar, int days) {
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static Date addMonths(Calendar calendar, int months) {
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    public static Date addMonthAndDay(Calendar calendar, int months, int days) {
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static boolean isBeforeToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        setTime(calendar, 0, 0, 0, 0);
        return date.getTime() < calendar.getTimeInMillis();
    }

    public static boolean isBeforeNow(Date date) {
        return date.compareTo(new Date()) == -1;
    }

    public static boolean isAfter(Date date1, Date date2) {
        return date1.compareTo(date2) == 1;
    }

    public static boolean isAfterEqual(Date date1, Date date2) {
        return date1.compareTo(date2) != -1;
    }

    public static boolean isBefore(Date date1, Date date2) {
        return date1.compareTo(date2) == -1;
    }

    public static boolean isBeforeEqual(Date date1, Date date2) {
        return date1.compareTo(date2) != 1;
    }

    public static boolean isTheSameDay(Date start, Date end) {
        Calendar calendar1 = getCalendar(start);
        Calendar calendar2 = getCalendar(end);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isTheSameDayAndHour(Date start, Date end) {
        Calendar calendar1 = getCalendar(start);
        Calendar calendar2 = getCalendar(end);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH) &&
                calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY);
    }

    public static int getDaysBetween(Date start, Date end) {
        return (int)( (Math.abs(end.getTime() - start.getTime())) / (1000 * 60 * 60 * 24));
    }

    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static DateTime getFixedDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static Date getFixedDate(DateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateTime.getYear());
        calendar.set(Calendar.MONTH, dateTime.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}