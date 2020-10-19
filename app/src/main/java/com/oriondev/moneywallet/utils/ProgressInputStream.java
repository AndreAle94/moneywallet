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

package com.oriondev.moneywallet.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * Created by andrea on 26/07/18.
 */
public class ProgressInputStream extends InputStream {

    private static final int MIN_NOTIFY_RANGE = 5;

    private final InputStream mInputStream;
    private final UploadProgressListener mListener;

    private final long mFileSize;
    private long mProgress;
    private int mLastPercentage;

    public ProgressInputStream(File file, UploadProgressListener listener) throws IOException {
        this(new FileInputStream(file), file.length(), listener);
    }

    public ProgressInputStream(InputStream in, long size, UploadProgressListener listener) throws FileNotFoundException {
        mInputStream = in;
        mListener = listener;
        mFileSize = size;
        mProgress = 0L;
        mLastPercentage = 0;
    }

    @Override
    public int read() throws IOException {
        int b = mInputStream.read();
        mProgress += 1;
        notifyProgress();
        return b;
    }

    @Override
    public int read(@NonNull byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(@NonNull byte b[], int off, int len) throws IOException {
        int bytes = mInputStream.read(b, off, len);
        mProgress += bytes;
        notifyProgress();
        return bytes;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = mInputStream.skip(n);
        mProgress += skipped;
        notifyProgress();
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return mInputStream.available();
    }

    @Override
    public void close() throws IOException {
        mInputStream.close();
    }

    private void notifyProgress() {
        int currentPercentage = (int) ((mProgress * 100) / mFileSize);
        if (currentPercentage - mLastPercentage > MIN_NOTIFY_RANGE) {
            mLastPercentage = Math.min(currentPercentage, 100);
            mListener.onUploadProgressUpdate(mLastPercentage);
        }
    }

    public interface UploadProgressListener {

        void onUploadProgressUpdate(int percentage);
    }
}