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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrea on 07/11/18.
 */
public class RecurrenceSetting implements Parcelable {

    public static final int TYPE_DAILY = 1;
    public static final int TYPE_WEEKLY = 2;
    public static final int TYPE_MONTHLY = 3;
    public static final int TYPE_YEARLY = 4;

    public static final int FLAG_MONTHLY_SAME_DAY = 1;

    public static final int END_FOREVER = 1;
    public static final int END_UNTIL = 2;
    public static final int END_FOR = 3;

    private Date mStartDate;
    private RecurrenceRule mRecurrenceRule;

    public RecurrenceSetting(Date startDate, int type) {
        mStartDate = startDate;
        mRecurrenceRule = new RecurrenceRule(getFreq(type));
    }

    public RecurrenceSetting(Date startDate, String rule) {
        mStartDate = startDate;
        try {
            mRecurrenceRule = new RecurrenceRule(rule);
        } catch (InvalidRecurrenceRuleException ignore) {
            mRecurrenceRule = new RecurrenceRule(Freq.DAILY);
        }
    }

    public static RecurrenceSetting build(Date startDate, String rule) throws InvalidRecurrenceRuleException {
        RecurrenceRule recurrenceRule = new RecurrenceRule(rule);
        return new RecurrenceSetting(startDate, rule);
    }

    private RecurrenceSetting(Date startDate, RecurrenceRule recurrenceRule) {
        mStartDate = startDate;
        mRecurrenceRule = recurrenceRule;
    }

    private RecurrenceSetting(Parcel in) {
        try {
            mStartDate = (Date) in.readSerializable();
            mRecurrenceRule = new RecurrenceRule(in.readString());
        } catch (InvalidRecurrenceRuleException e) {
            throw new RuntimeException(e);
        }
    }

