package com.oriondev.moneywallet.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
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
import com.oriondev.moneywallet.ui.view.AdvancedRecyclerView;

import java.util.List;

/**
 * Created by andrea on 27/12/18.
 */
public class CategorySortActivity extends SinglePanelSimpleListActivity implements CategorySortCursorAdapter.CategorySortListener {

    public static final String TYPE = "CategorySortActivity::Arguments::CategoryType";

    private Contract.CategoryType mCategoryType;

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected int getActivityTitleRes() {
        return R.string.title_activity_category_sort;
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
        Intent intent = getIntent();
        if (intent != null) {
            mCategoryType = (Contract.CategoryType) intent.getSerializableExtra(TYPE);
        }
        if (mCategoryType == null) {
            mCategoryType = Contract.CategoryType.INCOME;
        }
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
                if (adapter instanceof CategorySortCursorAdapter) {
                    return ((CategorySortCursorAdapter) adapter).moveItem(
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
        return new CategorySortCursorAdapter(this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = DataContentProvider.CONTENT_CATEGORIES;
        String[] projection = new String[] {
                Contract.Category.ID,
                Contract.Category.ICON,
                Contract.Category.NAME,
                Contract.Category.PARENT
        };
        String selection = Contract.Category.TYPE + " = ? AND " + Contract.Category.PARENT + " IS NULL";
        String[] selectionArgs = new String[] {String.valueOf(mCategoryType.getValue())};
        String sortOrder = Contract.Category.GROUP_INDEX + " ASC, " + Contract.Category.GROUP_NAME +
                " ASC, " + Contract.Category.GROUP_ID + " ASC, " + Contract.Category.PARENT +
                " IS NULL DESC, " + Contract.Category.NAME + " ASC";
        return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onCategoryDragStarted(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void saveChanges() {
        Uri baseUri = DataContentProvider.CONTENT_CATEGORIES;
        ContentResolver contentResolver = getContentResolver();
        RecyclerView.Adapter adapter = getAdvancedRecyclerView().getRecyclerView().getAdapter();
        if (adapter instanceof CategorySortCursorAdapter) {
            List<Long> categoryIdsList = ((CategorySortCursorAdapter) adapter).getSortedCategoryIds();
            for (int i = 0; i < categoryIdsList.size(); i++) {
                Long categoryId = categoryIdsList.get(i);
                Uri uri = ContentUris.withAppendedId(baseUri, categoryId);
                ContentValues contentValues = new ContentValues();
                contentValues.put(Contract.Category.INDEX, i + 1);
                contentResolver.update(uri, contentValues, null, null);
            }
        }
        setResult(Activity.RESULT_OK);
        finish();
    }
}