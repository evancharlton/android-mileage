package com.evancharlton.mileage;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.evancharlton.mileage.models.Vehicle;

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

	protected void showMessage(boolean success) {
		if (success) {
			Toast.makeText(this, getString(R.string.vehicle_saved), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.vehicle_error_saving), Toast.LENGTH_SHORT).show();
		}
	}

	protected void initUI() {
		m_year = (EditText) findViewById(R.id.vehicle_year);
		m_make = (EditText) findViewById(R.id.vehicle_make);
		m_model = (EditText) findViewById(R.id.vehicle_model);
		m_title = (EditText) findViewById(R.id.vehicle_title);
		m_save = (Button) findViewById(R.id.vehicle_save_btn);
		m_default = (CheckBox) findViewById(R.id.vehicle_default);

		m_year.requestFocus();

		// set up the handlers
		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Vehicle vehicle = setData();
				if (vehicle == null) {
					return;
				}

				vehicle.save();

				finish();
			}
		});
	}

	protected Vehicle setData() {
		// do some error checking
		String year = m_year.getText().toString();
		String make = m_make.getText().toString();
		String model = m_model.getText().toString();
		String title = m_title.getText().toString();

		Vehicle v = new Vehicle();
		v.setTitle(title);
		v.setMake(make);
		v.setModel(model);
		v.setYear(year);

		if (m_default.isChecked()) {
			v.setDefault(true);
		}

		int valid = v.validate();

		if (valid > 0) {
			AlertDialog dlg = new AlertDialog.Builder(this).create();
			dlg.setTitle(R.string.error);
			dlg.setMessage(getString(valid));
			dlg.show();
			return null;
		}

		return v;
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
