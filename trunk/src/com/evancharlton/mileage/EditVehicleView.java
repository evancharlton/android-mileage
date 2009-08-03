package com.evancharlton.mileage;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.evancharlton.mileage.models.Vehicle;

public class EditVehicleView extends AddVehicleView {
	private Cursor m_vehicleCursor;
	private boolean m_vehicleWasDefault = false;

	private static final int MENU_DELETE = Menu.FIRST + 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadData();
	}

	protected void initUI() {
		super.initUI();

		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				long id = Long.parseLong(getIntent().getData().getLastPathSegment());
				Vehicle vehicle = setData();
				if (vehicle == null) {
					showMessage(false);
					return;
				}
				vehicle.setId(id);
				vehicle.save();
				finish();

				showMessage(true);
			}
		});

		m_vehicleCursor = managedQuery(Vehicle.CONTENT_URI, new String[] {
				Vehicle._ID,
				Vehicle.TITLE
		}, null, null, Vehicle.DEFAULT_SORT_ORDER);
	}

	protected Vehicle setData() {
		Vehicle vehicle = super.setData();
		boolean checked = m_default.isChecked();
		if (m_vehicleWasDefault && checked == false) {
			// remove it from being default, fall back to whatever was last
			// default
			vehicle.setDefault(false);
		} else if (checked) {
			vehicle.setDefault(checked);
		}
		return vehicle;
	}

	private void loadData() {
		long id = Long.parseLong(getIntent().getData().getLastPathSegment());
		Vehicle v = new Vehicle(id);

		m_year.setText(v.getYear());
		m_make.setText(v.getMake());
		m_model.setText(v.getModel());
		m_title.setText(v.getTitle());

		if (m_vehicleCursor.getCount() == 1) {
			m_default.setVisibility(View.GONE);
		} else {
			if (v.isDefault()) {
				m_default.setChecked(true);
				m_vehicleWasDefault = true;
			}
		}
	}

	@Override
	protected void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		if (m_vehicleCursor.getCount() > 1) {
			menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete).setShortcut('1', 'd').setIcon(R.drawable.ic_menu_delete);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				showDialog(DELETE_DIALOG_ID);
				return true;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_vehicle_edit, R.string.help_vehicle_edit);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
