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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.service.disk.DiskBackupHandlerIntentService;
import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

import java.io.File;

/**
 * Created by andre on 21/03/2018.
 */
public class ExternalMemoryBackupHandlerFragment extends AbstractBackupHandlerFragment<LocalFile> {

    private static final int REQUEST_PERMISSION = 1;

    public static ExternalMemoryBackupHandlerFragment newInstance(boolean allowBackup, boolean allowRestore) {
        ExternalMemoryBackupHandlerFragment fragment = new ExternalMemoryBackupHandlerFragment();
        fragment.setArguments(generateArguments(allowBackup, allowRestore));
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkExternalStoragePermission(getActivity());
    }

    private void checkExternalStoragePermission(Activity activity) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int result = ContextCompat.checkSelfPermission(activity, permission);
        if (result != PackageManager.PERMISSION_GRANTED) {
            showCoverView();
        } else {
            onPermissionGranted();
        }
    }

    private void askForPermission(Activity activity, String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            ThemedDialog.buildMaterialDialog(activity)
                    .title(R.string.title_request_permission)
                    .content(R.string.message_permission_required_external_storage)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            requestExternalStoragePermission(getActivity());
                        }

                    }).show();
        } else {
            requestExternalStoragePermission(activity);
        }
    }

    private void requestExternalStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                onPermissionDenied();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void onPermissionGranted() {
        hideCoverView();
        Activity activity = getActivity();
        if (activity != null && isStackEmpty()) {
            File root = Environment.getExternalStorageDirectory();
            loadRootFolder(new LocalFile(root));
        }
    }

    private void onPermissionDenied() {
        showCoverView();
        Activity activity = getActivity();
        if (activity != null) {
            ThemedDialog.buildMaterialDialog(activity)
                    .title(R.string.title_warning)
                    .content(R.string.message_permission_required_not_granted)
                    .show();
        }
    }

    @Override
    protected View onCreateCoverView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_cover_text_button, container, true);
        setCoverToolbar((Toolbar) view.findViewById(R.id.cover_toolbar));
        TextView coverTextView = view.findViewById(R.id.cover_text_view);
        Button coverActionButton = view.findViewById(R.id.cover_action_button);
        // personalize the views
        coverTextView.setText(R.string.cover_message_backup_external_memory_title);
        coverActionButton.setText(R.string.cover_message_backup_external_memory_button);
        coverActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                askForPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

        });
        // return the reference to the cover layout
        return view.findViewById(R.id.cover_layout);
    }

    @Override
    protected int getTitle() {
        return R.string.service_backup_external_memory;
    }

    @Override
    protected void loadFolder(LocalFile folder) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DiskBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_LIST);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            activity.startService(intent);
        }
    }

    @Override
    protected void createBackup(LocalFile folder, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DiskBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_BACKUP);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }

    @Override
    protected void restoreBackup(LocalFile file, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, DiskBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_RESTORE);
            intent.putExtra(AbstractBackupHandlerIntentService.BACKUP_FILE, file);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }
}