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

import android.content.ContentResolver;

import com.oriondev.moneywallet.storage.database.DatabaseImporter;
import com.oriondev.moneywallet.storage.database.ImportException;
import com.oriondev.moneywallet.storage.database.SQLDatabaseImporter;
import com.oriondev.moneywallet.storage.database.model.Attachment;
import com.oriondev.moneywallet.storage.database.model.Budget;
import com.oriondev.moneywallet.storage.database.model.BudgetWallet;
import com.oriondev.moneywallet.storage.database.model.Category;
import com.oriondev.moneywallet.storage.database.model.Currency;
import com.oriondev.moneywallet.storage.database.model.Debt;
import com.oriondev.moneywallet.storage.database.model.DebtPerson;
import com.oriondev.moneywallet.storage.database.model.Event;
import com.oriondev.moneywallet.storage.database.model.EventPerson;
import com.oriondev.moneywallet.storage.database.model.Person;
import com.oriondev.moneywallet.storage.database.model.Place;
import com.oriondev.moneywallet.storage.database.model.RecurrentTransaction;
import com.oriondev.moneywallet.storage.database.model.RecurrentTransfer;
import com.oriondev.moneywallet.storage.database.model.Saving;
import com.oriondev.moneywallet.storage.database.model.Transaction;
import com.oriondev.moneywallet.storage.database.model.TransactionAttachment;
import com.oriondev.moneywallet.storage.database.model.TransactionModel;
import com.oriondev.moneywallet.storage.database.model.TransactionPerson;
import com.oriondev.moneywallet.storage.database.model.Transfer;
import com.oriondev.moneywallet.storage.database.model.TransferAttachment;
import com.oriondev.moneywallet.storage.database.model.TransferModel;
import com.oriondev.moneywallet.storage.database.model.TransferPerson;
import com.oriondev.moneywallet.storage.database.model.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an implementation of DatabaseImporter and it is able to read and parse a stream
 * of JSON data. The JSON data must be well formatted and ordered because it is parsed on the
 * fly to avoid huge memory allocations when the backup file is very large.
 */
public class JSONDatabaseImporter implements DatabaseImporter {

    private final JSONDataStreamReader mReader;
    private final JSONDataInputFactory mFactory;

    private int mVersion;

