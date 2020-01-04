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

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 06/03/18.
 */
public class SettingCategoryAdapter extends RecyclerView.Adapter<SettingCategoryAdapter.ViewHolder> {

    private final List<Item> mItems;
    private final ActionListener mActionListener;

    public SettingCategoryAdapter(ActionListener actionListener) {
        mItems = new ArrayList<>();
        mActionListener = actionListener;
    }

    public void addCategory(int id, @DrawableRes int icon, @StringRes int primaryText, @StringRes int secondaryText) {
        mItems.add(new Item(id, icon, primaryText, secondaryText));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_setting_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = mItems.get(position);
        holder.mIconImageView.setImageResource(item.mIconRes);
        holder.mPrimaryTextView.setText(item.mPrimaryTextRes);
        holder.mSecondaryTextView.setText(item.mSecondaryTextRes);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconImageView;
        private TextView mPrimaryTextView;
        private TextView mSecondaryTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mIconImageView = itemView.findViewById(R.id.icon_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mActionListener != null) {
                Item item = mItems.get(getAdapterPosition());
                mActionListener.onSettingCategoryClick(item.mId);
            }
        }
    }

    private class Item {

        private final int mId;
        private final int mIconRes;
        private final int mPrimaryTextRes;
        private final int mSecondaryTextRes;

        private Item(int id, int iconRes, int primaryTextRes, int secondaryTextRes) {
            mId = id;
            mIconRes = iconRes;
            mPrimaryTextRes = primaryTextRes;
            mSecondaryTextRes = secondaryTextRes;
        }
    }

    public interface ActionListener {

        void onSettingCategoryClick(int id);
    }
}