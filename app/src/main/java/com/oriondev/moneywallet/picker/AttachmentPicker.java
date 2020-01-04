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

package com.oriondev.moneywallet.picker;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.webkit.MimeTypeMap;

import com.oriondev.moneywallet.broadcast.LocalAction;
import com.oriondev.moneywallet.model.Attachment;
import com.oriondev.moneywallet.service.AttachmentHandlerIntentService;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by andrea on 27/03/18.
 */
public class AttachmentPicker extends Fragment {

    private static final String SS_EDIT_MODE = "AttachmentPicker::SavedState::EditMode";
    private static final String SS_OLD_ATTACHMENT_LIST = "AttachmentPicker::SavedState::OldAttachmentList";
    private static final String SS_NEW_ATTACHMENT_LIST = "AttachmentPicker::SavedState::NewAttachmentList";
    private static final String SS_DELETED_ATTACHMENT_LIST = "AttachmentPicker::SavedState::DeletedAttachmentList";

    private static final String ARG_EDIT_MODE = "AttachmentPicker::Arguments::EditMode";
    private static final String ARG_OLD_ATTACHMENT_LIST = "AttachmentPicker::Arguments::OldAttachmentList";

    private static final String ALL_FILES = "*/*";
    private static final int REQUEST_CODE_FILE_PICKER = 36347;

    public static AttachmentPicker createPicker(FragmentManager fragmentManager, String tag, ArrayList<Attachment> attachments) {
        AttachmentPicker picker = (AttachmentPicker) fragmentManager.findFragmentByTag(tag);
        if (picker == null) {
            picker = new AttachmentPicker();
            Bundle arguments = new Bundle();
            if (attachments != null) {
                arguments.putBoolean(ARG_EDIT_MODE, true);
                arguments.putParcelableArrayList(ARG_OLD_ATTACHMENT_LIST, attachments);
            } else {
                arguments.putBoolean(ARG_EDIT_MODE, false);
            }
            picker.setArguments(arguments);
            fragmentManager.beginTransaction().add(picker, tag).commit();
        }
        return picker;
    }

    private boolean mEditMode;
    private Controller mController;

    private ArrayList<Attachment> mOldAttachments;
    private ArrayList<Attachment> mNewAttachments;
    private ArrayList<Attachment> mDeletedAttachments;

