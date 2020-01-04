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

package com.oriondev.moneywallet.background;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

/**
 * Created by andrea on 06/04/18.
 */
public abstract class AbstractGenericLoader<T> extends AsyncTaskLoader<T> {

    private T mGenericData;

    public AbstractGenericLoader(Context context) {
        super(context);
    }

    @Override
    public abstract T loadInBackground();

    /* Runs on the UI thread */
    @Override
    public void deliverResult(T genericData) {
        mGenericData = genericData;
        if (isStarted()) {
            super.deliverResult(genericData);
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mGenericData != null) {
            deliverResult(mGenericData);
        }
        if (takeContentChanged() || mGenericData == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        mGenericData = null;
    }
}