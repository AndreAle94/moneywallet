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

/**
 * Created by andrea on 06/04/18.
 */
public class ChangeLog {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String versionName;
    private String versionDate;
    private String changeText;
    private int changeType;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(String versionDate) {
        this.versionDate = versionDate;
    }

    public String getChangeText() {
        return changeText;
    }

    public void setChangeText(String changeText) {
        this.changeText = changeText;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    // BUILDER CLASS

    public static class HeaderBuilder {

        private String versionName;
        private String versionDate;

        public HeaderBuilder versionName(String versionName) {
            this.versionName = versionName;
            return this;
        }

        public HeaderBuilder versionDate(String versionDate) {
            this.versionDate = versionDate;
            return this;
        }

        public ChangeLog build() {
            ChangeLog changeLog = new ChangeLog();
            changeLog.setType(TYPE_HEADER);
            changeLog.setVersionName(versionName);
            changeLog.setVersionDate(versionDate);
            return changeLog;
        }
    }

    public static class ItemBuilder {

        private String changeText;

        public ItemBuilder changeText(String changeText) {
            this.changeText = changeText;
            return this;
        }

        public ChangeLog build() {
            ChangeLog changeLog = new ChangeLog();
            changeLog.setType(TYPE_ITEM);
            changeLog.setChangeText(changeText);
            return changeLog;
        }
    }
}