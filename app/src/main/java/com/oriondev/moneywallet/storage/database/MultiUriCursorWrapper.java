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

import android.content.ContentResolver;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This class works as wrapper on top of {@link Cursor}.
 * This allow to register multiple notification uri instead of a single one.
 *
 * Credits to: https://gist.github.com/chalup/4201307da02b9cfe4f40
 */
public class MultiUriCursorWrapper extends CursorWrapper {

    public MultiUriCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    protected boolean mClosed;
    protected ContentResolver mContentResolver;

    private final LinkedHashSet<Uri> mNotifyUris = new LinkedHashSet<>();

    private final Object mSelfObserverLock = new Object();
    private ContentObserver mSelfObserver;
    private boolean mSelfObserverRegistered;

    private final LinkedHashSet<Uri> mChangedByUris = new LinkedHashSet<>();

    private final ContentObservable mContentObservable = new ContentObservable();

    @Override
    public void deactivate() {
        onDeactivateOrClose();
        super.deactivate();
    }

    protected void onDeactivateOrClose() {
        if (mSelfObserver != null) {
            mContentResolver.unregisterContentObserver(mSelfObserver);
            mSelfObserverRegistered = false;
        }
    }

    @Override
    public boolean requery() {
        if (mSelfObserver != null && !mSelfObserverRegistered) {
            for (Uri notifyUri : mNotifyUris) {
                mContentResolver.registerContentObserver(notifyUri, true, mSelfObserver);
            }
            mSelfObserverRegistered = true;
        }

        boolean success = super.requery();
        if (success) {
            mChangedByUris.clear();
        }
        return success;
    }

    @Override
    public boolean isClosed() {
        return super.isClosed() && mClosed;
    }

    @Override
    public void close() {
        super.close();
        mClosed = true;
        mContentObservable.unregisterAll();
        onDeactivateOrClose();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerContentObserver(ContentObserver observer) {
        mContentObservable.registerObserver(observer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (Uri changedByUri : mChangedByUris) {
                observer.dispatchChange(false, changedByUri);
            }
        } else {
            if (!mChangedByUris.isEmpty()) {
                observer.dispatchChange(false);
            }
        }
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        // cursor will unregister all observers when it close
        if (!mClosed) {
            mContentObservable.unregisterObserver(observer);
        }
    }

    @SuppressWarnings("deprecation")
    private void onChange(boolean selfChange, Uri uri) {
        synchronized (mSelfObserverLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mContentObservable.dispatchChange(selfChange, uri);
            } else {
                mContentObservable.dispatchChange(selfChange);
            }

            if (selfChange) {
                for (Uri notifyUri : mNotifyUris) {
                    mContentResolver.notifyChange(notifyUri, mSelfObserver);
                }
            }

            if (!selfChange) {
                mChangedByUris.add(uri);
            }
        }
    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri notifyUri) {
        synchronized (mSelfObserverLock) {
            mNotifyUris.add(notifyUri);
            mContentResolver = contentResolver;
            if (mSelfObserver == null) {
                mSelfObserver = new SelfContentObserver(this);
            }
            mContentResolver.registerContentObserver(notifyUri, true, mSelfObserver);
            mSelfObserverRegistered = true;
        }
    }

    @Override
    public Uri getNotificationUri() {
        synchronized (mSelfObserverLock) {
            return getFirstNotificationUri();
        }
    }

    private Uri getFirstNotificationUri() {
        if (mNotifyUris != null) {
            Iterator<Uri> iterator = mNotifyUris.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }

    @Override
    protected void finalize() {
        try {
            super.finalize();

            if (mSelfObserver != null && mSelfObserverRegistered) {
                mContentResolver.unregisterContentObserver(mSelfObserver);
            }

            if (!mClosed) close();
        } catch (Throwable e) {
            // ignored
        }
    }

    private static class SelfContentObserver extends ContentObserver {

        WeakReference<MultiUriCursorWrapper> mCursor;

        public SelfContentObserver(MultiUriCursorWrapper cursor) {
            super(null);
            mCursor = new WeakReference<>(cursor);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            MultiUriCursorWrapper cursor = mCursor.get();
            if (cursor != null) {
                cursor.onChange(false, uri);
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
    }
}
