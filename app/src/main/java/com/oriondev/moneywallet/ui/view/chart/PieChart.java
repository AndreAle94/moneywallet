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

package com.oriondev.moneywallet.ui.view.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 13/08/18.
 */
public class PieChart extends View {

    private final static float MIN_DISTANCE = 15f;
    private final static int MAX_ITERATIONS = 10000;

    private final static boolean DEFAULT_HOLE_ENABLED = true;
    private final static boolean DEFAULT_SHADOW_ENABLED = true;
    private final static boolean DEFAULT_PERCENTAGE_ENABLED = false;
    private final static int DEFAULT_SHADOW_COLOR = Color.parseColor("#40000000");
    private final static int DEFAULT_HOLE_COLOR = Color.WHITE;
    private final static int DEFAULT_LINE_COLOR = Color.GRAY;
    private final static float DEFAULT_LINE_SIZE = 3f;

    // current data
    private int[] mColors = new int[] {
            Color.rgb(204, 198, 24),
            Color.rgb(229, 163, 25),
            Color.rgb(232, 111, 40),
            Color.rgb(212, 75, 145),
            Color.rgb(117, 96, 165),
            Color.rgb(54, 142, 92),
            Color.rgb(129, 191, 22),
            Color.rgb(224, 184, 26),
            Color.rgb(229, 138, 24),
            Color.rgb(235, 89, 92),
            Color.rgb(167, 78, 160),
            Color.rgb(66, 117, 138),
            Color.rgb(85, 169, 48)
    };

    private boolean mHoleEnabled;
    private boolean mShadowEnabled;
    private boolean mPercentageEnabled;

    private RectF mRectangle;
    private Paint mSlicePaint;
    private Paint mLinePaint;

    private boolean mIsRunning;
    private PieData mPieData;

    private int mShadowColor;
    private int mHoleColor;
    private int mLineColor;

