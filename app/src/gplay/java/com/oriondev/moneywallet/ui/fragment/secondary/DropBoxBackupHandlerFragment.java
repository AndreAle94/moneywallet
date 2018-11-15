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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.service.dropbox.DropBoxBackupHandlerIntentService;
import com.oriondev.moneywallet.model.DropBoxFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 21/03/2018.
 */
public class DropBoxBackupHandlerFragment extends AbstractBackupHandlerFragment<DropBoxFile> {

    public static DropBoxBackupHandlerFragment newInstance(boolean allowBackup, boolean allowRestore) {
        DropBoxBackupHandlerFragment fragment = new DropBoxBackupHandlerFragment();
        fragment.setArguments(generateArguments(allowBackup, allowRestore));
        return fragment;
    }

    private DbxClientV2 mDropBoxClient;
    private boolean mActivityLaunched = false;

    @Override
    protected View onCreateCoverView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_cover_text_button, container, true);
        setCoverToolbar((Toolbar) view.findViewById(R.id.cover_toolbar));
        TextView coverTextView = view.findViewById(R.id.cover_text_view);
        Button coverActionButton = view.findViewById(R.id.cover_action_button);
        // personalize the views
        coverTextView.setText(R.string.cover_message_backup_dropbox_title);
        coverActionButton.setText(R.string.cover_message_backup_dropbox_button);
        coverActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Auth.startOAuth2Authentication(activity, BuildConfig.API_KEY_DROPBOX);
                }
            }

        });
        // return the reference to the cover layout
        return view.findViewById(R.id.cover_layout);
    }

    @Override
    protected int getTitle() {
        return R.string.service_backup_drop_box;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_backup_service_dropbox;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_disconnect:
                disconnectService(getActivity());
                break;
        }
        return false;
    }

    private void disconnectService(Activity activity) {
        ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.title_warning)
                .content(R.string.message_backup_service_dropbox_disconnect)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        destroyAccountInformation(getActivity());
                        showCoverView();
                    }

                })
                .show();
    }

    private void destroyAccountInformation(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(DropBoxBackupHandlerIntentService.DROPBOX_FILE, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIfUserIsLoggedIn(getActivity());
    }

    private void checkIfUserIsLoggedIn(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(DropBoxBackupHandlerIntentService.DROPBOX_FILE, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(DropBoxBackupHandlerIntentService.KEY_ACCESS_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(DropBoxBackupHandlerIntentService.KEY_ACCESS_TOKEN, accessToken).apply();
                initAndLoadData(accessToken);
            } else {
                showCoverView();
            }
        } else {
            initAndLoadData(accessToken);
        }
        String uid = Auth.getUid();
        String storedUid = prefs.getString(DropBoxBackupHandlerIntentService.KEY_USER_ID, null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString(DropBoxBackupHandlerIntentService.KEY_USER_ID, uid).apply();
        }
    }

    private void initAndLoadData(String accessToken) {
        if (mDropBoxClient == null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder(BuildConfig.API_KEY_DROPBOX).build();
            mDropBoxClient = new DbxClientV2(config, accessToken);
        }
        hideCoverView();
        loadRootFolder(new DropBoxFile(new Metadata("root", "", "", null)));
    }

    @Override
    protected void loadFolder(DropBoxFile folder) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DropBoxBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_LIST);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            activity.startService(intent);
        }
    }

    @Override
    protected void createBackup(DropBoxFile folder, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DropBoxBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_BACKUP);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }

    @Override
    protected void restoreBackup(DropBoxFile file, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DropBoxBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_RESTORE);
            intent.putExtra(AbstractBackupHandlerIntentService.BACKUP_FILE, file);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }
}