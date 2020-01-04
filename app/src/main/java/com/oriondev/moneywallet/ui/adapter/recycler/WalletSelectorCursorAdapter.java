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
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andre on 18/03/2018.
 */
public class WalletSelectorCursorAdapter extends AbstractCursorAdapter<WalletSelectorCursorAdapter.ViewHolder> {

    private int mIndexId;
    private int mIndexName;
    private int mIndexIcon;
    private int mIndexCurrency;
    private int mIndexStartMoney;
    private int mIndexTotalMoney;

    private MoneyFormatter mMoneyFormatter;

    private final Controller mController;

    public WalletSelectorCursorAdapter(Controller controller) {
        super(null, Contract.Wallet.ID);
        mController = controller;
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
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        IconLoader.parseAndLoad(cursor.getString(mIndexIcon), holder.mIconView);
        holder.mNameView.setText(cursor.getString(mIndexName));
        CurrencyUnit currency = CurrencyManager.getCurrency(cursor.getString(mIndexCurrency));
        long money = cursor.getLong(mIndexStartMoney) + cursor.getLong(mIndexTotalMoney);
        mMoneyFormatter.applyNotTinted(holder.mMoneyView, currency, money);
        if (mController != null) {
            long id = cursor.getLong(mIndexId);
            int visibility = mController.isWalletSelected(id) ? View.VISIBLE : View.INVISIBLE;
            holder.mSelectorImageView.setVisibility(visibility);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_wallet_selector_item, parent, false);
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconView;
        private TextView mNameView;
        private TextView mMoneyView;
        private ImageView mSelectorImageView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.avatar_image_view);
            mNameView = itemView.findViewById(R.id.name_text_view);
            mMoneyView = itemView.findViewById(R.id.money_text_view);
            mSelectorImageView = itemView.findViewById(R.id.selector_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    Wallet wallet = new Wallet(
                            cursor.getLong(mIndexId),
                            cursor.getString(mIndexName),
                            IconLoader.parse(cursor.getString(mIndexIcon)),
                            CurrencyManager.getCurrency(cursor.getString(mIndexCurrency)),
                            cursor.getLong(mIndexStartMoney),
                            cursor.getLong(mIndexTotalMoney)
                    );
                    mController.onWalletClick(wallet);
                }
            }
        }
    }

    public interface Controller {

        void onWalletClick(Wallet wallet);

        boolean isWalletSelected(long id);
    }
}