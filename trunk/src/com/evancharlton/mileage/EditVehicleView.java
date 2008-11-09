package com.evancharlton.mileage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

public class EditVehicleView extends Activity {
	private EditText m_year;
	private EditText m_make;
	private EditText m_model;
	private EditText m_title;
	private Button m_save;
	private AlertDialog m_deleteDialog;
	private SimpleCursorAdapter m_vehicleAdapter;
	private CheckBox m_default;

	private static final int DELETE_DIALOG_ID = Menu.FIRST;
	private static final int MENU_DELETE = Menu.FIRST + 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_vehicle);

		initUI();
		loadData();
	}

	private void initUI() {
		m_year = (EditText) findViewById(R.id.vehicle_edit_year);
		m_make = (EditText) findViewById(R.id.vehicle_edit_make);
		m_model = (EditText) findViewById(R.id.vehicle_edit_model);
		m_title = (EditText) findViewById(R.id.vehicle_edit_title);
		m_save = (Button) findViewById(R.id.vehicle_edit_save_btn);
		m_default = (CheckBox) findViewById(R.id.vehicle_edit_default);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		// set up the handlers
		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		Cursor c = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID,
				Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);

		m_vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
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

		if (m_vehicleAdapter.getCount() == 1) {
			m_default.setVisibility(View.GONE);
		} else {
			if (m_vehicleAdapter.getItemId(0) == c.getLong(0)) {
				m_default.setChecked(true);
			}
		}
	}

	private void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	private void save() {
		// do some error checking
		String year = m_year.getText().toString().trim();
		String make = m_make.getText().toString().trim();
		String model = m_model.getText().toString().trim();
		String title = m_title.getText().toString().trim();

		int error = 0;
		if (year.length() == 0) {
			error = R.string.error_year;
		}
		if (make.length() == 0) {
			error = R.string.error_make;
		}
		if (model.length() == 0) {
			error = R.string.error_model;
		}
		if (title.length() == 0) {
			error = R.string.error_title;
		}

		if (error != 0) {
			AlertDialog dlg = new AlertDialog.Builder(EditVehicleView.this).create();
			dlg.setTitle(R.string.error);
			dlg.setMessage(getString(error));
			dlg.show();
			return;
		}

		// save the changes
		ContentValues values = new ContentValues();
		values.put(Vehicles.YEAR, m_year.getText().toString().trim());
		values.put(Vehicles.MAKE, m_make.getText().toString().trim());
		values.put(Vehicles.MODEL, m_model.getText().toString().trim());
		values.put(Vehicles.TITLE, m_title.getText().toString().trim());

		if (m_default.isChecked()) {
			values.put(Vehicles.DEFAULT, System.currentTimeMillis());
		}

		getContentResolver().update(getIntent().getData(), values, null, null);
		finish();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		if (m_vehicleAdapter.getCount() > 1) {
			menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete).setShortcut('1', 'd');
		}
		HelpDialog.injectHelp(menu, 'h');
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				showDialog(DELETE_DIALOG_ID);
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_vehicle_edit, R.string.help_vehicle_edit);
				break;
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
