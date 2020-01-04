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
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.oriondev.moneywallet.R;
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;

/**
 * Created by andrea on 20/08/18.
 */
public class ThemedDialog {

    public static MaterialDialog.Builder buildMaterialDialog(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        ITheme theme = ThemeEngine.getTheme();
        builder.theme(theme.isDark() ? Theme.DARK : Theme.LIGHT);
        builder.positiveColor(theme.getColorAccent());
        builder.negativeColor(theme.getColorAccent());
        builder.neutralColor(theme.getColorAccent());
        builder.widgetColor(theme.getColorAccent());
        return builder;
    }

    public static ColorChooserDialog.Builder buildColorChooserDialog(Context context, @StringRes int title) {
        ColorChooserDialog.Builder builder = new ColorChooserDialog.Builder(context, R.string.dialog_color_picker_title);
        ITheme theme = ThemeEngine.getTheme();
        builder.theme(theme.isDark() ? Theme.DARK : Theme.LIGHT);
        return builder;
    }

    public static DatePickerDialog.Builder buildDatePickerDialog(DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {
        DatePickerDialog.Builder builder = new DatePickerDialog.Builder(listener, year, monthOfYear, dayOfMonth);
        ITheme theme = ThemeEngine.getTheme();
        builder.setThemeDark(theme.isDark());
        builder.setAccentColor(theme.getColorAccent());
        return builder;
    }

    public static NumberPadTimePickerDialog.Builder buildNumberPadTimePickerDialog(BottomSheetTimePickerDialog.OnTimeSetListener listener, boolean is24HourMode) {
        NumberPadTimePickerDialog.Builder builder = new NumberPadTimePickerDialog.Builder(listener, is24HourMode);
        ITheme theme = ThemeEngine.getTheme();
        builder.setThemeDark(theme.isDark());
        builder.setAccentColor(theme.getColorAccent());
        return builder;
    }

    public static BottomSheetBuilder buildBottomSheet(Context context) {
        BottomSheetBuilder builder = new BottomSheetBuilder(context);
        ITheme theme = ThemeEngine.getTheme();
        builder.setBackgroundColor(theme.getDialogBackgroundColor());
        builder.setTitleTextColor(theme.getTextColorPrimary());
        builder.setItemTextColor(theme.getTextColorPrimary());
        builder.setIconTintColor(theme.getIconColor());
        return builder;
    }
}