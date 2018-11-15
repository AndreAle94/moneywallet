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
 * Created by andrea on 16/10/18.
 */

public class Coordinates implements Parcelable {

    private final double mLatitude;
    private final double mLongitude;

    public Coordinates(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @SuppressWarnings("WeakerAccess")
    protected Coordinates(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public static final Creator<Coordinates> CREATOR = new Creator<Coordinates>() {

        @Override
        public Coordinates createFromParcel(Parcel in) {
            return new Coordinates(in);
        }

        @Override
        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }
}