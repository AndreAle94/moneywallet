package com.oriondev.moneywallet.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.oriondev.moneywallet.ui.activity.base.SinglePanelScrollActivity;

/**
 * Created by andrea on 05/01/19.
 */

public class NewEditCurrencyActivity extends SinglePanelScrollActivity {

    public static final String MODE = "NewEditCurrencyActivity::Mode";
    public static final String ISO = "NewEditCurrencyActivity::Iso";

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

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

    }

    @Override
    protected int getActivityTitleRes() {
        return 0;
    }
}
