package com.oriondev.moneywallet.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.DataFormat;
import com.oriondev.moneywallet.model.LocalFile;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.ExportColumnsPicker;
import com.oriondev.moneywallet.picker.ImportExportFormatPicker;
import com.oriondev.moneywallet.picker.LocalFilePicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.service.ImportExportIntentService;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.ui.fragment.dialog.GenericProgressDialog;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.DateFormatter;

import java.util.Date;
import java.util.Locale;

/**
 * Created by andrea on 19/12/18.
 */
public class ImportExportActivity extends SinglePanelActivity implements ImportExportFormatPicker.Controller, DateTimePicker.Controller, WalletPicker.MultiWalletController, LocalFilePicker.Controller, ExportColumnsPicker.Controller {

    public static final String MODE = "ImportExportActivity::Argument::Mode";

    public static final int MODE_EXPORT = 0;
    public static final int MODE_IMPORT = 1;

    private static final String TAG_DATA_FORMAT_PICKER = "ImportExportActivity::Tag::DataFormatPicker";
    private static final String TAG_START_DATE_TIME_PICKER = "ImportExportActivity::Tag::StartDateTimePicker";
    private static final String TAG_END_DATE_TIME_PICKER = "ImportExportActivity::Tag::EndDateTimePicker";
    private static final String TAG_WALLET_PICKER = "ImportExportActivity::Tag::WalletPicker";
    private static final String TAG_LOCAL_FILE_PICKER = "ImportExportActivity::Tag::LocalFilePicker";
    private static final String TAG_COLUMNS_PICKER = "ImportExportActivity::Tag::ColumnsPicker";
    private static final String TAG_PROGRESS_DIALOG = "ImportExportActivity::tag::GenericProgressDialog";

    private MaterialEditText mImportFormatEditText;
    private MaterialEditText mExportFormatEditText;
    private MaterialEditText mStartDateEditText;
    private MaterialEditText mEndDateEditText;
    private MaterialEditText mWalletsEditText;
    private MaterialEditText mImportFileEditText;
    private MaterialEditText mExportFolderEditText;
    private MaterialEditText mExportColumnsEditText;
    private CheckBox mUniqueWalletCheckbox;

    private ImportExportFormatPicker mDataFormatPicker;
    private DateTimePicker mStartDateTimePicker;
    private DateTimePicker mEndDateTimePicker;
    private WalletPicker mWalletPicker;
    private LocalFilePicker mLocalFilePicker;
    private ExportColumnsPicker mExportColumnsPicker;

    private int mMode;

