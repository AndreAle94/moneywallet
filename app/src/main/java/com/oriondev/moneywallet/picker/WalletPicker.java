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
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.ui.fragment.dialog.WalletPickerDialog;

/**
 * Created by andrea on 09/03/18.
 */
public class WalletPicker extends Fragment implements WalletPickerDialog.Callback {

    private static final String SS_SINGLE_WALLET = "WalletPicker::SavedState::SingleWallet";
    private static final String SS_CURRENT_WALLET = "WalletPicker::SavedState::CurrentWallet";

    private static final String ARG_SINGLE_WALLET = "WalletPicker::Arguments::SingleWallet";
    private static final String ARG_DEFAULT_WALLET = "WalletPicker::Arguments::DefaultWallet";

    private SingleWalletController mSingleWalletController;
    private MultiWalletController mMultiWalletController;

    private boolean mSingleWallet;
    private Wallet mCurrentWallet;
    private Wallet[] mCurrentWallets;

    private WalletPickerDialog mWalletPickerDialog;

    public static WalletPicker createPicker(FragmentManager fragmentManager, String tag, Wallet defaultWallet) {
        WalletPicker walletPicker = (WalletPicker) fragmentManager.findFragmentByTag(tag);
        if (walletPicker == null) {
            walletPicker = new WalletPicker();
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARG_SINGLE_WALLET, true);
            arguments.putParcelable(ARG_DEFAULT_WALLET, defaultWallet);
            walletPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(walletPicker, tag).commit();
        }
        return walletPicker;
    }

    public static WalletPicker createPicker(FragmentManager fragmentManager, String tag, Wallet[] defaultWallets) {
        WalletPicker walletPicker = (WalletPicker) fragmentManager.findFragmentByTag(tag);
        if (walletPicker == null) {
            walletPicker = new WalletPicker();
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARG_SINGLE_WALLET, false);
            arguments.putParcelableArray(ARG_DEFAULT_WALLET, defaultWallets);
            walletPicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(walletPicker, tag).commit();
        }
        return walletPicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SingleWalletController) {
            mSingleWalletController = (SingleWalletController) context;
        }
        if (context instanceof MultiWalletController) {
            mMultiWalletController = (MultiWalletController) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSingleWallet = savedInstanceState.getBoolean(SS_SINGLE_WALLET);
            if (mSingleWallet) {
                mCurrentWallet = savedInstanceState.getParcelable(SS_CURRENT_WALLET);
            } else {
                // this is required because some devices have issues
                Parcelable[] parcelables = savedInstanceState.getParcelableArray(SS_CURRENT_WALLET);
                if (parcelables != null) {
                    mCurrentWallets = new Wallet[parcelables.length];
                    for (int i = 0; i < parcelables.length; i++) {
                        mCurrentWallets[i] = (Wallet) parcelables[i];
                    }
                } else {
                    mCurrentWallets = new Wallet[0];
                }
                mCurrentWallets = (Wallet[]) savedInstanceState.getParcelableArray(SS_CURRENT_WALLET);
            }
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mSingleWallet = arguments.getBoolean(ARG_SINGLE_WALLET);
                if (mSingleWallet) {
                    mCurrentWallet = arguments.getParcelable(ARG_DEFAULT_WALLET);
                } else {
                    mCurrentWallets = (Wallet[]) arguments.getParcelableArray(ARG_DEFAULT_WALLET);
                }
            } else {
                mSingleWallet = true;
                mCurrentWallets = null;
            }
        }
        mWalletPickerDialog = (WalletPickerDialog) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mWalletPickerDialog == null) {
            mWalletPickerDialog = WalletPickerDialog.newInstance();
        }
        mWalletPickerDialog.setCallback(this);
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
        if (mSingleWallet) {
            if (mSingleWalletController != null) {
                mSingleWalletController.onWalletChanged(getTag(), mCurrentWallet);
            }
        } else {
            if (mMultiWalletController != null) {
                mMultiWalletController.onWalletListChanged(getTag(), mCurrentWallets);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SS_SINGLE_WALLET, mSingleWallet);
        if (mSingleWallet) {
            outState.putParcelable(SS_CURRENT_WALLET, mCurrentWallet);
        } else {
            outState.putParcelableArray(SS_CURRENT_WALLET, mCurrentWallets);
        }
    }

    public boolean isSelected() {
        return mSingleWallet ? mCurrentWallet != null : (mCurrentWallets != null && mCurrentWallets.length > 0);
    }

    public Wallet getCurrentWallet() {
        return mCurrentWallet;
    }

    public Wallet[] getCurrentWallets() {
        return mCurrentWallets;
    }

    public void showSingleWalletPicker() {
        mWalletPickerDialog.showSinglePicker(getChildFragmentManager(), getDialogTag(), mCurrentWallet);
    }

    public void showMultiWalletPicker() {
        mWalletPickerDialog.showMultiPicker(getChildFragmentManager(), getDialogTag(), mCurrentWallets);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSingleWalletController = null;
        mMultiWalletController = null;
    }

    @Override
    public void onWalletSelected(Wallet wallet) {
        mCurrentWallet = wallet;
        fireCallbackSafely();
    }

    @Override
    public void onWalletsSelected(Wallet[] wallets) {
        mCurrentWallets = wallets;
        fireCallbackSafely();
    }

    public interface SingleWalletController {

        void onWalletChanged(String tag, Wallet wallet);
    }

    public interface MultiWalletController {

        void onWalletListChanged(String tag, Wallet[] wallets);
    }
}