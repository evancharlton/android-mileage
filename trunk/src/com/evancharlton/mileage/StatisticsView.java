package com.evancharlton.mileage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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

	private static final int DIALOG_STATS_PROGRESS = 1;

	private ProgressDialog m_dlg = null;

	private CalculationTask m_calculationTask;

	@Override
	protected String getTag() {
		return "StatisticsView";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		m_calculationTask = (CalculationTask) getLastNonConfigurationInstance();
		if (m_calculationTask == null) {
			m_calculationTask = new CalculationTask();
		}
		m_calculationTask.activity = this;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// FIXME: This is horrible. Why am I recalculating on screen rotation?
		return m_calculationTask.getStatus() == AsyncTask.Status.FINISHED ? null : m_calculationTask;
	}

	public void onResume() {
		super.onResume();
		m_preferences = PreferencesProvider.getInstance(this);
		m_calcEngine = m_preferences.getCalculator();

		initUI();
		populateSpinner();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		Mileage.createMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return Mileage.parseMenuItem(item, this) || super.onOptionsItemSelected(item);
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
		LinearLayout container = (LinearLayout) findViewById(R.id.stats_container);
		container.removeAllViews();

		if (m_calculationTask.getStatus() == AsyncTask.Status.PENDING) {
			m_calculationTask.execute(id);
		} else if (m_calculationTask.getStatus() == AsyncTask.Status.RUNNING) {
			showDialog(DIALOG_STATS_PROGRESS);
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_STATS_PROGRESS:
				m_dlg = new ProgressDialog(this);
				m_dlg.setTitle(getString(R.string.calculating));
				m_dlg.setMessage(getString(R.string.statistics_calculating));
				m_dlg.setIndeterminate(false);
				m_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_dlg.setProgress(0);
				m_dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						if (m_calculationTask != null && m_calculationTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
							m_calculationTask.cancel(true);
						}
					}
				});
				return m_dlg;
		}
		return null;
	}

	public StatisticsGroup calcDistances(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.distance_between_fillups));

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

		group.add(new Statistic(getString(R.string.average), (total_distance / (fillups.size() - 1)), m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic(getString(R.string.maximum), max_distance, m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic(getString(R.string.minimum), min_distance, m_calcEngine.getDistanceUnitsAbbr()));
		group.add(new Statistic(getString(R.string.last), fillups.get(fillups.size() - 1).calcDistance(), m_calcEngine.getDistanceUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcEconomy(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fuel_economy));

		double total_distance = fillups.get(fillups.size() - 1).getOdometer() - fillups.get(0).getOdometer();
		double total_fuel = 0D;
		for (int i = fillups.size() - 1; i > 0; i--) {
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
				double economy = fillup.getEconomy();
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

		group.add(new Statistic(getString(R.string.average), m_calcEngine.calculateEconomy(total_distance, total_fuel), m_calcEngine.getEconomyUnits()));
		group.add(new Statistic("Best", max_economy, m_calcEngine.getEconomyUnits()));
		group.add(new Statistic("Worst", min_economy, m_calcEngine.getEconomyUnits()));
		group.add(new Statistic(getString(R.string.last), fillups.get(fillups.size() - 1).getEconomy(), m_calcEngine.getEconomyUnits()));

		return group;
	}

	public StatisticsGroup calcPrices(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fuel_prices));

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

		group.add(new Statistic(getString(R.string.average), m_preferences.getCurrency(), total_price / fillups.size(), "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.maximum), m_preferences.getCurrency(), max_price, "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.minimum), m_preferences.getCurrency(), min_price, "/" + m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.last), m_preferences.getCurrency(), fillups.get(fillups.size() - 1).getPrice(), "/" + m_calcEngine.getVolumeUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcCosts(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fillup_costs));

		double min_cost = Double.MAX_VALUE;
		double max_cost = 0D;
		double total_cost = 0D;
		double min_cost_per_mile = Double.MAX_VALUE;
		double max_cost_per_mile = 0D;
		double avg_cost_per_mile = 0D;
		int count = 0;

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
			avg_cost_per_mile += cost_per_mile;
			count++;
		}

		FillUp last = fillups.get(fillups.size() - 1);
		double last_cost = last.calcCost();
		double last_cost_per_mile = last.calcCostPerDistance();

		double avg_cost = total_cost / fillups.size();

		avg_cost_per_mile = avg_cost_per_mile / ((double) count);

		String distanceUnits = m_calcEngine.getDistanceUnitsAbbr().trim();

		group.add(new Statistic(getString(R.string.average), m_preferences.getCurrency(), avg_cost));
		group.add(new Statistic(getString(R.string.maximum), m_preferences.getCurrency(), max_cost));
		group.add(new Statistic(getString(R.string.minimum), m_preferences.getCurrency(), min_cost));
		group.add(new Statistic(getString(R.string.last), m_preferences.getCurrency(), last_cost));
		DecimalFormat fmt = new DecimalFormat("0.000");
		group.add(new Statistic(String.format(getString(R.string.average_cost_per), distanceUnits), m_preferences.getCurrency(), avg_cost_per_mile, fmt));
		group.add(new Statistic(String.format(getString(R.string.maximum_cost_per), distanceUnits), m_preferences.getCurrency(), max_cost_per_mile, fmt));
		group.add(new Statistic(String.format(getString(R.string.minimum_cost_per), distanceUnits), m_preferences.getCurrency(), min_cost_per_mile, fmt));
		group.add(new Statistic(String.format(getString(R.string.last_cost_per), distanceUnits), m_preferences.getCurrency(), last_cost_per_mile, fmt));

		return group;
	}

	public StatisticsGroup calcAmounts(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fuel_amounts));

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

		group.add(new Statistic(getString(R.string.total), total_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.average), total_amount / fillups.size(), m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.maximum), max_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.minimum), min_amount, m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.last), fillups.get(fillups.size() - 1).getAmount(), m_calcEngine.getVolumeUnitsAbbr()));
		group.add(new Statistic(getString(R.string.fuel_per, ten_thousand_miles, m_calcEngine.getDistanceUnitsAbbr()), fuel_per_10k, m_calcEngine.getVolumeUnitsAbbr()));

		return group;
	}

	public StatisticsGroup calcExpenses(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fuel_expenses));

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

		group.add(new Statistic(getString(R.string.total), m_preferences.getCurrency(), total));
		group.add(new Statistic(getString(R.string.last_month), m_preferences.getCurrency(), thirty_day_total));
		group.add(new Statistic(getString(R.string.last_year), m_preferences.getCurrency(), yearly_total));

		return group;
	}

	public StatisticsGroup calcLocations(final List<FillUp> fillups) {
		StatisticsGroup group = new StatisticsGroup(getString(R.string.fillup_locations));

		if (m_preferences.getBoolean(PreferencesProvider.LOCATION, true)) {
			double north = -90;
			double south = 90;
			double east = -180;
			double west = 180;

			double lat, lon;
			for (FillUp f : fillups) {
				lat = f.getLatitude();
				lon = f.getLongitude();

				if (lat >= north) {
					north = lat;
				}

				if (lat <= south) {
					south = lat;
				}

				if (lon >= east) {
					east = lon;
				}

				if (lon <= west) {
					west = lon;
				}
			}

			final String DEG = "";
			final DecimalFormat fmt = new DecimalFormat("0.0000");
			group.add(new Statistic(getString(R.string.location_north), north, DEG, fmt));
			group.add(new Statistic(getString(R.string.location_south), south, DEG, fmt));
			group.add(new Statistic(getString(R.string.location_east), east, DEG, fmt));
			group.add(new Statistic(getString(R.string.location_west), west, DEG, fmt));
		}

		return group;
	}

	private static class CalculationTask extends AsyncTask<Long, StatisticsGroup, Boolean> {
		public StatisticsView activity;

		private int t = 0;
		private int p = 0;

		private LinearLayout getContainer() {
			return (LinearLayout) activity.findViewById(R.id.stats_container);
		}

		@Override
		protected void onPreExecute() {
			activity.showDialog(DIALOG_STATS_PROGRESS);
		}

		@Override
		protected Boolean doInBackground(Long... ids) {
			// TODO: optimize this. This has huge overhead
			List<FillUp> fillups = new ArrayList<FillUp>();
			String[] projection = FillUp.getProjection();
			String selection = FillUp.VEHICLE_ID + " = ?";
			String[] selectionArgs = new String[] {
				String.valueOf(ids[0])
			};

			Cursor c = activity.getContentResolver().query(FillUp.CONTENT_URI, projection, selection, selectionArgs, FillUp.ODOMETER + " ASC");
			c.moveToFirst();

			t = c.getCount();
			publishProgress();

			FillUp prev = null;
			FillUp curr = null;
			while (c.isAfterLast() == false) {
				curr = new FillUp(activity.m_calcEngine, c);
				fillups.add(curr);

				curr.setPrevious(prev);
				if (prev != null) {
					prev.setNext(curr);
				}
				prev = curr;

				c.moveToNext();
				p++;
				publishProgress();
			}

			c.close();

			if (fillups.size() >= 2) {
				// crunch the numbers
				// TODO: Is there a way to remove this copy-pasta?
				if (!isCancelled())
					publishProgress(activity.calcDistances(fillups));
				if (!isCancelled())
					publishProgress(activity.calcEconomy(fillups));
				if (!isCancelled())
					publishProgress(activity.calcPrices(fillups));
				if (!isCancelled())
					publishProgress(activity.calcCosts(fillups));
				if (!isCancelled())
					publishProgress(activity.calcAmounts(fillups));
				if (!isCancelled())
					publishProgress(activity.calcExpenses(fillups));
				if (!isCancelled())
					publishProgress(activity.calcLocations(fillups));
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(StatisticsGroup... update) {
			if (!isCancelled()) {
				if (update.length == 0) {
					if (activity != null && activity.m_dlg != null) {
						activity.m_dlg.setMax(t);
						activity.m_dlg.setProgress(p);
					}
				} else {
					getContainer().addView(update[0].render(activity));
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.removeDialog(DIALOG_STATS_PROGRESS);
		}
	}
}
