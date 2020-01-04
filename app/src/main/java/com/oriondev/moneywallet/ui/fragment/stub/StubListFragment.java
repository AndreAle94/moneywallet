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

package com.oriondev.moneywallet.ui.fragment.stub;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 24/01/18.
 */
public class StubListFragment extends Fragment {

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        ArrayAdapter adapter = new ArrayAdapter();
        for (int i = 0; i < 100; i++) {
            adapter.add("Item " + i);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return recyclerView;
    }

    protected static class ArrayAdapter extends RecyclerView.Adapter<ArrayAdapter.StubViewHolder> {

        private List<String> mData;

        /*package-local*/ ArrayAdapter() {
            mData = new ArrayList<>();
        }

        public void add(String item) {
            mData.add(item);
        }

        @Override
        public StubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new StubViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StubViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        /*package-local*/ class StubViewHolder extends RecyclerView.ViewHolder {

            /*package-local*/ TextView mTextView;

            /*package-local*/ StubViewHolder(View itemView) {
                super(itemView);
                mTextView = itemView.findViewById(android.R.id.text1);
                itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                    }

                });
            }
        }
    }
}