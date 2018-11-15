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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.service.google.GoogleDriveBackupHandlerIntentService;
import com.oriondev.moneywallet.model.GoogleDriveFile;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by andre on 21/03/2018.
 */
public class GoogleDriveBackupHandlerFragment extends AbstractBackupHandlerFragment<GoogleDriveFile> {

    private static final int REQUEST_CODE_SIGN_IN = 8393;

    public static GoogleDriveBackupHandlerFragment newInstance(boolean allowBackup, boolean allowRestore) {
        GoogleDriveBackupHandlerFragment fragment = new GoogleDriveBackupHandlerFragment();
        fragment.setArguments(generateArguments(allowBackup, allowRestore));
        return fragment;
    }

    @Override
    protected View onCreateCoverView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_cover_text_button, container, true);
        setCoverToolbar((Toolbar) view.findViewById(R.id.cover_toolbar));
        TextView coverTextView = view.findViewById(R.id.cover_text_view);
        Button coverActionButton = view.findViewById(R.id.cover_action_button);
        // personalize the views
        coverTextView.setText(R.string.cover_message_backup_google_drive_title);
        coverActionButton.setText(R.string.cover_message_backup_google_drive_button);
        coverActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestScopes(Drive.SCOPE_FILE)
                                    .requestScopes(Drive.SCOPE_APPFOLDER)
                                    .build();
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, signInOptions);
                    startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                }
            }

        });
        // return the reference to the cover layout
        return view.findViewById(R.id.cover_layout);
    }

    @Override
    protected int getTitle() {
        return R.string.service_backup_google_drive;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_backup_service_google_drive;
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
                .content(R.string.message_backup_service_google_drive_disconnect)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        signOutFromGoogle(getActivity());
                    }

                })
                .show();
    }

    private void signOutFromGoogle(Activity activity) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, signInOptions);
        Task<Void> signOutTask = googleSignInClient.signOut();
        signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showCoverView();
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIfUserIsLoggedIn(getActivity());
    }

    private void checkIfUserIsLoggedIn(Activity activity) {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initAndLoadData(activity, signInAccount);
        } else {
            showCoverView();
        }
    }

    private void initAndLoadData(Context context, GoogleSignInAccount signInAccount) {
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(context.getApplicationContext(), signInAccount);
        hideCoverView();
        Task<DriveFolder> appFolderTask = driveResourceClient.getAppFolder();
        appFolderTask.addOnCompleteListener(new OnCompleteListener<DriveFolder>() {

            @Override
            public void onComplete(@NonNull Task<DriveFolder> task) {
                loadRootFolder(new GoogleDriveFile(task.getResult()));
            }

        });
    }

    @Override
    protected void loadFolder(GoogleDriveFile folder) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, GoogleDriveBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_LIST);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            activity.startService(intent);
        }
    }

    @Override
    protected void createBackup(GoogleDriveFile folder, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, GoogleDriveBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_BACKUP);
            intent.putExtra(AbstractBackupHandlerIntentService.PARENT_FOLDER, folder);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }

    @Override
    protected void restoreBackup(GoogleDriveFile file, String password) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, GoogleDriveBackupHandlerIntentService.class);
            intent.putExtra(AbstractBackupHandlerIntentService.SERVICE_ACTION, AbstractBackupHandlerIntentService.ACTION_RESTORE);
            intent.putExtra(AbstractBackupHandlerIntentService.BACKUP_FILE, file);
            intent.putExtra(AbstractBackupHandlerIntentService.PASSWORD, password);
            activity.startService(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                hideCoverView();
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initAndLoadData(getActivity(), getAccountTask.getResult());
                } else {
                    showCoverView();
                }
            } else {
                showCoverView();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}