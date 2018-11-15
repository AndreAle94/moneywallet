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

import android.content.Context;
import android.util.AttributeSet;

import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by andrea on 20/08/18.
 */
public class ThemedProgressWheel extends ProgressWheel implements ThemeEngine.ThemeConsumer {

    public ThemedProgressWheel(Context context) {
        super(context);
    }

    public ThemedProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        // This view is commonly used on the background color so we can
        // double check if the color to use as bar-color is very similar
        // to the color of the background. In this case we can dynamically
        // calculate the best color to use instead of the color accent.
        int barColor = theme.getColorAccent();
        int windowBackground = theme.getColorWindowBackground();
        if (!Util.isColorVisible(barColor, windowBackground)) {
            barColor = theme.getBestIconColor(windowBackground);
        }
        setBarColor(barColor);
    }
}