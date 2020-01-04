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
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.oriondev.moneywallet.ui.view.calendar.TimelineView;

/**
 * Created by andrea on 13/04/18.
 */
public class ThemedTimelineView extends TimelineView implements ThemeEngine.ThemeConsumer {

    public ThemedTimelineView(Context context) {
        super(context);
    }

    public ThemedTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedTimelineView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        int textColor = theme.getTextColorPrimary();
        setDayLabelColor(textColor);
        setDateLabelColor(textColor);
        setDateLabelSelectedColor(theme.getColorAccent());
        EdgeGlowUtil.setEdgeGlowColor(this, theme.getColorPrimary(), null);
    }
}