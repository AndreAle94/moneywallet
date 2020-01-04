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

package com.oriondev.moneywallet.ui.view.theme;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

/**
 * Created by andrea on 10/04/18.
 */
/*package-local*/ class Util {

    @ColorInt
    /*package-local*/ static int adjustAlpha(@ColorInt int color, @SuppressWarnings("SameParameterValue") float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @ColorInt
    /*package-local*/ static int shiftColor(@ColorInt int color, @FloatRange(from = 0.0f, to = 2.0f) float by) {
        if (by == 1f) return color;
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= by; // value component
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    /*package-local*/ static int darkenColor(@ColorInt int color) {
        return shiftColor(color, 0.9f);
    }

    /*package-local*/ static boolean isColorLight(@ColorInt int color) {
        if (color == Color.BLACK) {
            return false;
        } else if (color == Color.WHITE || color == Color.TRANSPARENT) {
            return true;
        }
        return (1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255) < 0.4;
    }

    /*package-local*/ static boolean isColorLight(@ColorInt int color, @ColorInt int bgColor) {
        if (Color.alpha(color) < 128) {
            return isColorLight(bgColor);
        }
        return isColorLight(color);
    }

    /*package-local*/ static boolean isColorVisible(@ColorInt int color, @ColorInt int bgColor) {
        int redOffset = Math.abs(Color.red(color) - Color.red(bgColor));
        int greenOffset = Math.abs(Color.green(color) - Color.green(bgColor));
        int blueOffset = Math.abs(Color.blue(color) - Color.blue(bgColor));
        return redOffset + greenOffset + blueOffset > 50;
    }

    /*package-local*/ static int stripAlpha(@ColorInt int color) {
        return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }
}