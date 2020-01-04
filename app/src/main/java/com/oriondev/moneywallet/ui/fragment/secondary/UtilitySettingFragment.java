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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.LockMode;
import com.oriondev.moneywallet.service.AbstractCurrencyRateDownloadIntentService;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.CurrencyListActivity;
import com.oriondev.moneywallet.ui.activity.LockActivity;
import com.oriondev.moneywallet.ui.preference.ThemedInputPreference;
import com.oriondev.moneywallet.ui.preference.ThemedListPreference;
import com.oriondev.moneywallet.utils.DateFormatter;

import java.util.Date;

/**
 * Created by andrea on 07/03/18.
 */
public class UtilitySettingFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_LOCK_ACTIVITY = 8239;

    private ThemedListPreference mDailyReminderPreference;
    private ThemedListPreference mSecurityModeListPreference;
    private Preference mSecurityModeChangeKeyPreference;
    private ThemedListPreference mExchangeRateServiceListPreference;
    private ThemedInputPreference mExchangeRateCustomApiKey;
    private Preference mExchangeRateUpdatePreference;
    private Preference mCurrencyManagementPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
            broadcastManager.registerReceiver(mLocalBroadcastReceiver, new IntentFilter(LocalAction.ACTION_EXCHANGE_RATES_UPDATED));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
            broadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_utility);
        mDailyReminderPreference = (ThemedListPreference) findPreference("daily_reminder");
        mSecurityModeListPreference = (ThemedListPreference) findPreference("security_mode");
        mSecurityModeChangeKeyPreference = findPreference("security_change_key");
        mExchangeRateServiceListPreference = (ThemedListPreference) findPreference("exchange_rate_source");
        mExchangeRateCustomApiKey = (ThemedInputPreference) findPreference("exchange_rate_api_key");
        mExchangeRateUpdatePreference = findPreference("exchange_rate_update");
        mCurrencyManagementPreference = findPreference("currency_management");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // setup preference logic
        mDailyReminderPreference.setEntries(new String[] {
                getString(R.string.setting_item_security_none),
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
        });
        mDailyReminderPreference.setEntryValues(new String[] {
                String.valueOf(PreferenceManager.DAILY_REMINDER_DISABLED),
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
                "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"
        });
        if (isFingerprintAuthSupported(getActivity())) {
            mSecurityModeListPreference.setEntries(new String[] {
                    getString(R.string.setting_item_security_none),
                    getString(R.string.setting_item_security_pin),
                    getString(R.string.setting_item_security_sequence),
                    getString(R.string.setting_item_security_fingerprint)
            });
            mSecurityModeListPreference.setEntryValues(new String[] {
                    String.valueOf(PreferenceManager.LOCK_MODE_NONE),
                    String.valueOf(PreferenceManager.LOCK_MODE_PIN),
                    String.valueOf(PreferenceManager.LOCK_MODE_SEQUENCE),
                    String.valueOf(PreferenceManager.LOCK_MODE_FINGERPRINT)
            });
        } else {
            mSecurityModeListPreference.setEntries(new String[] {
                    getString(R.string.setting_item_security_none),
                    getString(R.string.setting_item_security_pin),
                    getString(R.string.setting_item_security_sequence)
            });
            mSecurityModeListPreference.setEntryValues(new String[] {
                    String.valueOf(PreferenceManager.LOCK_MODE_NONE),
                    String.valueOf(PreferenceManager.LOCK_MODE_PIN),
                    String.valueOf(PreferenceManager.LOCK_MODE_SEQUENCE)
            });
        }
        mExchangeRateServiceListPreference.setEntries(new String[] {
                getString(R.string.setting_item_utility_exchange_rates_service_oer)
        });
        mExchangeRateServiceListPreference.setEntryValues(new String[] {
                String.valueOf(PreferenceManager.SERVICE_OPEN_EXCHANGE_RATE)
        });
        // setup current (or default) values
        setupCurrentDailyReminder();
        setupCurrentLockMode();
        setupCurrentExchangeRateService();
        setupCurrentExchangeRateCustomApiKey();
        setupCurrentExchangeRateUpdate();
        // attach a listener to get notified when values changes
        mDailyReminderPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int hour = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentDailyReminder(getActivity(), hour);
                setupCurrentDailyReminder();
                return false;
            }

        });
        mSecurityModeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String oldValue = ((ThemedListPreference) preference).getValue();
                String value = (String) newValue;
                if (TextUtils.equals(oldValue, value)) {
                    // the value is not changed
                    return false;
                }
                Intent intent = null;
                int integerValue = Integer.parseInt(value);
                switch (integerValue) {
                    case PreferenceManager.LOCK_MODE_NONE:
                        intent = LockActivity.disableLock(getActivity());
                        break;
                    case PreferenceManager.LOCK_MODE_PIN:
                    case PreferenceManager.LOCK_MODE_SEQUENCE:
                    case PreferenceManager.LOCK_MODE_FINGERPRINT:
                        if (Integer.parseInt(oldValue) == PreferenceManager.LOCK_MODE_NONE) {
                            intent = LockActivity.enableLock(getActivity(), LockMode.get(integerValue));
                        } else {
                            intent = LockActivity.changeMode(getActivity(), LockMode.get(integerValue));
                        }
                        break;
                }
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_LOCK_ACTIVITY);
                }
                return false;
            }

        });
        mSecurityModeChangeKeyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(LockActivity.changeKey(getActivity()));
                return false;
            }

        });
        mExchangeRateServiceListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = Integer.parseInt((String) newValue);
                PreferenceManager.setCurrentExchangeRateService(index);
                setupCurrentExchangeRateService();
                setupCurrentExchangeRateCustomApiKey();
                return false;
            }

        });
        mExchangeRateCustomApiKey.setInput(R.string.setting_item_utility_exchange_rates_custom_api_key_hint, true, InputType.TYPE_CLASS_TEXT);
        mExchangeRateCustomApiKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int service = PreferenceManager.getCurrentExchangeRateService();
                PreferenceManager.setServiceApiKey(service, (String) newValue);
                setupCurrentExchangeRateCustomApiKey();
                return false;
            }

        });
        mExchangeRateUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = AbstractCurrencyRateDownloadIntentService.buildIntent(activity);
                    activity.startService(intent);
                }
                return false;
            }

        });
        mCurrencyManagementPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, CurrencyListActivity.class);
                    intent.putExtra(CurrencyListActivity.ACTIVITY_MODE, CurrencyListActivity.CURRENCY_MANAGER);
                    startActivity(intent);
                }
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

    private boolean isFingerprintAuthSupported(Context context) {
        return FingerprintManagerCompat.from(context).isHardwareDetected();
    }

    private void setupCurrentDailyReminder() {
        int hour = PreferenceManager.getCurrentDailyReminder();
        mDailyReminderPreference.setValue(String.valueOf(hour));
        if (hour == PreferenceManager.DAILY_REMINDER_DISABLED) {
            mDailyReminderPreference.setSummary(R.string.setting_item_daily_reminder_none);
        } else {
            String summary = getString(R.string.setting_summary_daily_reminder, hour);
            mDailyReminderPreference.setSummary(summary);
        }
    }

    private void setupCurrentLockMode() {
        LockMode lockMode = PreferenceManager.getCurrentLockMode();
        mSecurityModeListPreference.setValue(lockMode.getValueAsString());
        switch (lockMode) {
            case NONE:
                mSecurityModeListPreference.setSummary(R.string.setting_item_security_none);
                mSecurityModeChangeKeyPreference.setVisible(false);
                break;
            case PIN:
                mSecurityModeListPreference.setSummary(R.string.setting_item_security_pin);
                mSecurityModeChangeKeyPreference.setTitle(R.string.setting_title_security_change_pin);
                mSecurityModeChangeKeyPreference.setVisible(true);
                break;
            case SEQUENCE:
                mSecurityModeListPreference.setSummary(R.string.setting_item_security_sequence);
                mSecurityModeChangeKeyPreference.setTitle(R.string.setting_title_security_change_sequence);
                mSecurityModeChangeKeyPreference.setVisible(true);
                break;
            case FINGERPRINT:
                mSecurityModeListPreference.setSummary(R.string.setting_item_security_fingerprint);
                mSecurityModeChangeKeyPreference.setVisible(false);
                break;
        }
    }

    private void setupCurrentExchangeRateService() {
        int index = PreferenceManager.getCurrentExchangeRateService();
        mExchangeRateServiceListPreference.setValue(String.valueOf(index));
        switch (index) {
            case PreferenceManager.SERVICE_OPEN_EXCHANGE_RATE:
                mExchangeRateServiceListPreference.setSummary(R.string.setting_item_utility_exchange_rates_service_oer);
                mExchangeRateCustomApiKey.setContent(R.string.setting_item_utility_exchange_rates_service_oer_custom_api_key_message);
                break;
        }
        if (PreferenceManager.hasCurrentExchangeRateServiceDefaultApiKey()) {
            mExchangeRateCustomApiKey.setVisible(false);
        } else {
            mExchangeRateCustomApiKey.setVisible(true);
        }
    }

    private void setupCurrentExchangeRateCustomApiKey() {
        if (!PreferenceManager.hasCurrentExchangeRateServiceDefaultApiKey()) {
            String apiKey = PreferenceManager.getCurrentExchangeRateServiceCustomApiKey();
            if (!TextUtils.isEmpty(apiKey)) {
                mExchangeRateCustomApiKey.setSummary(getString(R.string.setting_summary_exchange_rate_api_key, apiKey));
                mExchangeRateCustomApiKey.setCurrentValue(apiKey);
            } else {
                mExchangeRateCustomApiKey.setSummary(R.string.setting_summary_exchange_rate_api_key_missing);
                mExchangeRateCustomApiKey.setCurrentValue(null);
            }
        } else {
            mExchangeRateCustomApiKey.setSummary(null);
            mExchangeRateCustomApiKey.setCurrentValue(null);
        }
    }

    private void setupCurrentExchangeRateUpdate() {
        long timestamp = PreferenceManager.getLastExchangeRateUpdateTimestamp();
        String summary = DateFormatter.getDateFromToday(new Date(timestamp));
        String fullSummary = getString(R.string.setting_summary_exchange_rate_update, summary);
        mExchangeRateUpdatePreference.setSummary(fullSummary);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCK_ACTIVITY) {
            setupCurrentLockMode();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocalAction.ACTION_EXCHANGE_RATES_UPDATED.equals(intent.getAction())) {
                setupCurrentExchangeRateUpdate();
            }
        }

    };
}