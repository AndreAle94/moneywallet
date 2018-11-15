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

package com.oriondev.moneywallet.ui.fragment.secondary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;
import com.oriondev.moneywallet.ui.adapter.recycler.BackupFileAdapter;
import com.oriondev.moneywallet.ui.fragment.base.MultiPanelFragment;
import com.oriondev.moneywallet.ui.fragment.base.NavigableFragment;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 21/03/2018.
 */
public abstract class AbstractBackupHandlerFragment<T extends IFile> extends Fragment implements BackupFileAdapter.Controller<T>, SwipeRefreshLayout.OnRefreshListener, Toolbar.OnMenuItemClickListener {

    private static final String ARG_ALLOW_BACKUP = "AbstractBackupHandlerFragment::Arguments::AllowBackup";
    private static final String ARG_ALLOW_RESTORE = "AbstractBackupHandlerFragment::Arguments::AllowRestore";

    private boolean mAllowBackup;
    private boolean mAllowRestore;
    private List<T> mFileStack;

    private AdvancedRecyclerView mAdvancedRecyclerView;
    private BackupFileAdapter<T> mBackupAdapter;

    private View mCoverLayout;
    private View mPrimaryLayout;

    private Toolbar mCoverToolbar;

    private LocalBroadcastManager mLocalBroadcastManager;

