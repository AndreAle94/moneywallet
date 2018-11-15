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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by andre on 21/03/2018.
 */
public class BackupFileAdapter<T extends IFile> extends RecyclerView.Adapter<BackupFileAdapter.ViewHolder> implements Comparator<T> {

    private final Controller<T> mController;

    private List<T> mFileList;
    private boolean mCanNavigateBack;

    public BackupFileAdapter(Controller<T> controller) {
        mController = controller;
        mFileList = null;
        mCanNavigateBack = false;
    }

    public void setFileList(List<T> fileList, boolean canNavigateBack) {
        mFileList = fileList;
        mCanNavigateBack = canNavigateBack;
        if (mFileList != null) {
            Collections.sort(mFileList, this);
        }
        notifyDataSetChanged();
    }

    public void addFileToList(T file) {
        mFileList.add(file);
        if (mFileList != null) {
            Collections.sort(mFileList, this);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_backup_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BackupFileAdapter.ViewHolder holder, int position) {
        if (mCanNavigateBack && position == 0) {
            holder.mAvatarImageView.setVisibility(View.INVISIBLE);
            holder.mPrimaryTextView.setText(" ... ");
            holder.mSecondaryTextView.setText(R.string.hint_navigate_up);
        } else {
            holder.mAvatarImageView.setVisibility(View.VISIBLE);
            T file = mFileList.get(position - (mCanNavigateBack ? 1 : 0));
            holder.mPrimaryTextView.setText(file.getName());
            if (file.isDirectory()) {
                holder.mAvatarImageView.setImageResource(R.drawable.ic_folder_open_black_24dp);
                holder.mSecondaryTextView.setText(R.string.hint_directory);
            } else {
                Context context = holder.mAvatarImageView.getContext();
                String extension = file.getExtension();
                Glide.with(context)
                        .load(Utils.getFileIcon(extension))
                        .into(holder.mAvatarImageView);
                long size = file.getSize();
                if (size >= 0) {
                    holder.mSecondaryTextView.setText(Utils.readableFileSize(size));
                } else {
                    holder.mSecondaryTextView.setText(R.string.hint_size_unknown);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mFileList != null ? mFileList.size() : 0) + (mCanNavigateBack ? 1 : 0);
    }

    @Override
    public int compare(T file1, T file2) {
        if (file1.isDirectory()) {
            if (file2.isDirectory()) {
                return file1.getName().compareTo(file2.getName());
            } else {
                return -1;
            }
        } else {
            if (file2.isDirectory()) {
                return 1;
            } else {
                return file1.getName().compareTo(file2.getName());
            }
        }
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private TextView mSecondaryTextView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                int position = getAdapterPosition();
                if (mCanNavigateBack) {
                    if (position == 0) {
                        mController.navigateBack();
                    } else {
                        mController.onFileClick(mFileList.get(position - 1));
                    }
                } else {
                    mController.onFileClick(mFileList.get(position));
                }
            }
        }
    }

    public interface Controller<T extends IFile> {

        void navigateBack();

        void onFileClick(T file);
    }
}