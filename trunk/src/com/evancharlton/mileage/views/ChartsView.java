package com.evancharlton.mileage.views;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.PreferencesProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.TabChildActivity;
import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Vehicle;

public class ChartsView extends TabChildActivity {
	private Button m_fuelPriceBtn;
	private Button m_fuelAmountBtn;
	private Button m_distanceBtn;
	private Button m_fuelEconomyBtn;
	private Spinner m_vehicles;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.charts);
	}

	public void onResume() {
		super.onResume();
		initUI();
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
				HelpDialog.create(this, R.string.help_title_charts, R.string.help_charts);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private int calculateSkip(int size, int max) {
		if (size > max) {
			return (int) (Math.ceil((double) size) / ((double) max));
		}
		return 1;
	}

	private void initUI() {
		m_vehicles = (Spinner) findViewById(R.id.stats_vehicle_spinner);
		m_fuelPriceBtn = (Button) findViewById(R.id.fuel_price_btn);
		m_fuelAmountBtn = (Button) findViewById(R.id.fuel_amount_btn);
		m_distanceBtn = (Button) findViewById(R.id.delta_distance_btn);
		m_fuelEconomyBtn = (Button) findViewById(R.id.fuel_economy_btn);

		m_fuelPriceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				XYSeries series = new XYSeries("Price of fuel");
				Vehicle v = new Vehicle(ChartsView.this, m_vehicles.getSelectedItemId());
				List<FillUp> fillups = v.getAllFillUps(PreferencesProvider.getInstance(ChartsView.this).getCalculator());
				int size = fillups.size();
				int skip = calculateSkip(size, 50);
				for (int i = 0; i < size; i += skip) {
					FillUp fillup = fillups.get(i);
					series.add(fillup.getDate().getTimeInMillis(), fillup.getPrice());
				}
				showChart(series);
			}
		});

		m_fuelAmountBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				XYSeries series = new XYSeries("Amount of fuel");
				Vehicle v = new Vehicle(ChartsView.this, m_vehicles.getSelectedItemId());
				List<FillUp> fillups = v.getAllFillUps(PreferencesProvider.getInstance(ChartsView.this).getCalculator());
				int size = fillups.size();
				int skip = calculateSkip(size, 50);
				for (int i = 0; i < size; i += skip) {
					FillUp fillup = fillups.get(i);
					series.add(fillup.getDate().getTimeInMillis(), fillup.getAmount());
				}
				showChart(series);
			}
		});

		m_distanceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				XYSeries series = new XYSeries("Distance between fill-ups");
				Vehicle v = new Vehicle(ChartsView.this, m_vehicles.getSelectedItemId());
				List<FillUp> fillups = v.getAllFillUps(PreferencesProvider.getInstance(ChartsView.this).getCalculator());
				int size = fillups.size();
				int skip = calculateSkip(size, 50);
				for (int i = 0; i < size; i += skip) {
					FillUp fillup = fillups.get(i);
					series.add(fillup.getDate().getTimeInMillis(), fillup.calcDistance());
				}
				showChart(series);
			}
		});

		m_fuelEconomyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				XYSeries series = new XYSeries("Fuel economy");
				Vehicle v = new Vehicle(ChartsView.this, m_vehicles.getSelectedItemId());
				List<FillUp> fillups = v.getAllFillUps(PreferencesProvider.getInstance(ChartsView.this).getCalculator());
				int size = fillups.size();
				int skip = calculateSkip(size, 50);
				for (int i = 0; i < size; i += skip) {
					FillUp fillup = fillups.get(i);
					series.add(fillup.getDate().getTimeInMillis(), fillup.calcEconomy());
				}
				showChart(series);
			}
		});

		m_vehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				updateVehicleSelection(position);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		populateSpinner();
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

		if (vehicleAdapter.getCount() == 1) {
			m_vehicles.setVisibility(View.GONE);
		}
	}

	private void showChart(XYSeries series) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		seriesRenderer.setColor(0xFFFF9C24);
		renderer.addSeriesRenderer(seriesRenderer);
		renderer.setShowLegend(false);
		renderer.setAxesColor(0x66666666);

		PreferencesProvider prefs = PreferencesProvider.getInstance(this);
		String format = prefs.getString(PreferencesProvider.DATE, "MM/dd/yy");
		startActivity(ChartFactory.getTimeChartIntent(ChartsView.this, dataset, renderer, format));
	}
}
