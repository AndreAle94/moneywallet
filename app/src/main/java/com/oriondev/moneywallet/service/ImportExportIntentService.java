package com.oriondev.moneywallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.oriondev.moneywallet.broadcast.LocalAction;

/**
 * Created by andrea on 20/12/18.
 */
public class ImportExportIntentService extends IntentService {

    public static final String MODE = "ImportExportIntentService::Arguments::Mode";
    public static final String FORMAT = "ImportExportIntentService::Arguments::Format";
    public static final String START_DATE = "ImportExportIntentService::Arguments::StartDate";
    public static final String END_DATE = "ImportExportIntentService::Arguments::EndDate";
    public static final String WALLETS = "ImportExportIntentService::Arguments::Wallets";
    public static final String FOLDER = "ImportExportIntentService::Arguments::Folder";
    public static final String FILE = "ImportExportIntentService::Arguments::File";
    public static final String UNIQUE_WALLET = "ImportExportIntentService::Arguments::UniqueWallet";
    public static final String OPTIONAL_COLUMNS = "ImportExportIntentService::Arguments::OptionalColumns";

    public static final String RESULT_FILE_URI = "ImportExportIntentService::Results::FileUri";
    public static final String RESULT_FILE_TYPE = "ImportExportIntentService::Results::FileType";
    public static final String EXCEPTION = "ImportExportIntentService::Results::Exception";

    public static final int MODE_EXPORT = 0;
    public static final int MODE_IMPORT = 1;

    public static final String COLUMN_EVENT = "column_event";
    public static final String COLUMN_PEOPLE = "column_people";
    public static final String COLUMN_PLACE = "column_place";
    public static final String COLUMN_NOTE = "column_note";

    private LocalBroadcastManager mBroadcastManager;

    public ImportExportIntentService() {
        super("ImportExportIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            mBroadcastManager = LocalBroadcastManager.getInstance(this);
            int mode = intent.getIntExtra(MODE, MODE_EXPORT);
            switch (mode) {
                case MODE_EXPORT:
                    handleExport(intent);
                    break;
                case MODE_IMPORT:
                    handleImport(intent);
                    break;
            }
        }
    }

    private void handleImport(@NonNull Intent intent) {
        notifyTaskStarted(LocalAction.ACTION_IMPORT_SERVICE_STARTED);
        try {
            // TODO: import logic here
            Thread.sleep(5000);

            notifyTaskFinished(LocalAction.ACTION_IMPORT_SERVICE_FINISHED);
        } catch (Exception e) {
            notifyTaskFailed(LocalAction.ACTION_IMPORT_SERVICE_FAILED, e);
        }
    }

    private void handleExport(@NonNull Intent intent) {
        notifyTaskStarted(LocalAction.ACTION_EXPORT_SERVICE_STARTED);
        try {
            // TODO: export logic here
            Thread.sleep(5000);

            notifyTaskFinished(LocalAction.ACTION_EXPORT_SERVICE_FINISHED);
        } catch (Exception e) {
            notifyTaskFailed(LocalAction.ACTION_EXPORT_SERVICE_FAILED, e);
        }
    }

    private void notifyTaskStarted(String action) {
        Intent intent = new Intent(action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFinished(String action) {
        Intent intent = new Intent(action);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void notifyTaskFailed(String action, Exception exception) {
        Intent intent = new Intent(action);
        intent.putExtra(EXCEPTION, exception);
        mBroadcastManager.sendBroadcast(intent);
    }
}