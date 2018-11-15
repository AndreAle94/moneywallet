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

/**
 * Created by andrea on 03/03/18.
 */
public enum Group {

    DAILY(0), WEEKLY(1), MONTHLY(2), YEARLY(3);

    private final int mValue;

    Group(int value) {
        mValue = value;
    }

    public int getType() {
        return mValue;
    }

    public static Group fromType(int type) {
        switch (type) {
            case 0:
                return DAILY;
            case 1:
                return WEEKLY;
            case 2:
                return MONTHLY;
            case 3:
                return YEARLY;
        }
        throw new IllegalArgumentException("Group type not recognized: " + String.valueOf(type));
    }
}