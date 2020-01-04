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

package com.oriondev.moneywallet.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.IdRes;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by andrea on 03/02/18.
 */

public class Utils {

    private static final int[] PALETTE = new int[] {
            Color.rgb(204, 198, 24),
            Color.rgb(229, 163, 25),
            Color.rgb(232, 111, 40),
            Color.rgb(212, 75, 145),
            Color.rgb(117, 96, 165),
            Color.rgb(54, 142, 92),
            Color.rgb(129, 191, 22),
            Color.rgb(224, 184, 26),
            Color.rgb(229, 138, 24),
            Color.rgb(235, 89, 92),
            Color.rgb(167, 78, 160),
            Color.rgb(66, 117, 138),
            Color.rgb(85, 169, 48)
    };

    public static String getHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static boolean isColorLight(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return red * 0.299 + green * 0.587 + blue * 0.114 > 186;
    }

    public static int getBestColor(int color) {
        if (isColorLight(color)) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isAtLeastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void setBackgroundCompat(View view, Drawable drawable) {
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
        }
    }

    public static int getFileIcon(String extension) {
        if (extension != null) {
            switch (extension) {
                case BackupManager.BACKUP_EXTENSION_LEGACY:
                case BackupManager.BACKUP_EXTENSION_STANDARD:
                case BackupManager.BACKUP_EXTENSION_PROTECTED:
                    return R.drawable.ic_notification;
                case ".txt":
                case ".doc":
                case ".docx":
                    return R.drawable.ic_file_document_24dp;
                case ".png":
                case ".jpg":
                case ".jpeg":
                case ".gif":
                case ".bmp":
                    return R.drawable.ic_file_image_24dp;
                case ".mp4":
                    return R.drawable.ic_file_video_24dp;
                case ".pdf":
                    return R.drawable.ic_file_pdf_24dp;
            }
        }
        return R.drawable.ic_file_outline_24dp;
    }

    public static int getRandomMDColor(int index) {
        return PALETTE[index % PALETTE.length];
    }

    public static int getRandomMDColor() {
        return PALETTE[new Random().nextInt(PALETTE.length)];
    }

    public static ViewGroup findViewGroupByIds(Activity activity, @IdRes int... resIds) {
        View rootView = activity.getWindow().getDecorView();
        View contentView = rootView.findViewById(android.R.id.content);
        return findViewGroupByIds(contentView != null ? contentView : rootView, resIds);
    }

    public static ViewGroup findViewGroupByIds(View root, @IdRes int... resIds) {
        for (int redId : resIds) {
            ViewGroup view = root.findViewById(redId);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    public static ArrayList<IFile> wrapAsArrayList(List<IFile> list) {
        if (list instanceof ArrayList) {
            return (ArrayList<IFile>) list;
        } else {
            ArrayList<IFile> wrappedList = new ArrayList<>();
            wrappedList.addAll(list);
            return wrappedList;
        }
    }

}