
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.VehicleType;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

public class VehicleTypeListActivity extends BaseListActivity {
    private static final int MENU_CREATE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_vehicle_type).setIcon(
                R.drawable.ic_menu_add);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CREATE:
                startActivity(new Intent(this, VehicleTypeActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String[] getFrom() {
        return new String[] {
                VehicleType.TITLE,
                VehicleType.DESCRIPTION
        };
    }

    @Override
    protected Uri getUri() {
        return Uri.withAppendedPath(FillUpsProvider.BASE_URI, VehicleTypesTable.URI);
    }

    @Override
    public void onItemClick(long id) {
        loadItem(id, VehicleTypeActivity.class);
    }

    @Override
    protected boolean canDelete(int position) {
        return getAdapter().getCount() > 1;
    }
}
