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
import com.oriondev.moneywallet.model.BackupService;
import com.oriondev.moneywallet.storage.database.backup.BackupManager;

import java.util.List;

/**
 * Created by andre on 21/03/2018.
 */
public class BackupServiceAdapter extends RecyclerView.Adapter<BackupServiceAdapter.ViewHolder> {

    private final List<BackupService> mBackupServices;
    private final Controller mController;

    public BackupServiceAdapter(Controller controller) {
        mBackupServices = BackupManager.getBackupServices();
        mController = controller;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_backup_service_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BackupService backupService = mBackupServices.get(position);
        holder.mAvatarImageView.setImageResource(backupService.getIconRes());
        holder.mPrimaryTextView.setText(backupService.getNameRes());
    }

    @Override
    public int getItemCount() {
        return mBackupServices.size();
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                mController.onBackupServiceClick(mBackupServices.get(getAdapterPosition()));
            }
        }
    }

    public interface Controller {

        void onBackupServiceClick(BackupService service);
    }
}