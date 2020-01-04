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
import com.oriondev.moneywallet.ui.fragment.single.PeriodDetailFlowFragment;
import com.oriondev.moneywallet.ui.fragment.single.PeriodDetailSummaryFragment;

import java.util.Date;

/**
 * Created by andrea on 09/04/18.
 */
public class PeriodViewPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    private final Date mStartDate;
    private final Date mEndDate;

    public PeriodViewPagerAdapter(FragmentManager fm, Context context, Date startDate, Date endDate) {
        super(fm);
        mContext = context;
        mStartDate = startDate;
        mEndDate = endDate;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PeriodDetailFlowFragment.newInstance(mStartDate, mEndDate, true);
            case 1:
                return PeriodDetailFlowFragment.newInstance(mStartDate, mEndDate, false);
            case 2:
                return PeriodDetailSummaryFragment.newInstance(mStartDate, mEndDate);
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.hint_incomes);
            case 1:
                return mContext.getString(R.string.hint_expenses);
            case 2:
                return mContext.getString(R.string.hint_summary);
        }
        return null;
    }
}