    public JSONDatabaseImporter(InputStream inputStream) throws ImportException {
        try {
            mReader = new JSONDataStreamReader(inputStream);
            mFactory = new JSONDataInputFactory();
        } catch (IOException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importHeader() throws ImportException {
        try {
            if (JSONDatabase.Header.OBJECT.equals(mReader.readName())) {
                JSONObject object = mReader.readObject();
                mVersion = object.getInt(JSONDatabase.Header.VERSION_CODE);
                if (mVersion > JSONDatabase.MAX_SUPPORTED_VERSION) {
                    throw new ImportException("This backup belongs to a newer version of the " +
                            "application and cannot be imported. Please update the application " +
                            "to restore it.");
                } else if (mVersion < JSONDatabase.MIN_SUPPORTED_VERSION) {
                    throw new ImportException("This backup is too old and no more supported by this " +
                            "version of the application. It cannot be restored.");
                }
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importCurrencies(ContentResolver contentResolver) throws ImportException {
        // currencies are stored starting from backup version >= 2
        if (mVersion >= 2) {
            try {
                if (JSONDatabase.Currency.ARRAY.equals(mReader.readName())) {
                    mReader.beginArray();
                    while (mReader.hasArrayAnotherObject()) {
                        JSONObject object = mReader.readObject();
                        Currency currency = mFactory.getCurrency(object);
                        SQLDatabaseImporter.insert(contentResolver, currency);
                    }
                    mReader.endArray();
                } else {
                    throw new ImportException("Wrong array name (expected = 'currencies')");
                }
            } catch (IOException | JSONException e) {
                throw new ImportException(e.getMessage());
            }
        }
    }

    @Override
    public void importWallets(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Wallet.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Wallet wallet = mFactory.getWallet(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, wallet);
                    mFactory.cacheWallet(wallet.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'wallets')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importCategories(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Category.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Category category = mFactory.getCategory(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, category);
                    mFactory.cacheCategory(category.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'categories')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importEvents(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Event.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Event event = mFactory.getEvent(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, event);
                    mFactory.cacheEvent(event.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'events')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importPlaces(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Place.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Place place = mFactory.getPlace(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, place);
                    mFactory.cachePlace(place.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'places')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importPeople(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Person.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Person person = mFactory.getPerson(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, person);
                    mFactory.cachePerson(person.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'people')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importEventPeople(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.EventPeople.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    EventPerson eventPerson = mFactory.getEventPerson(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, eventPerson);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'event_people')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importDebts(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Debt.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Debt debt = mFactory.getDebt(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, debt);
                    mFactory.cacheDebt(debt.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'debts')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importDebtPeople(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.DebtPeople.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    DebtPerson debtPerson = mFactory.getDebtPerson(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, debtPerson);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'debt_people')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importBudgets(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Budget.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Budget budget = mFactory.getBudget(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, budget);
                    mFactory.cacheBudget(budget.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'budgets')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importBudgetWallets(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.BudgetWallet.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    BudgetWallet budgetWallet = mFactory.getBudgetWallet(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, budgetWallet);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'budget_wallets')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importSavings(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Saving.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Saving saving = mFactory.getSaving(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, saving);
                    mFactory.cacheSaving(saving.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'savings')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importRecurrentTransactions(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.RecurrentTransaction.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    RecurrentTransaction transaction = mFactory.getRecurrentTransaction(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transaction);
                    mFactory.cacheRecurrentTransaction(transaction.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'recurrent_transactions')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importRecurrentTransfers(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.RecurrentTransfer.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    RecurrentTransfer transfer = mFactory.getRecurrentTransfer(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transfer);
                    mFactory.cacheRecurrentTransfer(transfer.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'recurrent_transfers')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransactions(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Transaction.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Transaction transaction = mFactory.getTransaction(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transaction);
                    mFactory.cacheTransaction(transaction.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transactions')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransactionPeople(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransactionPeople.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransactionPerson transactionPerson = mFactory.getTransactionPerson(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transactionPerson);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transaction_people')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransactionModels(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransactionModel.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransactionModel transactionModel = mFactory.getTransactionModel(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transactionModel);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transaction_models')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransfers(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Transfer.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Transfer transfer = mFactory.getTransfer(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transfer);
                    mFactory.cacheTransfer(transfer.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transfers')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransferPeople(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransferPeople.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransferPerson transferPerson = mFactory.getTransferPerson(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transferPerson);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transfer_people')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransferModels(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransferModel.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransferModel transferModel = mFactory.getTransferModel(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transferModel);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transfer_models')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importAttachments(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.Attachment.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    Attachment attachment = mFactory.getAttachment(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, attachment);
                    mFactory.cacheAttachment(attachment.mUUID, id);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'attachments')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransactionAttachments(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransactionAttachment.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransactionAttachment transactionAttachment = mFactory.getTransactionAttachment(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transactionAttachment);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transaction_attachments')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void importTransferAttachments(ContentResolver contentResolver) throws ImportException {
        try {
            if (JSONDatabase.TransferAttachment.ARRAY.equals(mReader.readName())) {
                mReader.beginArray();
                while (mReader.hasArrayAnotherObject()) {
                    JSONObject object = mReader.readObject();
                    TransferAttachment transferAttachment = mFactory.getTransferAttachment(object);
                    long id = SQLDatabaseImporter.insert(contentResolver, transferAttachment);
                }
                mReader.endArray();
            } else {
                throw new ImportException("Wrong array name (expected = 'transfer_attachments')");
            }
        } catch (IOException | JSONException e) {
            throw new ImportException(e.getMessage());
        }
    }

    @Override
    public void close() throws ImportException {
        try {
            mReader.close();
        } catch (IOException e) {
            throw new ImportException(e.getMessage());
        }
    }
}