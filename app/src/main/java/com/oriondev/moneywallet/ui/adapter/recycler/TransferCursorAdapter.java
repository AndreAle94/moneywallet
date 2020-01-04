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
import com.oriondev.moneywallet.storage.wrapper.TransferHeaderCursor;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;
import java.util.Locale;

/**
 * Created by andrea on 03/03/18.
 */
public class TransferCursorAdapter extends AbstractCursorAdapter<RecyclerView.ViewHolder> {

    private final ActionListener mActionListener;

    private int mIndexType;
    private int mIndexHeaderStartDate;
    private int mIndexHeaderEndDate;
    private int mIndexHeaderGroupType;
    private int mIndexTransferId;
    private int mIndexWalletFromName;
    private int mIndexWalletFromIcon;
    private int mIndexWalletFromCurrency;
    private int mIndexWalletToName;
    private int mIndexWalletToIcon;
    private int mIndexWalletToCurrency;
    private int mIndexMoneyFrom;
    private int mIndexMoneyTo;
    private int mIndexMoneyTax;
    private int mIndexDate;
    private int mIndexDescription;

    private MoneyFormatter mMoneyFormatter;

    public TransferCursorAdapter(ActionListener actionListener) {
        super(null, Contract.Transfer.ID);
        mActionListener = actionListener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexType = cursor.getColumnIndex(TransferHeaderCursor.COLUMN_ITEM_TYPE);
        mIndexHeaderStartDate = cursor.getColumnIndex(TransferHeaderCursor.COLUMN_HEADER_START_DATE);
        mIndexHeaderEndDate = cursor.getColumnIndex(TransferHeaderCursor.COLUMN_HEADER_END_DATE);
        mIndexHeaderGroupType = cursor.getColumnIndex(TransferHeaderCursor.COLUMN_HEADER_GROUP_TYPE);
        mIndexTransferId = cursor.getColumnIndex(Contract.Transfer.ID);
        mIndexWalletFromName = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_NAME);
        mIndexWalletFromIcon = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_ICON);
        mIndexWalletFromCurrency = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_WALLET_CURRENCY);
        mIndexWalletToName = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_NAME);
        mIndexWalletToIcon = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_ICON);
        mIndexWalletToCurrency = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_WALLET_CURRENCY);
        mIndexMoneyFrom = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_FROM_MONEY);
        mIndexMoneyTo = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TO_MONEY);
        mIndexMoneyTax = cursor.getColumnIndex(Contract.Transfer.TRANSACTION_TAX_MONEY);
        mIndexDate = cursor.getColumnIndex(Contract.Transfer.DATE);
        mIndexDescription = cursor.getColumnIndex(Contract.Transfer.DESCRIPTION);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        if (holder instanceof HeaderViewHolder) {
            onBindHeaderViewHolder((HeaderViewHolder) holder, cursor);
        } else if (holder instanceof TransferViewHolder) {
            onBindItemViewHolder((TransferViewHolder) holder, cursor);
        }
    }

    private void onBindItemViewHolder(TransferViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexWalletToIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        String walletFrom = cursor.getString(mIndexWalletFromName);
        String walletTo = cursor.getString(mIndexWalletToName);
        holder.mPrimaryTextView.setText(String.format(Locale.ENGLISH, "%s -> %s", walletFrom, walletTo));
        holder.mSecondaryTextView.setText(cursor.getString(mIndexDescription));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexWalletFromCurrency));
        long money = cursor.getLong(mIndexMoneyFrom);
        mMoneyFormatter.applyNotTinted(holder.mMoneyTextView, currency, money);
        Date date = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexDate));
        DateFormatter.applyDate(holder.mDateTextView, date);
    }

    private void onBindHeaderViewHolder(HeaderViewHolder holder, Cursor cursor) {
        Date start = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderStartDate));
        Date end = DateUtils.getDateFromSQLDateTimeString(cursor.getString(mIndexHeaderEndDate));
        DateFormatter.applyDateRange(holder.mLeftTextView, start, end);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TransferHeaderCursor.TYPE_HEADER) {
            View itemView = inflater.inflate(R.layout.adapter_header_item, parent, false);
            return new HeaderViewHolder(itemView);
        } else if (viewType == TransferHeaderCursor.TYPE_ITEM){
            View itemView = inflater.inflate(R.layout.adapter_transfer_item, parent, false);
            return new TransferViewHolder(itemView);
        } else {
            throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getSafeCursor(position).getInt(mIndexType);
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
                mActionListener.onHeaderClick();
            }
        }
    }

    /*package-local*/ class TransferViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private TextView mMoneyTextView;
        private TextView mSecondaryTextView;
        private TextView mDateTextView;

        /*package-local*/ TransferViewHolder(View itemView) {
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
                    mActionListener.onTransferClick(cursor.getLong(mIndexTransferId));
                }
            }
        }
    }

    public interface ActionListener {

        void onHeaderClick();

        void onTransferClick(long id);
    }
}