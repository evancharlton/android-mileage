package com.evancharlton.mileage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.evancharlton.mileage.models.Vehicle;

public class EditVehicleView extends AddVehicleView {
	private AlertDialog m_deleteDialog;
	private Cursor m_vehicleCursor;
	private boolean m_vehicleWasDefault = false;

	private static final int DELETE_DIALOG_ID = Menu.FIRST;
	private static final int MENU_DELETE = Menu.FIRST + 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadData();
	}

	protected void initUI() {
		super.initUI();

		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Vehicle vehicle = setData();
				if (vehicle == null) {
					return;
				}
				vehicle.save();
				finish();

				// TODO: show Toast
			}
		});

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		m_vehicleCursor = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID,
				Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);
	}

	protected Vehicle setData() {
		Vehicle vehicle = super.setData();
		if (m_vehicleWasDefault && m_default.isChecked() == false) {
			// remove it from being default, fall back to whatever was last
			// default
			vehicle.setDefault(false);
		}
		return vehicle;
	}

	private void loadData() {
		Intent data = getIntent();

		// load the data
		String[] projections = new String[] {
				Vehicles._ID,
				Vehicles.YEAR,
				Vehicles.MAKE,
				Vehicles.MODEL,
				Vehicles.TITLE
		};

		Cursor c = managedQuery(data.getData(), projections, null, null, null);
		c.moveToFirst();

		m_year.setText(c.getString(1));
		m_make.setText(c.getString(2));
		m_model.setText(c.getString(3));
		m_title.setText(c.getString(4));

		if (m_vehicleCursor.getCount() == 1) {
			m_default.setVisibility(View.GONE);
		} else {
			m_vehicleCursor.moveToFirst();
			if (m_vehicleCursor.getLong(0) == c.getLong(0)) {
				m_default.setChecked(true);
				m_vehicleWasDefault = true;
			}
		}
	}

	private void delete() {
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

	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DELETE_DIALOG_ID:
				return m_deleteDialog;
		}
		return null;
	}

	private DialogInterface.OnClickListener m_deleteListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if (which == Dialog.BUTTON1) {
				delete();
				finish();
			}
		}
	};
}
