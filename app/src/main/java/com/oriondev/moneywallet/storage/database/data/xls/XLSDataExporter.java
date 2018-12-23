package com.oriondev.moneywallet.storage.database.data.xls;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.data.AbstractDataExporter;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by andrea on 22/12/18.
 */
public class XLSDataExporter extends AbstractDataExporter {

    private final File mOutputFile;
    private final WritableWorkbook mWorkbook;
    private final MoneyFormatter mMoneyFormatter;

    private boolean mShouldLoadPeople = false;

    public XLSDataExporter(Context context, File folder) throws IOException {
        super(context, folder);
        mOutputFile = new File(folder, getDefaultFileName(".xls"));
        mWorkbook = Workbook.createWorkbook(mOutputFile);
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    public boolean isMultiWalletSupported() {
        return true;
    }

    @Override
    public String[] getColumns(boolean uniqueWallet, String[] optionalColumns) {
        List<String> contractColumns = new ArrayList<>();
        contractColumns.add(Constants.COLUMN_DATETIME);
        contractColumns.add(Constants.COLUMN_CATEGORY);
        contractColumns.add(Constants.COLUMN_MONEY);
        if (uniqueWallet) {
            contractColumns.add(Constants.COLUMN_WALLET);
        }
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
        WritableSheet sheet = mWorkbook.createSheet(getSheetName(wallets), getSheetIndex());
        try {
            // write the header of each column
            writeSheetHeader(sheet, columns);
            // write the body of the wallet
            for (int r = 1; r <= cursor.getCount(); r++) {
                // move the cursor to the fixed position
                cursor.moveToPosition(r - 1);
                // for each line of the cursor, write a line in the sheet
                for (int i = 0; i < columns.length; i++) {
                    String label = null;
                    switch (columns[i]) {
                        case Constants.COLUMN_DATETIME:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.DATE));
                            break;
                        case Constants.COLUMN_CATEGORY:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.CATEGORY_NAME));
                            break;
                        case Constants.COLUMN_MONEY:
                            CurrencyUnit currencyUnit = CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY)));
                            long money = cursor.getLong(cursor.getColumnIndex(Contract.Transaction.MONEY));
                            int direction = cursor.getInt(cursor.getColumnIndex(Contract.Transaction.DIRECTION));
                            if (direction == Contract.Direction.EXPENSE) {
                                money *= -1;
                            }
                            label = mMoneyFormatter.getNotTintedString(currencyUnit, money);
                            break;
                        case Constants.COLUMN_WALLET:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.WALLET_NAME));
                            break;
                        case Constants.COLUMN_DESCRIPTION:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.DESCRIPTION));
                            break;
                        case Constants.COLUMN_EVENT:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.EVENT_NAME));
                            break;
                        case Constants.COLUMN_PEOPLE:
                            List<Long> peopleIds = Contract.parseObjectIds(cursor.getString(cursor.getColumnIndex(Contract.Transaction.PEOPLE_IDS)));
                            if (peopleIds != null && !peopleIds.isEmpty()) {
                                StringBuilder builder = new StringBuilder();
                                for (Long personId : peopleIds) {
                                    String name = getPersonName(personId);
                                    if (!TextUtils.isEmpty(name)) {
                                        if (builder.length() > 0) {
                                            builder.append(", ");
                                        }
                                        builder.append(name);
                                    }
                                }
                                label = builder.toString();
                            } else {
                                label = null;
                            }
                            break;
                        case Constants.COLUMN_PLACE:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.PLACE_NAME));
                            break;
                        case Constants.COLUMN_NOTE:
                            label = cursor.getString(cursor.getColumnIndex(Contract.Transaction.NOTE));
                            break;
                    }
                    sheet.addCell(new Label(i, r, label));
                }
            }
            // calculate the width of each column to fit the values
            for (int i = 0; i < columns.length; i++) {
                CellView cellView = sheet.getColumnView(i);
                cellView.setAutosize(true);
                sheet.setColumnView(i, cellView);
            }
        } catch (WriteException e) {
            throw new IOException(e);
        }
    }

    private String getSheetName(Wallet... wallets) {
        if (wallets != null && wallets.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (Wallet wallet : wallets) {
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(wallet.getName());
            }
            return builder.toString();
        }
        return getContext().getString(R.string.hint_unknown);
    }

    private int getSheetIndex() {
        return mWorkbook.getNumberOfSheets();
    }

    private void writeSheetHeader(WritableSheet sheet, String[] columns) throws WriteException {
        Context context = getContext();
        for (int i = 0; i < columns.length; i++) {
            String label = null;
            switch (columns[i]) {
                case Constants.COLUMN_DATETIME:
                    label = context.getString(R.string.hint_date);
                    break;
                case Constants.COLUMN_CATEGORY:
                    label = context.getString(R.string.hint_category);
                    break;
                case Constants.COLUMN_MONEY:
                    label = context.getString(R.string.hint_money);
                    break;
                case Constants.COLUMN_WALLET:
                    label = context.getString(R.string.hint_wallet);
                    break;
                case Constants.COLUMN_DESCRIPTION:
                    label = context.getString(R.string.hint_description);
                    break;
                case Constants.COLUMN_EVENT:
                    label = context.getString(R.string.hint_event);
                    break;
                case Constants.COLUMN_PEOPLE:
                    label = context.getString(R.string.hint_people);
                    break;
                case Constants.COLUMN_PLACE:
                    label = context.getString(R.string.hint_place);
                    break;
                case Constants.COLUMN_NOTE:
                    label = context.getString(R.string.hint_note);
                    break;
            }
            WritableFont cellFont = new WritableFont(WritableFont.TAHOMA, 10);
            cellFont.setBoldStyle(WritableFont.BOLD);
            WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
            sheet.addCell(new Label(i, 0, label, cellFormat));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            mWorkbook.write();
            mWorkbook.close();
        } catch (WriteException e) {
            throw new IOException(e);
        }
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