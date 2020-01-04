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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.utils.IconLoader;

/**
 * Created by andre on 20/03/2018.
 */
public class PlaceSelectorCursorAdapter extends AbstractCursorAdapter<PlaceSelectorCursorAdapter.ViewHolder> {

    private int mIndexId;
    private int mIndexName;
    private int mIndexIcon;
    private int mIndexAddress;
    private int mIndexLatitude;
    private int mIndexLongitude;

    private Controller mController;

    public PlaceSelectorCursorAdapter(Controller controller) {
        super(null, Contract.Place.ID);
        mController = controller;
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexId = cursor.getColumnIndex(Contract.Place.ID);
        mIndexName = cursor.getColumnIndex(Contract.Place.NAME);
        mIndexIcon = cursor.getColumnIndex(Contract.Place.ICON);
        mIndexAddress = cursor.getColumnIndex(Contract.Place.ADDRESS);
        mIndexLatitude = cursor.getColumnIndex(Contract.Place.LATITUDE);
        mIndexLongitude = cursor.getColumnIndex(Contract.Place.LONGITUDE);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mPrimaryTextView.setText(cursor.getString(mIndexName));
        String address = cursor.getString(mIndexAddress);
        if (TextUtils.isEmpty(address)) {
            holder.mSecondaryTextView.setText(R.string.hint_address_unknown);
        } else {
            holder.mSecondaryTextView.setText(address);
        }
        boolean selected = mController.isPlaceSelected(cursor.getLong(mIndexId));
        holder.mSelectorImageView.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_place_selector_item, parent, false);
        return new ViewHolder(itemView);
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private TextView mSecondaryTextView;
        private ImageView mSelectorImageView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            mSelectorImageView = itemView.findViewById(R.id.selector_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                Cursor cursor = getSafeCursor(getAdapterPosition());
                if (cursor != null) {
                    Place place = new Place(
                            cursor.getLong(mIndexId),
                            cursor.getString(mIndexName),
                            IconLoader.parse(cursor.getString(mIndexIcon)),
                            cursor.getString(mIndexAddress),
                            cursor.isNull(mIndexLatitude) ? null : cursor.getDouble(mIndexLatitude),
                            cursor.isNull(mIndexLongitude) ? null : cursor.getDouble(mIndexLongitude)
                    );
                    mController.onPlaceSelected(place);
                }
            }
        }
    }

    public interface Controller {

        void onPlaceSelected(Place place);

        boolean isPlaceSelected(long id);
    }
}