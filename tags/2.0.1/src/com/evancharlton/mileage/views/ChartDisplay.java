package com.evancharlton.mileage.views;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.evancharlton.mileage.PreferencesProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.Vehicle;

public abstract class ChartDisplay extends Activity {
	protected LineChart m_chart;
	public static final String VEHICLE_ID = "vehicle_id";
	protected List<FillUp> m_fillups;
	protected Vehicle m_vehicle;
	protected PreferencesProvider m_prefs;
	protected DecimalFormat m_format;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chart);

		m_chart = (LineChart) findViewById(R.id.chart);

		m_prefs = PreferencesProvider.getInstance(this);
		m_vehicle = new Vehicle(getIntent().getExtras().getLong(VEHICLE_ID));
		m_fillups = m_vehicle.getAllFillUps(m_prefs.getCalculator());

		m_format = new DecimalFormat("0.00");

		buildChart();
	}

	protected abstract void buildChart();
}
