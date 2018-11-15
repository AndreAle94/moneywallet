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

import android.database.Cursor;

import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.storage.database.Contract;

/**
 * Created by andrea on 05/03/18.
 */

public class DebtHeaderCursor extends AbstractHeaderCursor<DebtHeaderCursor.Header> {

    public static final String COLUMN_ITEM_TYPE = "item_type";
    public static final String COLUMN_HEADER_TYPE = "header_type";
    public static final String COLUMN_HEADER_MONEY = "header_money";

    public static final int INDEX_ITEM_TYPE = 0;
    public static final int INDEX_HEADER_TYPE = 1;
    public static final int INDEX_HEADER_MONEY = 2;

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_ITEM = 1;

    public static final int HEADER_CURRENT = 0;
    public static final int HEADER_ARCHIVED = 1;

    private final int mIndexDebtType;
    private final Contract.DebtType mDebtType;

    public DebtHeaderCursor(Cursor cursor, Contract.DebtType debtType) {
        super(cursor);
        generateHeaders(cursor);
        mIndexDebtType = getHeaderColumnNames().length + cursor.getColumnIndex(Contract.Debt.TYPE);
        mDebtType = debtType;
    }

    @Override
    protected void generateHeaders(Cursor cursor) {
        int indexDebtCurrency = cursor.getColumnIndex(Contract.Debt.WALLET_CURRENCY);
        int indexDebtMoney = cursor.getColumnIndex(Contract.Debt.MONEY);
        int indexDebtProgress = cursor.getColumnIndex(Contract.Debt.PROGRESS);
        int indexDebtArchived = cursor.getColumnIndex(Contract.Debt.ARCHIVED);
        if (cursor.moveToFirst()) {
            Header header = null;
            do {
                int archived = cursor.getInt(indexDebtArchived);
                if (header != null) {
                    // check the header state
                    if (archived == 0 && header.mType == 1) {
                        throw new IllegalStateException("SQL query has failed to sort the items.");
                    }
                    if (header.mType == 0 && archived == 1) {
                        // we can store the previous header and create a new one
                        header = new Header(HEADER_ARCHIVED);
                        addHeader(header);
                    }
                } else {
                    // initialize the header based on current item
                    header = new Header(archived == 0 ? HEADER_CURRENT : HEADER_ARCHIVED);
                    addHeader(header);
                }
                addItem(cursor.getPosition());
                // if current header than sum the remaining money
                if (archived == 0) {
                    String currency = cursor.getString(indexDebtCurrency);
                    long money = cursor.getLong(indexDebtMoney);
                    long progress = cursor.getLong(indexDebtProgress);
                    long modulus = Math.abs(money) - Math.abs(progress);
                    // TODO if modulus is < 0 than set it to 0?
                    header.addMoney(currency, modulus);
                }
            } while (cursor.moveToNext());
        }
    }

    @Override
    protected String[] getHeaderColumnNames() {
        return new String[] {
                COLUMN_ITEM_TYPE,
                COLUMN_HEADER_TYPE,
                COLUMN_HEADER_MONEY
        };
    }

    @Override
    protected String getHeaderString(int index) {
        if (isHeader()) {
            switch (index) {
                case INDEX_HEADER_MONEY:
                    Header header = getHeader();
                    return header.mMoney.toString();
            }
        }
        return null;
    }

    @Override
    protected short getHeaderShort(int index) {
        return 0;
    }

    @Override
    public int getInt(int index) {
        if (index == mIndexDebtType) {
            return mDebtType.getValue();
        }
        return super.getInt(index);
    }

    @Override
    protected int getHeaderInt(int index) {
        switch (index) {
            case INDEX_ITEM_TYPE:
                return isHeader() ? TYPE_HEADER : TYPE_ITEM;
            case INDEX_HEADER_TYPE:
                return getHeader().mType;
        }
        return 0;
    }

    @Override
    protected long getHeaderLong(int index) {
        return 0;
    }

    @Override
    protected float getHeaderFloat(int index) {
        return 0;
    }

    @Override
    protected double getHeaderDouble(int index) {
        return 0;
    }

    @Override
    protected boolean isHeaderNull(int index) {
        return false;
    }

    /*package-local*/ class Header {

        private final int mType;
        private final Money mMoney;

        private Header(int type) {
            mType = type;
            mMoney = new Money();
        }

        private void addMoney(String currency, long money) {
            mMoney.addMoney(currency, money);
        }
    }
}