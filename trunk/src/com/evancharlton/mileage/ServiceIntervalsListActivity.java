package com.evancharlton.mileage;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

public class ServiceIntervalsListActivity extends BaseListActivity {
	private static final int MENU_CREATE = 1;
	private static final int MENU_TEMPLATES = 2;

	@Override
	protected String[] getFrom() {
		return new String[] {
				ServiceInterval.TITLE,
				ServiceInterval.DESCRIPTION
		};
	}

	@Override
	protected Uri getUri() {
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalsTable.SERVICE_INTERVALS_URI);
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
}
