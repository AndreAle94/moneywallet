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

package com.oriondev.moneywallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.api.IBackendServiceAPI;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.Utils;

import java.util.List;

/**
 * Created by andrea on 26/11/18.
 */
public class BackendHandlerIntentService extends IntentService {

    public static final String ACTION = "BackendHandlerIntentService::Argument::Action";
    public static final String BACKEND_ID = "BackendHandlerIntentService::Argument::BackendId";
    public static final String PARENT_FOLDER = "BackendHandlerIntentService::Argument::ParentFolder";
    public static final String ERROR_MESSAGE = "BackendHandlerIntentService::Argument::ErrorMessage";
    public static final String FOLDER_CONTENT = "BackendHandlerIntentService::Argument::FolderContent";
    public static final String FOLDER_NAME = "BackendHandlerIntentService::Argument::FolderName";
    public static final String CREATED_FILE = "BackendHandlerIntentService::Argument::CreatedFile";

    public static final int ACTION_LIST = 0;
    public static final int ACTION_CREATE_FOLDER = 1;

    private String mBackendId;

    private LocalBroadcastManager mBroadcastManager;

    public BackendHandlerIntentService() {
        super("BackendHandlerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(BACKEND_ID)) {
            mBroadcastManager = LocalBroadcastManager.getInstance(this);
            mBackendId = intent.getStringExtra(BACKEND_ID);
            switch (intent.getIntExtra(ACTION, ACTION_LIST)) {
                case ACTION_LIST:
                    onActionList(intent);
                    break;
                case ACTION_CREATE_FOLDER:
                    onActionCreateFolder(intent);
                    break;
            }
        }
    }

    private void onActionList(@NonNull Intent intent) {
        notifyTaskStarted(ACTION_LIST);
        IFile remoteFolder = intent.getParcelableExtra(PARENT_FOLDER);
        try {
            IBackendServiceAPI api = BackendServiceFactory.getServiceAPIById(this, mBackendId);
            List<IFile> fileList = api.getFolderContent(remoteFolder);
            notifyListTaskFinished(fileList);
        } catch (BackendException e) {
            notifyTaskFailure(ACTION_LIST, e.getMessage());
        }
    }

    private void onActionCreateFolder(@NonNull Intent intent) {
        notifyTaskStarted(ACTION_CREATE_FOLDER);
        IFile remoteFolder = intent.getParcelableExtra(PARENT_FOLDER);
        String folderName = intent.getStringExtra(FOLDER_NAME);
        try {
            IBackendServiceAPI api = BackendServiceFactory.getServiceAPIById(this, mBackendId);
            IFile createdFolder = api.createFolder(remoteFolder, folderName);
            notifyCreateFolderTaskFinished(createdFolder);
        } catch (BackendException e) {
            notifyTaskFailure(ACTION_CREATE_FOLDER, e.getMessage());
        }
    }

    private void notifyTaskStarted(int action) {
        Intent intent = new Intent(LocalAction.ACTION_BACKEND_SERVICE_STARTED);
        intent.putExtra(ACTION, action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyListTaskFinished(List<IFile> files) {
        Intent intent = new Intent(LocalAction.ACTION_BACKEND_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_LIST);
        intent.putParcelableArrayListExtra(FOLDER_CONTENT, Utils.wrapAsArrayList(files));
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyCreateFolderTaskFinished(IFile file) {
        Intent intent = new Intent(LocalAction.ACTION_BACKEND_SERVICE_FINISHED);
        intent.putExtra(ACTION, ACTION_CREATE_FOLDER);
        intent.putExtra(CREATED_FILE, file);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFailure(int action, String message) {
        Intent intent = new Intent(LocalAction.ACTION_BACKEND_SERVICE_FAILED);
        intent.putExtra(ACTION, action);
        intent.putExtra(ERROR_MESSAGE, message);
        mBroadcastManager.sendBroadcast(intent);
    }
}