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
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by andrea on 08/03/18.
 */
public class MaterialProgressBar extends View {

    private Paint mPaint;
    private int mReachedBarColor;
    private int mUnreachedBarColor;

    private long mMaxValue;
    private long mProgressValue;

    public MaterialProgressBar(Context context) {
        super(context);
        initialize();
    }

    public MaterialProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MaterialProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMaxValue = 0;
        mProgressValue = 0;

        mUnreachedBarColor = Color.GRAY;
        mReachedBarColor = Color.RED;
    }

    public void setReachedBarColor(int color) {
        mReachedBarColor = color;
        invalidate();
    }

    public void setUnreachedBarColor(int color) {
        mUnreachedBarColor = color;
        invalidate();
    }

    public void setMaxValue(long maxValue) {
        mMaxValue = maxValue;
        invalidate();
    }

    public void setProgressValue(long progressValue) {
        mProgressValue = progressValue;
        invalidate();
    }

    protected double getProgress() {
        if (mMaxValue > 0 && mProgressValue > 0) {
            double progress = (double) mProgressValue / mMaxValue;
            return progress > 1 ? 1 : progress;
        }
        return 0d;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        // measure view
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (Math.min(measuredWidth, measuredHeight) == 0) {
            // skip drawing
            return;
        }
        // draw unreached bar
        mPaint.setColor(mUnreachedBarColor);
        c.drawRect(0, 0, measuredWidth, measuredHeight, mPaint);
        // calculate progress and draw on top
        mPaint.setColor(mReachedBarColor);
        int reachedBarWidth = (int) (getProgress() * measuredWidth);
        c.drawRect(0, 0, reachedBarWidth, measuredHeight, mPaint);
    }
}