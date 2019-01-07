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

import android.text.TextUtils;

import com.oriondev.moneywallet.model.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class contains all the static strings that are part of the results returned by the internal
 * content provider. It will virtually modify the structure of the database and return the items
 * in a different format. The application must use the virtual tables defined in this class when a
 * component interact with the {@link DataContentProvider}.
 */
public class Contract {

    public static final class Currency {
        public static final String NAME = Schema.Currency.NAME;
        public static final String ISO = Schema.Currency.ISO;
        public static final String SYMBOL = Schema.Currency.SYMBOL;
        public static final String DECIMALS = Schema.Currency.DECIMALS;
        public static final String FAVOURITE = Schema.Currency.FAVOURITE;
        public static final String FIX_MONEY_DECIMALS = "action_fix_money_decimals";
    }

    public static final class Wallet {
        public static final String ID = Schema.Wallet.ID;
        public static final String NAME = Schema.Wallet.NAME;
        public static final String ICON = Schema.Wallet.ICON;
        public static final String CURRENCY = Schema.Wallet.CURRENCY;
        public static final String NOTE = Schema.Wallet.NOTE;
        public static final String COUNT_IN_TOTAL = Schema.Wallet.COUNT_IN_TOTAL;
        public static final String START_MONEY = Schema.Wallet.START_MONEY;
        public static final String TOTAL_MONEY = "wallet_" + Schema.Alias.TOTAL_MONEY;
        public static final String ARCHIVED = Schema.Wallet.ARCHIVED;
        public static final String TAG = Schema.Wallet.TAG;
        public static final String INDEX = Schema.Wallet.INDEX;
    }

    public static final class Transaction {
        public static final String ID = Schema.Transaction.ID;
        public static final String MONEY = Schema.Transaction.MONEY;
        public static final String DATE = Schema.Transaction.DATE;
        public static final String DESCRIPTION = Schema.Transaction.DESCRIPTION;
        public static final String CATEGORY_ID = Schema.Transaction.CATEGORY;
        public static final String CATEGORY_NAME = "transaction_" + Schema.Category.NAME;
        public static final String CATEGORY_ICON = "transaction_" + Schema.Category.ICON;
        public static final String CATEGORY_PARENT_ID = "transaction_" + Schema.Category.PARENT;
        public static final String CATEGORY_TYPE = "transaction_" + Schema.Category.TYPE;
        public static final String CATEGORY_TAG = "transaction_" + Schema.Category.TAG;
        public static final String CATEGORY_SHOW_REPORT = "transaction_" + Schema.Category.SHOW_REPORT;
        public static final String DIRECTION = Schema.Transaction.DIRECTION;
        public static final String TYPE = Schema.Transaction.TYPE;
        public static final String WALLET_ID = Schema.Transaction.WALLET;
        public static final String WALLET_NAME = "transaction_" + Schema.Wallet.NAME;
        public static final String WALLET_ICON = "transaction_" + Schema.Wallet.ICON;
        public static final String WALLET_CURRENCY = "transaction_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_COUNT_IN_TOTAL = "transaction_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_ARCHIVED = "transaction_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TAG = "transaction_" + Schema.Wallet.TAG;
        public static final String PLACE_ID = Schema.Transaction.PLACE;
        public static final String PLACE_NAME = "transaction_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "transaction_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "transaction_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "transaction_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "transaction_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "transaction_" + Schema.Place.TAG;
        public static final String NOTE = Schema.Transaction.NOTE;
        public static final String EVENT_ID = Schema.Transaction.EVENT;
        public static final String EVENT_NAME = "transaction_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "transaction_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "transaction_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "transaction_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "transaction_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "transaction_" + Schema.Event.TAG;
        public static final String SAVING_ID = Schema.Transaction.SAVING;
        public static final String DEBT_ID = Schema.Transaction.DEBT;
        public static final String RECURRENCE_ID = Schema.Transaction.RECURRENCE;
        public static final String CONFIRMED = Schema.Transaction.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.Transaction.COUNT_IN_TOTAL;
        public static final String PEOPLE_IDS = "transaction_person_ids";
        public static final String ATTACHMENT_IDS = "transaction_attachment_ids";
        public static final String TAG = Schema.Transaction.TAG;
    }

