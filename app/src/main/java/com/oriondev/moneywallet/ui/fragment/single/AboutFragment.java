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

package com.oriondev.moneywallet.ui.fragment.single;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.app.FragmentManager;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.holders.MaterialAboutItemViewHolder;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.DefaultViewTypeManager;
import com.danielstone.materialaboutlibrary.util.ViewTypeManager;
import com.oriondev.moneywallet.BuildConfig;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.License;
import com.oriondev.moneywallet.ui.fragment.dialog.ChangeLogDialog;
import com.oriondev.moneywallet.ui.fragment.dialog.LicenseDialog;
import com.oriondev.moneywallet.ui.view.theme.ITheme;
import com.oriondev.moneywallet.ui.view.theme.ThemeEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by andrea on 31/03/18.
 */
public class AboutFragment extends MaterialAboutFragment {

    private static final String TAG_CHANGE_LOG = "AboutFragment::Tag::ChangeLogDialog";
    private static final String TAG_LICENSE = "AboutFragment::Tag::LicenseDialog";

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        try {
            StringBuilder jsonBuilder = new StringBuilder();
            InputStream inputStream = context.getAssets().open("resources/about.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONObject aboutObj = new JSONObject(jsonBuilder.toString());
            return generateAboutScreen(context, aboutObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MaterialAboutList generateAboutScreen(final Context context, final JSONObject aboutObj) throws JSONException {
        MaterialAboutList.Builder screenBuilder = new MaterialAboutList.Builder();
        // the main card contains static information about the project
        screenBuilder.addCard(createThemedAboutCard()
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_restore_black_24dp)
                        .text(R.string.about_hint_version)
                        .subText(BuildConfig.VERSION_NAME)
                        .build()
                )
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_track_changes_black_24dp)
                        .text(R.string.about_hint_changelog)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                FragmentManager fragmentManager = getChildFragmentManager();
                                ChangeLogDialog.showSafely(fragmentManager, TAG_CHANGE_LOG);
                            }

                        })
                        .build()
                )
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_favorite_border_black_24dp)
                        .text(R.string.about_hint_support)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                try {
                                    JSONObject repositoryObj = aboutObj.getJSONObject("repository");
                                    Uri uri = Uri.parse(repositoryObj.getString("donation_url"));
                                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                } catch (ActivityNotFoundException | JSONException ignore) {}
                            }

                        })
                        .build()
                )
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_star_black_24dp)
                        .text(R.string.about_hint_rate_app)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                                }
                            }

                        })
                        .build()
                )
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_library_books_black_24dp)
                        .text(R.string.about_hint_open_source_libraries)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                FragmentManager fragmentManager = getChildFragmentManager();
                                LicenseDialog.showSafely(fragmentManager, TAG_LICENSE, new LicenseDialog.Callback() {

                                    @Override
                                    public void onLicenseClick(License license) {
                                        try {
                                            Uri uri = Uri.parse(license.getUrl());
                                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                        } catch (ActivityNotFoundException ignore) {}
                                    }

                                });
                            }

                        })
                        .build()
                )
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_bug_report_black_24dp)
                        .text(R.string.about_hint_report_bug)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                try {
                                    JSONObject repositoryObj = aboutObj.getJSONObject("repository");
                                    Uri uri = Uri.parse(repositoryObj.getString("issue_url"));
                                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                } catch (ActivityNotFoundException | JSONException ignore) {}
                            }

                        })
                        .build()
                )
                .build());
        // now we have to iterate the other info object and build all the other cards
        JSONArray otherInfo = aboutObj.getJSONArray("other_info");
        if (otherInfo != null) {
            for (int i = 0; i < otherInfo.length(); i++) {
                MaterialAboutCard.Builder cardBuilder = createThemedAboutCard();
                JSONObject infoObj = otherInfo.getJSONObject(i);
                String nameRes = infoObj.getString("name_resource");
                cardBuilder.title(getStringByResource(context, nameRes));
                JSONArray itemArray = infoObj.getJSONArray("items");
                for (int j = 0; j < itemArray.length(); j++) {
                    MaterialAboutActionItem.Builder itemBuilder = new MaterialAboutActionItem.Builder();
                    JSONObject itemObj = itemArray.getJSONObject(j);
                    if (itemObj.has("icon")) {
                        String iconRes = itemObj.getString("icon");
                        itemBuilder.icon(getIconResourceId(context, iconRes));
                    }
                    if (itemObj.has("title")) {
                        String text = itemObj.getString("title");
                        itemBuilder.text(text);
                    } else if (itemObj.has("title_resource")) {
                        String textRes = itemObj.getString("title_resource");
                        itemBuilder.text(getStringByResource(context, textRes));
                    }
                    if (itemObj.has("subtitle")) {
                        String subtext = itemObj.getString("subtitle");
                        itemBuilder.subText(subtext);
                    } else if (itemObj.has("subtitle_resource")) {
                        String subtextRes = itemObj.getString("subtitle_resource");
                        itemBuilder.subText(getStringByResource(context, subtextRes));
                    }
                    if (itemObj.has("link")) {
                        final String url = itemObj.getString("link");
                        itemBuilder.setOnClickAction(new MaterialAboutItemOnClickAction() {

                            @Override
                            public void onClick() {
                                try {
                                    Uri uri = Uri.parse(url);
                                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                } catch (ActivityNotFoundException ignore) {}
                            }

                        });
                    }
                    cardBuilder.addItem(itemBuilder.build());
                }
                screenBuilder.addCard(cardBuilder.build());
            }
        }
        return screenBuilder.build();
    }

    private int getIconResourceId(Context context, String resource) {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier(resource, "drawable", packageName);
    }

    private String getStringByResource(Context context, String resource) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(resource, "string", packageName);
        return resId > 0 ? context.getString(resId) : null;
    }

    private MaterialAboutCard.Builder createThemedAboutCard() {
        ITheme theme = ThemeEngine.getTheme();
        return new MaterialAboutCard.Builder()
                .cardColor(theme.getColorCardBackground())
                .titleColor(theme.getTextColorPrimary());
    }

    /**
     * The library does not directly exposes any way to dynamically change the item colors.
     * This is a workaround and consists in a custom implementation of the object used by the
     * adapter to manage the layout of internal items.
     * @return a custom implementation of the ViewTypeManager that handle the theme application.
     */
    protected ViewTypeManager getViewTypeManager() {
        return new ThemedViewTypeManager();
    }

    /**
     * This class is a custom implementation of the DefaultViewTypeManager and let the ThemeEngine
     * to directly apply the theme to the view holder items.
     */
    private class ThemedViewTypeManager extends DefaultViewTypeManager {

        @Override
        public void setupItem(int itemType, MaterialAboutItemViewHolder holder, MaterialAboutItem item, Context context) {
            ITheme theme = ThemeEngine.getTheme();
            switch(itemType) {
                case 0:
                    MaterialAboutActionItem.setupItem((MaterialAboutActionItem.MaterialAboutActionItemViewHolder)holder, (MaterialAboutActionItem)item, context);
                    ((MaterialAboutActionItem.MaterialAboutActionItemViewHolder) holder).text.setTextColor(theme.getTextColorPrimary());
                    ((MaterialAboutActionItem.MaterialAboutActionItemViewHolder) holder).subText.setTextColor(theme.getTextColorSecondary());
                    ((MaterialAboutActionItem.MaterialAboutActionItemViewHolder) holder).icon.setColorFilter(theme.getIconColor(), PorterDuff.Mode.SRC_IN);
                    break;
                case 1:
                    MaterialAboutTitleItem.setupItem((MaterialAboutTitleItem.MaterialAboutTitleItemViewHolder)holder, (MaterialAboutTitleItem)item, context);
                    ((MaterialAboutTitleItem.MaterialAboutTitleItemViewHolder) holder).text.setTextColor(theme.getTextColorPrimary());
                    ((MaterialAboutTitleItem.MaterialAboutTitleItemViewHolder) holder).desc.setTextColor(theme.getTextColorSecondary());
                    ((MaterialAboutTitleItem.MaterialAboutTitleItemViewHolder) holder).icon.setColorFilter(theme.getIconColor(), PorterDuff.Mode.SRC_IN);
            }

        }
    }
}