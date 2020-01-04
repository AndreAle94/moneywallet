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

package com.oriondev.moneywallet.api.dropbox;

import android.content.Context;
import androidx.annotation.NonNull;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.api.AbstractBackendServiceAPI;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.model.DropBoxFile;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrea on 24/11/18.
 */
public class DropboxBackendServiceAPI extends AbstractBackendServiceAPI<DropBoxFile> {

    private final DbxClientV2 mDropBoxClient;

    public DropboxBackendServiceAPI(Context context) throws BackendException {
        super(DropBoxFile.class);
        String accessToken = DropboxBackendService.getAccessToken(context);
        if (accessToken != null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder(BuildConfig.API_KEY_DROPBOX)
                    // we can lower the read timeout to 20 seconds to avoid to have the service locked
                    // when the device goes offline and the library is performing http requests.
                    .withHttpRequestor(new StandardHttpRequestor(StandardHttpRequestor.Config.builder()
                            .withReadTimeout(20, TimeUnit.SECONDS)
                            .build())
                    )
                    .build();
            mDropBoxClient = new DbxClientV2(config, accessToken);
        } else {
            throw new BackendException("Dropbox backend cannot be initialized: missing access token");
        }
    }

    @Override
    public DropBoxFile upload(DropBoxFile folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException {
        try {
            String path = getFolderPath(folder) + "/" + file.getName();
            FileMetadata metadata = mDropBoxClient.files().uploadBuilder(path)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(new ProgressInputStream(file, listener));
            return new DropBoxFile(metadata);
        } catch (IOException | DbxException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    @Override
    public File download(File folder, @NonNull DropBoxFile file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException {
        try {
            File destination = new File(folder, file.getName());
            mDropBoxClient.files().downloadBuilder(file.getPath())
                    .download(new ProgressOutputStream(destination, file.getSize(), listener));
            return destination;
        } catch (IOException | DbxException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    @Override
    public List<IFile> list(DropBoxFile folder) throws BackendException {
        try {
            List<IFile> files = new ArrayList<>();
            ListFolderResult result = mDropBoxClient.files().listFolder(getFolderPath(folder));
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    files.add(new DropBoxFile(metadata));
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = mDropBoxClient.files().listFolderContinue(result.getCursor());
            }
            return files;
        } catch (DbxException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    @Override
    protected DropBoxFile newFolder(DropBoxFile parent, String name) throws BackendException {
        String path = getFolderPath(parent) + "/" + name;
        try {
            FolderMetadata metadata = mDropBoxClient.files()
                    .createFolderV2(path)
                    .getMetadata();
            return new DropBoxFile(metadata);
        } catch (DbxException e) {
            throw new BackendException(e.getMessage(), isRecoverable(e));
        }
    }

    private String getFolderPath(DropBoxFile folder) {
        if (folder == null) {
            return "";
        }
        return folder.getPath();
    }

    private boolean isRecoverable(Exception exception) {
        return exception instanceof IOException || exception instanceof NetworkIOException;
    }
}