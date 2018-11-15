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

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelSimpleListActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.CurrencyCursorAdapter;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andrea on 03/02/18.
 */
public class CurrencyListActivity extends SinglePanelSimpleListActivity implements CurrencyCursorAdapter.CurrencyActionListener {

    public static final String RESULT_CURRENCY = "CurrencyListActivity::Result::SelectedCurrency";

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyText(R.string.message_no_currency_found);
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new CurrencyCursorAdapter(this);
    }

    @Override
    @StringRes
    protected int getActivityTitleRes() {
        return R.string.title_activity_currency_list;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = DataContentProvider.CONTENT_CURRENCIES;
        String[] projection = new String[] {
                Contract.Currency.ISO,
                Contract.Currency.NAME,
                Contract.Currency.SYMBOL,
                Contract.Currency.DECIMALS,
                Contract.Currency.FAVOURITE
        };
        String sortBy = Contract.Currency.NAME;
        return new CursorLoader(this, uri, projection, null, null, sortBy);
    }

    @Override
    public void onCurrencyClick(String iso) {
        CurrencyUnit currency = CurrencyManager.getCurrency(iso);
        Intent intent = new Intent();
        intent.putExtra(RESULT_CURRENCY, currency);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCurrencyFavourite(String iso, boolean newValue) {
        // TODO handle it properly: if we update the database directly than the uri will be notify
        // TODO to be changed and the content provider will re-query the database. At the end the
        // TODO adapter will be refreshed and the new item will be at the top of the list.
        // TODO we need a wrapper to keep the data static until a refresh occurs.
    }
}