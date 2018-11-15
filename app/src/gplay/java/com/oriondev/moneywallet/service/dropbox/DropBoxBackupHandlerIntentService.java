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

package com.oriondev.moneywallet.service.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.model.DropBoxFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by andre on 22/03/2018.
 */
public class DropBoxBackupHandlerIntentService extends AbstractBackupHandlerIntentService<DropBoxFile> implements ProgressInputStream.UploadProgressListener, ProgressOutputStream.DownloadProgressListener {

    public static final String DROPBOX_FILE = "service_dropbox";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_USER_ID = "user_id";

    private DbxClientV2 mDropBoxClient;

    public DropBoxBackupHandlerIntentService() {
        super("DropBoxBackupHandlerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            SharedPreferences prefs = getSharedPreferences(DropBoxBackupHandlerIntentService.DROPBOX_FILE, Context.MODE_PRIVATE);
            String accessToken = prefs.getString(DropBoxBackupHandlerIntentService.KEY_ACCESS_TOKEN, null);
            if (accessToken != null) {
                DbxRequestConfig config = DbxRequestConfig.newBuilder(BuildConfig.API_KEY_DROPBOX).build();
                mDropBoxClient = new DbxClientV2(config, accessToken);
            }
        }
        super.onHandleIntent(intent);
    }

    @Override
    protected DropBoxFile uploadFile(DropBoxFile folder, File backup) throws Exception {
        try {
            String path = folder.getPath() + "/" + backup.getName();
            FileMetadata metadata = mDropBoxClient.files().uploadBuilder(path)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(new ProgressInputStream(backup, this));
            return new DropBoxFile(metadata);
        } catch (DbxException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    protected File downloadFile(File folder, DropBoxFile file) throws Exception {
        try {
            File destination = new File(folder, file.getName());
            mDropBoxClient.files().downloadBuilder(file.getPath())
                    .download(new ProgressOutputStream(destination, file.getSize(), this));
            return destination;
        } catch (DbxException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    protected ArrayList<DropBoxFile> getFolderContent(DropBoxFile folder) throws Exception {
        if (mDropBoxClient != null) {
            ArrayList<DropBoxFile> files = new ArrayList<>();
            ListFolderResult result = mDropBoxClient.files().listFolder(folder != null ? folder.getPath() : "");
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
        } else {
            throw new RuntimeException("Dropbox client not ready, maybe the user is not logged in or this application is not authorized to access the user account");
        }
    }

    @Override
    public void onUploadProgressUpdate(int percentage) {
        notifyUploadProgress(percentage);
    }

    @Override
    public void onDownloadProgressUpdate(int percentage) {
        notifyDownloadProgress(percentage);
    }
}