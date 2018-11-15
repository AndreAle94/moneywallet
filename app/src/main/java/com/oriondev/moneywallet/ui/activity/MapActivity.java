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

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelActivity;
import com.oriondev.moneywallet.view.MapViewWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 04/04/18.
 */
public class MapActivity extends SinglePanelActivity implements LoaderManager.LoaderCallbacks<Cursor>, MapViewWrapper.OnMapLoadedCallback, MapViewWrapper.OnInfoWindowClickListener {

    private static final int LOADER_PLACES = 23748;

    private MapViewWrapper mMapView;

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_map, parent, true);
        mMapView = new MapViewWrapper(view.findViewById(R.id.map_view));
        mMapView.onCreate(savedInstanceState);
        mMapView.loadMapAsync(this);
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

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = DataContentProvider.CONTENT_PLACES;
        String[] projection = new String[] {
                Contract.Place.ID,
                Contract.Place.NAME,
                Contract.Place.ICON,
                Contract.Place.ADDRESS,
                Contract.Place.LATITUDE,
                Contract.Place.LONGITUDE
        };
        String selection = Contract.Place.LATITUDE + " IS NOT NULL AND " + Contract.Place.LONGITUDE + " IS NOT NULL";
        return new CursorLoader(this, uri, projection, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (mMapView.isMapReady() && cursor.moveToFirst()) {
                List<Place> placeList = new ArrayList<>();
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(Contract.Place.ID));
                    String name = cursor.getString(cursor.getColumnIndex(Contract.Place.NAME));
                    String address = cursor.getString(cursor.getColumnIndex(Contract.Place.ADDRESS));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(Contract.Place.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(Contract.Place.LONGITUDE));
                    placeList.add(new Place(id, name, null, address, latitude, longitude));
                } while (cursor.moveToNext());
                // display the list of places on map
                mMapView.addPlaces(placeList);
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to release
    }

    @Override
    public void onMapReady() {
        mMapView.setOnInfoClickListener(this);
        getLoaderManager().restartLoader(LOADER_PLACES, null, this);
    }

    @Override
    public void onInfoWindowClick(long placeId) {
        Intent intent = new Intent(this, TransactionListActivity.class);
        intent.putExtra(TransactionListActivity.PLACE_ID, placeId);
        startActivity(intent);
    }
}