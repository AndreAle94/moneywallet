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

package com.oriondev.moneywallet.service.disk;

import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by andrea on 28/03/18.
 */
public class DiskBackupHandlerIntentService extends AbstractBackupHandlerIntentService<LocalFile> {

    public DiskBackupHandlerIntentService() {
        super("DiskBackupHandlerIntentService");
    }

    @Override
    protected LocalFile uploadFile(LocalFile folder, File backup) throws Exception {
        File destination = new File(folder.getFile(), backup.getName());
        if (!destination.exists() && !destination.createNewFile()) {
            throw new IOException("Failed to create the file on disk");
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(backup);
            outputStream = new FileOutputStream(destination);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            if (inputStream != null) { try { inputStream.close(); } catch (IOException ignore) {} }
            if (outputStream != null) { try { outputStream.close(); } catch (IOException ignore) {} }
        }
        return new LocalFile(destination);
    }

    @Override
    protected File downloadFile(File folder, LocalFile file) {
        return file.getFile();
    }

    @Override
    protected ArrayList<LocalFile> getFolderContent(LocalFile folder) throws Exception {
        return folder.getChildren();
    }
}