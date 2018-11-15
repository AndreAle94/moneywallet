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

package com.oriondev.moneywallet.service.google;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Tasks;
import com.oriondev.moneywallet.model.GoogleDriveFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by andrea on 27/07/18.
 */
public class GoogleDriveBackupHandlerIntentService extends AbstractBackupHandlerIntentService<GoogleDriveFile> {

    private DriveResourceClient mDriveResourceClient;

    public GoogleDriveBackupHandlerIntentService() {
        super("GoogleDriveBackupHandlerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (signInAccount != null) {
                mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
            } else {
                // no account linked to google drive
                return;
            }
        }
        super.onHandleIntent(intent);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources") // not supported by lower sdk
    @Override
    protected GoogleDriveFile uploadFile(GoogleDriveFile folder, File backup) throws Exception {
        InputStream inputStream = new ProgressInputStream(backup, new ProgressInputStream.UploadProgressListener() {

            @Override
            public void onUploadProgressUpdate(int percentage) {
                notifyUploadProgress(percentage);
            }

        });
        try {
            DriveContents driveContents = Tasks.await(mDriveResourceClient.createContents());
            OutputStream outputStream = driveContents.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(backup.getName())
                    .setMimeType("application/moneywallet")
                    .setStarred(true)
                    .build();
            DriveFile driveFile = Tasks.await(mDriveResourceClient.createFile(folder.getDriveFolder(), changeSet, driveContents));
            return new GoogleDriveFile(Tasks.await(mDriveResourceClient.getMetadata(driveFile)));
        } finally {
            inputStream.close();
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources") // not supported by lower sdk
    @Override
    protected File downloadFile(File folder, GoogleDriveFile file) throws Exception {
        File destination = new File(folder, file.getName());
        OutputStream outputStream = new ProgressOutputStream(destination, file.getSize(), new ProgressOutputStream.DownloadProgressListener() {

            @Override
            public void onDownloadProgressUpdate(int percentage) {
                notifyDownloadProgress(percentage);
            }

        });
        try {
            DriveContents driveContents = Tasks.await(mDriveResourceClient.openFile(file.getDriveFile(), DriveFile.MODE_READ_ONLY));
            InputStream inputStream = driveContents.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
        } finally {
            outputStream.close();
        }
        return destination;
    }

    @Override
    protected ArrayList<GoogleDriveFile> getFolderContent(GoogleDriveFile folder) throws Exception {
        ArrayList<GoogleDriveFile> fileArrayList = new ArrayList<>();
        MetadataBuffer buffer = Tasks.await(mDriveResourceClient.listChildren(folder.getDriveFolder()));
        for (Metadata metadata : buffer) {
            fileArrayList.add(new GoogleDriveFile(metadata));
        }
        buffer.release();
        return fileArrayList;
    }
}