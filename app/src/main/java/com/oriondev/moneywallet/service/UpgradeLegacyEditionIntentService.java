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

package com.oriondev.moneywallet.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.storage.database.LegacyEditionImporter;

/**
 * This service is used by the LauncherActivity when a legacy database is detected at startup.
 * The goal is to correctly import all the data coming from the old database and shared preferences
 * into the new data structures.
 */
public class UpgradeLegacyEditionIntentService extends IntentService {

    private static final String ERROR_MESSAGE = "UpgradeLegacyEditionIntentService::ErrorMessage";

    private final LocalBroadcastManager mBroadcastManager;

    public static boolean isLegacyEditionDetected(Context context) {
        // check if exists a legacy database to import
        return context.getDatabasePath(LegacyEditionImporter.DATABASE_NAME).exists();
    }

    public UpgradeLegacyEditionIntentService() {
        super("UpgradeLegacyEditionIntentService");
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notifyServiceStarted();
        try {
            LegacyEditionImporter importer = new LegacyEditionImporter(this);
            importer.importDatabase();
            importer.importAttachments();
            importer.importPreferences();
        } catch (Exception e) {
            notifyServiceFailed(e.getMessage());
        }
        RecurrenceBroadcastReceiver.scheduleRecurrenceTask(this);
        notifyServiceFinished();
    }

    private void notifyServiceStarted() {
        Intent intent = new Intent(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_STARTED);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyServiceFailed(String message) {
        Intent intent = new Intent(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FAILED);
        intent.putExtra(ERROR_MESSAGE, message);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyServiceFinished() {
        Intent intent = new Intent(LocalAction.ACTION_LEGACY_EDITION_UPGRADE_FINISHED);
        mBroadcastManager.sendBroadcast(intent);
    }
}