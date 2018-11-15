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

import android.util.JsonReader;
import android.util.JsonToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by andrea on 27/10/18.
 */
/*package-local*/ class JSONDataStreamReader implements Closeable {

    private final JsonReader mReader;

    /*package-local*/ JSONDataStreamReader(InputStream inputStream) throws IOException {
        mReader = new JsonReader(new InputStreamReader(inputStream));
        mReader.beginObject();
    }

    /*package-local*/ String readName() throws IOException {
        return mReader.nextName();
    }

    /*package-local*/ void beginArray() throws IOException {
        mReader.beginArray();
    }

    /*package-local*/ void endArray() throws IOException {
        mReader.endArray();
    }

    /*package-local*/ boolean hasArrayAnotherObject() throws IOException {
        return mReader.peek() != JsonToken.END_ARRAY;
    }

    /*package-local*/ JSONObject readObject() throws IOException, JSONException {
        mReader.beginObject();
        JSONObject object = new JSONObject();
        while (mReader.peek() != JsonToken.END_OBJECT) {
            String name = mReader.nextName();
            switch (mReader.peek()) {
                case STRING:
                    object.put(name, mReader.nextString());
                    break;
                case NUMBER:
                    String value = mReader.nextString();
                    try {object.put(name, Long.parseLong(value));} catch (NumberFormatException ignore) {}
                    try {object.put(name, Double.parseDouble(value));} catch (NumberFormatException ignore) {}
                    break;
                case BOOLEAN:
                    object.put(name, mReader.nextBoolean());
                    break;
            }
        }
        mReader.endObject();
        return object;
    }

    @Override
    public void close() throws IOException {
        mReader.close();
    }
}