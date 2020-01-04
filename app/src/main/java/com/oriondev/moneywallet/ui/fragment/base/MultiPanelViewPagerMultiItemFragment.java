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
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by andrea on 03/04/18.
 */
public abstract class MultiPanelViewPagerMultiItemFragment extends MultiPanelViewPagerFragment {

    private static final String SS_INDEX = "MultiPanelViewPagerMultiItemFragment::SavedState::CurrentIndex";

    private int mContainerId;

    private int mIndex;

    private SecondaryPanelFragment mSecondaryFragment;

    @Override
    protected void onCreateSecondaryPanel(LayoutInflater inflater, @NonNull ViewGroup secondaryPanel, @Nullable Bundle savedInstanceState) {
        mContainerId = secondaryPanel.getId();
        if (savedInstanceState != null) {
            replaceSecondaryFragment(savedInstanceState.getInt(SS_INDEX, 0));
        } else {
            replaceSecondaryFragment(0);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_INDEX, mIndex);
    }

    protected abstract SecondaryPanelFragment onCreateSecondaryPanel(int index);

    protected abstract String getSecondaryFragmentTag(int index);

    public void showItemId(int index, long id) {
        if (index != mIndex) {
            replaceSecondaryFragment(index);
        }
        mSecondaryFragment.showItemId(id);
        showSecondaryPanel();
    }

    private void replaceSecondaryFragment(int index) {
        String tag = getSecondaryFragmentTag(index);
        FragmentManager fragmentManager = getChildFragmentManager();
        mSecondaryFragment = (SecondaryPanelFragment) fragmentManager.findFragmentByTag(tag);
        if (mSecondaryFragment != null) {
            fragmentManager.beginTransaction().show(mSecondaryFragment).commitNow();
        } else {
            mSecondaryFragment = onCreateSecondaryPanel(index);
            fragmentManager.beginTransaction()
                    .replace(mContainerId, mSecondaryFragment, tag)
                    .commitNow();
        }
        mIndex = index;
    }
}