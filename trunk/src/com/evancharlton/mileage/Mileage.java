package com.evancharlton.mileage;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Mileage extends TabActivity {
	public static final String EXTRA_IGNORE_STATE = "ignore-state";
	public static final String PACKAGE = "com.evancharlton.mileage";
	private TabHost m_tabHost;

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_IMPORT_EXPORT = Menu.FIRST + 1;
	private static final int MENU_VEHICLES = Menu.FIRST + 2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tabs);

		m_tabHost = getTabHost();

		addFillUpTab();
		addHistoryTab();
		addStatisticsTab();

		setCurrentTab(getIntent());
	}

	private void addFillUpTab() {
		Intent i = new Intent();
		i.setClass(Mileage.this, AddFillUpView.class);

		TabSpec spec = m_tabHost.newTabSpec("fillup");
		spec.setIndicator(getString(R.string.fillup), getResources().getDrawable(R.drawable.history_i));
		spec.setContent(i);
		m_tabHost.addTab(spec);
	}

	private void addHistoryTab() {
		Intent i = new Intent();
		i.setClass(Mileage.this, HistoryView.class);

		TabSpec spec = m_tabHost.newTabSpec("fillup");
		spec.setIndicator(getString(R.string.fillup_history), getResources().getDrawable(R.drawable.history_i));
		spec.setContent(i);
		m_tabHost.addTab(spec);
	}

	private void addStatisticsTab() {
		Intent i = new Intent();
		i.setClass(Mileage.this, StatisticsView.class);

		TabSpec spec = m_tabHost.newTabSpec("fillup");
		spec.setIndicator(getString(R.string.statistics), getResources().getDrawable(R.drawable.vehicles_i));
		spec.setContent(i);
		m_tabHost.addTab(spec);
	}

	private void setCurrentTab(Intent intent) {
		intent.putExtra(EXTRA_IGNORE_STATE, true);
		if (intent.getComponent().getClassName().equals(getClass().getName())) {
			m_tabHost.setCurrentTab(0);
		}
		intent.putExtra(EXTRA_IGNORE_STATE, false);
	}

	public static void createMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_VEHICLES, Menu.NONE, R.string.vehicles).setShortcut('1', 'v').setIcon(R.drawable.vehicles_i);
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.settings).setShortcut('2', 'e').setIcon(R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_IMPORT_EXPORT, Menu.NONE, R.string.import_export).setShortcut('3', 'i');
	}

	public static boolean parseMenuItem(MenuItem item, Activity base) {
		Intent i = new Intent();
		switch (item.getItemId()) {
			case MENU_VEHICLES:
				i.setClass(base, VehiclesView.class);
				base.startActivity(i);
				return true;
			case MENU_SETTINGS:
				i.setClass(base, SettingsView.class);
				base.startActivity(i);
				return true;
			case MENU_IMPORT_EXPORT:
				i.setClass(base, ImportExportView.class);
				base.startActivity(i);
				return true;
		}
		return false;
	}
}