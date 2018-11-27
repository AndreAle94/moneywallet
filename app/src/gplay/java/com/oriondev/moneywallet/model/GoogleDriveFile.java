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

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andre on 21/03/2018.
 */
public class GoogleDriveFile implements IFile, Parcelable {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SIZE = "size";
    private static final String DIRECTORY = "directory";

    private final DriveId mDriveId;
    private final String mName;
    private final long mSize;

    private final boolean mIsDirectory;

    private GoogleDriveFile(Parcel in) {
        mDriveId = in.readParcelable(DriveId.class.getClassLoader());
        mName = in.readString();
        mSize = in.readLong();
        mIsDirectory = in.readByte() != 0;
    }

    public GoogleDriveFile(Metadata metadata) {
        mDriveId = metadata.getDriveId();
        mName = metadata.getTitle();
        mSize = metadata.getFileSize();
        mIsDirectory = metadata.isFolder();
    }

    public GoogleDriveFile(String encoded) {
        try {
            JSONObject object = new JSONObject(encoded);
            mDriveId = DriveId.decodeFromString(object.getString(ID));
            mName = object.optString(NAME);
            mSize = object.optLong(SIZE);
            mIsDirectory = object.getBoolean(DIRECTORY);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot decode file from string: " + e.getMessage());
        }
    }

    public static final Creator<GoogleDriveFile> CREATOR = new Creator<GoogleDriveFile>() {

        @Override
        public GoogleDriveFile createFromParcel(Parcel in) {
            return new GoogleDriveFile(in);
        }

        @Override
        public GoogleDriveFile[] newArray(int size) {
            return new GoogleDriveFile[size];
        }

    };

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
            object.put(ID, mDriveId.encodeToString());
            object.put(NAME, mName);
            object.put(SIZE, mSize);
            object.put(DIRECTORY, mIsDirectory);
            return object.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Cannot encode file to string: " + e.getMessage());
        }
    }

    public DriveFolder getDriveFolder() {
        return mDriveId.asDriveFolder();
    }

    public DriveFile getDriveFile() {
        return mDriveId.asDriveFile();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDriveId, flags);
        dest.writeString(mName);
        dest.writeLong(mSize);
        dest.writeByte((byte) (mIsDirectory ? 1 : 0));
    }
}