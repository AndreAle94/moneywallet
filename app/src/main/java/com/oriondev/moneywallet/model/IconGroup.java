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

import java.util.List;

/**
 * Created by andrea on 12/08/18.
 */
public class IconGroup {

    private final String mGroupName;
    private final List<Icon> mGroupIcons;

    public IconGroup(String name, List<Icon> iconList) {
        mGroupName = name;
        mGroupIcons = iconList;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public List<Icon> getGroupIcons() {
        return mGroupIcons;
    }

    public int size() {
        return mGroupIcons.size();
    }
}