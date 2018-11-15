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

/**
 * Created by andrea on 09/03/18.
 */
public class Wallet implements Identifiable, Parcelable {

    private final long mId;
    private final String mName;
    private final Icon mIcon;
    private final CurrencyUnit mCurrency;
    private final long mStartMoney;
    private final long mTotalMoney;

    public Wallet(long id, String name, Icon icon, CurrencyUnit currency, long startMoney, long totalMoney) {
        mId = id;
        mName = name;
        mIcon = icon;
        mCurrency = currency;
        mStartMoney = startMoney;
        mTotalMoney = totalMoney;
    }

    @Override
    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public CurrencyUnit getCurrency() {
        return mCurrency;
    }

    protected Wallet(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mIcon = in.readParcelable(Icon.class.getClassLoader());
        mCurrency = in.readParcelable(CurrencyUnit.class.getClassLoader());
        mStartMoney = in.readLong();
        mTotalMoney = in.readLong();
    }

    public static final Creator<Wallet> CREATOR = new Creator<Wallet>() {
        @Override
        public Wallet createFromParcel(Parcel in) {
            return new Wallet(in);
        }

        @Override
        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeParcelable(mIcon, flags);
        dest.writeParcelable(mCurrency, flags);
        dest.writeLong(mStartMoney);
        dest.writeLong(mTotalMoney);
    }
}