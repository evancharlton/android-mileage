package com.evancharlton.mileage;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Mileage extends TabActivity {
	public static final String EXTRA_IGNORE_STATE = "ignore-state";
	public static final String PACKAGE = "com.evancharlton.mileage";
	private TabHost m_tabHost;

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
}