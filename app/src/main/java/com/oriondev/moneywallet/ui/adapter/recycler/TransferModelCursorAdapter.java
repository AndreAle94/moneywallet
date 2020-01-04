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
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Locale;

/**
 * Created by andrea on 14/03/18.
 */
public class TransferModelCursorAdapter extends AbstractCursorAdapter<TransferModelCursorAdapter.TransferModelViewHolder> {

    private int mIndexId;
    private int mIndexWalletFromName;
    private int mIndexWalletFromCurrency;
    private int mIndexWalletToName;
    private int mIndexWalletToIcon;
    private int mIndexMoneyFrom;
    private int mIndexMoneyTax;

    private MoneyFormatter mMoneyFormatter;

    private final ActionListener mActionListener;

    public TransferModelCursorAdapter(ActionListener actionListener) {
        super(null, Contract.TransferModel.ID);
        mActionListener = actionListener;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexId = cursor.getColumnIndex(Contract.TransferModel.ID);
        mIndexWalletFromName = cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_NAME);
        mIndexWalletFromCurrency = cursor.getColumnIndex(Contract.TransferModel.WALLET_FROM_CURRENCY);
        mIndexWalletToName = cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_NAME);
        mIndexWalletToIcon = cursor.getColumnIndex(Contract.TransferModel.WALLET_TO_ICON);
        mIndexMoneyFrom = cursor.getColumnIndex(Contract.TransferModel.MONEY_FROM);
        mIndexMoneyTax = cursor.getColumnIndex(Contract.TransferModel.MONEY_TAX);
    }

    @Override
    public void onBindViewHolder(TransferModelViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexWalletToIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mPrimaryTextView.setText(R.string.system_category_transfer);
        long moneyFrom = cursor.getLong(mIndexMoneyFrom);
        long moneyTax = cursor.getLong(mIndexMoneyTax);
        CurrencyUnit currencyFrom = CurrencyManager.getCurrency(cursor.getString(mIndexWalletFromCurrency));
        mMoneyFormatter.applyNotTinted(holder.mMoneyTextView, currencyFrom, moneyFrom);
        String walletFrom = cursor.getString(mIndexWalletFromName);
        String walletTo = cursor.getString(mIndexWalletToName);
        holder.mSecondaryTextView.setText(String.format(Locale.ENGLISH, "%s → %s", walletFrom, walletTo));
        if (moneyTax != 0L) {
            holder.mTaxLayout.setVisibility(View.VISIBLE);
            mMoneyFormatter.applyNotTinted(holder.mTaxTextView, currencyFrom, moneyTax);
        } else {
            holder.mTaxLayout.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public TransferModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_model_transfer_item, parent, false);
        return new TransferModelViewHolder(itemView);
    }

    /*package-local*/ class TransferModelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private TextView mMoneyTextView;
        private TextView mSecondaryTextView;
        private ViewGroup mTaxLayout;
        private TextView mTaxTextView;
        private CardButton mAddButton;
        private CardButton mEditButton;

        /*package-local*/ TransferModelViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mMoneyTextView = itemView.findViewById(R.id.money_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            mTaxLayout = itemView.findViewById(R.id.tax_layout);
            mTaxTextView = itemView.findViewById(R.id.tax_body_text_view);
            mAddButton = itemView.findViewById(R.id.add_button);
            mEditButton = itemView.findViewById(R.id.edit_button);
            itemView.setOnClickListener(this);
            mAddButton.setOnClickListener(this);
            mEditButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mActionListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    long id = cursor.getLong(mIndexId);
                    if (v == mAddButton) {
                        mActionListener.onModelAddClick(id);
                    } else if (v == mEditButton) {
                        mActionListener.onModelEditClick(id);
                    } else {
                        mActionListener.onModelClick(id);
                    }
                }
            }
        }
    }

    public interface ActionListener {

        void onModelClick(long id);

        void onModelAddClick(long id);

        void onModelEditClick(long id);
    }
}