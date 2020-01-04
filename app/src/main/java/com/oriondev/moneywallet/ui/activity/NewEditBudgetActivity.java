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

package com.oriondev.moneywallet.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.BudgetTypePicker;
import com.oriondev.moneywallet.picker.CategoryPicker;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.SQLiteDataException;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 06/03/18.
 */
public class NewEditBudgetActivity extends NewEditItemActivity implements MoneyPicker.Controller,
                                                                        BudgetTypePicker.Controller,
                                                                        CategoryPicker.Controller,
                                                                        DateTimePicker.Controller,
                                                                        WalletPicker.MultiWalletController {

    private static final String TAG_MONEY_PICKER = "NewEditBudgetActivity::Tag::MoneyPicker";
    private static final String TAG_BUDGET_TYPE_PICKER = "NewEditBudgetActivity::Tag::BudgetTypePicker";
    private static final String TAG_CATEGORY_PICKER = "NewEditBudgetActivity::Tag::CategoryPicker";
    private static final String TAG_START_DATE_PICKER = "NewEditBudgetActivity::Tag::StartDatePicker";
    private static final String TAG_END_DATE_PICKER = "NewEditBudgetActivity::Tag::EndDatePicker";
    private static final String TAG_WALLETS_PICKER = "NewEditBudgetActivity::Tag::WalletsPicker";

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private MaterialEditText mTypeEditText;
    private MaterialEditText mCategoryEditText;
    private MaterialEditText mStartDateEditText;
    private MaterialEditText mEndDateEditText;
    private MaterialEditText mWalletsEditText;

    private MoneyPicker mMoneyPicker;
    private BudgetTypePicker mBudgetTypePicker;
    private CategoryPicker mCategoryPicker;
    private DateTimePicker mStartDatePicker;
    private DateTimePicker mEndDatePicker;
    private WalletPicker mWalletsPicker;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_money_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
        // attach a listener to the views
        mMoneyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_budget, parent, true);
        mTypeEditText = view.findViewById(R.id.type_edit_text);
        mCategoryEditText = view.findViewById(R.id.category_edit_text);
        mStartDateEditText = view.findViewById(R.id.start_date_edit_text);
        mEndDateEditText = view.findViewById(R.id.end_date_edit_text);
        mWalletsEditText = view.findViewById(R.id.wallets_edit_text);
        // disable unused edit text
        mTypeEditText.setTextViewMode(true);
        mCategoryEditText.setTextViewMode(true);
        mStartDateEditText.setTextViewMode(true);
        mEndDateEditText.setTextViewMode(true);
        mWalletsEditText.setTextViewMode(true);
        // setup validators
        mTypeEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_budget_type);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mBudgetTypePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletsEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_multiple_wallets);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletsPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletsEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_invalid_multiple_wallets);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                CurrencyUnit currencyUnit = null;
                Wallet[] wallets = mWalletsPicker.getCurrentWallets();
                for (Wallet wallet : wallets) {
                    if (currencyUnit == null) {
                        currencyUnit = wallet.getCurrency();
                    } else if (!currencyUnit.equals(wallet.getCurrency())) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mStartDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_start_date);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mStartDatePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mEndDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_end_date);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mEndDatePicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mStartDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_invalid_date_range);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                Date start = mStartDatePicker.getCurrentDateTime();
                Date end = mEndDatePicker.getCurrentDateTime();
                return start != null && end != null && start.getTime() <= end.getTime();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mCategoryEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_category);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mCategoryPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        // setup listeners
        mTypeEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mBudgetTypePicker.showPicker();
            }

        });
        mCategoryEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategoryPicker.showPicker();
            }

        });
        mStartDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartDatePicker.showDatePicker();
            }

        });
        mEndDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEndDatePicker.showDatePicker();
            }

        });
        mWalletsEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletsPicker.showMultiWalletPicker();
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        long money = 0L;
        Contract.BudgetType type = null;
        Category category = null;
        Date startDate = null;
        Date endDate = null;
        Wallet[] wallets = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_BUDGETS, getItemId());
                String[] projection = new String[] {
                        Contract.Budget.TYPE,
                        Contract.Budget.CATEGORY_ID,
                        Contract.Budget.CATEGORY_NAME,
                        Contract.Budget.CATEGORY_ICON,
                        Contract.Budget.CATEGORY_TYPE,
                        Contract.Budget.START_DATE,
                        Contract.Budget.END_DATE,
                        Contract.Budget.MONEY
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        type = Contract.BudgetType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Budget.TYPE)));
                        if (type == Contract.BudgetType.CATEGORY) {
                            category = new Category(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Budget.CATEGORY_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Budget.CATEGORY_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Budget.CATEGORY_ICON))),
                                    Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Budget.CATEGORY_TYPE)))
                            );
                        }
                        money = cursor.getLong(cursor.getColumnIndex(Contract.Budget.MONEY));
                        startDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Budget.START_DATE)));
                        endDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Budget.END_DATE)));
                    }
                    cursor.close();
                }
                // the previous cursor contains only a column with the list of ids of linked wallets.
                // we need instead to buildMaterialDialog the full wallet object so we must perform a separated
                // query to the database to obtain the full cursor.
                uri = Uri.withAppendedPath(uri, "wallets");
                projection = new String[] {
                        Contract.Wallet.ID,
                        Contract.Wallet.NAME,
                        Contract.Wallet.ICON,
                        Contract.Wallet.CURRENCY,
                        Contract.Wallet.START_MONEY
                };
                cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        wallets = new Wallet[cursor.getCount()];
                        for (int i = 0; cursor.moveToPosition(i) && i < cursor.getCount(); i++) {
                            wallets[i] = new Wallet(
                                    cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON))),
                                    CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY))),
                                    cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY)), 0);
                        }
                    }
                    cursor.close();
                }
            } else {
                type = Contract.BudgetType.EXPENSES;
                Calendar calendar = Calendar.getInstance();
                startDate = calendar.getTime();
                endDate = DateUtils.addMonths(calendar, 1);
                String[] projection = new String[] {
                        Contract.Wallet.ID,
                        Contract.Wallet.NAME,
                        Contract.Wallet.ICON,
                        Contract.Wallet.CURRENCY,
                        Contract.Wallet.START_MONEY,
                        Contract.Wallet.TOTAL_MONEY
                };
                long currentWallet = PreferenceManager.getCurrentWallet();
                Cursor cursor;
                if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                    Uri uri = DataContentProvider.CONTENT_WALLETS;
                    cursor = contentResolver.query(uri, projection, null, null, null);
                } else {
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, currentWallet);
                    cursor = contentResolver.query(uri, projection, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        wallets = new Wallet[] {
                                new Wallet(
                                        cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID)),
                                        cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)),
                                        IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON))),
                                        CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY))),
                                        cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY)),
                                        cursor.getLong(cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY))
                                )
                        };
                    }
                    cursor.close();
                }
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, money);
        mBudgetTypePicker = BudgetTypePicker.createPicker(fragmentManager, TAG_BUDGET_TYPE_PICKER, type);
        mCategoryPicker = CategoryPicker.createPicker(fragmentManager, TAG_CATEGORY_PICKER, category);
        mStartDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_START_DATE_PICKER, startDate);
        mEndDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_END_DATE_PICKER, endDate);
        mWalletsPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLETS_PICKER, wallets);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_budget;
            case EDIT_ITEM:
                return R.string.title_activity_edit_budget;
            default:
                return -1;
        }
    }

    private boolean validate() {
        return mTypeEditText.validate() && mWalletsEditText.validate() &&
                mStartDateEditText.validate() && mEndDateEditText.validate() &&
                (mBudgetTypePicker.getCurrentType() != Contract.BudgetType.CATEGORY ||
                        mCategoryEditText.validate());
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Budget.TYPE, mBudgetTypePicker.getCurrentType().getValue());
            if (mBudgetTypePicker.getCurrentType() == Contract.BudgetType.CATEGORY) {
                contentValues.put(Contract.Budget.CATEGORY_ID, mCategoryPicker.getCurrentCategory().getId());
            } else {
                contentValues.putNull(Contract.Budget.CATEGORY_ID);
            }
            contentValues.put(Contract.Budget.START_DATE, DateUtils.getSQLDateString(mStartDatePicker.getCurrentDateTime()));
            contentValues.put(Contract.Budget.END_DATE, DateUtils.getSQLDateString(mEndDatePicker.getCurrentDateTime()));
            contentValues.put(Contract.Budget.MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.Budget.CURRENCY, mWalletsPicker.getCurrentWallets()[0].getCurrency().getIso());
            contentValues.put(Contract.Budget.WALLET_IDS, Contract.getObjectIds(mWalletsPicker.getCurrentWallets()));
            ContentResolver contentResolver = getContentResolver();
            try {
                switch (mode) {
                    case NEW_ITEM:
                        contentResolver.insert(DataContentProvider.CONTENT_BUDGETS, contentValues);
                        break;
                    case EDIT_ITEM:
                        Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_BUDGETS, getItemId());
                        contentResolver.update(uri, contentValues, null, null);
                        break;
                }
            } catch (SQLiteDataException e) {
                int contentRes = 0;
                switch (e.getErrorCode()) {
                    case Contract.ErrorCode.WALLETS_NOT_FOUND:
                        contentRes = R.string.error_input_missing_multiple_wallets;
                        break;
                    case Contract.ErrorCode.WALLETS_NOT_CONSISTENT:
                        contentRes = R.string.error_input_invalid_multiple_wallets;
                        break;
                }
                if (contentRes != 0) {
                    ThemedDialog.buildMaterialDialog(this)
                            .title(R.string.title_error)
                            .content(contentRes)
                            .positiveText(android.R.string.ok)
                            .show();
                }
            }
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
        if (currency != null) {
            mCurrencyTextView.setText(currency.getSymbol());
        } else {
            mCurrencyTextView.setText("?");
        }
    }

    @Override
    public void onTypeChanged(String tag, Contract.BudgetType type) {
        switch (type) {
            case INCOMES:
                mTypeEditText.setText(R.string.hint_incomes);
                mCategoryEditText.setVisibility(View.GONE);
                break;
            case EXPENSES:
                mTypeEditText.setText(R.string.hint_expenses);
                mCategoryEditText.setVisibility(View.GONE);
                break;
            case CATEGORY:
                mTypeEditText.setText(R.string.hint_category);
                mCategoryEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onCategoryChanged(String tag, Category category) {
        if (category != null) {
            mCategoryEditText.setText(category.getName());
        } else {
            mCategoryEditText.setText(null);
        }
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_START_DATE_PICKER:
                if (date != null) {
                    DateFormatter.applyDate(mStartDateEditText, date);
                } else {
                    mStartDateEditText.setText(null);
                }
                break;
            case TAG_END_DATE_PICKER:
                if (date != null) {
                    DateFormatter.applyDate(mEndDateEditText, date);
                } else {
                    mEndDateEditText.setText(null);
                }
                break;
        }
    }

    @Override
    public void onWalletListChanged(String tag, Wallet[] wallets) {
        if (wallets != null && wallets.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < wallets.length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(wallets[i].getName());
            }
            mWalletsEditText.setText(builder);
            mMoneyPicker.setCurrency(wallets[0].getCurrency());
        } else {
            mWalletsEditText.setText(null);
            mMoneyPicker.setCurrency(null);
        }
    }
}