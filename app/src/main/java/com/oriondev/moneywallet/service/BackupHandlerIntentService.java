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
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.api.IBackendServiceAPI;
import com.oriondev.moneywallet.broadcast.AutoBackupBroadcastReceiver;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.NotificationBroadcastReceiver;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.ExportException;
import com.oriondev.moneywallet.storage.database.ImportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.LegacyBackupImporter;
import com.oriondev.moneywallet.storage.preference.BackendManager;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.notification.NotificationContract;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;
import com.oriondev.moneywallet.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by andrea on 21/11/18.
 */
public class BackupHandlerIntentService extends IntentService {

    public static final String ACTION = "BackupHandlerIntentService::Argument::Action";
    public static final String BACKEND_ID = "BackupHandlerIntentService::Argument::BackendId";
    public static final String AUTO_BACKUP = "BackupHandlerIntentService::Argument::AutoBackup";
    public static final String ONLY_ON_WIFI = "BackupHandlerIntentService::Argument::OnlyOnWifi";
    public static final String RUN_FOREGROUND = "BackupHandlerIntentService::Argument::RunForeground";
    public static final String BACKUP_FILE = "BackupHandlerIntentService::Argument::BackupFile";
    public static final String EXCEPTION = "BackupHandlerIntentService::Argument::Exception";
    public static final String FOLDER_CONTENT = "BackupHandlerIntentService::Argument::FolderContent";
    public static final String PARENT_FOLDER = "BackupHandlerIntentService::Argument::ParentFolder";
    public static final String PASSWORD = "BackupHandlerIntentService::Argument::Password";
    public static final String PROGRESS_STATUS = "BackupHandlerIntentService::Argument::ProgressStatus";
    public static final String PROGRESS_VALUE = "BackupHandlerIntentService::Argument::ProgressValue";
    public static final String CALLER_ID = "BackupHandlerIntentService::Argument::CallerId";

    private static final String ATTACHMENT_FOLDER = "attachments";
    private static final String BACKUP_CACHE_FOLDER = "backups";
    private static final String TEMP_FOLDER = "temp";
    private static final String FILE_DATETIME_PATTERN = "yyyy-MM-dd_HH:mm:ss";
    private static final String OUTPUT_FILE = "backup_%s%s";

    private static final int ACTION_NONE = 0;
    public static final int ACTION_LIST = 1;
    public static final int ACTION_BACKUP = 2;
    public static final int ACTION_RESTORE = 3;

    public static final int STATUS_BACKUP_CREATION = 1;
    public static final int STATUS_BACKUP_UPLOADING = 2;
    public static final int STATUS_BACKUP_DOWNLOADING = 3;
    public static final int STATUS_BACKUP_RESTORING = 4;

    private static final boolean DEFAULT_AUTO_BACKUP = false;
    private static final boolean DEFAULT_ONLY_ON_WIFI = false;
    private static final boolean DEFAULT_RUN_FOREGROUND = false;

    private boolean mAutoBackup;

    private IBackendServiceAPI mBackendServiceAPI;

    private String mCallerId;
    private LocalBroadcastManager mBroadcastManager;
    private NotificationCompat.Builder mNotificationBuilder;

