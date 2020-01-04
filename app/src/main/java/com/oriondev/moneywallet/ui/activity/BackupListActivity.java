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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.service.BackupHandlerIntentService;
import com.oriondev.moneywallet.ui.activity.base.BaseActivity;
import com.oriondev.moneywallet.ui.fragment.base.NavigableFragment;
import com.oriondev.moneywallet.ui.fragment.dialog.GenericProgressDialog;
import com.oriondev.moneywallet.ui.fragment.multipanel.BackupMultiPanelFragment;
import com.oriondev.moneywallet.ui.fragment.secondary.BackupHandlerFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 21/03/2018.
 */
public class BackupListActivity extends BaseActivity implements ToolbarController {

    public static final String BACKUP_MODE = "BackupListActivity::BackupMode";

    public static final int FULL = 0;
    public static final int RESTORE_ONLY = 1;

    private static final String TAG_FRAGMENT_BACKUP = "BackupListActivity::tag::BackupMultiPanelFragment";
    private static final String TAG_PROGRESS_DIALOG = "BackupListActivity::tag::GenericProgressDialog";

    private Fragment mFragment;
    private GenericProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_container);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_BACKUP);
        mProgressDialog = (GenericProgressDialog) fragmentManager.findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (mFragment != null) {
            fragmentManager.beginTransaction().show(mFragment).commit();
        } else {
            Intent intent = getIntent();
            int mode = intent.getIntExtra(BACKUP_MODE, FULL);
            mFragment = BackupMultiPanelFragment.newInstance(mode == FULL, true);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, mFragment, TAG_FRAGMENT_BACKUP)
                    .commit();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_STARTED);
        intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_RUNNING);
        intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if (mFragment instanceof NavigableFragment) {
            if (!((NavigableFragment) mFragment).navigateBack()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void setToolbar(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }

        });
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                String callerId = intent.getStringExtra(BackupHandlerIntentService.CALLER_ID);
                if (!BackupHandlerFragment.BACKUP_SERVICE_CALLER_ID.equals(callerId)) {
                    // the service has sent a message using the local broadcast manager but it
                    // is not directed to this fragment. we can simply ignore it. this is useful
                    // to avoid that the dialog appear when the auto backup is fired by the
                    // system and the user is browsing the backup section of the application.
                    return;
                }
                switch (intent.getAction()) {
                    case LocalAction.ACTION_BACKUP_SERVICE_STARTED:
                        if (mProgressDialog == null) {
                            switch (intent.getIntExtra(BackupHandlerIntentService.ACTION, 0)) {
                                case BackupHandlerIntentService.ACTION_BACKUP:
                                    mProgressDialog = GenericProgressDialog.newInstance(R.string.title_backup_creation, R.string.message_async_init, false);
                                    break;
                                case BackupHandlerIntentService.ACTION_RESTORE:
                                    mProgressDialog = GenericProgressDialog.newInstance(R.string.title_backup_restoring, R.string.message_async_init, false);
                                    break;
                                default:
                                    return;
                            }
                        }
                        mProgressDialog.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
                        break;
                    case LocalAction.ACTION_BACKUP_SERVICE_RUNNING:
                        if (mProgressDialog != null) {
                            int status = intent.getIntExtra(BackupHandlerIntentService.PROGRESS_STATUS, 0);
                            int value = intent.getIntExtra(BackupHandlerIntentService.PROGRESS_VALUE, 0);
                            int contentRes = 0;
                            switch (status) {
                                case BackupHandlerIntentService.STATUS_BACKUP_CREATION:
                                    contentRes = R.string.message_backup_status_creation;
                                    break;
                                case BackupHandlerIntentService.STATUS_BACKUP_UPLOADING:
                                    contentRes = R.string.message_backup_status_uploading;
                                    break;
                                case BackupHandlerIntentService.STATUS_BACKUP_DOWNLOADING:
                                    contentRes = R.string.message_backup_status_downloading;
                                    break;
                                case BackupHandlerIntentService.STATUS_BACKUP_RESTORING:
                                    contentRes = R.string.message_backup_status_restoring;
                                    break;
                            }
                            mProgressDialog.updateProgress(contentRes, value);
                        }
                        break;
                    case LocalAction.ACTION_BACKUP_SERVICE_FINISHED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        int titleRes = R.string.title_success;
                        int messageRes;
                        switch (intent.getIntExtra(BackupHandlerIntentService.ACTION, 0)) {
                            case BackupHandlerIntentService.ACTION_BACKUP:
                                messageRes = R.string.message_backup_creation_success;
                                break;
                            case BackupHandlerIntentService.ACTION_RESTORE:
                                messageRes = R.string.message_backup_restoring_success;
                                break;
                            default:
                                // ACTION_LIST
                                return;
                        }
                        ThemedDialog.buildMaterialDialog(BackupListActivity.this)
                                .title(titleRes)
                                .content(messageRes)
                                .positiveText(android.R.string.ok)
                                .onAny(new MaterialDialog.SingleButtonCallback() {

                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent = getIntent();
                                        if (intent != null) {
                                            int mode = intent.getIntExtra(BACKUP_MODE, FULL);
                                            if (mode == RESTORE_ONLY) {
                                                // we are probably waiting for the backup to be restored
                                                // in a parent activity, so we have to return an ok result
                                                BackupListActivity.this.setResult(Activity.RESULT_OK);
                                                finish();
                                            }
                                        }
                                    }

                                })
                                .show();
                        break;
                    case LocalAction.ACTION_BACKUP_SERVICE_FAILED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        Exception exception = (Exception) intent.getSerializableExtra(BackupHandlerIntentService.EXCEPTION);
                        String message = exception.getMessage();
                        titleRes = R.string.title_failed;
                        String messageString;
                        switch (intent.getIntExtra(BackupHandlerIntentService.ACTION, 0)) {
                            case BackupHandlerIntentService.ACTION_BACKUP:
                                messageString = getString(R.string.message_backup_creation_failed, message);
                                break;
                            case BackupHandlerIntentService.ACTION_RESTORE:
                                messageString = getString(R.string.message_backup_restoring_failed, message);
                                break;
                            default:
                                return;
                        }
                        ThemedDialog.buildMaterialDialog(BackupListActivity.this)
                                .title(titleRes)
                                .content(messageString)
                                .positiveText(android.R.string.ok)
                                .show();
                        break;
                }
            }
        }

    };
}