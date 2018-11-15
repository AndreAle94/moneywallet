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

/**
 * Created by andrea on 13/08/18.
 */
public class CategoryMoney {

    private final long mId;
    private final String mName;
    private final Icon mIcon;
    private final Money mMoney;

    public CategoryMoney(long id, String name, Icon icon, Money money) {
        mId = id;
        mName = name;
        mIcon = icon;
        mMoney = money;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Icon getIcon() {
        return mIcon;
    }

    public Money getMoney() {
        return mMoney;
    }
}