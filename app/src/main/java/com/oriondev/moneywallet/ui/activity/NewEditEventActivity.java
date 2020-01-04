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
import android.widget.ImageView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.DateTimePicker;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrea on 02/03/18.
 */
public class NewEditEventActivity extends NewEditItemActivity implements IconPicker.Controller, DateTimePicker.Controller {

    private static final String TAG_ICON_PICKER = "NewEditEventActivity::Tag::IconPicker";
    private static final String TAG_START_DATE_PICKER = "NewEditEventActivity::Tag::StartDatePicker";
    private static final String TAG_END_DATE_PICKER = "NewEditEventActivity::Tag::EndDatePicker";

    private ImageView mIconView;
    private MaterialEditText mNameEditText;
    private MaterialEditText mStartDateEditText;
    private MaterialEditText mEndDateEditText;
    private MaterialEditText mNoteEditText;

    private IconPicker mIconPicker;
    private DateTimePicker mStartDatePicker;
    private DateTimePicker mEndDatePicker;

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_icon_name_item, parent, true);
        mIconView = view.findViewById(R.id.icon_image_view);
        mNameEditText = view.findViewById(R.id.name_edit_text);
        mNameEditText.addValidator(new NonEmptyTextValidator(this, R.string.error_input_name_not_valid));
        // attach a listener to the views
        mIconView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mIconPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_event, parent, true);
        mStartDateEditText = view.findViewById(R.id.start_date_edit_text);
        mEndDateEditText = view.findViewById(R.id.end_date_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        // disable edit text capabilities when not needed
        mStartDateEditText.setTextViewMode(true);
        mEndDateEditText.setTextViewMode(true);
        // add validators
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
        // attach a listener to open the date picker
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
        // add validator
        mStartDateEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_date_range_not_valid);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                Date startDate = mStartDatePicker.getCurrentDateTime();
                Date endDate = mEndDatePicker.getCurrentDateTime();
                return startDate.compareTo(endDate) <= 0;
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        String name = null;
        Icon icon = null;
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        Date endDate = DateUtils.addMonths(calendar, 1);
        String note = null;
        // load default values if in edit mode
        if (savedInstanceState == null && getMode() == Mode.EDIT_ITEM) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_EVENTS, getItemId());
            String[] projection = new String[] {
                    Contract.Event.NAME,
                    Contract.Event.ICON,
                    Contract.Event.START_DATE,
                    Contract.Event.END_DATE,
                    Contract.Event.NOTE
            };
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(Contract.Event.NAME));
                    icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Event.ICON)));
                    startDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Event.START_DATE)));
                    endDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.Event.END_DATE)));
                    mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Event.NOTE)));
                }
                cursor.close();
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mNameEditText.setText(name);
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        mStartDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_START_DATE_PICKER, startDate);
        mEndDatePicker = DateTimePicker.createPicker(fragmentManager, TAG_END_DATE_PICKER, endDate);
        // configure pickers
        mIconPicker.listenOn(mNameEditText);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_event;
            case EDIT_ITEM:
                return R.string.title_activity_edit_event;
            default:
                return -1;
        }
    }

    private boolean validate() {
        return mNameEditText.validate() && mStartDateEditText.validate() && mEndDateEditText.validate();
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Event.NAME, mNameEditText.getTextAsString());
            contentValues.put(Contract.Event.ICON, mIconPicker.getCurrentIcon().toString());
            contentValues.put(Contract.Event.START_DATE, DateUtils.getSQLDateString(mStartDatePicker.getCurrentDateTime()));
            contentValues.put(Contract.Event.END_DATE, DateUtils.getSQLDateString(mEndDatePicker.getCurrentDateTime()));
            contentValues.put(Contract.Event.NOTE, mNoteEditText.getTextAsString());
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_EVENTS, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_EVENTS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mIconView);
    }

    @Override
    public void onDateTimeChanged(String tag, Date date) {
        switch (tag) {
            case TAG_START_DATE_PICKER:
                DateFormatter.applyDate(mStartDateEditText, date);
                break;
            case TAG_END_DATE_PICKER:
                DateFormatter.applyDate(mEndDateEditText, date);
                break;
        }
    }
}