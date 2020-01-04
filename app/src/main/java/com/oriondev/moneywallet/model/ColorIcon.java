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

package com.oriondev.moneywallet.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import android.widget.ImageView;

import com.oriondev.moneywallet.ui.drawable.TextDrawable;
import com.oriondev.moneywallet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrea on 23/01/18.
 */
public class ColorIcon extends Icon {

    private static final String COLOR = "color";
    private static final String NAME = "name";

    private final String mColor;
    private final String mName;

    public ColorIcon(JSONObject jsonObject) throws JSONException {
        mColor = jsonObject.getString(COLOR);
        mName = jsonObject.optString(NAME);
    }

    public ColorIcon(int color, String name) {
        mColor = Utils.getHexColor(color);
        mName = name;
    }

    public ColorIcon(String color, String name) {
        mColor = color;
        mName = name;
    }

    public ColorIcon(ColorIcon icon, String name) {
        mColor = icon.mColor;
        mName = name;
    }

    @SuppressWarnings("WeakerAccess")
    protected ColorIcon(Parcel source) {
        mColor = source.readString();
        mName = source.readString();
    }

    @Override
    public Type getType() {
        return Type.COLOR;
    }

    @Override
    protected void writeJSON(JSONObject jsonObject) throws JSONException {
        jsonObject.put(COLOR, mColor);
        jsonObject.put(NAME, mName);
    }

    @Override
    public boolean apply(ImageView imageView) {
        Drawable drawable = getDrawable();
        imageView.setImageDrawable(drawable);
        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mColor);
        dest.writeString(mName);
    }

    @ColorInt
    public int getColor() {
        return Color.parseColor(mColor);
    }

    @Override
    public Drawable getDrawable(Context context) {
        return getDrawable();
    }

    public Drawable getDrawable() {
        int backgroundColor = getColor();
        return TextDrawable.builder()
                .beginConfig()
                    .width(60)
                    .height(60)
                    .textColor(Utils.getBestColor(backgroundColor))
                .endConfig()
                .buildRound(mName, getColor());
    }

    public static final Parcelable.Creator<ColorIcon> CREATOR = new Parcelable.Creator<ColorIcon>() {

        @Override
        public ColorIcon createFromParcel(Parcel source) {
            return new ColorIcon(source);
        }

        @Override
        public ColorIcon[] newArray(int size) {
            return new ColorIcon[size];
        }
    };
}