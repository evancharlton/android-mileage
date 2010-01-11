package com.evancharlton.mileage;

import java.util.Calendar;

import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.evancharlton.mileage.models.FillUp;

public class FillUpView extends AddFillUpView {
	private static final int DELETE_DIALOG_ID = 1;

	public static final int MENU_DELETE = Menu.FIRST;

	@Override
	protected String getTag() {
		return "EditFillUp";
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
				fillup.setPartial(m_partialCheckbox.isChecked());

				fillup.save();
				findServiceIntervals(fillup);
				finish();
				showMessage(true);
			}
		});
	}

	@Override
	protected void delete() {
		getContentResolver().delete(getIntent().getData(), null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadData();
	}

	@Override
	protected void loadData() {
		super.loadData();
		Intent data = getIntent();

		Cursor c = managedQuery(data.getData(), FillUp.getProjection(), null, null, null);
		c.moveToFirst();

		m_priceEdit.setText(c.getString(c.getColumnIndex(FillUp.PRICE)));
		m_amountEdit.setText(c.getString(c.getColumnIndex(FillUp.AMOUNT)));
		m_mileageEdit.setText(c.getString(c.getColumnIndex(FillUp.ODOMETER)));
		int checked = c.getInt(c.getColumnIndex(FillUp.PARTIAL));
		m_partialCheckbox.setChecked(checked == 1);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(c.getColumnIndex(FillUp.DATE)));
		m_year = cal.get(Calendar.YEAR);
		m_month = cal.get(Calendar.MONTH);
		m_day = cal.get(Calendar.DAY_OF_MONTH);
		updateDate();

		m_commentEdit.setText(c.getString(c.getColumnIndex(FillUp.COMMENT)));
		int vehicleId = c.getInt(c.getColumnIndex(FillUp.VEHICLE_ID));

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
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE:
				showDialog(DELETE_DIALOG_ID);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}