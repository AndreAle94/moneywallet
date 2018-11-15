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

package com.oriondev.moneywallet.ui.fragment.secondary;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Coordinates;
import com.oriondev.moneywallet.model.Icon;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditItemActivity;
import com.oriondev.moneywallet.ui.activity.NewEditPlaceActivity;
import com.oriondev.moneywallet.ui.activity.TransactionListActivity;
import com.oriondev.moneywallet.ui.fragment.base.SecondaryPanelFragment;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.view.MapViewWrapper;

import java.util.Locale;

/**
 * Created by andrea on 03/04/18.
 */
public class PlaceItemFragment extends SecondaryPanelFragment implements LoaderManager.LoaderCallbacks<Cursor>, MapViewWrapper.OnMapLoadedCallback {

    private static final int PLACE_LOADER_ID = 54363;

    private View mProgressLayout;
    private View mMainLayout;

    private ImageView mAvatarImageView;
    private TextView mNameTextView;
    private TextView mAddressTextView;
    private CardView mMapCardView;
    private MapViewWrapper mMapView;

    private Coordinates mCoordinates;

    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_show_icon_name_item, parent, true);
        mAvatarImageView = view.findViewById(R.id.avatar_image_view);
        mNameTextView = view.findViewById(R.id.name_text_view);
    }

    @Override
    protected void onCreateBodyView(LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_show_place_item, parent, true);
        mProgressLayout = view.findViewById(R.id.secondary_panel_progress_wheel);
        mMainLayout = view.findViewById(R.id.secondary_panel_layout);
        mAddressTextView = view.findViewById(R.id.address_text_view);
        mMapCardView = view.findViewById(R.id.map_card_view);
        mMapView = new MapViewWrapper(view.findViewById(R.id.map_view));
        view.findViewById(R.id.open_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCoordinates != null) {
                    try {
                        Uri uri = Uri.parse(String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f", mCoordinates.getLatitude(), mCoordinates.getLongitude()));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            ThemedDialog.buildMaterialDialog(activity)
                                    .title(R.string.title_error)
                                    .content(R.string.message_error_activity_not_found)
                                    .positiveText(android.R.string.ok)
                                    .show();
                        }
                    }
                }
            }

        });
        mMapView.onCreate(savedInstanceState);
        mMapView.loadMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_fragment_item_place);
    }

    @Override
    protected int onInflateMenu() {
        return R.menu.menu_list_edit_delete_item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_transaction_list:
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(TransactionListActivity.PLACE_ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_edit_item:
                intent = new Intent(getActivity(), NewEditPlaceActivity.class);
                intent.putExtra(NewEditItemActivity.MODE, NewEditItemActivity.Mode.EDIT_ITEM);
                intent.putExtra(NewEditItemActivity.ID, getItemId());
                startActivity(intent);
                break;
            case R.id.action_delete_item:
                showDeleteDialog(getActivity());
                break;
        }
        return false;
    }

    private void showDeleteDialog(Context context) {
        ThemedDialog.buildMaterialDialog(context)
                .title(R.string.title_warning)
                .content(R.string.message_delete_place)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PLACES, getItemId());
                            ContentResolver contentResolver = activity.getContentResolver();
                            contentResolver.delete(uri, null, null);
                            navigateBackSafely();
                            showItemId(0L);
                        }
                    }

                })
                .show();
    }

    @Override
    protected void onShowItemId(long itemId) {
        setLoadingScreen(true);
        getLoaderManager().restartLoader(PLACE_LOADER_ID, null, this);
    }

    private void setLoadingScreen(boolean loading) {
        if (loading) {
            mAvatarImageView.setImageDrawable(null);
            mNameTextView.setText(null);
            mCoordinates = null;
            mProgressLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_PLACES, getItemId());
            String[] projection = new String[] {
                    Contract.Place.NAME,
                    Contract.Place.ICON,
                    Contract.Place.ADDRESS,
                    Contract.Place.LATITUDE,
                    Contract.Place.LONGITUDE
            };
            return new CursorLoader(getActivity(), uri, projection, null, null, null);
        }
        throw new IllegalStateException("Parent activity is null");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            Icon icon = IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Place.ICON)));
            IconLoader.loadInto(icon, mAvatarImageView);
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(Contract.Place.NAME)));
            String address = cursor.getString(cursor.getColumnIndex(Contract.Place.ADDRESS));
            if (!TextUtils.isEmpty(address)) {
                mAddressTextView.setText(address);
            } else {
                mAddressTextView.setText(R.string.hint_address_unknown);
            }
            if (!cursor.isNull(cursor.getColumnIndex(Contract.Place.LATITUDE)) && !cursor.isNull(cursor.getColumnIndex(Contract.Place.LONGITUDE))) {
                mCoordinates = new Coordinates(
                        cursor.getDouble(cursor.getColumnIndex(Contract.Place.LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(Contract.Place.LONGITUDE))
                );
            }
            onCoordinatesChanged();
        } else {
            showItemId(0L);
        }
        setLoadingScreen(false);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // nothing to release
    }

    private void onCoordinatesChanged() {
        if (mCoordinates != null && mMapView.isMapReady()) {
            mMapView.addCoordinates(mCoordinates);
            mMapCardView.setVisibility(View.VISIBLE);
        } else {
            if (mMapView.isMapReady()) {
                mMapView.clear();
            }
            mMapCardView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady() {
        mMapView.disableMapInteractions();
        onCoordinatesChanged();
    }
}