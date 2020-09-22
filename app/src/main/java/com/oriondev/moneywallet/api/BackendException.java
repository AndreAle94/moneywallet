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

package com.oriondev.moneywallet.api;

/**
 * Created by andrea on 21/11/18.
 */
public class BackendException extends Exception {

    private boolean mIsRecoverable;

    public BackendException(String message) {
        this(message, null);
    }

    public BackendException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public BackendException(String message, boolean isRecoverable) {
        this(message, null, isRecoverable);
    }

    public BackendException(String message, Throwable cause, boolean isRecoverable) {
        super(message, cause);
        mIsRecoverable = isRecoverable;
    }

    public boolean isRecoverable() {
        return mIsRecoverable;
    }
}