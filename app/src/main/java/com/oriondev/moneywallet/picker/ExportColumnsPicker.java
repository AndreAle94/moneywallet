package com.oriondev.moneywallet.picker;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.oriondev.moneywallet.ui.fragment.dialog.ExportColumnsDialogFragment;

/**
 * Created by andrea on 21/12/18.
 */
public class ExportColumnsPicker extends Fragment implements ExportColumnsDialogFragment.Callback {

    private static final String SS_CURRENT_INDICES = "ExportColumnsPicker::SavedState::CurrentIndices";

    private Controller mController;

    private Integer[] mCurrentIndices;

    private ExportColumnsDialogFragment mColumnsPickerDialog;

    public static ExportColumnsPicker createPicker(FragmentManager fragmentManager, String tag) {
        ExportColumnsPicker exportColumnsPicker = (ExportColumnsPicker) fragmentManager.findFragmentByTag(tag);
        if (exportColumnsPicker == null) {
            exportColumnsPicker = new ExportColumnsPicker();
            fragmentManager.beginTransaction().add(exportColumnsPicker, tag).commit();
        }
        return exportColumnsPicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Controller) {
            mController = (Controller) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentIndices = (Integer[]) savedInstanceState.getSerializable(SS_CURRENT_INDICES);
        } else {
            mCurrentIndices = new Integer[0];
        }
        mColumnsPickerDialog = (ExportColumnsDialogFragment) getChildFragmentManager().findFragmentByTag(getDialogTag());
        if (mColumnsPickerDialog == null) {
            mColumnsPickerDialog = ExportColumnsDialogFragment.newInstance();
        }
        mColumnsPickerDialog.setCallback(this);
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    private void fireCallbackSafely() {
        if (mController != null) {
            String[] columnNames = ExportColumnsDialogFragment.getColumnNameFromIndices(getActivity(), mCurrentIndices);
            mController.onExportColumnsChanged(getTag(), columnNames);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SS_CURRENT_INDICES, mCurrentIndices);
    }

    public String[] getCurrentServiceColumns() {
        return ExportColumnsDialogFragment.getCursorColumnsFromIndices(mCurrentIndices);
    }

    public void showPicker() {
        mColumnsPickerDialog.showPicker(getChildFragmentManager(), getDialogTag(), mCurrentIndices);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onExportColumnsSelected(Integer[] indices) {
        mCurrentIndices = indices;
        fireCallbackSafely();
    }

    public interface Controller {

        void onExportColumnsChanged(String tag, String[] columns);
    }
}