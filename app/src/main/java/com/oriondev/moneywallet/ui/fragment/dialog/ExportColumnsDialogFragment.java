package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.data.AbstractDataExporter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 21/12/18.
 */
public class ExportColumnsDialogFragment extends DialogFragment {

    private static final String SS_CURRENT_INDICES = "ExportColumnsDialogFragment::SavedState::CurrentIndices";

    private static final int[] COLUMN_NAMES = new int[] {
            R.string.hint_event,
            R.string.hint_people,
            R.string.hint_place,
            R.string.hint_note
    };

    private static final String[] SERVICE_COLUMNS = new String[] {
            AbstractDataExporter.COLUMN_EVENT,
            AbstractDataExporter.COLUMN_PEOPLE,
            AbstractDataExporter.COLUMN_PLACE,
            AbstractDataExporter.COLUMN_NOTE
    };

    public static ExportColumnsDialogFragment newInstance() {
        return new ExportColumnsDialogFragment();
    }

    private Callback mCallback;
    private Integer[] mIndices;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mIndices = (Integer[]) savedInstanceState.getSerializable(SS_CURRENT_INDICES);
        }
        String[] items = new String[COLUMN_NAMES.length];
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            items[i] = getString(COLUMN_NAMES[i]);
        }
        return ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_export_optional_columns_title)
                .items(items)
                .itemsCallbackMultiChoice(mIndices, new MaterialDialog.ListCallbackMultiChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] indices, CharSequence[] text) {
                        mIndices = indices;
                        mCallback.onExportColumnsSelected(mIndices);
                        return false;
                    }

                })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_CURRENT_INDICES, mIndices);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, Integer[] indices) {
        mIndices = indices;
        show(fragmentManager, tag);
    }

    public static String[] getColumnNameFromIndices(Context context, Integer[] indices) {
        if (indices != null) {
            String[] columnNames = new String[indices.length];
            for (int i = 0; i < indices.length; i++) {
                columnNames[i] = context.getString(COLUMN_NAMES[indices[i]]);
            }
            return columnNames;
        }
        return null;
    }

    public static String[] getCursorColumnsFromIndices(Integer[] indices) {
        if (indices != null) {
            String[] columnNames = new String[indices.length];
            for (int i = 0; i < indices.length; i++) {
                columnNames[i] = SERVICE_COLUMNS[indices[i]];
            }
            return columnNames;
        }
        return null;
    }

    public interface Callback {

        void onExportColumnsSelected(Integer[] indices);
    }
}