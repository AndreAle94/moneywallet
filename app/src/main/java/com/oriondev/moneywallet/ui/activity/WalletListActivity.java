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

import com.oriondev.moneywallet.ui.activity.base.MultiPanelActivity;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelFragment;
import com.oriondev.moneywallet.ui.fragment.multipanel.WalletMultiPanelFragment;

/**
 * Created by andrea on 17/01/18.
 */
public class WalletListActivity extends MultiPanelActivity {

    private static final String TAG_FRAGMENT_WALLET_LIST = "WalletListActivity::WalletMultiPanelFragment";

    @Override
    protected MultiPanelFragment onCreateMultiPanelFragment() {
        return new WalletMultiPanelFragment();
    }

    @Override
    protected String getMultiPanelFragmentTag() {
        return TAG_FRAGMENT_WALLET_LIST;
    }
}