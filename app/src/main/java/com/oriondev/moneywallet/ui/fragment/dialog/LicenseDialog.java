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
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.background.LicenseLoader;
import com.oriondev.moneywallet.model.License;
import com.oriondev.moneywallet.ui.adapter.recycler.LicenseAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

import java.util.List;

/**
 * Created by andrea on 06/04/18.
 */
public class LicenseDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<List<License>>,LicenseAdapter.Controller {

    private static final int LOADER_ID = 3698;

    public static void showSafely(FragmentManager fragmentManager, String tag, Callback callback) {
        LicenseDialog dialog = (LicenseDialog) fragmentManager.findFragmentByTag(tag);
        if (dialog == null) {
            dialog = new LicenseDialog();
            dialog.setCallback(callback);
            dialog.show(fragmentManager, tag);
        } else {
            dialog.setCallback(callback);
            fragmentManager.beginTransaction().show(dialog).commit();
        }
    }

    private Callback mCallback;
    private LicenseAdapter mAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        mAdapter = new LicenseAdapter(this);
        MaterialDialog.Builder builder = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.title_licenses)
                .adapter(mAdapter, new LinearLayoutManager(activity))
                .positiveText(android.R.string.ok);
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        return builder.build();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public Loader<List<License>> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            return new LicenseLoader(activity);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<License>> loader, List<License> data) {
        if (mAdapter != null) {
            mAdapter.setLicenses(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<License>> loader) {
        if (mAdapter != null) {
            mAdapter.setLicenses(null);
        }
    }

    @Override
    public void onLicenseClick(License license) {
        if (mCallback != null) {
            mCallback.onLicenseClick(license);
        }
    }

    public interface Callback {

        void onLicenseClick(License license);
    }
}