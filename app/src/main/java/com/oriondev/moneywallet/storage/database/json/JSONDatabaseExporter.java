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

package com.oriondev.moneywallet.storage.database.json;

import android.database.Cursor;

import com.oriondev.moneywallet.storage.database.DatabaseExporter;
import com.oriondev.moneywallet.storage.database.ExportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseExporter;
import com.oriondev.moneywallet.storage.database.model.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by andrea on 28/10/18.
 */
public class JSONDatabaseExporter implements DatabaseExporter {

    private final JSONDataStreamWriter mWriter;
    private final JSONDataOutputFactory mFactory;

    public JSONDatabaseExporter(OutputStream outputStream) throws ExportException {
        try {
            mWriter = new JSONDataStreamWriter(outputStream);
            mFactory = new JSONDataOutputFactory();
        } catch (IOException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportHeader() throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Header.OBJECT);
            JSONObject object = new JSONObject();
            object.put(JSONDatabase.Header.VERSION_CODE, JSONDatabase.VERSION);
            mWriter.writeJSONObject(object);
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportCurrencies(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Currency.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Currency currency = SQLDatabaseExporter.getCurrency(cursor);
                JSONObject object = mFactory.getObject(currency);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportWallets(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Wallet.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Wallet wallet = SQLDatabaseExporter.getWallet(cursor);
                JSONObject object = mFactory.getObject(wallet);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportCategories(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Category.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Category category = SQLDatabaseExporter.getCategory(cursor);
                JSONObject object = mFactory.getObject(category);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportEvents(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Event.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Event event = SQLDatabaseExporter.getEvent(cursor);
                JSONObject object = mFactory.getObject(event);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportPlaces(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Place.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Place place = SQLDatabaseExporter.getPlace(cursor);
                JSONObject object = mFactory.getObject(place);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportPeople(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Person.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Person person = SQLDatabaseExporter.getPerson(cursor);
                JSONObject object = mFactory.getObject(person);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportEventPeople(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.EventPeople.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                EventPerson eventPerson = SQLDatabaseExporter.getEventPerson(cursor);
                JSONObject object = mFactory.getObject(eventPerson);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportDebts(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Debt.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Debt debt = SQLDatabaseExporter.getDebt(cursor);
                JSONObject object = mFactory.getObject(debt);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportDebtPeople(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.DebtPeople.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                DebtPerson debtPerson = SQLDatabaseExporter.getDebtPerson(cursor);
                JSONObject object = mFactory.getObject(debtPerson);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportBudgets(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Budget.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Budget budget = SQLDatabaseExporter.getBudget(cursor);
                JSONObject object = mFactory.getObject(budget);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportBudgetWallets(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.BudgetWallet.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                BudgetWallet budgetWallet = SQLDatabaseExporter.getBudgetWallet(cursor);
                JSONObject object = mFactory.getObject(budgetWallet);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportSavings(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Saving.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Saving saving = SQLDatabaseExporter.getSaving(cursor);
                JSONObject object = mFactory.getObject(saving);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportRecurrentTransactions(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.RecurrentTransaction.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                RecurrentTransaction recurrentTransaction = SQLDatabaseExporter.getRecurrentTransaction(cursor);
                JSONObject object = mFactory.getObject(recurrentTransaction);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportRecurrentTransfers(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.RecurrentTransfer.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                RecurrentTransfer recurrentTransfer = SQLDatabaseExporter.getRecurrentTransfer(cursor);
                JSONObject object = mFactory.getObject(recurrentTransfer);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransactions(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Transaction.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Transaction transaction = SQLDatabaseExporter.getTransaction(cursor);
                JSONObject object = mFactory.getObject(transaction);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransactionPeople(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransactionPeople.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                TransactionPerson transactionPerson = SQLDatabaseExporter.getTransactionPerson(cursor);
                JSONObject object = mFactory.getObject(transactionPerson);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransactionModels(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransactionModel.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                TransactionModel transactionModel = SQLDatabaseExporter.getTransactionModel(cursor);
                JSONObject object = mFactory.getObject(transactionModel);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransfers(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Transfer.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Transfer transfer = SQLDatabaseExporter.getTransfer(cursor);
                JSONObject object = mFactory.getObject(transfer);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransferPeople(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransferPeople.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                TransferPerson transferPerson = SQLDatabaseExporter.getTransferPerson(cursor);
                JSONObject object = mFactory.getObject(transferPerson);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransferModels(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransferModel.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                TransferModel transferModel = SQLDatabaseExporter.getTransferModel(cursor);
                JSONObject object = mFactory.getObject(transferModel);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportAttachments(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.Attachment.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                Attachment attachment = SQLDatabaseExporter.getAttachment(cursor);
                JSONObject object = mFactory.getObject(attachment);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransactionAttachments(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransactionAttachment.ARRAY);
            mWriter.beginArray();
            while (cursor.moveToNext()) {
                TransactionAttachment transactionAttachment = SQLDatabaseExporter.getTransactionAttachment(cursor);
                JSONObject object = mFactory.getObject(transactionAttachment);
                mWriter.writeJSONObject(object);
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void exportTransferAttachments(Cursor cursor) throws ExportException {
        try {
            mWriter.writeName(JSONDatabase.TransferAttachment.ARRAY);
            mWriter.beginArray();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    TransferAttachment transferAttachment = SQLDatabaseExporter.getTransferAttachment(cursor);
                    JSONObject object = mFactory.getObject(transferAttachment);
                    mWriter.writeJSONObject(object);
                }
                cursor.close();
            }
            mWriter.endArray();
        } catch (IOException | JSONException e) {
            throw new ExportException(e.getMessage());
        }
    }

    @Override
    public void close() throws ExportException {
        try {
            mWriter.close();
        } catch (IOException e) {
            throw new ExportException(e.getMessage());
        }
    }
}