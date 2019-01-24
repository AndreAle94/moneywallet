package com.oriondev.moneywallet.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.SQLiteDataException;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelScrollActivity;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.CurrencyManager;

/**
 * Created by andrea on 05/01/19.
 */
public class NewEditCurrencyActivity extends SinglePanelScrollActivity {

    public static final String MODE = "NewEditCurrencyActivity::Mode";
    public static final String ISO = "NewEditCurrencyActivity::Iso";

    public enum Mode {

        /**
         * This mode indicates that a new item must be created.
         */
        NEW_ITEM,

        /**
         *  When the mode is EDIT_ITEM, the caller MUST set a valid item id or a RuntimeException
         *  is thrown.
         */
        EDIT_ITEM
    }

    private Mode mMode;
    private String mIso;

    private MaterialEditText mNameEditText;
    private MaterialEditText mISOEditText;
    private MaterialEditText mSymbolEditText;
    private MaterialEditText mDecimalsEditText;

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_name_item, parent, true);
        mNameEditText = view.findViewById(R.id.name_edit_text);
        // add validator
        mNameEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_name_not_valid));
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_currency, parent, true);
        mISOEditText = view.findViewById(R.id.iso_edit_text);
        mSymbolEditText = view.findViewById(R.id.symbol_edit_text);
        mDecimalsEditText = view.findViewById(R.id.decimal_number_edit_text);
        // add validators
        mISOEditText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        mISOEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_iso_not_valid);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return charSequence.length() == 3;
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mDecimalsEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_decimals_not_valid);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                try {
                    int decimals = Integer.parseInt(charSequence.toString());
                    return decimals >= 0 && decimals <= 8;
                } catch (NumberFormatException ignore) {}
                return false;
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
    }

    @Override
    @CallSuper
    protected void onViewCreated(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            mMode = (Mode) intent.getSerializableExtra(MODE);
            if (mMode != null) {
                if (mMode == Mode.EDIT_ITEM) {
                    mIso = intent.getStringExtra(ISO);
                    if (TextUtils.isEmpty(mIso)) {
                        throw new RuntimeException("Trying to edit a currency with an invalid iso");
                    }
                } else if (mMode == Mode.NEW_ITEM) {
                    mIso = null;
                }
            } else {
                mMode = Mode.NEW_ITEM;
                mIso = null;
            }
        }
        if (savedInstanceState == null && mMode == Mode.EDIT_ITEM) {
            // always load data from the database, the currency manager can be not up-to-date
            Uri uri = Uri.withAppendedPath(DataContentProvider.CONTENT_CURRENCIES, mIso);
            String[] projections = new String[] {
                    Contract.Currency.NAME,
                    Contract.Currency.ISO,
                    Contract.Currency.SYMBOL,
                    Contract.Currency.DECIMALS
            };
            Cursor cursor = getContentResolver().query(uri, projections, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mNameEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Currency.NAME)));
                    mISOEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Currency.ISO)));
                    mSymbolEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Currency.SYMBOL)));
                    mDecimalsEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Currency.DECIMALS)));
                }
                cursor.close();
            }
        }
        // disable the iso fields in editing mode
        if (mMode == Mode.EDIT_ITEM) {
            mISOEditText.setTextViewMode(true);
        }
    }

    @Override
    protected int getActivityTitleRes() {
        switch (mMode) {
            case NEW_ITEM:
                return R.string.title_activity_new_currency;
            case EDIT_ITEM:
                return R.string.title_activity_edit_currency;
            default:
                return -1;
        }
    }

    @Override
    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_new_edit_delete_item;
    }

    @Override
    protected void onMenuCreated(Menu menu) {
        if (mMode == Mode.NEW_ITEM) {
            menu.findItem(R.id.action_delete_item).setVisible(false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_changes:
                onSaveChanges();
                break;
            case R.id.action_delete_item:
                ThemedDialog.buildMaterialDialog(this)
                        .title(R.string.title_warning)
                        .content(R.string.message_delete_currency)
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                onDelete();
                            }

                        })
                        .show();
                break;
        }
        return false;
    }

    private boolean validate() {
        return mNameEditText.validate() && mISOEditText.validate() && mDecimalsEditText.validate();
    }

    private void onSaveChanges() {
        if (validate()) {
            int decimalCount = Integer.parseInt(mDecimalsEditText.getTextAsString());
            final ContentResolver contentResolver = getContentResolver();
            final ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Currency.NAME, mNameEditText.getTextAsString().trim());
            contentValues.put(Contract.Currency.SYMBOL, TextUtils.isEmpty(mSymbolEditText.getTextAsString().trim()) ? null : mSymbolEditText.getTextAsString().trim());
            contentValues.put(Contract.Currency.DECIMALS, decimalCount);
            switch (mMode) {
                case NEW_ITEM:
                    contentValues.put(Contract.Currency.ISO, mISOEditText.getTextAsString().trim());
                    contentResolver.insert(DataContentProvider.CONTENT_CURRENCIES, contentValues);
                    CurrencyManager.invalidateCache(this);
                    break;
                case EDIT_ITEM:
                    // check if the database should be normalized when updating the currency
                    CurrencyUnit currencyUnit = CurrencyManager.getCurrency(mIso);
                    if (currencyUnit != null) {
                        final Uri uri = Uri.withAppendedPath(DataContentProvider.CONTENT_CURRENCIES, mIso);
                        if (currencyUnit.getDecimals() != decimalCount) {
                            MaterialDialog.Builder builder = ThemedDialog.buildMaterialDialog(this)
                                    .title(R.string.title_warning)
                                    .positiveText(android.R.string.ok)
                                    .negativeText(android.R.string.cancel)
                                    .neutralText(R.string.action_skip)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {

                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            contentValues.put(Contract.Currency.FIX_MONEY_DECIMALS, true);
                                            contentResolver.update(uri, contentValues, null, null);
                                            CurrencyManager.invalidateCache(NewEditCurrencyActivity.this);
                                            setResult(Activity.RESULT_OK);
                                            finish();
                                        }

                                    })
                                    .onNeutral(new MaterialDialog.SingleButtonCallback() {

                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            contentValues.put(Contract.Currency.FIX_MONEY_DECIMALS, false);
                                            contentResolver.update(uri, contentValues, null, null);
                                            CurrencyManager.invalidateCache(NewEditCurrencyActivity.this);
                                            setResult(Activity.RESULT_OK);
                                            finish();
                                        }

                                    });
                            if (currencyUnit.getDecimals() < decimalCount) {
                                // in this case we have to multiply the current values
                                int exponent = decimalCount - currencyUnit.getDecimals();
                                int multiplier = (int) Math.pow(10, exponent);
                                builder.content(R.string.message_multiply_currency_decimals, multiplier);
                            } else {
                                // in this case we have to divide the current values
                                int exponent = currencyUnit.getDecimals() - decimalCount;
                                int divider = (int) Math.pow(10, exponent);
                                builder.content(R.string.message_divide_currency_decimals, divider);
                            }
                            builder.show();
                            return;
                        } else {
                            // no normalization is needed, directly update the database
                            contentResolver.update(uri, contentValues, null, null);
                            CurrencyManager.invalidateCache(this);
                        }
                    }
                    break;
            }
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void onDelete() {
        if (mMode == Mode.EDIT_ITEM) {
            Uri uri = Uri.withAppendedPath(DataContentProvider.CONTENT_CURRENCIES, mIso);
            ContentResolver contentResolver = getContentResolver();
            try {
                contentResolver.delete(uri, null, null);
                CurrencyManager.invalidateCache(this);
                setResult(Activity.RESULT_OK);
                finish();
            } catch (SQLiteDataException e) {
                if (e.getErrorCode() == Contract.ErrorCode.CURRENCY_IN_USE) {
                    ThemedDialog.buildMaterialDialog(this)
                            .title(R.string.title_error)
                            .content(R.string.message_error_delete_currency)
                            .positiveText(android.R.string.ok)
                            .show();
                }
            }
        }
    }
}