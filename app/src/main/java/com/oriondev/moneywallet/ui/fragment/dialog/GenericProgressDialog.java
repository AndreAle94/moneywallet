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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.service.AbstractBackupHandlerIntentService;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 22/03/2018.
 */
public class GenericProgressDialog extends DialogFragment {

    private static final String ARG_TITLE_RES = "GenericProgressDialog::Arguments::TitleRes";

    public static GenericProgressDialog newInstance(int title) {
        GenericProgressDialog dialog = new GenericProgressDialog();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_TITLE_RES, title);
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        Bundle arguments = getArguments();
        int titleRes = arguments != null ? arguments.getInt(ARG_TITLE_RES) : 0;
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(titleRes)
                .content(R.string.message_async_init)
                .progress(false, 100)
                .cancelable(false)
                .build();
        dialog.setCancelable(false);
        return dialog;
    }

    public void updateProgress(int status, int progress) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            switch (status) {
                case AbstractBackupHandlerIntentService.STATUS_BACKUP_CREATION:
                    dialog.setContent(R.string.message_backup_status_creation);
                    break;
                case AbstractBackupHandlerIntentService.STATUS_BACKUP_UPLOADING:
                    dialog.setContent(R.string.message_backup_status_uploading);
                    break;
                case AbstractBackupHandlerIntentService.STATUS_BACKUP_DOWNLOADING:
                    dialog.setContent(R.string.message_backup_status_downloading);
                    break;
                case AbstractBackupHandlerIntentService.STATUS_BACKUP_RESTORING:
                    dialog.setContent(R.string.message_backup_status_restoring);
                    break;
            }
            dialog.setProgress(progress);
        }
    }
}