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

package com.oriondev.moneywallet.picker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.api.disk.DiskBackendService;
import com.oriondev.moneywallet.model.ColorIcon;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.model.VectorIcon;
import com.oriondev.moneywallet.ui.activity.BackendExplorerActivity;
import com.oriondev.moneywallet.ui.activity.IconListActivity;
import com.oriondev.moneywallet.ui.activity.ImportExportActivity;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.Utils;

/**
 * Created by andrea on 01/02/18.
 */
public class LocalFilePicker extends Fragment {

    public static final int MODE_FILE_PICKER = 0;
    public static final int MODE_FOLDER_PICKER = 1;

    private static final String SS_PICKER_MODE = "LocalFilePicker::SavedState::PickerMode";
    private static final String SS_CURRENT_FILE = "LocalFilePicker::SavedState::CurrentIcon";

    private static final String ARG_PICKER_MODE = "LocalFilePicker::Argument::PickerMode";

    private static final int REQUEST_FILE_PICKER = 23;
    private static final int REQUEST_PERMISSION = 35;

    private Controller mController;

    private int mPickerMode;
    private LocalFile mCurrentFile;

    public static LocalFilePicker createPicker(FragmentManager fragmentManager, String tag, int mode) {
        LocalFilePicker filePicker = (LocalFilePicker) fragmentManager.findFragmentByTag(tag);
        if (filePicker == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(ARG_PICKER_MODE, mode);
            filePicker = new LocalFilePicker();
            filePicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(filePicker, tag).commit();
        }
        return filePicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPickerMode = savedInstanceState.getInt(SS_PICKER_MODE);
            mCurrentFile = savedInstanceState.getParcelable(SS_CURRENT_FILE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mPickerMode = arguments.getInt(ARG_PICKER_MODE);
            } else {
                mPickerMode = MODE_FILE_PICKER;
            }
            mCurrentFile = null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onLocalFileChanged(getTag(), mPickerMode, mCurrentFile);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_PICKER_MODE, mPickerMode);
        outState.putParcelable(SS_CURRENT_FILE, mCurrentFile);
    }

    public boolean isSelected() {
        return mCurrentFile != null;
    }

    public LocalFile getCurrentFile() {
        return mCurrentFile;
    }

    public void showPicker() {
        Activity activity = getActivity();
        if (activity != null) {
            if (isPermissionGranted(activity)) {
                startPicker(activity);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    private boolean isPermissionGranted(Context context) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int result = ContextCompat.checkSelfPermission(context, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void startPicker(Context context) {
        Intent intent = new Intent(context, BackendExplorerActivity.class);
        intent.putExtra(BackendExplorerActivity.BACKEND_ID, BackendServiceFactory.SERVICE_ID_EXTERNAL_MEMORY);
        switch (mPickerMode) {
            case MODE_FILE_PICKER:
                intent.putExtra(BackendExplorerActivity.MODE, BackendExplorerActivity.MODE_FILE_PICKER);
                break;
            case MODE_FOLDER_PICKER:
                intent.putExtra(BackendExplorerActivity.MODE, BackendExplorerActivity.MODE_FOLDER_PICKER);
                break;
        }
        startActivityForResult(intent, REQUEST_FILE_PICKER);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_FILE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentFile = intent.getParcelableExtra(BackendExplorerActivity.RESULT_FILE);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPicker(getActivity());
            }
        }
    }

    public interface Controller {

        void onLocalFileChanged(String tag, int mode, LocalFile localFile);
    }
}