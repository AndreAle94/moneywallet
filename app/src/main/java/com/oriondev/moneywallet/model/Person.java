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
 * Created by andrea on 10/03/18.
 */
public class Person implements Identifiable, Parcelable {

    private final long mId;
    private final String mName;
    private final Icon mIcon;

    public Person(long id, String name, Icon icon) {
        mId = id;
        mName = name;
        mIcon = icon;
    }

    protected Person(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mIcon = in.readParcelable(Icon.class.getClassLoader());
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Icon getIcon() {
        return mIcon;
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
    }
}
