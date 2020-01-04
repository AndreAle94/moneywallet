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
import androidx.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 25/07/18.
 */
public class ThemedSwitchPreference extends SwitchPreferenceCompat {

    public ThemedSwitchPreference(Context context) {
        super(context);
        initialize();
    }

    public ThemedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ThemedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setLayoutResource(R.layout.layout_preference_material_design);
    }
}