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

/**
 * This class contains all the static strings that defines the raw SQL database structure.
 * The access is granted only to the current package because for a better management of the
 * internal items, the content provider will serve the cursor with a different structure.
 * For a more detailed explanation please look at {@link Contract}.
 */
/*package-local*/ class Schema {

    /**
     * This class defines the common columns for all SQL tables.
     * Those columns are necessary for future updates in case of creation of a SyncAdapter.
     */
    private static class BaseTable {
        /*package-local*/ static final String UUID = "uuid";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Alias {
        /*package-local*/ static final String TOTAL_MONEY = "total_money";
        /*package-local*/ static final String PROGRESS = "progress";
        /*package-local*/ static final String CATEGORY_GROUP_ID = "category_group_id";
        /*package-local*/ static final String CATEGORY_GROUP_NAME = "category_group_name";
        /*package-local*/ static final String CATEGORY_GROUP_INDEX = "category_group_index";
    }

    /*package-local*/ static class Currency extends BaseTable {
        /*package-local*/ static final String TABLE = "currencies";
        /*package-local*/ static final String NAME = "currency_name";
        /*package-local*/ static final String ISO = "currency_iso";
        /*package-local*/ static final String SYMBOL = "currency_symbol";
        /*package-local*/ static final String DECIMALS = "currency_decimals";
        /*package-local*/ static final String FAVOURITE = "currency_favourite";
    }

    /*package-local*/ static class Wallet extends BaseTable {
        /*package-local*/ static final String TABLE = "wallets";
        /*package-local*/ static final String ID = "wallet_id";
        /*package-local*/ static final String NAME = "wallet_name";
        /*package-local*/ static final String ICON = "wallet_icon";
        /*package-local*/ static final String CURRENCY = "wallet_currency";
        /*package-local*/ static final String NOTE = "wallet_note";
        /*package-local*/ static final String START_MONEY = "wallet_start_money";
        /*package-local*/ static final String COUNT_IN_TOTAL = "wallet_count_in_total";
        /*package-local*/ static final String ARCHIVED = "wallet_archived";
        /*package-local*/ static final String TAG = "wallet_tag";
        /*package-local*/ static final String INDEX = "wallet_index";
    }

    /*package-local*/ static final class Category extends BaseTable {
        /*package-local*/ static final String TABLE = "categories";
        /*package-local*/ static final String ID = "category_id";
        /*package-local*/ static final String NAME = "category_name";
        /*package-local*/ static final String ICON = "category_icon";
        /*package-local*/ static final String TYPE = "category_type";
        /*package-local*/ static final String PARENT = "category_parent";
        /*package-local*/ static final String TAG = "category_tag";
        /*package-local*/ static final String SHOW_REPORT = "category_show_report";
        /*package-local*/ static final String INDEX = "category_index";
    }

    /*package-local*/ static final class Event extends BaseTable {
        /*package-local*/ static final String TABLE = "events";
        /*package-local*/ static final String ID = "event_id";
        /*package-local*/ static final String NAME = "event_name";
        /*package-local*/ static final String ICON = "event_icon";
        /*package-local*/ static final String NOTE = "event_note";
        /*package-local*/ static final String START_DATE = "event_start_date";
        /*package-local*/ static final String END_DATE = "event_end_date";
        /*package-local*/ static final String TAG = "event_tag";
    }

    /*package-local*/ static final class Place extends BaseTable {
        /*package-local*/ static final String TABLE = "places";
        /*package-local*/ static final String ID = "place_id";
        /*package-local*/ static final String NAME = "place_name";
        /*package-local*/ static final String ICON = "place_icon";
        /*package-local*/ static final String ADDRESS = "place_address";
        /*package-local*/ static final String LATITUDE = "place_latitude";
        /*package-local*/ static final String LONGITUDE = "place_longitude";
        /*package-local*/ static final String TAG = "place_tag";
    }

    /*package-local*/ static final class Person extends BaseTable {
        /*package-local*/ static final String TABLE = "people";
        /*package-local*/ static final String ID = "person_id";
        /*package-local*/ static final String NAME = "person_name";
        /*package-local*/ static final String ICON = "person_icon";
        /*package-local*/ static final String NOTE = "person_note";
        /*package-local*/ static final String TAG = "person_tag";
    }

    /*package-local*/ static final class EventPeople extends BaseTable {
        /*package-local*/ static final String TABLE = "event_people";
        /*package-local*/ static final String EVENT = "_event";
        /*package-local*/ static final String PERSON = "_person";
    }

    /*package-local*/ static final class Debt extends BaseTable {
        /*package-local*/ static final String TABLE = "debts";
        /*package-local*/ static final String ID = "debt_id";
        /*package-local*/ static final String TYPE = "debt_type";
        /*package-local*/ static final String ICON = "debt_icon";
        /*package-local*/ static final String DESCRIPTION = "debt_description";
        /*package-local*/ static final String DATE = "debt_date";
        /*package-local*/ static final String EXPIRATION_DATE = "debt_expiration_date";
        /*package-local*/ static final String WALLET = "debt_wallet";
        /*package-local*/ static final String NOTE = "debt_note";
        /*package-local*/ static final String PLACE = "debt_place";
        /*package-local*/ static final String MONEY = "debt_money";
        /*package-local*/ static final String ARCHIVED = "debt_archived";
        /*package-local*/ static final String TAG = "debt_tag";
    }

    /*package-local*/ static final class DebtPeople extends BaseTable {
        /*package-local*/ static final String TABLE = "debt_people";
        /*package-local*/ static final String DEBT = "_debt";
        /*package-local*/ static final String PERSON = "_person";
    }

    /*package-local*/ static final class Budget extends BaseTable {
        /*package-local*/ static final String TABLE = "budgets";
        /*package-local*/ static final String ID = "budget_id";
        /*package-local*/ static final String TYPE = "budget_type";
        /*package-local*/ static final String CATEGORY = "budget_category";
        /*package-local*/ static final String START_DATE = "budget_start_date";
        /*package-local*/ static final String END_DATE = "budget_end_date";
        /*package-local*/ static final String MONEY = "budget_money";
        /*package-local*/ static final String CURRENCY = "budget_currency";
        /*package-local*/ static final String TAG = "budget_tag";
    }

    /*package-local*/ static final class BudgetWallet extends BaseTable {
        /*package-local*/ static final String TABLE = "budget_wallets";
        /*package-local*/ static final String BUDGET = "_budget";
        /*package-local*/ static final String WALLET = "_wallet";
    }

    /*package-local*/ static final class Saving extends BaseTable {
        /*package-local*/ static final String TABLE = "savings";
        /*package-local*/ static final String ID = "saving_id";
        /*package-local*/ static final String DESCRIPTION = "saving_description";
        /*package-local*/ static final String ICON = "saving_icon";
        /*package-local*/ static final String START_MONEY = "saving_start_money";
        /*package-local*/ static final String END_MONEY = "saving_end_money";
        /*package-local*/ static final String WALLET = "saving_wallet";
        /*package-local*/ static final String END_DATE = "saving_end_date";
        /*package-local*/ static final String COMPLETE = "saving_complete";
        /*package-local*/ static final String NOTE = "saving_note";
        /*package-local*/ static final String TAG = "saving_tag";
    }

    /*package-local*/ static final class Transaction extends BaseTable {
        /*package-local*/ static final String TABLE = "transactions";
        /*package-local*/ static final String ID = "transaction_id";
        /*package-local*/ static final String MONEY = "transaction_money";
        /*package-local*/ static final String DATE = "transaction_date";
        /*package-local*/ static final String DESCRIPTION = "transaction_description";
        /*package-local*/ static final String CATEGORY = "transaction_category";
        /*package-local*/ static final String DIRECTION = "transaction_direction";
        /*package-local*/ static final String TYPE = "transaction_type";
        /*package-local*/ static final String WALLET = "transaction_wallet";
        /*package-local*/ static final String PLACE = "transaction_place";
        /*package-local*/ static final String NOTE = "transaction_note";
        /*package-local*/ static final String SAVING = "transaction_saving";
        /*package-local*/ static final String DEBT = "transaction_debt";
        /*package-local*/ static final String EVENT = "transaction_event";
        /*package-local*/ static final String RECURRENCE = "transaction_recurrence";
        /*package-local*/ static final String CONFIRMED = "transaction_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "transaction_count_in_total";
        /*package-local*/ static final String TAG = "transaction_tag";
    }

    /*package-local*/ static final class TransactionPeople extends BaseTable {
        /*package-local*/ static final String TABLE = "transaction_people";
        /*package-local*/ static final String TRANSACTION = "_transaction";
        /*package-local*/ static final String PERSON = "_person";
    }

    /*package-local*/ static final class Transfer extends BaseTable {
        /*package-local*/ static final String TABLE = "transfers";
        /*package-local*/ static final String ID = "transfer_id";
        /*package-local*/ static final String DESCRIPTION = "transfer_description";
        /*package-local*/ static final String DATE = "transfer_date";
        /*package-local*/ static final String TRANSACTION_FROM = "transfer_transaction_from";
        /*package-local*/ static final String TRANSACTION_TO = "transfer_transaction_to";
        /*package-local*/ static final String TRANSACTION_TAX = "transfer_transaction_tax";
        /*package-local*/ static final String NOTE = "transfer_note";
        /*package-local*/ static final String PLACE = "transfer_place";
        /*package-local*/ static final String EVENT = "transfer_event";
        /*package-local*/ static final String RECURRENCE = "transaction_recurrence";
        /*package-local*/ static final String CONFIRMED = "transfer_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "transfer_count_in_total";
        /*package-local*/ static final String TAG = "transfer_tag";
    }

    /*package-local*/ static final class TransferPeople extends BaseTable {
        /*package-local*/ static final String TABLE = "transfer_people";
        /*package-local*/ static final String TRANSFER = "_transfer";
        /*package-local*/ static final String PERSON = "_person";
    }

    /*package-local*/ static final class TransactionModel extends BaseTable {
        /*package-local*/ static final String TABLE = "transaction_models";
        /*package-local*/ static final String ID = "model_id";
        /*package-local*/ static final String MONEY = "model_transaction_money";
        /*package-local*/ static final String DESCRIPTION = "model_transaction_description";
        /*package-local*/ static final String CATEGORY = "model_transaction_category";
        /*package-local*/ static final String DIRECTION = "model_transaction_direction";
        /*package-local*/ static final String WALLET = "model_transaction_wallet";
        /*package-local*/ static final String PLACE = "model_transaction_place";
        /*package-local*/ static final String NOTE = "model_transaction_note";
        /*package-local*/ static final String EVENT = "model_transaction_event";
        /*package-local*/ static final String CONFIRMED = "model_transaction_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "model_transaction_count_in_total";
        /*package-local*/ static final String TAG = "model_transaction_tag";
    }

    /*package-local*/ static final class TransferModel extends BaseTable {
        /*package-local*/ static final String TABLE = "transfer_models";
        /*package-local*/ static final String ID = "model_id";
        /*package-local*/ static final String DESCRIPTION = "model_transfer_description";
        /*package-local*/ static final String WALLET_FROM = "model_transfer_from_wallet";
        /*package-local*/ static final String WALLET_TO = "model_transfer_to_wallet";
        /*package-local*/ static final String MONEY_FROM = "model_transfer_from_money";
        /*package-local*/ static final String MONEY_TO = "model_transfer_to_money";
        /*package-local*/ static final String MONEY_TAX = "model_transfer_tax_money";
        /*package-local*/ static final String NOTE = "model_transfer_note";
        /*package-local*/ static final String EVENT = "model_transfer_event";
        /*package-local*/ static final String PLACE = "model_transfer_place";
        /*package-local*/ static final String CONFIRMED = "model_transfer_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "model_transfer_count_in_total";
        /*package-local*/ static final String TAG = "model_transfer_tag";
    }

    /*package-local*/ static final class RecurrentTransaction extends BaseTable {
        /*package-local*/ static final String TABLE = "recurrent_transactions";
        /*package-local*/ static final String ID = "recurrent_transaction_id";
        /*package-local*/ static final String MONEY = "recurrent_transaction_money";
        /*package-local*/ static final String DESCRIPTION = "recurrent_transaction_description";
        /*package-local*/ static final String CATEGORY = "recurrent_transaction_category";
        /*package-local*/ static final String DIRECTION = "recurrent_transaction_direction";
        /*package-local*/ static final String WALLET = "recurrent_transaction_wallet";
        /*package-local*/ static final String PLACE = "recurrent_transaction_place";
        /*package-local*/ static final String NOTE = "recurrent_transaction_note";
        /*package-local*/ static final String EVENT = "recurrent_transaction_event";
        /*package-local*/ static final String CONFIRMED = "recurrent_transaction_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "recurrent_transaction_count_in_total";
        /*package-local*/ static final String START_DATE = "recurrent_transaction_start_date";
        /*package-local*/ static final String LAST_OCCURRENCE = "recurrent_transaction_last_occurrence";
        /*package-local*/ static final String NEXT_OCCURRENCE = "recurrent_transaction_next_occurrence";
        /*package-local*/ static final String RULE = "recurrent_transaction_rule";
        /*package-local*/ static final String TAG = "recurrent_transaction_tag";
    }

    /*package-local*/ static final class RecurrentTransfer extends BaseTable {
        /*package-local*/ static final String TABLE = "recurrent_transfers";
        /*package-local*/ static final String ID = "recurrent_transfer_id";
        /*package-local*/ static final String DESCRIPTION = "recurrent_transfer_description";
        /*package-local*/ static final String WALLET_FROM = "recurrent_transfer_from_wallet";
        /*package-local*/ static final String WALLET_TO = "recurrent_transfer_to_wallet";
        /*package-local*/ static final String MONEY_FROM = "recurrent_transfer_from_money";
        /*package-local*/ static final String MONEY_TO = "recurrent_transfer_to_money";
        /*package-local*/ static final String MONEY_TAX = "recurrent_transfer_tax_money";
        /*package-local*/ static final String NOTE = "recurrent_transfer_note";
        /*package-local*/ static final String EVENT = "recurrent_transfer_event";
        /*package-local*/ static final String PLACE = "recurrent_transfer_place";
        /*package-local*/ static final String CONFIRMED = "recurrent_transfer_confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "recurrent_transfer_count_in_total";
        /*package-local*/ static final String START_DATE = "recurrent_transfer_start_date";
        /*package-local*/ static final String LAST_OCCURRENCE = "recurrent_transfer_last_occurrence";
        /*package-local*/ static final String NEXT_OCCURRENCE = "recurrent_transfer_next_occurrence";
        /*package-local*/ static final String RULE = "recurrent_transfer_rule";
        /*package-local*/ static final String TAG = "recurrent_transfer_tag";
    }

    /*package-local*/ static final class Attachment extends BaseTable {
        /*package-local*/ static final String TABLE = "attachments";
        /*package-local*/ static final String ID = "attachment_id";
        /*package-local*/ static final String FILE = "attachment_file";
        /*package-local*/ static final String NAME = "attachment_name";
        /*package-local*/ static final String TYPE = "attachment_type";
        /*package-local*/ static final String SIZE = "attachment_size";
        /*package-local*/ static final String TAG = "attachment_tag";
    }

    /*package-local*/ static final class TransactionAttachment extends BaseTable {
        /*package-local*/ static final String TABLE = "transaction_attachment";
        /*package-local*/ static final String TRANSACTION = "_transaction";
        /*package-local*/ static final String ATTACHMENT = "_attachment";
    }

    /*package-local*/ static final class TransferAttachment extends BaseTable {
        /*package-local*/ static final String TABLE = "transfer_attachment";
        /*package-local*/ static final String TRANSFER = "_transfer";
        /*package-local*/ static final String ATTACHMENT = "_attachment";
    }

    /*package-local*/ static final class CategoryType {
        /*package-local*/ static final int INCOME = 0;
        /*package-local*/ static final int EXPENSE = 1;
        /*package-local*/ static final int SYSTEM = 2;
    }

    /*package-local*/ static final class CategoryTag {
        /*package-local*/ static final String TRANSFER = "system::transfer";
        /*package-local*/ static final String TRANSFER_TAX = "system::transfer_tax";
        /*package-local*/ static final String DEBT = "system::debt";
        /*package-local*/ static final String CREDIT = "system::credit";
        /*package-local*/ static final String PAID_DEBT = "system::paid_debt";
        /*package-local*/ static final String PAID_CREDIT = "system::paid_credit";
        /*package-local*/ static final String TAX = "system::tax";
        /*package-local*/ static final String SAVING_DEPOSIT = "system::deposit";
        /*package-local*/ static final String SAVING_WITHDRAW = "system::withdraw";
    }

    /*package-local*/ static final class BudgetType {
        /*package-local*/ static final int EXPENSES = 0;
        /*package-local*/ static final int INCOMES = 1;
        /*package-local*/ static final int CATEGORY = 2;
    }

    /*package-local*/ static class Direction {
        /*package-local*/ static final int INCOME = 1;
        /*package-local*/ static final int EXPENSE = 0;
    }

    /*package-local*/ static final String CREATE_TABLE_CURRENCY = "CREATE TABLE " + Currency.TABLE + " (" +
            Currency.ISO + " TEXT PRIMARY KEY, " +
            Currency.NAME + " TEXT NOT NULL, " +
            Currency.SYMBOL + " TEXT, " +
            Currency.DECIMALS + " INTEGER NOT NULL, " +
            Currency.FAVOURITE + " INTEGER NOT NULL DEFAULT 0, " +
            Currency.UUID + " TEXT NOT NULL UNIQUE, " +
            Currency.LAST_EDIT + " INTEGER NOT NULL, " +
            Currency.DELETED + " INTEGER NOT NULL DEFAULT 0" +
            ")";

    /*package-local*/ static final String CREATE_TABLE_WALLET = "CREATE TABLE " + Wallet.TABLE + " (" +
            Wallet.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Wallet.NAME + " TEXT NOT NULL, " +
            Wallet.ICON + " TEXT, " +
            Wallet.CURRENCY + " TEXT NOT NULL, " +
            Wallet.START_MONEY + " INTEGER NOT NULL DEFAULT 0, " +
            Wallet.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            Wallet.NOTE + " TEXT, " +
            Wallet.ARCHIVED + " INTEGER NOT NULL DEFAULT 0, " +
            Wallet.INDEX + " INTEGER NOT NULL DEFAULT 0, " +
            Wallet.TAG + " TEXT, " +
            Wallet.UUID + " TEXT NOT NULL UNIQUE, " +
            Wallet.LAST_EDIT + " INTEGER NOT NULL, " +
            Wallet.DELETED + " INTEGER NOT NULL DEFAULT 0" +
            ")";

    /*package-local*/ static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + Category.TABLE + " (" +
            Category.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Category.NAME + " TEXT NOT NULL, " +
            Category.ICON + " TEXT NOT NULL, " +
            Category.TYPE + " INTEGER NOT NULL, " +
            Category.PARENT + " INTEGER, " +
            Category.SHOW_REPORT + " INTEGER NOT NULL DEFAULT 1, " +
            Category.INDEX + " INTEGER NOT NULL DEFAULT 0, " +
            Category.TAG + " TEXT, " +
            Category.UUID + " TEXT NOT NULL UNIQUE, " +
            Category.LAST_EDIT + " INTEGER NOT NULL, " +
            Category.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Category.PARENT + ") REFERENCES " + Category.TABLE +
            "(" + Category.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_EVENT = "CREATE TABLE " + Event.TABLE + " (" +
            Event.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Event.NAME + " TEXT NOT NULL, " +
            Event.ICON + " TEXT NOT NULL, " +
            Event.NOTE + " TEXT, " +
            Event.START_DATE + " DATETIME NOT NULL, " +
            Event.END_DATE + " DATETIME NOT NULL, " +
            Event.TAG + " TEXT, " +
            Event.UUID + " TEXT NOT NULL UNIQUE, " +
            Event.LAST_EDIT + " INTEGER NOT NULL, " +
            Event.DELETED + " INTEGER NOT NULL DEFAULT 0 " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_PLACE = "CREATE TABLE " + Place.TABLE + " (" +
            Place.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Place.NAME + " TEXT NOT NULL, " +
            Place.ICON + " TEXT NOT NULL, " +
            Place.ADDRESS + " TEXT, " +
            Place.LATITUDE + " REAL, " +
            Place.LONGITUDE + " REAL, " +
            Place.TAG + " TEXT, " +
            Place.UUID + " TEXT NOT NULL UNIQUE, " +
            Place.LAST_EDIT + " INTEGER NOT NULL, " +
            Place.DELETED + " INTEGER NOT NULL DEFAULT 0 " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_PERSON = "CREATE TABLE " + Person.TABLE + " (" +
            Person.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Person.NAME + " TEXT NOT NULL, " +
            Person.ICON + " TEXT NOT NULL, " +
            Person.NOTE + " TEXT, " +
            Person.TAG + " TEXT, " +
            Person.UUID + " TEXT NOT NULL UNIQUE, " +
            Person.LAST_EDIT + " INTEGER NOT NULL, " +
            Person.DELETED + " INTEGER NOT NULL DEFAULT 0 " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_EVENT_PEOPLE = "CREATE TABLE " + EventPeople.TABLE + " (" +
            EventPeople.EVENT + " INTEGER NOT NULL, " +
            EventPeople.PERSON + " INTEGER NOT NULL, " +
            EventPeople.UUID + " TEXT NOT NULL UNIQUE, " +
            EventPeople.LAST_EDIT + " INTEGER NOT NULL, " +
            EventPeople.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + EventPeople.EVENT + ", " + EventPeople.PERSON + ")," +
            "FOREIGN KEY (" + EventPeople.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + EventPeople.PERSON + ") REFERENCES " + Person.TABLE +
            "(" + Person.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_DEBT = "CREATE TABLE " + Debt.TABLE + " (" +
            Debt.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Debt.TYPE + " INTEGER NOT NULL, " +
            Debt.ICON + " TEXT NOT NULL, " +
            Debt.DESCRIPTION + " TEXT NOT NULL, " +
            Debt.DATE + " DATETIME NOT NULL, " +
            Debt.EXPIRATION_DATE + " DATETIME, " +
            Debt.WALLET + " INTEGER NOT NULL, " +
            Debt.NOTE + " TEXT, " +
            Debt.PLACE + " INTEGER, " +
            Debt.MONEY + " INTEGER NOT NULL, " +
            Debt.ARCHIVED + " INTEGER NOT NULL DEFAULT 0, " +
            Debt.TAG + " TEXT, " +
            Debt.UUID + " TEXT NOT NULL UNIQUE, " +
            Debt.LAST_EDIT + " INTEGER NOT NULL, " +
            Debt.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Debt.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + Debt.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_DEBT_PEOPLE = "CREATE TABLE " + DebtPeople.TABLE + " (" +
            DebtPeople.DEBT + " INTEGER NOT NULL, " +
            DebtPeople.PERSON + " INTEGER NOT NULL, " +
            DebtPeople.UUID + " TEXT NOT NULL UNIQUE, " +
            DebtPeople.LAST_EDIT + " INTEGER NOT NULL, " +
            DebtPeople.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + DebtPeople.DEBT + ", " + DebtPeople.PERSON + ")," +
            "FOREIGN KEY (" + DebtPeople.DEBT + ") REFERENCES " + Debt.TABLE +
            "(" + Debt.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + DebtPeople.PERSON + ") REFERENCES " + Person.TABLE +
            "(" + Person.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_BUDGET = "CREATE TABLE " + Budget.TABLE + " (" +
            Budget.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Budget.TYPE + " INTEGER NOT NULL, " +
            Budget.CATEGORY + " INTEGER, " +
            Budget.START_DATE + " DATETIME NOT NULL, " +
            Budget.END_DATE + " DATETIME NOT NULL, " +
            Budget.MONEY + " INTEGER NOT NULL, " +
            Budget.CURRENCY + " TEXT NOT NULL, " +
            Budget.TAG + " TEXT, " +
            Budget.UUID + " TEXT NOT NULL UNIQUE, " +
            Budget.LAST_EDIT + " INTEGER NOT NULL, " +
            Budget.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Budget.CATEGORY + ") REFERENCES " + Category.TABLE +
            "(" + Category.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_BUDGET_WALLET = "CREATE TABLE " + BudgetWallet.TABLE + " (" +
            BudgetWallet.BUDGET + " INTEGER NOT NULL, " +
            BudgetWallet.WALLET + " INTEGER NOT NULL, " +
            BudgetWallet.UUID + " TEXT NOT NULL UNIQUE, " +
            BudgetWallet.LAST_EDIT + " INTEGER NOT NULL, " +
            BudgetWallet.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + BudgetWallet.BUDGET + ", " + BudgetWallet.WALLET + ")," +
            "FOREIGN KEY (" + BudgetWallet.BUDGET + ") REFERENCES " + Budget.TABLE +
            "(" + Budget.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + BudgetWallet.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_SAVING = "CREATE TABLE " + Saving.TABLE + " (" +
            Saving.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Saving.DESCRIPTION + " TEXT, " +
            Saving.ICON + " TEXT NOT NULL, " +
            Saving.START_MONEY + " INTEGER NOT NULL, " +
            Saving.END_MONEY + " INTEGER NOT NULL, " +
            Saving.WALLET + " INTEGER NOT NULL, " +
            Saving.END_DATE + " DATETIME, " +
            Saving.COMPLETE + " INTEGER NOT NULL DEFAULT 0, " +
            Saving.NOTE + " TEXT, " +
            Saving.TAG + " TEXT, " +
            Saving.UUID + " TEXT NOT NULL UNIQUE, " +
            Saving.LAST_EDIT + " INTEGER NOT NULL, " +
            Saving.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Saving.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + Transaction.TABLE + " (" +
            Transaction.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Transaction.MONEY + " INTEGER NOT NULL, " +
            Transaction.DATE + " DATETIME NOT NULL, " +
            Transaction.DESCRIPTION + " TEXT, " +
            Transaction.CATEGORY + " INTEGER NOT NULL, " +
            Transaction.DIRECTION + " INTEGER NOT NULL, " +
            Transaction.TYPE + " INTEGER NOT NULL, " +
            Transaction.WALLET + " INTEGER NOT NULL, " +
            Transaction.PLACE + " INTEGER, " +
            Transaction.NOTE + " TEXT, " +
            Transaction.SAVING + " INTEGER, " +
            Transaction.DEBT + " INTEGER, " +
            Transaction.EVENT + " INTEGER, " +
            Transaction.RECURRENCE + " INTEGER, " +
            Transaction.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            Transaction.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            Transaction.TAG + " TEXT, " +
            Transaction.UUID + " TEXT NOT NULL UNIQUE, " +
            Transaction.LAST_EDIT + " INTEGER NOT NULL, " +
            Transaction.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Transaction.CATEGORY + ") REFERENCES " + Category.TABLE +
            "(" + Category.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + Transaction.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + Transaction.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transaction.SAVING + ") REFERENCES " + Saving.TABLE +
            "(" + Saving.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transaction.DEBT + ") REFERENCES " + Debt.TABLE +
            "(" + Debt.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transaction.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transaction.RECURRENCE + ") REFERENCES " + RecurrentTransaction.TABLE +
            "(" + RecurrentTransaction.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSACTION_PEOPLE = "CREATE TABLE " + TransactionPeople.TABLE + " (" +
            TransactionPeople.TRANSACTION + " INTEGER NOT NULL, " +
            TransactionPeople.PERSON + " INTEGER NOT NULL, " +
            TransactionPeople.UUID + " TEXT NOT NULL UNIQUE, " +
            TransactionPeople.LAST_EDIT + " INTEGER NOT NULL, " +
            TransactionPeople.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + TransactionPeople.TRANSACTION + ", " + TransactionPeople.PERSON + ")," +
            "FOREIGN KEY (" + TransactionPeople.TRANSACTION + ") REFERENCES " + Transaction.TABLE +
            "(" + Transaction.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransactionPeople.PERSON + ") REFERENCES " + Person.TABLE +
            "(" + Person.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSFER = "CREATE TABLE " + Transfer.TABLE + " (" +
            Transfer.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Transfer.DESCRIPTION + " TEXT, " +
            Transfer.DATE + " DATETIME NOT NULL, " +
            Transfer.TRANSACTION_FROM + " INTEGER NOT NULL, " +
            Transfer.TRANSACTION_TO + " INTEGER NOT NULL, " +
            Transfer.TRANSACTION_TAX + " INTEGER, " +
            Transfer.NOTE + " TEXT, " +
            Transfer.PLACE + " INTEGER, " +
            Transfer.EVENT + " INTEGER, " +
            Transfer.RECURRENCE + " INTEGER, " +
            Transfer.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            Transfer.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            Transfer.TAG + " TEXT, " +
            Transfer.UUID + " TEXT NOT NULL UNIQUE, " +
            Transfer.LAST_EDIT + " INTEGER NOT NULL, " +
            Transfer.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + Transfer.TRANSACTION_FROM + ") REFERENCES " + Transaction.TABLE +
            "(" + Transaction.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + Transfer.TRANSACTION_TO + ") REFERENCES " + Transaction.TABLE +
            "(" + Transaction.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + Transfer.TRANSACTION_TAX + ") REFERENCES " + Transaction.TABLE +
            "(" + Transaction.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transfer.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transfer.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + Transfer.RECURRENCE + ") REFERENCES " + RecurrentTransfer.TABLE +
            "(" + RecurrentTransfer.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSFER_PEOPLE = "CREATE TABLE " + TransferPeople.TABLE + " (" +
            TransferPeople.TRANSFER + " INTEGER NOT NULL, " +
            TransferPeople.PERSON + " INTEGER NOT NULL, " +
            TransferPeople.UUID + " TEXT NOT NULL UNIQUE, " +
            TransferPeople.LAST_EDIT + " INTEGER NOT NULL, " +
            TransferPeople.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + TransferPeople.TRANSFER + ", " + TransferPeople.PERSON + ")," +
            "FOREIGN KEY (" + TransferPeople.TRANSFER + ") REFERENCES " + Transfer.TABLE +
            "(" + Transfer.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransferPeople.PERSON + ") REFERENCES " + Person.TABLE +
            "(" + Person.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSACTION_MODEL = "CREATE TABLE " + TransactionModel.TABLE + " (" +
            TransactionModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TransactionModel.MONEY + " INTEGER NOT NULL, " +
            TransactionModel.DESCRIPTION + " TEXT, " +
            TransactionModel.CATEGORY + " INTEGER NOT NULL, " +
            TransactionModel.DIRECTION + " INTEGER NOT NULL, " +
            TransactionModel.WALLET + " INTEGER NOT NULL, " +
            TransactionModel.PLACE + " INTEGER, " +
            TransactionModel.NOTE + " TEXT, " +
            TransactionModel.EVENT + " INTEGER, " +
            TransactionModel.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            TransactionModel.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            TransactionModel.TAG + " TEXT, " +
            TransactionModel.UUID + " TEXT NOT NULL UNIQUE, " +
            TransactionModel.LAST_EDIT + " INTEGER NOT NULL, " +
            TransactionModel.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + TransactionModel.CATEGORY + ") REFERENCES " + Category.TABLE +
            "(" + Category.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransactionModel.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransactionModel.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + TransactionModel.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSFER_MODEL = "CREATE TABLE " + TransferModel.TABLE + " (" +
            TransferModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TransferModel.DESCRIPTION + " TEXT, " +
            TransferModel.WALLET_FROM + " INTEGER NOT NULL, " +
            TransferModel.WALLET_TO + " INTEGER NOT NULL, " +
            TransferModel.MONEY_FROM + " INTEGER NOT NULL, " +
            TransferModel.MONEY_TO + " INTEGER NOT NULL, " +
            TransferModel.MONEY_TAX + " INTEGER, " +
            TransferModel.NOTE + " TEXT, " +
            TransferModel.EVENT + " INTEGER, " +
            TransferModel.PLACE + " INTEGER, " +
            TransferModel.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            TransferModel.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            TransferModel.TAG + " TEXT, " +
            TransferModel.UUID + " TEXT NOT NULL UNIQUE, " +
            TransferModel.LAST_EDIT + " INTEGER NOT NULL, " +
            TransferModel.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + TransferModel.WALLET_FROM + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransferModel.WALLET_TO + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransferModel.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + TransferModel.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_RECURRENT_TRANSACTION = "CREATE TABLE " + RecurrentTransaction.TABLE + " (" +
            RecurrentTransaction.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RecurrentTransaction.MONEY + " INTEGER NOT NULL, " +
            RecurrentTransaction.DESCRIPTION + " TEXT, " +
            RecurrentTransaction.CATEGORY + " INTEGER NOT NULL, " +
            RecurrentTransaction.DIRECTION + " INTEGER NOT NULL, " +
            RecurrentTransaction.WALLET + " INTEGER NOT NULL, " +
            RecurrentTransaction.PLACE + " INTEGER, " +
            RecurrentTransaction.NOTE + " TEXT, " +
            RecurrentTransaction.EVENT + " INTEGER, " +
            RecurrentTransaction.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            RecurrentTransaction.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            RecurrentTransaction.START_DATE + " DATETIME NOT NULL, " +
            RecurrentTransaction.LAST_OCCURRENCE + " DATETIME NOT NULL, " +
            RecurrentTransaction.NEXT_OCCURRENCE + " DATETIME, " +
            RecurrentTransaction.RULE + " TEXT NOT NULL, " +
            RecurrentTransaction.TAG + " TEXT, " +
            RecurrentTransaction.UUID + " TEXT NOT NULL UNIQUE, " +
            RecurrentTransaction.LAST_EDIT + " INTEGER NOT NULL, " +
            RecurrentTransaction.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + RecurrentTransaction.CATEGORY + ") REFERENCES " + Category.TABLE +
            "(" + Category.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + RecurrentTransaction.WALLET + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + RecurrentTransaction.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + RecurrentTransaction.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_RECURRENT_TRANSFER = "CREATE TABLE " + RecurrentTransfer.TABLE + " (" +
            RecurrentTransfer.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RecurrentTransfer.DESCRIPTION + " TEXT, " +
            RecurrentTransfer.WALLET_FROM + " INTEGER NOT NULL, " +
            RecurrentTransfer.WALLET_TO + " INTEGER NOT NULL, " +
            RecurrentTransfer.MONEY_FROM + " INTEGER NOT NULL, " +
            RecurrentTransfer.MONEY_TO + " INTEGER NOT NULL, " +
            RecurrentTransfer.MONEY_TAX + " INTEGER, " +
            RecurrentTransfer.NOTE + " TEXT, " +
            RecurrentTransfer.EVENT + " INTEGER, " +
            RecurrentTransfer.PLACE + " INTEGER, " +
            RecurrentTransfer.CONFIRMED + " INTEGER NOT NULL DEFAULT 1, " +
            RecurrentTransfer.COUNT_IN_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
            RecurrentTransfer.START_DATE + " DATETIME NOT NULL, " +
            RecurrentTransfer.LAST_OCCURRENCE + " DATETIME NOT NULL, " +
            RecurrentTransfer.NEXT_OCCURRENCE + " DATETIME, " +
            RecurrentTransfer.RULE + " TEXT NOT NULL, " +
            RecurrentTransfer.TAG + " TEXT, " +
            RecurrentTransfer.UUID + " TEXT NOT NULL UNIQUE, " +
            RecurrentTransfer.LAST_EDIT + " INTEGER NOT NULL, " +
            RecurrentTransfer.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (" + RecurrentTransfer.WALLET_FROM + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + RecurrentTransfer.WALLET_TO + ") REFERENCES " + Wallet.TABLE +
            "(" + Wallet.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + RecurrentTransfer.EVENT + ") REFERENCES " + Event.TABLE +
            "(" + Event.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL, " +
            "FOREIGN KEY (" + RecurrentTransfer.PLACE + ") REFERENCES " + Place.TABLE +
            "(" + Place.ID + ") ON UPDATE NO ACTION ON DELETE SET NULL " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_ATTACHMENT = "CREATE TABLE " + Attachment.TABLE + " (" +
            Attachment.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Attachment.FILE + " TEXT NOT NULL, " +
            Attachment.NAME + " TEXT NOT NULL, " +
            Attachment.TYPE + " TEXT, " +
            Attachment.SIZE + " INTEGER NOT NULL, " +
            Attachment.TAG + " TEXT, " +
            Attachment.UUID + " TEXT NOT NULL UNIQUE, " +
            Attachment.LAST_EDIT + " INTEGER NOT NULL, " +
            Attachment.DELETED + " INTEGER NOT NULL DEFAULT 0 " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSACTION_ATTACHMENT = "CREATE TABLE " + TransactionAttachment.TABLE + " (" +
            TransactionAttachment.TRANSACTION + " INTEGER NOT NULL, " +
            TransactionAttachment.ATTACHMENT + " INTEGER NOT NULL, " +
            TransactionAttachment.UUID + " TEXT NOT NULL UNIQUE, " +
            TransactionAttachment.LAST_EDIT + " INTEGER NOT NULL, " +
            TransactionAttachment.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + TransactionAttachment.TRANSACTION + ", " + TransactionAttachment.ATTACHMENT + ")," +
            "FOREIGN KEY (" + TransactionAttachment.TRANSACTION + ") REFERENCES " + Transaction.TABLE +
            "(" + Transaction.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransactionAttachment.ATTACHMENT + ") REFERENCES " + Attachment.TABLE +
            "(" + Attachment.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_TABLE_TRANSFER_ATTACHMENT = "CREATE TABLE " + TransferAttachment.TABLE + " (" +
            TransferAttachment.TRANSFER + " INTEGER NOT NULL, " +
            TransferAttachment.ATTACHMENT + " INTEGER NOT NULL, " +
            TransferAttachment.UUID + " TEXT NOT NULL UNIQUE, " +
            TransferAttachment.LAST_EDIT + " INTEGER NOT NULL, " +
            TransferAttachment.DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY (" + TransferAttachment.TRANSFER + ", " + TransferAttachment.ATTACHMENT + ")," +
            "FOREIGN KEY (" + TransferAttachment.TRANSFER + ") REFERENCES " + Transfer.TABLE +
            "(" + Transfer.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE, " +
            "FOREIGN KEY (" + TransactionAttachment.ATTACHMENT + ") REFERENCES " + Attachment.TABLE +
            "(" + Attachment.ID + ") ON UPDATE NO ACTION ON DELETE CASCADE " +
            ")";

    /*package-local*/ static final String CREATE_CATEGORY_INDEX_COLUMN = "ALTER TABLE " +
            Category.TABLE + " ADD COLUMN " + Category.INDEX + " INTEGER NOT NULL DEFAULT 0";

    /*package-local*/ static final String CREATE_WALLET_INDEX_COLUMN = "ALTER TABLE " +
            Wallet.TABLE + " ADD COLUMN " + Wallet.INDEX + " INTEGER NOT NULL DEFAULT 0";
}