    protected static Bundle generateArguments(boolean allowBackup, boolean allowRestore) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ALLOW_BACKUP, allowBackup);
        arguments.putBoolean(ARG_ALLOW_RESTORE, allowRestore);
        return arguments;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mAllowBackup = arguments.getBoolean(ARG_ALLOW_BACKUP, true);
            mAllowRestore = arguments.getBoolean(ARG_ALLOW_RESTORE, true);
            mFileStack = new ArrayList<>();
        } else {
            throw new IllegalStateException("Arguments bundle is null, please instantiate the fragment using the newInstance() method instead.");
        }
        // bind to local broadcast manager to get notified of background operations
        Activity activity = getActivity();
        if (activity != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_STARTED);
            intentFilter.addAction(LocalAction.ACTION_BACKUP_SERVICE_FINISHED);
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity);
            mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_secondary_panel_external_storage_backup_list, container, false);
        mPrimaryLayout = view.findViewById(R.id.primary_layout);
        mCoverLayout = onCreateCoverView(inflater, view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.secondary_toolbar);
        toolbar.setTitle(getTitle());
        Fragment parent = getParentFragment();
        if (parent instanceof MultiPanelFragment && !((MultiPanelFragment) parent).isExtendedLayout()) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Fragment parent = getParentFragment();
                    if (parent instanceof NavigableFragment) {
                        ((NavigableFragment) parent).navigateBack();
                    }
                }

            });
            int menuResId = onInflateMenu();
            if (menuResId > 0) {
                toolbar.inflateMenu(menuResId);
                toolbar.setOnMenuItemClickListener(this);
            }
            if (mCoverToolbar != null) {
                mCoverToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                mCoverToolbar.setNavigationOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Fragment parent = getParentFragment();
                        if (parent instanceof NavigableFragment) {
                            ((NavigableFragment) parent).navigateBack();
                        }
                    }

                });
            }
        }
        mAdvancedRecyclerView = view.findViewById(R.id.advanced_recycler_view);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        mAdvancedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBackupAdapter = new BackupFileAdapter<>(this);
        mAdvancedRecyclerView.setAdapter(mBackupAdapter);
        mAdvancedRecyclerView.setOnRefreshListener(this);
        if (mAllowBackup) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!mFileStack.isEmpty()) {
                        ThemedDialog.buildMaterialDialog(v.getContext())
                                .title(R.string.title_backup_create)
                                .content(R.string.message_backup_create)
                                .negativeText(android.R.string.cancel)
                                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                .input(R.string.hint_password, 0, new MaterialDialog.InputCallback() {

                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        T file = mFileStack.get(mFileStack.size() - 1);
                                        createBackup(file, input.length() == 0 ? null : input.toString());
                                    }

                                })
                                .show();
                    }
                }

            });
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }
        return view;
    }

    protected abstract View onCreateCoverView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected void setCoverToolbar(Toolbar toolbar) {
        mCoverToolbar = toolbar;
    }

    protected void showCoverView() {
        if (mCoverLayout != null) {
            mPrimaryLayout.setVisibility(View.GONE);
            mCoverLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void hideCoverView() {
        if (mCoverLayout != null) {
            mCoverLayout.setVisibility(View.GONE);
            mPrimaryLayout.setVisibility(View.VISIBLE);
        }
    }

    protected abstract int getTitle();

    @MenuRes
    protected int onInflateMenu() {
        return 0;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    protected boolean isStackEmpty() {
        return mFileStack.isEmpty();
    }

    protected void loadRootFolder(T root) {
        mFileStack.add(root);
        loadCurrentFolder();
    }

    public void loadCurrentFolder() {
        loadFolder(mFileStack.isEmpty() ? null : mFileStack.get(mFileStack.size() - 1));
    }

    protected abstract void loadFolder(T folder);

    @Override
    public void navigateBack() {
        int stackSize = mFileStack.size();
        if (stackSize > 1) {
            mFileStack.remove(stackSize - 1);
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
            loadFolder(mFileStack.get(mFileStack.size() - 1));
        }
    }

    @Override
    public void onFileClick(final T file) {
        if (file.isDirectory()) {
            mFileStack.add(file);
            mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
            loadFolder(file);
        } else {
            Activity activity = getActivity();
            if (mAllowRestore && activity != null) {
                if (file.getName().endsWith(BackupManager.BACKUP_EXTENSION_STANDARD)) {
                    ThemedDialog.buildMaterialDialog(activity)
                            .title(R.string.title_backup_restore)
                            .content(R.string.message_backup_restore_standard)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {

                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    restoreBackup(file, null);
                                }

                            })
                            .show();
                } else if (file.getName().endsWith(BackupManager.BACKUP_EXTENSION_PROTECTED)) {
                    ThemedDialog.buildMaterialDialog(activity)
                            .title(R.string.title_backup_restore)
                            .content(R.string.message_backup_restore_protected)
                            .positiveText(android.R.string.ok)
                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                            .input(R.string.hint_password, 0, false, new MaterialDialog.InputCallback() {

                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    restoreBackup(file, input.toString());
                                }

                            })
                            .show();
                } else if (file.getName().endsWith(BackupManager.BACKUP_EXTENSION_LEGACY)) {
                    ThemedDialog.buildMaterialDialog(activity)
                            .title(R.string.title_backup_restore)
                            .content(R.string.message_backup_restore_legacy)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {

                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    restoreBackup(file, null);
                                }

                            })
                            .show();
                }
            }
        }
    }

    protected abstract void createBackup(T folder, String password);

    protected abstract void restoreBackup(T file, String password);

    @Override
    public void onRefresh() {
        int stackSize = mFileStack.size();
        if (stackSize > 0) {
            loadFolder(mFileStack.get(stackSize - 1));
        }
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, LocalAction.ACTION_BACKUP_SERVICE_STARTED)) {
                int operation = intent.getIntExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, 0);
                if (operation == AbstractBackupHandlerIntentService.ACTION_LIST) {
                    mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.LOADING);
                }
            } else if (TextUtils.equals(action, LocalAction.ACTION_BACKUP_SERVICE_FINISHED)) {
                int operation = intent.getIntExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, 0);
                if (operation == AbstractBackupHandlerIntentService.ACTION_LIST) {
                    List<T> files = intent.getParcelableArrayListExtra(AbstractBackupHandlerIntentService.FOLDER_CONTENT);
                    mBackupAdapter.setFileList(files, mFileStack.size() > 1);
                    if (mBackupAdapter.getItemCount() == 0) {
                        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.EMPTY);
                    } else {
                        mAdvancedRecyclerView.setState(AdvancedRecyclerView.State.READY);
                    }
                } else if (operation == AbstractBackupHandlerIntentService.ACTION_BACKUP) {
                    T backup = intent.getParcelableExtra(AbstractBackupHandlerIntentService.BACKUP_FILE);
                    mBackupAdapter.addFileToList(backup);
                }
            }
        }

    };
}