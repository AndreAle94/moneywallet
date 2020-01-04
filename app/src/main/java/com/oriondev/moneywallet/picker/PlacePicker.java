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

import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.ui.fragment.dialog.PlacePickerDialog;

/**
 * Created by andrea on 10/03/18.
 */

public class PlacePicker extends Fragment implements PlacePickerDialog.Callback {

    private static final String SS_CURRENT_PLACE = "PlacePicker::SavedState::CurrentPlace";

    private static final String ARG_DEFAULT_PLACE = "PlacePicker::Arguments::DefaultPlace";

    private Controller mController;

    private Place mCurrentPlace;

    private PlacePickerDialog mPlacePickerDialog;

    public static PlacePicker createPicker(FragmentManager fragmentManager, String tag, Place defaultPlace) {
        PlacePicker placePicker = (PlacePicker) fragmentManager.findFragmentByTag(tag);
        if (placePicker == null) {
            placePicker = new PlacePicker();
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_PLACE, defaultPlace);
            placePicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(placePicker, tag).commit();
        }
        return placePicker;
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
            mCurrentPlace = savedInstanceState.getParcelable(SS_CURRENT_PLACE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentPlace = arguments.getParcelable(ARG_DEFAULT_PLACE);
            }
        }
        mPlacePickerDialog = (PlacePickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mPlacePickerDialog == null) {
            mPlacePickerDialog = PlacePickerDialog.newInstance();
        }
        mPlacePickerDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onPlaceChanged(getTag(), mCurrentPlace);
        }
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_PLACE, mCurrentPlace);
    }

    public boolean isSelected() {
        return mCurrentPlace != null;
    }

    public void setCurrentPlace(Place place) {
        mCurrentPlace = place;
        fireCallbackSafely();
    }

    public Place getCurrentPlace() {
        return mCurrentPlace;
    }

    public void showPicker() {
        mPlacePickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentPlace);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onPlaceSelected(Place place) {
        mCurrentPlace = place;
        fireCallbackSafely();
    }

    public interface Controller {

        void onPlaceChanged(String tag, Place place);
    }
}