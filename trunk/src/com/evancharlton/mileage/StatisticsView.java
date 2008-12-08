package com.evancharlton.mileage;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.calculators.CalculationEngine;

public class StatisticsView extends Activity {
	private static final int MAX_DATA_POINTS = 100;

	private HashMap<Integer, TextView> m_stats = new HashMap<Integer, TextView>();
	private Spinner m_vehicles;
	private ArrayList<Double> m_amounts;
	private ArrayList<Double> m_costs;
	private ArrayList<Long> m_dates;
	private ArrayList<Double> m_miles;
	private PreferencesProvider m_pref;
	private CalculationEngine m_engine;
	private Button m_fuelAmountBtn;
	private Button m_fuelPriceBtn;
	private Button m_economyBtn;
	private Button m_distanceBtn;
	private DecimalFormat m_format = new DecimalFormat("0.00");

	// TODO: set chs to be the same as the display size
	private static final String CHART_URL_BASE = "http://chart.apis.google.com/chart?cht=lc&chs=480x320&chd=t:";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);
	}

	public void onResume() {
		super.onResume();
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
				HelpDialog.create(this, R.string.help_title_statistics, new int[] {
						R.string.help_statistics,
						R.string.help_charts
				});
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

		m_fuelPriceBtn = (Button) findViewById(R.id.fuel_price_btn);
		m_fuelAmountBtn = (Button) findViewById(R.id.fuel_amount_btn);
		m_distanceBtn = (Button) findViewById(R.id.delta_distance_btn);
		m_economyBtn = (Button) findViewById(R.id.fuel_economy_btn);

		m_fuelPriceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuilder builder = new StringBuilder();
				builder.append(CHART_URL_BASE);
				double total = 0D;
				double max = 0D;
				double min = Double.MAX_VALUE;
				int min_index = 0;
				int max_index = 0;
				int skip = 1;
				if (m_costs.size() > MAX_DATA_POINTS) {
					skip = (int) Math.floor((double) m_costs.size() / (double) MAX_DATA_POINTS);
				}
				for (int i = 0; i < m_costs.size(); i += skip) {
					double price = m_costs.get(i);
					builder.append(m_format.format(price)).append(",");
					total += price;
					if (price > max) {
						max = price;
						max_index = i;
					}
					if (price < min) {
						min = price;
						min_index = i;
					}
				}
				double avg = total / (MAX_DATA_POINTS > m_costs.size() ? m_costs.size() : MAX_DATA_POINTS);
				double chart_max = Math.ceil(max);
				double chart_min = Math.floor(min);
				double avg_percent = ((avg - chart_min) / (chart_max - chart_min));
				builder.deleteCharAt(builder.length() - 1);

				setUpChart(builder, chart_min, chart_max, avg_percent);
				chartTitle(builder, "Fuel Price vs Time");

				long end_time = m_dates.get(0);
				long start_time = m_dates.get(m_dates.size() - 1);
				long min_time = m_dates.get(min_index);
				long max_time = m_dates.get(max_index);
				double min_pos = (((double) min_index) / ((double) m_dates.size())) * 100D;
				double max_pos = (((double) max_index) / ((double) m_dates.size())) * 100D;

				addLabels(builder, start_time, end_time, min_time, min_pos, max_time, max_pos, avg, avg_percent, chart_min, chart_max);

				showChart(builder);
			}
		});

		m_fuelAmountBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuilder builder = new StringBuilder();
				builder.append(CHART_URL_BASE);
				double total = 0D;
				double max = 0D;
				double min = Double.MAX_VALUE;
				int max_index = 0;
				int min_index = 0;
				int skip = 1;
				if (m_amounts.size() > MAX_DATA_POINTS) {
					skip = (int) Math.floor((double) m_amounts.size() / (double) MAX_DATA_POINTS);
				}
				for (int i = 0; i < m_amounts.size(); i += skip) {
					double amount = m_amounts.get(i);
					builder.append(m_format.format(amount)).append(",");
					total += amount;

					if (amount > max) {
						max = amount;
						max_index = i;
					}
					if (amount < min) {
						min = amount;
						min_index = i;
					}
				}
				double avg = total / (MAX_DATA_POINTS > m_amounts.size() ? m_amounts.size() : MAX_DATA_POINTS);
				double chart_max = Math.ceil(max);
				double chart_min = 0D; // TODO: support this later?
				double avg_percent = (avg / chart_max);
				builder.deleteCharAt(builder.length() - 1);

				setUpChart(builder, 0, chart_max, avg_percent);
				chartTitle(builder, "Amount of Fuel per Fill-Up vs Time");

				long end_time = m_dates.get(0);
				long start_time = m_dates.get(m_dates.size() - 1);
				long min_time = m_dates.get(min_index);
				long max_time = m_dates.get(max_index);
				double min_pos = (((double) min_index) / ((double) m_dates.size())) * 100D;
				double max_pos = (((double) max_index) / ((double) m_dates.size())) * 100D;

				addLabels(builder, start_time, end_time, min_time, min_pos, max_time, max_pos, avg, avg_percent, chart_min, chart_max);

				showChart(builder);
			}
		});

		m_economyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuilder builder = new StringBuilder();
				builder.append(CHART_URL_BASE);
				PreferencesProvider prefs = PreferencesProvider.getInstance(StatisticsView.this);
				CalculationEngine engine = prefs.getCalculator();
				double min = engine.getBestEconomy();
				double max = engine.getWorstEconomy();
				double fuel = 0D;
				int min_index = 0;
				int max_index = 0;
				int skip = 1;
				if (m_amounts.size() > MAX_DATA_POINTS) {
					skip = (int) Math.floor((double) m_amounts.size() / (double) MAX_DATA_POINTS);
				}
				double total_distance = 0D;
				for (int i = 0; i < m_miles.size() - skip; i += skip) {
					double distance = m_miles.get(i) - m_miles.get(i + skip);
					total_distance += distance;
					fuel += m_amounts.get(i);
					double economy = engine.calculateEconomy(distance, m_amounts.get(i));
					if (economy > max) {
						max = economy;
						max_index = i;
					}
					if (economy < min) {
						min = economy;
						min_index = i;
					}
					builder.append(m_format.format(economy)).append(",");
				}

				builder.deleteCharAt(builder.length() - 1);

				double chart_min = Math.floor(min);
				double chart_max = Math.ceil(max);
				double avg = engine.calculateEconomy(total_distance, fuel);
				double avg_percent = (avg - chart_min) / (chart_max - chart_min);

				setUpChart(builder, chart_min, chart_max, avg_percent, engine.getBestEconomy() < engine.getWorstEconomy());
				chartTitle(builder, "Fuel Economy vs Time");

				long end_time = m_dates.get(0);
				long start_time = m_dates.get(m_dates.size() - 1);
				long min_time = m_dates.get(min_index);
				long max_time = m_dates.get(max_index);
				double min_pos = (((double) min_index) / ((double) m_dates.size())) * 100D;
				double max_pos = (((double) max_index) / ((double) m_dates.size())) * 100D;

				addLabels(builder, start_time, end_time, min_time, min_pos, max_time, max_pos, avg, avg_percent, chart_min, chart_max);

				showChart(builder);
			}
		});

		m_distanceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuilder builder = new StringBuilder();
				builder.append(CHART_URL_BASE);
				double min = Double.MAX_VALUE;
				double max = 0D;
				double total = 0D;
				int min_index = 0;
				int max_index = 0;
				int skip = 1;
				if (m_amounts.size() > MAX_DATA_POINTS) {
					skip = (int) Math.floor((double) m_amounts.size() / (double) MAX_DATA_POINTS);
				}
				for (int i = 0; i < m_miles.size() - skip; i += skip) {
					double distance = m_miles.get(i) - m_miles.get(i + skip);
					builder.append(distance).append(",");
					total += distance;

					if (distance > max) {
						max = distance;
						max_index = i;
					}
					if (distance < min) {
						min = distance;
						min_index = i;
					}
				}
				builder.deleteCharAt(builder.length() - 1);

				double chart_min = Math.floor(min);
				double chart_max = Math.ceil(max);
				double avg = total / (MAX_DATA_POINTS > m_miles.size() ? m_miles.size() : MAX_DATA_POINTS);
				double avg_percent = (avg - chart_min) / (chart_max - chart_min);

				setUpChart(builder, chart_min, chart_max, avg_percent, false);
				chartTitle(builder, "Distance Between Fill-ups vs Time");

				long end_time = m_dates.get(0);
				long start_time = m_dates.get(m_dates.size() - 1);
				long min_time = m_dates.get(min_index);
				long max_time = m_dates.get(max_index);
				double min_pos = (((double) min_index) / ((double) m_dates.size())) * 100D;
				double max_pos = (((double) max_index) / ((double) m_dates.size())) * 100D;

				addLabels(builder, start_time, end_time, min_time, min_pos, max_time, max_pos, avg, avg_percent, chart_min, chart_max);

				showChart(builder);
			}
		});
	}

	private void chartTitle(StringBuilder builder, String title) {
		builder.append("&chtt=").append(URLEncoder.encode(title));
	}

	private void addLabels(StringBuilder builder, long start_time, long end_time, long min_time, double min_pos, long max_time, double max_pos, double avg, double avg_percent, double chart_min, double chart_max) {
		DecimalFormat format = new DecimalFormat("0.00");
		SimpleDateFormat monthFmt = new SimpleDateFormat("MMM");
		SimpleDateFormat yearFmt = new SimpleDateFormat("yyyy");

		Date start_date = new Date(start_time);
		Date end_date = new Date(end_time);
		Date min_date = new Date(min_time);
		Date max_date = new Date(max_time);

		builder.append("&chxt=x,x,y");
		builder.append("&chxl=");

		builder.append("0:|").append(monthFmt.format(start_date)).append("|");
		builder.append(monthFmt.format(min_date)).append("|");
		builder.append(monthFmt.format(max_date)).append("|");
		builder.append(monthFmt.format(end_date));

		builder.append("|1:|").append(yearFmt.format(start_date));
		builder.append("|").append(yearFmt.format(min_date));
		builder.append("|").append(yearFmt.format(max_date));
		builder.append("|").append(yearFmt.format(end_date));

		builder.append("|2:|").append(chart_min).append("|").append(format.format(avg)).append("|").append(chart_max);

		builder.append("&chxp=");
		builder.append("0,0,").append(min_pos).append(",").append(max_pos).append(",100");
		builder.append("|1,0,").append(min_pos).append(",").append(max_pos).append(",100");
		builder.append("|2,0,").append(format.format(avg_percent * 100)).append(",100");
	}

	private void showChart(StringBuilder url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url.toString()));
		startActivity(i);
	}

	private void setUpChart(StringBuilder builder, double chart_min, double chart_max, double avg_percent) {
		setUpChart(builder, chart_min, chart_max, avg_percent, true);
	}

	private void setUpChart(StringBuilder builder, double chart_min, double chart_max, double avg_percent, boolean good_on_bottom) {
		builder.append("&chco=000000");
		builder.append("&chds=").append(chart_min).append(",").append(chart_max);

		builder.append("&chm=");
		if (good_on_bottom) {
			builder.append("r,FF8880,0,").append(m_format.format(avg_percent)).append(",1|");
			builder.append("r,70FF9D,0,0,").append(m_format.format(avg_percent));
		} else {
			builder.append("r,70FF9D,0,").append(m_format.format(avg_percent)).append(",1|");
			builder.append("r,FF8880,0,0,").append(m_format.format(avg_percent));
		}
	};

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
		}, FillUps.MILEAGE + " DESC");

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
