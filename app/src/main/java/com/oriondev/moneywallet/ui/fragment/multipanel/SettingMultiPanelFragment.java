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

package com.oriondev.moneywallet.ui.fragment.multipanel;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.AboutActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.SettingCategoryAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.DatabaseSettingFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.UserInterfaceSettingFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.UtilitySettingFragment;

/**
 * Created by andrea on 03/03/18.
 */
public class SettingMultiPanelFragment extends MultiPanelFragment implements SettingCategoryAdapter.ActionListener {

    private static final String SS_CURRENT_ID = "SettingMultiPanelFragment::SavedState::CurrentId";

    private static final int ID_USER_INTERFACE = 0;
    private static final int ID_UTILITY = 1;
    private static final int ID_DATABASE = 2;
    private static final int ID_ABOUT = 3;

    private static final int[] FRAGMENT_TITLES = new int[] {
            R.string.setting_title_user_interface,
            R.string.setting_title_utility,
            R.string.setting_title_database
    };

    private Toolbar mSecondaryToolbar;

    private int mCurrentId;

    @Override
    protected void onCreatePrimaryPanel(LayoutInflater inflater, @NonNull ViewGroup primaryPanel, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_setting_primary_panel, primaryPanel, true);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        SettingCategoryAdapter adapter = new SettingCategoryAdapter(this);
        adapter.addCategory(ID_USER_INTERFACE, R.drawable.ic_color_lens_black_24dp, R.string.setting_title_user_interface, R.string.setting_subtitle_user_interface);
        adapter.addCategory(ID_UTILITY, R.drawable.ic_vpn_key_black_24dp, R.string.setting_title_utility, R.string.setting_subtitle_utility);
        adapter.addCategory(ID_DATABASE, R.drawable.ic_storage_black_24dp, R.string.setting_title_database, R.string.setting_subtitle_database);
        adapter.addCategory(ID_ABOUT, R.drawable.ic_info_outline_24dp, R.string.setting_title_about, R.string.setting_subtitle_about);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_setting_secondary_panel, secondaryPanel, true);
        mSecondaryToolbar = view.findViewById(R.id.secondary_toolbar);
        if (!isExtendedLayout()) {
            mSecondaryToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            mSecondaryToolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    hideSecondaryPanel();
                }

            });
        }
        if (savedInstanceState != null) {
            mCurrentId = savedInstanceState.getInt(SS_CURRENT_ID, ID_USER_INTERFACE);
        }
        loadSecondaryFragment(mCurrentId);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_CURRENT_ID, mCurrentId);
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_setting;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public void onSettingCategoryClick(int id) {
        if (id < FRAGMENT_TITLES.length) {
            loadSecondaryFragment(id);
            showSecondaryPanel();
        } else {
            startActivity(new Intent(getActivity(), AboutActivity.class));
        }
    }

    private void loadSecondaryFragment(int identifier) {
        FragmentManager fragmentManager = getChildFragmentManager();
        String fragmentTag = getSecondaryFragmentTag(identifier);
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment == null) {
            fragment = onCreateFragment(identifier);
            fragmentManager.beginTransaction()
                    .replace(R.id.secondary_panel_fragment_container, fragment, fragmentTag)
                    .commitNow();
        } else {
            fragmentManager.beginTransaction().show(fragment).commit();
        }
        mSecondaryToolbar.setTitle(FRAGMENT_TITLES[identifier]);
        mCurrentId = identifier;
    }

    private String getSecondaryFragmentTag(int identifier) {
        return "SettingMultiPanelFragment::Tag::SecondaryFragment" + identifier;
    }

    private Fragment onCreateFragment(int identifier) {
        switch (identifier) {
            case ID_USER_INTERFACE:
                return new UserInterfaceSettingFragment();
            case ID_UTILITY:
                return new UtilitySettingFragment();
            case ID_DATABASE:
                return new DatabaseSettingFragment();
            default:
                throw new IllegalArgumentException("Invalid fragment id: " + identifier);
        }
    }
}