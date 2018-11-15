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

/**
 * Created by andrea on 13/04/18.
 */
/*package-local*/ enum BackgroundColor {

    COLOR_PRIMARY(0),
    COLOR_PRIMARY_DARK(1),
    COLOR_ACCENT(2),
    COLOR_WINDOW_BACKGROUND(3),
    COLOR_WINDOW_FOREGROUND(4);

    private int mValue;

    /*package-local*/ BackgroundColor(int value) {
        mValue = value;
    }

    /*package-local*/ static int getValue(BackgroundColor backgroundColor) {
        return backgroundColor.mValue;
    }

    /*package-local*/ int getColor(ITheme theme) {
        switch (mValue) {
            case 0:
                return theme.getColorPrimary();
            case 1:
                return theme.getColorPrimaryDark();
            case 2:
                return theme.getColorAccent();
            case 4:
                return theme.getColorWindowForeground();
            default:
                return theme.getColorWindowBackground();
        }
    }

    /*package-local*/ static BackgroundColor fromValue(int value) {
        switch (value) {
            case 0:
                return COLOR_PRIMARY;
            case 1:
                return COLOR_PRIMARY_DARK;
            case 2:
                return COLOR_ACCENT;
            case 3:
                return COLOR_WINDOW_BACKGROUND;
            case 4:
                return COLOR_WINDOW_FOREGROUND;
            default:
                return null;
        }
    }
}