package com.evancharlton.mileage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddVehicleView extends Activity {
	protected EditText m_year;
	protected EditText m_make;
	protected EditText m_model;
	protected EditText m_title;
	protected Button m_save;
	protected CheckBox m_default;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_vehicle);

		initUI();
	}

	protected void initUI() {
		m_year = (EditText) findViewById(R.id.vehicle_year);
		m_make = (EditText) findViewById(R.id.vehicle_make);
		m_model = (EditText) findViewById(R.id.vehicle_model);
		m_title = (EditText) findViewById(R.id.vehicle_title);
		m_save = (Button) findViewById(R.id.vehicle_save_btn);
		m_default = (CheckBox) findViewById(R.id.vehicle_default);

		// set up the handlers
		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ContentValues values = save();
				if (values == null) {
					return;
				}

				getContentResolver().insert(Vehicles.CONTENT_URI, values);
				finish();
			}
		});
	}

	protected ContentValues save() {
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
			AlertDialog dlg = new AlertDialog.Builder(this).create();
			dlg.setTitle(R.string.error);
			dlg.setMessage(getString(error));
			dlg.show();
			return null;
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

		return values;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		HelpDialog.injectHelp(menu, 'h');
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_vehicle_add, R.string.help_vehicle_add);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
