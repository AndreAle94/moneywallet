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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.Contract;

/**
 * Created by andre on 20/03/2018.
 */
public class BudgetTypeSelectorAdapter extends RecyclerView.Adapter<BudgetTypeSelectorAdapter.ViewHolder> {

    private static final Contract.BudgetType[] BUDGET_TYPES = new Contract.BudgetType[] {
            Contract.BudgetType.INCOMES,
            Contract.BudgetType.EXPENSES,
            Contract.BudgetType.CATEGORY
    };

    private final Controller mController;

    public BudgetTypeSelectorAdapter(Controller controller) {
        mController = controller;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_budget_type_selector_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contract.BudgetType budgetType = BUDGET_TYPES[position];
        switch (budgetType) {
            case INCOMES:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_bank_transfer_in_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_incomes);
                break;
            case EXPENSES:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_bank_transfer_out_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_expenses);
                break;
            case CATEGORY:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_table_large_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_category);
                break;
        }
        boolean selected = mController.isBudgetTypeSelected(budgetType);
        holder.mSelectorImageView.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return BUDGET_TYPES.length;
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private ImageView mSelectorImageView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSelectorImageView = itemView.findViewById(R.id.selector_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {;
                mController.onBudgetTypeSelected(BUDGET_TYPES[getAdapterPosition()]);
            }
        }
    }

    public interface Controller {

        void onBudgetTypeSelected(Contract.BudgetType budgetType);

        boolean isBudgetTypeSelected(Contract.BudgetType budgetType);
    }
}