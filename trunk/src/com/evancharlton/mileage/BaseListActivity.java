
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.Dao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public abstract class BaseListActivity extends ListActivity implements
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
    protected ListView mListView;
    protected LinearLayout mEmptyView;
    private BaseAdapter mAdapter;

    public BaseListActivity() {
        super();
    }

    protected BaseListActivity(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.onCreate(savedInstanceState, R.layout.list);
    }

    protected void onCreate(Bundle savedInstanceState, int layoutResId) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResId);
        mEmptyView = (LinearLayout) findViewById(android.R.id.empty);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initUI();

        mListView = getListView();
        if (mAdapter == null) {
            mAdapter = new SimpleCursorAdapter(this, getListLayout(), getCursor(), getFrom(),
                    getTo());
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);

        setupEmptyView();

        postUI();
    }

    protected final BaseAdapter getAdapter() {
        return mAdapter;
    }

    protected void initUI() {
    }

    protected void postUI() {
    }

    protected void setupEmptyView() {
    }

    protected Cursor getCursor() {
        return managedQuery(getUri(), getProjectionArray(), getSelection(), getSelectionArgs(),
                getSortOrder());
    }

    protected String getSelection() {
        return null;
    }

    protected String[] getSelectionArgs() {
        return null;
    }

    protected String getSortOrder() {
        return null;
    }

    protected String[] getProjectionArray() {
        final String[] from = getFrom();
        final int length = from.length;
        final String[] projection = new String[length + 1];
        projection[0] = BaseColumns._ID;
        for (int i = 0; i < length; i++) {
            projection[1 + i] = from[i];
        }
        return projection;
    }

    protected int[] getTo() {
        return new int[] {
                android.R.id.text1,
                android.R.id.text2
        };
    }

    protected int getListLayout() {
        return android.R.layout.simple_list_item_2;
    }

    protected void loadItem(long id, Class<? extends Activity> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, id);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View row, int position, long id) {
        onItemClick(id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        long id = getAdapter().getItemId(info.position);

        addContextMenuItems(menu, info, id);
    }

    protected Intent createContextMenuIntent(String action, long itemId) {
        Intent i = new Intent(action);
        i.putExtra(BaseColumns._ID, itemId);
        return i;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent = item.getIntent();
        if (intent != null) {
            final long itemId = intent.getLongExtra(BaseColumns._ID, -1);
            if (itemId >= 0) {
                return handleContextMenuSelection(intent, itemId);
            }
        }
        return super.onContextItemSelected(item);
    }

    protected void showDeleteDialog(final Runnable deleteAction) {
        // TODO(3.1) - This dialog doesn't persist through rotations.
        Dialog deleteDialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_title_delete)
                .setMessage(R.string.dialog_message_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAction.run();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        deleteDialog.show();
    }

    protected boolean canDelete(int position) {
        return true;
    }

    protected void addContextMenuItems(ContextMenu menu, AdapterContextMenuInfo info, long id) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.edit).setIntent(
                createContextMenuIntent(Intent.ACTION_EDIT, id));

        if (canDelete(info.position)) {
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.delete).setIntent(
                    createContextMenuIntent(Intent.ACTION_DELETE, id));
        }
    }

    protected boolean handleContextMenuSelection(Intent intent, final long itemId) {
        if (intent.getAction().equals(Intent.ACTION_EDIT)) {
            onItemClick(itemId);
            return true;
        } else if (intent.getAction().equals(Intent.ACTION_DELETE)) {
            showDeleteDialog(new Runnable() {
                @Override
                public void run() {
                    getContentResolver().delete(getUri(), Dao._ID + " = ?", new String[] {
                            String.valueOf(itemId)
                    });
                    itemDeleted(itemId);
                }
            });
            return true;
        }
        return false;
    }

    protected void itemDeleted(long itemId) {
    }

    abstract protected String[] getFrom();

    abstract protected Uri getUri();

    abstract public void onItemClick(long id);
}
