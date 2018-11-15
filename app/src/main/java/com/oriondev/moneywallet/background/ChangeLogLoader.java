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

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.ChangeLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 06/04/18.
 */
public class ChangeLogLoader extends AbstractGenericLoader<List<ChangeLog>> {

    private static final String VERSION = "version";
    private static final String CHANGE_TEXT = "change";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_DATE = "versionDate";

    public ChangeLogLoader(Context context) {
        super(context);
    }

    @Override
    public List<ChangeLog> loadInBackground() {
        List<ChangeLog> changeLogs = new ArrayList<>();
        XmlPullParser parser = getContext().getResources().getXml(R.xml.changelog);
        try {
            int eventType = parser.getEventType();
            String buffer = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(VERSION)) {
                            ChangeLog changeLog = new ChangeLog.HeaderBuilder()
                                    .versionName(parser.getAttributeValue(null, VERSION_NAME))
                                    .versionDate(parser.getAttributeValue(null, VERSION_DATE))
                                    .build();
                            changeLogs.add(changeLog);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        buffer = parser.getText();
                        if (buffer != null) {
                            buffer = buffer.replaceAll("\\[", "<").replaceAll("\\]", ">");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equals(CHANGE_TEXT)) {
                            ChangeLog changeLog = new ChangeLog.ItemBuilder()
                                    .changeText(buffer)
                                    .build();
                            changeLogs.add(changeLog);
                            buffer = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // Log.e("ChangeLogLoader", "Exception while parsing change log xml file", e);
        } catch (IOException e) {
            // Log.e("ChangeLogLoader", "I/O Exception while parsing change log xml file", e);
        }
        return changeLogs;
    }
}