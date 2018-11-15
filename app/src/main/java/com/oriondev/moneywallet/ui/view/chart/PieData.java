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

package com.oriondev.moneywallet.ui.view.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 14/08/18.
 */
public class PieData {

    private final List<PieSlice> mPieSlices;

    public PieData() {
        mPieSlices = new ArrayList<>();
    }

    public PieData(List<PieSlice> sliceList) {
        mPieSlices = new ArrayList<>(sliceList);
    }

    public PieSlice get(int index) {
        return mPieSlices.get(index);
    }

    public boolean add(PieSlice slice) {
        return mPieSlices.add(slice);
    }

    public PieSlice remove(int index) {
        return mPieSlices.remove(index);
    }

    public List<PieSlice> getAllSlices() {
        return mPieSlices;
    }

    public int size() {
        return mPieSlices.size();
    }
}