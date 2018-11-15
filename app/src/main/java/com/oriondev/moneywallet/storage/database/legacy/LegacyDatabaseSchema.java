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

package com.oriondev.moneywallet.storage.database.legacy;

/**
 * Created by andrea on 02/11/18.
 */
/*package-local*/ class LegacyDatabaseSchema {

    /*package-local*/ final static String DATABASE_NAME = "MoneyWallet";

    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_TRANSFER = -10;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_TRANSFER_TAX = -11;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_DEBT = -12;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_DEBT_PAID = -13;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_CREDIT = -14;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_CREDIT_PAID = -15;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_OPERATION_TAX = -16;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_SAVING_IN = -17;
    /*package-local*/ static final int INDEX_HIDDEN_CATEGORY_SAVING_OUT = -18;

    /*package-local*/ static final String HIDDEN_CATEGORY_TRANSFER_KEY = "system_transfer";
    /*package-local*/ static final String HIDDEN_CATEGORY_TRANSFER_TAX_KEY = "system_transfer_tax";
    /*package-local*/ static final String HIDDEN_CATEGORY_DEBT_KEY = "system_debt";
    /*package-local*/ static final String HIDDEN_CATEGORY_DEBT_PAID_KEY = "system_debt_paid";
    /*package-local*/ static final String HIDDEN_CATEGORY_CREDIT_KEY = "system_credit";
    /*package-local*/ static final String HIDDEN_CATEGORY_CREDIT_PAID_KEY = "system_credit_paid";
    /*package-local*/ static final String HIDDEN_CATEGORY_OPERATION_TAX_KEY = "system_operation_tax";
    /*package-local*/ static final String HIDDEN_CATEGORY_SAVING_IN_KEY = "system_saving_in";
    /*package-local*/ static final String HIDDEN_CATEGORY_SAVING_OUT_KEY = "system_saving_out";

    /*package-local*/ static final String TRANSFER_HIDDEN_CATEGORY_ICON = "icon_transfer";
    /*package-local*/ static final String TRANSFER_TAX_HIDDEN_CATEGORY_ICON = "icon_transfer_tax";
    /*package-local*/ static final String HIDDEN_CATEGORY_DEBT = "icon_debt";
    /*package-local*/ static final String HIDDEN_CATEGORY_DEBT_PAID = "icon_debt_paid";
    /*package-local*/ static final String HIDDEN_CATEGORY_CREDIT = "icon_credit";
    /*package-local*/ static final String HIDDEN_CATEGORY_CREDIT_PAID = "icon_credit_paid";
    /*package-local*/ static final String HIDDEN_CATEGORY_OPERATION_TAX = "icon_operation_tax";
    /*package-local*/ static final String HIDDEN_CATEGORY_SAVING_IN = "icon_saving_in";
    /*package-local*/ static final String HIDDEN_CATEGORY_SAVING_OUT = "icon_saving_out";

    /*package-local*/ static final int TYPE_DEBT = 0;
    /*package-local*/ static final int TYPE_CREDIT = 1;

    /*package-local*/ static final int TYPE_BUDGET_CATEGORY = 0;
    /*package-local*/ static final int TYPE_BUDGET_CASH_FLOW = 1;
    /*package-local*/ static final int BUDGET_INFLOW = 1;
    /*package-local*/ static final int BUDGET_OUTFLOW = 0;

    /*package-local*/ static class Wallet {
        /*package-local*/ static final String TABLE = "wallet_table";
        /*package-local*/ static final String ID = "wallet_id";
        /*package-local*/ static final String NAME = "wallet_name";
        /*package-local*/ static final String ICON = "wallet_icon";
        /*package-local*/ static final String CURRENCY_ISO = "wallet_currency_iso";
        /*package-local*/ static final String IN_TOTAL = "wallet_in_total";
        /*package-local*/ static final String INITIAL = "wallet_initial";
        /*package-local*/ static final String UUID = "wallet_UUID";
        /*package-local*/ static final String DELETED = "wallet_deleted";
        /*package-local*/ static final String LAST_EDIT = "wallet_last_edit";
    }

    /*package-local*/ static class Category {
        /*package-local*/ static final String TABLE = "category_table";
        /*package-local*/ static final String ID = "category_id";
        /*package-local*/ static final String NAME = "category_name";
        /*package-local*/ static final String ICON = "category_icon";
        /*package-local*/ static final String IS_IN = "category_is_in";
        /*package-local*/ static final String IS_HIDDEN = "category_is_hidden";
        /*package-local*/ static final String PARENT = "category_sub_id";
        /*package-local*/ static final String REPORT = "category_report";
        /*package-local*/ static final String INDEX = "category_index";
        /*package-local*/ static final String UUID = "category_UUID";
        /*package-local*/ static final String DELETED = "category_deleted";
        /*package-local*/ static final String LAST_EDIT = "category_last_edit";
    }

    /*package-local*/ static class Transaction {
        /*package-local*/ static final String TABLE = "transaction_table";
        /*package-local*/ static final String ID = "transaction_id";
        /*package-local*/ static final String DESCRIPTION = "transaction_description";
        /*package-local*/ static final String DATE = "transaction_date";
        /*package-local*/ static final String PLACE = "transaction_place";
        /*package-local*/ static final String LATITUDE = "transaction_latitude";
        /*package-local*/ static final String LONGITUDE = "transaction_longitude";
        /*package-local*/ static final String NOTE = "transaction_note";
        /*package-local*/ static final String WALLET = "transaction_wallet_id";
        /*package-local*/ static final String CATEGORY = "transaction_category";
        /*package-local*/ static final String TRANSFER = "transaction_transfer_id";
        /*package-local*/ static final String IS_IN = "transaction_is_in";
        /*package-local*/ static final String IMPORT = "transaction_import";
        /*package-local*/ static final String RECURRENCE = "transaction_recurrent";
        /*package-local*/ static final String DEBT = "transaction_debt";
        /*package-local*/ static final String TAX_ON = "transaction_tax_on";
        /*package-local*/ static final String SAVING = "transaction_saving";
        /*package-local*/ static final String EVENT = "transaction_event";
        /*package-local*/ static final String UUID = "transaction_UUID";
        /*package-local*/ static final String DELETED = "transaction_deleted";
        /*package-local*/ static final String LAST_EDIT = "transaction_last_edit";
    }

    /*package-local*/ static class Budget {
        /*package-local*/ static final String TABLE = "budget_table";
        /*package-local*/ static final String ID = "budget_id";
        /*package-local*/ static final String TYPE = "budget_type";
        /*package-local*/ static final String CATEGORY_OR_FLOW = "budget_category_or_flow";
        /*package-local*/ static final String WALLETS = "budget_wallets";
        /*package-local*/ static final String DATE_FROM = "budget_date_from";
        /*package-local*/ static final String DATE_TO = "budget_date_to";
        /*package-local*/ static final String MAX = "budget_max";
        /*package-local*/ static final String NOTIFY = "budget_notify";
        /*package-local*/ static final String NOTIFY_PERCENTAGE = "budget_notify_percentage";
        /*package-local*/ static final String UUID = "budget_UUID";
        /*package-local*/ static final String DELETED = "budget_deleted";
        /*package-local*/ static final String LAST_EDIT = "budget_last_edit";
    }

    /*package-local*/ static class Recurrence {
        /*package-local*/ static final String TABLE = "recurring_table";
        /*package-local*/ static final String ID = "recurring_id";
        /*package-local*/ static final String DESCRIPTION = "recurring_description";
        /*package-local*/ static final String PLACE = "recurring_place";
        /*package-local*/ static final String LATITUDE = "recurring_latitude";
        /*package-local*/ static final String LONGITUDE = "recurring_longitude";
        /*package-local*/ static final String NOTE = "recurring_note";
        /*package-local*/ static final String WALLET = "recurring_wallet_id";
        /*package-local*/ static final String CATEGORY = "recurring_category";
        /*package-local*/ static final String IS_IN = "recurring_is_in";
        /*package-local*/ static final String IMPORT = "recurring_import";
        /*package-local*/ static final String ENCODED_INFO = "recurring_encoded_info";
        /*package-local*/ static final String UUID = "recurring_UUID";
        /*package-local*/ static final String DELETED = "recurring_deleted";
        /*package-local*/ static final String LAST_EDIT = "recurring_last_edit";
    }

    /*package-local*/ static class Debt {
        /*package-local*/ static final String TABLE = "debt_table";
        /*package-local*/ static final String ID = "debt_id";
        /*package-local*/ static final String TYPE = "debt_type";
        /*package-local*/ static final String DESCRIPTION = "debt_description";
        /*package-local*/ static final String DATE = "debt_date";
        /*package-local*/ static final String EXPIRATION_DATE = "debt_expiration_date";
        /*package-local*/ static final String PLACE = "debt_place";
        /*package-local*/ static final String NOTE = "debt_note";
        /*package-local*/ static final String WALLET = "debt_wallet_id";
        /*package-local*/ static final String IMPORT = "debt_import";
        /*package-local*/ static final String UUID = "debt_UUID";
        /*package-local*/ static final String DELETED = "debt_deleted";
        /*package-local*/ static final String LAST_EDIT = "debt_last_edit";
    }

    /*package-local*/ static class Saving {
        /*package-local*/ static final String TABLE = "saving_table";
        /*package-local*/ static final String ID = "saving_id";
        /*package-local*/ static final String ICON = "saving_icon";
        /*package-local*/ static final String DESCRIPTION = "saving_description";
        /*package-local*/ static final String INITIAL = "saving_initial_cash";
        /*package-local*/ static final String TARGET = "saving_target_cash";
        /*package-local*/ static final String WALLET = "saving_wallet";
        /*package-local*/ static final String END_DATE = "saving_end_date";
        /*package-local*/ static final String COMPLETE = "saving_complete";
        /*package-local*/ static final String UUID = "saving_UUID";
        /*package-local*/ static final String DELETED = "saving_deleted";
        /*package-local*/ static final String LAST_EDIT = "saving_last_edit";
    }

    /*package-local*/ static class Event {
        /*package-local*/ static final String TABLE = "event_table";
        /*package-local*/ static final String ID = "event_id";
        /*package-local*/ static final String NAME = "event_name";
        /*package-local*/ static final String ICON = "event_icon";
        /*package-local*/ static final String NOTE = "event_note";
        /*package-local*/ static final String DATE_FROM = "event_date_from";
        /*package-local*/ static final String DATE_TO = "event_date_to";
        /*package-local*/ static final String UUID = "event_UUID";
        /*package-local*/ static final String DELETED = "event_deleted";
        /*package-local*/ static final String LAST_EDIT = "event_last_edit";
    }
}