    public static void startInForeground(Context context, Intent intent) {
        intent.putExtra(BackupHandlerIntentService.RUN_FOREGROUND, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public BackupHandlerIntentService() {
        super("BackupHandlerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            // unpack the base intent
            int action = intent.getIntExtra(ACTION, ACTION_NONE);
            String backendId = intent.getStringExtra(BACKEND_ID);
            mAutoBackup = intent.getBooleanExtra(AUTO_BACKUP, DEFAULT_AUTO_BACKUP);
            boolean onlyOnWiFi = intent.getBooleanExtra(ONLY_ON_WIFI, DEFAULT_ONLY_ON_WIFI);
            boolean runForeground = intent.getBooleanExtra(RUN_FOREGROUND, DEFAULT_RUN_FOREGROUND);
            mCallerId = intent.getStringExtra(CALLER_ID);
            // execute the action in a safe code-block: if an exception is thrown, it
            // is handled by both the local broadcast manager as an error and reported
            // in the notification if it is required
            Exception exception = null;
            try {
                // initialize the broadcast manager
                mBroadcastManager = LocalBroadcastManager.getInstance(this);
                // if the notification is required, start the service in foreground
                if (runForeground && (action == ACTION_BACKUP || action == ACTION_RESTORE)) {
                    mNotificationBuilder = getBaseNotificationBuilder(NotificationContract.NOTIFICATION_CHANNEL_BACKUP)
                            .setProgress(0, 0, true)
                            .setCategory(NotificationCompat.CATEGORY_PROGRESS);
                }
                // send a local message to notify the receivers that the task is started
                notifyTaskStarted(action);
                // check if backend id is available and initialize it
                mBackendServiceAPI = BackendServiceFactory.getServiceAPIById(this, backendId);
                // the first step is to check if the task should be executed only when
                // connected on a WiFi network: in this case we need to check if the
                // device is connected to a WiFi network or stop the task
                if (onlyOnWiFi && (action == ACTION_BACKUP || action == ACTION_RESTORE)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager != null) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (!networkInfo.isConnected()) {
                            throw new WiFiNotConnectedException();
                        }
                    }
                }
                switch (action) {
                    case ACTION_LIST:
                        onActionList(intent);
                        break;
                    case ACTION_BACKUP:
                        onActionBackup(intent);
                        break;
                    case ACTION_RESTORE:
                        onActionRestore(intent);
                        break;
                }
            } catch (Exception e) {
                exception = e;
            }
            // handle the exception if the task failed
            if (exception != null) {
                if (exception instanceof BackendException) {
                    if (!((BackendException) exception).isRecoverable()) {
                        // disable auto-backup for this backend id
                        BackendManager.setAutoBackupEnabled(backendId, false);
                        AutoBackupBroadcastReceiver.scheduleAutoBackupTask(this);
                    }
                }
                notifyTaskFailure(intent, exception);
            }
            // clear the environment
            if (mNotificationBuilder != null) {
                stopForeground(true);
            }
        }
    }

    private void onActionList(@NonNull Intent intent) throws BackendException {
        IFile remoteFolder = intent.getParcelableExtra(PARENT_FOLDER);
        List<IFile> fileList = mBackendServiceAPI.getFolderContent(remoteFolder);
        notifyListTaskFinished(fileList);
    }

