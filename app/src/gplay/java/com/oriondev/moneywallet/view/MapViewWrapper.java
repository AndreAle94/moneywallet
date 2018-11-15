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

package com.oriondev.moneywallet.view;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oriondev.moneywallet.model.Coordinates;
import com.oriondev.moneywallet.model.Place;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrea on 16/10/18.
 */

public class MapViewWrapper implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final float DEFAULT_ZOOM_LEVEL = 14;
    private static final float GLOBAL_ZOOM_LEVEL = 8;

    private final MapView mMapView;

    private OnMapLoadedCallback mMapLoadedCallback;
    private OnInfoWindowClickListener mInfoWindowClickCallback;

    private GoogleMap mGoogleMap;

    private Map<String, Long> mMarkerCache = new HashMap<>();

    public MapViewWrapper(View view) {
        mMapView = (MapView) view;
    }

    public void onCreate(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
    }

    public void onStart() {
        mMapView.onStart();
    }

    public void onResume() {
        mMapView.onResume();
    }

    public void onPause() {
        mMapView.onPause();
    }

    public void onStop() {
        mMapView.onStop();
    }

    public void onDestroy() {
        mMapView.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
    }

    public void onLowMemory() {
        mMapView.onLowMemory();
    }

    public void loadMapAsync(OnMapLoadedCallback callback) {
        mMapLoadedCallback = callback;
        mMapView.getMapAsync(this);
    }

    public boolean isMapReady() {
        return mGoogleMap != null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mMapLoadedCallback != null) {
            mMapLoadedCallback.onMapReady();
        }
    }

    public void disableMapInteractions() {
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
    }

    public void addPlaces(List<Place> places) {
        if (mGoogleMap != null && places != null && !places.isEmpty()) {
            int pointCount = 0;
            double centerLatitude = 0;
            double centerLongitude = 0;
            for (Place place : places) {
                if (place.hasCoordinates()) {
                    Coordinates coordinates = place.getCoordinates();
                    String identifier = String.valueOf(place.getId());
                    String name = place.getName();
                    String description = place.getAddress();
                    MarkerOptions markerOptions = new MarkerOptions()
                            .title(place.getName())
                            .snippet(place.getAddress())
                            .position(new LatLng(coordinates.getLatitude(), coordinates.getLongitude()));
                    Marker marker = mGoogleMap.addMarker(markerOptions);
                    mMarkerCache.put(marker.getId(), place.getId());
                    pointCount += 1;
                    centerLatitude += coordinates.getLatitude();
                    centerLongitude += coordinates.getLongitude();
                }
            }
            // setup the map-view to better handle multiple locations
            if (pointCount > 0) {
                centerLatitude = centerLatitude / pointCount;
                centerLongitude = centerLongitude / pointCount;
                LatLng centerPoint = new LatLng(centerLatitude, centerLongitude);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, GLOBAL_ZOOM_LEVEL));
            }
        }
    }

    public void addCoordinates(Coordinates coordinates) {
        if (mGoogleMap != null && coordinates != null) {
            LatLng _coordinates = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions().position(_coordinates));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(_coordinates, DEFAULT_ZOOM_LEVEL));
        }
    }

    public void clear() {
        mGoogleMap.clear();
        mMarkerCache.clear();
    }

    public void setOnInfoClickListener(OnInfoWindowClickListener callback) {
        mInfoWindowClickCallback = callback;
        if (mGoogleMap != null) {
            mGoogleMap.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mInfoWindowClickCallback != null) {
            String markerId = marker.getId();
            if (mMarkerCache.containsKey(markerId)) {
                Long placeId = mMarkerCache.get(markerId);
                mInfoWindowClickCallback.onInfoWindowClick(placeId);
            }
        }
    }

    public interface OnMapLoadedCallback {

        void onMapReady();
    }

    public interface OnInfoWindowClickListener {

        void onInfoWindowClick(long placeId);
    }
}