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

package com.oriondev.moneywallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.utils.EquationSolver;

/**
 * Created by andre on 23/03/2018.
 */
public class CalculatorActivity extends SinglePanelActivity implements View.OnClickListener, EquationSolver.Controller {

    public static final String ACTIVITY_MODE = "CalculatorActivity::Parameters::ActivityMode";
    public static final String CURRENCY = "CalculatorActivity::Parameters::Currency";
    public static final String MONEY = "CalculatorActivity::Parameters::Money";

    public static final int MODE_CALCULATOR = 0;
    public static final int MODE_KEYPAD = 1;

    private static final String OP_000 = "000";
    private static final String OP_0 = "0";
    private static final String OP_1 = "1";
    private static final String OP_2 = "2";
    private static final String OP_3 = "3";
    private static final String OP_4 = "4";
    private static final String OP_5 = "5";
    private static final String OP_6 = "6";
    private static final String OP_7 = "7";
    private static final String OP_8 = "8";
    private static final String OP_9 = "9";
    private static final String OP_POINT = ".";
    private static final String OP_CLEAR = "C";
    private static final String OP_CANCEL = "B";
    private static final String OP_ADDITION = "A";
    private static final String OP_SUBTRACTION = "S";
    private static final String OP_MULTIPLICATION = "M";
    private static final String OP_DIVISION = "D";
    private static final String OP_EXECUTE = "E";

    private TextView mDisplayTextView;
    private EquationSolver mSolver;

    private boolean mKeypadMode;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_calculator, parent, true);
        mDisplayTextView = view.findViewById(R.id.display_text_view);
        registerListener(view.findViewById(R.id.keyboard_000_button), OP_000);
        registerListener(view.findViewById(R.id.keyboard_0_button), OP_0);
        registerListener(view.findViewById(R.id.keyboard_1_button), OP_1);
        registerListener(view.findViewById(R.id.keyboard_2_button), OP_2);
        registerListener(view.findViewById(R.id.keyboard_3_button), OP_3);
        registerListener(view.findViewById(R.id.keyboard_4_button), OP_4);
        registerListener(view.findViewById(R.id.keyboard_5_button), OP_5);
        registerListener(view.findViewById(R.id.keyboard_6_button), OP_6);
        registerListener(view.findViewById(R.id.keyboard_7_button), OP_7);
        registerListener(view.findViewById(R.id.keyboard_8_button), OP_8);
        registerListener(view.findViewById(R.id.keyboard_9_button), OP_9);
        registerListener(view.findViewById(R.id.keyboard_clear_button), OP_CLEAR);
        registerListener(view.findViewById(R.id.keyboard_cancel_button), OP_CANCEL);
        registerListener(view.findViewById(R.id.keyboard_point_button), OP_POINT);
        registerListener(view.findViewById(R.id.keyboard_division_button), OP_DIVISION);
        registerListener(view.findViewById(R.id.keyboard_multiplication_button), OP_MULTIPLICATION);
        registerListener(view.findViewById(R.id.keyboard_addition_button), OP_ADDITION);
        registerListener(view.findViewById(R.id.keyboard_subtraction_button), OP_SUBTRACTION);
        registerListener(view.findViewById(R.id.keyboard_action_button), OP_EXECUTE);
        mSolver = new EquationSolver(savedInstanceState, this);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mKeypadMode = intent.getIntExtra(ACTIVITY_MODE, MODE_CALCULATOR) == MODE_KEYPAD;
        if (savedInstanceState == null) {
            CurrencyUnit currency = intent.getParcelableExtra(CURRENCY);
            long money = intent.getLongExtra(MONEY, 0L);
            mSolver.setValue(currency, money);
        }
    }

    private void registerListener(View button, String operation) {
        button.setTag(operation);
        button.setOnClickListener(this);
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_calculator;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public void onClick(View button) {
        String operation = (String) button.getTag();
        switch (operation) {
            case OP_CLEAR:
                mSolver.clear();
                break;
            case OP_CANCEL:
                mSolver.cancel();
                break;
            case OP_EXECUTE:
                execute();
                break;
            case OP_ADDITION:
                mSolver.appendOperation(EquationSolver.Operation.ADDITION);
                break;
            case OP_SUBTRACTION:
                mSolver.appendOperation(EquationSolver.Operation.SUBTRACTION);
                break;
            case OP_MULTIPLICATION:
                mSolver.appendOperation(EquationSolver.Operation.MULTIPLICATION);
                break;
            case OP_DIVISION:
                mSolver.appendOperation(EquationSolver.Operation.DIVISION);
                break;
            case OP_POINT:
                mSolver.appendPoint();
                break;
            default:
                mSolver.appendNumber(operation);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSolver.onSaveInstanceState(outState);
    }

    private void execute() {
        if (mSolver.isPendingOperation()) {
            if (!mSolver.execute(true)) {
                // TODO: show error!
            }
        } else if (mKeypadMode) {
            Intent intent = new Intent();
            intent.putExtra(MONEY, mSolver.getResult());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onUpdateDisplay(String text) {
        mDisplayTextView.setText(text);
    }
}