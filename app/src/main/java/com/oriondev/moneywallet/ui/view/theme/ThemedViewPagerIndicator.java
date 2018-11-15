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

import com.itsronald.widget.ViewPagerIndicator;

/**
 * Created by andrea on 14/08/18.
 */
public class ThemedViewPagerIndicator extends ViewPagerIndicator implements ThemeEngine.ThemeConsumer {

    public ThemedViewPagerIndicator(Context context) {
        super(context);
    }

    public ThemedViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        setSelectedDotColor(theme.getColorPrimary());
        setUnselectedDotColor(theme.getIconColor());
    }
}