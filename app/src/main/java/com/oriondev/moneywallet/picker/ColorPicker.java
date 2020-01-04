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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 15/04/18.
 */
public class ColorPicker extends Fragment implements ColorChooserDialog.ColorCallback {

    private static final String SS_CURRENT_COLOR = "ColorPicker::SavedState::CurrentColor";
    private static final String SS_ACCENT_PALETTE = "ColorPicker::SavedState::AccentPalette";
    private static final String ARG_DEFAULT_COLOR = "ColorPicker::Arguments::DefaultColor";
    private static final String ARG_ACCENT_PALETTE = "ColorPicker::Arguments::AccentPalette";

    public static ColorPicker createPicker(FragmentManager fragmentManager, String tag, int color, boolean accentPalette, Controller controller) {
        ColorPicker picker = (ColorPicker) fragmentManager.findFragmentByTag(tag);
        if (picker == null) {
            picker = new ColorPicker();
            Bundle arguments = new Bundle();
            arguments.putInt(ARG_DEFAULT_COLOR, color);
            arguments.putBoolean(ARG_ACCENT_PALETTE, accentPalette);
            picker.setArguments(arguments);
            fragmentManager.beginTransaction().add(picker, tag).commit();
        }
        picker.setController(controller);
        return picker;
    }

    private Controller mController;

    private int mCurrentColor;
    private boolean mAccentPalette;

    private ColorChooserDialog mColorChooserDialog;

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
            mCurrentColor = savedInstanceState.getInt(SS_CURRENT_COLOR);
            mAccentPalette = savedInstanceState.getBoolean(SS_ACCENT_PALETTE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentColor = arguments.getInt(ARG_DEFAULT_COLOR);
                mAccentPalette = arguments.getBoolean(ARG_ACCENT_PALETTE);
            } else {
                mCurrentColor = Color.BLACK;
                mAccentPalette = false;
            }
        }
        createColorChooserDialog(getActivity());
    }

    private void createColorChooserDialog(Activity activity) {
        mColorChooserDialog = ThemedDialog.buildColorChooserDialog(activity, R.string.dialog_color_picker_title)
                .accentMode(mAccentPalette)
                .preselect(mCurrentColor)
                .dynamicButtonColor(true)
                .tag(getDialogTag())
                .build();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely(true);
    }

    private void fireCallbackSafely(boolean autoFired) {
        if (mController != null) {
            mController.onColorChanged(getTag(), mCurrentColor, autoFired);
        }
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_CURRENT_COLOR, mCurrentColor);
        outState.putBoolean(SS_ACCENT_PALETTE, mAccentPalette);
    }

    public void setController(Controller controller) {
        mController = controller;
    }

    public void showPicker() {
        mColorChooserDialog.show(getChildFragmentManager());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        mCurrentColor = selectedColor;
        fireCallbackSafely(false);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        createColorChooserDialog(getActivity());
    }

    public interface Controller {

        void onColorChanged(String tag, int color, boolean autoFired);
    }
}