    public static final class Transfer {
        public static final String ID = Schema.Transfer.ID;
        public static final String DESCRIPTION = Schema.Transfer.DESCRIPTION;
        public static final String DATE = Schema.Transfer.DATE;
        public static final String TRANSACTION_FROM_ID = Schema.Transfer.TRANSACTION_FROM;
        public static final String TRANSACTION_FROM_WALLET_ID = Schema.Transfer.TRANSACTION_FROM + "_wallet_id";
        public static final String TRANSACTION_FROM_WALLET_NAME = Schema.Transfer.TRANSACTION_FROM + "_wallet_name";
        public static final String TRANSACTION_FROM_WALLET_ICON = Schema.Transfer.TRANSACTION_FROM + "_wallet_icon";
        public static final String TRANSACTION_FROM_WALLET_CURRENCY = Schema.Transfer.TRANSACTION_FROM + "_wallet_currency";
        public static final String TRANSACTION_FROM_WALLET_COUNT_IN_TOTAL = Schema.Transfer.TRANSACTION_FROM + "_wallet_count_in_total";
        public static final String TRANSACTION_FROM_WALLET_ARCHIVED = Schema.Transfer.TRANSACTION_FROM + "_wallet_archived";
        public static final String TRANSACTION_FROM_WALLET_TAG = Schema.Transfer.TRANSACTION_FROM + "_wallet_tag";
        public static final String TRANSACTION_FROM_MONEY = Schema.Transfer.TRANSACTION_FROM + "_money";
        public static final String TRANSACTION_FROM_TAG = Schema.Transfer.TRANSACTION_FROM + "_tax";
        public static final String TRANSACTION_TO_ID = Schema.Transfer.TRANSACTION_TO;
        public static final String TRANSACTION_TO_WALLET_ID = Schema.Transfer.TRANSACTION_TO + "_wallet_id";
        public static final String TRANSACTION_TO_WALLET_NAME = Schema.Transfer.TRANSACTION_TO + "_wallet_name";
        public static final String TRANSACTION_TO_WALLET_ICON = Schema.Transfer.TRANSACTION_TO + "_wallet_icon";
        public static final String TRANSACTION_TO_WALLET_CURRENCY = Schema.Transfer.TRANSACTION_TO + "_wallet_currency";
        public static final String TRANSACTION_TO_WALLET_COUNT_IN_TOTAL = Schema.Transfer.TRANSACTION_TO + "_wallet_count_in_total";
        public static final String TRANSACTION_TO_WALLET_ARCHIVED = Schema.Transfer.TRANSACTION_TO + "_wallet_archived";
        public static final String TRANSACTION_TO_WALLET_TAG = Schema.Transfer.TRANSACTION_TO + "_wallet_tag";
        public static final String TRANSACTION_TO_MONEY = Schema.Transfer.TRANSACTION_TO + "_money";
        public static final String TRANSACTION_TO_TAG = Schema.Transfer.TRANSACTION_TO + "_tax";
        public static final String TRANSACTION_TAX_ID = Schema.Transfer.TRANSACTION_TAX;
        public static final String TRANSACTION_TAX_WALLET_ID = Schema.Transfer.TRANSACTION_TAX + "_wallet_id";
        public static final String TRANSACTION_TAX_WALLET_NAME = Schema.Transfer.TRANSACTION_TAX + "_wallet_name";
        public static final String TRANSACTION_TAX_WALLET_CURRENCY = Schema.Transfer.TRANSACTION_TAX + "_wallet_currency";
        public static final String TRANSACTION_TAX_WALLET_COUNT_IN_TOTAL = Schema.Transfer.TRANSACTION_TAX + "_wallet_count_in_total";
        public static final String TRANSACTION_TAX_WALLET_ARCHIVED = Schema.Transfer.TRANSACTION_TAX + "_wallet_archived";
        public static final String TRANSACTION_TAX_WALLET_TAG = Schema.Transfer.TRANSACTION_TAX + "_wallet_tag";
        public static final String TRANSACTION_TAX_MONEY = Schema.Transfer.TRANSACTION_TAX + "_money";
        public static final String TRANSACTION_TAX_TAG = Schema.Transfer.TRANSACTION_TAX + "_tax";
        public static final String NOTE = Schema.Transfer.NOTE;
        public static final String PLACE_ID = Schema.Transfer.PLACE;
        public static final String PLACE_NAME = "transfer_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "transfer_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "transfer_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "transfer_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "transfer_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "transfer_" + Schema.Place.TAG;
        public static final String EVENT_ID = Schema.Transfer.EVENT;
        public static final String EVENT_NAME = "transfer_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "transfer_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "transfer_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "transfer_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "transfer_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "transfer_" + Schema.Event.TAG;
        public static final String RECURRENCE_ID = Schema.Transfer.RECURRENCE;
        public static final String CONFIRMED = Schema.Transfer.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.Transfer.COUNT_IN_TOTAL;
        public static final String PEOPLE_IDS = "transfer_person_ids";
        public static final String ATTACHMENT_IDS = "transfer_attachment_ids";
        public static final String TAG = Schema.Transfer.TAG;
    }