    public PieChart(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // initialize parameters
        mIsRunning = false;
        mRectangle = new RectF();
        mSlicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // obtain attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieChart, defStyleAttr, defStyleRes);
        try {
            mHoleEnabled = a.getBoolean(R.styleable.PieChart_pie_chartHoleEnabled, DEFAULT_HOLE_ENABLED);
            mShadowEnabled = a.getBoolean(R.styleable.PieChart_pie_chartShadowEnabled, DEFAULT_SHADOW_ENABLED);
            mPercentageEnabled = a.getBoolean(R.styleable.PieChart_pie_chartPercentageEnabled, DEFAULT_PERCENTAGE_ENABLED);
            mShadowColor = a.getColor(R.styleable.PieChart_pie_chartShadowColor, DEFAULT_SHADOW_COLOR);
            mHoleColor = a.getColor(R.styleable.PieChart_pie_chartHoleColor, DEFAULT_HOLE_COLOR);
            mLinePaint.setColor(a.getColor(R.styleable.PieChart_pie_chartLineColor, DEFAULT_LINE_COLOR));
            mLinePaint.setStrokeWidth(a.getFloat(R.styleable.PieChart_pie_chartLineSize, DEFAULT_LINE_SIZE));
        } catch (Exception e) {
            // initialize variables at default state
            mHoleEnabled = DEFAULT_HOLE_ENABLED;
            mShadowEnabled = DEFAULT_SHADOW_ENABLED;
            mPercentageEnabled = DEFAULT_PERCENTAGE_ENABLED;
            mShadowColor = DEFAULT_SHADOW_COLOR;
            mHoleColor = DEFAULT_HOLE_COLOR;
            mLineColor = DEFAULT_LINE_COLOR;
            mLinePaint.setStrokeWidth(DEFAULT_LINE_SIZE);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    public void setHoleEnabled(boolean enabled) {
        mHoleEnabled = enabled;
        if (!mHoleEnabled) {
            mShadowEnabled = false;
        }
        invalidate();
    }

    public void setLineColor(int color) {
        mLineColor = color;
    }

    public void setHoleColor(int color) {
        mHoleColor = color;
    }

    public void setShadowColor(int color) {
        mShadowColor = color;
    }

    public void setChartData(PieData pieData) {
        mPieData = pieData;
        if (mPieData == null) {
            mIsRunning = false;
            return;
        }
        mIsRunning = true;
        // calculate chart data async
        new Thread(new Runnable() {

            @Override
            public void run() {
                // check if some slices should be grouped
                int maxSlices = (int) (360f / MIN_DISTANCE);
                if (mPieData.size() > maxSlices) {
                    // create "other" slice
                    int slicesToRemove = mPieData.size() - maxSlices + 1;
                    long extraSliceValue = 0L;
                    for (int i = 0; i < slicesToRemove; i++) {
                        // lock for smallest slice
                        int smallSliceIndex = -1;
                        long smallSliceValue = 0;
                        for (int n = 0; n < mPieData.size(); n++) {
                            long sliceValue = mPieData.get(n).getValue();
                            if (smallSliceIndex == -1 || smallSliceValue > sliceValue) {
                                smallSliceIndex = n;
                                smallSliceValue = sliceValue;
                            }
                        }
                        // remove index and add slice value
                        extraSliceValue += smallSliceValue;
                        mPieData.remove(smallSliceIndex);
                    }
                    // create extra slice
                    mPieData.add(new PieSlice("others", extraSliceValue, null, true));
                }
                // calculate slice size
                float totalValue = 0L;
                for (PieSlice slice : mPieData.getAllSlices()) {
                    totalValue += slice.getValue();
                }
                // calculate start and sweep angle
                float startAngle = 0;
                for (int i = 0; i < mPieData.size(); i++) {
                    // process slice by slice
                    float sweepAngle = 360f * mPieData.get(i).getValue() / totalValue;
                    mPieData.get(i).setStartAngle(startAngle);
                    mPieData.get(i).setIconAngle(startAngle + (sweepAngle / 2));
                    mPieData.get(i).setSweepAngle(sweepAngle);
                    // update start angle for the next slice
                    startAngle += sweepAngle;
                }
                // move icons if overlaps
                int icons = 1;
                float backAngle = 0f;
                int i;
                for (i = 1; (icons > 1 || iconOverlaps()) && i < MAX_ITERATIONS; i++) {
                    // get current and previous angle
                    float previousAngle = mPieData.get((i - 1) % mPieData.size()).getIconAngle() + ((i - 1) / mPieData.size() * 360f);
                    float currentAngle = mPieData.get(i % mPieData.size()).getIconAngle() + (i / mPieData.size() * 360f);
                    // compare if overlaps
                    float difference = currentAngle - previousAngle + backAngle;
                    if (difference < MIN_DISTANCE) {
                        // icons overlaps
                        float moveDegrees = MIN_DISTANCE - difference;
                        mPieData.get(i % mPieData.size()).increaseIconAngle(moveDegrees);
                        icons += 1;
                        backAngle += moveDegrees;
                    } else {
                        // icon not overlaps, move everything back if needed
                        if (icons > 1) {
                            float moveAngle = backAngle / icons;
                            for (int j = (i - 1); j >= (i - icons); j--) {
                                mPieData.get(j % mPieData.size()).decreaseIconAngle(moveAngle);
                            }
                            // reset flags
                            icons = 1;
                            backAngle = 0f;
                        }
                    }
                }
                mIsRunning = false;
                postInvalidate();
            }

        }).start();
    }

    private boolean iconOverlaps() {
        // check if icons overlaps
        for (int i = 1; i <= mPieData.size(); i++) {
            // get current slice angle and the previous one
            float previousAngle = mPieData.get((i - 1) % mPieData.size()).getIconAngle() + ((i - 1) / mPieData.size() * 360f);
            float currentAngle = mPieData.get(i % mPieData.size()).getIconAngle() + (i / mPieData.size() * 360f);
            // check if current angle is not okay
            if ((currentAngle - previousAngle) < MIN_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    private Bitmap scaleIcon(Bitmap source, float iconRadius) {
        int size = (int) (iconRadius * 2);
        return Bitmap.createScaledBitmap(source, size, size, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // get current height param and height size
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // calculate best height from width size
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int bestHeight = (int) (width * 0.75);
        // set appropriate height size
        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(bestHeight, heightSize);
        } else {
            height = bestHeight;
        }
        // pass new measure specific to super class
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        // need to call super method
        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas c) {
        // stop if size is 0
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth == 0 || measuredHeight == 0) return;
        // if data is null or empty return
        if (mPieData == null || mPieData.size() == 0 || mIsRunning || mColors.length == 0) return;
        // get view size
        float centerX = measuredWidth / 2;
        float centerY = measuredHeight / 2;
        float maxRadius = Math.min(centerX, centerY);
        float chartRadius = maxRadius * 0.5f;
        float iconDistance = maxRadius * 0.8f;
        float iconRadius = maxRadius * 0.1f;
        float textDistance = maxRadius * 0.95f;
        float textSize = 30;
        mLinePaint.setColor(mLineColor);
        mLinePaint.setTextSize(textSize);
        // set padding
        mRectangle.set(centerX - chartRadius, centerY - chartRadius, centerX + chartRadius, centerY + chartRadius);
        // calculating initial angle
        for (int i = 0; i < mPieData.size(); i++) {
            // retrieve slice object from list
            PieSlice pieSlice = mPieData.get(i);
            // set slice color and draw pie slice
            float startAngle = pieSlice.getStartAngle();
            float sweepAngle = pieSlice.getSweepAngle();
            mSlicePaint.setColor(mColors[i % mColors.length]);
            c.drawArc(mRectangle, startAngle, sweepAngle, true, mSlicePaint);
            // calculate start point
            float startingAngle = startAngle + (sweepAngle / 2);
            float startX = ((float) (Math.cos(Math.toRadians(startingAngle)) * chartRadius)) + centerX;
            float startY = ((float) (Math.sin(Math.toRadians(startingAngle)) * chartRadius)) + centerY;
            // calculate icon position
            float iconAngle = mPieData.get(i).getIconAngle();
            float iconX = ((float) (Math.cos(Math.toRadians(iconAngle)) * iconDistance)) + centerX;
            float iconY = ((float) (Math.sin(Math.toRadians(iconAngle)) * iconDistance)) + centerY;
            // draw line
            c.drawLine(startX, startY, iconX, iconY, mLinePaint);
            // draw icon
            Drawable drawable = mPieData.get(i).getIcon();
            if (drawable != null) {
                drawable.setBounds((int) (iconX - iconRadius), (int) (iconY - iconRadius), (int) (iconX + iconRadius), (int) (iconY + iconRadius));
                drawable.draw(c);
                // c.drawBitmap(scaleIcon(bitmap, iconRadius), iconX - iconRadius, iconY - iconRadius, mSlicePaint);
            } else {
                c.drawCircle(iconX, iconY, iconRadius, mSlicePaint);
            }
            // draw percentage
            if (mPercentageEnabled) {
                // calculate center text coordinates
                float textX = ((float) (Math.cos(Math.toRadians(iconAngle)) * textDistance)) + centerX - (textSize / 4 * 3);
                float textY = ((float) (Math.sin(Math.toRadians(iconAngle)) * textDistance)) + centerY + (textSize / 2);
                int percentage = (int) (sweepAngle * 100 / 360f);
                c.drawText(String.valueOf(percentage) + "%", textX, textY, mLinePaint);
            }
        }
        // draw hole if required
        if (mHoleEnabled) {
            if (mShadowEnabled) {
                // draw blurred circle
                mSlicePaint.setColor(mShadowColor);
                c.drawCircle(centerX, centerY, chartRadius * 0.70f, mSlicePaint);
            }
            // draw white hole
            mSlicePaint.setColor(mHoleColor);
            c.drawCircle(centerX, centerY, chartRadius * 0.50f, mSlicePaint);
        }
    }
}