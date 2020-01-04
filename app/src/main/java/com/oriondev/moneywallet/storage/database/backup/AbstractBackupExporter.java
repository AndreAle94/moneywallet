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
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.oriondev.moneywallet.storage.database.ExportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseExporter;
import com.oriondev.moneywallet.storage.database.model.Attachment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 28/10/18.
 */
public abstract class AbstractBackupExporter {

    private final ContentResolver mContentResolver;
    private final File mBackupFile;

    /*package-local*/ AbstractBackupExporter(ContentResolver contentResolver, File backupFile) {
        mContentResolver = contentResolver;
        mBackupFile = backupFile;
    }

    public abstract void exportDatabase(@NonNull File tempFolder) throws ExportException;

    public void exportAttachments(@NonNull File attachmentFolder) throws ExportException {
        Cursor cursor = SQLDatabaseExporter.getAllAttachments(mContentResolver);
        if (cursor != null) {
            List<File> fileList = new ArrayList<>();
            while (cursor.moveToNext()) {
                Attachment attachment = SQLDatabaseExporter.getAttachment(cursor);
                File file = new File(attachmentFolder, attachment.mFile);
                if (file.exists()) {
                    fileList.add(file);
                }
            }
            cursor.close();
            exportAttachmentFiles(fileList);
        }
    }

    protected abstract void exportAttachmentFiles(@NonNull List<File> fileList) throws ExportException;

    /*package-local*/ File getBackupFile() {
        return mBackupFile;
    }

    protected ContentResolver getContentResolver() {
        return mContentResolver;
    }
}