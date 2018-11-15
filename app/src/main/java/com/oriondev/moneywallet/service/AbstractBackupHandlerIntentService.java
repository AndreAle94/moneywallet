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

import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.AbstractBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupExporter;
import com.oriondev.moneywallet.storage.database.backup.DefaultBackupImporter;
import com.oriondev.moneywallet.storage.database.backup.LegacyBackupImporter;
import com.oriondev.moneywallet.utils.DateUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by andre on 22/03/2018.
 */
public abstract class AbstractBackupHandlerIntentService<T extends IFile> extends IntentService {

    public static final String SERVICE_ACTION = "AbstractBackupHandlerIntentService::Action";
    public static final String SERVICE_PROGRESS_STATUS = "AbstractBackupHandlerIntentService::ProgressStatus";
    public static final String SERVICE_PROGRESS_VALUE = "AbstractBackupHandlerIntentService::ProgressValue";
    public static final String SERVICE_ERROR_MESSAGE = "AbstractBackupHandlerIntentService::ErrorMessage";
    public static final String PARENT_FOLDER = "AbstractBackupHandlerIntentService::ParentFolder";
    public static final String BACKUP_FILE = "AbstractBackupHandlerIntentService::BackupFile";
    public static final String FOLDER_CONTENT = "AbstractBackupHandlerIntentService::FolderContent";

    public static final String PASSWORD = "AbstractBackupHandlerIntentService::Password";

    private static final int ACTION_NONE = 0;
    public static final int ACTION_LIST = 1;
    public static final int ACTION_BACKUP = 2;
    public static final int ACTION_RESTORE = 3;

    public static final int STATUS_BACKUP_CREATION = 1;
    public static final int STATUS_BACKUP_UPLOADING = 2;
    public static final int STATUS_BACKUP_DOWNLOADING = 3;
    public static final int STATUS_BACKUP_RESTORING = 4;

    private static final String BACKUP_FOLDER = "backups";
    private static final String TEMP_FOLDER = "temp";
    private static final String FILE_DATETIME_PATTERN = "yyyy-MM-dd_HH:mm:ss";
    private static final String OUTPUT_FILE = "backup_%s%s";

