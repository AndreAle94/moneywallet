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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.oriondev.moneywallet.api.AbstractBackendServiceAPI;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.api.AbstractBackendServiceDelegate;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.api.IBackendServiceAPI;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.storage.database.ExportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.LegacyBackupImporter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;
import com.oriondev.moneywallet.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public static final String BACKUP_FILE = "BackupHandlerIntentService::Argument::BackupFile";
    public static final String ERROR_MESSAGE = "BackupHandlerIntentService::Argument::ErrorMessage";
    public static final String FOLDER_CONTENT = "BackupHandlerIntentService::Argument::FolderContent";
    public static final String PARENT_FOLDER = "BackupHandlerIntentService::Argument::ParentFolder";
    public static final String PASSWORD = "BackupHandlerIntentService::Argument::Password";
    public static final String PROGRESS_STATUS = "BackupHandlerIntentService::Argument::ProgressStatus";
    public static final String PROGRESS_VALUE = "BackupHandlerIntentService::Argument::ProgressValue";

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

    private IBackendServiceAPI mBackendServiceAPI;

    private LocalBroadcastManager mBroadcastManager;

    public BackupHandlerIntentService() {
        super("BackupHandlerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(BACKEND_ID)) {
            // initialize the correct backend and the broadcast manager
            String backendId = intent.getStringExtra(BACKEND_ID);
            try {
                mBackendServiceAPI = BackendServiceFactory.getServiceAPIById(this, backendId);
            } catch (BackendException e) {
                e.printStackTrace();
                return;
            }
            mBroadcastManager = LocalBroadcastManager.getInstance(this);
            // strip the intent and check what the user is trying to do
            switch (intent.getIntExtra(ACTION, ACTION_NONE)) {
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
        }
    }

    private void onActionList(@NonNull Intent intent) {
        notifyTaskStarted(ACTION_LIST);
        IFile remoteFolder = intent.getParcelableExtra(PARENT_FOLDER);
        try {
            List<IFile> fileList = mBackendServiceAPI.getFolderContent(remoteFolder);
            notifyListTaskFinished(fileList);
        } catch (BackendException e) {
            notifyTaskFailure(ACTION_LIST, e.getMessage());
        }
    }

    private void onActionBackup(@NonNull Intent intent) {
        notifyTaskStarted(ACTION_BACKUP);
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
        } catch (Exception e) {
            notifyTaskFailure(ACTION_BACKUP, e.getMessage());
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
        File backupFile = createBackupFile(folder, BackupManager.getExtension(password != null));
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

    private void onActionRestore(@NonNull Intent intent) {
        IFile remoteFile = intent.getParcelableExtra(BACKUP_FILE);
        if (remoteFile != null) {
            notifyTaskStarted(ACTION_RESTORE);
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
                notifyTaskFinished(ACTION_RESTORE);
                RecurrenceBroadcastReceiver.scheduleRecurrenceTask(this);
            } catch (Exception e) {
                e.printStackTrace();
                notifyTaskFailure(ACTION_RESTORE, e.getMessage());
            } finally {
                FileUtils.deleteQuietly(revision);
            }
        }
    }

    private void restoreLocalBackupFile(@NonNull File backup, @Nullable String password) throws Exception {
        AbstractBackupImporter importer;
        String fileName = backup.getName();
        if (fileName.endsWith(BackupManager.BACKUP_EXTENSION_LEGACY)) {
            importer = new LegacyBackupImporter(getContentResolver(), backup);
        } else {
            importer = new DefaultBackupImporter(getContentResolver(), backup, password);
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

    private void notifyTaskStarted(int action) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_STARTED);
        intent.putExtra(ACTION, action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskProgress(int action, int status, int progress) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_RUNNING);
        intent.putExtra(ACTION, action);
        intent.putExtra(PROGRESS_STATUS, status);
        intent.putExtra(PROGRESS_VALUE, progress);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFinished(int action) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyListTaskFinished(List<IFile> files) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_LIST);
        intent.putParcelableArrayListExtra(FOLDER_CONTENT, Utils.wrapAsArrayList(files));
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyUploadTaskFinished(IFile file) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_BACKUP);
        intent.putExtra(BACKUP_FILE, file);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFailure(int action, String message) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FAILED);
        intent.putExtra(ACTION, action);
        intent.putExtra(ERROR_MESSAGE, message);
        mBroadcastManager.sendBroadcast(intent);
    }
}