    public static final class Category {
        public static final String ID = Schema.Category.ID;
        public static final String NAME = Schema.Category.NAME;
        public static final String ICON = Schema.Category.ICON;
        public static final String TYPE = Schema.Category.TYPE;
        public static final String PARENT = Schema.Category.PARENT;
        public static final String PARENT_NAME = "category_parent_name";
        public static final String PARENT_ICON = "category_parent_icon";
        public static final String PARENT_TYPE = "category_parent_type";
        public static final String PARENT_SHOW_REPORT = "category_parent_show_report";
        public static final String PARENT_TAG = "category_parent_tag";
        public static final String SHOW_REPORT = Schema.Category.SHOW_REPORT;
        public static final String INDEX = Schema.Category.INDEX;
        public static final String GROUP_ID = Schema.Alias.CATEGORY_GROUP_ID;
        public static final String GROUP_NAME = Schema.Alias.CATEGORY_GROUP_NAME;
        public static final String GROUP_INDEX = Schema.Alias.CATEGORY_GROUP_INDEX;
        public static final String TAG = Schema.Category.TAG;
    }

    public static final class Debt {
        public static final String ID = Schema.Debt.ID;
        public static final String TYPE = Schema.Debt.TYPE;
        public static final String ICON = Schema.Debt.ICON;
        public static final String DESCRIPTION = Schema.Debt.DESCRIPTION;
        public static final String DATE = Schema.Debt.DATE;
        public static final String EXPIRATION_DATE = Schema.Debt.EXPIRATION_DATE;
        public static final String WALLET_ID = Schema.Debt.WALLET;
        public static final String WALLET_NAME = "debt_" + Schema.Wallet.NAME;
        public static final String WALLET_ICON = "debt_" + Schema.Wallet.ICON;
        public static final String WALLET_CURRENCY = "debt_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_COUNT_IN_TOTAL = "debt_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_ARCHIVED = "debt_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TAG = "debt_" + Schema.Wallet.TAG;
        public static final String NOTE = Schema.Debt.NOTE;
        public static final String PLACE_ID = Schema.Debt.PLACE;
        public static final String PLACE_NAME = "debt_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "debt_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "debt_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "debt_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "debt_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "debt_" + Schema.Place.TAG;
        public static final String MONEY = Schema.Debt.MONEY;
        public static final String ARCHIVED = Schema.Debt.ARCHIVED;
        public static final String PROGRESS = "debt_" + Schema.Alias.PROGRESS;
        public static final String PEOPLE_IDS = "debt_person_ids";
        public static final String TAG = Schema.Debt.TAG;
        public static final String INSERT_MASTER_TRANSACTION = "action_insert_master_transaction";
    }

