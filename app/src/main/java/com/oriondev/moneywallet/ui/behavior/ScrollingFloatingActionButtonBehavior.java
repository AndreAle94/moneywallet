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

package com.oriondev.moneywallet.ui.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import com.oriondev.moneywallet.R;

import java.util.List;

/**
 * This class implements a custom behavior of the Floating Action Button when attached to a
 * CoordinatorLayout: it allows to show and hide the fab when a nested view is scrolled.
 */
public class ScrollingFloatingActionButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private int mToolbarHeight;

    public ScrollingFloatingActionButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        mToolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return dependency instanceof AppBarLayout || dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = fab.getHeight() + fabBottomMargin;
            float ratio = dependency.getY() / (float) mToolbarHeight;
            fab.setTranslationY(distanceToScroll * ratio * -1);
        } else if (dependency instanceof Snackbar.SnackbarLayout) {
            float translationY = getFabTranslationYForSnackBar(parent, fab);
            float percentComplete = -translationY / dependency.getHeight();
            float scaleFactor = 1 - percentComplete;
            fab.setScaleX(scaleFactor);
            fab.setScaleY(scaleFactor);
        }
        return true;
    }

    private float getFabTranslationYForSnackBar(CoordinatorLayout parent, FloatingActionButton fab) {
        float minOffset = 0;
        final List<View> dependencies = parent.getDependencies(fab);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            final View view = dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset, view.getTranslationY() - view.getHeight());
            }
        }
        return minOffset;
    }
}