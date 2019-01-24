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

/**
 * Created by andrea on 28/03/18.
 */
/*package-local*/ class JSONDatabase {

    /*package-local*/ static final int MIN_SUPPORTED_VERSION = 1;
    /*package-local*/ static final int MAX_SUPPORTED_VERSION = 2;

    /*package-local*/ static final int VERSION = 2;

    /*package-local*/ static class Header {
        /*package-local*/ static final String OBJECT = "header";
        /*package-local*/ static final String VERSION_CODE = "version_code";
    }

    /*package-local*/ static class Currency {
        /*package-local*/ static final String ARRAY = "currencies";
        /*package-local*/ static final String ISO = "iso";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String SYMBOL = "symbol";
        /*package-local*/ static final String DECIMALS = "decimals";
        /*package-local*/ static final String FAVOURITE = "favourite";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static class Wallet {
        /*package-local*/ static final String ARRAY = "wallets";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String CURRENCY = "currency";
        /*package-local*/ static final String START_MONEY = "start_money";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String ARCHIVED = "archived";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String INDEX = "index";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Category {
        /*package-local*/ static final String ARRAY = "categories";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String TYPE = "type";
        /*package-local*/ static final String PARENT = "parent";
        /*package-local*/ static final String SHOW_REPORT = "show_report";
        /*package-local*/ static final String INDEX = "index";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Event {
        /*package-local*/ static final String ARRAY = "events";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String START_DATE = "start_date";
        /*package-local*/ static final String END_DATE = "end_date";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Place {
        /*package-local*/ static final String ARRAY = "places";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String ADDRESS = "address";
        /*package-local*/ static final String LATITUDE = "latitude";
        /*package-local*/ static final String LONGITUDE = "longitude";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Person {
        /*package-local*/ static final String ARRAY = "people";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class EventPeople {
        /*package-local*/ static final String ARRAY = "event_people";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String PERSON = "person";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Debt {
        /*package-local*/ static final String ARRAY = "debts";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TYPE = "type";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String DATE = "date";
        /*package-local*/ static final String EXPIRATION_DATE = "expiration_date";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String MONEY = "money";
        /*package-local*/ static final String ARCHIVED = "archived";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class DebtPeople {
        /*package-local*/ static final String ARRAY = "debt_people";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String DEBT = "debt";
        /*package-local*/ static final String PERSON = "person";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Budget {
        /*package-local*/ static final String ARRAY = "budgets";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TYPE = "type";
        /*package-local*/ static final String CATEGORY = "category";
        /*package-local*/ static final String START_DATE = "start_date";
        /*package-local*/ static final String END_DATE = "end_date";
        /*package-local*/ static final String MONEY = "money";
        /*package-local*/ static final String CURRENCY = "currency";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class BudgetWallet {
        /*package-local*/ static final String ARRAY = "budget_wallets";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String BUDGET = "budget";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Saving {
        /*package-local*/ static final String ARRAY = "savings";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String ICON = "icon";
        /*package-local*/ static final String START_MONEY = "start_money";
        /*package-local*/ static final String END_MONEY = "end_money";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String END_DATE = "end_date";
        /*package-local*/ static final String COMPLETE = "complete";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class RecurrentTransaction {
        /*package-local*/ static final String ARRAY = "recurrent_transactions";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String MONEY = "money";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String CATEGORY = "category";
        /*package-local*/ static final String DIRECTION = "direction";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String START_DATE = "start_date";
        /*package-local*/ static final String LAST_OCCURRENCE = "last_occurrence";
        /*package-local*/ static final String NEXT_OCCURRENCE = "next_occurrence";
        /*package-local*/ static final String RULE = "rule";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class RecurrentTransfer {
        /*package-local*/ static final String ARRAY = "recurrent_transfers";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String WALLET_FROM = "from_wallet";
        /*package-local*/ static final String WALLET_TO = "to_wallet";
        /*package-local*/ static final String MONEY_FROM = "from_money";
        /*package-local*/ static final String MONEY_TO = "to_money";
        /*package-local*/ static final String MONEY_TAX = "tax_money";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String START_DATE = "start_date";
        /*package-local*/ static final String LAST_OCCURRENCE = "last_occurrence";
        /*package-local*/ static final String NEXT_OCCURRENCE = "next_occurrence";
        /*package-local*/ static final String RULE = "rule";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Transaction {
        /*package-local*/ static final String ARRAY = "transactions";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String MONEY = "money";
        /*package-local*/ static final String DATE = "date";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String CATEGORY = "category";
        /*package-local*/ static final String DIRECTION = "direction";
        /*package-local*/ static final String TYPE = "type";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String SAVING = "saving";
        /*package-local*/ static final String DEBT = "debt";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String RECURRENCE = "recurrence";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransactionPeople {
        /*package-local*/ static final String ARRAY = "transaction_people";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TRANSACTION = "transaction";
        /*package-local*/ static final String PERSON = "person";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Transfer {
        /*package-local*/ static final String ARRAY = "transfers";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String DATE = "date";
        /*package-local*/ static final String FROM = "from";
        /*package-local*/ static final String TO = "to";
        /*package-local*/ static final String TAX = "tax";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String RECURRENCE = "recurrence";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransferPeople {
        /*package-local*/ static final String ARRAY = "transfer_people";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TRANSFER = "transfer";
        /*package-local*/ static final String PERSON = "person";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransactionModel {
        /*package-local*/ static final String ARRAY = "transaction_models";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String MONEY = "money";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String CATEGORY = "category";
        /*package-local*/ static final String DIRECTION = "direction";
        /*package-local*/ static final String WALLET = "wallet";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransferModel {
        /*package-local*/ static final String ARRAY = "transfer_models";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String DESCRIPTION = "description";
        /*package-local*/ static final String WALLET_FROM = "from_wallet";
        /*package-local*/ static final String WALLET_TO = "to_wallet";
        /*package-local*/ static final String MONEY_FROM = "from_money";
        /*package-local*/ static final String MONEY_TO = "to_money";
        /*package-local*/ static final String MONEY_TAX = "tax_money";
        /*package-local*/ static final String NOTE = "note";
        /*package-local*/ static final String EVENT = "event";
        /*package-local*/ static final String PLACE = "place";
        /*package-local*/ static final String CONFIRMED = "confirmed";
        /*package-local*/ static final String COUNT_IN_TOTAL = "count_in_total";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class Attachment {
        /*package-local*/ static final String ARRAY = "attachments";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String FILE = "file";
        /*package-local*/ static final String NAME = "name";
        /*package-local*/ static final String TYPE = "type";
        /*package-local*/ static final String SIZE = "size";
        /*package-local*/ static final String TAG = "tag";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransactionAttachment {
        /*package-local*/ static final String ARRAY = "transaction_attachment";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TRANSACTION = "transaction";
        /*package-local*/ static final String ATTACHMENT = "attachment";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }

    /*package-local*/ static final class TransferAttachment {
        /*package-local*/ static final String ARRAY = "transfer_attachment";
        /*package-local*/ static final String ID = "id";
        /*package-local*/ static final String TRANSFER = "transfer";
        /*package-local*/ static final String ATTACHMENT = "attachment";
        /*package-local*/ static final String LAST_EDIT = "last_edit";
        /*package-local*/ static final String DELETED = "deleted";
    }
}