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

import android.content.Context;
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
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.wrapper.DebtHeaderCursor;
import com.oriondev.moneywallet.ui.view.CardButton;
import com.oriondev.moneywallet.ui.view.MaterialProgressBar;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 05/03/18.
 */
public class DebtCursorAdapter extends AbstractCursorAdapter {

    private int mIndexType;
    private int mIndexHeaderType;
    private int mIndexHeaderMoney;
    private int mIndexId;
    private int mIndexDebtType;
    private int mIndexIcon;
    private int mIndexDescription;
    private int mIndexMoney;
    private int mIndexProgress;
    private int mIndexCurrency;
    private int mIndexExpirationDate;
    private int mIndexArchived;
    private int mIndexPlaceId;
    private int mIndexPlaceName;

    private MoneyFormatter mMoneyFormatter;

    private final ActionListener mActionListener;

    public DebtCursorAdapter(ActionListener actionListener) {
        super(null, Contract.Debt.ID);
        mActionListener = actionListener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexType = cursor.getColumnIndex(DebtHeaderCursor.COLUMN_ITEM_TYPE);
        mIndexHeaderType = cursor.getColumnIndex(DebtHeaderCursor.COLUMN_HEADER_TYPE);
        mIndexHeaderMoney = cursor.getColumnIndex(DebtHeaderCursor.COLUMN_HEADER_MONEY);
        mIndexId = cursor.getColumnIndex(Contract.Debt.ID);
        mIndexDebtType = cursor.getColumnIndex(Contract.Debt.TYPE);
        mIndexIcon = cursor.getColumnIndex(Contract.Debt.ICON);
        mIndexDescription = cursor.getColumnIndex(Contract.Debt.DESCRIPTION);
        mIndexMoney = cursor.getColumnIndex(Contract.Debt.MONEY);
        mIndexProgress = cursor.getColumnIndex(Contract.Debt.PROGRESS);
        mIndexCurrency = cursor.getColumnIndex(Contract.Debt.WALLET_CURRENCY);
        mIndexExpirationDate = cursor.getColumnIndex(Contract.Debt.EXPIRATION_DATE);
        mIndexArchived = cursor.getColumnIndex(Contract.Debt.ARCHIVED);
        mIndexPlaceId = cursor.getColumnIndex(Contract.Debt.PLACE_ID);
        mIndexPlaceName = cursor.getColumnIndex(Contract.Debt.PLACE_NAME);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        if (holder instanceof HeaderViewHolder) {
            onBindHeaderViewHolder((HeaderViewHolder) holder, cursor);
        } else if (holder instanceof DebtViewHolder) {
            onBindItemViewHolder((DebtViewHolder) holder, cursor);
        }
    }

    private void onBindHeaderViewHolder(HeaderViewHolder holder, Cursor cursor) {
        switch (cursor.getInt(mIndexHeaderType)) {
            case DebtHeaderCursor.HEADER_CURRENT:
                Contract.DebtType debtType = Contract.DebtType.fromValue(cursor.getInt(mIndexDebtType));
                Money money = Money.parse(cursor.getString(mIndexHeaderMoney));
                if (debtType != null) {
                    switch (debtType) {
                        case DEBT:
                            holder.mLeftTextView.setText(R.string.header_debts_to_pay);
                            mMoneyFormatter.applyTintedExpense(holder.mRightTextView, money);
                            break;
                        case CREDIT:
                            holder.mLeftTextView.setText(R.string.header_debts_to_receive);
                            mMoneyFormatter.applyTintedIncome(holder.mRightTextView, money);
                            break;
                    }
                }
                break;
            case DebtHeaderCursor.HEADER_ARCHIVED:
                holder.mLeftTextView.setText(R.string.header_debts_archived);
                holder.mRightTextView.setText(null);
                break;
        }
    }

