package com.oriondev.moneywallet.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.base.SinglePanelSimpleListActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.AbstractCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.CategorySortCursorAdapter;
import com.oriondev.moneywallet.ui.adapter.recycler.WalletSortCursorAdapter;
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

import java.util.List;

/**
 * Created by andrea on 05/01/19.
 */

public class WalletSortActivity extends SinglePanelSimpleListActivity implements WalletSortCursorAdapter.WalletSortListener {

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_wallet_sort;
    }

    @MenuRes
    @Override
    protected int onInflateMenu() {
        return R.menu.menu_save_changes;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_changes:
                saveChanges();
                break;
        }
        return false;
    }

    @Override
    protected boolean isFloatingActionButtonEnabled() {
        // floating action button is not used here
        return false;
    }

    @Override
    protected void onPrepareRecyclerView(AdvancedRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyText(R.string.message_no_category_found);
        recyclerView.setEnabled(false);
        // setup item touch helper and attach it to the recycler view
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (adapter instanceof WalletSortCursorAdapter) {
                    return ((WalletSortCursorAdapter) adapter).moveItem(
                            viewHolder.getAdapterPosition(),
                            target.getAdapterPosition()
                    );
                }
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // not used here, only drag and drop is handled
            }

            @Override
            public boolean isLongPressDragEnabled() {
                // disable long press to drag: the only way to drag the categories is to
                // click and drag the appropriate action image view at the end of it
                return false;
            }

        });
        mItemTouchHelper.attachToRecyclerView(recyclerView.getRecyclerView());
    }

    @Override
    protected AbstractCursorAdapter onCreateAdapter() {
        return new WalletSortCursorAdapter(this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri uri = DataContentProvider.CONTENT_WALLETS;
        String[] projection = new String[] {
                Contract.Wallet.ID,
                Contract.Wallet.NAME,
                Contract.Wallet.ICON
        };
        String sortOrder = Contract.Wallet.INDEX + " ASC, " + Contract.Wallet.NAME + " ASC";
        return new CursorLoader(this, uri, projection, null, null, sortOrder);
    }

    @Override
    public void onWalletDragStarted(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void saveChanges() {
        Uri baseUri = DataContentProvider.CONTENT_WALLETS;
        ContentResolver contentResolver = getContentResolver();
        RecyclerView.Adapter adapter = getAdvancedRecyclerView().getRecyclerView().getAdapter();
        if (adapter instanceof WalletSortCursorAdapter) {
            List<Long> walletIdsList = ((WalletSortCursorAdapter) adapter).getSortedCategoryIds();
            for (int i = 0; i < walletIdsList.size(); i++) {
                Long walletId = walletIdsList.get(i);
                Uri uri = ContentUris.withAppendedId(baseUri, walletId);
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.Wallet.INDEX, i + 1);
                contentResolver.update(uri, contentValues, null, null);
            }
        }
        setResult(Activity.RESULT_OK);
        finish();
    }
}