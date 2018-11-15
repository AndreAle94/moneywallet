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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.service.UpgradeLegacyEditionIntentService;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.base.ThemedActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by andrea on 30/07/18.
 */
public class LauncherActivity extends ThemedActivity {

    private static final int REQUEST_FIRST_START = 273;

    private ProgressWheel mProgressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UpgradeLegacyEditionIntentService.isLegacyEditionDetected(this)) {
            setContentView(R.layout.activity_launcher_legacy_edition_upgrade);
            mProgressWheel = findViewById(R.id.progress_wheel);
            // prepare the broadcast receiver
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_STARTED);
            intentFilter.addAction(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FINISHED);
            intentFilter.addAction(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FAILED);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
            // start the service
            if (savedInstanceState == null) {
                mProgressWheel.setVisibility(View.INVISIBLE);
                startService(new Intent(this, UpgradeLegacyEditionIntentService.class));
            }
        } else {
            if (!PreferenceManager.isFirstStartDone()) {
                setContentView(R.layout.activity_launcher_first_start);
                Button firstStartButton = findViewById(R.id.first_start_button);
                Button restoreBackupButton = findViewById(R.id.restore_backup_button);
                firstStartButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LauncherActivity.this, TutorialActivity.class);
                        startActivityForResult(intent, REQUEST_FIRST_START);
                    }

                });
                restoreBackupButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LauncherActivity.this, BackupListActivity.class);
                        intent.putExtra(BackupListActivity.BACKUP_MODE, BackupListActivity.RESTORE_ONLY);
                        startActivityForResult(intent, REQUEST_FIRST_START);
                    }

                });
            } else {
                startMainActivity();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FIRST_START) {
            if (resultCode == RESULT_OK) {
                PreferenceManager.setIsFirstStartDone(true);
                startMainActivity();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case LocalAction.ACTION_LEGACY_EDITION_UPGRADE_STARTED:
                        if (mProgressWheel != null) {
                            mProgressWheel.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FINISHED:
                        startMainActivity();
                        break;
                    case LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FAILED:
                        if (mProgressWheel != null) {
                            mProgressWheel.setVisibility(View.INVISIBLE);
                        }
                        // TODO: display an error message
                        break;
                }
            }
        }

    };
}