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

package com.oriondev.moneywallet.ui.adapter.recycler;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.view.CardButton;
import com.oriondev.moneywallet.ui.view.MaterialProgressBar;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 06/03/18.
 */
public class SavingCursorAdapter extends AbstractCursorAdapter<SavingCursorAdapter.ViewHolder> {

    private int mIndexId;
    private int mIndexIcon;
    private int mIndexDescription;
    private int mIndexComplete;
    private int mIndexStartMoney;
    private int mIndexEndMoney;
    private int mIndexCurrency;
    private int mIndexProgress;

    private MoneyFormatter mMoneyFormatter;

    private final ActionListener mActionListener;

    public SavingCursorAdapter(ActionListener actionListener) {
        super(null, Contract.Saving.ID);
        mActionListener = actionListener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexId = cursor.getColumnIndex(Contract.Saving.ID);
        mIndexIcon = cursor.getColumnIndex(Contract.Saving.ICON);
        mIndexDescription = cursor.getColumnIndex(Contract.Saving.DESCRIPTION);
        mIndexComplete = cursor.getColumnIndex(Contract.Saving.COMPLETE);
        mIndexStartMoney = cursor.getColumnIndex(Contract.Saving.START_MONEY);
        mIndexEndMoney = cursor.getColumnIndex(Contract.Saving.END_MONEY);
        mIndexCurrency = cursor.getColumnIndex(Contract.Saving.WALLET_CURRENCY);
        mIndexProgress = cursor.getColumnIndex(Contract.Saving.PROGRESS);
    }

    @Override
    public void onBindViewHolder(SavingCursorAdapter.ViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mDescriptionTextView.setText(cursor.getString(mIndexDescription));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexCurrency));
        long target = cursor.getLong(mIndexEndMoney);
        mMoneyFormatter.applyNotTinted(holder.mMoneyTextView, currency, target);
        if (cursor.getInt(mIndexComplete) == 0) {
            holder.mProgressLayout.setVisibility(View.VISIBLE);
            holder.mButtonLayoutFix.setVisibility(View.GONE);
            holder.mButtonLayout.setVisibility(View.VISIBLE);
            long startMoney = cursor.getLong(mIndexStartMoney);
            long progress = cursor.getLong(mIndexProgress);
            long current = startMoney + progress;
            long needed = Math.max(target - current, 0L);
            holder.mProgressBar.setMaxValue(target);
            holder.mProgressBar.setProgressValue(current);
            mMoneyFormatter.applyNotTinted(holder.mCurrentMoneyTextView, currency, current);
            mMoneyFormatter.applyNotTinted(holder.mNeededMoneyTextView, currency, needed);
            holder.mWithdrawEverythingButton.setVisibility(needed == 0 ? View.VISIBLE : View.GONE);
            holder.mWithdrawButton.setVisibility(needed == 0 ? View.GONE : View.VISIBLE);
            holder.mDepositButton.setVisibility(needed == 0 ? View.GONE : View.VISIBLE);
        } else {
            holder.mProgressLayout.setVisibility(View.GONE);
            holder.mButtonLayoutFix.setVisibility(View.VISIBLE);
            holder.mButtonLayout.setVisibility(View.GONE);
            holder.mProgressBar.setMaxValue(target);
            holder.mProgressBar.setProgressValue(target);
        }
    }

    @Override
    public SavingCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_saving_item, parent, false);
        return new ViewHolder(itemView);
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mDescriptionTextView;
        private TextView mMoneyTextView;
        private MaterialProgressBar mProgressBar;
        private View mProgressLayout;
        private TextView mCurrentMoneyTextView;
        private TextView mNeededMoneyTextView;
        private View mButtonLayoutFix;
        private ViewGroup mButtonLayout;
        private CardButton mWithdrawEverythingButton;
        private CardButton mWithdrawButton;
        private CardButton mDepositButton;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mDescriptionTextView = itemView.findViewById(R.id.primary_text_view);
            mMoneyTextView = itemView.findViewById(R.id.money_text_view);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mProgressLayout = itemView.findViewById(R.id.progress_layout);
            mCurrentMoneyTextView = itemView.findViewById(R.id.current_body_text_view);
            mNeededMoneyTextView = itemView.findViewById(R.id.needed_body_text_view);
            mButtonLayoutFix = itemView.findViewById(R.id.button_bar_empty_fix);
            mButtonLayout = itemView.findViewById(R.id.button_bar_layout);
            mWithdrawEverythingButton = itemView.findViewById(R.id.withdraw_everything_button);
            mWithdrawButton = itemView.findViewById(R.id.withdraw_button);
            mDepositButton = itemView.findViewById(R.id.deposit_button);
            itemView.setOnClickListener(this);
            mWithdrawEverythingButton.setOnClickListener(this);
            mWithdrawButton.setOnClickListener(this);
            mDepositButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mActionListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    long savingId = cursor.getLong(mIndexId);
                    if (v == itemView) {
                        mActionListener.onSavingClick(savingId);
                    } else if (v == mWithdrawEverythingButton) {
                        mActionListener.onWithdrawEverything(savingId);
                    } else if (v == mWithdrawButton) {
                        mActionListener.onWithdraw(savingId);
                    } else if (v == mDepositButton) {
                        mActionListener.onDeposit(savingId);
                    }
                }
            }
        }
    }

    public interface ActionListener {

        void onSavingClick(long id);

        void onWithdrawEverything(long id);

        void onWithdraw(long id);

        void onDeposit(long id);
    }
}