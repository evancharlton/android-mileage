package com.evancharlton.mileage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditVehicleView extends Activity {
	private EditText m_year;
	private EditText m_make;
	private EditText m_model;
	private EditText m_title;
	private Button m_save;
	private Button m_cancel;
	private Button m_delete;
	private AlertDialog m_deleteDialog;

	private static final int DELETE_DIALOG_ID = 1;

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
		m_cancel = (Button) findViewById(R.id.vehicle_edit_cancel_btn);
		m_delete = (Button) findViewById(R.id.vehicle_edit_delete_btn);

		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		// set up the handlers
		m_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the changes
				ContentValues values = new ContentValues();
				values.put(Vehicles.YEAR, m_year.getText().toString().trim());
				values.put(Vehicles.MAKE, m_make.getText().toString().trim());
				values.put(Vehicles.MODEL, m_model.getText().toString().trim());
				values.put(Vehicles.TITLE, m_title.getText().toString().trim());

				getContentResolver().update(getIntent().getData(), values, null, null);
				finish();
			}
		});

		m_delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// prompt to make sure
				showDialog(DELETE_DIALOG_ID);
			}
		});

		m_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// bail out
				finish();
			}
		});
	}

	private void loadData() {
		Intent data = getIntent();

		// load the data
		String[] projections = new String[] {
				Vehicles._ID, Vehicles.YEAR, Vehicles.MAKE, Vehicles.MODEL, Vehicles.TITLE
		};

		Cursor c = managedQuery(data.getData(), projections, null, null, null);
		c.moveToFirst();

		m_year.setText(c.getString(1));
		m_make.setText(c.getString(2));
		m_model.setText(c.getString(3));
		m_title.setText(c.getString(4));
	}

	private void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
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
