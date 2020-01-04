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
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrea on 23/01/18.
 */
public class VectorIcon extends Icon {

    private static final String RESOURCE = "resource";

    private final String mResourceName;

    public VectorIcon(JSONObject jsonObject) throws JSONException {
        mResourceName = jsonObject.getString(RESOURCE);
    }

    protected VectorIcon(Parcel source) {
        mResourceName = source.readString();
    }

    @Override
    public Type getType() {
        return Type.RESOURCE;
    }

    @Override
    protected void writeJSON(JSONObject jsonObject) throws JSONException {
        jsonObject.put(RESOURCE, mResourceName);
    }

    @Override
    public Drawable getDrawable(Context context) {
        int drawableId = getDrawableId(context, mResourceName);
        if (drawableId > 0) {
            return ContextCompat.getDrawable(context, drawableId);
        }
        return null;
    }

    @Override
    public boolean apply(ImageView imageView) {
        int resourceId = getResource(imageView.getContext());
        if (resourceId > 0) {
            Glide.with(imageView)
                    .load(resourceId)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(imageView);
            return true;
        } else {
            return false;
        }
    }

    @DrawableRes
    public int getResource(Context context) {
        return getDrawableId(context, mResourceName);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mResourceName);
    }

    public static final Parcelable.Creator<VectorIcon> CREATOR = new Parcelable.Creator<VectorIcon>() {

        @Override
        public VectorIcon createFromParcel(Parcel source) {
            return new VectorIcon(source);
        }

        @Override
        public VectorIcon[] newArray(int size) {
            return new VectorIcon[size];
        }
    };
}