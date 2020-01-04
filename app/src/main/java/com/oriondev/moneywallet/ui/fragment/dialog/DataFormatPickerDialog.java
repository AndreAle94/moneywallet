package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.DataFormat;
import com.oriondev.moneywallet.ui.adapter.recycler.DataFormatSelectorAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andrea on 20/12/18.
 */

public class DataFormatPickerDialog extends DialogFragment implements DataFormatSelectorAdapter.Controller {

    private static final String SS_DATA_FORMATS = "DataFormatPickerDialog::SavedState::DataFormats";
    private static final String SS_CURRENT_INDEX = "DataFormatPickerDialog::SavedState::CurrentIndex";

    public static DataFormatPickerDialog newInstance() {
        return new DataFormatPickerDialog();
    }

    private DataFormatPickerDialog.Callback mCallback;
    private DataFormat[] mDataFormats;
    private int mIndex = -1;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mDataFormats = (DataFormat[]) savedInstanceState.getSerializable(SS_DATA_FORMATS);
            mIndex = savedInstanceState.getInt(SS_CURRENT_INDEX, -1);
        }
        return ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_data_format_picker_title)
                .adapter(new DataFormatSelectorAdapter(mDataFormats, this), null)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_DATA_FORMATS, mDataFormats);
        outState.putInt(SS_CURRENT_INDEX, mIndex);
    }

    public void setCallback(DataFormatPickerDialog.Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, DataFormat[] dataFormats, int index) {
        mDataFormats = dataFormats;
        mIndex = index;
        show(fragmentManager, tag);
    }

    @Override
    public void onDataFormatSelected(DataFormat dataFormat) {
        mCallback.onDataFormatSelected(dataFormat);
        dismiss();
    }

    @Override
    public boolean isDataFormatSelected(DataFormat dataFormat) {
        return mDataFormats != null && mIndex >= 0 && mIndex < mDataFormats.length && mDataFormats[mIndex] == dataFormat;
    }

    public interface Callback {

        void onDataFormatSelected(DataFormat dataFormat);
    }
}