    private GenericProgressDialog mProgressDialog;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalAction.ACTION_IMPORT_SERVICE_STARTED);
        intentFilter.addAction(LocalAction.ACTION_IMPORT_SERVICE_FINISHED);
        intentFilter.addAction(LocalAction.ACTION_IMPORT_SERVICE_FAILED);
        intentFilter.addAction(LocalAction.ACTION_EXPORT_SERVICE_STARTED);
        intentFilter.addAction(LocalAction.ACTION_EXPORT_SERVICE_FINISHED);
        intentFilter.addAction(LocalAction.ACTION_EXPORT_SERVICE_FAILED);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_import_export, parent, true);
        mImportFormatEditText = view.findViewById(R.id.import_format_edit_text);
        mExportFormatEditText = view.findViewById(R.id.export_format_edit_text);
        mStartDateEditText = view.findViewById(R.id.start_date_edit_text);
        mEndDateEditText = view.findViewById(R.id.end_date_edit_text);
        mWalletsEditText = view.findViewById(R.id.wallets_edit_text);
        mImportFileEditText = view.findViewById(R.id.import_file_edit_text);
        mExportFolderEditText = view.findViewById(R.id.export_folder_edit_text);
        mExportColumnsEditText = view.findViewById(R.id.export_optional_columns_edit_text);
        mUniqueWalletCheckbox = view.findViewById(R.id.export_unique_wallet_checkbox);
        // check activity mode and update ui
        mMode = getActivityMode();
        mImportFormatEditText.setVisibility(mMode == MODE_IMPORT ? View.VISIBLE : View.GONE);
        mExportFormatEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mStartDateEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mEndDateEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mWalletsEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mImportFileEditText.setVisibility(mMode == MODE_IMPORT ? View.VISIBLE : View.GONE);
        mExportFolderEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mExportColumnsEditText.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        mUniqueWalletCheckbox.setVisibility(mMode == MODE_EXPORT ? View.VISIBLE : View.GONE);
        // attach listeners to views
        mImportFormatEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DataFormat[] dataFormats = new DataFormat[]{
                        DataFormat.CSV
                };
                mDataFormatPicker.showPicker(dataFormats);
            }

        });
        mExportFormatEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DataFormat[] dataFormats = new DataFormat[]{
                        DataFormat.CSV,
                        DataFormat.XLS,
                        DataFormat.PDF
                };
                mDataFormatPicker.showPicker(dataFormats);
            }

        });
        mStartDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartDateTimePicker.showDatePicker();
            }

        });
        mStartDateEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mStartDateTimePicker.setCurrentDateTime(null);
                return false;
            }

        });
        mEndDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEndDateTimePicker.showDatePicker();
            }

        });
        mEndDateEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mEndDateTimePicker.setCurrentDateTime(null);
                return false;
            }

        });
        mWalletsEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletPicker.showMultiWalletPicker();
            }

        });
        mImportFileEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLocalFilePicker.showPicker();
            }

        });
        mExportFolderEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLocalFilePicker.showPicker();
            }

        });
        // disable edit texts
        mImportFormatEditText.setTextViewMode(true);
        mExportFormatEditText.setTextViewMode(true);
        mStartDateEditText.setTextViewMode(true);
        mEndDateEditText.setTextViewMode(true);
        mWalletsEditText.setTextViewMode(true);
        mImportFileEditText.setTextViewMode(true);
        mExportFolderEditText.setTextViewMode(true);
        mExportColumnsEditText.setTextViewMode(true);
        // attach validators
        mImportFormatEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_format);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mDataFormatPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mExportFormatEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_format);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mDataFormatPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletsEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_multiple_wallets);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mImportFileEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_input_file);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mLocalFilePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mExportFolderEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_output_folder);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mLocalFilePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mExportColumnsEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mExportColumnsPicker.showPicker();
            }

        });
        // initialize pickers
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDataFormatPicker = ImportExportFormatPicker.createPicker(fragmentManager, TAG_DATA_FORMAT_PICKER);
        mStartDateTimePicker = DateTimePicker.createPicker(fragmentManager, TAG_START_DATE_TIME_PICKER, null);
        mEndDateTimePicker = DateTimePicker.createPicker(fragmentManager, TAG_END_DATE_TIME_PICKER, null);
        mWalletPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_PICKER, (Wallet[]) null);
        mExportColumnsPicker = ExportColumnsPicker.createPicker(fragmentManager, TAG_COLUMNS_PICKER);
        mProgressDialog = (GenericProgressDialog) fragmentManager.findFragmentByTag(TAG_PROGRESS_DIALOG);
        switch (mMode) {
            case MODE_IMPORT:
                mLocalFilePicker = LocalFilePicker.createPicker(fragmentManager, TAG_LOCAL_FILE_PICKER, LocalFilePicker.MODE_FILE_PICKER);
                break;
            case MODE_EXPORT:
                mLocalFilePicker = LocalFilePicker.createPicker(fragmentManager, TAG_LOCAL_FILE_PICKER, LocalFilePicker.MODE_FOLDER_PICKER);
                break;
        }
    }

    private int getActivityMode() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getIntExtra(MODE, MODE_EXPORT);
        }
        return MODE_EXPORT;
    }

    @Override
    protected int getActivityTitleRes() {
        switch (mMode) {
            case MODE_EXPORT:
                return R.string.title_activity_export_data;
            case MODE_IMPORT:
                return R.string.title_activity_import_data;
        }
        return 0;
    }

    @Override
    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_import_export;
    }

    @Override
    protected void onMenuCreated(Menu menu) {
        switch (mMode) {
            case MODE_IMPORT:
                menu.findItem(R.id.action_import_data).setVisible(true);
                menu.findItem(R.id.action_export_data).setVisible(false);
                break;
            case MODE_EXPORT:
                menu.findItem(R.id.action_import_data).setVisible(false);
                menu.findItem(R.id.action_export_data).setVisible(true);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import_data:
                if (mImportFormatEditText.validate() && mImportFileEditText.validate()) {
                    ThemedDialog.buildMaterialDialog(this)
                            .title(R.string.title_warning)
                            .content(R.string.message_data_import_without_backup)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {

                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    importData();
                                }

                            })
                            .show();
                }
                break;
            case R.id.action_export_data:
                if (mExportFormatEditText.validate() && mWalletsEditText.validate() && mExportFolderEditText.validate()) {
                    exportData();
                }
                break;
        }
        return false;
    }

    private void importData() {
        Intent intent = new Intent(this, ImportExportIntentService.class);
        intent.putExtra(ImportExportIntentService.MODE, ImportExportIntentService.MODE_IMPORT);
        intent.putExtra(ImportExportIntentService.FORMAT, mDataFormatPicker.getCurrentFormat());
        intent.putExtra(ImportExportIntentService.FILE, mLocalFilePicker.getCurrentFile().getFile());
        startService(intent);
    }

    private void exportData() {
        Intent intent = new Intent(this, ImportExportIntentService.class);
        intent.putExtra(ImportExportIntentService.MODE, ImportExportIntentService.MODE_EXPORT);
        intent.putExtra(ImportExportIntentService.FORMAT, mDataFormatPicker.getCurrentFormat());
        intent.putExtra(ImportExportIntentService.START_DATE, mStartDateTimePicker.getCurrentDateTime());
        intent.putExtra(ImportExportIntentService.END_DATE, mEndDateTimePicker.getCurrentDateTime());
        intent.putExtra(ImportExportIntentService.WALLETS, mWalletPicker.getCurrentWallets());
        intent.putExtra(ImportExportIntentService.FOLDER, mLocalFilePicker.getCurrentFile().getFile());
        intent.putExtra(ImportExportIntentService.UNIQUE_WALLET, mUniqueWalletCheckbox.isChecked());
        intent.putExtra(ImportExportIntentService.OPTIONAL_COLUMNS, mExportColumnsPicker.getCurrentServiceColumns());
        startService(intent);
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        // the floating action button is not required here
        return false;
    }

    @Override
    public void onFormatChanged(String tag, DataFormat format) {
        if (format != null) {
            switch (format) {
                case CSV:
                    mImportFormatEditText.setText(R.string.hint_data_format_csv);
                    mExportFormatEditText.setText(R.string.hint_data_format_csv);
                    if (mMode == MODE_EXPORT) {
                        mExportColumnsEditText.setVisibility(View.VISIBLE);
                    }
                    break;
                case XLS:
                    mImportFormatEditText.setText(R.string.hint_data_format_xls);
                    mExportFormatEditText.setText(R.string.hint_data_format_xls);
                    if (mMode == MODE_EXPORT) {
                        mExportColumnsEditText.setVisibility(View.VISIBLE);
                    }
                    break;
                case PDF:
                    mImportFormatEditText.setText(R.string.hint_data_format_pdf);
                    mExportFormatEditText.setText(R.string.hint_data_format_pdf);
                    if (mMode == MODE_EXPORT) {
                        mExportColumnsEditText.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        } else {
            mImportFormatEditText.setText(null);
            mExportFormatEditText.setText(null);
            mExportColumnsEditText.setVisibility(View.GONE);
        }
        onFormatOrWalletChanged();
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_START_DATE_TIME_PICKER:
                if (date != null) {
                    DateFormatter.applyDate(mStartDateEditText, date);
                } else {
                    mStartDateEditText.setText(null);
                }
                break;
            case TAG_END_DATE_TIME_PICKER:
                if (date != null) {
                    DateFormatter.applyDate(mEndDateEditText, date);
                } else {
                    mEndDateEditText.setText(null);
                }
                break;
        }
    }

    @Override
    public void onWalletListChanged(String tag, Wallet[] wallets) {
        if (wallets != null && wallets.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < wallets.length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(wallets[i].getName());
            }
            mWalletsEditText.setText(builder);
        } else {
            mWalletsEditText.setText(null);
        }
        onFormatOrWalletChanged();
    }

    @Override
    public void onLocalFileChanged(String tag, int mode, LocalFile localFile) {
        switch (mode) {
            case LocalFilePicker.MODE_FILE_PICKER:
                mImportFileEditText.setText(localFile != null ? localFile.getLocalPath() : null);
                if (localFile != null && !mDataFormatPicker.isSelected()) {
                    // try to detect the file type starting from the file extension
                    String name = localFile.getName();
                    String extension = name.substring(name.lastIndexOf("."));
                    if (!TextUtils.isEmpty(extension)) {
                        switch (extension.toLowerCase(Locale.ENGLISH)) {
                            case ".csv":
                                mDataFormatPicker.setCurrentFormat(DataFormat.CSV);
                                break;
                        }
                    }
                }
                break;
            case LocalFilePicker.MODE_FOLDER_PICKER:
                mExportFolderEditText.setText(localFile != null ? localFile.getLocalPath() : null);
                break;
        }
    }

    private void onFormatOrWalletChanged() {
        if (mMode == MODE_EXPORT) {
            if (mWalletPicker.isSelected()) {
                Wallet[] wallets = mWalletPicker.getCurrentWallets();
                if (wallets != null && wallets.length > 1) {
                    DataFormat dataFormat = mDataFormatPicker.getCurrentFormat();
                    if (dataFormat != null) {
                        switch (dataFormat) {
                            case CSV:
                                mUniqueWalletCheckbox.setVisibility(View.GONE);
                                break;
                            case XLS:
                            case PDF:
                                mUniqueWalletCheckbox.setVisibility(View.VISIBLE);
                                break;
                        }
                        return;
                    }
                }
            }
        }
        mUniqueWalletCheckbox.setVisibility(View.GONE);
    }

    @Override
    public void onExportColumnsChanged(String tag, String[] columns) {
        if (columns != null && columns.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < columns.length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(columns[i]);
            }
            mExportColumnsEditText.setText(builder);
        } else {
            mExportColumnsEditText.setText(null);
        }
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case LocalAction.ACTION_IMPORT_SERVICE_STARTED:
                        if (mProgressDialog == null) {
                            mProgressDialog = GenericProgressDialog.newInstance(R.string.title_data_importing, R.string.message_data_import_running, true);
                        }
                        mProgressDialog.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
                        break;
                    case LocalAction.ACTION_IMPORT_SERVICE_FINISHED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        ThemedDialog.buildMaterialDialog(ImportExportActivity.this)
                                .title(R.string.title_success)
                                .content(R.string.message_data_import_success)
                                .positiveText(android.R.string.ok)
                                .show();
                        break;
                    case LocalAction.ACTION_IMPORT_SERVICE_FAILED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        Exception exception = (Exception) intent.getSerializableExtra(ImportExportIntentService.EXCEPTION);
                        ThemedDialog.buildMaterialDialog(ImportExportActivity.this)
                                .title(R.string.title_failed)
                                .content(R.string.message_data_import_failed, exception.getMessage())
                                .positiveText(android.R.string.ok)
                                .show();
                        break;
                    case LocalAction.ACTION_EXPORT_SERVICE_STARTED:
                        if (mProgressDialog == null) {
                            mProgressDialog = GenericProgressDialog.newInstance(R.string.title_data_exporting, R.string.message_data_export_running, true);
                        }
                        mProgressDialog.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
                        break;
                    case LocalAction.ACTION_EXPORT_SERVICE_FINISHED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        ThemedDialog.buildMaterialDialog(ImportExportActivity.this)
                                .title(R.string.title_success)
                                .content(R.string.message_data_export_success)
                                .positiveText(android.R.string.ok)
                                .negativeText(android.R.string.cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {

                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Uri uri = intent.getParcelableExtra(ImportExportIntentService.RESULT_FILE_URI);
                                        String type = intent.getStringExtra(ImportExportIntentService.RESULT_FILE_TYPE);
                                        if (uri != null) {
                                            Intent target = new Intent(Intent.ACTION_VIEW);
                                            target.setDataAndType(uri, type);
                                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            Intent intent = Intent.createChooser(target, getString(R.string.action_open));
                                            try {
                                                startActivity(intent);
                                            } catch (ActivityNotFoundException ignore) {
                                                // no activity to handle this type of file
                                            }
                                        }
                                    }

                                })
                                .show();
                        break;
                    case LocalAction.ACTION_EXPORT_SERVICE_FAILED:
                        if (mProgressDialog != null) {
                            mProgressDialog.dismissAllowingStateLoss();
                            mProgressDialog = null;
                        }
                        exception = (Exception) intent.getSerializableExtra(ImportExportIntentService.EXCEPTION);
                        ThemedDialog.buildMaterialDialog(ImportExportActivity.this)
                                .title(R.string.title_failed)
                                .content(R.string.message_data_export_failed, exception.getMessage())
                                .positiveText(android.R.string.ok)
                                .show();
                        break;
                }
            }
        }

    };
}