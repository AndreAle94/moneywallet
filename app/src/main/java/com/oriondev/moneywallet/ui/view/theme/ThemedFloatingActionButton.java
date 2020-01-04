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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Created by andrea on 11/04/18.
 */
public class ThemedFloatingActionButton extends FloatingActionButton implements ThemeEngine.ThemeConsumer {

    public ThemedFloatingActionButton(Context context) {
        super(context);
    }

    public ThemedFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        boolean isDark = theme.isDark();
        int colorAccent = theme.getColorAccent();
        int colorPressed = Util.shiftColor(colorAccent, isDark ? 0.9f : 1.1f);
        int colorActivated = Util.shiftColor(colorAccent, isDark ? 1.1f : 0.9f);
        int colorRipple = theme.getColorRipple();
        ColorStateList stateList = new ColorStateList(
                new int[][] {
                        new int[] {-android.R.attr.state_pressed},
                        new int[] {android.R.attr.state_pressed}
                },
                new int[] {colorAccent, colorPressed}
        );
        setRippleColor(colorRipple);
        setBackgroundTintList(stateList);
        setIconColor(theme);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        setIconColor(ThemeEngine.getTheme());
    }

    private void setIconColor(ITheme theme) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int color = theme.getBestColor(theme.getColorAccent());
            super.setImageDrawable(TintHelper.createTintedDrawable(drawable, color));
        }
    }
}