    public static final class Budget {
        public static final String ID = Schema.Budget.ID;
        public static final String TYPE = Schema.Budget.TYPE;
        public static final String CATEGORY_ID = Schema.Budget.CATEGORY;
        public static final String CATEGORY_NAME = "budget_" + Schema.Category.NAME;
        public static final String CATEGORY_ICON = "budget_" + Schema.Category.ICON;
        public static final String CATEGORY_TYPE = "budget_" + Schema.Category.TYPE;
        public static final String CATEGORY_SHOW_REPORT = "budget_" + Schema.Category.SHOW_REPORT;
        public static final String CATEGORY_TAG = "budget_" + Schema.Category.TAG;
        public static final String START_DATE = Schema.Budget.START_DATE;
        public static final String END_DATE = Schema.Budget.END_DATE;
        public static final String MONEY = Schema.Budget.MONEY;
        public static final String CURRENCY = Schema.Budget.CURRENCY;
        public static final String PROGRESS = "budget_" + Schema.Alias.PROGRESS;
        public static final String WALLET_IDS = "budget_wallet_ids";
        public static final String HAS_WALLET_IN_TOTAL = "budget_has_wallet_in_total";
        public static final String TAG = Schema.Budget.TAG;
    }

    public static final class Saving {
        public static final String ID = Schema.Saving.ID;
        public static final String DESCRIPTION = Schema.Saving.DESCRIPTION;
        public static final String ICON = Schema.Saving.ICON;
        public static final String START_MONEY = Schema.Saving.START_MONEY;
        public static final String END_MONEY = Schema.Saving.END_MONEY;
        public static final String WALLET_ID = Schema.Saving.WALLET;
        public static final String WALLET_NAME = "saving_" + Schema.Wallet.NAME;
        public static final String WALLET_ICON = "saving_" + Schema.Wallet.ICON;
        public static final String WALLET_CURRENCY = "saving_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_COUNT_IN_TOTAL = "saving_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_ARCHIVED = "saving_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TAG = "saving_" + Schema.Wallet.TAG;
        public static final String END_DATE = Schema.Saving.END_DATE;
        public static final String COMPLETE = Schema.Saving.COMPLETE;
        public static final String NOTE = Schema.Saving.NOTE;
        public static final String PROGRESS = "saving_" + Schema.Alias.PROGRESS;
        public static final String TAG = Schema.Saving.TAG;
    }

    public static final class Event {
        public static final String ID = Schema.Event.ID;
        public static final String NAME = Schema.Event.NAME;
        public static final String ICON = Schema.Event.ICON;
        public static final String NOTE = Schema.Event.NOTE;
        public static final String START_DATE = Schema.Event.START_DATE;
        public static final String END_DATE = Schema.Event.END_DATE;
        public static final String PROGRESS = "event_" + Schema.Alias.PROGRESS;
        public static final String TAG = Schema.Event.TAG;
    }

