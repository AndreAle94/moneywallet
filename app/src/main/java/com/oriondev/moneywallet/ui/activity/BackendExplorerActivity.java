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

package com.oriondev.moneywallet.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.disk.DiskBackendService;
import com.oriondev.moneywallet.api.disk.DiskBackendServiceAPI;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.service.BackendHandlerIntentService;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.BackupFileAdapter;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 26/11/18.
 */
public class BackendExplorerActivity extends SinglePanelActivity implements SwipeRefreshLayout.OnRefreshListener, BackupFileAdapter.Controller {

    public static final String BACKEND_ID = "BackendExplorerActivity::Arguments::BackendId";
    public static final String MODE = "BackendExplorerActivity::Arguments::Mode";

    public static final String RESULT_FILE = "BackendExplorerActivity::Result::File";

    public static final int MODE_EXPLORER = 0;
    public static final int MODE_FILE_PICKER = 1;
    public static final int MODE_FOLDER_PICKER = 2;

    private static final IFile ROOT_FOLDER = null;

    private AdvancedRecyclerView mAdvancedRecyclerView;
    private BackupFileAdapter mAdapter;

    private String mBackendId;
    private int mActivityMode;
    private List<IFile> mFileStack;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_activity_single_panel_body_list, parent, true);
        mAdvancedRecyclerView = view.findViewById(R.id.advanced_recycler_view);
        mAdvancedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdvancedRecyclerView.setEmptyText(R.string.message_no_icon_found);
        mAdapter = new BackupFileAdapter(this);
        mAdvancedRecyclerView.setAdapter(mAdapter);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        // unpack intent info
        Intent intent = getIntent();
        mBackendId = intent.getStringExtra(BACKEND_ID);
        mActivityMode = intent.getIntExtra(MODE, MODE_EXPLORER);
        mFileStack = new ArrayList<>();
        // attach the activity to the service
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalAction.ACTION_BACKEND_SERVICE_STARTED);
        intentFilter.addAction(LocalAction.ACTION_BACKEND_SERVICE_FINISHED);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        loadCurrentFolder();
    }

    @Override
    protected void onSetupFloatingActionButton(FloatingActionButton floatingActionButton) {
        super.onSetupFloatingActionButton(floatingActionButton);
        if (floatingActionButton != null) {
            floatingActionButton.setImageResource(R.drawable.ic_create_new_folder_black_24dp);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    @Override
    protected int getActivityTitleRes() {
        switch (mActivityMode) {
            case MODE_EXPLORER:
                return R.string.title_activity_backend_explorer;
            case MODE_FILE_PICKER:
                return R.string.title_activity_backend_file_picker;
            case MODE_FOLDER_PICKER:
                return R.string.title_activity_backend_folder_picker;
        }
        return 0;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_backend_explorer;
    }

    @Override
    protected void onMenuCreated(Menu menu) {
        switch (mActivityMode) {
            case MODE_EXPLORER:
                menu.findItem(R.id.action_select_folder).setVisible(false);
                break;
            case MODE_FILE_PICKER:
                menu.findItem(R.id.action_select_folder).setVisible(false);
                break;
            case MODE_FOLDER_PICKER:
                menu.findItem(R.id.action_select_folder).setVisible(true);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_folder:
                if (mActivityMode == MODE_FOLDER_PICKER) {
                    IFile folder = getCurrentFolder();
                    if (folder == null) {
                        // return the root folder of the device instead of null
                        folder = DiskBackendServiceAPI.getRootFolder();
                    }
                    Intent intent = new Intent();
                    intent.putExtra(RESULT_FILE, folder);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
        return false;
    }

    @Override
    public void navigateBack() {
        int stackSize = mFileStack.size();
        if (stackSize > 0) {
            mFileStack.remove(stackSize - 1);
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
            loadCurrentFolder();
        }
    }

    @Override
    public void onFileClick(IFile file) {
        if (file.isDirectory()) {
            mFileStack.add(file);
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
            loadFolder(file);
        } else if (mActivityMode == MODE_FILE_PICKER) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE, file);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onRefresh() {
        loadCurrentFolder();
    }

    private IFile getCurrentFolder() {
        return mFileStack.isEmpty() ? ROOT_FOLDER : mFileStack.get(mFileStack.size() - 1);
    }

    private void loadCurrentFolder() {
        loadFolder(getCurrentFolder());
    }

    private void loadFolder(IFile folder) {
        Intent intent = new Intent(this, BackendHandlerIntentService.class);
        intent.putExtra(BackendHandlerIntentService.BACKEND_ID, mBackendId);
        intent.putExtra(BackendHandlerIntentService.ACTION, BackendHandlerIntentService.ACTION_LIST);
        intent.putExtra(BackendHandlerIntentService.PARENT_FOLDER, folder);
        startService(intent);
    }

    @Override
    protected void onFloatingActionButtonClick() {
        ThemedDialog.buildMaterialDialog(this)
                .title(R.string.title_backend_create_folder)
                .content(R.string.message_backend_create_folder)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.hint_new_folder, 0, false, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Intent intent = new Intent(BackendExplorerActivity.this, BackendHandlerIntentService.class);
                        intent.putExtra(BackendHandlerIntentService.BACKEND_ID, mBackendId);
                        intent.putExtra(BackendHandlerIntentService.ACTION, BackendHandlerIntentService.ACTION_CREATE_FOLDER);
                        intent.putExtra(BackendHandlerIntentService.PARENT_FOLDER, getCurrentFolder());
                        intent.putExtra(BackendHandlerIntentService.FOLDER_NAME, input.toString());
                        startService(intent);
                    }

                })
                .show();
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, LocalAction.ACTION_BACKEND_SERVICE_STARTED)) {
                int operation = intent.getIntExtra(BackendHandlerIntentService.ACTION, 0);
                if (operation == BackendHandlerIntentService.ACTION_LIST) {
                    mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
                }
            } else if (TextUtils.equals(action, LocalAction.ACTION_BACKEND_SERVICE_FINISHED)) {
                int operation = intent.getIntExtra(BackendHandlerIntentService.ACTION, 0);
                if (operation == BackendHandlerIntentService.ACTION_LIST) {
                    List<IFile> files = intent.getParcelableArrayListExtra(BackendHandlerIntentService.FOLDER_CONTENT);
                    mAdapter.setFileList(files, mFileStack.size() > 0);
                    if (mAdapter.getItemCount() == 0) {
                        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.EMPTY);
                    } else {
                        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.READY);
                    }
                } else if (operation == BackendHandlerIntentService.ACTION_CREATE_FOLDER) {
                    IFile folder = intent.getParcelableExtra(BackendHandlerIntentService.CREATED_FILE);
                    mAdapter.addFileToList(folder);
                }
            }
        }

    };
}