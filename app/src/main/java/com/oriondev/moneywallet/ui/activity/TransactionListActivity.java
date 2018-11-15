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

import com.oriondev.moneywallet.ui.activity.base.MultiPanelActivity;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelFragment;
import com.oriondev.moneywallet.ui.fragment.multipanel.TransactionMultiPanelFragment;

import java.util.Date;

/**
 * Created by andrea on 04/04/18.
 */
public class TransactionListActivity extends MultiPanelActivity {

    private static final String MULTI_PANEL_FRAGMENT_TAG = "TransactionListActivity::Tag::MultiPanelFragment";

    public static final String START_DATE = "TransactionListActivity::Arguments::StartDate";
    public static final String END_DATE = "TransactionListActivity::Arguments::EndDate";

    public static final String CATEGORY_ID = "TransactionListActivity::Arguments::CategoryId";
    public static final String DEBT_ID = "TransactionListActivity::Arguments::DebtId";
    public static final String BUDGET_ID = "TransactionListActivity::Arguments::BudgetId";
    public static final String SAVING_ID = "TransactionListActivity::Arguments::SavingId";
    public static final String EVENT_ID = "TransactionListActivity::Arguments::EventId";
    public static final String PLACE_ID = "TransactionListActivity::Arguments::PlaceId";
    public static final String PERSON_ID = "TransactionListActivity::Arguments::PersonId";

    @Override
    protected MultiPanelFragment onCreateMultiPanelFragment() {
        Intent intent = getIntent();
        if (intent != null) {
            TransactionMultiPanelFragment.FilterType type = null;
            long itemId = 0;
            if (intent.hasExtra(CATEGORY_ID)) {
                type = TransactionMultiPanelFragment.FilterType.CATEGORY;
                itemId = intent.getLongExtra(CATEGORY_ID, 0L);
            } else if (intent.hasExtra(DEBT_ID)) {
                type = TransactionMultiPanelFragment.FilterType.DEBT;
                itemId = intent.getLongExtra(DEBT_ID, 0L);
            } else if (intent.hasExtra(BUDGET_ID)) {
                type = TransactionMultiPanelFragment.FilterType.BUDGET;
                itemId = intent.getLongExtra(BUDGET_ID, 0L);
            } else if (intent.hasExtra(SAVING_ID)) {
                type = TransactionMultiPanelFragment.FilterType.SAVING;
                itemId = intent.getLongExtra(SAVING_ID, 0L);
            } else if (intent.hasExtra(EVENT_ID)) {
                type = TransactionMultiPanelFragment.FilterType.EVENT;
                itemId = intent.getLongExtra(EVENT_ID, 0L);
            } else if (intent.hasExtra(PLACE_ID)) {
                type = TransactionMultiPanelFragment.FilterType.PLACE;
                itemId = intent.getLongExtra(PLACE_ID, 0L);
            } else if (intent.hasExtra(PERSON_ID)) {
                type = TransactionMultiPanelFragment.FilterType.PERSON;
                itemId = intent.getLongExtra(PERSON_ID, 0L);
            }
            return TransactionMultiPanelFragment.newInstance(
                    type,
                    itemId,
                    (Date) intent.getSerializableExtra(START_DATE),
                    (Date) intent.getSerializableExtra(END_DATE)
                    );
        }
        return null;
    }

    @Override
    protected String getMultiPanelFragmentTag() {
        return MULTI_PANEL_FRAGMENT_TAG;
    }
}