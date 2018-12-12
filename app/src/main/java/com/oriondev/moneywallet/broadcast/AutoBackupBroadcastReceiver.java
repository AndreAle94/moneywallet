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

package com.oriondev.moneywallet.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.service.BackupHandlerIntentService;
import com.oriondev.moneywallet.storage.preference.BackendManager;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by andrea on 27/11/18.
 */
public class AutoBackupBroadcastReceiver extends BroadcastReceiver {

    private static final int MILLIS_IN_HOUR = 1000 * 60 * 60;

    public static void scheduleAutoBackupTask(Context context) {
        cancelPendingIntent(context);
        Set<String> backendIdSet = BackendManager.getAutoBackupEnabledServices();
        if (backendIdSet != null && !backendIdSet.isEmpty()) {
            Long nextTimestamp = null;
            for (String backendId : backendIdSet) {
                long lastTimestamp = BackendManager.getAutoBackupLastTime(backendId);
                int hourOffset = BackendManager.getAutoBackupHoursOffset(backendId);
                long nextOccurrence = lastTimestamp + (hourOffset * MILLIS_IN_HOUR);
                if (nextTimestamp == null || nextOccurrence < nextTimestamp) {
                    nextTimestamp = nextOccurrence;
                }
            }
            if (nextTimestamp != null) {
                if (nextTimestamp <= System.currentTimeMillis()) {
                    startBackgroundTask(context);
                } else {
                    schedulePendingIntent(context, nextTimestamp);
                }
            }
        }
    }

    private static void schedulePendingIntent(Context context, long timestamp) {
        PendingIntent pendingIntent = createPendingIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            System.out.println("[ALARM] AutoBackupTask scheduled at: " + timestamp);
        }
    }

    private static void cancelPendingIntent(Context context) {
        PendingIntent pendingIntent = createPendingIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoBackupBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        startBackgroundTask(context);
    }

    private static void startBackgroundTask(Context context) {
        System.out.println("[ALARM] AutoBackupTask fired now");
        Set<String> backendIdSet = BackendManager.getAutoBackupEnabledServices();
        if (backendIdSet != null && !backendIdSet.isEmpty()) {
            for (String backendId : backendIdSet) {
                long lastTimestamp = BackendManager.getAutoBackupLastTime(backendId);
                int hourOffset = BackendManager.getAutoBackupHoursOffset(backendId);
                long nextOccurrence = lastTimestamp + (hourOffset * MILLIS_IN_HOUR);
                if (nextOccurrence <= System.currentTimeMillis()) {
                    if (!BackendManager.isAutoBackupWhenDataIsChangedOnly(backendId) || PreferenceManager.getLastTimeDataIsChanged() > lastTimestamp) {
                        boolean onlyOnWiFi = BackendManager.isAutoBackupOnWiFiOnly(backendId);
                        IFile folder = BackendServiceFactory.getFile(backendId, BackendManager.getAutoBackupFolder(backendId));
                        String password = BackendManager.getAutoBackupPassword(backendId);
                        // build the intent to start the service
                        Intent intent = new Intent(context, BackupHandlerIntentService.class);
                        intent.putExtra(BackupHandlerIntentService.ACTION, BackupHandlerIntentService.ACTION_BACKUP);
                        intent.putExtra(BackupHandlerIntentService.BACKEND_ID, backendId);
                        intent.putExtra(BackupHandlerIntentService.AUTO_BACKUP, true);
                        intent.putExtra(BackupHandlerIntentService.ONLY_ON_WIFI, onlyOnWiFi);
                        intent.putExtra(BackupHandlerIntentService.PARENT_FOLDER, folder);
                        intent.putExtra(BackupHandlerIntentService.PASSWORD, password);
                        BackupHandlerIntentService.startInForeground(context, intent);
                    }
                    // register the next occurrence as the last time the auto backup
                    // for this specific backend has been executed: if an error occur
                    // and the backup process is interrupted, the error is shown in
                    // a specific notification (no auto rescheduling because we don't
                    // know if the error is a recoverable error or a critical one)
                    BackendManager.setAutoBackupLastTime(backendId, nextOccurrence);
                }
            }
        }
        // reschedule the auto backup immediately (if one or more intent services are fired, they
        // are executed in a serial way but the last time is already updated). in this way we can
        // calculate the next occurrence without waiting that all the intent services are executed.
        // if an exception occur inside one intent service and a backend should be disabled, it
        // will handle it automatically without the need to wait here for a response.
        AutoBackupBroadcastReceiver.scheduleAutoBackupTask(context);
    }
}