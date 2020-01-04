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
import android.graphics.drawable.Drawable;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 10/04/18.
 */
public class ThemedToolbar extends Toolbar implements ThemeEngine.ThemeConsumer {

    private BackgroundColor mBackgroundColor;

    public ThemedToolbar(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ThemedToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ThemedToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemedToolbar, defStyleAttr, 0);
        try {
            mBackgroundColor = BackgroundColor.fromValue(typedArray.getInt(R.styleable.ThemedToolbar_theme_backgroundColor, 0));
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
            setTitleTextColor(theme);
            setOverflowButtonColor(theme);
            setNavigationIconColor(theme);
            setMenuIconColor(theme);
        }
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        super.setNavigationIcon(icon);
        setNavigationIconColor(ThemeEngine.getTheme());
    }

    @Override
    public void inflateMenu(@MenuRes int resId) {
        super.inflateMenu(resId);
        setMenuIconColor(ThemeEngine.getTheme());
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

    private void setTitleTextColor(ITheme theme) {
        int background = getBackgroundColor(theme);
        int color = theme.getBestTextColor(background);
        setTitleTextColor(color);
        setSubtitleTextColor(color);
    }

    private void setOverflowButtonColor(ITheme theme) {
        Drawable drawable = getOverflowIcon();
        if (drawable != null) {
            int background = getBackgroundColor(theme);
            int color = theme.getBestTextColor(background);
            super.setOverflowIcon(TintHelper.createTintedDrawable(drawable, color));
        }
    }

    private void setNavigationIconColor(ITheme theme) {
        Drawable drawable = getNavigationIcon();
        if (drawable != null) {
            int background = getBackgroundColor(theme);
            int color = theme.getBestTextColor(background);
            if (drawable instanceof DrawerArrowDrawable) {
                ((DrawerArrowDrawable) drawable).setColor(color);
                super.setNavigationIcon(drawable);
            } else {
                super.setNavigationIcon(TintHelper.createTintedDrawable(drawable, color));
            }
        }
    }

    private void setMenuIconColor(ITheme theme) {
        Menu menu = getMenu();
        if (menu != null) {
            int background = getBackgroundColor(theme);
            int color = theme.getBestTextColor(background);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                Drawable drawable = item.getIcon();
                if (drawable != null) {
                    item.setIcon(TintHelper.createTintedDrawable(drawable, color));
                }
            }
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