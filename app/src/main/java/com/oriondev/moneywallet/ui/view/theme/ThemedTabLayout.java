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
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 10/04/18.
 */
public class ThemedTabLayout extends TabLayout implements ThemeEngine.ThemeConsumer {

    private static final float UNFOCUSED_ALPHA = 0.5f;

    private BackgroundColor mBackgroundColor;

    public ThemedTabLayout(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ThemedTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ThemedTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemedTabLayout, defStyleAttr, 0);
        try {
            mBackgroundColor = BackgroundColor.fromValue(typedArray.getInt(R.styleable.ThemedTabLayout_theme_backgroundColor, 0));
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
            setIconsColor(theme);
            setTabTextColor(theme);
            setSelectedTabIndicatorColor(theme);
        }
    }

    private void setIconsColor(ITheme theme) {
        int background = getBackgroundColor(theme);
        int color = theme.getBestTextColor(background);
        ColorStateList stateList = new ColorStateList(
                new int[][] {
                        new int[] {-android.R.attr.state_selected},
                        new int[] {android.R.attr.state_selected}
                },
                new int[] {Util.adjustAlpha(color, UNFOCUSED_ALPHA), color}
        );
        for (int i = 0; i < getTabCount(); i++) {
            TabLayout.Tab tab = getTabAt(i);
            if (tab != null && tab.getIcon() != null) {
                tab.setIcon(TintHelper.createTintedDrawable(tab.getIcon(), stateList));
            }
        }
    }

    private void setTabTextColor(ITheme theme) {
        int background = getBackgroundColor(theme);
        int color = theme.getBestTextColor(background);
        setTabTextColors(Util.adjustAlpha(color, UNFOCUSED_ALPHA), color);
    }

    private void setSelectedTabIndicatorColor(ITheme theme) {
        setSelectedTabIndicatorColor(theme.getColorAccent());
    }

    private int getBackgroundColor(ITheme theme) {
        if (mBackgroundColor != null) {
            return mBackgroundColor.getColor(theme);
        } else {
            return theme.getColorPrimary();
        }
    }
}