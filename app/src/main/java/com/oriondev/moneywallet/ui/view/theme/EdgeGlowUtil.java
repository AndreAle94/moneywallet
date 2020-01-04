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

import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.EdgeEffectCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.oriondev.moneywallet.BuildConfig;

import java.lang.reflect.Field;

/**
 * This class has been imported as-is from the Aesthetic open source project
 * created by: @author Aidan Follestad (afollestad).
 * You can find it here: 'https://github.com/afollestad/aesthetic'
 */
final class EdgeGlowUtil {

    private static Field EDGE_GLOW_FIELD_EDGE;
    private static Field EDGE_GLOW_FIELD_GLOW;
    private static Field EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT;
    private static Field SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT;
    private static Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Field LIST_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT;
    private static Field RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM;
    private static Field VIEW_PAGER_FIELD_EDGE_GLOW_LEFT;
    private static Field VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT;

    private static void invalidateEdgeEffectFields() {
        if (EDGE_GLOW_FIELD_EDGE != null
                && EDGE_GLOW_FIELD_GLOW != null
                && EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT != null) {
            EDGE_GLOW_FIELD_EDGE.setAccessible(true);
            EDGE_GLOW_FIELD_GLOW.setAccessible(true);
            EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT.setAccessible(true);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Field edge = null;
            Field glow = null;
            for (Field f : EdgeEffect.class.getDeclaredFields()) {
                switch (f.getName()) {
                    case "mEdge":
                        f.setAccessible(true);
                        edge = f;
                        break;
                    case "mGlow":
                        f.setAccessible(true);
                        glow = f;
                        break;
                }
            }
            EDGE_GLOW_FIELD_EDGE = edge;
            EDGE_GLOW_FIELD_GLOW = glow;
        } else {
            EDGE_GLOW_FIELD_EDGE = null;
            EDGE_GLOW_FIELD_GLOW = null;
        }

        Field efc = null;
        try {
            efc = EdgeEffectCompat.class.getDeclaredField("mEdgeEffect");
        } catch (NoSuchFieldException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
        EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT = efc;
    }

    private static void invalidateScrollViewFields() {
        if (SCROLL_VIEW_FIELD_EDGE_GLOW_TOP != null && SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM != null) {
            SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.setAccessible(true);
            SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.setAccessible(true);
            return;
        }
        final Class<?> cls = ScrollView.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = f;
                    break;
            }
        }
    }

    private static void invalidateHorizontalScrollViewFields() {
        if (SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT != null && SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT != null) {
            SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT.setAccessible(true);
            SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT.setAccessible(true);
            return;
        }
        final Class<?> cls = ScrollView.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowLeft":
                    f.setAccessible(true);
                    SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT = f;
                    break;
                case "mEdgeGlowRight":
                    f.setAccessible(true);
                    SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT = f;
                    break;
            }
        }
    }

    private static void invalidateNestedScrollViewFields() {
        if (NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP != null
                && NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM != null) {
            NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.setAccessible(true);
            NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.setAccessible(true);
            return;
        }
        Class cls = NestedScrollView.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM = f;
                    break;
            }
        }
    }

    private static void invalidateListViewFields() {
        if (LIST_VIEW_FIELD_EDGE_GLOW_TOP != null && LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM != null) {
            LIST_VIEW_FIELD_EDGE_GLOW_TOP.setAccessible(true);
            LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM.setAccessible(true);
            return;
        }
        final Class<?> cls = AbsListView.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mEdgeGlowTop":
                    f.setAccessible(true);
                    LIST_VIEW_FIELD_EDGE_GLOW_TOP = f;
                    break;
                case "mEdgeGlowBottom":
                    f.setAccessible(true);
                    LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM = f;
                    break;
            }
        }
    }

    private static void invalidateRecyclerViewFields() {
        if (RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP != null
                && RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT != null
                && RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT != null
                && RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM != null) {
            RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP.setAccessible(true);
            RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT.setAccessible(true);
            RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT.setAccessible(true);
            RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM.setAccessible(true);
            return;
        }
        Class cls = RecyclerView.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mTopGlow":
                    f.setAccessible(true);
                    RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP = f;
                    break;
                case "mBottomGlow":
                    f.setAccessible(true);
                    RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM = f;
                    break;
                case "mLeftGlow":
                    f.setAccessible(true);
                    RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT = f;
                    break;
                case "mRightGlow":
                    f.setAccessible(true);
                    RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT = f;
                    break;
            }
        }
    }

    private static void invalidateViewPagerFields() {
        if (VIEW_PAGER_FIELD_EDGE_GLOW_LEFT != null && VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT != null) {
            VIEW_PAGER_FIELD_EDGE_GLOW_LEFT.setAccessible(true);
            VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT.setAccessible(true);
            return;
        }
        Class cls = ViewPager.class;
        for (Field f : cls.getDeclaredFields()) {
            switch (f.getName()) {
                case "mLeftEdge":
                    f.setAccessible(true);
                    VIEW_PAGER_FIELD_EDGE_GLOW_LEFT = f;
                    break;
                case "mRightEdge":
                    f.setAccessible(true);
                    VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT = f;
                    break;
            }
        }
    }

    // Setter methods

    /*package-local*/ static void setEdgeGlowColor(@NonNull ScrollView scrollView, @ColorInt int color) {
        invalidateScrollViewFields();
        try {
            Object ee;
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEffectColor(ee, color);
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    /*package-local*/ static void setEdgeGlowColor(@NonNull HorizontalScrollView scrollView, @ColorInt int color) {
        invalidateHorizontalScrollViewFields();
        try {
            Object ee;
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_LEFT.get(scrollView);
            setEffectColor(ee, color);
            ee = SCROLL_VIEW_FIELD_EDGE_GLOW_RIGHT.get(scrollView);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    /*package-local*/ static void setEdgeGlowColor(@NonNull NestedScrollView scrollView, @ColorInt int color) {
        invalidateNestedScrollViewFields();
        try {
            Object ee = NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEffectColor(ee, color);
            ee = NESTED_SCROLL_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    /*package-local*/ static void setEdgeGlowColor(@NonNull AbsListView listView, @ColorInt int color) {
        invalidateListViewFields();
        try {
            Object ee = LIST_VIEW_FIELD_EDGE_GLOW_TOP.get(listView);
            setEffectColor(ee, color);
            ee = LIST_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(listView);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    /*package-local*/ static void setEdgeGlowColor(
            @NonNull RecyclerView scrollView,
            final @ColorInt int color,
            @Nullable RecyclerView.OnScrollListener scrollListener) {
        invalidateRecyclerViewFields();
        invalidateRecyclerViewFields();
        if (scrollListener == null) {
            scrollListener =
                    new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            EdgeGlowUtil.setEdgeGlowColor(recyclerView, color, this);
                        }
                    };
            scrollView.addOnScrollListener(scrollListener);
        }
        try {
            Object ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_TOP.get(scrollView);
            setEffectColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_BOTTOM.get(scrollView);
            setEffectColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_LEFT.get(scrollView);
            setEffectColor(ee, color);
            ee = RECYCLER_VIEW_FIELD_EDGE_GLOW_RIGHT.get(scrollView);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    /*package-local*/ static void setEdgeGlowColor(@NonNull ViewPager pager, @ColorInt int color) {
        invalidateViewPagerFields();
        try {
            Object ee = VIEW_PAGER_FIELD_EDGE_GLOW_LEFT.get(pager);
            setEffectColor(ee, color);
            ee = VIEW_PAGER_FIELD_EDGE_GLOW_RIGHT.get(pager);
            setEffectColor(ee, color);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) ex.printStackTrace();
        }
    }

    // Utilities

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setEffectColor(Object edgeEffect, @ColorInt int color) {
        invalidateEdgeEffectFields();
        if (edgeEffect instanceof EdgeEffectCompat) {
            // EdgeEffectCompat
            try {
                EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT.setAccessible(true);
                edgeEffect = EDGE_EFFECT_COMPAT_FIELD_EDGE_EFFECT.get(edgeEffect);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }
        if (edgeEffect == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // EdgeGlow
            try {
                EDGE_GLOW_FIELD_EDGE.setAccessible(true);
                final Drawable mEdge = (Drawable) EDGE_GLOW_FIELD_EDGE.get(edgeEffect);
                EDGE_GLOW_FIELD_GLOW.setAccessible(true);
                final Drawable mGlow = (Drawable) EDGE_GLOW_FIELD_GLOW.get(edgeEffect);
                mEdge.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                mGlow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                mEdge.setCallback(null); // free up any references
                mGlow.setCallback(null); // free up any references
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            // EdgeEffect
            ((EdgeEffect) edgeEffect).setColor(color);
        }
    }
}
