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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by andrea on 26/01/18.
 */
public class AdvancedRecyclerView extends SwipeRefreshLayout {

    private RecyclerView mRecyclerView;
    private ProgressWheel mProgressWheel;
    private TextView mEmptyTextView;

    private int mEmptyTextRes;
    private int mErrorTextRes;
    private State mCurrentState;

    public AdvancedRecyclerView(Context context) {
        super(context);
        initialize(context);
    }

    public AdvancedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(@NonNull Context context) {
        inflate(context, R.layout.view_advanced_recycler, this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mProgressWheel = findViewById(R.id.progress_wheel);
        mEmptyTextView = findViewById(R.id.empty_text_view);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public ProgressWheel getProgressWheel() {
        return mProgressWheel;
    }

    public TextView getTextView() {
        return mEmptyTextView;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    public void setEmptyText(@StringRes int resId) {
        mEmptyTextRes = resId;
    }

    public void setErrorText(@StringRes int resId) {
        mErrorTextRes = resId;
    }

    public void setState(State state) {
        if (mCurrentState != state) {
            switch (state) {
                case EMPTY:
                    mRecyclerView.setVisibility(View.GONE);
                    mProgressWheel.setVisibility(View.GONE);
                    mEmptyTextView.setText(mEmptyTextRes);
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    setRefreshing(false);
                    break;
                case LOADING:
                    mRecyclerView.setVisibility(View.GONE);
                    mProgressWheel.setVisibility(View.VISIBLE);
                    mEmptyTextView.setVisibility(View.GONE);
                    setRefreshing(false);
                    break;
                case REFRESHING:
                    mRecyclerView.setVisibility(View.GONE);
                    mProgressWheel.setVisibility(View.GONE);
                    mEmptyTextView.setVisibility(View.GONE);
                    setRefreshing(true);
                    break;
                case READY:
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mProgressWheel.setVisibility(View.GONE);
                    mEmptyTextView.setVisibility(View.GONE);
                    setRefreshing(false);
                    break;
                case ERROR:
                    mRecyclerView.setVisibility(View.GONE);
                    mProgressWheel.setVisibility(View.GONE);
                    mEmptyTextView.setText(mErrorTextRes);
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    setRefreshing(false);
                    break;
            }
        }
    }

    public enum State {
        EMPTY,
        LOADING,
        REFRESHING,
        READY,
        ERROR
    }
}