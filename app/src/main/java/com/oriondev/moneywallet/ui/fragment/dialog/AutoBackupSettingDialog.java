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

package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.broadcast.AutoBackupBroadcastReceiver;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.storage.preference.BackendManager;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.BackendExplorerActivity;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 26/11/18.
 */
public class AutoBackupSettingDialog extends DialogFragment {

    private static final String SS_BACKEND_ID = "AutoBackupSettingDialog::SavedState::BackendId";
    private static final String SS_FOLDER = "AutoBackupSettingDialog::SavedState::Folder";

    private static final int REQUEST_CODE_FOLDER_PICKER = 35625;

    private static final int OFFSET_MIN_HOURS = 24;
    private static final int OFFSET_MAX_HOURS = 168;
    private static final int OFFSET_BETWEEN_HOURS = 4;

    private String mBackendId;

    private SwitchCompat mServiceEnabledSwitchCompat;
    private CheckBox mOnlyWiFiCheckBox;
    private CheckBox mOnlyDataChangedCheckBox;
    private TextView mOffsetTextView;
    private SeekBar mOffsetSeekBar;
    private TextView mFolderTextView;
    private EditText mPasswordEditText;

    private IFile mFolder;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mBackendId = savedInstanceState.getString(SS_BACKEND_ID);
            mFolder = savedInstanceState.getParcelable(SS_FOLDER);
        }
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_auto_backup_title)
                .customView(R.layout.dialog_auto_backup_setting, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onSaveSetting();
                    }

                })
                .build();
        View view = dialog.getCustomView();
        if (view != null) {
            mServiceEnabledSwitchCompat = view.findViewById(R.id.auto_backup_enable_switch);
            mOnlyWiFiCheckBox = view.findViewById(R.id.auto_backup_wifi_check_box);
            mOnlyDataChangedCheckBox = view.findViewById(R.id.auto_backup_data_change_check_box);
            mOffsetTextView = view.findViewById(R.id.auto_backup_offset_text_view);
            mOffsetSeekBar = view.findViewById(R.id.auto_backup_offset_seek_bar);
            mFolderTextView = view.findViewById(R.id.auto_backup_folder_text_view);
            mPasswordEditText = view.findViewById(R.id.auto_backup_password_edit_text);
            // set listeners
            mOffsetSeekBar.setMax((OFFSET_MAX_HOURS - OFFSET_MIN_HOURS) / OFFSET_BETWEEN_HOURS);
            mOffsetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    AutoBackupSettingDialog.this.onProgressChanged(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // not used
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // not used
                }

            });
            mFolderTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        Intent intent = new Intent(activity, BackendExplorerActivity.class);
                        intent.putExtra(BackendExplorerActivity.BACKEND_ID, mBackendId);
                        intent.putExtra(BackendExplorerActivity.MODE, BackendExplorerActivity.MODE_FOLDER_PICKER);
                        startActivityForResult(intent, REQUEST_CODE_FOLDER_PICKER);
                    }
                }

            });
            if (savedInstanceState == null) {
                mServiceEnabledSwitchCompat.setChecked(BackendManager.isAutoBackupEnabled(mBackendId));
                mOnlyWiFiCheckBox.setChecked(BackendManager.isAutoBackupOnWiFiOnly(mBackendId));
                mOnlyDataChangedCheckBox.setChecked(BackendManager.isAutoBackupWhenDataIsChangedOnly(mBackendId));
                mOffsetSeekBar.setProgress((BackendManager.getAutoBackupHoursOffset(mBackendId) - OFFSET_MIN_HOURS) / OFFSET_BETWEEN_HOURS);
                mFolder = BackendServiceFactory.getFile(mBackendId, BackendManager.getAutoBackupFolder(mBackendId));
                mPasswordEditText.setText(BackendManager.getAutoBackupPassword(mBackendId));
            }
            onProgressChanged(mOffsetSeekBar.getProgress());
            onFolderChanged();
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SS_BACKEND_ID, mBackendId);
        outState.putParcelable(SS_FOLDER, mFolder);
    }

    public void show(FragmentManager fragmentManager, String tag, String backendId) {
        mBackendId = backendId;
        show(fragmentManager, tag);
    }

    private void onProgressChanged(int progress) {
        int hours = OFFSET_MIN_HOURS + (progress * OFFSET_BETWEEN_HOURS);
        mOffsetTextView.setText(getString(R.string.hint_auto_backup_every_n_hours, hours));
    }

    private void onFolderChanged() {
        if (mFolder != null) {
            mFolderTextView.setText(mFolder.getName());
        } else {
            mFolderTextView.setText(R.string.hint_auto_backup_root_folder);
        }
    }

    private void onSaveSetting() {
        BackendManager.setAutoBackupEnabled(mBackendId, mServiceEnabledSwitchCompat.isChecked());
        BackendManager.setAutoBackupOnWiFiOnly(mBackendId, mOnlyWiFiCheckBox.isChecked());
        BackendManager.setAutoBackupWhenDataIsChangedOnly(mBackendId, mOnlyDataChangedCheckBox.isChecked());
        BackendManager.setAutoBackupHoursOffset(mBackendId, OFFSET_MIN_HOURS + (mOffsetSeekBar.getProgress() * OFFSET_BETWEEN_HOURS));
        BackendManager.setAutoBackupFolder(mBackendId, mFolder != null ? mFolder.encodeToString() : null);
        BackendManager.setAutoBackupPassword(mBackendId, mPasswordEditText.getText().toString());
        AutoBackupBroadcastReceiver.scheduleAutoBackupTask(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FOLDER_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mFolder = data.getParcelableExtra(BackendExplorerActivity.RESULT_FILE);
                onFolderChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}