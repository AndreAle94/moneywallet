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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.storage.database.Contract;

import java.util.Locale;

/**
 * Created by andrea on 09/02/18.
 */
public class CurrencyCursorAdapter extends AbstractCursorAdapter<CurrencyCursorAdapter.CurrencyViewHolder> {

    private int mIndexIso;
    private int mIndexName;
    private int mIndexSymbol;
    private int mIndexFavourite;

    private final CurrencyActionListener mListener;

    public CurrencyCursorAdapter(CurrencyActionListener listener) {
        super(null, Contract.Currency.ISO);
        mListener = listener;
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexIso = cursor.getColumnIndex(Contract.Currency.ISO);
        mIndexName = cursor.getColumnIndex(Contract.Currency.NAME);
        mIndexSymbol = cursor.getColumnIndex(Contract.Currency.SYMBOL);
        mIndexFavourite = cursor.getColumnIndex(Contract.Currency.FAVOURITE);
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, Cursor cursor) {
        String iso = cursor.getString(mIndexIso);
        String name = cursor.getString(mIndexName);
        String symbol = cursor.getString(mIndexSymbol);
        Context context = holder.mIconView.getContext();
        int currencyFlag = CurrencyUnit.getCurrencyFlag(context, iso);
        if (currencyFlag > 0) {
            holder.mIconView.setImageDrawable(null);
            Glide.with(holder.mIconView)
                    .load(currencyFlag)
                    .into(holder.mIconView);
        } else {
            holder.mIconView.setImageDrawable(CurrencyUnit.getCurrencyDrawable(iso));
        }
        holder.mNameView.setText(name);
        if (!TextUtils.isEmpty(symbol)) {
            holder.mSymbolView.setText(String.format(Locale.ENGLISH, "%s (%s)", iso, symbol));
        } else {
            holder.mSymbolView.setText(iso);
        }
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_currency_item, parent, false);
        return new CurrencyViewHolder(itemView);
    }

    /*package-local*/ class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconView;
        private TextView mNameView;
        private TextView mSymbolView;

        /*package-local*/ CurrencyViewHolder(View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.icon_image_view);
            mNameView = itemView.findViewById(R.id.name_text_view);
            mSymbolView = itemView.findViewById(R.id.secondary_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    String iso = cursor.getString(mIndexIso);
                    if (/* view is the star */ false) {
                        boolean favourite = cursor.getInt(mIndexFavourite) == 1;
                        mListener.onCurrencyFavourite(iso, !favourite);
                    } else {
                        mListener.onCurrencyClick(iso);
                    }
                }
            }
        }
    }

    public interface CurrencyActionListener {

        void onCurrencyClick(String iso);

        void onCurrencyFavourite(String iso, boolean newValue);
    }
}