    public int getType() {
        switch (mRecurrenceRule.getFreq()) {
            case DAILY:
                return TYPE_DAILY;
            case WEEKLY:
                return TYPE_WEEKLY;
            case MONTHLY:
                return TYPE_MONTHLY;
            case YEARLY:
                return TYPE_YEARLY;
            default:
                return TYPE_DAILY;
        }
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public int getOffsetValue() {
        return mRecurrenceRule.getInterval();
    }

    public boolean[] getWeekDays() {
        boolean[] weekDays = new boolean[] {false, false, false, false, false, false, false};
        List<RecurrenceRule.WeekdayNum> weekdayNumList = mRecurrenceRule.getByDayPart();
        if (weekdayNumList != null && weekdayNumList.size() > 0) {
            for (RecurrenceRule.WeekdayNum weekdayNum : weekdayNumList) {
                switch (weekdayNum.weekday) {
                    case SU:
                        weekDays[0] = true;
                        break;
                    case MO:
                        weekDays[1] = true;
                        break;
                    case TU:
                        weekDays[2] = true;
                        break;
                    case WE:
                        weekDays[3] = true;
                        break;
                    case TH:
                        weekDays[4] = true;
                        break;
                    case FR:
                        weekDays[5] = true;
                        break;
                    case SA:
                        weekDays[6] = true;
                        break;
                }
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mStartDate);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            weekDays[dayOfWeek - 1] = true;
        }
        return weekDays;
    }

    public int getMonthDay() {
        return FLAG_MONTHLY_SAME_DAY;
    }

    public int getEndType() {
        if (mRecurrenceRule.isInfinite()) {
            return END_FOREVER;
        } else if (mRecurrenceRule.getUntil() != null) {
            return END_UNTIL;
        } else {
            return END_FOR;
        }
    }

    public Date getEndDate() {
        DateTime dateTime = mRecurrenceRule.getUntil();
        return dateTime != null ? new Date(dateTime.getTimestamp()) : mStartDate;
    }

    public int getOccurrenceValue() {
        Integer integer = mRecurrenceRule.getCount();
        return integer != null ? integer : 1;
    }

    public String getRule() {
        return mRecurrenceRule.toString();
    }

    public static final Creator<RecurrenceSetting> CREATOR = new Creator<RecurrenceSetting>() {

        @Override
        public RecurrenceSetting createFromParcel(Parcel in) {
            return new RecurrenceSetting(in);
        }

        @Override
        public RecurrenceSetting[] newArray(int size) {
            return new RecurrenceSetting[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mStartDate);
        dest.writeString(mRecurrenceRule.toString());
    }

    public String getUserReadableString(Context context) {
        StringBuilder builder = new StringBuilder();
        int offset = mRecurrenceRule.getInterval();
        switch (mRecurrenceRule.getFreq()) {
            case DAILY:
                if (offset <= 1) {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_day));
                } else {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_n_days, offset));
                }
                break;
            case WEEKLY:
                if (offset <= 1) {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_week));
                } else {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_n_weeks, offset));
                }
                boolean[] weekDays = getWeekDays();
                if (weekDays[0]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_sunday));}
                if (weekDays[1]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_monday));}
                if (weekDays[2]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_tuesday));}
                if (weekDays[3]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_wednesday));}
                if (weekDays[4]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_thursday));}
                if (weekDays[5]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_friday));}
                if (weekDays[6]) {builder.append(context.getString(R.string.recurrence_hint_repeat_weekly_saturday));}
                break;
            case MONTHLY:
                if (offset <= 1) {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_month));
                } else {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_n_months, offset));
                }
                break;
            case YEARLY:
                if (offset <= 1) {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_year));
                } else {
                    builder.append(context.getString(R.string.recurrence_hint_repeat_every_n_years, offset));
                }
                break;
        }
        String formattedStartDate = DateFormatter.getFormattedDate(mStartDate);
        builder.append(context.getString(R.string.recurrence_hint_repeat_starting_from, formattedStartDate));
        if (mRecurrenceRule.isInfinite()) {
            builder.append(context.getString(R.string.recurrence_hint_repeat_forever));
        } else if (mRecurrenceRule.getUntil() != null) {
            Date endDate = new Date(mRecurrenceRule.getUntil().getTimestamp());
            String formattedEndDate = DateFormatter.getFormattedDate(endDate);
            builder.append(context.getString(R.string.recurrence_hint_repeat_until, formattedEndDate));
        } else {
            int occurrences = mRecurrenceRule.getCount();
            if (occurrences <= 1) {
                builder.append(context.getString(R.string.recurrence_hint_repeat_for_one_occurrence));
            } else {
                builder.append(context.getString(R.string.recurrence_hint_repeat_for_n_occurrence, occurrences));
            }
        }
        return builder.toString();
    }

    public Date getNextOccurrence(Date lastOccurrence) {
        DateTime startDateTime = DateUtils.getFixedDateTime(mStartDate);
        DateTime lastOccurrenceDateTime = DateUtils.getFixedDateTime(lastOccurrence);
        DateTime nextOccurrenceDateTime = null;
        RecurrenceRuleIterator iterator = mRecurrenceRule.iterator(startDateTime);
        while (iterator.hasNext()) {
            DateTime nextInstance = iterator.nextDateTime();
            if (nextInstance.after(lastOccurrenceDateTime)) {
                nextOccurrenceDateTime = nextInstance;
                break;
            }
        }
        return nextOccurrenceDateTime != null ? DateUtils.getFixedDate(nextOccurrenceDateTime) : null;
    }

    public static class Builder {

        private Date mStartDate;
        private RecurrenceRule mRecurrenceRule;

        public Builder(Date startDate, int type) {
            mStartDate = startDate;
            mRecurrenceRule = new RecurrenceRule(getFreq(type));
        }

        public void setOffset(int offset) {
            mRecurrenceRule.setInterval(offset);
        }

        public void setRepeatWeekDay(boolean[] weekDays) {
            List<RecurrenceRule.WeekdayNum> weekdayNumList = new ArrayList<>();
            if (weekDays[0]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.SU));}
            if (weekDays[1]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));}
            if (weekDays[2]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.TU));}
            if (weekDays[3]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.WE));}
            if (weekDays[4]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.TH));}
            if (weekDays[5]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.FR));}
            if (weekDays[6]) {weekdayNumList.add(new RecurrenceRule.WeekdayNum(0, Weekday.SA));}
            mRecurrenceRule.setByDayPart(weekdayNumList);
        }

        public void setRepeatSameMonthDay() {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mStartDate);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                mRecurrenceRule.setByPart(RecurrenceRule.Part.BYMONTHDAY, dayOfMonth);
            } catch (InvalidRecurrenceRuleException e) {
                throw new RuntimeException(e);
            }
        }

        public void setEndFor(int occurrences) {
            mRecurrenceRule.setCount(occurrences);
        }

        public void setEndUntil(Date endDate) {
            mRecurrenceRule.setUntil(DateUtils.getFixedDateTime(endDate));
        }

        public RecurrenceSetting build() {
            return new RecurrenceSetting(mStartDate, mRecurrenceRule);
        }
    }

    private static Freq getFreq(int type) {
        switch (type) {
            case TYPE_DAILY:
                return Freq.DAILY;
            case TYPE_WEEKLY:
                return Freq.WEEKLY;
            case TYPE_MONTHLY:
                return Freq.MONTHLY;
            case TYPE_YEARLY:
                return Freq.YEARLY;
        }
        return Freq.DAILY;
    }
}