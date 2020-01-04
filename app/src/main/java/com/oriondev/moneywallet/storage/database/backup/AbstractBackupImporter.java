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

package com.oriondev.moneywallet.storage.database.backup;

import android.content.ContentResolver;
import android.content.Context;
import androidx.annotation.NonNull;

import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.ImportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.SyncContentProvider;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by andrea on 25/10/18.
 */
public abstract class AbstractBackupImporter {

    private static final String TEMP_BACKUP_FILE = "database.bk";

    private final Context mContext;
    private final File mBackupFile;

    /*package-local*/ AbstractBackupImporter(Context context, File backupFile) {
        mContext = context;
        mBackupFile = backupFile;
    }

    /**
     * This method imports all the data included in the backup file inside the main database.
     * The current database is backed-up before starting the import procedure and restored if
     * something goes wrong. The old data is then permanently removed if success.
     * @param temporaryFolder folder where temporary data can be stored.
     * @param databaseFolder folder where the database is located.
     * @throws ImportException if an error occur while importing the backup file.
     */
    public void importDatabase(@NonNull File temporaryFolder, @NonNull File databaseFolder) throws ImportException {
        File temporary = createBackupCopyOfCurrentDatabase(databaseFolder);
        try {
            importDatabase(temporaryFolder);
        } catch (ImportException e) {
            restoreBackupCopyOfDatabase(databaseFolder, temporary);
            throw e;
        } finally {
            FileUtils.deleteQuietly(temporary);
        }
    }

    protected abstract void importDatabase(@NonNull File temporaryFolder) throws ImportException;

    public void importAttachments(@NonNull File attachmentFolder) throws ImportException {
        try {
            if (attachmentFolder.exists()) {
                FileUtils.cleanDirectory(attachmentFolder);
            } else {
                FileUtils.forceMkdir(attachmentFolder);
            }
            importAttachmentFiles(attachmentFolder);
        } catch (IOException e) {
            throw new ImportException(e.getMessage());
        }
    }

    protected abstract void importAttachmentFiles(File attachmentFolder) throws IOException, ImportException;

    /*package-local*/ File getBackupFile() {
        return mBackupFile;
    }

    protected ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }

    /*package-local*/ void notifyImportStarted() {
        // Before starting to write the database file, it is necessary to notify both the providers
        // to ensure that they point to the new file (and not the old reference)
        DataContentProvider.notifyDatabaseIsChanged(mContext);
        SyncContentProvider.notifyDatabaseIsChanged(mContext);
    }

    private File createBackupCopyOfCurrentDatabase(@NonNull File databaseFolder) throws ImportException {
        File temporary = new File(databaseFolder, TEMP_BACKUP_FILE);
        if (temporary.exists()) {
            FileUtils.deleteQuietly(temporary);
        }
        File database = new File(databaseFolder, SQLDatabaseImporter.DATABASE_NAME);
        if (database.exists() && !database.renameTo(temporary)) {
            throw new ImportException("Cannot backup the old database file");
        }
        return temporary;
    }

    private void restoreBackupCopyOfDatabase(@NonNull File databaseFolder, @NonNull File backup) throws ImportException {
        File database = new File(databaseFolder, SQLDatabaseImporter.DATABASE_NAME);
        if (database.exists()) {
            FileUtils.deleteQuietly(database);
        }
        if (backup.exists() && !backup.renameTo(database)) {
            throw new ImportException("Rollback failed, all data is lost");
        }
    }
}