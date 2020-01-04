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

package com.oriondev.moneywallet.ui.activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelViewPagerActivity;
import com.oriondev.moneywallet.ui.adapter.pager.PeriodViewPagerAdapter;
import com.oriondev.moneywallet.utils.DateFormatter;

import java.util.Date;

/**
 * Created by andrea on 04/04/18.
 */
public class PeriodDetailActivity extends SinglePanelViewPagerActivity {

    public static final String START_DATE = "PeriodDetailActivity::Arguments::StartDate";
    public static final String END_DATE = "PeriodDetailActivity::Arguments::EndDate";

    @NonNull
    @Override
    protected PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager) {
        Intent intent = getIntent();
        if (intent != null) {
            Date startDate = (Date) intent.getSerializableExtra(START_DATE);
            Date endDate = (Date) intent.getSerializableExtra(END_DATE);
            if (startDate != null && endDate != null) {
                setToolbarSubtitle(DateFormatter.getDateRange(this, startDate, endDate));
            }
            return new PeriodViewPagerAdapter(fragmentManager, this, startDate, endDate);
        } else {
            return new PeriodViewPagerAdapter(fragmentManager, this, null, null);
        }
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.menu_overview;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }
}