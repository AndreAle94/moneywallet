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

package com.oriondev.moneywallet.ui.fragment.stub;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelFragment;

/**
 * This class is a stub implementation of a simple multi panel fragment and it is useful until
 * a fragment for each section is not ready
 */
public class StubMultiPanelFragment extends MultiPanelFragment {

    @Override
    protected void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(primaryPanel.getContext());
        textView.setText("Primary panel");
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showSecondaryPanel();
            }

        });
        textView.setBackgroundColor(Color.RED);

        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        primaryPanel.addView(textView, params);
    }

    @Override
    protected void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(secondaryPanel.getContext());
        textView.setText("Secondary panel");
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                hideSecondaryPanel();
            }

        });
        secondaryPanel.addView(textView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        );
    }

    @Override
    protected int getTitleRes() {
        return R.string.app_name;
    }
}