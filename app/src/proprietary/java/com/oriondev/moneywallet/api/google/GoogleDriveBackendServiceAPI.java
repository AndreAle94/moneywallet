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

package com.oriondev.moneywallet.api.google;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Tasks;
import com.oriondev.moneywallet.api.AbstractBackendServiceAPI;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.model.GoogleDriveFile;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by andrea on 24/11/18.
 */
public class GoogleDriveBackendServiceAPI extends AbstractBackendServiceAPI<GoogleDriveFile> {

    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private final DriveResourceClient mDriveResourceClient;

    public GoogleDriveBackendServiceAPI(Context context) throws BackendException {
        super(GoogleDriveFile.class);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if (signInAccount != null) {
            mDriveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
        } else {
            throw new BackendException("GoogleDrive backend cannot be initialized: account is null");
        }
    }

    @Override
    public GoogleDriveFile upload(GoogleDriveFile folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException {
        try (InputStream inputStream = new ProgressInputStream(file, listener)) {
            DriveContents driveContents = Tasks.await(mDriveResourceClient.createContents());
            OutputStream outputStream = driveContents.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(file.getName())
                    .setStarred(true)
                    .build();
            DriveFolder driveFolder = getFolder(folder);
            DriveFile driveFile = Tasks.await(mDriveResourceClient.createFile(driveFolder, changeSet, driveContents));
            return new GoogleDriveFile(Tasks.await(mDriveResourceClient.getMetadata(driveFile)));
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    @Override
    public File download(File folder, @NonNull GoogleDriveFile file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException {
        File destination = new File(folder, file.getName());
        try (OutputStream outputStream = new ProgressOutputStream(destination, file.getSize(), listener)) {
            DriveFile driveFile = file.getDriveFile();
            DriveContents driveContents = Tasks.await(mDriveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY));
            InputStream inputStream = driveContents.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
        return destination;
    }

    @Override
    public List<IFile> list(GoogleDriveFile folder) throws BackendException {
        List<IFile> fileList = new ArrayList<>();
        try {
            DriveFolder driveFolder = getFolder(folder);
            MetadataBuffer buffer = Tasks.await(mDriveResourceClient.listChildren(driveFolder));
            for (Metadata metadata : buffer) {
                fileList.add(new GoogleDriveFile(metadata));
            }
            buffer.release();
        } catch (InterruptedException | ExecutionException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
        return fileList;
    }

    @Override
    protected GoogleDriveFile newFolder(GoogleDriveFile parent, String name) throws BackendException {
        try {
            DriveFolder driveFolder = getFolder(parent);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(name)
                    .setMimeType(MIME_TYPE_FOLDER)
                    .setStarred(true)
                    .build();
            DriveFolder folder = Tasks.await(mDriveResourceClient.createFolder(driveFolder, changeSet));
            return new GoogleDriveFile(Tasks.await(mDriveResourceClient.getMetadata(folder)));
        } catch (ExecutionException | InterruptedException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    private DriveFolder getFolder(GoogleDriveFile folder) throws BackendException {
        if (folder == null) {
            try {
                return Tasks.await(mDriveResourceClient.getAppFolder());
            } catch (ExecutionException | InterruptedException e) {
                throw new BackendException(e.getMessage(), isRecoverable(e));
            }
        }
        return folder.getDriveFolder();
    }

    private boolean isRecoverable(Exception e) {
        return e instanceof IOException;
    }
}