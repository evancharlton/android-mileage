package com.evancharlton.mileage.views;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.evancharlton.mileage.HelpDialog;
import com.evancharlton.mileage.Mileage;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.binders.VehicleBinder;
import com.evancharlton.mileage.models.Vehicle;

public class ChartsView extends Activity {
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

	private void initUI() {
		m_vehicles = (Spinner) findViewById(R.id.stats_vehicle_spinner);
		m_fuelPriceBtn = (Button) findViewById(R.id.fuel_price_btn);
		m_fuelAmountBtn = (Button) findViewById(R.id.fuel_amount_btn);
		m_distanceBtn = (Button) findViewById(R.id.delta_distance_btn);
		m_fuelEconomyBtn = (Button) findViewById(R.id.fuel_economy_btn);

		m_fuelPriceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showChart(FuelPriceChart.class);
			}
		});

		m_fuelAmountBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showChart(FuelAmountChart.class);
			}
		});

		m_distanceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showChart(DistanceChart.class);
			}
		});

		m_fuelEconomyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				showChart(FuelEconomyChart.class);
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

		if (vehicleAdapter.getCount() == 1) {
			m_vehicles.setVisibility(View.GONE);
		}
	}

	private void showChart(Class<?> cls) {
		Intent i = new Intent();
		i.setClass(ChartsView.this, cls);
		i.putExtra(ChartDisplay.VEHICLE_ID, m_vehicles.getSelectedItemId());
		startActivity(i);
	}
}
