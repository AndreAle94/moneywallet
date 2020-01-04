package com.oriondev.moneywallet.ui.adapter.recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.DataFormat;

/**
 * Created by andrea on 20/12/18.
 */
public class DataFormatSelectorAdapter extends RecyclerView.Adapter<DataFormatSelectorAdapter.ViewHolder> {

    private final DataFormat[] mDataFormats;
    private final Controller mController;

    public DataFormatSelectorAdapter(DataFormat[] dataFormats, Controller controller) {
        mDataFormats = dataFormats;
        mController = controller;
    }

    @NonNull
    @Override
    public DataFormatSelectorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.adapter_data_format_selector_item, parent, false);
        return new DataFormatSelectorAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DataFormatSelectorAdapter.ViewHolder holder, int position) {
        DataFormat dataFormat = mDataFormats[position];
        switch (dataFormat) {
            case CSV:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_file_csv_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_data_format_csv);
                break;
            case XLS:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_file_xls_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_data_format_xls);
                break;
            case PDF:
                holder.mAvatarImageView.setImageResource(R.drawable.ic_file_pdf_24dp);
                holder.mPrimaryTextView.setText(R.string.hint_data_format_pdf);
                break;
        }
        boolean selected = mController.isDataFormatSelected(dataFormat);
        holder.mSelectorImageView.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mDataFormats != null ? mDataFormats.length : 0;
    }

    /*package-local*/ class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAvatarImageView;
        private TextView mPrimaryTextView;
        private ImageView mSelectorImageView;

        /*package-local*/ ViewHolder(View itemView) {
            super(itemView);
            mAvatarImageView = itemView.findViewById(R.id.avatar_image_view);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSelectorImageView = itemView.findViewById(R.id.selector_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {;
                mController.onDataFormatSelected(mDataFormats[getAdapterPosition()]);
            }
        }
    }

    public interface Controller {

        void onDataFormatSelected(DataFormat dataFormat);

        boolean isDataFormatSelected(DataFormat dataFormat);
    }
}