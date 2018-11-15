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

package com.oriondev.moneywallet.storage.wrapper;

import android.database.AbstractCursor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.SparseArray;

import com.oriondev.moneywallet.model.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 03/03/18.
 */
public abstract class AbstractHeaderCursor<H> extends AbstractCursor {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ITEM = 1;

    private final Cursor mCursor;
    private final SparseArray<H> mHeaders;
    private final List<Pair<Integer, Integer>> mIndices;
    private final int mHeaderColumnCount;
    private final String[] mColumnNames;

    private boolean mIsHeader;
    private int mPosition;

    /*package-local*/ AbstractHeaderCursor(Cursor cursor) {
        mCursor = cursor;
        mHeaders = new SparseArray<>();
        mIndices = new ArrayList<>();
        String[] headerColumnNames = getHeaderColumnNames();
        mHeaderColumnCount = headerColumnNames.length;
        mColumnNames = generateColumnNames(headerColumnNames, cursor);
    }

    private String[] generateColumnNames(String[] headerColumnNames, Cursor cursor) {
        String[] cursorColumnNames = cursor.getColumnNames();
        String[] columnNames = new String[headerColumnNames.length + cursorColumnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            if (i < headerColumnNames.length) {
                columnNames[i] = headerColumnNames[i];
            } else {
                columnNames[i] = cursorColumnNames[i - headerColumnNames.length];
            }
        }
        return columnNames;
    }

    protected abstract void generateHeaders(Cursor cursor);

    protected H getHeader() {
        return mHeaders.get(mPosition);
    }

    protected boolean isHeader() {
        return mIsHeader;
    }

    protected void addHeader(H header) {
        int id = mHeaders.size();
        mHeaders.put(id, header);
        mIndices.add(new Pair<>(TYPE_HEADER, id));
    }

    protected void addItem(int position) {
        mIndices.add(new Pair<>(TYPE_ITEM, position));
    }

    /**
     * Obtain the size of the wrapped cursor.
     * It must consider the size of the SQL cursor and the size of all the headers.
     * @return the correct size of this wrapper.
     */
    @Override
    public int getCount() {
        return mIndices.size();
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (newPosition < mIndices.size()) {
            Pair<Integer, Integer> item = mIndices.get(newPosition);
            mIsHeader = item.getL() == TYPE_HEADER;
            mPosition = item.getR();
            if (!mIsHeader) {
                mCursor.moveToPosition(mPosition);
            }
            return true;
        }
        return false;
    }

    @Override
    public String[] getColumnNames() {
        return mColumnNames;
    }

    protected abstract String[] getHeaderColumnNames();

    @Override
    public String getString(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderString(i);
        }
        return mCursor.getString(i - mHeaderColumnCount);
    }

    protected abstract String getHeaderString(int index);

    @Override
    public short getShort(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderShort(i);
        }
        return mCursor.getShort(i - mHeaderColumnCount);
    }

    protected abstract short getHeaderShort(int index);

    @Override
    public int getInt(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderInt(i);
        }
        return mCursor.getInt(i - mHeaderColumnCount);
    }

    protected abstract int getHeaderInt(int index);

    @Override
    public long getLong(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderLong(i);
        }
        return mCursor.getLong(i - mHeaderColumnCount);
    }

    protected abstract long getHeaderLong(int index);

    @Override
    public float getFloat(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderFloat(i);
        }
        return mCursor.getFloat(i - mHeaderColumnCount);
    }

    protected abstract float getHeaderFloat(int index);

    @Override
    public double getDouble(int i) {
        if (i < mHeaderColumnCount) {
            return getHeaderDouble(i);
        }
        return mCursor.getDouble(i - mHeaderColumnCount);
    }

    protected abstract double getHeaderDouble(int index);

    @Override
    public boolean isNull(int i) {
        if (i < mHeaderColumnCount) {
            return isHeaderNull(i);
        }
        return mCursor.isNull(i - mHeaderColumnCount);
    }

    protected abstract boolean isHeaderNull(int index);

    @Override
    public void deactivate() {
        mCursor.deactivate();
        super.deactivate();
    }

    @Override
    public void close() {
        mCursor.close();
        super.close();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        mCursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        mCursor.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mCursor.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mCursor.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean requery() {
        return mCursor.requery();
    }
}