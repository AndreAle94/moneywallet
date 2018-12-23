package com.oriondev.moneywallet.storage.database.data.csv;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.opencsv.CSVParserWriter;
import com.opencsv.CSVWriter;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.data.AbstractDataExporter;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 21/12/18.
 */
public class CSVDataExporter extends AbstractDataExporter {

    private final File mOutputFile;
    private final CSVWriter mWriter;
    private final MoneyFormatter mMoneyFormatter;

    private boolean mShouldLoadPeople = false;

    public CSVDataExporter(Context context, File folder) throws IOException {
        super(context, folder);
        mOutputFile = new File(folder, getDefaultFileName(".csv"));
        mWriter = new CSVWriter(new FileWriter(mOutputFile));
        mMoneyFormatter = MoneyFormatter.getInstance();
        mMoneyFormatter.setCurrencyEnabled(false);
        mMoneyFormatter.setRoundDecimalsEnabled(false);
        mMoneyFormatter.setGroupDigitEnabled(false);
    }

    @Override
    public boolean isMultiWalletSupported() {
        // in a csv file we cannot create different sections for the transactions of
        // each wallet so we have to list all the transactions inside the same file
        return false;
    }

    @Override
    public String[] getColumns(boolean uniqueWallet, String[] optionalColumns) {
        List<String> contractColumns = new ArrayList<>();
        contractColumns.add(Constants.COLUMN_WALLET);
        contractColumns.add(Constants.COLUMN_CURRENCY);
        contractColumns.add(Constants.COLUMN_CATEGORY);
        contractColumns.add(Constants.COLUMN_DATETIME);
        contractColumns.add(Constants.COLUMN_MONEY);
        contractColumns.add(Constants.COLUMN_DESCRIPTION);
        if (optionalColumns != null) {
            for (String column : optionalColumns) {
                switch (column) {
                    case COLUMN_EVENT:
                        contractColumns.add(Constants.COLUMN_EVENT);
                        break;
                    case COLUMN_PEOPLE:
                        contractColumns.add(Constants.COLUMN_PEOPLE);
                        mShouldLoadPeople = true;
                        break;
                    case COLUMN_PLACE:
                        contractColumns.add(Constants.COLUMN_PLACE);
                        break;
                    case COLUMN_NOTE:
                        contractColumns.add(Constants.COLUMN_NOTE);
                        break;
                }
            }
        }
        return contractColumns.toArray(new String[contractColumns.size()]);
    }

    @Override
    public boolean shouldLoadPeople() {
        return mShouldLoadPeople;
    }

    @Override
    public void exportData(Cursor cursor, String[] columns, Wallet... wallets) throws IOException {
        // initialize the header line
        mWriter.writeNext(columns);
        // export all the rows
        while (cursor.moveToNext()) {
            // for each row, we need to export all the fields as string
            String[] csvRow = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                switch (columns[i]) {
                    case Constants.COLUMN_WALLET:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_NAME));
                        break;
                    case Constants.COLUMN_CURRENCY:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY));
                        break;
                    case Constants.COLUMN_CATEGORY:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.CATEGORY_NAME));
                        break;
                    case Constants.COLUMN_DATETIME:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.DATE));
                        break;
                    case Constants.COLUMN_MONEY:
                        CurrencyUnit currencyUnit = CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY)));
                        long money = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY));
                        int direction = cursor.getInt(cursor.getColumnIndex(Contract.Transaction.DIRECTION));
                        if (direction == Contract.Direction.EXPENSE) {
                            money *= -1;
                        }
                        csvRow[i] = mMoneyFormatter.getNotTintedString(currencyUnit, money);
                        break;
                    case Constants.COLUMN_DESCRIPTION:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.DESCRIPTION));
                        break;
                    case Constants.COLUMN_EVENT:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_NAME));
                        break;
                    case Constants.COLUMN_PEOPLE:
                        List<Long> peopleIds = Contract.parseObjectIds(cursor.getString(cursor.getColumnIndex(Contract.Transaction.PEOPLE_IDS)));
                        if (peopleIds != null && !peopleIds.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            for (Long personId : peopleIds) {
                                String name = getPersonName(personId);
                                if (!TextUtils.isEmpty(name)) {
                                    if (builder.length() > 0) {
                                        builder.append(",");
                                    }
                                    builder.append(name);
                                }
                            }
                            csvRow[i] = builder.toString();
                        } else {
                            csvRow[i] = null;
                        }
                        break;
                    case Constants.COLUMN_PLACE:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.PLACE_NAME));
                        break;
                    case Constants.COLUMN_NOTE:
                        csvRow[i] = cursor.getString(cursor.getColumnIndex(Contract.Transaction.NOTE));
                        break;
                }
            }
            mWriter.writeNext(csvRow);
        }
    }

    @Override
    public void close() throws IOException {
        mWriter.close();
    }

    @Override
    public File getOutputFile() {
        return mOutputFile;
    }

    @Override
    public String getResultType() {
        return "application/vnd.ms-excel";
    }
}