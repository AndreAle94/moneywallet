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

import android.database.Cursor;

/**
 * Created by andrea on 28/10/18.
 */

public interface DatabaseExporter {

    void exportHeader() throws ExportException;

    void exportCurrencies(Cursor cursor) throws ExportException;

    void exportWallets(Cursor cursor) throws ExportException;

    void exportCategories(Cursor cursor) throws ExportException;

    void exportEvents(Cursor cursor) throws ExportException;

    void exportPlaces(Cursor cursor) throws ExportException;

    void exportPeople(Cursor cursor) throws ExportException;

    void exportEventPeople(Cursor cursor) throws ExportException;

    void exportDebts(Cursor cursor) throws ExportException;

    void exportDebtPeople(Cursor cursor) throws ExportException;

    void exportBudgets(Cursor cursor) throws ExportException;

    void exportBudgetWallets(Cursor cursor) throws ExportException;

    void exportSavings(Cursor cursor) throws ExportException;

    void exportRecurrentTransactions(Cursor cursor) throws ExportException;

    void exportRecurrentTransfers(Cursor cursor) throws ExportException;

    void exportTransactions(Cursor cursor) throws ExportException;

    void exportTransactionPeople(Cursor cursor) throws ExportException;

    void exportTransactionModels(Cursor cursor) throws ExportException;

    void exportTransfers(Cursor cursor) throws ExportException;

    void exportTransferPeople(Cursor cursor) throws ExportException;

    void exportTransferModels(Cursor cursor) throws ExportException;

    void exportAttachments(Cursor cursor) throws ExportException;

    void exportTransactionAttachments(Cursor cursor) throws ExportException;

    void exportTransferAttachments(Cursor cursor) throws ExportException;

    void close() throws ExportException;
}