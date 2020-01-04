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
import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.utils.DateFormatter;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;

import java.util.Date;

/**
 * Created by andrea on 03/03/18.
 */
public class EventSelectorCursorAdapter extends AbstractCursorAdapter<EventSelectorCursorAdapter.ViewHolder> {

    private int mIndexId;
    private int mIndexIcon;
    private int mIndexName;
    private int mIndexStartDate;
    private int mIndexEndDate;

    private final Controller mController;

    public EventSelectorCursorAdapter(Controller controller) {
        super(null, Contract.Event.ID);
        mController = controller;
    }

    @Override
    protected void onLoadColumnIndices(@NonNull Cursor cursor) {
        mIndexId = cursor.getColumnIndex(Contract.Event.ID);
        mIndexIcon = cursor.getColumnIndex(Contract.Event.ICON);
        mIndexName = cursor.getColumnIndex(Contract.Event.NAME);
        mIndexStartDate = cursor.getColumnIndex(Contract.Event.START_DATE);
        mIndexEndDate = cursor.getColumnIndex(Contract.Event.END_DATE);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        Icon icon = IconLoader.parse(cursor.getString(mIndexIcon));
        IconLoader.loadInto(icon, holder.mAvatarImageView);
        holder.mPrimaryTextView.setText(cursor.getString(mIndexName));
        Date endDate = DateUtils.getDateFromSQLDateString(cursor.getString(mIndexEndDate));
        DateFormatter.applyDateFromToday(holder.mSecondaryTextView, endDate, R.string.relative_string_will_end_on);
        boolean selected = mController.isEventSelected(cursor.getLong(mIndexId));
        holder.mSelectorImageView.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_event_selector_item, parent, false);
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
                    Event event = new Event(
                            cursor.getLong(mIndexId),
                            cursor.getString(mIndexName),
                            IconLoader.parse(cursor.getString(mIndexIcon)),
                            DateUtils.getDateFromSQLDateString(cursor.getString(mIndexStartDate)),
                            DateUtils.getDateFromSQLDateString(cursor.getString(mIndexEndDate))
                    );
                    mController.onEventClick(event);
                }
            }
        }
    }

    public interface Controller {

        void onEventClick(Event event);

        boolean isEventSelected(long id);
    }

}