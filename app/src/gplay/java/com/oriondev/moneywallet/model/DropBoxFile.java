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

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.google.android.gms.drive.DriveId;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andre on 21/03/2018.
 */
public class DropBoxFile implements IFile, Parcelable {

    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String SIZE = "size";
    private static final String DIRECTORY = "directory";

    private final String mName;
    private final String mPath;
    private final long mSize;

    private final boolean mIsDirectory;

    public DropBoxFile(Metadata metadata) {
        mName = metadata.getName();
        mPath = metadata.getPathDisplay();
        if (metadata instanceof FileMetadata) {
            mSize = ((FileMetadata) metadata).getSize();
            mIsDirectory = false;
        } else if (metadata instanceof FolderMetadata) {
            mSize = 0L;
            mIsDirectory = true;
        } else {
            mSize = -1L;
            mIsDirectory = mName.contains(".");
        }
    }

    public DropBoxFile(String encoded) {
        try {
            JSONObject object = new JSONObject(encoded);
            mName = object.optString(NAME);
            mPath = object.optString(PATH);
            mSize = object.optLong(SIZE);
            mIsDirectory = object.getBoolean(DIRECTORY);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot decode file from string: " + e.getMessage());
        }
    }

    private DropBoxFile(Parcel in) {
        mName = in.readString();
        mPath = in.readString();
        mSize = in.readLong();
        mIsDirectory = in.readByte() != 0;
    }

    public static final Creator<DropBoxFile> CREATOR = new Creator<DropBoxFile>() {
        @Override
        public DropBoxFile createFromParcel(Parcel in) {
            return new DropBoxFile(in);
        }

        @Override
        public DropBoxFile[] newArray(int size) {
            return new DropBoxFile[size];
        }
    };

    public String getPath() {
        return mPath;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getExtension() {
        int index = mName.lastIndexOf(".");
        return index >= 0 ? mName.substring(index) : null;
    }

    @Override
    public boolean isDirectory() {
        return mIsDirectory;
    }

    @Override
    public long getSize() {
        return mSize;
    }

    @Override
    public String encodeToString() {
        try {
            JSONObject object = new JSONObject();
            object.put(NAME, mName);
            object.put(PATH, mPath);
            object.put(SIZE, mSize);
            object.put(DIRECTORY, mIsDirectory);
            return object.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Cannot encode file to string: " + e.getMessage());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPath);
        dest.writeLong(mSize);
        dest.writeByte((byte) (mIsDirectory ? 1 : 0));
    }
}