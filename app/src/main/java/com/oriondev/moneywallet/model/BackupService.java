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

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Created by andre on 21/03/2018.
 */
public class BackupService {

    private final String mIdentifier;
    private final int mIconRes;
    private final int mNameRes;

    public BackupService(String identifier, int icon, int name) {
        mIdentifier = identifier;
        mIconRes = icon;
        mNameRes = name;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    @DrawableRes
    public int getIconRes() {
        return mIconRes;
    }

    @StringRes
    public int getNameRes() {
        return mNameRes;
    }
}