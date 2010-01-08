package com.evancharlton.mileage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.models.Vehicle;

public class VehiclesView extends DeleteActivity implements View.OnCreateContextMenuListener {
	public static final String TAG = "VehiclesList";
	public static final int MENU_ADD = Menu.FIRST;
	public static final int MENU_DEFAULT = Menu.FIRST;
	public static final int MENU_EDIT = Menu.FIRST + 1;
	public static final int MENU_DELETE = Menu.FIRST + 2;

	private long m_deleteId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Vehicle.CONTENT_URI);
		}

		setContentView(R.layout.vehicles_list);
		getListView().setOnCreateContextMenuListener(this);

		Cursor c = managedQuery(intent.getData(), Vehicle.getProjection(), null, null, Vehicle.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.vehicles, c, new String[] {
			Vehicle.TITLE
		}, new int[] {
			android.R.id.text1
		});
		adapter.setViewBinder(new VehicleBinder());
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_ADD, 0, R.string.vehicle_add).setShortcut('1', 'v').setIcon(R.drawable.ic_menu_add);
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				Intent i = new Intent();
				i.setClass(VehiclesView.this, AddVehicleView.class);
				startActivity(i);
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_vehicles, R.string.help_vehicles);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(VehiclesView.this, EditVehicleView.class);
		startActivity(intent);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
		menu.setHeaderTitle(R.string.operations);
		int count = getListView().getCount();
		menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.edit);
		if (count >= 2) {
			menu.add(Menu.NONE, MENU_DEFAULT, Menu.NONE, R.string.make_default_short);
			menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = null;
		try {
			info = (AdapterContextMenuInfo) item.getMenuInfo();
			long id = getListAdapter().getItemId(info.position);
			switch (item.getItemId()) {
				case MENU_DELETE:
					m_deleteId = id;
					showDialog(DELETE_DIALOG_ID);
					return true;
				case MENU_EDIT:
					onListItemClick(getListView(), info.targetView, info.position, id);
					return true;
				case MENU_DEFAULT:
					Uri data = ContentUris.withAppendedId(Vehicle.CONTENT_URI, id);
					ContentValues values = new ContentValues();
					values.put(Vehicle.DEFAULT, System.currentTimeMillis());
					getContentResolver().update(data, values, null, null);
					return true;
			}
		} catch (ClassCastException e) {
			// fail gracefully?
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void delete() {
		Uri uri = ContentUris.withAppendedId(Vehicle.CONTENT_URI, m_deleteId);
		getContentResolver().delete(uri, null, null);
	}
}