    public static final class RecurrentTransaction {
        public static final String ID = Schema.RecurrentTransaction.ID;
        public static final String MONEY = Schema.RecurrentTransaction.MONEY;
        public static final String DESCRIPTION = Schema.RecurrentTransaction.DESCRIPTION;
        public static final String CATEGORY_ID = Schema.RecurrentTransaction.CATEGORY;
        public static final String CATEGORY_NAME = "model_transaction_" + Schema.Category.NAME;
        public static final String CATEGORY_ICON = "model_transaction_" + Schema.Category.ICON;
        public static final String CATEGORY_TYPE = "model_transaction_" + Schema.Category.TYPE;
        public static final String CATEGORY_SHOW_REPORT = "model_transaction_" + Schema.Category.SHOW_REPORT;
        public static final String CATEGORY_TAG = "model_transaction_" + Schema.Category.TAG;
        public static final String DIRECTION = Schema.RecurrentTransaction.DIRECTION;
        public static final String WALLET_ID = Schema.RecurrentTransaction.WALLET;
        public static final String WALLET_NAME = "model_transaction_" + Schema.Wallet.NAME;
        public static final String WALLET_ICON = "model_transaction_" + Schema.Wallet.ICON;
        public static final String WALLET_CURRENCY = "model_transaction_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_COUNT_IN_TOTAL = "model_transaction_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_ARCHIVED = "model_transaction_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TAG = "model_transaction_" + Schema.Wallet.TAG;
        public static final String PLACE_ID = Schema.RecurrentTransaction.PLACE;
        public static final String PLACE_NAME = "model_transaction_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "model_transaction_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "model_transaction_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "model_transaction_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "model_transaction_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "model_transaction_" + Schema.Place.TAG;
        public static final String NOTE = Schema.RecurrentTransaction.NOTE;
        public static final String EVENT_ID = Schema.RecurrentTransaction.EVENT;
        public static final String EVENT_NAME = "model_transaction_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "model_transaction_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "model_transaction_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "model_transaction_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "model_transaction_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "model_transaction_" + Schema.Event.TAG;
        public static final String CONFIRMED = Schema.RecurrentTransaction.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.RecurrentTransaction.COUNT_IN_TOTAL;
        public static final String START_DATE = Schema.RecurrentTransaction.START_DATE;
        public static final String LAST_OCCURRENCE = Schema.RecurrentTransaction.LAST_OCCURRENCE;
        public static final String NEXT_OCCURRENCE = Schema.RecurrentTransaction.NEXT_OCCURRENCE;
        public static final String RULE = Schema.RecurrentTransaction.RULE;
        public static final String TAG = Schema.RecurrentTransaction.TAG;
    }

    public static final class RecurrentTransfer {
        public static final String ID = Schema.RecurrentTransfer.ID;
        public static final String DESCRIPTION = Schema.RecurrentTransfer.DESCRIPTION;
        public static final String WALLET_FROM_ID = Schema.RecurrentTransfer.WALLET_FROM;
        public static final String WALLET_FROM_NAME = "model_transaction_from_" + Schema.Wallet.NAME;
        public static final String WALLET_FROM_ICON = "model_transaction_from_" + Schema.Wallet.ICON;
        public static final String WALLET_FROM_CURRENCY = "model_transaction_from_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_FROM_COUNT_IN_TOTAL = "model_transaction_from_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_FROM_ARCHIVED = "model_transaction_from_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_FROM_TAG = "model_transaction_from_" + Schema.Wallet.TAG;
        public static final String WALLET_TO_ID = Schema.RecurrentTransfer.WALLET_TO;
        public static final String WALLET_TO_NAME = "model_transaction_to_" + Schema.Wallet.NAME;
        public static final String WALLET_TO_ICON = "model_transaction_to_" + Schema.Wallet.ICON;
        public static final String WALLET_TO_CURRENCY = "model_transaction_to_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_TO_COUNT_IN_TOTAL = "model_transaction_to_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_TO_ARCHIVED = "model_transaction_to_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TO_TAG = "model_transaction_to_" + Schema.Wallet.TAG;
        public static final String MONEY_FROM = Schema.RecurrentTransfer.MONEY_FROM;
        public static final String MONEY_TO = Schema.RecurrentTransfer.MONEY_TO;
        public static final String MONEY_TAX = Schema.RecurrentTransfer.MONEY_TAX;
        public static final String NOTE = Schema.RecurrentTransfer.NOTE;
        public static final String EVENT_ID = Schema.RecurrentTransfer.EVENT;
        public static final String EVENT_NAME = "model_transfer_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "model_transfer_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "model_transfer_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "model_transfer_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "model_transfer_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "model_transfer_" + Schema.Event.TAG;
        public static final String PLACE_ID = Schema.RecurrentTransfer.PLACE;
        public static final String PLACE_NAME = "model_transfer_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "model_transfer_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "model_transfer_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "model_transfer_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "model_transfer_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "model_transfer_" + Schema.Place.TAG;
        public static final String CONFIRMED = Schema.RecurrentTransfer.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.RecurrentTransfer.COUNT_IN_TOTAL;
        public static final String START_DATE = Schema.RecurrentTransfer.START_DATE;
        public static final String LAST_OCCURRENCE = Schema.RecurrentTransfer.LAST_OCCURRENCE;
        public static final String NEXT_OCCURRENCE = Schema.RecurrentTransfer.NEXT_OCCURRENCE;
        public static final String RULE = Schema.RecurrentTransfer.RULE;
        public static final String TAG = Schema.RecurrentTransfer.TAG;
    }

