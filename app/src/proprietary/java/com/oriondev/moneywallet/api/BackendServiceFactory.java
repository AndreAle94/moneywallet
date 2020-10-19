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

package com.oriondev.moneywallet.api;

import android.content.Context;
import android.os.Build;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.disk.DiskBackendService;
import com.oriondev.moneywallet.api.disk.DiskBackendServiceAPI;
import com.oriondev.moneywallet.api.dropbox.DropboxBackendService;
import com.oriondev.moneywallet.api.dropbox.DropboxBackendServiceAPI;
import com.oriondev.moneywallet.api.google.GoogleDriveBackendService;
import com.oriondev.moneywallet.api.google.GoogleDriveBackendServiceAPI;
import com.oriondev.moneywallet.api.saf.SAFBackendService;
import com.oriondev.moneywallet.api.saf.SAFBackendServiceAPI;
import com.oriondev.moneywallet.model.BackupService;
import com.oriondev.moneywallet.model.DropBoxFile;
import com.oriondev.moneywallet.model.GoogleDriveFile;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.model.SAFFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 21/11/18.
 */
public class BackendServiceFactory {

    public static final String SERVICE_ID_DROPBOX = "dropbox";
    public static final String SERVICE_ID_GOOGLE_DRIVE = "google_drive";
    public static final String SERVICE_ID_EXTERNAL_MEMORY = "external_memory";
    public static final String SERVICE_ID_SAF = "storage_access_framework";

    public static AbstractBackendServiceDelegate getServiceById(String backendId, AbstractBackendServiceDelegate.BackendServiceStatusListener listener) {
        switch (backendId) {
            case SERVICE_ID_DROPBOX:
                return new DropboxBackendService(listener);
            case SERVICE_ID_GOOGLE_DRIVE:
                return new GoogleDriveBackendService(listener);
            case SERVICE_ID_EXTERNAL_MEMORY:
                return new DiskBackendService(listener);
            case SERVICE_ID_SAF:
                return new SAFBackendService(listener);
        }
        return null;
    }

    public static IBackendServiceAPI getServiceAPIById(Context context, String backendId) throws BackendException {
        switch (backendId) {
            case SERVICE_ID_DROPBOX:
                return new DropboxBackendServiceAPI(context);
            case SERVICE_ID_GOOGLE_DRIVE:
                return new GoogleDriveBackendServiceAPI(context);
            case SERVICE_ID_EXTERNAL_MEMORY:
                return new DiskBackendServiceAPI();
            case SERVICE_ID_SAF:
                return new SAFBackendServiceAPI(context);
            default:
                throw new BackendException("Invalid backend");
        }
    }

    public static List<BackupService> getBackupServices() {
        List<BackupService> services = new ArrayList<>();
        services.add(new BackupService(SERVICE_ID_DROPBOX, R.drawable.ic_dropbox_24dp, R.string.service_backup_drop_box));
        services.add(new BackupService(SERVICE_ID_GOOGLE_DRIVE, R.drawable.ic_google_drive_24dp, R.string.service_backup_google_drive));
        services.add(new BackupService(SERVICE_ID_EXTERNAL_MEMORY, R.drawable.ic_sd_24dp, R.string.service_backup_external_memory));
        if (Build.VERSION.SDK_INT >= 21) {
            services.add(new BackupService(SERVICE_ID_SAF, R.drawable.ic_storage_black_24dp, R.string.service_backup_storage_access_framework));
        }
        return services;
    }

    public static IFile getFile(String backendId, String encoded) {
        if (encoded != null) {
            switch (backendId) {
                case SERVICE_ID_DROPBOX:
                    return new DropBoxFile(encoded);
                case SERVICE_ID_GOOGLE_DRIVE:
                    return new GoogleDriveFile(encoded);
                case SERVICE_ID_EXTERNAL_MEMORY:
                    return new LocalFile(encoded);
                case SERVICE_ID_SAF:
                    return new SAFFile(encoded);
            }
        }
        return null;
    }
}