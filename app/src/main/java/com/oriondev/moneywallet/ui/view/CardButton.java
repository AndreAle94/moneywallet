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

package com.oriondev.moneywallet.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 08/03/18.
 */
public class CardButton extends androidx.appcompat.widget.AppCompatButton {

    public CardButton(Context context) {
        super(context);
        initialize(getResources());
    }

    public CardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(getResources());
    }

    public CardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(getResources());
    }

    private void initialize(Resources resources) {
        // setTypeface(TypefaceCache.get(getContext(), TypefaceCache.ROBOTO_MEDIUM));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.material_component_lists_card_button_text_size));
        setMinWidth(resources.getDimensionPixelSize(R.dimen.material_component_lists_card_button_min_width));
        int internalPadding = resources.getDimensionPixelSize(R.dimen.material_component_lists_card_button_internal_padding);
        setPadding(
                internalPadding,
                getPaddingTop(),
                internalPadding,
                getPaddingBottom()
        );
    }
}