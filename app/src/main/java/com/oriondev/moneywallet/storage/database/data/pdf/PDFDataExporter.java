package com.oriondev.moneywallet.storage.database.data.pdf;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.data.AbstractDataExporter;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 22/12/18.
 */
public class PDFDataExporter extends AbstractDataExporter {

    private final File mOutputFile;
    private final Document mDocument;
    private final MoneyFormatter mMoneyFormatter;

    private boolean mShouldLoadPeople = false;
    private int mChapterCount = 0;

    public PDFDataExporter(Context context, File folder) throws IOException {
        super(context, folder);
        mOutputFile = new File(folder, getDefaultFileName(".pdf"));
        mMoneyFormatter = MoneyFormatter.getInstance();
        mDocument = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(mDocument, new FileOutputStream(mOutputFile));
        } catch (DocumentException e) {
            throw new IOException(e);
        }
        mDocument.addAuthor("MoneyWallet - Expense Manager");
        mDocument.open();
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
        try {
            Chapter chapter = createChapter(wallets);
            chapter.add(createTable(cursor, columns));
            mDocument.add(chapter);
        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    private Chapter createChapter(Wallet... wallets) throws DocumentException {
        StringBuilder chapterTitleBuilder = new StringBuilder();
        if (wallets != null && wallets.length > 0) {
            for (Wallet wallet : wallets) {
                if (chapterTitleBuilder.length() != 0) {
                    chapterTitleBuilder.append(", ");
                }
                chapterTitleBuilder.append(wallet.getName());
            }
        } else {
            chapterTitleBuilder.append(getContext().getString(R.string.hint_unknown));
        }
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        Chunk chunk = new Chunk(chapterTitleBuilder.toString(), font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setSpacingAfter(30);
        return new Chapter(paragraph, ++mChapterCount);
    }

    private PdfPTable createTable(Cursor cursor, String[] columns) throws DocumentException {
        PdfPTable table = createTable(columns);
        for (int i = 0; i < cursor.getCount(); i++) {
            // move the cursor to the fixed position
            cursor.moveToPosition(i);
            // for each line of the cursor, write a line in the sheet
            for (String column : columns) {
                String label = null;
                switch (column) {
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
                table.addCell(label);
            }
        }
        return table;
    }

    private PdfPTable createTable(String[] columns) throws DocumentException {
        // create the table with a fixed number of columns
        PdfPTable table = new PdfPTable(columns.length);
        table.setWidthPercentage(100f);
        table.getDefaultCell().setBackgroundColor(BaseColor.YELLOW);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        // initialize the table creating the header line
        Context context = getContext();
        for (String column : columns) {
            String label = null;
            switch (column) {
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
            table.addCell(label);
        }
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        return table;
    }

    @Override
    public void close() throws IOException {
        mDocument.close();
    }

    @Override
    public File getOutputFile() {
        return mOutputFile;
    }

    @Override
    public String getResultType() {
        return "application/pdf";
    }
}