package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class StatisticsView extends Activity {
	private HashMap<Integer, TextView> m_stats = new HashMap<Integer, TextView>();
	private Spinner m_vehicles;
	private SimpleCursorAdapter m_fillups;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		initUI();
		populateSpinner();
	}

	private void initUI() {
		getTextView(R.id.stats_distance_average);
		getTextView(R.id.stats_distance_maximum);
		getTextView(R.id.stats_distance_running);
		getTextView(R.id.stats_economy_average);
		getTextView(R.id.stats_economy_maximum);
		getTextView(R.id.stats_economy_minimum);
		getTextView(R.id.stats_economy_running);

		m_vehicles = (Spinner) findViewById(R.id.stats_vehicle_spinner);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				calculateStats();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// uh, do nothing?
			}
		});
	}

	private void getTextView(int id) {
		m_stats.put(id, (TextView) findViewById(id));
	}

	private void populateSpinner() {
		Cursor c = managedQuery(Vehicles.CONTENT_URI, new String[] {
				Vehicles._ID, Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicles.setAdapter(vehicleAdapter);
	}

	private void calculateStats() {
		long id = m_vehicles.getSelectedItemId();
		String[] projection = new String[] {
				FillUps.AMOUNT, FillUps.COST, FillUps.DATE, FillUps.MILEAGE
		};
		Cursor c = managedQuery(FillUps.CONTENT_URI, projection, FillUps.VEHICLE_ID + " = ?", new String[] {
			String.valueOf(id)
		}, FillUps.DATE + " DESC");

		ArrayList<Double> amounts = new ArrayList<Double>();
		ArrayList<Double> costs = new ArrayList<Double>();
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<Double> miles = new ArrayList<Double>();

		int i = 0;
		c.moveToFirst();
		while (c.isLast() == false) {
			amounts.add(c.getDouble(0));
			costs.add(c.getDouble(1));
			dates.add(c.getString(2));
			miles.add(c.getDouble(3));
			c.moveToNext();
			i++;
		}

		if (i == 0) {
			return;
		}

		// calculate the distance stats
		double distance_running = 0.0D;
		if (miles.size() == 1) {
			distance_running = miles.get(0);
		} else {
			distance_running = miles.get(0) - miles.get(1);
		}
		m_stats.get(R.id.stats_distance_running).setText(String.valueOf(distance_running));
	}
}
