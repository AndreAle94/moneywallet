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

import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Created by andrea on 21/11/18.
 */
public abstract class AbstractBackendServiceDelegate {

    private final BackendServiceStatusListener mListener;

    public AbstractBackendServiceDelegate(BackendServiceStatusListener listener) {
        mListener = listener;
    }

    public abstract String getId();

    @StringRes
    public abstract int getBackupCoverMessage();

    @StringRes
    public abstract int getName();

    @StringRes
    public abstract int getBackupCoverAction();

    public abstract boolean isServiceEnabled(Context context);

    public abstract void setup(ComponentActivity activity) throws BackendException;

    public abstract void teardown(ComponentActivity activity) throws BackendException;

    public boolean handlePermissionsResult(Context context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        return false;
    }

    public boolean handleActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        return false;
    }

    protected void setBackendServiceEnabled(boolean enabled) {
        if (mListener != null) {
            mListener.onBackendStatusChange(enabled);
        }
    }

    public interface BackendServiceStatusListener {

        void onBackendStatusChange(boolean enabled);
    }
}