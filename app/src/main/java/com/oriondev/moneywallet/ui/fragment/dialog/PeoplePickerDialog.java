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

package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Person;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditPersonActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.PeopleSelectorCursorAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 20/03/2018.
 */
public class PeoplePickerDialog extends DialogFragment implements PeopleSelectorCursorAdapter.Controller, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SS_SELECTED_PEOPLE = "PeoplePickerDialog::SavedState::SelectedPeople";

    private static final int DEFAULT_LOADER_ID = 1;

    public static PeoplePickerDialog newInstance() {
        return new PeoplePickerDialog();
    }

    private Callback mCallback;

    private LongSparseArray<Person> mSelectedPeople;

    private RecyclerView mRecyclerView;
    private TextView mMessageTextView;

    private PeopleSelectorCursorAdapter mCursorAdapter;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mSelectedPeople = new LongSparseArray<>();
            Person[] people = (Person[]) savedInstanceState.getParcelableArray(SS_SELECTED_PEOPLE);
            if (people != null) {
                for (Person person : people) {
                    mSelectedPeople.append(person.getId(), person);
                }
            }
        }
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_people_picker_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.action_new)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mCallback != null) {
                            Person[] people = new Person[mSelectedPeople.size()];
                            for (int i = 0; i < mSelectedPeople.size(); i++) {
                                people[i] = mSelectedPeople.valueAt(i);
                            }
                            mCallback.onPeopleSelected(people);
                        }
                    }

                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(getActivity(), NewEditPersonActivity.class));
                    }

                })
                .customView(R.layout.dialog_advanced_list, false)
                .build();
        mCursorAdapter = new PeopleSelectorCursorAdapter(this);
        View view = dialog.getCustomView();
        if (view != null) {
            mRecyclerView = view.findViewById(R.id.recycler_view);
            mMessageTextView = view.findViewById(R.id.message_text_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.setAdapter(mCursorAdapter);
            mMessageTextView.setText(R.string.message_no_person_found);
        }
        mRecyclerView.setVisibility(View.GONE);
        mMessageTextView.setVisibility(View.GONE);
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Person[] people = new Person[mSelectedPeople.size()];
        for (int i = 0; i < mSelectedPeople.size(); i++) {
            people[i] = mSelectedPeople.valueAt(i);
        }
        outState.putParcelableArray(SS_SELECTED_PEOPLE, people);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, Person[] people) {
        mSelectedPeople = new LongSparseArray<>();
        if (people != null && people.length > 0) {
            for (Person person : people) {
                mSelectedPeople.append(person.getId(), person);
            }
        }
        show(fragmentManager, tag);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = DataContentProvider.CONTENT_PEOPLE;
            String[] projection = new String[] {
                    Contract.Person.ID,
                    Contract.Person.NAME,
                    Contract.Person.ICON
            };
            String sortOrder = Contract.Person.NAME;
            return new CursorLoader(activity, uri, projection, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        if (cursor != null && cursor.getCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mMessageTextView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onPersonSelected(Person person) {
        if (mSelectedPeople.indexOfKey(person.getId()) >= 0) {
            mSelectedPeople.remove(person.getId());
        } else {
            mSelectedPeople.append(person.getId(), person);
        }
        mCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean isPersonSelected(long id) {
        return mSelectedPeople.indexOfKey(id) >= 0;
    }

    public interface Callback {

        void onPeopleSelected(Person[] people);
    }
}