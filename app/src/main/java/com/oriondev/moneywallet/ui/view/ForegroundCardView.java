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
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 24/08/18.
 */
public class ForegroundCardView extends CardView {

    private int mMaxWidth = -1;
    private int mMaxHeight = -1;

    public ForegroundCardView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ForegroundCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ForegroundCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ForegroundCardView, defStyleAttr, 0);
        try {
            mMaxWidth = typedArray.getDimensionPixelSize(R.styleable.ForegroundCardView_fcv_max_width, -1);
            mMaxHeight = typedArray.getDimensionPixelSize(R.styleable.ForegroundCardView_fcv_max_height, -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // initialize the reference to the final values to pass to super class
        int finalWidthMeasureSpec = widthMeasureSpec;
        int finalHeightMeasureSpec = heightMeasureSpec;
        // first of all we need to get the current measured dimensions
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // check if width is larger than the maximum value
        if (mMaxWidth > -1) {
            int width = Math.min(widthSize, mMaxWidth);
            finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        // check if height is larger than the maximum value
        if (mMaxHeight > -1) {
            int height = Math.min(heightSize, mMaxHeight);
            finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        // apply the measured dimensions
        super.onMeasure(finalWidthMeasureSpec, finalHeightMeasureSpec);
    }
}