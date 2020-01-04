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

package com.oriondev.moneywallet.ui.view.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.cache.TypefaceCache;

/**
 * Created by andrea on 01/05/18.
 */
public class MaterialTextView extends AppCompatTextView {

    private final static int ICON_CONTAINER_SIZE_DP = 48;
    private final static int ICON_MARGIN_SIZE_DP = 8;
    private final static int BOTTOM_PADDING_DP = 8;
    private final static int BOTTOM_LINE_PADDING_DP = 8;
    private final static int SIDE_PADDING_DP = 8;

    private int mIconColor;

    private Mode mMode;
    private Drawable mLeftDrawable;
    private String mHint;
    private boolean mBottomLineEnabled;

    private int mIconMargin;
    private int mBottomLineSize;

    private Paint mFloatingLabelPaint;
    private Paint mBottomLinePaint;

    private Rect mMeter;

    public MaterialTextView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MaterialTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MaterialTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        mFloatingLabelPaint = new Paint();
        mFloatingLabelPaint.setAntiAlias(true);
        mBottomLinePaint = new Paint();
        mBottomLinePaint.setAntiAlias(true);
        mMeter = new Rect();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialTextView, defStyleAttr, 0);
        try {
            onParseAttributes(typedArray);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
        calculatePadding();
    }

    protected void onParseAttributes(TypedArray typedArray) {
        mMode = Mode.getMode(typedArray.getInt(R.styleable.MaterialTextView_mtv_mode, Mode.STANDARD.mMode));
        mIconMargin = typedArray.getDimensionPixelSize(R.styleable.MaterialTextView_mtv_leftIconMargin, getPixels(ICON_MARGIN_SIZE_DP));
        mIconColor = typedArray.getColor(R.styleable.MaterialTextView_mtv_leftIconColor, Color.GRAY);
        int leftIcon = typedArray.getResourceId(R.styleable.MaterialTextView_mtv_leftIcon, -1);
        if (leftIcon > 0) {
            mLeftDrawable = Utils.getDrawableCompat(getContext(), leftIcon);
            applyTinting(mLeftDrawable, mIconColor);
        }
        mHint = typedArray.getString(R.styleable.MaterialTextView_mtv_floatingLabelText);
        if (mHint == null) {
            CharSequence hint = getHint();
            if (hint != null) {
                mHint = hint.toString();
            }
        }
        mFloatingLabelPaint.setColor(typedArray.getColor(R.styleable.MaterialTextView_mtv_floatingLabelTextColor, Color.GRAY));
        mFloatingLabelPaint.setTextSize(typedArray.getDimension(R.styleable.MaterialTextView_mtv_floatingLabelTextSize, getContext().getResources().getDimension(R.dimen.material_component_floating_label_text_size)));
        String floatingLabelTypeface = typedArray.getString(R.styleable.MaterialTextView_mtv_floatingLabelTextTypeface);
        if (floatingLabelTypeface != null) {
            mFloatingLabelPaint.setTypeface(TypefaceCache.get(getContext(), floatingLabelTypeface));
        }
        mBottomLineEnabled = typedArray.getBoolean(R.styleable.MaterialTextView_mtv_bottomLineEnabled, false);
        mBottomLinePaint.setColor(typedArray.getColor(R.styleable.MaterialTextView_mtv_bottomLineColor, Color.GRAY));
        mBottomLineSize = typedArray.getDimensionPixelSize(R.styleable.MaterialTextView_mtv_bottomLineSize, getPixels(1));
    }

    private void calculatePadding() {
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingBottom = 0;
        int paddingRight = 0;
        // add default 8 dp padding
        paddingLeft += getPixels(SIDE_PADDING_DP);
        paddingRight += getPixels(SIDE_PADDING_DP);
        // calculate padding left / right
        if (mLeftDrawable != null) {
            int extraPadding = getPixels(ICON_CONTAINER_SIZE_DP) + mIconMargin;
            if (isRtl()) {
                paddingRight += extraPadding;
            } else {
                paddingLeft += extraPadding;
            }
        }
        // calculate bottom padding
        paddingBottom += getPixels(BOTTOM_PADDING_DP);
        // calculate top padding
        paddingTop += getPixels(16);
        if (mMode == Mode.FLOATING_LABEL && mHint != null) {
            // append floating label text size
            mFloatingLabelPaint.getTextBounds(mHint, 0, mHint.length(), mMeter);
            paddingTop += mMeter.height();
        }
        // apply padding
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    private boolean isRtl() {
        return Utils.isRtl(getResources());
    }

    private int getPixels(int dp) {
        return (int) Utils.getPixels(dp, getResources());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // measure
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (Math.min(measuredWidth, measuredHeight) == 0) {
            // skip drawing
            return;
        }
        onDrawLeftIcon(canvas);
        onDrawFloatingLabel(canvas);
        onDrawBottomLine(canvas);
    }

    protected void onDrawLeftIcon(Canvas canvas) {
        if (mLeftDrawable != null) {
            int rectTop = getPaddingTop() - getPixels(12);
            int rectLeft = isRtl() ? (getMeasuredWidth() - getPaddingRight() + mIconMargin) : getPixels(SIDE_PADDING_DP);
            int padding = (int) Utils.getPixels(12, getResources());
            int iconTop = rectTop + padding;
            int iconLeft = rectLeft + padding + getScrollX();
            int size = (int) Utils.getPixels(24, getResources());
            mLeftDrawable.setBounds(iconLeft, iconTop, iconLeft + size, iconTop + size);
            mLeftDrawable.draw(canvas);
        }
    }

    protected void onDrawFloatingLabel(Canvas canvas) {
        if (mMode == Mode.FLOATING_LABEL && mHint != null) {
            mFloatingLabelPaint.getTextBounds(mHint, 0, mHint.length(), mMeter);
            int startY = getPaddingTop() + mMeter.height();
            int endY = getPaddingTop() - getPixels(8);
            int startX = getScrollX() + (isRtl() ? (getMeasuredWidth() - getPaddingRight() - mMeter.width()) : getPaddingLeft());
            if (endY < startY) {
                canvas.drawText(mHint, startX, endY, mFloatingLabelPaint);
            }
        }
    }

    protected void onDrawBottomLine(Canvas canvas) {
        if (mBottomLineEnabled) {
            int top = getMeasuredHeight() - getPaddingBottom() + getPixels(BOTTOM_LINE_PADDING_DP) - mBottomLineSize;
            int left = getPaddingLeft() + getScrollX();
            int right = getScrollX() + getMeasuredWidth() - getPaddingRight();
            canvas.drawRect(
                    left,
                    top,
                    right,
                    top + mBottomLineSize,
                    mBottomLinePaint
            );
        }
    }

    private void applyTinting(Drawable drawable, int color) {
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setMode(Mode mode) {
        if (mMode != mode) {
            mMode = mode;
            calculatePadding();
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public void setFloatingLabelColor(int color) {
        mFloatingLabelPaint.setColor(color);
    }

    public void setBottomLineColor(int color) {
        mBottomLinePaint.setColor(color);
    }

    public void setLeftIconColor(int iconColor) {
        mIconColor = iconColor;
        if (mLeftDrawable != null) {
            applyTinting(mLeftDrawable, mIconColor);
        }
    }

    public void setLeftDrawable(@DrawableRes int drawable) {
        setLeftDrawable(Utils.getDrawableCompat(getContext(), drawable));
    }

    public void setLeftDrawable(Drawable drawable) {
        mLeftDrawable = drawable;
        applyTinting(mLeftDrawable, mIconColor);
        calculatePadding();
    }
}