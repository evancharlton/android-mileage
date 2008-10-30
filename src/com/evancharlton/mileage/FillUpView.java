package com.evancharlton.mileage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class FillUpView extends Activity {

	private static final int DATE_DIALOG_ID = 0;
	private static final int DELETE_DIALOG_ID = 1;

	private EditText m_priceEdit;
	private EditText m_amountEdit;
	private EditText m_mileageEdit;
	private Button m_dateButton;
	private Button m_saveButton;
	private Button m_cancelButton;
	private Button m_deleteButton;
	private AlertDialog m_deleteDialog;
	private Spinner m_vehicleSpinner;

	private int m_day;
	private int m_year;
	private int m_month;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fillup);

		initMembers();
		loadData();
	}

	private void initMembers() {
		m_priceEdit = (EditText) findViewById(R.id.price_edit);
		m_amountEdit = (EditText) findViewById(R.id.amount_edit);
		m_mileageEdit = (EditText) findViewById(R.id.odometer_edit);
		m_dateButton = (Button) findViewById(R.id.change_date_btn);
		m_saveButton = (Button) findViewById(R.id.save_btn);
		m_cancelButton = (Button) findViewById(R.id.cancel_btn);
		m_deleteButton = (Button) findViewById(R.id.delete_btn);
		m_vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		Calendar cal = Calendar.getInstance();
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);

		// handlers
		m_dateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the changes
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

				values.put(FillUps.VEHICLE_ID, m_vehicleSpinner.getSelectedItemId());

				Calendar c = new GregorianCalendar(m_year, m_month, m_day);
				values.put(FillUps.DATE, c.getTimeInMillis());

				getContentResolver().update(getIntent().getData(), values, null, null);
				finish();
			}
		});

		m_cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		m_deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// delete
				showDialog(DELETE_DIALOG_ID);
			}
		});
	}

	private void delete() {
		// delete this record
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	private void updateDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd");

		StringBuilder changedText = new StringBuilder();
		changedText.append(sdf.format(new Date(m_year, m_month, m_day)));
		changedText.append(" (").append(getString(R.string.custom_date)).append(")");

		m_dateButton.setText(changedText.toString());
	}

	private void loadData() {
		Intent data = getIntent();

		// load the data
		String[] projections = new String[] {
				FillUps._ID, FillUps.COST, FillUps.AMOUNT, FillUps.MILEAGE, FillUps.DATE, FillUps.VEHICLE_ID
		};

		Cursor c = managedQuery(data.getData(), projections, null, null, null);
		c.moveToFirst();

		m_priceEdit.setText(c.getString(1));
		m_amountEdit.setText(c.getString(2));
		m_mileageEdit.setText(c.getString(3));
		int vehicleId = c.getInt(5);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(4));
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		updateDate();

		c = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID, Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicleSpinner.setAdapter(vehicleAdapter);

		int position = 0;
		for (int i = 0; i < vehicleAdapter.getCount(); i++) {
			if (vehicleId == vehicleAdapter.getItemId(i)) {
				position = i;
				break;
			}
		}
		m_vehicleSpinner.setSelection(position);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_DIALOG_ID:
				return new DatePickerDialog(this, m_dateSetListener, m_year, m_month, m_day);
			case DELETE_DIALOG_ID:
				return m_deleteDialog;
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
