package com.evancharlton.mileage;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.evancharlton.mileage.views.ChartsView;
import com.evancharlton.mileage.views.intervals.ServiceIntervalsView;

public class Mileage extends TabActivity {
	public static final String EXTRA_IGNORE_STATE = "ignore-state";
	public static final String PACKAGE = "com.evancharlton.mileage";
	private TabHost m_tabHost;

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_IMPORT_EXPORT = Menu.FIRST + 1;
	private static final int MENU_VEHICLES = Menu.FIRST + 2;
	private static final int MENU_SERVICE_INTERVALS = Menu.FIRST + 3;

	private static final String CURRENT_TAB = "current_tab";
	private static final String CURRENT_VIEW = "current_view";

	private int m_selectedVehicleIndex = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tabs);

		m_tabHost = getTabHost();

		addFillUpTab();
		addHistoryTab();
		addStatisticsTab();
		addChartsTab();

		if (savedInstanceState != null) {
			int c = savedInstanceState.getInt(CURRENT_TAB, 0);
			if (c == 0) {
				Activity a = getCurrentActivity();
				Persistent p = (Persistent) a;
				p.restoreState(savedInstanceState);
			}
			m_tabHost.setCurrentTab(c);
			int id = savedInstanceState.getInt(CURRENT_VIEW, -1);
			if (id != -1) {
				View current = m_tabHost.getCurrentView();
				if (current != null) {
					View focus = current.findViewById(id);
					if (focus != null) {
						focus.requestFocus();
					}
				}
			}
		}

		FillUpsProvider.upgradeDatabase();
	}

	public void onResume() {
		super.onResume();
		setRequestedOrientation(PreferencesProvider.getInstance(this).getOrientation());
	}

	private void addFillUpTab() {
		Intent i = new Intent();
		i.setClass(Mileage.this, AddFillUpView.class);

		TabSpec spec = m_tabHost.newTabSpec("fillup");
		spec.setIndicator(getString(R.string.fillup), getResources().getDrawable(R.drawable.gas_i));
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
		spec.setIndicator(getString(R.string.statistics), getResources().getDrawable(R.drawable.statistics_i));
		spec.setContent(i);
		m_tabHost.addTab(spec);
	}

	private void addChartsTab() {
		Intent i = new Intent();
		i.setClass(Mileage.this, ChartsView.class);

		TabSpec spec = m_tabHost.newTabSpec("fillup");
		spec.setIndicator(getString(R.string.charts), getResources().getDrawable(R.drawable.charts_i));
		spec.setContent(i);
		m_tabHost.addTab(spec);
	}

	public static void createMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_VEHICLES, Menu.NONE, R.string.vehicles).setShortcut('1', 'v').setIcon(R.drawable.vehicles_i);
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.settings).setShortcut('2', 'e').setIcon(R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_IMPORT_EXPORT, Menu.NONE, R.string.import_export).setShortcut('3', 'i').setIcon(R.drawable.importexport_i);
		menu.add(Menu.NONE, MENU_SERVICE_INTERVALS, Menu.NONE, R.string.service_intervals).setShortcut('4', 's').setIcon(R.drawable.wrench);
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
			case MENU_SERVICE_INTERVALS:
				i.setClass(base, ServiceIntervalsView.class);
				base.startActivity(i);
				return true;
		}
		return false;
	}

	protected void onSaveInstanceState(Bundle outState) {
		int current = m_tabHost.getCurrentTab();
		outState.putInt(CURRENT_TAB, current);
		if (current == 0) {
			Activity a = getCurrentActivity();
			Persistent p = (Persistent) a;
			p.saveState(outState);
		}
		View focused = getCurrentFocus();
		if (focused != null) {
			outState.putInt(CURRENT_VIEW, focused.getId());
		}
	}

	/**
	 * Returns the index of the last selected vehicle. This is used so that the
	 * vehicle selection list (for history, stats, charts, etc) isn't constantly
	 * resetting to the default vehicle. Note that this shouldn't be used to set
	 * selections when recording (or editing) fill-ups or anything
	 * 
	 * @return index of last selected vehicle
	 */
	public int getSelectedVehicleIndex() {
		return m_selectedVehicleIndex;
	}

	public void setSelectedVehicleIndex(int pos) {
		m_selectedVehicleIndex = pos;
	}
}