    public static final class TransactionModel {
        public static final String ID = Schema.TransactionModel.ID;
        public static final String MONEY = Schema.TransactionModel.MONEY;
        public static final String DESCRIPTION = Schema.TransactionModel.DESCRIPTION;
        public static final String CATEGORY_ID = Schema.TransactionModel.CATEGORY;
        public static final String CATEGORY_NAME = "model_transaction_" + Schema.Category.NAME;
        public static final String CATEGORY_ICON = "model_transaction_" + Schema.Category.ICON;
        public static final String CATEGORY_TYPE = "model_transaction_" + Schema.Category.TYPE;
        public static final String CATEGORY_SHOW_REPORT = "model_transaction_" + Schema.Category.SHOW_REPORT;
        public static final String CATEGORY_TAG = "model_transaction_" + Schema.Category.TAG;
        public static final String DIRECTION = Schema.TransactionModel.DIRECTION;
        public static final String WALLET_ID = Schema.TransactionModel.WALLET;
        public static final String WALLET_NAME = "model_transaction_" + Schema.Wallet.NAME;
        public static final String WALLET_ICON = "model_transaction_" + Schema.Wallet.ICON;
        public static final String WALLET_CURRENCY = "model_transaction_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_COUNT_IN_TOTAL = "model_transaction_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_ARCHIVED = "model_transaction_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TAG = "model_transaction_" + Schema.Wallet.TAG;
        public static final String PLACE_ID = Schema.TransactionModel.PLACE;
        public static final String PLACE_NAME = "model_transaction_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "model_transaction_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "model_transaction_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "model_transaction_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "model_transaction_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "model_transaction_" + Schema.Place.TAG;
        public static final String NOTE = Schema.TransactionModel.NOTE;
        public static final String EVENT_ID = Schema.TransactionModel.EVENT;
        public static final String EVENT_NAME = "model_transaction_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "model_transaction_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "model_transaction_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "model_transaction_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "model_transaction_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "model_transaction_" + Schema.Event.TAG;
        public static final String CONFIRMED = Schema.TransactionModel.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.TransactionModel.COUNT_IN_TOTAL;
        public static final String TAG = Schema.TransactionModel.TAG;
    }

