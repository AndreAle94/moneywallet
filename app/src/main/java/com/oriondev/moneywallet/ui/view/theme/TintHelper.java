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

package com.oriondev.moneywallet.ui.view.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.utils.Utils;

import java.lang.reflect.Field;

/**
 * Created by andrea on 11/04/18.
 */
/*package-local*/ class TintHelper {

    private static final int COLOR_BUTTON_DISABLED_LIGHT = Color.parseColor("#1F000000");
    private static final int COLOR_BUTTON_DISABLED_DARK = Color.parseColor("#1F000000");

    private static final int COLOR_CONTROL_DISABLED_DARK = Color.parseColor("#43000000");
    private static final int COLOR_CONTROL_DISABLED_LIGHT = Color.parseColor("#4DFFFFFF");
    private static final int COLOR_CONTROL_NORMAL_DARK = Color.parseColor("#B3FFFFFF");
    private static final int COLOR_CONTROL_NORMAL_LIGHT = Color.parseColor("#8A000000");

    private static final int COLOR_SWITCH_THUMB_DISABLED_LIGHT = Color.parseColor("#FFBDBDBD");
    private static final int COLOR_SWITCH_THUMB_DISABLED_DARK = Color.parseColor("#FF424242");
    private static final int COLOR_SWITCH_THUMB_NORMAL_LIGHT = Color.parseColor("#FFFAFAFA");
    private static final int COLOR_SWITCH_THUMB_NORMAL_DARK = Color.parseColor("#FFBDBDBD");
    private static final int COLOR_SWITCH_TRACK_DISABLED_LIGHT = Color.parseColor("#1F000000");
    private static final int COLOR_SWITCH_TRACK_DISABLED_DARK = Color.parseColor("#1AFFFFFF");
    private static final int COLOR_SWITCH_TRACK_NORMAL_LIGHT = Color.parseColor("#43000000");
    private static final int COLOR_SWITCH_TRACK_NORMAL_DARK = Color.parseColor("#4DFFFFFF");

    // This returns a NEW Drawable because of the mutate() call. The mutate() call is necessary because Drawables with the same resource have shared states otherwise.
    @CheckResult
    @Nullable
    /*package-local*/ static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable == null) {
            return null;
        }
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    @CheckResult
    @Nullable
    /*package-local*/ static Drawable createTintedDrawable(@Nullable Drawable drawable, @NonNull ColorStateList colorStateList) {
        if (drawable == null) {
            return null;
        }
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, colorStateList);
        return drawable;
    }

    private static Drawable modifySwitchDrawable(@NonNull Context context, @NonNull Drawable from, @ColorInt int tint, boolean thumb, boolean useDarker) {
        if (useDarker) {
            tint = Util.shiftColor(tint, 1.1f);
        }
        tint = Util.adjustAlpha(tint, thumb ? 1.0f : 0.5f);
        int disabled;
        int normal;
        if (thumb) {
            disabled = useDarker ? COLOR_SWITCH_THUMB_DISABLED_DARK : COLOR_SWITCH_THUMB_DISABLED_LIGHT;
            normal = useDarker ? COLOR_SWITCH_THUMB_NORMAL_DARK : COLOR_SWITCH_THUMB_NORMAL_LIGHT;
        } else {
            disabled = useDarker ? COLOR_SWITCH_TRACK_DISABLED_DARK : COLOR_SWITCH_TRACK_DISABLED_LIGHT;
            normal = useDarker ? COLOR_SWITCH_TRACK_NORMAL_DARK : COLOR_SWITCH_TRACK_NORMAL_LIGHT;
        }
        ColorStateList stateList = new ColorStateList(new int[][] {
                    new int[] {-android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled, -android.R.attr.state_activated, -android.R.attr.state_checked},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_activated},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
                }, new int[] {disabled, normal, tint, tint}
        );
        return createTintedDrawable(from, stateList);
    }

    /*package-local*/ static void applyTint(Button button, @ColorInt int color, @ColorInt int rippleColor, boolean useDarker) {
        int disabled = useDarker ? COLOR_BUTTON_DISABLED_DARK : COLOR_BUTTON_DISABLED_LIGHT;
        int pressed = Util.shiftColor(color, useDarker ? 0.9f : 1.1f);
        int activated = Util.shiftColor(color, useDarker ? 1.1f : 0.9f);
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[] {-android.R.attr.state_enabled}, new int[] {android.R.attr.state_enabled}
                },
                new int[] {disabled, color}
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && button.getBackground() instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) button.getBackground();
            rippleDrawable.setColor(ColorStateList.valueOf(rippleColor));
        }
        Drawable drawable = button.getBackground();
        if (drawable != null) {
            drawable = createTintedDrawable(drawable, colorStateList);
            Utils.setBackgroundCompat(button, drawable);
        }
    }

    /*package-local*/ static void applyTint(CheckBox checkBox, @ColorInt int color, boolean useDarker) {
        Context context = checkBox.getContext();
        ColorStateList stateList = new ColorStateList(new int[][] {
                new int[] {-android.R.attr.state_enabled},
                new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
                new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
        },
                new int[] {
                        Util.stripAlpha(useDarker ? COLOR_CONTROL_DISABLED_DARK : COLOR_CONTROL_DISABLED_LIGHT),
                        useDarker ? COLOR_CONTROL_NORMAL_DARK : COLOR_CONTROL_NORMAL_LIGHT,
                        color
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkBox.setButtonTintList(stateList);
        } else {
            @SuppressLint("PrivateResource")
            int resource = R.drawable.abc_btn_radio_material;
            Drawable drawable = ContextCompat.getDrawable(context, resource);
            drawable = createTintedDrawable(drawable, stateList);
            checkBox.setButtonDrawable(drawable);
        }
    }

    /*package-local*/ static void applyTint(RadioButton radioButton, @ColorInt int color, boolean useDarker) {
        Context context = radioButton.getContext();
        ColorStateList stateList = new ColorStateList(new int[][] {
                                        new int[] {-android.R.attr.state_enabled},
                                        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
                                        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
                                    },
                                    new int[] {
                                        Util.stripAlpha(useDarker ? COLOR_CONTROL_DISABLED_DARK : COLOR_CONTROL_DISABLED_LIGHT),
                                        useDarker ? COLOR_CONTROL_NORMAL_DARK : COLOR_CONTROL_NORMAL_LIGHT,
                                        color
                                    }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(stateList);
        } else {
            @SuppressLint("PrivateResource")
            int resource = R.drawable.abc_btn_radio_material;
            Drawable drawable = ContextCompat.getDrawable(context, resource);
            drawable = createTintedDrawable(drawable, stateList);
            radioButton.setButtonDrawable(drawable);
        }
    }

    /*package-local*/ static void applyTint(SwitchCompat switchCompat, @ColorInt int color, boolean useDarker) {
        if (switchCompat.getTrackDrawable() != null) {
            switchCompat.setTrackDrawable(
                    modifySwitchDrawable(
                            switchCompat.getContext(),
                            switchCompat.getTrackDrawable(),
                            color,
                            false,
                            useDarker
                    )
            );
        }
        if (switchCompat.getThumbDrawable() != null) {
            switchCompat.setThumbDrawable(
                    modifySwitchDrawable(
                            switchCompat.getContext(),
                            switchCompat.getThumbDrawable(),
                            color,
                            true,
                            useDarker
                    )
            );
        }
    }

    /*package-local*/ static void applyTint(@NonNull SeekBar seekBar, @ColorInt int color, boolean useDarker) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[] {-android.R.attr.state_enabled},
                        new int[] {android.R.attr.state_enabled}
                },
                new int[] {
                        useDarker ? COLOR_CONTROL_DISABLED_DARK : COLOR_CONTROL_DISABLED_LIGHT,
                        color
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(colorStateList);
            seekBar.setProgressTintList(colorStateList);
        } else {
            Drawable progressDrawable = createTintedDrawable(seekBar.getProgressDrawable(), colorStateList);
            seekBar.setProgressDrawable(progressDrawable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Drawable thumbDrawable = createTintedDrawable(seekBar.getThumb(), colorStateList);
                seekBar.setThumb(thumbDrawable);
            }
        }
    }

    /*package-local*/ static void setCursorTint(@NonNull EditText editText, @ColorInt int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0] = createTintedDrawable(drawables[0], color);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = createTintedDrawable(drawables[1], color);
            fCursorDrawable.set(editor, drawables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}