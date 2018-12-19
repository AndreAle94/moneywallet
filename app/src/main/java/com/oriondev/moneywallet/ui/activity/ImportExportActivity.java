package com.oriondev.moneywallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;

/**
 * Created by andrea on 19/12/18.
 */
public class ImportExportActivity extends SinglePanelActivity {

    public static final String MODE = "ImportExportActivity::Argument::Mode";

    public static final int MODE_EXPORT = 0;
    public static final int MODE_IMPORT = 1;

    private int mMode;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mMode = getActivityMode();
        // TODO prepare ui here
    }

    private int getActivityMode() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getIntExtra(MODE, MODE_EXPORT);
        }
        return MODE_EXPORT;
    }

    @Override
    protected int getActivityTitleRes() {
        switch (mMode) {
            case MODE_EXPORT:
                return R.string.title_activity_export_data;
            case MODE_IMPORT:
                return R.string.title_activity_import_data;
        }
        return 0;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        // the floating action button is not required here
        return false;
    }
}