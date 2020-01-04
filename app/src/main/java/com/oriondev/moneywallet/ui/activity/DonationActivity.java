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

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.background.JsonResourceLoader;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrea on 06/12/18.
 */
public class DonationActivity extends SinglePanelActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private static final int LOADER_ID = 24;

    private ScrollView mRootScrollView;
    private ImageView mDonationOptionOneImageView;
    private ImageView mDonationOptionTwoImageView;
    private ImageView mDonationOptionThreeImageView;
    private Button mDonationOptionOneButton;
    private Button mDonationOptionTwoButton;
    private Button mDonationOptionThreeButton;
    private View mShareActionLayout;
    private View mRateActionLayout;
    private View mTranslateActionLayout;
    private View mContributeActionLayout;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_support_developer, parent, true);
        mRootScrollView = view.findViewById(R.id.root_scroll_view);
        mDonationOptionOneImageView = view.findViewById(R.id.donation_option_one_image_view);
        mDonationOptionTwoImageView = view.findViewById(R.id.donation_option_two_image_view);
        mDonationOptionThreeImageView = view.findViewById(R.id.donation_option_three_image_view);
        mDonationOptionOneButton = view.findViewById(R.id.donation_option_one_button);
        mDonationOptionTwoButton = view.findViewById(R.id.donation_option_two_button);
        mDonationOptionThreeButton = view.findViewById(R.id.donation_option_three_button);
        mShareActionLayout = view.findViewById(R.id.action_share_layout);
        mRateActionLayout = view.findViewById(R.id.action_rate_layout);
        mTranslateActionLayout = view.findViewById(R.id.action_translate_layout);
        mContributeActionLayout = view.findViewById(R.id.action_contribute_layout);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        // start an async loader to read the json file
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_support_developer;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        // disable floating action button
        return false;
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        return new JsonResourceLoader(this, "donation.json");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        try {
            JSONObject monetary = data.getJSONObject("monetary");
            JSONObject nonMonetary = data.getJSONObject("non-monetary");
            // setup the monetary donation options
            JSONObject optionOne = monetary.getJSONObject("option_one");
            JSONObject optionTwo = monetary.getJSONObject("option_two");
            JSONObject optionThree = monetary.getJSONObject("option_three");
            // set icon to each donation option
            mDonationOptionOneImageView.setImageResource(getIconResourceId(this, optionOne.getString("icon")));
            mDonationOptionTwoImageView.setImageResource(getIconResourceId(this, optionTwo.getString("icon")));
            mDonationOptionThreeImageView.setImageResource(getIconResourceId(this, optionThree.getString("icon")));
            // set name to each donation option
            mDonationOptionOneButton.setText(optionOne.getString("name"));
            mDonationOptionTwoButton.setText(optionTwo.getString("name"));
            mDonationOptionThreeButton.setText(optionThree.getString("name"));
            // register listeners for each donation option
            registerCryptoAddress(mDonationOptionOneButton, optionOne.getString("crypto_scheme"), optionOne.getString("crypto_link"));
            registerPaypalAddress(mDonationOptionTwoButton, optionTwo.getString("url"));
            registerCryptoAddress(mDonationOptionThreeButton, optionThree.getString("crypto_scheme"), optionThree.getString("crypto_link"));
            // setup the non-monetary donation options
            registerShareAction(mShareActionLayout, nonMonetary.getString("share_link"));
            registerWebAction(mRateActionLayout, nonMonetary.getString("rate_link"));
            registerWebAction(mTranslateActionLayout, nonMonetary.getString("translate_link"));
            registerWebAction(mContributeActionLayout, nonMonetary.getString("contribute_link"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getIconResourceId(Context context, String resource) {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier(resource, "drawable", packageName);
    }

    private void registerCryptoAddress(Button button, final String scheme, final String address) {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(scheme + ":" + address);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignore) {
                    // no activity to handle this kind of uri: we can simply copy the
                    // address to the clipboard of the device and notify the user
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText(scheme, address);
                        clipboard.setPrimaryClip(clip);
                        // notify user
                        Snackbar.make(mRootScrollView,
                                    R.string.activity_donation_crypto_address_copied,
                                    Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            }

        });
    }

    private void registerPaypalAddress(Button button, final String url) {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    Uri uri = Uri.parse(url);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignore) {
                    // no activity found to open the url
                }
            }

        });
    }

    private void registerShareAction(View actionLayout, final String url) {
        actionLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.activity_donation_action_share_message, url));
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.activity_donation_action_share_using)));
            }

        });
    }

    private void registerWebAction(View actionLayout, final String url) {
        actionLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(url);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignore) {}
            }

        });
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {
        // do nothing
    }
}