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

package com.oriondev.moneywallet.background;

import android.content.Context;

import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.IconGroup;
import com.oriondev.moneywallet.model.VectorIcon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is used by the IconListActivity to fetch all the available icons and organize
 * them inside a list of IconGroup items. The list of available icons is statically described
 * by the icons_old.json file inside the assets/resources directory of the project.
 * When a new buildMaterialDialog is performed, the developer should check that all the icons inside this file
 * are available inside the drawable directory of the project.
 *
 * NB: This project is open source but the icon pack used is not free to use and the license
 * of the author explicitly says that those icons cannot be released in public. For this reason
 * the public repository has very few icons (only the ones that are license-free).
 */
public class IconGroupLoader extends AbstractGenericLoader<List<IconGroup>> {

    public IconGroupLoader(Context context) {
        super(context);
    }

    @Override
    public List<IconGroup> loadInBackground() {
        List<IconGroup> iconGroups = new ArrayList<>();
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            InputStream inputStream = getContext().getAssets().open("resources/icons.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONObject iconsObj = new JSONObject(jsonBuilder.toString());
            JSONArray categories = iconsObj.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                iconGroups.add(parseIconGroup(categories.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Collections.sort(iconGroups, new AlphabeticIconComparator());
        return iconGroups;
    }

    private IconGroup parseIconGroup(JSONObject category) throws JSONException {
        String groupName = getStringByName(category.getString("name"));
        List<Icon> icons = parseIconList(category.getJSONArray("icons"));
        return new IconGroup(groupName, icons);
    }

    private List<Icon> parseIconList(JSONArray icons) throws JSONException {
        List<Icon> iconList = new ArrayList<>();
        for (int i = 0; i < icons.length(); i++) {
            iconList.add(new VectorIcon(icons.getJSONObject(i)));
        }
        return iconList;
    }

    private String getStringByName(String name) {
        Context context = getContext();
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(name, "string", packageName);
        return context.getString(resId);
    }

    private class AlphabeticIconComparator implements Comparator<IconGroup> {

        @Override
        public int compare(IconGroup icon1, IconGroup icon2) {
            return icon1.getGroupName().compareTo(icon2.getGroupName());
        }
    }
}