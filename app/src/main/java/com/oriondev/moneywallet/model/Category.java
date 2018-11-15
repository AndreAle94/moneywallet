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

import com.oriondev.moneywallet.storage.database.Contract;

/**
 * Created by andrea on 02/03/18.
 */
public class Category implements Parcelable {

    private final long mId;
    private final String mName;
    private final Icon mIcon;
    private final Contract.CategoryType mType;
    private final String mTag;

    public Category(long id, String name, Icon icon, Contract.CategoryType type) {
        mId = id;
        mName = name;
        mIcon = icon;
        mType = type;
        mTag = null;
    }

    public Category(long id, String name, Icon icon, Contract.CategoryType type, String tag) {
        mId = id;
        mName = name;
        mIcon = icon;
        mType = type;
        mTag = tag;
    }

    @SuppressWarnings("WeakerAccess")
    protected Category(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mIcon = in.readParcelable(Icon.class.getClassLoader());
        mType = (Contract.CategoryType) in.readSerializable();
        mTag = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Icon getIcon() {
        return mIcon;
    }

    public Contract.CategoryType getType() {
        return mType;
    }

    public String getTag() {
        return mTag;
    }

    public int getDirection() {
        switch (mType) {
            case INCOME:
                return 1;
            case EXPENSE:
                return 0;
            case SYSTEM:
                if (mTag != null) {
                    switch (mTag) {
                        case Contract.CategoryTag.CREDIT:
                        case Contract.CategoryTag.PAID_DEBT:
                        case Contract.CategoryTag.SAVING_DEPOSIT:
                        case Contract.CategoryTag.TAX:
                        case Contract.CategoryTag.TRANSFER_TAX:
                            return 0;
                        case Contract.CategoryTag.DEBT:
                        case Contract.CategoryTag.PAID_CREDIT:
                        case Contract.CategoryTag.SAVING_WITHDRAW:
                            return 1;
                    }
                }
                break;
        }
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeParcelable(mIcon, flags);
        dest.writeSerializable(mType);
        dest.writeString(mTag);
    }
}