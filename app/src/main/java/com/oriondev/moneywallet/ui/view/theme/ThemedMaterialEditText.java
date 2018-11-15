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
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;

/**
 * Created by andrea on 20/08/18.
 */
public class ThemedMaterialEditText extends MaterialEditText implements ThemeEngine.ThemeConsumer {

    private BackgroundColor mBackgroundColor;

    public ThemedMaterialEditText(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ThemedMaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ThemedMaterialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemedMaterialEditText, defStyleAttr, 0);
        try {
            mBackgroundColor = BackgroundColor.fromValue(typedArray.getInt(R.styleable.ThemedMaterialEditText_theme_backgroundColor, BackgroundColor.getValue(BackgroundColor.COLOR_WINDOW_FOREGROUND)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        if (mBackgroundColor != null) {
            int backgroundColor = mBackgroundColor.getColor(theme);
            setTextColor(theme.getBestTextColor(backgroundColor));
            setFloatingLabelColorNormal(theme.getBestHintColor(backgroundColor));
            setHintTextColor(theme.getBestHintColor(backgroundColor));
            setFloatingLabelColorFocused(theme.getColorAccent());
            setLeftIconColorNormal(theme.getBestIconColor(backgroundColor));
            setLeftIconColorFocused(theme.getColorAccent());
            setBottomLineColorNormal(theme.getBestIconColor(backgroundColor));
            setBottomLineColorFocused(theme.getColorAccent());
            setBottomLineColorError(theme.getErrorColor());
            TintHelper.setCursorTint(this, theme.getColorAccent());
        }
    }
}