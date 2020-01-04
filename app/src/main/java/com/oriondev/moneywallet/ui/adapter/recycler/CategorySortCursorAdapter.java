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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by andrea on 12/02/18.
 */
public class CategorySortCursorAdapter extends AbstractCursorAdapter<CategorySortCursorAdapter.CategoryViewHolder> {

    private int mIndexCategoryId;
    private int mIndexCategoryIcon;
    private int mIndexCategoryName;

    private final CategorySortListener mListener;
    private final List<Integer> mSortedIndices;

    public CategorySortCursorAdapter(CategorySortListener listener) {
        super(null, Contract.Category.ID);
        mListener = listener;
        mSortedIndices = new ArrayList<>();
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexCategoryId = cursor.getColumnIndex(Contract.Category.ID);
        mIndexCategoryIcon = cursor.getColumnIndex(Contract.Category.ICON);
        mIndexCategoryName = cursor.getColumnIndex(Contract.Category.NAME);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position >= 0 && position < mSortedIndices.size()) {
            super.onBindViewHolder(holder, mSortedIndices.get(position));
        }
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexCategoryIcon));
        IconLoader.loadInto(icon, holder.mIconImageView);
        holder.mNameTextView.setText(cursor.getString(mIndexCategoryName));
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_category_sort_item, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mSortedIndices.size()) {
            return super.getItemId(mSortedIndices.get(position));
        }
        return 0L;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = super.swapCursor(newCursor);
        mSortedIndices.clear();
        if (newCursor != null) {
            for (int i = 0; i < newCursor.getCount(); i++) {
                mSortedIndices.add(i);
            }
        }
        return oldCursor;
    }

    public boolean moveItem(int from, int to) {
        if (Math.min(from, to) >= 0 && Math.max(from, to) < mSortedIndices.size()) {
            Collections.swap(mSortedIndices, from, to);
            notifyItemMoved(from, to);
            return true;
        }
        return false;
    }

    public List<Long> getSortedCategoryIds() {
        List<Long> categoryIds = new ArrayList<>();
        for (int i = 0; i < mSortedIndices.size(); i++) {
            Cursor cursor = getSafeCursor(mSortedIndices.get(i));
            if (cursor != null) {
                categoryIds.add(cursor.getLong(mIndexCategoryId));
            }
        }
        return categoryIds;
    }

    /*package-local*/ class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        private ImageView mIconImageView;
        private TextView mNameTextView;
        private ImageView mActionImageView;

        /*package-local*/ CategoryViewHolder(View itemView) {
            super(itemView);
            mIconImageView = itemView.findViewById(R.id.icon_image_view);
            mNameTextView = itemView.findViewById(R.id.name_text_view);
            mActionImageView = itemView.findViewById(R.id.action_image_view);
            mActionImageView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (mListener != null) {
                    mListener.onCategoryDragStarted(this);
                }
            }
            return false;
        }
    }

    public interface CategorySortListener {

        void onCategoryDragStarted(RecyclerView.ViewHolder viewHolder);
    }
}