    public static final class TransferModel {
        public static final String ID = Schema.TransferModel.ID;
        public static final String DESCRIPTION = Schema.TransferModel.DESCRIPTION;
        public static final String WALLET_FROM_ID = Schema.TransferModel.WALLET_FROM;
        public static final String WALLET_FROM_NAME = "model_transaction_from_" + Schema.Wallet.NAME;
        public static final String WALLET_FROM_ICON = "model_transaction_from_" + Schema.Wallet.ICON;
        public static final String WALLET_FROM_CURRENCY = "model_transaction_from_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_FROM_COUNT_IN_TOTAL = "model_transaction_from_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_FROM_ARCHIVED = "model_transaction_from_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_FROM_TAG = "model_transaction_from_" + Schema.Wallet.TAG;
        public static final String WALLET_TO_ID = Schema.TransferModel.WALLET_TO;
        public static final String WALLET_TO_NAME = "model_transaction_to_" + Schema.Wallet.NAME;
        public static final String WALLET_TO_ICON = "model_transaction_to_" + Schema.Wallet.ICON;
        public static final String WALLET_TO_CURRENCY = "model_transaction_to_" + Schema.Wallet.CURRENCY;
        public static final String WALLET_TO_COUNT_IN_TOTAL = "model_transaction_to_" + Schema.Wallet.COUNT_IN_TOTAL;
        public static final String WALLET_TO_ARCHIVED = "model_transaction_to_" + Schema.Wallet.ARCHIVED;
        public static final String WALLET_TO_TAG = "model_transaction_to_" + Schema.Wallet.TAG;
        public static final String MONEY_FROM = Schema.TransferModel.MONEY_FROM;
        public static final String MONEY_TO = Schema.TransferModel.MONEY_TO;
        public static final String MONEY_TAX = Schema.TransferModel.MONEY_TAX;
        public static final String NOTE = Schema.TransferModel.NOTE;
        public static final String EVENT_ID = Schema.TransferModel.EVENT;
        public static final String EVENT_NAME = "model_transfer_" + Schema.Event.NAME;
        public static final String EVENT_ICON = "model_transfer_" + Schema.Event.ICON;
        public static final String EVENT_NOTE = "model_transfer_" + Schema.Event.NOTE;
        public static final String EVENT_START_DATE = "model_transfer_" + Schema.Event.START_DATE;
        public static final String EVENT_END_DATE = "model_transfer_" + Schema.Event.END_DATE;
        public static final String EVENT_TAG = "model_transfer_" + Schema.Event.TAG;
        public static final String PLACE_ID = Schema.TransferModel.PLACE;
        public static final String PLACE_NAME = "model_transfer_" + Schema.Place.NAME;
        public static final String PLACE_ICON = "model_transfer_" + Schema.Place.ICON;
        public static final String PLACE_ADDRESS = "model_transfer_" + Schema.Place.ADDRESS;
        public static final String PLACE_LATITUDE = "model_transfer_" + Schema.Place.LATITUDE;
        public static final String PLACE_LONGITUDE = "model_transfer_" + Schema.Place.LONGITUDE;
        public static final String PLACE_TAG = "model_transfer_" + Schema.Place.TAG;
        public static final String CONFIRMED = Schema.TransferModel.CONFIRMED;
        public static final String COUNT_IN_TOTAL = Schema.TransferModel.COUNT_IN_TOTAL;
        public static final String TAG = Schema.TransferModel.TAG;
    }

    public static final class Place {
        public static final String ID = Schema.Place.ID;
        public static final String NAME = Schema.Place.NAME;
        public static final String ICON = Schema.Place.ICON;
        public static final String ADDRESS = Schema.Place.ADDRESS;
        public static final String LATITUDE = Schema.Place.LATITUDE;
        public static final String LONGITUDE = Schema.Place.LONGITUDE;
        public static final String TAG = Schema.Place.TAG;
    }

    public static final class Person {
        public static final String ID = Schema.Person.ID;
        public static final String NAME = Schema.Person.NAME;
        public static final String ICON = Schema.Person.ICON;
        public static final String NOTE = Schema.Person.NOTE;
        public static final String TAG = Schema.Person.TAG;
    }

    public static final class Attachment {
        public static final String ID = Schema.Attachment.ID;
        public static final String FILE = Schema.Attachment.FILE;
        public static final String NAME = Schema.Attachment.NAME;
        public static final String TYPE = Schema.Attachment.TYPE;
        public static final String SIZE = Schema.Attachment.SIZE;
        public static final String TAG = Schema.Attachment.TAG;
    }

    public enum CategoryType {

        INCOME(Schema.CategoryType.INCOME),
        EXPENSE(Schema.CategoryType.EXPENSE),
        SYSTEM(Schema.CategoryType.SYSTEM);

        private final int mValue;

        CategoryType(int value) {
            mValue = value;
        }

        public static CategoryType fromValue(int value) {
            switch (value) {
                case Schema.CategoryType.INCOME:
                    return INCOME;
                case Schema.CategoryType.EXPENSE:
                    return EXPENSE;
                case Schema.CategoryType.SYSTEM:
                    return SYSTEM;
            }
            return null;
        }

        public int getValue() {
            return mValue;
        }
    }

