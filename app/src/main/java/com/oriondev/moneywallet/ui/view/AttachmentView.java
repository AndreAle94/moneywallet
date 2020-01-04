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

package com.oriondev.moneywallet.ui.view;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Attachment;
import com.oriondev.moneywallet.utils.Utils;

import java.util.List;

/**
 * Created by andrea on 30/03/18.
 */
public class AttachmentView extends LinearLayout {

    private LinearLayout mContainer;
    private List<Attachment> mAttachments;

    private Controller mController;

    private boolean mAllowRemove = true;

    public AttachmentView(Context context) {
        super(context);
        initialize(context);
    }

    public AttachmentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public AttachmentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        setOrientation(VERTICAL);
        View view = inflate(context, R.layout.layout_attachment_view, this);
        mContainer = view.findViewById(R.id.attachment_container);
    }

    public void setController(Controller controller) {
        mController = controller;
    }

    public void setAllowRemove(boolean allowRemove) {
        mAllowRemove = allowRemove;
    }

    public void setAttachments(List<Attachment> attachments) {
        mAttachments = attachments;
        mContainer.removeAllViewsInLayout();
        if (mAttachments != null) {
            for (int i = 0; i < mAttachments.size(); i++) {
                addAttachment(i, mAttachments.get(i));
            }
        }
    }

    private void addAttachment(final int position, Attachment attachment) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_attachment_item, mContainer, false);
        ImageView avatarImageView = view.findViewById(R.id.avatar_image_view);
        TextView primaryTextView = view.findViewById(R.id.primary_text_view);
        TextView secondaryTextView = view.findViewById(R.id.secondary_text_view);
        ImageView removeImageView = view.findViewById(R.id.remove_image_view);
        Glide.with(this)
                .load(Attachment.getIconResByType(attachment.getType()))
                .into(avatarImageView);
        primaryTextView.setText(attachment.getName());
        if (attachment.getStatus() == Attachment.Status.READY) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mController != null) {
                        if (position >= 0 && position < mAttachments.size()) {
                            Attachment attachment = mAttachments.get(position);
                            mController.onAttachmentClick(attachment);
                        }
                    }
                }
            });
            removeImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mController != null) {
                        if (position >= 0 && position < mAttachments.size()) {
                            Attachment attachment = mAttachments.get(position);
                            mController.onAttachmentDelete(attachment);
                        }
                    }
                }

            });
            secondaryTextView.setText(Utils.readableFileSize(attachment.getSize()));
        } else {
            secondaryTextView.setText(R.string.hint_operation_in_progress);
            removeImageView.setVisibility(INVISIBLE);
        }
        if (!mAllowRemove) {
            removeImageView.setVisibility(GONE);
        }
        mContainer.addView(view);
    }

    private void onAttachmentDelete(View v) {
        if (mController != null && mAllowRemove) {
            int position = (int) v.getTag();
            if (position >= 0 && position < mAttachments.size()) {
                mController.onAttachmentDelete(mAttachments.get(position));
            }
        }
    }

    public interface Controller {

        void onAttachmentClick(Attachment attachment);

        void onAttachmentDelete(Attachment attachment);
    }
}