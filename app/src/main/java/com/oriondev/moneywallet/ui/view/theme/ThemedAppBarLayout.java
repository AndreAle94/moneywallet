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
import android.content.res.TypedArray;
import com.google.android.material.appbar.AppBarLayout;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 25/07/18.
 */
public class ThemedAppBarLayout extends AppBarLayout implements ThemeEngine.ThemeConsumer {

    private BackgroundColor mBackgroundColor;

    public ThemedAppBarLayout(Context context) {
        super(context);
        initialize(context, null);
    }

    public ThemedAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemedAppBarLayout, 0, 0);
        try {
            mBackgroundColor = BackgroundColor.fromValue(typedArray.getInt(R.styleable.ThemedAppBarLayout_theme_backgroundColor, 0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        if (mBackgroundColor != null) {
            int background = getBackgroundColor(theme);
            setBackgroundColor(background);
        }
    }

    private int getBackgroundColor(ITheme theme) {
        if (mBackgroundColor != null) {
            if (mBackgroundColor == BackgroundColor.COLOR_PRIMARY) {
                return theme.getColorPrimary();
            } else {
                return theme.getColorPrimaryDark();
            }
        } else {
            return theme.getColorPrimary();
        }
    }

    public enum BackgroundColor {
        COLOR_PRIMARY(0),
        COLOR_PRIMARY_DARK(1);

        private int mValue;

        BackgroundColor(int value) {
            mValue = value;
        }

        static BackgroundColor fromValue(int value) {
            switch (value) {
                case 0:
                    return COLOR_PRIMARY;
                case 1:
                    return COLOR_PRIMARY_DARK;
                default:
                    return null;
            }
        }
    }
}