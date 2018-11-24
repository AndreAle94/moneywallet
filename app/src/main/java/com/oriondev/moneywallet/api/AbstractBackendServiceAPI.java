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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import java.io.File;
import java.util.List;

/**
 * Created by andrea on 24/11/18.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBackendServiceAPI<T extends IFile> implements IBackendServiceAPI {

    private final Class<T> mType;

    public AbstractBackendServiceAPI(Class<T> type) throws BackendException {
        mType = type;
    }

    @Override
    public IFile uploadFile(IFile folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException {
        if (folder == null || mType.isInstance(file)) {
            return upload((T) folder, file, listener);
        } else {
            throw new BackendException("Backend cannot upload a file to a folder that is not an instance of " + mType.getName());
        }
    }

    protected abstract T upload(@Nullable T folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException;

    @Override
    public File downloadFile(File folder, IFile file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException {
        if (mType.isInstance(file)) {
            return download(folder, (T) file, listener);
        } else {
            throw new BackendException("Backend cannot download a file that is not an instance of " + mType.getName());
        }
    }

    protected abstract File download(File folder, @NonNull T file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException;

    @Override
    public List<IFile> getFolderContent(IFile folder) throws BackendException {
        if (folder == null || mType.isInstance(folder)) {
            return list((T) folder);
        } else {
            throw new BackendException("Backend cannot list the content of a folder that is not an instance of " + mType.getName());
        }
    }

    protected abstract List<IFile> list(@Nullable T folder) throws BackendException;
}