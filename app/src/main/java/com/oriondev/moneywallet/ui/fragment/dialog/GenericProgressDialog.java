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
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 22/03/2018.
 */
public class GenericProgressDialog extends DialogFragment {

    private static final String ARG_TITLE_RES = "GenericProgressDialog::Arguments::TitleRes";
    private static final String ARG_CONTENT_RES = "GenericProgressDialog::Arguments::ContentRes";
    private static final String ARG_INDETERMINATE = "GenericProgressDialog::Arguments::Indeterminate";

    public static GenericProgressDialog newInstance(int title, int content, boolean indeterminate) {
        GenericProgressDialog dialog = new GenericProgressDialog();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_TITLE_RES, title);
        arguments.putInt(ARG_CONTENT_RES, content);
        arguments.putBoolean(ARG_INDETERMINATE, indeterminate);
        dialog.setArguments(arguments);
        dialog.setCancelable(false);
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
        int contentRes = arguments != null ? arguments.getInt(ARG_CONTENT_RES) : 0;
        boolean indeterminate = arguments != null && arguments.getBoolean(ARG_INDETERMINATE);
        MaterialDialog.Builder builder = ThemedDialog.buildMaterialDialog(activity);
                if (titleRes != 0) {
                    builder.title(titleRes);
                }
                if (contentRes != 0) {
                    builder.content(contentRes);
                }
        return builder.progress(indeterminate, 100)
                .cancelable(false)
                .build();
    }

    public void updateProgress(int contentRes, int progress) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            if (contentRes != 0) {
                dialog.setContent(contentRes);
            } else {
                dialog.setContent(null);
            }
            dialog.setProgress(progress);
        }
    }
}