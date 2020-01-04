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

package com.oriondev.moneywallet.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.oriondev.moneywallet.R;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

/**
 * Created by andrea on 07/11/18.
 */
public class RecurrenceView extends LinearLayout {

    private MaterialSpinner mTypeSpinner;
    private MaterialSpinner mStartDateSpinner;

    public RecurrenceView(Context context) {
        super(context);
        initialize(context);
    }

    public RecurrenceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public RecurrenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecurrenceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context) {
        View view = inflate(context, R.layout.dialog_recurrence_setting_picker, this);
        mTypeSpinner = view.findViewById(R.id.type_spinner);
        mStartDateSpinner = view.findViewById(R.id.start_date_spinner);



        mTypeSpinner.setItems(
                "Repeat daily",
                "Repeat weekly",
                "Repeat monthly",
                "Repeat yearly"
        );
        mTypeSpinner.setPadding(0, mTypeSpinner.getPaddingTop(), 0, mTypeSpinner.getPaddingBottom());
        mStartDateSpinner.setPadding(0, mStartDateSpinner.getPaddingTop(), 0, mStartDateSpinner.getPaddingBottom());
        mStartDateSpinner.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO: open picker
                mStartDateSpinner.collapse();
            }

        });
        mStartDateSpinner.setText(
                "From: Today"
        );
    }

    public RecurrenceRule getRecurrenceRule() {
        RecurrenceRule recurrenceRule = new RecurrenceRule(Freq.MONTHLY);
        // TODO: build the recurrence rule using user settings
        return recurrenceRule;
    }
}