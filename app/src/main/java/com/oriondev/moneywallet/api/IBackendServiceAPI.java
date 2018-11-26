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

import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import java.io.File;
import java.util.List;

/**
 * Created by andrea on 22/11/18.
 */
public interface IBackendServiceAPI {

    IFile uploadFile(IFile folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException;

    File downloadFile(File folder, IFile file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException;

    List<IFile> getFolderContent(IFile folder) throws BackendException;

    IFile createFolder(IFile parent, String name) throws BackendException;
}