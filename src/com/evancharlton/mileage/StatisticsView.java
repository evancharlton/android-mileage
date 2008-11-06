package com.evancharlton.mileage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private ArrayList<Double> m_amounts;
	private ArrayList<Double> m_costs;
	private ArrayList<Long> m_dates;
	private ArrayList<Double> m_miles;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		initUI();
		populateSpinner();
	}

	private void initUI() {
		m_vehicles = (Spinner) findViewById(R.id.stats_vehicle_spinner);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				calculateStatistics(m_vehicles.getSelectedItemId());
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
				Vehicles._ID,
				Vehicles.TITLE
		}, null, null, Vehicles.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicles.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_vehicles.setAdapter(vehicleAdapter);

		if (vehicleAdapter.getCount() == 1) {
			m_vehicles.setVisibility(View.GONE);
			calculateStatistics(vehicleAdapter.getItemId(0));
		}
	}

	private void calculateStatistics(long id) {
		String[] projection = new String[] {
				FillUps.AMOUNT,
				FillUps.COST,
				FillUps.DATE,
				FillUps.MILEAGE
		};
		Cursor c = managedQuery(FillUps.CONTENT_URI, projection, FillUps.VEHICLE_ID + " = ?", new String[] {
			String.valueOf(id)
		}, FillUps.DATE + " DESC, " + FillUps.MILEAGE + " DESC");

		HashMap<Integer, String> calculatedData = new HashMap<Integer, String>();

		m_amounts = new ArrayList<Double>();
		m_costs = new ArrayList<Double>();
		m_dates = new ArrayList<Long>();
		m_miles = new ArrayList<Double>();

		int count = 0;
		c.moveToFirst();
		int num = c.getCount();
		while (num > 0) {
			try {
				m_amounts.add(c.getDouble(0));
				m_costs.add(c.getDouble(1));
				m_dates.add(c.getLong(2));
				m_miles.add(c.getDouble(3));
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

		calculatedData.putAll(distanceStats());
		calculatedData.putAll(economyStats());
		calculatedData.putAll(costStats());

		// update the text
		for (Integer dataId : calculatedData.keySet()) {
			setText(dataId, calculatedData.get(dataId));
		}
	}

	private Map<Integer, String> distanceStats() {
		HashMap<Integer, String> data = new HashMap<Integer, String>();
		// total distance tracked
		double total_distance = 0.0D;
		// distance between last two fill-ups
		double running_distance = 0.0D;
		if (m_miles.size() > 1) {
			running_distance = Math.abs(m_miles.get(0) - m_miles.get(1));
			total_distance = Math.abs(m_miles.get(0) - m_miles.get(m_miles.size() - 1));
		}

		double max_distance = 0.0D;
		double min_distance = Double.MAX_VALUE;
		for (int i = 0; i < m_miles.size() - 1; i++) {
			double diff = m_miles.get(i) - m_miles.get(i + 1);
			if (diff > max_distance) {
				max_distance = diff;
			}
			if (diff < min_distance) {
				min_distance = diff;
			}
		}
		double avg_distance = total_distance / m_miles.size();
		data.put(R.id.stats_distance_running, string(running_distance));
		data.put(R.id.stats_distance_average, string(avg_distance));
		data.put(R.id.stats_distance_maximum, string(max_distance));
		data.put(R.id.stats_distance_minimum, string(min_distance));
		return data;
	}

	private Map<Integer, String> economyStats() {
		HashMap<Integer, String> data = new HashMap<Integer, String>();
		double average_mpg = 0.0D;
		double minimum_mpg = Double.MAX_VALUE;
		double maximum_mpg = 0.0D;
		double total_miles = 0.0D;
		double total_fuel = 0.0D;
		double running_mpg = 0.0D;

		for (int i = 0; i < m_amounts.size() - 1; i++) {
			total_fuel += m_amounts.get(i);
			double mile_diff = m_miles.get(i) - m_miles.get(i + 1);
			total_miles += mile_diff;
			double mpg = mile_diff / m_amounts.get(i);
			if (mpg > maximum_mpg) {
				maximum_mpg = mpg;
			}
			if (mpg < minimum_mpg) {
				minimum_mpg = mpg;
			}

			if (i == 0) {
				running_mpg = mpg;
			}
		}
		// note that you don't sum up ALL of m_amounts because the last one
		// (which is the oldest in history terms) does not relate to the average
		// mileage.

		average_mpg = total_miles / total_fuel;

		data.put(R.id.stats_economy_average, string(average_mpg));
		data.put(R.id.stats_economy_maximum, string(maximum_mpg));
		data.put(R.id.stats_economy_minimum, string(minimum_mpg));
		data.put(R.id.stats_economy_running, string(running_mpg));

		return data;
	}

	private Map<Integer, String> costStats() {
		HashMap<Integer, String> data = new HashMap<Integer, String>();
		double total_cost = 0.0D;
		double lowest_ppg = Double.MAX_VALUE;
		double highest_ppg = 0.0D;
		double total_fuel = 0.0D;
		double highest_amt = 0.0D;
		double lowest_amt = Double.MAX_VALUE;
		double lowest_cost = Double.MAX_VALUE;
		double highest_cost = 0.0D;
		double total_expense = 0.0D;
		double thirty_day_cost = 0.0D;
		long then = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000);

		for (int i = 0; i < m_costs.size(); i++) {
			double cost_ppg = m_costs.get(i);
			if (cost_ppg > highest_ppg) {
				highest_ppg = cost_ppg;
			}
			if (cost_ppg < lowest_ppg) {
				lowest_ppg = cost_ppg;
			}

			double amount = m_amounts.get(i);
			if (amount > highest_amt) {
				highest_amt = amount;
			}
			if (amount < lowest_amt) {
				lowest_amt = amount;
			}

			double cost = cost_ppg * amount;
			if (cost > highest_cost) {
				highest_cost = cost;
			}
			if (cost < lowest_cost) {
				lowest_cost = cost;
			}

			double date = m_dates.get(i);
			if (date > then) {
				thirty_day_cost += cost;
			}

			total_cost += cost_ppg;
			total_fuel += amount;
			total_expense += cost;
		}

		data.put(R.id.stats_price_thirty_days, string(thirty_day_cost, "$"));
		data.put(R.id.stats_price_latest, string(m_costs.get(0), "$"));
		data.put(R.id.stats_price_average, string(total_cost / m_costs.size(), "$"));
		data.put(R.id.stats_price_running, string(total_expense, "$"));
		data.put(R.id.stats_price_minimum, string(lowest_ppg, "$"));
		data.put(R.id.stats_price_maximum, string(highest_ppg, "$"));
		data.put(R.id.stats_amount_running, string(total_fuel));
		data.put(R.id.stats_amount_average, string(total_fuel / m_amounts.size()));
		data.put(R.id.stats_amount_average_cost, string(total_expense / m_costs.size(), "$"));
		data.put(R.id.stats_amount_maximum, string(highest_amt));
		data.put(R.id.stats_amount_minimum, string(lowest_amt));
		data.put(R.id.stats_amount_maximum_cost, string(highest_cost, "$"));
		data.put(R.id.stats_amount_minimum_cost, string(lowest_cost, "$"));

		return data;
	}

	private void setText(int id, String text) {
		TextView tv = m_stats.get(id);
		if (tv == null) {
			getTextView(id);
			tv = m_stats.get(id);
			if (tv == null) {
				throw new IllegalArgumentException("Invalid ID: " + String.valueOf(id));
			}
		}
		tv.setText(text);
	}

	private String string(double val) {
		return string(val, "");
	}

	private String string(double val, String prefix) {
		DecimalFormat format = new DecimalFormat("##0.00");
		String str = prefix + format.format(val);
		return str;
	}
}
