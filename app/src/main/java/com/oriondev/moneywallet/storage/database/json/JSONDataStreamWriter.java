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

package com.oriondev.moneywallet.storage.database.json;

import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

/**
 * Created by andrea on 28/10/18.
 */

/*package-local*/ class JSONDataStreamWriter implements Closeable {

    private final JsonWriter mWriter;

    /*package-local*/ JSONDataStreamWriter(OutputStream outputStream) throws IOException {
        mWriter = new JsonWriter(new OutputStreamWriter(outputStream));
        mWriter.beginObject();
    }

    /*package-local*/ void writeName(String name) throws IOException {
        mWriter.name(name);
    }

    /*package-local*/ void beginArray() throws IOException {
        mWriter.beginArray();
    }

    /*package-local*/ void endArray() throws IOException {
        mWriter.endArray();
    }

    /*package-local*/ void writeJSONObject(JSONObject object) throws IOException, JSONException {
        mWriter.beginObject();
        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
            String key = it.next();
            mWriter.name(key);
            Object value = object.get(key);
            if (value instanceof String) {
                mWriter.value((String) value);
            } else if (value instanceof Number) {
                mWriter.value((Number) value);
            } else if (value instanceof Boolean) {
                mWriter.value((Boolean) value);
            } else {
                mWriter.nullValue();
            }
        }
        mWriter.endObject();
    }

    @Override
    public void close() throws IOException {
        mWriter.endObject();
        mWriter.close();
    }
}