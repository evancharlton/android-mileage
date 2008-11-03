package com.evancharlton.mileage;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddVehicleView extends Activity {
	private EditText m_year;
	private EditText m_make;
	private EditText m_model;
	private EditText m_title;
	private Button m_save;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_vehicle);

		initUI();
	}

	private void initUI() {
		m_year = (EditText) findViewById(R.id.vehicle_year);
		m_make = (EditText) findViewById(R.id.vehicle_make);
		m_model = (EditText) findViewById(R.id.vehicle_model);
		m_title = (EditText) findViewById(R.id.vehicle_title);
		m_save = (Button) findViewById(R.id.vehicle_add);

		// set up the handlers
		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the changes
				ContentValues values = new ContentValues();
				values.put(Vehicles.YEAR, m_year.getText().toString().trim());
				values.put(Vehicles.MAKE, m_make.getText().toString().trim());
				values.put(Vehicles.MODEL, m_model.getText().toString().trim());
				values.put(Vehicles.TITLE, m_title.getText().toString().trim());

				getContentResolver().insert(Vehicles.CONTENT_URI, values);
				finish();
			}
		});

		m_year.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus(m_year, R.string.vehicle_year, hasFocus);
			}
		});

		m_make.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus(m_make, R.string.vehicle_make, hasFocus);
			}
		});

		m_model.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus(m_model, R.string.vehicle_model, hasFocus);
			}
		});

		m_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus(m_title, R.string.vehicle_title, hasFocus);
			}
		});
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

	private void setTextOnFocus(EditText editor, int defaultString, boolean hasFocus) {
		String text = editor.getText().toString();
		text = text.trim();
		if (hasFocus) {
			if (text.equals(getString(defaultString))) {
				editor.setText("");
			}
		} else {
			if (text.length() == 0) {
				editor.setText(getString(defaultString));
			}
		}
	}
}
