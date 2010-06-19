package com.evancharlton.mileage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

public class ServiceIntervalsListActivity extends BaseListActivity implements DialogInterface.OnClickListener {
	private static final int MENU_CREATE = 1;
	private static final int MENU_TEMPLATES = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		long id = intent.getLongExtra(ServiceInterval._ID, -1);
		if (id > 0) {
			showDialog(R.string.delete_service_interval);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case R.string.delete_service_interval:
				return new AlertDialog.Builder(this).setPositiveButton(android.R.string.yes, this).setNegativeButton(android.R.string.no, this)
						.setNeutralButton(R.string.remind_later, this).setMessage(R.string.delete_service_interval).create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected String[] getFrom() {
		return new String[] {
				ServiceInterval.TITLE,
				ServiceInterval.DESCRIPTION
		};
	}

	@Override
	protected Uri getUri() {
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalsTable.URI);
	}

	@Override
	public void onItemClick(long id) {
		loadItem(id, ServiceIntervalActivity.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_service_interval);
		menu.add(Menu.NONE, MENU_TEMPLATES, Menu.NONE, R.string.service_interval_templates);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_CREATE:
				startActivity(new Intent(this, ServiceIntervalActivity.class));
				return true;
			case MENU_TEMPLATES:
				startActivity(new Intent(this, ServiceIntervalTemplateListActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		ServiceInterval interval = ServiceInterval.loadById(this, getIntent().getLongExtra(ServiceInterval._ID, -1));
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// delete the interval
				interval.delete(this);
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				// remind tomorrow
				interval.deleteAlarm(this);
				interval.scheduleAlarm(this, System.currentTimeMillis() + Calculator.DAY_MS);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// no action
		}
		dialog.dismiss();
	}
}
