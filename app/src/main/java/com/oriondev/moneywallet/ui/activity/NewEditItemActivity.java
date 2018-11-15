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
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.view.MenuItem;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelScrollActivity;

/**
 * This activity is an abstract implementation that act as a base for every
 * activity that is capable to insert or modify an item of the application.
 * It simplifies the development creating a much easier api to use.
 */
public abstract class NewEditItemActivity extends SinglePanelScrollActivity {

    public static final String MODE = "NewEditItemActivity::Mode";
    public static final String ID = "NewEditItemActivity::Id";

    public enum Mode {

        /**
         * This mode indicates that a new item must be created.
         */
        NEW_ITEM,

        /**
         *  When the mode is EDIT_ITEM, the caller MUST set a valid item id or a RuntimeException
         *  is thrown.
         */
        EDIT_ITEM
    }

    private Mode mMode;
    private Long mId;

    @Override
    @CallSuper
    protected void onViewCreated(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            mMode = (Mode) intent.getSerializableExtra(MODE);
            if (mMode != null) {
                if (mMode == Mode.EDIT_ITEM) {
                    mId = intent.getLongExtra(ID, 0L);
                    if (mId <= 0L) {
                        throw new RuntimeException("Trying to edit an item with an invalid id");
                    }
                } else if (mMode == Mode.NEW_ITEM) {
                    mId = -1L;
                }
            } else {
                mMode = Mode.NEW_ITEM;
                mId = -1L;
            }
        }
    }

    @Override
    @StringRes
    protected int getActivityTitleRes() {
        return getActivityTileRes(mMode);
    }

    protected Mode getMode() {
        return mMode;
    }

    protected long getItemId() {
        return mId;
    }

    /**
     * This method is an implementation of the standard {@link SinglePanelActivity#getActivityTitleRes()}
     * to let the sub activity return the correct title according to the current mode.
     * @param mode current mode of the activity.
     * @return the string to set as title.
     */
    @StringRes
    protected abstract int getActivityTileRes(Mode mode);

    @Override
    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_new_edit_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_changes:
                onSaveChanges(mMode);
                break;
        }
        return false;
    }

    /**
     * When this method is called, the sub activity should check for data consistency
     * and correctness, then modify the data inside the content provider.
     * @param mode current mode of the activity.
     */
    protected abstract void onSaveChanges(Mode mode);
}