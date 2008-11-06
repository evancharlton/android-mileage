package com.evancharlton.mileage;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class VehiclesView extends ListActivity {

	public static final String TAG = "VehiclesList";
	public static final int MENU_ADD = Menu.FIRST;

	private static final String[] PROJECTIONS = new String[] {
			Vehicles._ID,
			Vehicles.TITLE
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Vehicles.CONTENT_URI);
		}

		getListView().setOnCreateContextMenuListener(this);

		Cursor c = managedQuery(intent.getData(), PROJECTIONS, null, null, Vehicles.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.vehicles, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_ADD, 0, R.string.vehicle_add).setShortcut('1', 'v').setIcon(R.drawable.add_vehicle_i);
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
}
