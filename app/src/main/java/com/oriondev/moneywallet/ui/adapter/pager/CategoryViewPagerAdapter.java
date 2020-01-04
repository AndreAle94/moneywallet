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

package com.oriondev.moneywallet.ui.adapter.pager;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.fragment.primary.CategoryListFragment;

/**
 * Created by andrea on 10/02/18.
 */
public class CategoryViewPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    private final boolean mShowSubCategories;
    private final boolean mShowSystemCategories;

    public CategoryViewPagerAdapter(FragmentManager fm, Context context, boolean showSubCategories, boolean showSystemCategories) {
        super(fm);
        mContext = context;
        mShowSubCategories = showSubCategories;
        mShowSystemCategories = showSystemCategories;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CategoryListFragment.newInstance(Contract.CategoryType.INCOME, mShowSubCategories);
            case 1:
                return CategoryListFragment.newInstance(Contract.CategoryType.EXPENSE, mShowSubCategories);
            case 2:
                return CategoryListFragment.newInstance(Contract.CategoryType.SYSTEM, mShowSubCategories);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mShowSystemCategories ? 3 : 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.menu_category_tab_income);
            case 1:
                return mContext.getString(R.string.menu_category_tab_expense);
            case 2:
                return mContext.getString(R.string.menu_category_tab_system);
        }
        return null;
    }
}