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

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.cache.TypefaceCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 27/01/18.
 */
public class MaterialEditText extends androidx.appcompat.widget.AppCompatEditText implements ValueAnimator.AnimatorUpdateListener, View.OnTouchListener {

    private final static int ANIMATION_DURATION = 300;
    private final static int ICON_CONTAINER_SIZE_DP = 48;
    private final static int ICON_MARGIN_SIZE_DP = 8;
    private final static int NO_ERROR_BOTTOM_PADDING_DP = 16;
    private final static int BOTTOM_LINE_PADDING_DP = 8;
    private final static int SIDE_PADDING_DP = 8;
    private final static int CANCEL_BUTTON_PADDING_DP = 36;
    private final static int CANCEL_BUTTON_SIZE_DP = 24;

    private int mIconColorNormal;
    private int mIconColorFocused;
    private int mFloatingLabelColorNormal;
    private int mFloatingLabelColorFocused;
    private int mBottomLineColorNormal;
    private int mBottomLineColorFocused;
    private int mBottomLineColorError;
    private int mErrorTextColor;

    private Mode mMode;
    private Drawable mLeftDrawable[];
    private String mHint;

    private boolean mError;
    private String mErrorMessage;
    private List<Validator> mValidators;
    private ValueAnimator mAnimator;
    private boolean mLabelVisible;
    private Drawable mCancelButton;
    private boolean mShowCancelButton;

    private int mIconMargin;
    private int mDefaultBottomLineSize;
    private int mFocusedBottomLineSize;

    private CancelButtonListener mCancelButtonListener;

    private Paint mFloatingLabelPaint;
    private Paint mErrorTextPaint;
    private Paint mBottomLinePaint;

    private Rect mMeter;

    public MaterialEditText(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        // initialize paints
        mFloatingLabelPaint = new Paint();
        mFloatingLabelPaint.setAntiAlias(true);
        mErrorTextPaint = new Paint();
        mErrorTextPaint.setAntiAlias(true);
        mBottomLinePaint = new Paint();
        mBottomLinePaint.setAntiAlias(true);
        mMeter = new Rect();
        mLabelVisible = false;
        // disable background
        Utils.setBackgroundCompat(this, null);
        // parse attributes from xml
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText, defStyleAttr, 0);
        try {
            onParseAttributes(typedArray);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
        // create validator manager and calculate view padding
        mValidators = new ArrayList<>();
        setupValidators();
        calculatePadding();
        setOnTouchListener(this);
    }

