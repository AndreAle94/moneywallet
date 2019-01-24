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

package com.oriondev.moneywallet.storage.database;

import android.content.ContentResolver;

/**
 * Created by andrea on 25/10/18.
 */

public interface DatabaseImporter {

    void importHeader() throws ImportException;

    void importCurrencies(ContentResolver contentResolver) throws ImportException;

    void importWallets(ContentResolver contentResolver) throws ImportException;

    void importCategories(ContentResolver contentResolver) throws ImportException;

    void importEvents(ContentResolver contentResolver) throws ImportException;

    void importPlaces(ContentResolver contentResolver) throws ImportException;

    void importPeople(ContentResolver contentResolver) throws ImportException;

    void importEventPeople(ContentResolver contentResolver) throws ImportException;

    void importDebts(ContentResolver contentResolver) throws ImportException;

    void importDebtPeople(ContentResolver contentResolver) throws ImportException;

    void importBudgets(ContentResolver contentResolver) throws ImportException;

    void importBudgetWallets(ContentResolver contentResolver) throws ImportException;

    void importSavings(ContentResolver contentResolver) throws ImportException;

    void importRecurrentTransactions(ContentResolver contentResolver) throws ImportException;

    void importRecurrentTransfers(ContentResolver contentResolver) throws ImportException;

    void importTransactions(ContentResolver contentResolver) throws ImportException;

    void importTransactionPeople(ContentResolver contentResolver) throws ImportException;

    void importTransactionModels(ContentResolver contentResolver) throws ImportException;

    void importTransfers(ContentResolver contentResolver) throws ImportException;

    void importTransferPeople(ContentResolver contentResolver) throws ImportException;

    void importTransferModels(ContentResolver contentResolver) throws ImportException;

    void importAttachments(ContentResolver contentResolver) throws ImportException;

    void importTransactionAttachments(ContentResolver contentResolver) throws ImportException;

    void importTransferAttachments(ContentResolver contentResolver) throws ImportException;

    void close() throws ImportException;
}