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

package com.oriondev.moneywallet.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andrea on 17/08/18.
 */
public class DoubleIconDrawable extends Drawable {

    private final Drawable mDrawable1;
    private final Drawable mDrawable2;

    public DoubleIconDrawable(Context context, Icon icon1, Icon icon2) {
        mDrawable1 = icon1 != null ? icon1.getDrawable(context) : IconLoader.UNKNOWN.getDrawable(context);
        mDrawable2 = icon2 != null ? icon2.getDrawable(context) : IconLoader.UNKNOWN.getDrawable(context);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        mDrawable1.setBounds(0, 0, width / 2, height);
        mDrawable2.setBounds(width / 2, 0, width, height);
        mDrawable1.draw(canvas);
        mDrawable2.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}