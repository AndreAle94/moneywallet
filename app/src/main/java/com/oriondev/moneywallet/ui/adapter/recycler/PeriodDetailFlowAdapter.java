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

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.CategoryMoney;
import com.oriondev.moneywallet.model.PeriodDetailFlowData;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

/**
 * Created by andrea on 13/08/18.
 */
public class PeriodDetailFlowAdapter extends RecyclerView.Adapter<PeriodDetailFlowAdapter.ViewHolder> {

    private final Controller mController;
    private final boolean mIncomes;
    private final MoneyFormatter mMoneyFormatter;

    private PeriodDetailFlowData mData;

    public PeriodDetailFlowAdapter(Controller controller, boolean incomes) {
        mController = controller;
        mIncomes = incomes;
        mMoneyFormatter = MoneyFormatter.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_category_money_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryMoney category = mData.getCategory(position);
        IconLoader.loadInto(category.getIcon(), holder.mIconImageView);
        holder.mNameTextView.setText(category.getName());
        if (mIncomes) {
            mMoneyFormatter.applyTintedIncome(holder.mMoneyTextView, category.getMoney());
        } else {
            mMoneyFormatter.applyTintedExpense(holder.mMoneyTextView, category.getMoney());
        }
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.getCategoryCount() : 0;
    }

    public void setData(PeriodDetailFlowData data) {
        mData = data;
        notifyDataSetChanged();
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconImageView;
        private TextView mNameTextView;
        private TextView mMoneyTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mIconImageView = itemView.findViewById(R.id.icon_image_view);
            mNameTextView = itemView.findViewById(R.id.name_text_view);
            mMoneyTextView = itemView.findViewById(R.id.money_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                int index = getAdapterPosition();
                if (mData != null) {
                    CategoryMoney category = mData.getCategory(index);
                    mController.onCategoryClick(category.getId());
                }
            }
        }
    }

    public interface Controller {

        void onCategoryClick(long id);
    }
}