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

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.activity.BackupListActivity;
import com.oriondev.moneywallet.ui.activity.ImportExportActivity;

/**
 * Created by andre on 21/03/2018.
 */
public class DatabaseSettingFragment extends PreferenceFragmentCompat {

    private Preference mBackupServicesPreference;
    private Preference mImportDataPreference;
    private Preference mExportDataPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_database);
        mBackupServicesPreference = findPreference("backup_services");
        mImportDataPreference = findPreference("import_data");
        mExportDataPreference = findPreference("export_data");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBackupServicesPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(androidx.preference.Preference preference) {
                Intent intent = new Intent(getActivity(), BackupListActivity.class);
                intent.putExtra(BackupListActivity.BACKUP_MODE, BackupListActivity.FULL);
                startActivity(intent);
                return false;
            }

        });
        mImportDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ImportExportActivity.class);
                intent.putExtra(ImportExportActivity.MODE, ImportExportActivity.MODE_IMPORT);
                startActivity(intent);
                return false;
            }

        });
        mExportDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ImportExportActivity.class);
                intent.putExtra(ImportExportActivity.MODE, ImportExportActivity.MODE_EXPORT);
                startActivity(intent);
                return false;
            }

        });
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.setPadding(0, 0, 0, 0);
        return recyclerView;
    }
}