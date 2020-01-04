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
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 26/01/18.
 */
public class WalletCursorAdapter extends AbstractCursorAdapter<WalletCursorAdapter.WalletViewHolder> {

    private int mIndexId;
    private int mIndexName;
    private int mIndexIcon;
    private int mIndexCurrency;
    private int mIndexStartMoney;
    private int mIndexTotalMoney;
    private int mIndexCountInTotal;

    private MoneyFormatter mMoneyFormatter;

    private final WalletActionListener mListener;

    public WalletCursorAdapter(WalletActionListener listener) {
        super(null, Contract.Wallet.ID);
        mListener = listener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexId = cursor.getColumnIndex(Contract.Wallet.ID);
        mIndexName = cursor.getColumnIndex(Contract.Wallet.NAME);
        mIndexIcon = cursor.getColumnIndex(Contract.Wallet.ICON);
        mIndexCurrency = cursor.getColumnIndex(Contract.Wallet.CURRENCY);
        mIndexStartMoney = cursor.getColumnIndex(Contract.Wallet.START_MONEY);
        mIndexTotalMoney = cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY);
        mIndexCountInTotal = cursor.getColumnIndex(Contract.Wallet.COUNT_IN_TOTAL);
    }

    @Override
    public WalletViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_wallet_item, parent, false);
        return new WalletViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WalletViewHolder holder, Cursor cursor) {
        IconLoader.parseAndLoad(cursor.getString(mIndexIcon), holder.mIconView);
        holder.mNameView.setText(cursor.getString(mIndexName));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexCurrency));
        long money = cursor.getLong(mIndexStartMoney) + cursor.getLong(mIndexTotalMoney);
        mMoneyFormatter.applyNotTinted(holder.mMoneyView, currency, money);
    }

    /*package-local*/ class WalletViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconView;
        private TextView mNameView;
        private TextView mMoneyView;

        /*package-local*/ WalletViewHolder(View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.icon_image_view);
            mNameView = itemView.findViewById(R.id.name_text_view);
            mMoneyView = itemView.findViewById(R.id.money_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    mListener.onWalletClick(cursor.getLong(mIndexId));
                }
            }
        }
    }

    public interface WalletActionListener {

        void onWalletClick(long id);
    }
}