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
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 10/04/18.
 */
public class ThemedTextView extends AppCompatTextView implements ThemeEngine.ThemeConsumer {

    private TextColor mTextColor;

    public ThemedTextView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ThemedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ThemedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemedTextView, defStyleAttr, 0);
        try {
            mTextColor = TextColor.fromValue(typedArray.getInt(R.styleable.ThemedTextView_theme_textColor, 1));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void onApplyTheme(ITheme theme) {
        if (mTextColor != null) {
            switch (mTextColor) {
                case PRIMARY:
                    setTextColor(theme.getTextColorPrimary());
                    break;
                case SECONDARY:
                    setTextColor(theme.getTextColorSecondary());
                    break;
                case PRIMARY_INVERSE:
                    setTextColor(theme.getTextColorPrimaryInverse());
                    break;
                case SECONDARY_INVERSE:
                    setTextColor(theme.getTextColorSecondaryInverse());
                    break;
                case COLOR_PRIMARY:
                    setTextColor(theme.getColorPrimary());
                    break;
                case COLOR_ACCENT:
                    setTextColor(theme.getColorAccent());
                    break;
                case OVER_COLOR_PRIMARY:
                    setTextColor(theme.getBestTextColor(theme.getColorPrimary()));
                    break;
                case OVER_COLOR_PRIMARY_DARK:
                    setTextColor(theme.getBestTextColor(theme.getColorPrimaryDark()));
                    break;
                case OVER_COLOR_ACCENT:
                    setTextColor(theme.getBestTextColor(theme.getColorAccent()));
                    break;
                default:
                    System.out.println("{WARN} UNKNOWN");
                    break;
            }
        } else {
            System.out.println("{WARN} IS NULL WTF?");
        }
    }

    public enum TextColor {
        PRIMARY(0),
        SECONDARY(1),
        PRIMARY_INVERSE(2),
        SECONDARY_INVERSE(3),
        COLOR_PRIMARY(4),
        COLOR_ACCENT(5),
        OVER_COLOR_PRIMARY(6),
        OVER_COLOR_PRIMARY_DARK(7),
        OVER_COLOR_ACCENT(8);

        private int mValue;

        TextColor(int value) {
            mValue = value;
        }

        static TextColor fromValue(int value) {
            switch (value) {
                case 0:
                    return PRIMARY;
                case 1:
                    return SECONDARY;
                case 2:
                    return PRIMARY_INVERSE;
                case 3:
                    return SECONDARY_INVERSE;
                case 4:
                    return COLOR_PRIMARY;
                case 5:
                    return COLOR_ACCENT;
                case 6:
                    return OVER_COLOR_PRIMARY;
                case 7:
                    return OVER_COLOR_PRIMARY_DARK;
                case 8:
                    return OVER_COLOR_ACCENT;
                default:
                    return null;
            }
        }
    }
}