    private void onBindItemViewHolder(DebtViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mDescriptionTextView.setText(cursor.getString(mIndexDescription));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexCurrency));
        long target = cursor.getLong(mIndexMoney);
        long progress = Math.abs(cursor.getLong(mIndexProgress));
        Contract.DebtType debtType = Contract.DebtType.fromValue(cursor.getInt(mIndexDebtType));
        if (debtType != null) {
            switch (debtType) {
                case DEBT:
                    mMoneyFormatter.applyTintedExpense(holder.mMoneyTextView, currency, target);
                    holder.mPayButton.setVisibility(View.VISIBLE);
                    holder.mReceiveButton.setVisibility(View.GONE);
                    break;
                case CREDIT:
                    mMoneyFormatter.applyTintedIncome(holder.mMoneyTextView, currency, target);
                    holder.mPayButton.setVisibility(View.GONE);
                    holder.mReceiveButton.setVisibility(View.VISIBLE);
                    break;
            }
        }
        holder.mProgressBar.setMaxValue(target);
        holder.mProgressBar.setProgressValue(progress);
        if (progress < target) {
            Context context = holder.mProgressTextView.getContext();
            long remaining = Math.abs(target - progress);
            String differenceText = mMoneyFormatter.getNotTintedString(currency, remaining);
            String progressText = context.getString(R.string.relative_string_money_to_settle_debt, differenceText);
            holder.mProgressTextView.setText(progressText);
        } else {
            holder.mProgressTextView.setText(R.string.relative_string_debt_completed);
        }
        if (cursor.isNull(mIndexExpirationDate)) {
            holder.mRemainingDaysTextView.setVisibility(View.GONE);
        } else {
            holder.mRemainingDaysTextView.setVisibility(View.VISIBLE);
            Context context = holder.mRemainingDaysTextView.getContext();
            Date date = DateUtils.getDateFromSQLDateString(cursor.getString(mIndexExpirationDate));
            String formatted = DateFormatter.getFormattedDate(date);
            if (date.getTime() <= System.currentTimeMillis()) {
                holder.mRemainingDaysTextView.setText(context.getString(R.string.relative_string_ended_on, formatted));
            } else {
                holder.mRemainingDaysTextView.setText(context.getString(R.string.relative_string_will_expire_on, formatted));
            }
        }
        if (cursor.isNull(mIndexPlaceId)) {
            holder.mPlaceTextView.setVisibility(View.GONE);
        } else {
            holder.mPlaceTextView.setVisibility(View.VISIBLE);
            holder.mPlaceTextView.setText(cursor.getString(mIndexPlaceName));
        }
        if (cursor.getInt(mIndexArchived) == 1) {
            holder.mButtonLayoutFix.setVisibility(View.VISIBLE);
            holder.mButtonLayout.setVisibility(View.GONE);
        } else {
            holder.mButtonLayoutFix.setVisibility(View.GONE);
            holder.mButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == DebtHeaderCursor.TYPE_HEADER) {
            View itemView = inflater.inflate(R.layout.adapter_header_item, parent, false);
            return new HeaderViewHolder(itemView);
        } else if (viewType == DebtHeaderCursor.TYPE_ITEM){
            View itemView = inflater.inflate(R.layout.adapter_debt_item, parent, false);
            return new DebtViewHolder(itemView);
        } else {
            throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getSafeCursor(position).getInt(mIndexType);
    }

    /*package-local*/ class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mLeftTextView;
        private TextView mRightTextView;

        /*package-local*/ HeaderViewHolder(View itemView) {
            super(itemView);
            mLeftTextView = itemView.findViewById(R.id.left_text_view);
            mRightTextView = itemView.findViewById(R.id.right_text_view);
        }
    }

    /*package-local*/ class DebtViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mDescriptionTextView;
        private TextView mMoneyTextView;
        private MaterialProgressBar mProgressBar;
        private TextView mProgressTextView;
        private TextView mRemainingDaysTextView;
        private TextView mPlaceTextView;
        private View mButtonLayoutFix;
        private ViewGroup mButtonLayout;
        private CardButton mPayButton;
        private CardButton mReceiveButton;

        /*package-local*/ DebtViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mDescriptionTextView = itemView.findViewById(R.id.primary_text_view);
            mMoneyTextView = itemView.findViewById(R.id.money_text_view);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mProgressTextView = itemView.findViewById(R.id.money_progress_text_view);
            mRemainingDaysTextView = itemView.findViewById(R.id.remaining_days_text_view);
            mPlaceTextView = itemView.findViewById(R.id.place_text_view);
            mButtonLayoutFix = itemView.findViewById(R.id.button_bar_empty_fix);
            mButtonLayout = itemView.findViewById(R.id.button_bar_layout);
            mPayButton = itemView.findViewById(R.id.pay_button);
            mReceiveButton = itemView.findViewById(R.id.receive_button);
            itemView.setOnClickListener(this);
            mPayButton.setOnClickListener(this);
            mReceiveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mActionListener != null) {
                int position = getAdapterPosition();
                Cursor cursor = getSafeCursor(position);
                if (cursor != null) {
                    long id = cursor.getLong(mIndexId);
                    if (view == mPayButton) {
                        mActionListener.onPayClick(id);
                    } else if (view == mReceiveButton) {
                        mActionListener.onReceiveClick(id);
                    } else {
                        mActionListener.onDebtClick(id);
                    }
                }
            }
        }
    }

    public interface ActionListener {

        void onDebtClick(long id);

        void onPayClick(long id);

        void onReceiveClick(long id);
    }
}