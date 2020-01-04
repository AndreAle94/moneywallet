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
import com.oriondev.moneywallet.model.Money;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.wrapper.TransactionHeaderCursor;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 03/03/18.
 */
public class TransactionCursorAdapter extends AbstractCursorAdapter<RecyclerView.ViewHolder> {

    private final ActionListener mActionListener;

    private int mIndexType;
    private int mIndexHeaderStartDate;
    private int mIndexHeaderEndDate;
    private int mIndexHeaderMoney;
    private int mIndexHeaderGroupType;
    private int mIndexCategoryName;
    private int mIndexCategoryIcon;
    private int mIndexTransactionId;
    private int mIndexTransactionDirection;
    private int mIndexTransactionDescription;
    private int mIndexTransactionDate;
    private int mIndexTransactionMoney;
    private int mIndexCurrency;

    private MoneyFormatter mMoneyFormatter;

    public TransactionCursorAdapter(ActionListener actionListener) {
        super(null, Contract.Transaction.ID);
        mActionListener = actionListener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexType = cursor.getColumnIndex(TransactionHeaderCursor.COLUMN_ITEM_TYPE);
        mIndexHeaderStartDate = cursor.getColumnIndex(TransactionHeaderCursor.COLUMN_HEADER_START_DATE);
        mIndexHeaderEndDate = cursor.getColumnIndex(TransactionHeaderCursor.COLUMN_HEADER_END_DATE);
        mIndexHeaderMoney = cursor.getColumnIndex(TransactionHeaderCursor.COLUMN_HEADER_MONEY);
        mIndexHeaderGroupType = cursor.getColumnIndex(TransactionHeaderCursor.COLUMN_HEADER_GROUP_TYPE);
        mIndexCategoryName = cursor.getColumnIndex(Contract.Transaction.CATEGORY_NAME);
        mIndexCategoryIcon = cursor.getColumnIndex(Contract.Transaction.CATEGORY_ICON);
        mIndexTransactionId = cursor.getColumnIndex(Contract.Transaction.ID);
        mIndexTransactionDirection = cursor.getColumnIndex(Contract.Transaction.DIRECTION);
        mIndexTransactionDescription = cursor.getColumnIndex(Contract.Transaction.DESCRIPTION);
        mIndexTransactionDate = cursor.getColumnIndex(Contract.Transaction.DATE);
        mIndexTransactionMoney = cursor.getColumnIndex(Contract.Transaction.MONEY);
        mIndexCurrency = cursor.getColumnIndex(Contract.Transaction.WALLET_CURRENCY);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        if (viewHolder instanceof HeaderViewHolder) {
            onBindHeaderViewHolder((HeaderViewHolder) viewHolder, cursor);
        } else if (viewHolder instanceof TransactionViewHolder) {
            onBindItemViewHolder((TransactionViewHolder) viewHolder, cursor);
        }
    }

    private void onBindItemViewHolder(TransactionViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexCategoryIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mPrimaryTextView.setText(cursor.getString(mIndexCategoryName));
        holder.mSecondaryTextView.setText(cursor.getString(mIndexTransactionDescription));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexCurrency));
        long money = cursor.getLong(mIndexTransactionMoney);
        if (cursor.getInt(mIndexTransactionDirection) == Contract.Direction.INCOME) {
            mMoneyFormatter.applyTintedIncome(holder.mMoneyTextView, currency, money);
        } else {
            mMoneyFormatter.applyTintedExpense(holder.mMoneyTextView, currency, money);
        }
        Date date = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexTransactionDate));
        DateFormatter.applyDate(holder.mDateTextView, date);
    }

    private void onBindHeaderViewHolder(HeaderViewHolder holder, Cursor cursor) {
        Date start = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderStartDate));
        Date end = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderEndDate));
        DateFormatter.applyDateRange(holder.mLeftTextView, start, end);
        Money money = Money.parse(cursor.getString(mIndexHeaderMoney));
        mMoneyFormatter.applyTinted(holder.mRightTextView, money);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TransactionHeaderCursor.TYPE_HEADER) {
            View itemView = inflater.inflate(R.layout.adapter_header_item, parent, false);
            return new HeaderViewHolder(itemView);
        } else if (viewType == TransactionHeaderCursor.TYPE_ITEM){
            View itemView = inflater.inflate(R.layout.adapter_transaction_item, parent, false);
            return new TransactionViewHolder(itemView);
        } else {
            throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mIndexType != -1) {
            return getSafeCursor(position).getInt(mIndexType);
        } else {
            return TransactionHeaderCursor.TYPE_ITEM;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mLeftTextView;
        private TextView mRightTextView;

        /*package-local*/ HeaderViewHolder(View itemView) {
            super(itemView);
            mLeftTextView = itemView.findViewById(R.id.left_text_view);
            mRightTextView = itemView.findViewById(R.id.right_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mActionListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    Date start = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderStartDate));
                    Date end = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderEndDate));
                    mActionListener.onHeaderClick(start, end);
                }
            }
        }
    }

    /*package-local*/ class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private TextView mMoneyTextView;
        private TextView mSecondaryTextView;
        private TextView mDateTextView;

        /*package-local*/ TransactionViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mMoneyTextView = itemView.findViewById(R.id.money_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            mDateTextView = itemView.findViewById(R.id.date_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mActionListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    mActionListener.onTransactionClick(cursor.getLong(mIndexTransactionId));
                }
            }
        }
    }

    public interface ActionListener {

        void onHeaderClick(Date startDate, Date endDate);

        void onTransactionClick(long id);
    }
}