package com.evancharlton.mileage;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.evancharlton.mileage.provider.tables.FillupsTable;

public class Mileage extends TabActivity {
	public static final String VISIBLE_TAB = "visible_tab";

	public static final String TAG_FILLUP = "fillups";
	public static final String TAG_HISTORY = "history";
	public static final String TAG_STATISTICS = "statistics";
	public static final String TAG_VEHICLES = "vehicles";

	private TabHost mTabHost;

	private final Handler mHandler = new Handler();

	private final ContentObserver mFillupsObserver = new ContentObserver(mHandler) {
		public void onChange(boolean selfChange) {
			// TODO(3.1) - Fix this so it doesn't spontaneously change tabs
			if (TAG_FILLUP.equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag(TAG_HISTORY);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);

		mTabHost = getTabHost();
		mTabHost.addTab(createTabSpec(TAG_FILLUP, FillupActivity.class, R.string.fillup));
		mTabHost.addTab(createTabSpec(TAG_HISTORY, FillupListActivity.class, R.string.history));
		mTabHost.addTab(createTabSpec(TAG_STATISTICS, VehicleStatisticsActivity.class, R.string.statistics));
		mTabHost.addTab(createTabSpec(TAG_VEHICLES, VehicleListActivity.class, R.string.vehicles));

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				// hide the virtual keyboard when switching tabs
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTabHost.getApplicationWindowToken(), 0);
			}
		});

		String requestedTab = getIntent().getStringExtra(VISIBLE_TAB);
		if (requestedTab != null) {
			switchTo(requestedTab);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getContentResolver().registerContentObserver(FillupsTable.BASE_URI, true, mFillupsObserver);
	}

	@Override
	protected void onPause() {
		super.onPause();

		getContentResolver().unregisterContentObserver(mFillupsObserver);
	}

	public void switchTo(String tag) {
		mTabHost.setCurrentTabByTag(tag);
	}

	private TabSpec createTabSpec(String tag, Class<? extends Activity> cls, int string) {
		TabSpec spec = mTabHost.newTabSpec(tag);
		spec.setContent(new Intent(this, cls));
		spec.setIndicator(getString(string));
		return spec;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		add(menu, R.string.service_intervals, ServiceIntervalsListActivity.class).setIcon(R.drawable.wrench);
		add(menu, R.string.import_export, ImportExportActivity.class).setIcon(R.drawable.importexport_i);
		add(menu, R.string.settings, SettingsActivity.class).setIcon(R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	private final MenuItem add(final Menu menu, final int string, final Class<? extends Activity> cls) {
		return menu.add(Menu.NONE, string, Menu.NONE, string).setIntent(new Intent(this, cls));
	}
}