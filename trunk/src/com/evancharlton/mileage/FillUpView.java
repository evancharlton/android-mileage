package com.evancharlton.mileage;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.evancharlton.mileage.models.FillUp;

public class FillUpView extends AddFillUpView {
	private static final int DELETE_DIALOG_ID = 1;

	public static final int MENU_DELETE = Menu.FIRST;

	private AlertDialog m_deleteDialog = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initHandlers() {
		super.initHandlers();
		m_saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// get the ID
				long id = Long.parseLong(getIntent().getData().getLastPathSegment());
				// save the new fill-up
				FillUp fillup = saveData();
				if (fillup == null) {
					showMessage(false);
					return;
				}

				fillup.setId(id);

				fillup.save();
				finish();
				showMessage(true);
			}
		});
	}

	private void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	@Override
	protected void loadData() {
		super.loadData();
		m_deleteDialog = new AlertDialog.Builder(this).create();
		m_deleteDialog.setMessage(getString(R.string.confirm_delete));
		m_deleteDialog.setCancelable(false);
		m_deleteDialog.setButton(getString(R.string.yes), m_deleteListener);
		m_deleteDialog.setButton2(getString(R.string.no), m_deleteListener);

		Intent data = getIntent();

		// load the data
		String[] projections = new String[] {
				FillUp._ID,
				FillUp.COST,
				FillUp.AMOUNT,
				FillUp.MILEAGE,
				FillUp.DATE,
				FillUp.VEHICLE_ID,
				FillUp.COMMENT
		};

		Cursor c = managedQuery(data.getData(), projections, null, null, null);
		c.moveToFirst();

		m_priceEdit.setText(c.getString(1));
		m_amountEdit.setText(c.getString(2));
		m_mileageEdit.setText(c.getString(3));

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(4));
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		updateDate();

		m_commentEdit.setText(c.getString(6));
		int vehicleId = c.getInt(5);

		int position = 0;
		for (int i = 0; i < m_vehicleAdapter.getCount(); i++) {
			if (vehicleId == m_vehicleAdapter.getItemId(i)) {
				position = i;
				break;
			}
		}
		m_vehicleSpinner.setSelection(position);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete).setShortcut('1', 'd').setIcon(R.drawable.ic_menu_delete);
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				showDialog(DELETE_DIALOG_ID);
				return true;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_fillup_edit, R.string.help_fillup_edit);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DELETE_DIALOG_ID:
				return m_deleteDialog;
		}
		return super.onCreateDialog(id);
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