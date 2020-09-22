package com.oriondev.moneywallet.model;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

/**
 * Represents a file/document with regards to the android storage access framework.
 */
public class SAFFile implements IFile {

    private static final String URI = "uri";
    private static final String NAME = "name";
    private static final String SIZE = "size";
    private static final String IS_DIRECTORY = "isDir";

    private final Uri mUri;
    private String mName;
    private long mSize;
    private boolean mIsDir;

    public static final Creator<SAFFile> CREATOR = new Creator<SAFFile>() {
        @Override
        public SAFFile createFromParcel(Parcel in) {
            return new SAFFile(in);
        }

        @Override
        public SAFFile[] newArray(int size) {
            return new SAFFile[size];
        }
    };

    public SAFFile(@NonNull String encoded) {
        try {
            JSONObject object = new JSONObject(encoded);
            mUri = Uri.parse(object.getString(URI));
            mName = object.getString(NAME);
            mSize = object.getLong(SIZE);
            mIsDir = object.getBoolean(IS_DIRECTORY);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot decode file from string: " + e.getMessage());
        }
    }

    public SAFFile(@NonNull DocumentFile documentFile) {
        mIsDir = documentFile.isDirectory();
        mUri = documentFile.getUri();
        mSize = documentFile.length();
        mName = documentFile.getName();
    }

    public SAFFile(Parcel in) {
        mUri = Uri.parse(in.readString());
        mName = in.readString();
        mSize = in.readLong();
        mIsDir = in.readByte() != 0;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getExtension() {
        int index = getName().lastIndexOf(".");
        return index >= 0 ? getName().substring(index) : null;
    }

    @Override
    public boolean isDirectory() {
        return mIsDir;
    }

    @Override
    public long getSize() {
        return mSize;
    }

    @Override
    public String encodeToString() {
        try {
            JSONObject object = new JSONObject();
            object.put(URI, mUri.toString());
            object.put(NAME, mName);
            object.put(SIZE, mSize);
            object.put(IS_DIRECTORY, mIsDir);
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
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mUri.toString());
        dest.writeString(mName);
        dest.writeLong(mSize);
        dest.writeByte((byte) (mIsDir ? 1 : 0));
    }

    public Uri getUri() {
        return mUri;
    }
}
