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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by andrea on 02/03/18.
 */
public class CategoryChildIndicator extends View {

    private Paint mPaint;
    private boolean mIsLast;

    public CategoryChildIndicator(Context context) {
        super(context);
        initialize();
    }

    public CategoryChildIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CategoryChildIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CategoryChildIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mIsLast = true;
    }

    public void setLast(boolean last) {
        mIsLast = last;
    }

    public void setLineColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int lineSize = 8;
        if (width == 0 || height == 0) {
            return;
        }
        int startX = (width / 2) - (lineSize / 2);
        int endX = startX + lineSize;
        int startY = 0;
        int endY = mIsLast ? ((height / 2) + (lineSize / 2)) : height;
        c.drawRect(startX, startY, endX, endY, mPaint);
        endX = width;
        startY = (height / 2) + (lineSize / 2);
        endY = startY + lineSize;
        c.drawRect(startX, startY, endX, endY, mPaint);
    }
}