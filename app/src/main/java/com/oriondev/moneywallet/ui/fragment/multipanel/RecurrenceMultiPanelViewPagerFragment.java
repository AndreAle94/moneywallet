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
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.Message;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditRecurrentTransactionActivity;
import com.oriondev.moneywallet.ui.activity.NewEditRecurrentTransferActivity;
import com.oriondev.moneywallet.ui.adapter.pager.RecurrenceViewPagerAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelViewPagerMultiItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.RecurrentTransactionItemFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.RecurrentTransferItemFragment;

/**
 * Created by andrea on 04/11/18.
 */
public class RecurrenceMultiPanelViewPagerFragment extends MultiPanelViewPagerMultiItemFragment {

    private static final String SECONDARY_FRAGMENT_TAG = "RecurrenceMultiPanelViewPagerFragment::Tag::SecondaryPanelFragment";

    private static final int TYPE_TRANSACTION = 0;
    private static final int TYPE_TRANSFER = 1;

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel(int index) {
        switch (index) {
            case TYPE_TRANSACTION:
                return new RecurrentTransactionItemFragment();
            case TYPE_TRANSFER:
                return new RecurrentTransferItemFragment();
            default:
                throw new IllegalArgumentException("Unknown item type: " + index);
        }
    }

    @Override
    protected String getSecondaryFragmentTag(int index) {
        return SECONDARY_FRAGMENT_TAG + "::Type" + String.valueOf(index);
    }

    @Override
    protected void onFloatingActionButtonClick() {
        Intent intent;
        switch (getViewPagerPosition()) {
            case 0:
                intent = new Intent(getActivity(), NewEditRecurrentTransactionActivity.class);
                break;
            case 1:
                intent = new Intent(getActivity(), NewEditRecurrentTransferActivity.class);
                break;
            default:
                return;
        }
        intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.NEW_ITEM);
        startActivity(intent);
    }

    @NonNull
    @Override
    protected PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager) {
        return new RecurrenceViewPagerAdapter(fragmentManager, getActivity());
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_recurrences;
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
                    case Message.TYPE_RECURRENT_TRANSACTION:
                        showItemId(TYPE_TRANSACTION, id);
                        break;
                    case Message.TYPE_RECURRENT_TRANSFER:
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