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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.ColorIcon;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.VectorIcon;
import com.oriondev.moneywallet.ui.activity.IconListActivity;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.Utils;

/**
 * Created by andrea on 01/02/18.
 */
public class IconPicker extends Fragment implements ColorChooserDialog.ColorCallback {

    private static final String SS_CURRENT_ICON = "IconPicker::SavedState::CurrentIcon";
    private static final String SS_LAST_BG_COLOR = "IconPicker::SavedState::LastBackgroundColor";
    private static final String ARG_DEFAULT_ICON = "IconPicker::Arguments::DefaultIcon";

    private static final int REQUEST_ICON_PICKER = 57;

    private static final String DEFAULT_BACKGROUND_COLOR = "#000000";

    private Controller mController;

    private Icon mCurrentIcon;
    private String mLastBackgroundColor;

    private EditText mBindEditText;

    public static IconPicker createPicker(FragmentManager fragmentManager, String tag) {
        return createPicker(fragmentManager, tag, new ColorIcon(DEFAULT_BACKGROUND_COLOR, "?"));
    }

    public static IconPicker createPicker(FragmentManager fragmentManager, String tag, Icon defaultIcon) {
        IconPicker iconPicker = (IconPicker) fragmentManager.findFragmentByTag(tag);
        if (iconPicker == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_ICON, defaultIcon);
            iconPicker = new IconPicker();
            iconPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(iconPicker, tag).commit();
        }
        return iconPicker;
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
            mCurrentIcon = savedInstanceState.getParcelable(SS_CURRENT_ICON);
            mLastBackgroundColor = savedInstanceState.getString(SS_LAST_BG_COLOR);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentIcon = arguments.getParcelable(ARG_DEFAULT_ICON);
                if (mCurrentIcon != null) {
                    if (mCurrentIcon instanceof ColorIcon) {
                        mLastBackgroundColor = Utils.getHexColor(((ColorIcon) mCurrentIcon).getColor());
                    } else {
                        mLastBackgroundColor = DEFAULT_BACKGROUND_COLOR;
                    }
                } else {
                    mCurrentIcon = new ColorIcon(DEFAULT_BACKGROUND_COLOR, "?");
                    mLastBackgroundColor = DEFAULT_BACKGROUND_COLOR;
                }
            } else {
                mCurrentIcon = new ColorIcon(DEFAULT_BACKGROUND_COLOR, "?");
                mLastBackgroundColor = DEFAULT_BACKGROUND_COLOR;
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onIconChanged(getTag(), mCurrentIcon);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_ICON, mCurrentIcon);
        outState.putString(SS_LAST_BG_COLOR, mLastBackgroundColor);
    }

    public void listenOn(EditText editText) {
        mBindEditText = editText;
        mBindEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) {
                if (mCurrentIcon instanceof ColorIcon) {
                    String text = getColorIconString(sequence.toString());
                    mCurrentIcon = new ColorIcon((ColorIcon) mCurrentIcon, text);
                    fireCallbackSafely();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }

    public static String getColorIconString(String source) {
        String trimmed = source != null ? source.trim().replaceAll(" +", " ") : null;
        if (!TextUtils.isEmpty(trimmed)) {
            String[] parts = trimmed.split(" ");
            if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                String builder = String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
                return builder.toUpperCase();
            } else {
                return trimmed.substring(0, 1).toUpperCase();
            }
        }
        return "?";
    }

    public Icon getCurrentIcon() {
        return mCurrentIcon;
    }

    public void setCurrentIcon(Icon icon) {
        mCurrentIcon = icon;
        fireCallbackSafely();
    }

    /**
     * This method is responsible to analyze the current icon and detect the possible actions that
     * the user can make on it.
     * In details, if the current icon is an instance of:
     * - {@link ColorIcon} the user can select an icon instead or modify the background color.
     * - {@link VectorIcon} the user can change the icon or simply remove it.
     */
    public void showPicker() {
        Activity activity = getActivity();
        if (activity != null) {
            if (mCurrentIcon instanceof ColorIcon) {
                ThemedDialog.buildBottomSheet(activity)
                        .setMode(BottomSheetBuilder.MODE_LIST)
                        .addTitleItem(R.string.bottom_sheet_icon_picker_title)
                        .addItem(1, R.string.bottom_sheet_icon_picker_action_select_icon, R.drawable.ic_add_24dp)
                        .addItem(2, R.string.bottom_sheet_icon_picker_action_change_bg_color, R.drawable.ic_format_color_fill_black_24dp)
                        .setItemClickListener(new BottomSheetItemClickListener() {

                            @Override
                            public void onBottomSheetItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case 1:
                                        startIconPickerActivity();
                                        break;
                                    case 2:
                                        openColorPicker();
                                        break;
                                }
                            }

                        })
                        .createDialog()
                        .show();
            } else if (mCurrentIcon instanceof VectorIcon) {
                ThemedDialog.buildBottomSheet(activity)
                        .setMode(BottomSheetBuilder.MODE_LIST)
                        .addTitleItem(R.string.bottom_sheet_icon_picker_title)
                        .addItem(1, R.string.bottom_sheet_icon_picker_action_change_icon, R.drawable.ic_add_24dp)
                        .addItem(2, R.string.bottom_sheet_icon_picker_action_remove_icon, R.drawable.ic_format_color_fill_black_24dp)
                        .setItemClickListener(new BottomSheetItemClickListener() {

                            @Override
                            public void onBottomSheetItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case 1:
                                        startIconPickerActivity();
                                        break;
                                    case 2:
                                        restoreColorIcon();
                                        break;
                                }
                            }

                        })
                        .createDialog()
                        .show();
            }
        }
    }

    private void startIconPickerActivity() {
        Intent intent = new Intent(getActivity(), IconListActivity.class);
        startActivityForResult(intent, REQUEST_ICON_PICKER);
    }

    private void openColorPicker() {
        Activity activity = getActivity();
        if (activity != null) {
            ThemedDialog.buildColorChooserDialog(activity, R.string.dialog_color_picker_title_icon_picker_background_color)
                    .accentMode(true)
                    .preselect(Color.parseColor(mLastBackgroundColor))
                    .dynamicButtonColor(false)
                    .show(getChildFragmentManager());
        }
    }

    private void restoreColorIcon() {
        if (!(mCurrentIcon instanceof ColorIcon)) {
            String text = mBindEditText != null ? mBindEditText.getText().toString() : "?";
            text = text.isEmpty() ? "?" : text;
            mCurrentIcon = new ColorIcon(mLastBackgroundColor, text.substring(0, 1));
            fireCallbackSafely();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        mLastBackgroundColor = Utils.getHexColor(selectedColor);
        if (mCurrentIcon instanceof ColorIcon) {
            String text = getColorIconString(mBindEditText != null ? mBindEditText.getText().toString() : null);
            mCurrentIcon = new ColorIcon(mLastBackgroundColor, text);
            fireCallbackSafely();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        // do nothing here: if the user dismiss the picker, it means that he don't want to change
        // the color at the moment.
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ICON_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentIcon = intent.getParcelableExtra(IconListActivity.RESULT_ICON);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public interface Controller {

        void onIconChanged(String tag, Icon icon);
    }
}