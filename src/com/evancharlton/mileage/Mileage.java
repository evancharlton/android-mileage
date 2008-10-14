package com.evancharlton.mileage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class Mileage extends Activity {

	public static final int DATE_DIALOG_ID = 0;

	public static final int MENU_HISTORY = Menu.FIRST;
	public static final int MENU_GRAPHS = Menu.FIRST + 1;

	private int m_year;
	private int m_month;
	private int m_day;

	private Button m_customDateButton;
	private Button m_saveButton;
	private EditText m_priceEdit;
	private EditText m_amountEdit;
	private EditText m_mileageEdit;

	// private Spinner m_vehicleSpinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		setContentView(R.layout.main);

		initData();
		initHandlers();
		updateDate();
	}

	private void initData() {
		m_saveButton = (Button) findViewById(R.id.add_fillup_btn);
		m_customDateButton = (Button) findViewById(R.id.change_date_btn);
		m_mileageEdit = (EditText) findViewById(R.id.odometer_edit);
		m_amountEdit = (EditText) findViewById(R.id.amount_edit);
		m_priceEdit = (EditText) findViewById(R.id.price_edit);
		// m_vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);

		/*
		 * ArrayAdapter<CharSequence> vehicleAdapter =
		 * ArrayAdapter.createFromResource(this, R.array.vehicles,
		 * android.R.layout.simple_spinner_item);
		 * vehicleAdapter.setDropDownViewResource
		 * (android.R.layout.simple_spinner_dropdown_item);
		 * m_vehicleSpinner.setAdapter(vehicleAdapter);
		 */
		// populate with initial data
		m_saveButton.setText(getString(R.string.add_fillup));
		m_priceEdit.setText(getString(R.string.price_per_gallon));
		m_mileageEdit.setText(getString(R.string.odometer));
		m_amountEdit.setText(getString(R.string.gallons));
	}

	private void initHandlers() {
		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the new fill-up
				ContentValues values = new ContentValues();

				try {
					double cost = Double.parseDouble(m_priceEdit.getText().toString());
					values.put(FillUps.COST, cost);
				} catch (NumberFormatException nfe) {
					values.put(FillUps.COST, 0.00);
				}

				try {
					double amount = Double.parseDouble(m_amountEdit.getText().toString());
					values.put(FillUps.AMOUNT, amount);
				} catch (NumberFormatException nfe) {
					values.put(FillUps.AMOUNT, 0.00);
				}

				try {
					int mileage = Integer.parseInt(m_mileageEdit.getText().toString());
					values.put(FillUps.MILEAGE, mileage);
				} catch (NumberFormatException nfe) {
					values.put(FillUps.MILEAGE, 0);
				}

				Calendar c = new GregorianCalendar(m_year, m_month, m_day);
				values.put(FillUps.DATE, c.getTimeInMillis());

				getContentResolver().insert(FillUps.CONTENT_URI, values);
				resetForm(v);
			}
		});

		m_customDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// show a date picker
				showDialog(DATE_DIALOG_ID);
			}
		});

		m_priceEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus((EditText) v.findViewById(R.id.price_edit), R.string.price_per_gallon, hasFocus, false);
			}
		});

		m_amountEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus((EditText) v.findViewById(R.id.amount_edit), R.string.gallons, hasFocus, false);
			}
		});

		m_mileageEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				setTextOnFocus((EditText) v.findViewById(R.id.odometer_edit), R.string.odometer, hasFocus, true);
			}
		});
	}

	private void resetForm(View v) {
		// reset the form, discard the URI
		m_saveButton.setText(getString(R.string.add_fillup));
		m_priceEdit.setText(getString(R.string.price_per_gallon));
		m_amountEdit.setText(getString(R.string.gallons));
		m_mileageEdit.setText(getString(R.string.odometer));

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		m_month = cal.get(Calendar.MONTH);
		m_year = cal.get(Calendar.YEAR);
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		updateDate();

		getIntent().setData(FillUps.CONTENT_URI);
	}

	private void setTextOnFocus(EditText editor, int defaultString, boolean hasFocus, boolean isInt) {
		String text = editor.getText().toString();
		text = text.trim();
		if (hasFocus) {
			try {
				if (isInt) {
					int tmp = Integer.parseInt(text);
					editor.setText(String.valueOf(tmp));
				} else {
					double tmp = Double.parseDouble(text);
					editor.setText(String.valueOf(tmp));
				}
			} catch (NumberFormatException nfe) {
				editor.setText("");
			}
		} else {
			if (text.length() == 0) {
				editor.setText(getString(defaultString));
			}
		}
	}

	private void updateDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd");

		StringBuilder changedText = new StringBuilder();
		changedText.append(sdf.format(new Date(m_year, m_month, m_day)));
		changedText.append(" (").append(getString(R.string.custom_date)).append(")");

		m_customDateButton.setText(changedText.toString());
	}

	private void showHistory() {
		startActivity(new Intent(Intent.ACTION_PICK));
	}

	private void showGraphs() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_HISTORY, 0, R.string.fillup_history).setShortcut('1', 'h');
		menu.add(Menu.NONE, MENU_GRAPHS, 0, R.string.graphs).setShortcut('2', 'g');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HISTORY:
				showHistory();
				break;
			case MENU_GRAPHS:
				showGraphs();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_DIALOG_ID:
				return new DatePickerDialog(this, m_dateSetListener, m_year, m_month, m_day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener m_dateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int month, int day) {
			m_year = year;
			m_month = month;
			m_day = day;
			updateDate();
		}
	};
}