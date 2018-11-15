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

package com.oriondev.moneywallet.storage.database;

import android.database.sqlite.SQLiteException;

/**
 * Created by andrea on 20/07/18.
 */
public class SQLiteDataException extends SQLiteException {

    private final int mErrorCode;

    /*package-local*/ SQLiteDataException(int code) {
        mErrorCode = code;
    }

    /*package-local*/ SQLiteDataException(int code, String error) {
        super(error);
        mErrorCode = code;
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}