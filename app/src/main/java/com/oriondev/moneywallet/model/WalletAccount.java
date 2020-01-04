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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.DrawableRes;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 23/01/18.
 */
public class WalletAccount extends ProfileDrawerItem {

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    private long mId;
    private Money mMoney;

    @Override
    public WalletAccount withIdentifier(long identifier) {
        super.withIdentifier(identifier);
        return this;
    }

    @Override
    public WalletAccount withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public WalletAccount withEmail(String email) {
        throw new IllegalStateException("Email field is not supported in WalletAccount.");
    }

    @Override
    public StringHolder getEmail() {
        return new StringHolder(mMoneyFormatter.getNotTintedString(mMoney));
    }

    public WalletAccount withIcon(Context context, Icon icon) {
        Icon safeIcon = icon != null ? icon : IconLoader.UNKNOWN;
        if (safeIcon instanceof VectorIcon) {
            super.withIcon(((VectorIcon) safeIcon).getResource(context));
        } else if (safeIcon instanceof ColorIcon) {
            super.withIcon(((ColorIcon) safeIcon).getDrawable());
        }
        return this;
    }

    @Override
    public WalletAccount withIcon(Drawable icon) {
        super.withIcon(icon);
        return this;
    }

    @Override
    public WalletAccount withIcon(@DrawableRes int iconRes) {
        super.withIcon(iconRes);
        return this;
    }

    @Override
    public WalletAccount withIcon(Bitmap iconBitmap) {
        super.withIcon(iconBitmap);
        return this;
    }

    @Override
    public WalletAccount withIcon(IIcon icon) {
        super.withIcon(icon);
        return this;
    }

    @Override
    public WalletAccount withIcon(String url) {
        super.withIcon(url);
        return this;
    }

    @Override
    public WalletAccount withIcon(Uri uri) {
        super.withIcon(uri);
        return this;
    }

    public WalletAccount withId(long id) {
        mId = id;
        return this;
    }

    public long getId() {
        return mId;
    }

    public WalletAccount withMoney(String currency, long money) {
        mMoney = new Money(currency, money);
        return this;
    }

    public WalletAccount withMoney(Money money) {
        mMoney = money;
        return this;
    }
}