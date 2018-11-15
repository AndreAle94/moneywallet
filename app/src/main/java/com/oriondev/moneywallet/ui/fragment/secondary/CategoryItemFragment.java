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

package com.oriondev.moneywallet.ui.fragment.secondary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.database.SQLiteDataException;
import com.oriondev.moneywallet.ui.activity.NewEditCategoryActivity;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andrea on 01/04/18.
 */
public class CategoryItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CATEGORY_LOADER_ID = 246327;

    private View mProgressLayout;
    private View mMainLayout;

    private ImageView mAvatarImageView;
    private TextView mNameTextView;
    private TextView mParentCategoryTextView;
    private RadioGroup mCategoryTypeRadioGroup;
    private RadioButton mIncomeRadioButton;
    private RadioButton mExpenseRadioButton;
    private CheckBox mShowReportCheckBox;

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_icon_name_item, parent, true);
        mAvatarImageView = view.findViewById(R.id.avatar_image_view);
        mNameTextView = view.findViewById(R.id.name_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_category_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mParentCategoryTextView = view.findViewById(R.id.parent_category_text_view);
        mCategoryTypeRadioGroup = view.findViewById(R.id.category_type);
        mIncomeRadioButton = view.findViewById(R.id.income_radio_button);
        mExpenseRadioButton = view.findViewById(R.id.expense_radio_button);
        mShowReportCheckBox = view.findViewById(R.id.show_report_check_box);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_category);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_list_edit_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_transaction_list:
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(TransactionListActivity.CATEGORY_ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_edit_item:
                intent = new Intent(getActivity(), NewEditCategoryActivity.class);
                intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.EDIT_ITEM);
                intent.putExtra(NewEditItemActivity.ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_delete_item:
                showDeleteDialog(getActivity());
                break;
        }
        return false;
    }

    private void showDeleteDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_delete_category)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            try {
                                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, getItemId());
                                ContentResolver contentResolver = activity.getContentResolver();
                                contentResolver.delete(uri, null, null);
                                navigateBackSafely();
                                showItemId(0L);
                            } catch (SQLiteDataException e) {
                                int contentRes = 0;
                                switch (e.getErrorCode()) {
                                    case Contract.ErrorCode.CATEGORY_HAS_CHILDREN:
                                        contentRes = R.string.message_error_delete_category_with_children;
                                        break;
                                    case Contract.ErrorCode.CATEGORY_IN_USE:
                                        contentRes = R.string.message_error_delete_category_in_use;
                                        break;
                                    case Contract.ErrorCode.SYSTEM_CATEGORY_NOT_MODIFIABLE:
                                        contentRes = R.string.message_error_delete_system_category;
                                        break;
                                }
                                if (contentRes != 0) {
                                    ThemedDialog.buildMaterialDialog(activity)
                                            .title(R.string.title_error)
                                            .content(contentRes)
                                            .positiveText(android.R.string.ok)
                                            .show();
                                }
                            }
                        }
                    }

                })
                .show();
    }

    @Override
    protected void onShowItemId(long itemId) {
        setLoadingScreen(true);
        getLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mAvatarImageView.setImageDrawable(null);
            mNameTextView.setText(null);
            mProgressLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_CATEGORIES, getItemId());
            String[] projection = new String[] {
                    Contract.Category.NAME,
                    Contract.Category.ICON,
                    Contract.Category.PARENT,
                    Contract.Category.PARENT_NAME,
                    Contract.Category.TYPE,
                    Contract.Category.SHOW_REPORT
            };
            return new CursorLoader(getActivity(), uri, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            Icon icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Category.ICON)));
            IconLoader.loadInto(icon, mAvatarImageView);
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Category.NAME)));
            if (!cursor.isNull(cursor.getColumnIndex(Contract.Category.PARENT))) {
                mParentCategoryTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Category.PARENT_NAME)));
                mParentCategoryTextView.setVisibility(View.VISIBLE);
            } else {
                mParentCategoryTextView.setVisibility(View.GONE);
            }
            Contract.CategoryType type = Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.Category.TYPE)));
            if (type != null) {
                switch (type) {
                    case INCOME:
                        mIncomeRadioButton.setChecked(true);
                        mCategoryTypeRadioGroup.setVisibility(View.VISIBLE);
                        break;
                    case EXPENSE:
                        mExpenseRadioButton.setChecked(true);
                        mCategoryTypeRadioGroup.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mCategoryTypeRadioGroup.setVisibility(View.GONE);
                        break;
                }
            } else {
                mCategoryTypeRadioGroup.setVisibility(View.GONE);
            }
            if (cursor.getInt(cursor.getColumnIndex(Contract.Category.SHOW_REPORT)) == 1) {
                mShowReportCheckBox.setChecked(true);
                mShowReportCheckBox.setText(R.string.hint_show_category_report_on);
            } else {
                mShowReportCheckBox.setChecked(false);
                mShowReportCheckBox.setText(R.string.hint_show_category_report_off);
            }
        } else {
            showItemId(0L);
        }
        setLoadingScreen(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }
}