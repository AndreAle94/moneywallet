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

package com.oriondev.moneywallet.ui.preference;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 13/11/18.
 */

public class ThemedInputPreference extends Preference {

    private String mContent;

    private String mHint;
    private boolean mAllowEmptyInput;
    private int mInputType;

    private String mValue;

    public ThemedInputPreference(Context context) {
        super(context);
        initialize();
    }

    public ThemedInputPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ThemedInputPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setLayoutResource(R.layout.layout_preference_material_design);
        setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDialog();
                return false;
            }

        });
    }

    public void setContent(String content) {
        mContent = content;
    }

    public void setContent(int content) {
        mContent = getContext().getString(content);
    }

    public void setInput(String hint, boolean allowEmptyInput, int inputType) {
        mHint = hint;
        mAllowEmptyInput = allowEmptyInput;
        mInputType = inputType;
    }

    public void setCurrentValue(String value) {
        mValue = value;
    }

    public void setInput(int hint, boolean allowEmptyInput, int inputType) {
        mHint = getContext().getString(hint);
        mAllowEmptyInput = allowEmptyInput;
        mInputType = inputType;
    }

    private void showDialog() {
        ThemedDialog.buildMaterialDialog(getContext())
                .title(getTitle())
                .content(mContent)
                .inputType(mInputType)
                .input(mHint, mValue, mAllowEmptyInput, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!TextUtils.equals(input, mValue)) {
                            mValue = input.toString();
                            callChangeListener(mValue);
                        }
                    }

                })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }
}