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

package com.oriondev.moneywallet.picker;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.model.Person;
import com.oriondev.moneywallet.ui.fragment.dialog.PeoplePickerDialog;

/**
 * Created by andrea on 10/03/18.
 */
public class PersonPicker extends Fragment implements PeoplePickerDialog.Callback {

    private static final String SS_CURRENT_PEOPLE = "PersonPicker::SavedState::CurrentPeople";

    private static final String ARG_DEFAULT_PEOPLE = "PersonPicker::Arguments::DefaultPeople";

    private Controller mController;

    private Person[] mCurrentPeople;

    private PeoplePickerDialog mPeoplePickerDialog;

    public static PersonPicker createPicker(FragmentManager fragmentManager, String tag, Person[] defaultPeople) {
        PersonPicker personPicker = (PersonPicker) fragmentManager.findFragmentByTag(tag);
        if (personPicker == null) {
            personPicker = new PersonPicker();
            Bundle arguments = new Bundle();
            arguments.putParcelableArray(ARG_DEFAULT_PEOPLE, defaultPeople);
            personPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(personPicker, tag).commit();
        }
        return personPicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPeople = (Person[]) savedInstanceState.getParcelableArray(SS_CURRENT_PEOPLE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentPeople = (Person[]) arguments.getParcelableArray(ARG_DEFAULT_PEOPLE);
            }
        }
        mPeoplePickerDialog = (PeoplePickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mPeoplePickerDialog == null) {
            mPeoplePickerDialog = PeoplePickerDialog.newInstance();
        }
        mPeoplePickerDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onPeopleChanged(getTag(), mCurrentPeople);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(SS_CURRENT_PEOPLE, mCurrentPeople);
    }

    public boolean isSelected() {
        return mCurrentPeople != null && mCurrentPeople.length > 0;
    }

    public void setPeople(Person[] people) {
        mCurrentPeople = people;
        fireCallbackSafely();
    }

    public Person[] getCurrentPeople() {
        return mCurrentPeople;
    }

    public void showPicker() {
        mPeoplePickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentPeople);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onPeopleSelected(Person[] people) {
        mCurrentPeople = people;
        fireCallbackSafely();
    }

    public interface Controller {

        void onPeopleChanged(String tag, Person[] people);
    }
}