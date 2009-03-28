package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Statistic;
import com.evancharlton.mileage.models.StatisticsGroup;
import com.evancharlton.mileage.models.Vehicle;

public class StatisticsView extends TabChildActivity {
	private Spinner m_vehicles;
	private PreferencesProvider m_preferences;
	private CalculationEngine m_calcEngine;

	private static final String DATA_GROUP = "group";
	private static final String DATA_DONE = "done";

	private static final int DIALOG_STATS_PROGRESS = 1;

	private static final int MSG_TOTAL = 1;
	private static final int MSG_UPDATE = 2;
	private static final int MSG_CALCULATING = 3;

	private ProgressDialog m_dlg = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);
	}

	public void onResume() {
		super.onResume();
		m_preferences = PreferencesProvider.getInstance(this);
		m_calcEngine = m_preferences.getCalculator();

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
	}

	private void populateSpinner() {
		Cursor c = managedQuery(Vehicle.CONTENT_URI, Vehicle.getProjection(), null, null, Vehicle.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter vehicleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[] {
			Vehicle.TITLE
		}, new int[] {
			android.R.id.text1
		});
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vehicleAdapter.setViewBinder(new VehicleBinder());
		m_vehicles.setAdapter(vehicleAdapter);

		setVehicleSelection(m_vehicles);
		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				updateVehicleSelection(position);
				calculateStatistics(m_vehicles.getSelectedItemId());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		if (vehicleAdapter.getCount() == 1) {
			m_vehicles.setVisibility(View.GONE);
			calculateStatistics(vehicleAdapter.getItemId(0));
		}
	}

	private void calculateStatistics(final long id) {
		// first, clean up the UI
		LinearLayout container = (LinearLayout) findViewById(R.id.stats_container);
		container.removeAllViews();

		final Handler statsHandler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				boolean done = data.getBoolean(DATA_DONE, false);
				if (!done) {
					LinearLayout container = (LinearLayout) findViewById(R.id.stats_container);
					StatisticsGroup group = (StatisticsGroup) data.getSerializable(DATA_GROUP);
					if (group != null) {
						container.addView(group.render(StatisticsView.this));
					}
				} else {
					dismissDialog(DIALOG_STATS_PROGRESS);
					m_dlg = null;
				}
			}
		};

		final Thread t = new Thread() {
			public void run() {
				// TODO: optimize this. This has huge overhead
				final List<FillUp> fillups = getAllFillUps(id, m_calcEngine);

				if (fillups.size() >= 2) {
					// crunch the numbers
					send(calcDistances(fillups));
					send(calcEconomy(fillups));
					send(calcPrices(fillups));
					send(calcCosts(fillups));
					send(calcAmounts(fillups));
					send(calcExpenses(fillups));
				}

				statsHandler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putBoolean(DATA_DONE, true);
						Message msg = new Message();
						msg.setData(data);
						statsHandler.handleMessage(msg);
					}
				});
			}

			private void send(final StatisticsGroup results) {
				statsHandler.post(new Runnable() {
					public void run() {
						Bundle data = new Bundle();
						data.putSerializable(DATA_GROUP, results);
						Message msg = new Message();
						msg.setData(data);
						statsHandler.handleMessage(msg);
					}
				});
			}
		};
		t.start();
		showDialog(DIALOG_STATS_PROGRESS);
	}

	private List<FillUp> getAllFillUps(long id, CalculationEngine engine) {
		List<FillUp> all = new ArrayList<FillUp>();
		String[] projection = FillUp.getProjection();
		String selection = FillUp.VEHICLE_ID + " = ?";
		String[] selectionArgs = new String[] {
			String.valueOf(id)
		};

		Cursor c = managedQuery(FillUp.CONTENT_URI, projection, selection, selectionArgs, FillUp.ODOMETER + " ASC");
		c.moveToFirst();

		final int t = c.getCount();
		calculationHandler.post(new Runnable() {
			public void run() {
				Message msg = new Message();
				msg.what = t;
				msg.arg1 = MSG_TOTAL;
				calculationHandler.sendMessage(msg);
			}
		});

		int i = 0;
		FillUp prev = null;
		FillUp curr = null;
		while (c.isAfterLast() == false) {
			curr = new FillUp(engine, c);
			all.add(curr);

			curr.setPrevious(prev);
			if (prev != null) {
				prev.setNext(curr);
			}
			prev = curr;

			c.moveToNext();

			final int p = ++i;
			calculationHandler.post(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = p;
					msg.arg1 = MSG_UPDATE;
					calculationHandler.sendMessage(msg);
				}
			});
		}
		calculationHandler.post(new Runnable() {
			public void run() {
				Message msg = new Message();
				msg.arg1 = MSG_CALCULATING;
				calculationHandler.sendMessage(msg);
			}
		});

		return all;
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_STATS_PROGRESS:
				m_dlg = new ProgressDialog(this);
				m_dlg.setTitle("Calculating");
				m_dlg.setMessage("Calculating statistics...");
				m_dlg.setIndeterminate(false);
				m_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_dlg.setProgress(0);
				return m_dlg;
		}
		return null;
	}

	public StatisticsGroup calcDistances(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Distance Between Fill-Ups");

		double total_distance = 0D;
		double min_distance = Double.MAX_VALUE;
		double max_distance = 0D;

		for (FillUp fillup : fillups) {
			double distance = fillup.calcDistance();
			if (distance > 0) {
				total_distance += distance;
				if (distance < min_distance) {
					min_distance = distance;
				}
				if (distance > max_distance) {
					max_distance = distance;
				}
			}
		}

		group.add(new Statistic("Average", (total_distance / (fillups.size() - 1)), m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic("Maximum", max_distance, m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic("Minimum", min_distance, m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic("Last", fillups.get(fillups.size() - 1).calcDistance(), m_calcEngine.getDistanceUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcEconomy(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Fuel Economy");

		double total_distance = fillups.get(fillups.size() - 1).getOdometer() - fillups.get(0).getOdometer();
		double total_fuel = 0D;
		for (int i = 0; i < fillups.size() - 1; i++) {
			total_fuel += fillups.get(i).getAmount();
		}

		double max_economy = 0D;
		double min_economy = Double.MAX_VALUE;
		if (m_calcEngine.isInverted()) {
			max_economy = Double.MAX_VALUE;
			min_economy = 0D;
		}
		for (FillUp fillup : fillups) {
			if (!fillup.isPartial()) {
				double economy = fillup.calcEconomy();
				if (economy < 0) {
					continue;
				}
				if (economy > 0) {
					if (m_calcEngine.better(economy, max_economy)) {
						max_economy = economy;
					}
					if (m_calcEngine.worse(economy, min_economy)) {
						min_economy = economy;
					}
				}
			}
		}

		group.add(new Statistic("Average", m_calcEngine.calculateEconomy(total_distance, total_fuel), m_calcEngine.getEconomyUnits()));
		group.add(new Statistic("Maximum", max_economy, m_calcEngine.getEconomyUnits()));
		group.add(new Statistic("Minimum", min_economy, m_calcEngine.getEconomyUnits()));
		group.add(new Statistic("Last", fillups.get(fillups.size() - 1).calcEconomy(), m_calcEngine.getEconomyUnits()));

		return group;
	}

	public StatisticsGroup calcPrices(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Fuel Prices");

		double min_price = Double.MAX_VALUE;
		double max_price = 0D;
		double total_price = 0D;

		for (FillUp fillup : fillups) {
			double price = fillup.getPrice();
			total_price += price;
			if (price < min_price) {
				min_price = price;
			}
			if (price > max_price) {
				max_price = price;
			}
		}

		group.add(new Statistic("Average", m_preferences.getCurrency(), total_price / fillups.size(), "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Maximum", m_preferences.getCurrency(), max_price, "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Minimum", m_preferences.getCurrency(), min_price, "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Last", m_preferences.getCurrency(), fillups.get(fillups.size() - 1).getPrice(), "/" + m_calcEngine.getVolumeUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcCosts(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Fill-Up Costs");

		double min_cost = Double.MAX_VALUE;
		double max_cost = 0D;
		double total_cost = 0D;
		double min_cost_per_mile = Double.MAX_VALUE;
		double max_cost_per_mile = 0D;

		for (FillUp fillup : fillups) {
			double cost = fillup.calcCost();
			total_cost += cost;
			if (cost < min_cost) {
				min_cost = cost;
			}
			if (cost > max_cost) {
				max_cost = cost;
			}

			double cost_per_mile = fillup.calcCostPerDistance();
			if (cost_per_mile < 0) {
				continue;
			}
			if (cost_per_mile < min_cost_per_mile) {
				min_cost_per_mile = cost_per_mile;
			}
			if (cost_per_mile > max_cost_per_mile) {
				max_cost_per_mile = cost_per_mile;
			}
		}

		FillUp last = fillups.get(fillups.size() - 1);
		double last_cost = last.calcCost();
		double last_cost_per_mile = last.calcCostPerDistance();

		double avg_cost = total_cost / fillups.size();
		double total_distance = last.getOdometer() - fillups.get(0).getOdometer();
		double avg_cost_per_mile = total_cost / total_distance;

		String distanceUnits = m_calcEngine.getDistanceUnitsAbbr().trim();

		group.add(new Statistic("Average", m_preferences.getCurrency(), avg_cost));
		group.add(new Statistic(String.format("Average Cost per %s", distanceUnits), m_preferences.getCurrency(), avg_cost_per_mile));
		group.add(new Statistic("Maximum", m_preferences.getCurrency(), max_cost));
		group.add(new Statistic(String.format("Maximum cost per %s", distanceUnits), m_preferences.getCurrency(), max_cost_per_mile));
		group.add(new Statistic("Minimum", m_preferences.getCurrency(), min_cost));
		group.add(new Statistic(String.format("Minimum cost per %s", distanceUnits), m_preferences.getCurrency(), min_cost_per_mile));
		group.add(new Statistic("Last", m_preferences.getCurrency(), last_cost));
		group.add(new Statistic(String.format("Last Cost per %s", distanceUnits), m_preferences.getCurrency(), last_cost_per_mile));

		return group;
	}

	public StatisticsGroup calcAmounts(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Fuel Amounts");

		double min_amount = Double.MAX_VALUE;
		double max_amount = 0D;
		double total_amount = 0D;

		for (FillUp fillup : fillups) {
			double amount = fillup.getAmount();
			total_amount += amount;
			if (amount < min_amount) {
				min_amount = amount;
			}
			if (amount > max_amount) {
				max_amount = amount;
			}
		}

		int ten_thousand_miles = (int) Math.ceil(m_calcEngine.convertDistance(PreferencesProvider.MILES, m_calcEngine.getOutputDistance(), 10000));
		double distance = fillups.get(fillups.size() - 1).getOdometer() - fillups.get(0).getOdometer();
		double fuel_per_10k = (m_calcEngine.convertVolume(total_amount) / m_calcEngine.convertDistance(distance)) * ten_thousand_miles;

		group.add(new Statistic("Total", total_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Average", total_amount / fillups.size(), m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Maximum", max_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Minimum", min_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic("Last", fillups.get(fillups.size() - 1).calcCost(), m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(String.format("Fuel / %d %s", ten_thousand_miles, m_calcEngine.getDistanceUnitsAbbr()), fuel_per_10k, m_calcEngine.getVolumeUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcExpenses(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup("Fuel Expenses");

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());

		long thirty_days_ago = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) - 1, cal.get(Calendar.DAY_OF_MONTH)).getTimeInMillis();
		long one_year_ago = new GregorianCalendar(cal.get(Calendar.YEAR) - 1, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).getTimeInMillis();

		double thirty_day_total = 0D;
		double yearly_total = 0D;
		double total = 0D;

		for (FillUp fillup : fillups) {
			total += fillup.calcCost();
			long time = fillup.getDate().getTimeInMillis();
			if (time > thirty_days_ago) {
				thirty_day_total += fillup.calcCost();
			}
			if (time > one_year_ago) {
				yearly_total += fillup.calcCost();
			}
		}

		group.add(new Statistic("Total", m_preferences.getCurrency(), total));
		group.add(new Statistic("Last Month", m_preferences.getCurrency(), thirty_day_total));
		group.add(new Statistic("Last Year", m_preferences.getCurrency(), yearly_total));

		return group;
	}

	private Handler calculationHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg1 == MSG_TOTAL) {
				if (m_dlg != null) {
					m_dlg.setMax(msg.what);
				}
			} else if (msg.arg1 == MSG_UPDATE) {
				if (m_dlg != null) {
					m_dlg.setProgress(msg.what);
				}
			} else if (msg.arg1 == MSG_CALCULATING) {
				if (m_dlg != null) {
					m_dlg.setIndeterminate(true);
				}
			}
		}
	};
}
