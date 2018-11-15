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

import android.support.v4.app.Fragment;

import com.oriondev.moneywallet.model.BackupService;
import com.oriondev.moneywallet.storage.database.BackupServices;

import java.util.List;

/**
 * Created by andre on 21/03/2018.
 */
public class BackupManager {

    public static final String BACKUP_EXTENSION_LEGACY = ".mwb";
    public static final String BACKUP_EXTENSION_STANDARD = ".mwbx";
    public static final String BACKUP_EXTENSION_PROTECTED = ".mwbs";

    /*package-local*/ static final class FileStructure {
        /*package-local*/ static final String ENCODING = "UTF-8";
        /*package-local*/ static final String FILE_DATABASE = "database.json";
        /*package-local*/ static final String FOLDER_DATABASES = "databases/";
        /*package-local*/ static final String FOLDER_ATTACHMENTS = "attachments/";
    }

    public static List<BackupService> getBackupServices() {
        return BackupServices.getBackupServices();
    }

    public static Fragment getBackupServicePanel(BackupService service, boolean allowBackup, boolean allowRestore) {
        return BackupServices.getBackupServiceFragment(service, allowBackup, allowRestore);
    }

    public static String getExtension(boolean encrypted) {
        return encrypted ? BACKUP_EXTENSION_PROTECTED : BACKUP_EXTENSION_STANDARD;
    }
}