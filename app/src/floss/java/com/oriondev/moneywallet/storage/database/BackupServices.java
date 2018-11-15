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

package com.oriondev.moneywallet.storage.database;

import android.support.v4.app.Fragment;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.BackupService;
import com.oriondev.moneywallet.ui.fragment.secondary.ExternalMemoryBackupHandlerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 17/10/18.
 */

public class BackupServices {

    private static final String SERVICE_ID_EXTERNAL_MEMORY = "ExternalMemory";

    public static List<BackupService> getBackupServices() {
        List<BackupService> services = new ArrayList<>();
        services.add(new BackupService(SERVICE_ID_EXTERNAL_MEMORY, R.drawable.ic_sd_24dp, R.string.service_backup_external_memory));
        return services;
    }

    public static Fragment getBackupServiceFragment(BackupService service, boolean allowBackup, boolean allowRestore) {
        switch (service.getIdentifier()) {
            case SERVICE_ID_EXTERNAL_MEMORY:
                return ExternalMemoryBackupHandlerFragment.newInstance(allowBackup, allowRestore);
            default:
                throw new IllegalArgumentException("Unknown service");
        }
    }
}
