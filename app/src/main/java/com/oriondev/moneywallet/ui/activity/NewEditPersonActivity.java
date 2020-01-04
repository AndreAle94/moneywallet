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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.picker.IconPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.NonEmptyTextValidator;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andrea on 06/03/18.
 */
public class NewEditPersonActivity extends NewEditItemActivity implements IconPicker.Controller {

    private static final String TAG_ICON_PICKER = "NewEditPersonActivity::Tag::IconPicker";

    private ImageView mIconView;
    private MaterialEditText mNameEditText;
    private MaterialEditText mNoteEditText;

    private IconPicker mIconPicker;

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
        View view = inflater.inflate(R.layout.layout_panel_new_edit_person, parent, true);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        Icon icon = null;
        if (savedInstanceState == null) {
            if (getMode() == Mode.EDIT_ITEM) {
                ContentResolver contentResolver = getContentResolver();
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PEOPLE, getItemId());
                String[] projection = new String[] {
                        Contract.Person.ID,
                        Contract.Person.NAME,
                        Contract.Person.ICON,
                        Contract.Person.NOTE
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mNameEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Person.NAME)));
                        icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Person.ICON)));
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.Person.NOTE)));
                    }
                    cursor.close();
                }
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mIconPicker = IconPicker.createPicker(fragmentManager, TAG_ICON_PICKER, icon);
        // configure pickers
        mIconPicker.listenOn(mNameEditText);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_person;
            case EDIT_ITEM:
                return R.string.title_activity_edit_person;
            default:
                return -1;
        }
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (mNameEditText.validate()) {
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.Person.NAME, mNameEditText.getTextAsString());
            contentValues.put(Contract.Person.ICON, mIconPicker.getCurrentIcon().toString());
            contentValues.put(Contract.Person.NOTE, mNoteEditText.getTextAsString());
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_PEOPLE, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PEOPLE, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onIconChanged(String tag, Icon icon) {
        IconLoader.loadInto(icon, mIconView);
    }
}