    protected void onParseAttributes(TypedArray typedArray) {
        mMode = Mode.getMode(typedArray.getInt(R.styleable.MaterialEditText_met_mode, Mode.STANDARD.mMode));
        mIconMargin = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_leftIconMargin, getPixels(ICON_MARGIN_SIZE_DP));
        mIconColorNormal = typedArray.getColor(R.styleable.MaterialEditText_met_leftIconColorNormal, Color.GRAY);
        mIconColorFocused = typedArray.getColor(R.styleable.MaterialEditText_met_leftIconColorFocused, Color.BLACK);
        int leftIcon = typedArray.getResourceId(R.styleable.MaterialEditText_met_leftIcon, -1);
        if (leftIcon > 0) {
            Drawable drawable = Utils.getDrawableCompat(getContext(), leftIcon);
            mLeftDrawable = generateDrawables(drawable);
            applyTinting(mLeftDrawable);
        }
        mHint = typedArray.getString(R.styleable.MaterialEditText_met_floatingLabelText);
        if (mHint == null) {
            CharSequence hint = getHint();
            if (hint != null) {
                mHint = hint.toString();
            }
        }
        mFloatingLabelColorNormal = typedArray.getColor(R.styleable.MaterialEditText_met_floatingLabelTextColorNormal, Color.GRAY);
        mFloatingLabelColorFocused = typedArray.getColor(R.styleable.MaterialEditText_met_floatingLabelTextColorFocused, Color.BLACK);
        mFloatingLabelPaint.setTextSize(typedArray.getDimension(R.styleable.MaterialEditText_met_floatingLabelTextSize, getContext().getResources().getDimension(R.dimen.material_component_floating_label_text_size)));
        String floatingLabelTypeface = typedArray.getString(R.styleable.MaterialEditText_met_floatingLabelTextTypeface);
        if (floatingLabelTypeface != null) {
            mFloatingLabelPaint.setTypeface(TypefaceCache.get(getContext(), floatingLabelTypeface));
        }
        mBottomLineColorNormal = typedArray.getColor(R.styleable.MaterialEditText_met_bottomLineColorNormal, Color.GRAY);
        mBottomLineColorFocused = typedArray.getColor(R.styleable.MaterialEditText_met_bottomLineColorFocused, Color.BLACK);
        mBottomLineColorError = typedArray.getColor(R.styleable.MaterialEditText_met_bottomLineColorError, Color.RED);
        mDefaultBottomLineSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_bottomLineColorUnfocusedSize, getPixels(1));
        mFocusedBottomLineSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_bottomLineColorFocusedSize, getPixels(2));
        mErrorTextColor = typedArray.getColor(R.styleable.MaterialEditText_met_errorTextColor, Color.RED);
        mErrorTextPaint.setTextSize(typedArray.getDimension(R.styleable.MaterialEditText_met_errorTextSize, getContext().getResources().getDimension(R.dimen.material_component_bottom_line_error_text_size)));
        String errorTypeface = typedArray.getString(R.styleable.MaterialEditText_met_errorTextTypeface);
        if (errorTypeface != null) {
            mErrorTextPaint.setTypeface(TypefaceCache.get(getContext(), errorTypeface));
        }
        mShowCancelButton = typedArray.getBoolean(R.styleable.MaterialEditText_met_showCancelButton, false);
        int cancelButtonRes = typedArray.getResourceId(R.styleable.MaterialEditText_met_cancelButton, R.drawable.ic_clear_black_24dp);
        mCancelButton = Utils.getDrawableCompat(getContext(), cancelButtonRes);
        applyTinting(mCancelButton, mBottomLineColorNormal);
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
        // calculate cancel button padding
        if (mShowCancelButton) {
            int cancelButtonSize = getPixels(CANCEL_BUTTON_PADDING_DP);
            if (isRtl()) {
                paddingLeft += cancelButtonSize;
            } else {
                paddingRight += cancelButtonSize;
            }
        }
        // calculate bottom padding
        paddingBottom += getPixels(NO_ERROR_BOTTOM_PADDING_DP);
        if (mError) {
            // append error text size
            mErrorTextPaint.getTextBounds(mErrorMessage, 0, mErrorMessage.length(), mMeter);
            paddingBottom += mMeter.height();
        }
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
        onDrawCancelButton(canvas);
        onDrawErrorMessage(canvas);
    }

    protected void onDrawLeftIcon(Canvas canvas) {
        if (mLeftDrawable != null) {
            Drawable drawable = hasFocus() ? mLeftDrawable[1] : mLeftDrawable[0];
            int rectTop = getPaddingTop() - getPixels(8);
            int rectLeft = isRtl() ? (getMeasuredWidth() - getPaddingRight() + mIconMargin) : getPixels(SIDE_PADDING_DP);
            int padding = (int) Utils.getPixels(12, getResources());
            int iconTop = rectTop + padding;
            int iconLeft = rectLeft + padding + getScrollX();
            int size = (int) Utils.getPixels(24, getResources());
            drawable.setBounds(iconLeft, iconTop, iconLeft + size, iconTop + size);
            drawable.draw(canvas);
        }
    }

    protected void onDrawFloatingLabel(Canvas canvas) {
        if (mMode == Mode.FLOATING_LABEL && mHint != null) {
            mFloatingLabelPaint.setColor(hasFocus() ? mFloatingLabelColorFocused : mFloatingLabelColorNormal);
            mFloatingLabelPaint.getTextBounds(mHint, 0, mHint.length(), mMeter);
            int startY = getPaddingTop() + mMeter.height();
            int endY = getPaddingTop() - getPixels(8);
            int startX = getScrollX() + (isRtl() ? (getMeasuredWidth() - getPaddingRight() - mMeter.width()) : getPaddingLeft());
            int textY = startY - ((int) ((startY - endY) * getAnimatedY()));
            if (textY < startY) {
                canvas.drawText(mHint, startX, textY, mFloatingLabelPaint);
            }
        }
    }

    protected void onDrawBottomLine(Canvas canvas) {
        if (mError) {
            mBottomLinePaint.setColor(mBottomLineColorError);
        } else if (hasFocus()) {
            mBottomLinePaint.setColor(mBottomLineColorFocused);
        } else {
            mBottomLinePaint.setColor(mBottomLineColorNormal);
        }
        int lineHeight = hasFocus() ? mFocusedBottomLineSize : mDefaultBottomLineSize;
        int top = getMeasuredHeight() - getPaddingBottom() + getPixels(BOTTOM_LINE_PADDING_DP) - lineHeight;
        int left = getPaddingLeft() + getScrollX() + (mShowCancelButton && isRtl() ? getPixels(CANCEL_BUTTON_PADDING_DP) : 0);
        int right = getScrollX() + getMeasuredWidth() - getPaddingRight() + (mShowCancelButton  && !isRtl()? getPixels(CANCEL_BUTTON_PADDING_DP) : 0);
        canvas.drawRect(
                left,
                top,
                right,
                top + lineHeight,
                mBottomLinePaint
        );
    }

    protected void onDrawCancelButton(Canvas canvas) {
        if (mShowCancelButton && mCancelButton != null && getText().length() > 0) {
            int iconSize = getPixels(CANCEL_BUTTON_SIZE_DP);
            int iconPadding = getPixels(CANCEL_BUTTON_PADDING_DP);
            int internalMargin = (iconPadding - iconSize) / 2;
            int left = getScrollX() + internalMargin + (isRtl() ? (getPaddingLeft() - iconPadding) : (getMeasuredWidth() - getPaddingRight()));
            int top = getPaddingTop();
            mCancelButton.setBounds(left, top, left + iconSize, top + iconSize);
            mCancelButton.draw(canvas);
        }
    }

    protected void onDrawErrorMessage(Canvas canvas) {
        if (mError) {
            mErrorTextPaint.setColor(mErrorTextColor);
            mErrorTextPaint.getTextBounds(mErrorMessage, 0, mErrorMessage.length(), mMeter);
            int startX = isRtl() ? (getMeasuredWidth() - getPaddingRight() - mMeter.width()) : getPaddingLeft();
            int startY = getMeasuredHeight() - getPixels(4);
            canvas.drawText(mErrorMessage, startX + getScrollX(), startY, mErrorTextPaint);
        }
    }

    private void showFloatingLabel() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    private void hideFloatingLabel() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(1, 0);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    private float getAnimatedY() {
        if (mAnimator != null) {
            return (float) mAnimator.getAnimatedValue();
        }
        return 0;
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

    public void setFloatingLabelColorNormal(int floatingLabelColorNormal) {
        mFloatingLabelColorNormal = floatingLabelColorNormal;
    }

    public void setFloatingLabelColorFocused(int floatingLabelColorFocused) {
        mFloatingLabelColorFocused = floatingLabelColorFocused;
    }

    public void setBottomLineColorNormal(int bottomLineColorNormal) {
        mBottomLineColorNormal = bottomLineColorNormal;
        applyTinting(mCancelButton, bottomLineColorNormal);
    }

    public void setBottomLineColorFocused(int bottomLineColorFocused) {
        mBottomLineColorFocused = bottomLineColorFocused;
    }

    public void setBottomLineColorError(int bottomLineColorError) {
        mBottomLineColorError = bottomLineColorError;
    }

    public void setLeftIconColorNormal(int iconColorNormal) {
        mIconColorNormal = iconColorNormal;
        if (mLeftDrawable != null) {
            mLeftDrawable[0].setColorFilter(mIconColorNormal, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setLeftIconColorFocused(int iconColorFocused) {
        mIconColorFocused = iconColorFocused;
        if (mLeftDrawable != null) {
            mLeftDrawable[1].setColorFilter(mIconColorFocused, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setLeftDrawable(@DrawableRes int drawable) {
        setLeftDrawable(Utils.getDrawableCompat(getContext(), drawable));
    }

    public void setLeftDrawable(Drawable drawable) {
        mLeftDrawable = generateDrawables(drawable);
        applyTinting(mLeftDrawable);
        calculatePadding();
    }

    public void setTextViewMode(boolean textViewMode) {
        setFocusable(!textViewMode);
        setFocusableInTouchMode(!textViewMode);
        setLongClickable(!textViewMode);
    }

    public String getTextAsString() {
        return getText().toString();
    }

    public void setOnCancelButtonClickListener(CancelButtonListener cancelButtonClickListener) {
        mCancelButtonListener = cancelButtonClickListener;
    }

    private Drawable[] generateDrawables(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable[] drawables = new Drawable[2];
        drawables[0] = drawable;
        drawables[1] = drawable.getConstantState().newDrawable();
        return drawables;
    }

    private void applyTinting(Drawable[] drawables) {
        if (drawables != null) {
            drawables[0].setColorFilter(mIconColorNormal, PorterDuff.Mode.SRC_ATOP);
            drawables[1].setColorFilter(mIconColorFocused, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void applyTinting(Drawable drawable, int color) {
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void setupValidators() {
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(s, true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = s.length() == 0;
                if (mLabelVisible && isEmpty) {
                    mLabelVisible = false;
                    hideFloatingLabel();
                } else if (!mLabelVisible && !isEmpty) {
                    mLabelVisible = true;
                    showFloatingLabel();
                }
            }

        });
    }

    public void showError(@StringRes int errorMessage) {
        setError(getContext().getString(errorMessage));
    }

    public void showError(String errorMessage) {
        mError = true;
        mErrorMessage = errorMessage;
        invalidate();
    }

    public void addValidator(Validator validator) {
        mValidators.add(validator);
    }

    public void removeValidator(Validator validator) {
        mValidators.remove(validator);
    }

    public void removeAllValidators() {
        mValidators.clear();
    }

    public int getValidatorCount() {
        return mValidators.size();
    }

    public boolean validate() {
        validate(getText(), false);
        return !mError;
    }

    protected void validate(CharSequence charSequence, boolean onlyAutoValidator) {
        boolean shouldInvalidate = false;
        if (mError) {
            mError = false;
            mErrorMessage = null;
            shouldInvalidate = true;
        }
        for (Validator validator : mValidators) {
            if (!onlyAutoValidator || validator.autoValidate()) {
                mError = !validator.isValid(charSequence);
                if (mError) {
                    mErrorMessage = validator.getErrorMessage();
                    shouldInvalidate = true;
                    break;
                }
            }
        }
        if (shouldInvalidate) {
            calculatePadding();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mShowCancelButton && isClearButtonAction(event)) {
                boolean actionConsumed = false;
                if (mCancelButtonListener != null) {
                    actionConsumed = mCancelButtonListener.onCancelButtonClick(MaterialEditText.this);
                }
                if (!actionConsumed) {
                    setText("");
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isClearButtonAction(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int iconPadding = getPixels(CANCEL_BUTTON_PADDING_DP);
        int left = isRtl() ? (getPaddingLeft() - iconPadding) : (getMeasuredWidth() - getPaddingRight());
        int right = left + iconPadding;
        int top = getPaddingTop();
        int bottom = top + iconPadding;
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public interface CancelButtonListener {

        boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText);
    }
}