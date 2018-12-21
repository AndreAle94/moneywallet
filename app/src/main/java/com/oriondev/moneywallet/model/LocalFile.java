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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 21/03/2018.
 */
public class LocalFile implements IFile {

    private final File mFile;

    public LocalFile(File file) {
        mFile = file;
    }

    public LocalFile(String encoded) {
        mFile = new File(encoded);
    }

    private LocalFile(Parcel in) {
        mFile = new File(in.readString());
    }

    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {
        @Override
        public LocalFile createFromParcel(Parcel in) {
            return new LocalFile(in);
        }

        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }
    };

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getExtension() {
        int index = mFile.getName().lastIndexOf(".");
        return index >= 0 ? mFile.getName().substring(index) : null;
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public long getSize() {
        return mFile.length();
    }

    @Override
    public String encodeToString() {
        return mFile.getPath();
    }

    public File getFile() {
        return mFile;
    }

    public String getLocalPath() {
        return mFile.getPath();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFile.getPath());
    }
}