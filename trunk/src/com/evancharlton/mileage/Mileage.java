package com.evancharlton.mileage;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Mileage extends TabActivity {
	private static final int MENU_FIELDS = 1;
	private static final int MENU_VEHICLES = 2;
	private static final int MENU_INTERVALS = 3;

	private static final String TAG_FILLUP = "fillups";
	private static final String TAG_HISTORY = "history";
	private static final String TAG_STATISTICS = "statistics";
	private static final String TAG_CHARTS = "charts";

	private TabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);

		mTabHost = getTabHost();
		mTabHost.addTab(createTabSpec(TAG_FILLUP, FillupActivity.class, R.string.fillup));
		mTabHost.addTab(createTabSpec(TAG_HISTORY, FillupListActivity.class, R.string.history));
	}

	private TabSpec createTabSpec(String tag, Class<? extends Activity> cls, int string) {
		TabSpec spec = mTabHost.newTabSpec(tag);
		spec.setContent(new Intent(this, cls));
		spec.setIndicator(getString(string));
		return spec;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_FIELDS, Menu.NONE, R.string.edit_fields);
		menu.add(Menu.NONE, MENU_VEHICLES, Menu.NONE, R.string.vehicles);
		menu.add(Menu.NONE, MENU_INTERVALS, Menu.NONE, R.string.service_intervals);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_FIELDS:
				startActivity(new Intent(this, FieldListActivity.class));
				return true;
			case MENU_VEHICLES:
				startActivity(new Intent(this, VehicleListActivity.class));
				return true;
			case MENU_INTERVALS:
				startActivity(new Intent(this, ServiceIntervalsListActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}