    private LocalBroadcastManager mBroadcastManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractBackupHandlerIntentService(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            mBroadcastManager = LocalBroadcastManager.getInstance(this);
            switch (intent.getIntExtra(SERVICE_ACTION, ACTION_NONE)) {
                case ACTION_LIST:
                    onActionList(intent, (T) intent.getParcelableExtra(PARENT_FOLDER));
                    break;
                case ACTION_BACKUP:
                    onActionBackup(intent, (T) intent.getParcelableExtra(PARENT_FOLDER));
                    break;
                case ACTION_RESTORE:
                    onActionRestore(intent, (T) intent.getParcelableExtra(BACKUP_FILE));
                    break;
            }
        }
    }

    private void onActionList(Intent intent, T remote) {
        if (remote != null) {
            notifyTaskStarted(ACTION_LIST);
            try {
                ArrayList<T> files = getFolderContent(remote);
                notifyListTaskFinished(files);
            } catch (Exception e) {
                e.printStackTrace();
                notifyTaskFailure(ACTION_LIST, e.getMessage());
            }
        }
    }

    private void onActionBackup(Intent intent, T remote) {
        if (remote != null) {
            notifyTaskStarted(ACTION_BACKUP);
            File folder = getExternalFilesDir(null);
            File cache = new File(folder, BACKUP_FOLDER);
            File revision = new File(cache, UUID.randomUUID().toString());
            try {
                FileUtils.forceMkdir(revision);
                String password = intent.getStringExtra(PASSWORD);
                notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_CREATION, 0);
                File backup = prepareLocalBackupFile(revision, password);
                notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, 30);
                T uploaded = uploadFile(remote, backup);
                notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, 100);
                notifyUploadTaskFinished(uploaded);
            } catch (Exception e) {
                e.printStackTrace();
                notifyTaskFailure(ACTION_BACKUP, e.getMessage());
            } finally {
                FileUtils.deleteQuietly(revision);
            }
        }
    }

    /**
     * Create a local zip file that contains the database entries according to the backup
     * file specification. If a password is provided, set it to the zip file.
     * @param folder where the local backup is stored.
     * @param password if the backup should be protected.
     * @return the backup file is success.
     */
    private File prepareLocalBackupFile(@NonNull File folder, @Nullable String password) throws Exception {
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
        File folder = new File(getExternalFilesDir(null), "attachments");
        FileUtils.forceMkdir(folder);
        return folder;
    }

    protected abstract T uploadFile(T folder, File backup) throws Exception;

    private void onActionRestore(Intent intent, T file) {
        if (file != null) {
            notifyTaskStarted(ACTION_RESTORE);
            File folder = getExternalFilesDir(null);
            File cache = new File(folder, BACKUP_FOLDER);
            File revision = new File(cache, UUID.randomUUID().toString());
            try {
                FileUtils.forceMkdir(revision);
                notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_DOWNLOADING, 0);
                File backup = downloadFile(revision, file);
                notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_RESTORING, 75);
                String password = intent.getStringExtra(PASSWORD);
                restoreLocalBackupFile(revision, backup, password);
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

    protected abstract File downloadFile(File folder, T file) throws Exception;

    private void restoreLocalBackupFile(@NonNull File folder, @NonNull File backup, @Nullable String password) throws Exception {
        AbstractBackupImporter importer;
        String fileName = backup.getName();
        if (fileName.endsWith(BackupManager.BACKUP_EXTENSION_LEGACY)) {
            importer = new LegacyBackupImporter(getContentResolver(), backup);
        } else {
            importer = new DefaultBackupImporter(getContentResolver(), backup, password);
        }
        File temporaryFolder = new File(getFilesDir(), TEMP_FOLDER);
        FileUtils.forceMkdir(temporaryFolder);
        try {
            File databaseFile = getDatabasePath(SQLDatabaseImporter.DATABASE_NAME);
            importer.importDatabase(temporaryFolder, databaseFile.getParentFile());
            importer.importAttachments(getAttachmentFolder());
        } finally {
            FileUtils.cleanDirectory(temporaryFolder);
        }
    }

    protected abstract ArrayList<T> getFolderContent(T folder) throws Exception;

    private void notifyTaskStarted(int action) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_STARTED);
        intent.putExtra(SERVICE_ACTION, action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskProgress(int action, int status, int progress) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_RUNNING);
        intent.putExtra(SERVICE_ACTION, action);
        intent.putExtra(SERVICE_PROGRESS_STATUS, status);
        intent.putExtra(SERVICE_PROGRESS_VALUE, progress);
        mBroadcastManager.sendBroadcast(intent);
    }

    protected void notifyUploadProgress(int percentage) {
        int realProgress = 30 + (percentage * 70 / 100);
        notifyTaskProgress(ACTION_BACKUP, STATUS_BACKUP_UPLOADING, realProgress);
    }

    protected void notifyDownloadProgress(int percentage) {
        int realProgress = (percentage * 70 / 100);
        notifyTaskProgress(ACTION_RESTORE, STATUS_BACKUP_DOWNLOADING, realProgress);
    }

    private void notifyTaskFinished(int action) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(SERVICE_ACTION, action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyListTaskFinished(ArrayList<T> files) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(SERVICE_ACTION, ACTION_LIST);
        intent.putParcelableArrayListExtra(FOLDER_CONTENT, files);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyUploadTaskFinished(T file) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
        intent.putExtra(SERVICE_ACTION, ACTION_BACKUP);
        intent.putExtra(BACKUP_FILE, file);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFailure(int action, String message) {
        Intent intent = new Intent(LocalAction.ACTION_BACKUP_SERVICE_FAILED);
        intent.putExtra(SERVICE_ACTION, action);
        intent.putExtra(SERVICE_ERROR_MESSAGE, message);
        mBroadcastManager.sendBroadcast(intent);
    }
}