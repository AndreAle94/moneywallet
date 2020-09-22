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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;

/**
 * Created by andrea on 26/07/18.
 */
public class ProgressOutputStream extends OutputStream {

    private static final int MIN_NOTIFY_RANGE = 5;

    private final OutputStream mOutputStream;
    private final DownloadProgressListener mListener;

    private final long mFileSize;
    private long mProgress;
    private int mLastPercentage;

    public ProgressOutputStream(File file, long size, DownloadProgressListener listener) throws FileNotFoundException {
        this(new FileOutputStream(file), size, listener);
    }

    public ProgressOutputStream(OutputStream out, long size, DownloadProgressListener listener) throws FileNotFoundException {
        mOutputStream = out;
        mListener = listener;
        mFileSize = size;
        mProgress = 0L;
        mLastPercentage = 0;
    }

    @Override
    public void write(int b) throws IOException {
        mOutputStream.write(b);
        mProgress += 1;
        notifyProgress();
    }

    @Override
    public void write(@NonNull byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(@NonNull byte b[], int off, int len) throws IOException {
        mOutputStream.write(b, off, len);
        mProgress += len;
        notifyProgress();
    }

    @Override
    public void flush() throws IOException {
        mOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        mOutputStream.close();
    }

    private void notifyProgress() {
        int currentPercentage = (int) ((mProgress * 100) / mFileSize);
        if (currentPercentage - mLastPercentage > MIN_NOTIFY_RANGE) {
            mLastPercentage = Math.min(currentPercentage, 100);
            mListener.onDownloadProgressUpdate(mLastPercentage);
        }
    }

    public interface DownloadProgressListener {

        void onDownloadProgressUpdate(int percentage);
    }
}