    private void onActionBackup(@NonNull Intent intent) throws ExportException, BackendException, IOException {
        IFile remoteFolder = intent.getParcelableExtra(PARENT_FOLDER);
        File folder = getExternalFilesDir(null);
        File cache = new File(folder, BACKUP_CACHE_FOLDER);
        File revision = new File(cache, UUID.randomUUID().toString());
        try {
            FileUtils.forceMkdir(revision);
            String password = intent.getStringExtra(PASSWORD);
            notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_CREATION, 0);
            File backup = prepareLocalBackupFile(revision, password);
            notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, 30);
            IFile uploaded = mBackendServiceAPI.uploadFile(remoteFolder, backup, new ProgressInputStream.UploadProgressListener() {

                @Override
                public void onUploadProgressUpdate(int percentage) {
                    int realProgress = 30 + (percentage * 70 / 100);
                    notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, realProgress);
                }

            });
            notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, 100);
            notifyUploadTaskFinished(uploaded);
        } finally {
            FileUtils.deleteQuietly(revision);
        }
    }

    /**
     * Create a local zip file that contains the database entries according to the backup
     * file specification. If a password is provided, set it to the zip file.
     * @param folder where the local backup is stored.
     * @param password if the backup should be protected.
     * @return the backup file is success.
     */
    private File prepareLocalBackupFile(@NonNull File folder, @Nullable String password) throws ExportException, IOException {
        File backupFile = createBackupFile(folder, BackupManager.getExtension(!TextUtils.isEmpty(password)));
        AbstractBackupExporter exporter = new DefaultBackupExporter(getContentResolver(), backupFile, password);
        exporter.exportDatabase(getFilesDir());
        exporter.exportAttachments(getAttachmentFolder());
        return backupFile;
    }

    private File createBackupFile(@NonNull File folder, @NonNull String extension) {
        String datetime = DateUtils.getDateTimeString(new Date(), FILE_DATETIME_PATTERN);
        String name = String.format(Locale.ENGLISH, OUTPUT_FILE, datetime, extension);
        return new File(folder, name);
    }

    private File getAttachmentFolder() throws IOException {
        File root = getExternalFilesDir(null);
        File folder = new File(root, ATTACHMENT_FOLDER);
        FileUtils.forceMkdir(folder);
        return folder;
    }

    private void onActionRestore(@NonNull Intent intent) throws ImportException, BackendException, IOException {
        IFile remoteFile = intent.getParcelableExtra(BACKUP_FILE);
        if (remoteFile != null) {
            File folder = getExternalFilesDir(null);
            File cache = new File(folder, BACKUP_CACHE_FOLDER);
            File revision = new File(cache, UUID.randomUUID().toString());
            try {
                FileUtils.forceMkdir(revision);
                notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_DOWNLOADING, 0);
                File backup = mBackendServiceAPI.downloadFile(revision, remoteFile, new ProgressOutputStream.DownloadProgressListener() {

                    @Override
                    public void onDownloadProgressUpdate(int percentage) {
                        int realProgress = (percentage * 70 / 100);
                        notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_DOWNLOADING, realProgress);
                    }

                });
                notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_RESTORING, 75);
                String password = intent.getStringExtra(PASSWORD);
                restoreLocalBackupFile(backup, password);
                notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_RESTORING, 100);
                DataContentProvider.notifyDatabaseIsChanged(this);
                PreferenceManager.setLastTimeDataIsChanged(0L);
                CurrencyManager.invalidateCache(this);
                RecurrenceBroadcastReceiver.scheduleRecurrenceTask(this);
                AutoBackupBroadcastReceiver.scheduleAutoBackupTask(this);
                notifyTaskFinished(ACTION_RESTORE);
            } finally {
                FileUtils.deleteQuietly(revision);
            }
        } else {
            throw new RuntimeException("Backup file to restore not specified");
        }
    }

    private void restoreLocalBackupFile(@NonNull File backup, @Nullable String password) throws ImportException, IOException {
        AbstractBackupImporter importer;
        String fileName = backup.getName();
        if (fileName.endsWith(BackupManager.BACKUP_EXTENSION_LEGACY)) {
            importer = new LegacyBackupImporter(this, backup);
        } else {
            importer = new DefaultBackupImporter(this, backup, password);
        }
        File temporaryFolder = new File(getExternalFilesDir(null), TEMP_FOLDER);
        FileUtils.forceMkdir(temporaryFolder);
        try {
            File databaseFile = getDatabasePath(SQLDatabaseImporter.DATABASE_NAME);
            importer.importDatabase(temporaryFolder, databaseFile.getParentFile());
            importer.importAttachments(getAttachmentFolder());
        } finally {
            FileUtils.cleanDirectory(temporaryFolder);
        }
    }

    private String getNotificationContentTitle(int action, boolean error) {
        if (action == ACTION_BACKUP) {
            return getString(error ? R.string.notification_title_backup_creation_failed : R.string.notification_title_backup_creation);
        } else if (action == ACTION_RESTORE) {
            return getString(error ? R.string.notification_title_backup_restoring_failed : R.string.notification_title_backup_restoring);
        }
        return null;
    }

    private NotificationCompat.Builder getBaseNotificationBuilder(String channelId) {
        return new NotificationCompat.Builder(getBaseContext(), channelId)
                .setSmallIcon(Utils.isAtLeastLollipop() ? R.drawable.ic_notification : R.mipmap.ic_launcher);
    }

    private String getNotificationContentText(int status) {
        switch (status) {
            case STATUS_BACKUP_CREATION:
                return getString(R.string.notification_content_backup_file_creation);
            case STATUS_BACKUP_UPLOADING:
                return getString(R.string.notification_content_backup_file_uploading);
            case STATUS_BACKUP_DOWNLOADING:
                return getString(R.string.notification_content_backup_file_downloading);
            case STATUS_BACKUP_RESTORING:
                return getString(R.string.notification_content_backup_file_restoring);
        }
        return null;
    }

    private void notifyTaskStarted(int action) {
        // notify the local broadcast manager
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_STARTED);
        intent.putExtra(ACTION, action);
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
        // update the notification if required
        if (mNotificationBuilder != null) {
            mNotificationBuilder.setContentTitle(getNotificationContentTitle(action, false));
            startForeground(NotificationContract.NOTIFICATION_ID_BACKUP_PROGRESS, mNotificationBuilder.build());
        }
    }

    private void notifyTaskProgress(int action, int status, int progress) {
        // notify the local broadcast manager
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_RUNNING);
        intent.putExtra(ACTION, action);
        intent.putExtra(PROGRESS_STATUS, status);
        intent.putExtra(PROGRESS_VALUE, progress);
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
        // update the notification if required
        if (mNotificationBuilder != null) {
            mNotificationBuilder.setContentText(getNotificationContentText(status));
            mNotificationBuilder.setProgress(100, progress, false);
            startForeground(NotificationContract.NOTIFICATION_ID_BACKUP_PROGRESS, mNotificationBuilder.build());
        }
    }

    private void notifyTaskFinished(int action) {
        // notify only the local broadcast manager: it is not required to update
        // the notification because it is removed when everything is gone right
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, action);
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyListTaskFinished(List<IFile> files) {
        // notify only the local broadcast manager: it is not required to update
        // the notification because it is removed when everything is gone right
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_LIST);
        intent.putParcelableArrayListExtra(FOLDER_CONTENT, Utils.wrapAsArrayList(files));
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyUploadTaskFinished(IFile file) {
        // notify only the local broadcast manager: it is not required to update
        // the notification because it is removed when everything is gone right
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_BACKUP);
        intent.putExtra(BACKUP_FILE, file);
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFailure(Intent baseIntent, Exception exception) {
        // notify the local broadcast manager
        int action = baseIntent.getIntExtra(ACTION, ACTION_NONE);
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FAILED);
        intent.putExtra(ACTION, action);
        intent.putExtra(EXCEPTION, exception);
        intent.putExtra(CALLER_ID, mCallerId);
        mBroadcastManager.sendBroadcast(intent);
        // update the notification if required
        if (mNotificationBuilder != null || mAutoBackup) {
            mNotificationBuilder = getBaseNotificationBuilder(NotificationContract.NOTIFICATION_CHANNEL_ERROR)
                    .setContentTitle(getNotificationContentTitle(action, true))
                    .setCategory(NotificationCompat.CATEGORY_ERROR);
            if (exception instanceof WiFiNotConnectedException) {
                // create a copy of the arguments used in this service
                Bundle intentArguments = new Bundle();
                intentArguments.putInt(ACTION, action);
                intentArguments.putString(BACKEND_ID, baseIntent.getStringExtra(BACKEND_ID));
                intentArguments.putBoolean(AUTO_BACKUP, baseIntent.getBooleanExtra(AUTO_BACKUP, DEFAULT_AUTO_BACKUP));
                intentArguments.putBoolean(ONLY_ON_WIFI, baseIntent.getBooleanExtra(ONLY_ON_WIFI, DEFAULT_ONLY_ON_WIFI));
                intentArguments.putBoolean(RUN_FOREGROUND, baseIntent.getBooleanExtra(RUN_FOREGROUND, DEFAULT_RUN_FOREGROUND));
                intentArguments.putString(PASSWORD, baseIntent.getStringExtra(PASSWORD));
                intentArguments.putParcelable(PARENT_FOLDER, baseIntent.getParcelableExtra(PARENT_FOLDER));
                intentArguments.putParcelable(BACKUP_FILE, baseIntent.getParcelableExtra(BACKUP_FILE));
                // prepare the pending intent for the notification receiver
                Intent retryIntent = new Intent(this, NotificationBroadcastReceiver.class);
                retryIntent.setAction(NotificationBroadcastReceiver.ACTION_RETRY_BACKUP_CREATION);
                retryIntent.putExtra(NotificationBroadcastReceiver.ACTION_INTENT_ARGUMENTS, intentArguments);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // finish to setup the notification body
                mNotificationBuilder.setContentText(getString(R.string.notification_content_backup_error_wifi_network));
                mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_backup_error_wifi_network)));
                mNotificationBuilder.addAction(R.drawable.ic_refresh_black_24dp, getString(R.string.notification_action_retry), pendingIntent);
            } else if (exception instanceof BackendException) {
                if (((BackendException) exception).isRecoverable()) {
                    // create a copy of the arguments used in this service
                    Bundle intentArguments = new Bundle();
                    intentArguments.putInt(ACTION, action);
                    intentArguments.putString(BACKEND_ID, baseIntent.getStringExtra(BACKEND_ID));
                    intentArguments.putBoolean(AUTO_BACKUP, baseIntent.getBooleanExtra(AUTO_BACKUP, DEFAULT_AUTO_BACKUP));
                    intentArguments.putBoolean(ONLY_ON_WIFI, baseIntent.getBooleanExtra(ONLY_ON_WIFI, DEFAULT_ONLY_ON_WIFI));
                    intentArguments.putBoolean(RUN_FOREGROUND, baseIntent.getBooleanExtra(RUN_FOREGROUND, DEFAULT_RUN_FOREGROUND));
                    intentArguments.putString(PASSWORD, baseIntent.getStringExtra(PASSWORD));
                    intentArguments.putParcelable(PARENT_FOLDER, baseIntent.getParcelableExtra(PARENT_FOLDER));
                    intentArguments.putParcelable(BACKUP_FILE, baseIntent.getParcelableExtra(BACKUP_FILE));
                    // prepare the pending intent for the notification receiver
                    Intent retryIntent = new Intent(this, NotificationBroadcastReceiver.class);
                    retryIntent.setAction(NotificationBroadcastReceiver.ACTION_RETRY_BACKUP_CREATION);
                    retryIntent.putExtra(NotificationBroadcastReceiver.ACTION_INTENT_ARGUMENTS, intentArguments);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    // finish to setup the notification body
                    mNotificationBuilder.setContentText(getString(R.string.notification_content_backup_error_backend_recoverable));
                    mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_backup_error_backend_recoverable)));
                    mNotificationBuilder.addAction(R.drawable.ic_refresh_black_24dp, getString(R.string.notification_action_retry), pendingIntent);
                } else {
                    mNotificationBuilder.setContentText(getString(R.string.notification_content_backup_error_backend));
                    mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_backup_error_backend)));
                }
            } else {
                String message = getString(R.string.notification_content_backup_error_internal, exception.getMessage());
                mNotificationBuilder.setContentText(message);
                mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            }
            // use the notification service instead of the foreground service
            // because when the intent service has finished the notification
            // is removed even if the stopForeground is set to false
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NotificationContract.NOTIFICATION_ID_BACKUP_ERROR, mNotificationBuilder.build());
        }
    }

    private class WiFiNotConnectedException extends Exception {

        private WiFiNotConnectedException() {
            super("the device is not connected to a WiFi network");
        }
    }
}