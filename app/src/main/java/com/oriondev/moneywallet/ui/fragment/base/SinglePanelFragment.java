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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.ToolbarController;
import com.oriondev.moneywallet.utils.Utils;

/**
 * Created by andrea on 17/08/18.
 */
public abstract class SinglePanelFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    private Toolbar mToolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_single_panel, container, false);
        mToolbar = view.findViewById(R.id.primary_toolbar);
        ViewGroup parent = Utils.findViewGroupByIds(view,
                R.id.primary_panel_container_frame_layout,
                R.id.primary_panel_container_card_view,
                R.id.primary_panel_container_linear_layout,
                R.id.primary_panel_container_coordinator_layout
        );
        onCreatePanelView(inflater, parent, savedInstanceState);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        onSetupFloatingActionButton(floatingActionButton);
        setupPrimaryToolbar(mToolbar);
        return view;
    }

    protected abstract void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState);

    @StringRes
    protected abstract int getTitleRes();

    protected void onSetupFloatingActionButton(FloatingActionButton floatingActionButton) {
        if (floatingActionButton != null) {
            if (isFloatingActionButtonEnabled()) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        onFloatingActionButtonClick();
                    }

                });
            } else {
                floatingActionButton.setVisibility(View.GONE);
            }
        }
    }

    protected void setupPrimaryToolbar(Toolbar toolbar) {
        // setup toolbar title and menu (if provided)
        toolbar.setTitle(getTitleRes());
        int menuResId = onInflateMenu();
        if (menuResId > 0) {
            toolbar.inflateMenu(menuResId);
            toolbar.setOnMenuItemClickListener(this);
            onMenuCreated(toolbar.getMenu());
        }
        // attach toolbar to the activity
        Activity activity = getActivity();
        if (activity instanceof ToolbarController) {
            ((ToolbarController) activity).setToolbar(toolbar);
        }
    }

    protected void onMenuCreated(Menu menu) {

    }

    protected void setToolbarSubtitle(String subtitle) {
        if (mToolbar != null) {
            mToolbar.setSubtitle(subtitle);
        }
    }

    @MenuRes
    protected int onInflateMenu() {
        return 0;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    protected boolean isFloatingActionButtonEnabled() {
        return true;
    }

    protected void onFloatingActionButtonClick() {
        // override this method if you have to handle the floating action button click event
    }
}