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
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.oriondev.moneywallet.storage.database.DatabaseExporter;
import com.oriondev.moneywallet.storage.database.ExportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseExporter;
import com.oriondev.moneywallet.storage.database.json.JSONDatabaseExporter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by andrea on 28/10/18.
 */
public class DefaultBackupExporter extends AbstractBackupExporter {

    private static final String INTERNAL_BACKUP_DATABASE = BackupManager.FileStructure.FILE_DATABASE;
    
    private final String mPassword;

    public DefaultBackupExporter(ContentResolver contentResolver, File backupFile, String password) throws ExportException {
        super(contentResolver, backupFile);
        mPassword = password;
    }

    @Override
    public void exportDatabase(@NonNull File tempFolder) throws ExportException {
        DatabaseExporter exporter = null;
        File tempFile = new File(tempFolder, INTERNAL_BACKUP_DATABASE);
        try {
            if (!tempFile.exists() && !tempFile.createNewFile()) {
                throw new ExportException("Failed to create temporary file. Probably the internal storage of the device is full.");
            }
            exporter = new JSONDatabaseExporter(new FileOutputStream(tempFile));
            ContentResolver contentResolver = getContentResolver();
            exporter.exportHeader();
            exporter.exportCurrencies(SQLDatabaseExporter.getAllCurrencies(contentResolver));
            exporter.exportWallets(SQLDatabaseExporter.getAllWallets(contentResolver));
            exporter.exportCategories(SQLDatabaseExporter.getAllCategories(contentResolver));
            exporter.exportEvents(SQLDatabaseExporter.getAllEvents(contentResolver));
            exporter.exportPlaces(SQLDatabaseExporter.getAllPlaces(contentResolver));
            exporter.exportPeople(SQLDatabaseExporter.getAllPeople(contentResolver));
            exporter.exportEventPeople(SQLDatabaseExporter.getAllEventPeople(contentResolver));
            exporter.exportDebts(SQLDatabaseExporter.getAllDebt(contentResolver));
            exporter.exportDebtPeople(SQLDatabaseExporter.getAllDebtPeople(contentResolver));
            exporter.exportBudgets(SQLDatabaseExporter.getAllBudget(contentResolver));
            exporter.exportBudgetWallets(SQLDatabaseExporter.getAllBudgetWallets(contentResolver));
            exporter.exportSavings(SQLDatabaseExporter.getAllSavings(contentResolver));
            exporter.exportRecurrentTransactions(SQLDatabaseExporter.getAllRecurrentTransactions(contentResolver));
            exporter.exportRecurrentTransfers(SQLDatabaseExporter.getAllRecurrentTransfers(contentResolver));
            exporter.exportTransactions(SQLDatabaseExporter.getAllTransactions(contentResolver));
            exporter.exportTransactionPeople(SQLDatabaseExporter.getAllTransactionPeople(contentResolver));
            exporter.exportTransactionModels(SQLDatabaseExporter.getAllTransactionModels(contentResolver));
            exporter.exportTransfers(SQLDatabaseExporter.getAllTransfers(contentResolver));
            exporter.exportTransferPeople(SQLDatabaseExporter.getAllTransferPeople(contentResolver));
            exporter.exportTransferModels(SQLDatabaseExporter.getAllTransferModels(contentResolver));
            exporter.exportAttachments(SQLDatabaseExporter.getAllAttachments(contentResolver));
            exporter.exportTransactionAttachments(SQLDatabaseExporter.getAllTransactionAttachments(contentResolver));
            exporter.exportTransferAttachments(SQLDatabaseExporter.getAllTransferAttachments(contentResolver));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExportException(e.getMessage());
        } finally {
            if (exporter != null) {
                try {
                    exporter.close();
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        try {
            ZipFile zipFile = new ZipFile(getBackupFile());
            zipFile.addFile(tempFile, generateZipParameters(BackupManager.FileStructure.FOLDER_DATABASES));
        } catch (ZipException e) {
            throw new ExportException(e.getMessage());
        }
        FileUtils.deleteQuietly(tempFile);
    }

    @Override
    protected void exportAttachmentFiles(@NonNull List<File> attachmentList) throws ExportException {
        try {
            ZipFile zipFile = new ZipFile(getBackupFile());
            if (!zipFile.isValidZipFile()) {
                throw new ExportException("Invalid backup file: not a zip file");
            }
            ZipParameters zipParameters = generateZipParameters(BackupManager.FileStructure.FOLDER_ATTACHMENTS);
            for (File attachment : attachmentList) {
                zipFile.addFile(attachment, zipParameters);
            }
        } catch (ZipException e) {
            throw new ExportException(e.getMessage());
        }
    }

    private ZipParameters generateZipParameters(@NonNull String root) {
        ZipParameters parameters = new ZipParameters();
        parameters.setRootFolderInZip(root);
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (!TextUtils.isEmpty(mPassword)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            parameters.setPassword(mPassword);
        }
        return parameters;
    }
}