    private Queue<Uri> mQueuedUris = new ArrayDeque<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mEditMode = savedInstanceState.getBoolean(SS_EDIT_MODE);
            mOldAttachments = savedInstanceState.getParcelableArrayList(SS_OLD_ATTACHMENT_LIST);
            mNewAttachments = savedInstanceState.getParcelableArrayList(SS_NEW_ATTACHMENT_LIST);
            mDeletedAttachments = savedInstanceState.getParcelableArrayList(SS_DELETED_ATTACHMENT_LIST);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mEditMode = arguments.getBoolean(ARG_EDIT_MODE);
                if (mEditMode) {
                    mOldAttachments = arguments.getParcelableArrayList(ARG_OLD_ATTACHMENT_LIST);
                } else {
                    mOldAttachments = new ArrayList<>();
                }
            } else {
                mEditMode = false;
                mOldAttachments = new ArrayList<>();
            }
            mNewAttachments = new ArrayList<>();
            mDeletedAttachments = new ArrayList<>();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!mQueuedUris.isEmpty()) {
            // check if there is some uri queued that should be processed
            while (!mQueuedUris.isEmpty()) {
                Uri queuedUri = mQueuedUris.poll();
                onFileSelected(queuedUri);
            }
        } else {
            fireCallbackSafely();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalAction.ACTION_ATTACHMENT_OP_STARTED);
        intentFilter.addAction(LocalAction.ACTION_ATTACHMENT_OP_FINISHED);
        intentFilter.addAction(LocalAction.ACTION_ATTACHMENT_OP_FAILED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public boolean areAllAttachmentsReady() {
        for (Attachment attachment : mNewAttachments) {
            if (attachment.getStatus() == Attachment.Status.PENDING) {
                return false;
            }
        }
        return true;
    }

    public void remove(Attachment attachment) {
        if (mEditMode && mOldAttachments.contains(attachment)) {
            // simply flag the item as deleted: in this way, if the user
            // decides to cancel the modification of the attachments,
            // they are not definitively lost.
            mOldAttachments.remove(attachment);
            mDeletedAttachments.add(attachment);
        } else {
            // remove the file permanently
            mNewAttachments.remove(attachment);
            deleteAttachmentAsync(attachment);
        }
        fireCallbackSafely();
    }

    public void showPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ALL_FILES);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, REQUEST_CODE_FILE_PICKER);
    }

    public void cleanUp(boolean rollback) {
        if (rollback) {
            for (Attachment attachment : mNewAttachments) {
                deleteAttachmentAsync(attachment);
            }
        } else {
            for (Attachment attachment : mDeletedAttachments) {
                deleteAttachmentAsync(attachment);
            }
        }
    }

    public List<Attachment> getCurrentAttachments() {
        List<Attachment> attachments = new ArrayList<>();
        attachments.addAll(mOldAttachments);
        attachments.addAll(mNewAttachments);
        return attachments;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            if (uri != null) {
                                onFileSelected(uri);
                            }
                        }
                        return;
                    }
                }
                Uri uri = data.getData();
                if (uri != null) {
                    onFileSelected(uri);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addFileFromUri(@NonNull Uri uri) {
        mQueuedUris.add(uri);
    }

    private void onFileSelected(@NonNull Uri uri) {
        Activity activity = getActivity();
        if (activity != null) {
            // retrieve information from the given uri
            Attachment attachment = null;
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                ContentResolver contentResolver = activity.getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        attachment = new Attachment(
                                0L, Attachment.generateFileUID(),
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)),
                                contentResolver.getType(uri),
                                cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)),
                                Attachment.Status.PENDING
                        );
                    }
                    cursor.close();
                }
            } else {
                // this may be caused by an app that has returned an uri that points to a file
                // on the filesystem (e.g. 'file:///storage/emulated/0/...'). in this case, no
                // valid cursor is returned and the attachment is not loaded. we can try to
                // check if the user has already granted the read permission on the external
                // memory and load it as a generic file
                String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                int status = activity.checkCallingOrSelfPermission(permission);
                if (status == PackageManager.PERMISSION_GRANTED) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                        String contentType = "application/octet-stream";
                        if (extension != null) {
                            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                            if (mimeType != null) {
                                contentType = mimeType;
                            }
                        }
                        attachment = new Attachment(0L,
                                Attachment.generateFileUID(),
                                file.getName(),
                                contentType,
                                file.length(),
                                Attachment.Status.PENDING
                        );
                    }
                }
            }
            if (attachment != null) {
                mNewAttachments.add(attachment);
                fireCallbackSafely();
                createAttachmentAsync(uri, attachment);
            }
        }
    }

    private void createAttachmentAsync(Uri uri, Attachment attachment) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, AttachmentHandlerIntentService.class);
            intent.setData(uri);
            intent.putExtra(AttachmentHandlerIntentService.ATTACHMENT, attachment);
            intent.putExtra(AttachmentHandlerIntentService.ACTION, AttachmentHandlerIntentService.ACTION_CREATE);
            activity.startService(intent);
        }
    }

    private void deleteAttachmentAsync(Attachment attachment) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, AttachmentHandlerIntentService.class);
            intent.putExtra(AttachmentHandlerIntentService.ATTACHMENT, attachment);
            intent.putExtra(AttachmentHandlerIntentService.ACTION, AttachmentHandlerIntentService.ACTION_DELETE);
            activity.startService(intent);
        }
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            mController.onAttachmentListChanged(getCurrentAttachments());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SS_EDIT_MODE, mEditMode);
        outState.putParcelableArrayList(SS_OLD_ATTACHMENT_LIST, mOldAttachments);
        outState.putParcelableArrayList(SS_NEW_ATTACHMENT_LIST, mNewAttachments);
        outState.putParcelableArrayList(SS_DELETED_ATTACHMENT_LIST, mDeletedAttachments);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mBroadcastReceiver);
        }
        mController = null;
    }

    public interface Controller {

        void onAttachmentListChanged(List<Attachment> attachments);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                int type = intent.getIntExtra(AttachmentHandlerIntentService.ACTION, 0);
                if (type == AttachmentHandlerIntentService.ACTION_CREATE) {
                    Attachment attachment = intent.getParcelableExtra(AttachmentHandlerIntentService.ATTACHMENT);
                    switch (action) {
                        case LocalAction.ACTION_ATTACHMENT_OP_STARTED:
                            updateAttachmentStatus(attachment, Attachment.Status.PENDING, null);
                            break;
                        case LocalAction.ACTION_ATTACHMENT_OP_FINISHED:
                            updateAttachmentStatus(attachment, Attachment.Status.READY, null);
                            break;
                        case LocalAction.ACTION_ATTACHMENT_OP_FAILED:
                            String error = intent.getStringExtra(AttachmentHandlerIntentService.ERROR);
                            updateAttachmentStatus(attachment, Attachment.Status.PENDING, error);
                            break;
                    }
                }
            }
        }

    };

    private void updateAttachmentStatus(Attachment attachment, Attachment.Status status, String error) {
        for (Attachment attach : mNewAttachments) {
            if (attach.equals(attachment)) {
                attach.setId(attachment.getId());
                attach.setStatus(status, error);
                fireCallbackSafely();
                break;
            }
        }
    }
}