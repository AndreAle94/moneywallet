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

import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.ui.fragment.dialog.EventPickerDialog;

import java.util.Date;

/**
 * Created by andrea on 10/03/18.
 */
public class EventPicker extends Fragment implements EventPickerDialog.Callback {

    private static final String SS_CURRENT_EVENT = "EventPicker::SavedState::CurrentEvent";

    private static final String ARG_DEFAULT_EVENT = "EventPicker::Arguments::DefaultEvent";

    private Controller mController;

    private Event mCurrentEvent;

    private EventPickerDialog mEventPickerDialog;

    public static EventPicker createPicker(FragmentManager fragmentManager, String tag, Event defaultEvent) {
        EventPicker eventPicker = (EventPicker) fragmentManager.findFragmentByTag(tag);
        if (eventPicker == null) {
            eventPicker = new EventPicker();
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_EVENT, defaultEvent);
            eventPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(eventPicker, tag).commit();
        }
        return eventPicker;
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
            mCurrentEvent = savedInstanceState.getParcelable(SS_CURRENT_EVENT);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentEvent = arguments.getParcelable(ARG_DEFAULT_EVENT);
            }
        }
        mEventPickerDialog = (EventPickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mEventPickerDialog == null) {
            mEventPickerDialog = EventPickerDialog.newInstance();
        }
        mEventPickerDialog.setCallback(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onEventChanged(getTag(), mCurrentEvent);
        }
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_EVENT, mCurrentEvent);
    }

    public boolean isSelected() {
        return mCurrentEvent != null;
    }

    public void setCurrentEvent(Event event) {
        mCurrentEvent = event;
        fireCallbackSafely();
    }

    public Event getCurrentEvent() {
        return mCurrentEvent;
    }

    public void showPicker(Date filterDate) {
        mEventPickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentEvent, filterDate);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onEventSelected(Event event) {
        mCurrentEvent = event;
        fireCallbackSafely();
    }

    public interface Controller {

        void onEventChanged(String tag, Event event);
    }
}