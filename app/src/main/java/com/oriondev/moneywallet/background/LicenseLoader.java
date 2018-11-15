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

import com.oriondev.moneywallet.model.License;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 06/04/18.
 */
public class LicenseLoader extends AbstractGenericLoader<List<License>> {

    public LicenseLoader(Context context) {
        super(context);
    }

    @Override
    public List<License> loadInBackground() {
        List<License> licenses = new ArrayList<>();
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            InputStream inputStream = getContext().getAssets().open("resources/libraries.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONArray array = new JSONArray(jsonBuilder.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                licenses.add(new License(
                        item.getString("name"),
                        item.optString("author"),
                        item.optString("website"),
                        item.optString("version"),
                        item.optString("copyright"),
                        item.optString("license")
                ));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return licenses;
    }
}