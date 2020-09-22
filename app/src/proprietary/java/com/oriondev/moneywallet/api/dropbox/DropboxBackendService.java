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

package com.oriondev.moneywallet.api.dropbox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.core.android.Auth;
import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.api.AbstractBackendServiceDelegate;
import com.oriondev.moneywallet.api.BackendServiceFactory;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 21/11/18.
 */
public class DropboxBackendService extends AbstractBackendServiceDelegate {

    private static final String PREFERENCE_FILE = "dropbox";
    private static final String ACCESS_TOKEN = "access_token";

    public DropboxBackendService(BackendServiceStatusListener listener) {
        super(listener);
    }

    @Override
    public String getId() {
        return BackendServiceFactory.SERVICE_ID_DROPBOX;
    }

    @Override
    public int getName() {
        return R.string.service_backup_drop_box;
    }

    @Override
    public int getBackupCoverMessage() {
        return R.string.cover_message_backup_dropbox_title;
    }

    @Override
    public int getBackupCoverAction() {
        return R.string.cover_message_backup_dropbox_button;
    }

    @Override
    public boolean isServiceEnabled(Context context) {
        return getAccessToken(context) != null;
    }

    @Override
    public void setup(ComponentActivity activity) throws BackendException {
        Auth.startOAuth2Authentication(activity, BuildConfig.API_KEY_DROPBOX);
    }

    @Override
    public void teardown(final ComponentActivity activity) throws BackendException {
        ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.title_warning)
                .content(R.string.message_backup_service_dropbox_disconnect)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        destroyAccountInformation(activity);
                    }

                })
                .show();
    }

    private void destroyAccountInformation(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        setBackendServiceEnabled(false);
    }

    /*package-local*/ static String getAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        String accessToken = preferences.getString(ACCESS_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                preferences.edit().putString(ACCESS_TOKEN, accessToken).apply();
            }
        }
        return accessToken;
    }
}