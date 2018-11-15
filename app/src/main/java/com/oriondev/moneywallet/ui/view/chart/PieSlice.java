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

package com.oriondev.moneywallet.ui.view.chart;

import android.graphics.drawable.Drawable;

/**
 * Created by andrea on 13/08/18.
 */
public class PieSlice {

    private String mName;
    private long mValue;
    private Drawable mIcon;
    private boolean mIsExtra = false;

    private float mStartAngle = 0;
    private float mSweepAngle = 0;
    private float mIconAngle = 0;

    public PieSlice(String name, long value, Drawable icon) {
        this(name, value, icon, false);
    }

    public PieSlice(String name, long value, Drawable icon, boolean isExtra) {
        mName = name;
        mValue = value;
        mIcon = icon;
        mIsExtra = isExtra;
    }

    public void setIconAngle(float angle) {
        mIconAngle = angle;
    }

    public void increaseIconAngle(float degrees) {
        mIconAngle += degrees;
    }

    public void decreaseIconAngle(float degrees) {
        mIconAngle -= degrees;
    }

    public void setValue(long value) {
        mValue = value;
    }

    public long getValue() {
        return mValue;
    }

    public String getName() {
        return mName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public float getIconAngle() {
        return mIconAngle;
    }

    public boolean isExtra() {
        return mIsExtra;
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float startAngle) {
        mStartAngle = startAngle;
    }

    public float getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(float sweepAngle) {
        mSweepAngle = sweepAngle;
    }
}