    public static final class CategoryTag {
        public static final String TRANSFER = Schema.CategoryTag.TRANSFER;
        public static final String TRANSFER_TAX = Schema.CategoryTag.TRANSFER_TAX;
        public static final String DEBT = Schema.CategoryTag.DEBT;
        public static final String CREDIT = Schema.CategoryTag.CREDIT;
        public static final String PAID_DEBT = Schema.CategoryTag.PAID_DEBT;
        public static final String PAID_CREDIT = Schema.CategoryTag.PAID_CREDIT;
        public static final String TAX = Schema.CategoryTag.TAX;
        public static final String SAVING_DEPOSIT = Schema.CategoryTag.SAVING_DEPOSIT;
        public static final String SAVING_WITHDRAW = Schema.CategoryTag.SAVING_WITHDRAW;
    }

    public enum DebtType {

        DEBT(0), CREDIT(1);

        private final int mValue;

        DebtType(int value) {
            mValue = value;
        }

        public static DebtType fromValue(int value) {
            switch (value) {
                case 0:
                    return DEBT;
                case 1:
                    return CREDIT;
                default:
                    throw new IllegalArgumentException("Invalid debt type");
            }
        }

        public int getValue() {
            return mValue;
        }
    }

    public enum BudgetType {

        EXPENSES(Schema.BudgetType.EXPENSES),
        INCOMES(Schema.BudgetType.INCOMES),
        CATEGORY(Schema.BudgetType.CATEGORY);

        private final int mValue;

        BudgetType(int value) {
            mValue = value;
        }

        public static BudgetType fromValue(int value) {
            switch (value) {
                case Schema.BudgetType.EXPENSES:
                    return EXPENSES;
                case Schema.BudgetType.INCOMES:
                    return INCOMES;
                case Schema.BudgetType.CATEGORY:
                    return CATEGORY;
                default:
                    return null;
            }
        }

        public int getValue() {
            return mValue;
        }
    }

    public static class Direction {
        public static final int INCOME = Schema.Direction.INCOME;
        public static final int EXPENSE = Schema.Direction.EXPENSE;
    }

    public static class TransactionType {
        public static final int STANDARD = 0;
        public static final int TRANSFER = 1;
        public static final int DEBT = 2;
        public static final int SAVING = 3;
        public static final int MODEL = 4;
    }

    public static class ErrorCode {
        public static final int CURRENCY_IN_USE = 4535;
        public static final int WALLET_USED_IN_TRANSFER = 4536;
        public static final int CATEGORY_HAS_CHILDREN = 4537;
        public static final int CATEGORY_IN_USE = 4538;
        public static final int CATEGORY_HIERARCHY_NOT_SUPPORTED = 4539;
        public static final int CATEGORY_NOT_CONSISTENT = 4540;
        public static final int WALLETS_NOT_FOUND = 4541;
        public static final int WALLETS_NOT_CONSISTENT = 4542;
        public static final int SYSTEM_CATEGORY_NOT_MODIFIABLE = 4543;
        public static final int TRANSACTION_USED_IN_TRANSFER = 4544;
        public static final int INVALID_RECURRENCE_RULE = 4545;
    }

    public static List<Long> parseObjectIds(String encodedIds) {
        if (!TextUtils.isEmpty(encodedIds)) {
            List<Long> objectIds = new ArrayList<>();
            String[] parts = encodedIds.split(",");
            for (String part : parts) {
                if (part.startsWith("<") && part.endsWith(">")) {
                    String encodedId = part.substring(1, part.length() - 1);
                    try {
                        objectIds.add(Long.parseLong(encodedId));
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                }
            }
            return objectIds;
        }
        return null;
    }

    public static <T extends Identifiable> String getObjectIds(T[] objects) {
        if (objects != null && objects.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < objects.length; i++) {
                if (i != 0) {
                    builder.append(",");
                }
                builder.append(String.format(Locale.ENGLISH, "<%d>", objects[i].getId()));
            }
            return builder.toString();
        }
        return null;
    }

    public static <T extends Identifiable> String getObjectIds(List<T> objects) {
        if (objects != null && objects.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < objects.size(); i++) {
                if (i != 0) {
                    builder.append(",");
                }
                builder.append(String.format(Locale.ENGLISH, "<%d>", objects.get(i).getId()));
            }
            return builder.toString();
        }
        return null;
    }
}