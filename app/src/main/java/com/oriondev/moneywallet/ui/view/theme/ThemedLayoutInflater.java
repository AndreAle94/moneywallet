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

package com.oriondev.moneywallet.ui.view.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class is a wrapper implementation of the default android layout inflater and is
 * used to automatically apply the current theme during the layout inflation.
 * ThemeEngine will be responsible to reapply the theme dynamically if something changes
 * at runtime.
 */
public class ThemedLayoutInflater implements LayoutInflater.Factory2 {

    private final LayoutInflater.Factory2 mBaseFactory;

    public ThemedLayoutInflater(LayoutInflater.Factory2 baseFactory) {
        mBaseFactory = baseFactory;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (name.startsWith("com.oriondev.moneywallet.ui.view.theme.Themed")) {
            View view = inflateThemeView(name, context, attrs);
            ThemeEngine.applyTheme(view, false);
            return view;
        } else {
            return mBaseFactory.onCreateView(name, context, attrs);
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (name.startsWith("com.oriondev.moneywallet.ui.view.theme.Themed")) {
            View view = inflateThemeView(name, context, attrs);
            ThemeEngine.applyTheme(view, false);
            return view;
        } else {
            return mBaseFactory.onCreateView(parent, name, context, attrs);
        }
    }

    private View inflateThemeView(String name, Context context, AttributeSet attrs) {
        try {
            Class<?> clazz = Class.forName(name);
            Constructor<?> constructor = clazz.getConstructor(Context.class, AttributeSet.class);
            return (View) constructor.newInstance(context, attrs);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}