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

package com.oriondev.moneywallet.ui.view.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/** Simple view to draw a colored and sized dot */
public class DotView extends View {

    private Paint paint;
    private int size = -1;

    public DotView(Context context) {
        this(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DotView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (size == -1) {
            // Default size if full view
            size = w;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, size / 2, paint);
    }

    public void setColor(@ColorInt int color) {
        paint.setColor(color);
        invalidate();
    }

    public void setColorRes(@ColorRes int colorRes) {
        setColor(ContextCompat.getColor(getContext(), colorRes));
    }

    public void setCircleSize(int size) {
        this.size = size;
        invalidate();
    }

    public void setCircleSizeDp(int sizeInDp) {
        setCircleSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getResources().getDisplayMetrics()));
    }
}