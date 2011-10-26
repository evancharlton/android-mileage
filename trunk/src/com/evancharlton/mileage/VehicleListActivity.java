
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class VehicleListActivity extends BaseListActivity {
    private static final int MENU_TYPES = 1;
    private static final int MENU_CREATE = 2;

    public VehicleListActivity() {
        super();
    }

    protected VehicleListActivity(BaseAdapter adapter) {
        super(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_vehicle).setIcon(
                R.drawable.ic_menu_add);
        menu.add(Menu.NONE, MENU_TYPES, Menu.NONE, R.string.edit_vehicle_types).setIcon(
                R.drawable.ic_menu_edit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_TYPES:
                startActivity(new Intent(this, VehicleTypeListActivity.class));
                return true;
            case MENU_CREATE:
                startActivity(new Intent(this, VehicleActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String[] getFrom() {
        return new String[] {
                Vehicle.TITLE,
                Vehicle.DESCRIPTION
        };
    }

    @Override
    protected Uri getUri() {
        return VehiclesTable.BASE_URI;
    }

    @Override
    public void onItemClick(long id) {
        loadItem(id, VehicleActivity.class);
    }

    @Override
    protected void addContextMenuItems(ContextMenu menu, AdapterContextMenuInfo info, long id) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.set_vehicle_as_default_menu).setIntent(
                createContextMenuIntent(Intent.ACTION_DEFAULT, id));
        super.addContextMenuItems(menu, info, id);
    }

    @Override
    protected boolean handleContextMenuSelection(Intent intent, final long itemId) {
        if (intent.getAction().equals(Intent.ACTION_DEFAULT)) {
            ContentValues values = new ContentValues();
            values.put(Vehicle.DEFAULT_TIME, System.currentTimeMillis());
            Uri uri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, itemId);
            getContentResolver().update(uri, values, null, null);
            Toast.makeText(this, getString(R.string.toast_vehicle_set_as_default),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.handleContextMenuSelection(intent, itemId);
    }

    @Override
    protected boolean canDelete(int position) {
        return getAdapter().getCount() > 1;
    }
}
