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

package com.oriondev.moneywallet.ui.fragment.base;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;

/**
 * Created by andrea on 09/02/18.
 */
public abstract class MultiPanelAppBarFragment extends MultiPanelFragment {

    private ViewGroup mAppBarContainer;
    private ViewGroup mLeftAppBarContainer;

    @Override
    protected View onInflateRootLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_panel_appbar, container, false);
    }

    protected void onSetupRootLayout(View view) {
        super.onSetupRootLayout(view);
        mAppBarContainer = view.findViewById(R.id.primary_app_bar_container);
        mLeftAppBarContainer = view.findViewById(R.id.left_primary_app_bar_container);
    }

    protected void onConfigureRootLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup appBarContainer = mLeftAppBarContainer != null ? mLeftAppBarContainer : mAppBarContainer;
        onCreatePrimaryAppBar(inflater, appBarContainer, savedInstanceState);
        super.onConfigureRootLayout(inflater, container, savedInstanceState);
    }

    protected abstract void onCreatePrimaryAppBar(LayoutInflater inflater, @NonNull ViewGroup primaryAppBar, @Nullable Bundle savedInstanceState);

    protected abstract void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState);

    protected abstract void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState);
}