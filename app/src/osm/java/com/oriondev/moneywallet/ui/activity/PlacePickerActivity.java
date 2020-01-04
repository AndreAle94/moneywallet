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

package com.oriondev.moneywallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Coordinates;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.view.MapViewWrapper;

/**
 * Created by andrea on 02/11/18.
 */
public class PlacePickerActivity extends SinglePanelActivity {

    public static final String RESULT_PLACE = "PlacePickerActivity::Result::Place";

    private MapViewWrapper mMapView;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_place_picker, parent, true);
        mMapView = new MapViewWrapper(view.findViewById(R.id.map_view));
        mMapView.onCreate(savedInstanceState);
        mMapView.setMinZoomLevel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_map;
    }

    @MenuRes
    protected int onInflateMenu() {
        return R.menu.menu_select_place;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_place:
                if (mMapView.isMapReady()) {
                    Coordinates coordinates = mMapView.getCenterCoordinates();
                    Place place = new Place(0L, null, null, null, coordinates);
                    Intent intent = new Intent();
                    intent.putExtra(RESULT_PLACE, place);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
        return false;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }
}