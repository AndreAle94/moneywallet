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

package com.oriondev.moneywallet.storage.database.model;

/**
 * Created by andrea on 27/10/18.
 */
public class Transaction extends BaseItem {

    public Long mId;
    public long mMoney;
    public String mDate;
    public String mDescription;
    public Long mCategory;
    public int mDirection;
    public int mType;
    public Long mWallet;
    public Long mPlace;
    public String mNote;
    public Long mSaving;
    public Long mDebt;
    public Long mEvent;
    public Long mRecurrence;
    public boolean mConfirmed;
    public boolean mCountInTotal;
    public String mTag;
}