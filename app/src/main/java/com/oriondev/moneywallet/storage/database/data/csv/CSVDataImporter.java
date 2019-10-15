package com.oriondev.moneywallet.storage.database.data.csv;

import android.content.Context;
import android.text.TextUtils;

import com.opencsv.CSVReaderHeaderAware;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.data.AbstractDataImporter;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by andrea on 23/12/18.
 */
public class CSVDataImporter extends AbstractDataImporter {

    private final CSVReaderHeaderAware mReader;

    public CSVDataImporter(Context context, File file) throws IOException {
        super(context, file);
        mReader = new CSVReaderHeaderAware(new FileReader(file));
    }

    @Override
    public void importData() throws IOException {
        Map<String, String> lineMap = mReader.readMap();
        while (lineMap != null) {
            System.out.println("Importing line");
            // extract required information from the csv file
            String wallet = getTrimmedString(lineMap.get(Constants.COLUMN_WALLET));
            String currency = getTrimmedString(lineMap.get(Constants.COLUMN_CURRENCY));
            String category = getTrimmedString(lineMap.get(Constants.COLUMN_CATEGORY));
            String datetimeString = getTrimmedString(lineMap.get(Constants.COLUMN_DATETIME));
            String moneyString = getTrimmedString(lineMap.get(Constants.COLUMN_MONEY));
            // if one of this information is missing, we should stop the import
            // process because the file is not valid
            if (TextUtils.isEmpty(wallet) || TextUtils.isEmpty(currency) || TextUtils.isEmpty(category) || TextUtils.isEmpty(datetimeString) || TextUtils.isEmpty(moneyString)) {
                throw new RuntimeException("Invalid csv file: one or more required columns are missing");
            }
            // extract the optional information from the csv file
            String description = getTrimmedString(lineMap.get(Constants.COLUMN_DESCRIPTION));
            String event = getTrimmedString(lineMap.get(Constants.COLUMN_EVENT));
            String people = getTrimmedString(lineMap.get(Constants.COLUMN_PEOPLE));
            String place = getTrimmedString(lineMap.get(Constants.COLUMN_PLACE));
            String note = getTrimmedString(lineMap.get(Constants.COLUMN_NOTE));
            // try to build the internal transaction state starting from strings
            CurrencyUnit currencyUnit = CurrencyManager.getCurrency(currency);
            if (currencyUnit == null) {
                throw new RuntimeException("Unknown currency unit (" + currency + ")");
            }
            BigDecimal moneyDecimal;
            try {
                moneyDecimal = new BigDecimal(moneyString.replaceAll(",", "."));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid money amount (" + e.getMessage() + ")");
            }
            BigDecimal decimalMultiply = new BigDecimal(Math.pow(10, currencyUnit.getDecimals()));
            moneyDecimal = moneyDecimal.multiply(decimalMultiply);
            long money = moneyDecimal.longValue();
            int direction = money < 0 ? Contract.Direction.EXPENSE : Contract.Direction.INCOME;
            Date datetime = DateUtils.getDateFromSQLDateTimeString(datetimeString);
            insertTransaction(wallet, currencyUnit, category, datetime, Math.abs(money), direction, description, event, place, people, note);
            lineMap = mReader.readMap();
        }
    }

    private String getTrimmedString(String source) {
        if (source != null) {
            return source.trim();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        mReader.close();
    }
}
