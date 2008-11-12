package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
	private PreferencesProvider m_pref;
	private CalculationEngine m_engine;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);
		m_pref = PreferencesProvider.getInstance(this);
		m_engine = m_pref.getCalculator();

		initUI();
		populateSpinner();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Mileage.createMenu(menu);
		HelpDialog.injectHelp(menu, 'h');
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = Mileage.parseMenuItem(item, this);
		if (ret) {
			return true;
		}
		switch (item.getItemId()) {
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_statistics, R.string.help_statistics);
				break;
		}
		return super.onOptionsItemSelected(item);
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

		if (count < 2) {
			// throw up a notice
			AlertDialog dlg = new AlertDialog.Builder(this).create();
			dlg.setTitle(R.string.statistics_no_data);
			dlg.setMessage(getString(R.string.statistics_no_data_msg));
			dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dlg.show();
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
		double avg_distance = total_distance / (m_miles.size() - 1);
		data.put(R.id.stats_distance_running, string(running_distance, m_engine.getDistanceUnitsAbbr()));
		data.put(R.id.stats_distance_average, string(avg_distance, m_engine.getDistanceUnitsAbbr()));
		data.put(R.id.stats_distance_maximum, string(max_distance, m_engine.getDistanceUnitsAbbr()));
		data.put(R.id.stats_distance_minimum, string(min_distance, m_engine.getDistanceUnitsAbbr()));
		return data;
	}

	private Map<Integer, String> economyStats() {
		HashMap<Integer, String> data = new HashMap<Integer, String>();
		double average_mpg = 0.0D;
		double minimum_mpg = m_engine.getBestEconomy();
		double maximum_mpg = m_engine.getWorstEconomy();
		double total_miles = 0.0D;
		double total_fuel = 0.0D;
		double running_mpg = 0.0D;

		for (int i = 0; i < m_amounts.size() - 1; i++) {
			total_fuel += m_amounts.get(i);
			double mile_diff = m_miles.get(i) - m_miles.get(i + 1);
			total_miles += mile_diff;
			double mpg = m_engine.calculateEconomy(mile_diff, m_amounts.get(i));
			if (m_engine.better(mpg, maximum_mpg)) {
				maximum_mpg = mpg;
			}
			if (m_engine.worse(mpg, minimum_mpg)) {
				minimum_mpg = mpg;
			}

			if (i == 0) {
				running_mpg = mpg;
			}
		}
		// note that you don't sum up ALL of m_amounts because the last one
		// (which is the oldest in history terms) does not relate to the average
		// mileage.

		average_mpg = m_engine.calculateEconomy(total_miles, total_fuel);

		data.put(R.id.stats_economy_average, string(average_mpg, m_engine.getEconomyUnits()));
		data.put(R.id.stats_economy_maximum, string(maximum_mpg, m_engine.getEconomyUnits()));
		data.put(R.id.stats_economy_minimum, string(minimum_mpg, m_engine.getEconomyUnits()));
		data.put(R.id.stats_economy_running, string(running_mpg, m_engine.getEconomyUnits()));

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
		double yearly_cost = 0.0D;

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());

		long month_then = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) - 1, cal.get(Calendar.DAY_OF_MONTH)).getTimeInMillis();
		long year_then = new GregorianCalendar(cal.get(Calendar.YEAR) - 1, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).getTimeInMillis();

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

			long date = m_dates.get(i);
			if (date > month_then) {
				thirty_day_cost += cost;
			}

			if (date > year_then) {
				yearly_cost += cost;
			}

			total_cost += cost_ppg;
			total_fuel += amount;
			total_expense += cost;
		}

		data.put(R.id.stats_price_latest, string(m_pref.getCurrency(), m_costs.get(0), "/" + m_engine.getVolumeUnitsAbbr().trim()));
		data.put(R.id.stats_price_average, string(m_pref.getCurrency(), total_cost / m_costs.size(), "/" + m_engine.getVolumeUnitsAbbr().trim()));
		data.put(R.id.stats_price_minimum, string(m_pref.getCurrency(), lowest_ppg, "/" + m_engine.getVolumeUnitsAbbr().trim()));
		data.put(R.id.stats_price_maximum, string(m_pref.getCurrency(), highest_ppg, "/" + m_engine.getVolumeUnitsAbbr().trim()));
		data.put(R.id.stats_amount_total, string(total_fuel, m_engine.getVolumeUnitsAbbr()));
		data.put(R.id.stats_amount_last, string(m_amounts.get(0), m_engine.getVolumeUnitsAbbr()));
		data.put(R.id.stats_amount_average, string(total_fuel / m_amounts.size(), m_engine.getVolumeUnitsAbbr()));
		data.put(R.id.stats_amount_average_cost, string(m_pref.getCurrency(), total_expense / m_costs.size()));
		data.put(R.id.stats_amount_maximum, string(highest_amt, m_engine.getVolumeUnitsAbbr()));
		data.put(R.id.stats_amount_minimum, string(lowest_amt, m_engine.getVolumeUnitsAbbr()));
		data.put(R.id.stats_amount_maximum_cost, string(m_pref.getCurrency(), highest_cost));
		data.put(R.id.stats_amount_minimum_cost, string(m_pref.getCurrency(), lowest_cost));
		data.put(R.id.stats_expense_thirty_days, string(m_pref.getCurrency(), thirty_day_cost));
		data.put(R.id.stats_expense_running, string(m_pref.getCurrency(), total_expense));
		data.put(R.id.stats_expense_yearly, string(m_pref.getCurrency(), yearly_cost));
		data.put(R.id.stats_cost_last, string(m_pref.getCurrency(), m_costs.get(0) * m_amounts.get(0)));

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

	private String string(double val, String postfix) {
		return string("", val, postfix);
	}

	private String string(String prefix, double val) {
		return string(prefix, val, "");
	}

	private String string(String prefix, double val, String postfix) {
		String str = prefix + m_pref.format(val) + postfix;
		return str;
	}
}
