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

import android.os.Parcel;
import android.os.Parcelable;

import com.oriondev.moneywallet.utils.DateUtils;

import java.util.Date;

/**
 * Created by andrea on 17/08/18.
 */
public class OverviewSetting implements Parcelable {

    private final Date mStartDate;
    private final Date mEndDate;
    private final Group mGroupType;
    private final Type mType;
    private final CashFlow mCashFlow;
    private final long mCategoryId;

    public OverviewSetting(Date startDate, Date endDate, Group groupType, CashFlow cashFlow) {
        mStartDate = DateUtils.setTime(startDate, 0, 0, 0, 0);
        mEndDate = DateUtils.setTime(endDate, 23, 59, 59, 999);
        mGroupType = groupType;
        mType = Type.CASH_FLOW;
        mCashFlow = cashFlow;
        mCategoryId = 0L;
    }

    public OverviewSetting(Date startDate, Date endDate, Group groupType, long categoryId) {
        mStartDate = DateUtils.setTime(startDate, 0, 0, 0, 0);
        mEndDate = DateUtils.setTime(endDate, 23, 59, 59, 999);
        mGroupType = groupType;
        mType = Type.CATEGORY;
        mCashFlow = CashFlow.NET_INCOMES;
        mCategoryId = categoryId;
    }

    private OverviewSetting(Parcel in) {
        mStartDate = (Date) in.readSerializable();
        mEndDate = (Date) in.readSerializable();
        mGroupType = (Group) in.readSerializable();
        mType = (Type) in.readSerializable();
        mCashFlow = (CashFlow) in.readSerializable();
        mCategoryId = in.readLong();
    }

    public static final Creator<OverviewSetting> CREATOR = new Creator<OverviewSetting>() {

        @Override
        public OverviewSetting createFromParcel(Parcel in) {
            return new OverviewSetting(in);
        }

        @Override
        public OverviewSetting[] newArray(int size) {
            return new OverviewSetting[size];
        }

    };

    public Date getStartDate() {
        return mStartDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public Group getGroupType() {
        return mGroupType;
    }

    public Type getType() {
        return mType;
    }

    public CashFlow getCashFlow() {
        return mCashFlow;
    }

    public long getCategoryId() {
        return mCategoryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mStartDate);
        dest.writeSerializable(mEndDate);
        dest.writeSerializable(mGroupType);
        dest.writeSerializable(mType);
        dest.writeSerializable(mCashFlow);
        dest.writeLong(mCategoryId);
    }

    public enum Type {
        CASH_FLOW,
        CATEGORY
    }

    public enum CashFlow {
        INCOMES,
        EXPENSES,
        NET_INCOMES
    }
}