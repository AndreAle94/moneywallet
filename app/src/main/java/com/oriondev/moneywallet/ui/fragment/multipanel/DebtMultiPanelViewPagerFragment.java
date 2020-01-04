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
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.activity.NewEditDebtActivity;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.adapter.pager.DebtViewPagerAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelViewPagerItemFragment;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.DebtItemFragment;

/**
 * Created by andrea on 02/03/18.
 */
public class DebtMultiPanelViewPagerFragment extends MultiPanelViewPagerItemFragment {

    private static final String SECONDARY_FRAGMENT_TAG = "DebtMultiPanelViewPagerFragment::Tag::SecondaryPanelFragment";

    @NonNull
    @Override
    protected PagerAdapter onCreatePagerAdapter(FragmentManager fragmentManager) {
        return new DebtViewPagerAdapter(fragmentManager, getActivity());
    }

    @Override
    protected int getTitleRes() {
        return R.string.menu_debt;
    }

    @Override
    protected void onFloatingActionButtonClick() {
        Intent intent = new Intent(getActivity(), NewEditDebtActivity.class);
        intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.NEW_ITEM);
        intent.putExtra(NewEditDebtActivity.TYPE, getCurrentDebtType());
        startActivity(intent);
    }

    private Contract.DebtType getCurrentDebtType() {
        switch (getViewPagerPosition()) {
            case 0:
                return Contract.DebtType.DEBT;
            case 1:
                return Contract.DebtType.CREDIT;
            default:
                return null;
        }
    }

    @Override
    protected SecondaryPanelFragment onCreateSecondaryPanel() {
        return new DebtItemFragment();
    }

    @Override
    protected String getSecondaryFragmentTag() {
        return SECONDARY_FRAGMENT_TAG;
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
            if (intent != null && intent.getIntExtra(Message.ITEM_TYPE, 0) == Message.TYPE_DEBT) {
                showItemId(intent.getLongExtra(Message.ITEM_ID, 0L));
                showSecondaryPanel();
            }
        }

    };
}