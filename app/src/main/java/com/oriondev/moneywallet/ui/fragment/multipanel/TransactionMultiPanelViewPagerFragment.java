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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.view.MenuItem;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.Message;
import com.oriondev.moneywallet.ui.activity.CalendarActivity;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditTransactionActivity;
import com.oriondev.moneywallet.ui.activity.NewEditTransferActivity;
import com.oriondev.moneywallet.ui.activity.SearchActivity;
import com.oriondev.moneywallet.ui.adapter.pager.TransactionViewPagerAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelViewPagerMultiItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.TransactionItemFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.TransferItemFragment;

/**
 * Created by andrea on 02/03/18.
 */
public class TransactionMultiPanelViewPagerFragment extends MultiPanelViewPagerMultiItemFragment {

    private static final String SECONDARY_FRAGMENT_TAG = "TransactionMultiPanelViewPagerFragment::Tag::SecondaryPanelFragment";

    private static final int TYPE_TRANSACTION = 0;
    private static final int TYPE_TRANSFER = 1;

    @NonNull
    @Override
    protected PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager) {
        return new TransactionViewPagerAdapter(fragmentManager, getActivity());
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_transaction;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_transaction_multipanel_fragment;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_item:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.action_open_calendar:
                startActivity(new Intent(getActivity(), CalendarActivity.class));
                break;
        }
        return false;
    }

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel(int type) {
        switch (type) {
            case TYPE_TRANSACTION:
                return new TransactionItemFragment();
            case TYPE_TRANSFER:
                return new TransferItemFragment();
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }

    @Override
    protected String getSecondaryFragmentTag(int type) {
        return SECONDARY_FRAGMENT_TAG + "::Type" + type;
    }

    @Override
    protected void onFloatingActionButtonClick() {
        Intent intent;
        switch (getViewPagerPosition()) {
            case 0:
                intent = new Intent(getActivity(), NewEditTransactionActivity.class);
                break;
            case 1:
                intent = new Intent(getActivity(), NewEditTransferActivity.class);
                break;
            default:
                return;
        }
        intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.NEW_ITEM);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IntentFilter filter = new IntentFilter(LocalAction.ACTION_ITEM_CLICK);
        LocalBroadcastManager.getInstance(context).registerReceiver(mItemClickReceiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mItemClickReceiver);
        }
    }

    private BroadcastReceiver mItemClickReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                long id = intent.getLongExtra(Message.ITEM_ID, 0L);
                switch (intent.getIntExtra(Message.ITEM_TYPE, 0)) {
                    case Message.TYPE_TRANSACTION:
                        showItemId(TYPE_TRANSACTION, id);
                        break;
                    case Message.TYPE_TRANSFER:
                        showItemId(TYPE_TRANSFER, id);
                        break;
                    default:
                        return;
                }
                showSecondaryPanel();
            }
        }

    };
}