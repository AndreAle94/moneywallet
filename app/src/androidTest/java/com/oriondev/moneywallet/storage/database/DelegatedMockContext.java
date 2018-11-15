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

package com.oriondev.moneywallet.storage.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.test.mock.MockContext;

import java.io.File;

/**
 * Created by andrea on 28/08/18.
 */

public class DelegatedMockContext extends MockContext {

    private Context mDelegatedContext;

    public DelegatedMockContext(Context context) {
        mDelegatedContext = context;
    }

    @Override
    public AssetManager getAssets() {
        return mDelegatedContext.getAssets();
    }

    @Override
    public Resources getResources() {
        return mDelegatedContext.getResources();
    }

    @Override
    public File getDir(String name, int mode) {
        // name the directory so the directory will be separated from
        // one created through the regular Context
        return mDelegatedContext.getDir("mock_context_" + name, mode);
    }

    @Override
    public Context getApplicationContext() {
        return DelegatedMockContext.this;
    }
}