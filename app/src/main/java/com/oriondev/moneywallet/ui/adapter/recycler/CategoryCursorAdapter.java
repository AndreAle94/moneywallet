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
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.ui.view.CategoryChildIndicator;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andrea on 12/02/18.
 */
public class CategoryCursorAdapter extends AbstractCursorAdapter<CategoryCursorAdapter.CategoryViewHolder> {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;

    private int mIndexCategoryId;
    private int mIndexCategoryIcon;
    private int mIndexCategoryName;
    private int mIndexCategoryParentId;

    private final CategoryActionListener mListener;

    public CategoryCursorAdapter(CategoryActionListener listener) {
        super(null, Contract.Category.ID);
        mListener = listener;
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexCategoryId = cursor.getColumnIndex(Contract.Category.ID);
        mIndexCategoryIcon = cursor.getColumnIndex(Contract.Category.ICON);
        mIndexCategoryName = cursor.getColumnIndex(Contract.Category.NAME);
        mIndexCategoryParentId = cursor.getColumnIndex(Contract.Category.PARENT);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexCategoryIcon));
        IconLoader.loadInto(icon, holder.mIconImageView);
        holder.mNameTextView.setText(cursor.getString(mIndexCategoryName));
        if (holder.getItemViewType() == TYPE_CHILD) {
            holder.mChildIndicator.setLast(isLastChild(cursor.getPosition()));
        }
    }

    private boolean isLastChild(int position) {
        int nextCategoryPosition = position + 1;
        if (nextCategoryPosition < getItemCount()) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(nextCategoryPosition);
            boolean hasParent = !cursor.isNull(mIndexCategoryParentId);
            cursor.moveToPosition(position);
            return !hasParent;
        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = getSafeCursor(position);
        return cursor.isNull(mIndexCategoryParentId) ? TYPE_PARENT : TYPE_CHILD;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_PARENT) {
            View itemView = inflater.inflate(R.layout.adapter_category_item, parent, false);
            return new CategoryViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.adapter_sub_category_item, parent, false);
            return new CategoryViewHolder(itemView);
        }
    }

    /*package-local*/ class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CategoryChildIndicator mChildIndicator;
        private ImageView mIconImageView;
        private TextView mNameTextView;

        /*package-local*/ CategoryViewHolder(View itemView) {
            super(itemView);
            mChildIndicator = itemView.findViewById(R.id.category_child_indicator);
            mIconImageView = itemView.findViewById(R.id.icon_image_view);
            mNameTextView = itemView.findViewById(R.id.name_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    mListener.onCategoryClick(cursor.getLong(mIndexCategoryId));
                }
            }
        }
    }

    public interface CategoryActionListener {

        void onCategoryClick(long id);
    }
}