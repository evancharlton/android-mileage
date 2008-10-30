package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class StatisticsView extends Activity {
	private HashMap<Integer, TextView> m_stats = new HashMap<Integer, TextView>();
	private Spinner m_vehicles;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		initUI();
		populateSpinner();
	}

	private void initUI() {
		getTextView(R.id.stats_distance_average);
		getTextView(R.id.stats_distance_maximum);
		getTextView(R.id.stats_distance_minimum);
		getTextView(R.id.stats_distance_running);
		getTextView(R.id.stats_economy_average);
		getTextView(R.id.stats_economy_maximum);
		getTextView(R.id.stats_economy_minimum);
		getTextView(R.id.stats_economy_running);
		getTextView(R.id.stats_price_average);
		getTextView(R.id.stats_price_latest);
		getTextView(R.id.stats_price_maximum);
		getTextView(R.id.stats_price_minimum);
		getTextView(R.id.stats_price_running);
		getTextView(R.id.stats_amount_average);
		getTextView(R.id.stats_amount_average_cost);
		getTextView(R.id.stats_amount_maximum);
		getTextView(R.id.stats_amount_minimum);
		getTextView(R.id.stats_amount_running);

		m_vehicles = (Spinner) findViewById(R.id.stats_vehicle_spinner);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				calculateStatistics();
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

	private void calculateStatistics() {
		long id = m_vehicles.getSelectedItemId();
		String[] projection = new String[] {
				FillUps.AMOUNT, FillUps.COST, FillUps.DATE, FillUps.MILEAGE
		};
		Cursor c = managedQuery(FillUps.CONTENT_URI, projection, FillUps.VEHICLE_ID + " = ?", new String[] {
			String.valueOf(id)
		}, FillUps.DATE + " DESC, " + FillUps.MILEAGE + " DESC");

		ArrayList<Double> amounts = new ArrayList<Double>();
		ArrayList<Double> costs = new ArrayList<Double>();
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<Double> miles = new ArrayList<Double>();

		int count = 0;
		c.moveToFirst();
		int num = c.getCount();
		while (num > 0) {
			try {
				amounts.add(c.getDouble(0));
				costs.add(c.getDouble(1));
				dates.add(c.getString(2));
				miles.add(c.getDouble(3));
				c.moveToNext();
			} catch (CursorIndexOutOfBoundsException e) {
				break;
			}
			count++;
			num--;
		}

		if (count == 0) {
			return;
		}

		// calculate the distance stats
		double distance_running = 0.0D;
		if (miles.size() == 1) {
			distance_running = miles.get(0);
		} else {
			distance_running = miles.get(0) - miles.get(1);
		}
		setText(R.id.stats_distance_running, distance_running);

		double max_distance = 0.0D;
		double min_distance = Double.MAX_VALUE;
		for (int i = 0; i < miles.size() - 1; i++) {
			double diff = miles.get(i) - miles.get(i + 1);
			if (diff > max_distance) {
				max_distance = diff;
			} else if (diff < min_distance) {
				min_distance = diff;
			}
		}
		double total_distance = miles.get(0) - miles.get(miles.size() - 1);
		double avg_distance = total_distance / miles.size();
		setText(R.id.stats_distance_average, avg_distance);
		setText(R.id.stats_distance_maximum, max_distance);
		setText(R.id.stats_distance_minimum, min_distance);

		// calculate the stats on fuel economy
		double total_fuel = 0.0D;
		double maximum_mileage = 0.0D;
		double minimum_mileage = Double.MAX_VALUE;
		double running_mileage = distance_running / amounts.get(0);
		double largest_fillup = 0.0D;
		double smallest_fillup = Double.MAX_VALUE;
		double total_cost = 0.0D;
		double min_price = Double.MAX_VALUE;
		double max_price = 0.0D;
		for (int i = 0; i < amounts.size() - 1; i++) {
			Double a = amounts.get(i);
			total_fuel += a;
			double diff = miles.get(i) - miles.get(i + 1);
			double mileage = diff / a;
			if (mileage > maximum_mileage) {
				maximum_mileage = mileage;
			} else if (mileage < minimum_mileage) {
				minimum_mileage = mileage;
			}

			// volumes
			if (a > largest_fillup) {
				largest_fillup = a;
			} else if (a < smallest_fillup) {
				smallest_fillup = a;
			}

			// costs
			Double cost = costs.get(i);
			total_cost += (cost * amounts.get(i));
			if (cost < min_price) {
				min_price = cost;
			} else if (cost > max_price) {
				max_price = cost;
			}
		}
		int sz = amounts.size();
		largest_fillup = (amounts.get(sz - 1) > largest_fillup) ? amounts.get(sz - 1) : largest_fillup;
		smallest_fillup = (amounts.get(sz - 1) < smallest_fillup) ? amounts.get(sz - 1) : smallest_fillup;
		double last_cost = (costs.get(sz - 1) * amounts.get(sz - 1));
		min_price = (last_cost < min_price) ? last_cost : min_price;
		max_price = (last_cost > max_price) ? last_cost : max_price;
		total_cost += last_cost;
		total_fuel += amounts.get(sz - 1);
		double economy_avg = total_distance / total_fuel;
		setText(R.id.stats_economy_average, economy_avg);
		setText(R.id.stats_economy_maximum, maximum_mileage);
		setText(R.id.stats_economy_minimum, minimum_mileage);
		setText(R.id.stats_economy_running, running_mileage);

		// calculate gas statistics
		double avg_price = total_cost / total_fuel;
		double latest_price = costs.get(0);

		setText(R.id.stats_price_average, avg_price, "$");
		setText(R.id.stats_price_latest, latest_price, "$");
		setText(R.id.stats_price_maximum, max_price, "$");
		setText(R.id.stats_price_minimum, min_price, "$");
		setText(R.id.stats_price_running, total_cost, "$");

		// calculate stats on the volume of gasoline used
		double avg_amount = total_fuel / amounts.size();
		double avg_cost_per_fillup = total_cost / amounts.size();
		setText(R.id.stats_amount_average, avg_amount);
		setText(R.id.stats_amount_average_cost, avg_cost_per_fillup, "$");
		setText(R.id.stats_amount_maximum, largest_fillup);
		setText(R.id.stats_amount_minimum, smallest_fillup);
		setText(R.id.stats_amount_running, total_fuel);
	}

	private void setText(int id, double val) {
		setText(id, val, "");
	}

	private void setText(int id, double val, String prefix) {
		// set the precision
		val *= 100;
		val = Math.round(val);
		val /= 100;
		String str = prefix + String.valueOf(val);
		m_stats.get(id).setText(str);
	}
}
