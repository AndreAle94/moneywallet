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
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.IconGroup;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 12/08/18.
 */
public class IconAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int INDEX_HEADER = -1;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final Controller mController;

    private final List<IconGroup> mIconGroups = new ArrayList<>();
    private final List<ItemWrapper> mItems = new ArrayList<>();

    public IconAdapter(Controller controller) {
        mController = controller;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_header_item, parent, false));
            case VIEW_TYPE_ITEM:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_icon_item, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view holder type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).mHeaderTextView.setText(getHeaderTextAt(position));
        } else if (holder instanceof ItemViewHolder) {
            IconLoader.loadInto(getIconAt(position), ((ItemViewHolder) holder).mIconImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    public void setIconGroups(List<IconGroup> iconGroups) {
        mIconGroups.clear();
        mItems.clear();
        for (int i = 0; i < iconGroups.size(); i++) {
            IconGroup group = iconGroups.get(i);
            mItems.add(new ItemWrapper(i, INDEX_HEADER));
            mIconGroups.add(group);
            for (int j = 0; j < group.size(); j++) {
                mItems.add(new ItemWrapper(i, j));
            }
        }
        notifyDataSetChanged();
    }

    public boolean isHeader(int position) {
        return mItems.get(position).mItemIndex == INDEX_HEADER;
    }

    private String getHeaderTextAt(int position) {
        ItemWrapper itemWrapper = mItems.get(position);
        IconGroup group = mIconGroups.get(itemWrapper.mGroupIndex);
        return group.getGroupName();
    }

    private Icon getIconAt(int position) {
        ItemWrapper itemWrapper = mItems.get(position);
        IconGroup group = mIconGroups.get(itemWrapper.mGroupIndex);
        return group.getGroupIcons().get(itemWrapper.mItemIndex);
    }

    private static class ItemWrapper {

        private final int mGroupIndex;
        private final int mItemIndex;

        private ItemWrapper(int groupIndex, int itemIndex) {
            mGroupIndex = groupIndex;
            mItemIndex = itemIndex;
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mHeaderTextView;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            mHeaderTextView = itemView.findViewById(R.id.left_text_view);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIconImageView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            mIconImageView = itemView.findViewById(R.id.icon_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                int position = getAdapterPosition();
                Icon icon = getIconAt(position);
                if (icon != null) {
                    mController.onIconClick(icon);
                }
            }
        }
    }

    public interface Controller {

        void onIconClick(Icon icon);
    }
}