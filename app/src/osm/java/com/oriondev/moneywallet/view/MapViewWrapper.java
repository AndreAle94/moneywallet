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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Coordinates;
import com.oriondev.moneywallet.model.Place;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.util.constants.OverlayConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrea on 16/10/18.
 */

public class MapViewWrapper {

    private static final String TILE_CACHE_FOLDER = "osm_tiles";

    private static final double DEFAULT_MIN_ZOOM_LEVEL = 4d;
    private static final double DEFAULT_ZOOM_LEVEL = 14d;
    private static final double GLOBAL_ZOOM_LEVEL = 8d;

    private final MapView mMapView;

    private OnInfoWindowClickListener mInfoWindowClickListener;

    public MapViewWrapper(View view) {
        mMapView = (MapView) view;
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.setMinZoomLevel(DEFAULT_MIN_ZOOM_LEVEL);
        mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
        setupConfiguration(mMapView.getContext());
    }

    private void setupConfiguration(Context context) {
        IConfigurationProvider configuration = Configuration.getInstance();
        // to prevent using a default user agent, we can set it to the current package name
        configuration.setUserAgentValue(context.getPackageName());
        // fix for empty map caused by a read-only access to the file system: to overcame this issue
        // we can use a temp directory inside the cache folder of the application
        File cacheFolder = context.getExternalCacheDir();
        if (cacheFolder != null) {
            // we have a reference to the global cache folder of the application, maybe it's better
            // to create a sub folder for caching tiles
            File tileCache = new File(cacheFolder, TILE_CACHE_FOLDER);
            if (tileCache.exists() || tileCache.mkdir()) {
                configuration.setOsmdroidTileCache(tileCache);
            }
        }
    }

    private void setupCopyrightOverlay() {
        // OpenStreetMap requires that you add “© OpenStreetMap contributors” to the map
        mMapView.getOverlayManager().add(0, new CopyrightOverlay(mMapView.getContext()));
    }

    public void onCreate(Bundle savedInstanceState) {
        // here we can initialize the copyright overlay
        setupCopyrightOverlay();
    }

    public void onStart() {
        // do nothing
    }

    public void onResume() {
        mMapView.onResume();
    }

    public void onPause() {
        mMapView.onPause();
    }

    public void onStop() {
        // do nothing
    }

    public void onDestroy() {
        // do nothing
    }

    public void onSaveInstanceState(Bundle outState) {
        // do nothing
    }

    public void onLowMemory() {
        // do nothing
    }

    public void loadMapAsync(OnMapLoadedCallback callback) {
        // it does not require to load anything in background
        // so we can just fire the callback immediately
        if (callback != null) {
            callback.onMapReady();
        }
    }

    public boolean isMapReady() {
        return mMapView != null;
    }

    public void disableMapInteractions() {
        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private Drawable getMarkerIcon() {
        return mMapView.getContext().getResources().getDrawable(R.drawable.ic_osm_marker);
    }

    public void addPlaces(List<Place> places) {
        if (mMapView != null && places != null && !places.isEmpty()) {
            int pointCount = 0;
            double centerLatitude = 0;
            double centerLongitude = 0;
            List<OverlayItem> items = new ArrayList<>();
            for (Place place : places) {
                if (place.hasCoordinates()) {
                    Coordinates coordinates = place.getCoordinates();
                    String identifier = String.valueOf(place.getId());
                    String name = place.getName();
                    String description = place.getAddress();
                    GeoPoint geoPoint = new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude());
                    items.add(new OverlayItem(identifier, name, description, geoPoint));
                    pointCount += 1;
                    centerLatitude += coordinates.getLatitude();
                    centerLongitude += coordinates.getLongitude();
                }
            }
            ItemizedOverlayWithFocus<OverlayItem> overlay = new ItemizedOverlayWithFocus<>(items, getMarkerIcon(), getMarkerIcon(), OverlayConstants.NOT_SET, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    if (mInfoWindowClickListener != null && item.getUid() != null) {
                        long parsedId = Long.parseLong(item.getUid());
                        mInfoWindowClickListener.onInfoWindowClick(parsedId);
                    }
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }

            }, mMapView.getContext());
            overlay.setFocusItemsOnTap(true);
            overlay.setMarkerTitleForegroundColor(Color.BLACK);
            overlay.setMarkerDescriptionForegroundColor(Color.BLACK);
            overlay.setMarkerBackgroundColor(Color.WHITE);

            overlay.setDescriptionBoxPadding(10);
            overlay.setDescriptionBoxCornerWidth(10);

            mMapView.getOverlays().add(overlay);
            // setup the map-view to better handle multiple locations
            if (pointCount > 0) {
                centerLatitude = centerLatitude / pointCount;
                centerLongitude = centerLongitude / pointCount;
                GeoPoint centerPoint = new GeoPoint(centerLatitude, centerLongitude);
                mMapView.getController().setCenter(centerPoint);
            }
            mMapView.getController().setZoom(GLOBAL_ZOOM_LEVEL);
        }
    }

    public void addCoordinates(Coordinates coordinates) {
        if (mMapView != null && coordinates != null) {
            GeoPoint geoPoint = new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude());
            List<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("", "", geoPoint));
            ItemizedOverlayWithFocus<OverlayItem> overlay = new ItemizedOverlayWithFocus<>(items, getMarkerIcon(), null, OverlayConstants.NOT_SET, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    return false;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }

            }, mMapView.getContext());
            overlay.setFocusItemsOnTap(true);
            mMapView.getOverlays().add(overlay);
            // setup map-view using current point as center
            mMapView.getController().setCenter(geoPoint);
            mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
        }
    }

    public void clear() {
        mMapView.getOverlays().clear();
        setupCopyrightOverlay();
    }

    public void setOnInfoClickListener(OnInfoWindowClickListener callback) {
        mInfoWindowClickListener = callback;
    }

    public Coordinates getCenterCoordinates() {
        IGeoPoint geoPoint = mMapView.getMapCenter();
        return new Coordinates(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public void setMinZoomLevel() {
        mMapView.getController().setZoom(DEFAULT_MIN_ZOOM_LEVEL);
    }

    public interface OnMapLoadedCallback {

        void onMapReady();
    }

    public interface OnInfoWindowClickListener {

        void onInfoWindowClick(long placeId);
    }
}