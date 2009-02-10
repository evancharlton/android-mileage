package com.evancharlton.mileage.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.evancharlton.mileage.R;

public class ChartsView extends Activity {
	private Button m_fuelPriceBtn;
	private Button m_fuelAmountBtn;
	private Button m_distanceBtn;
	private Button m_fuelEconomyBtn;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.charts);

		initUI();
	}

	private void initUI() {
		m_fuelPriceBtn = (Button) findViewById(R.id.fuel_price_btn);
		m_fuelAmountBtn = (Button) findViewById(R.id.fuel_amount_btn);
		m_distanceBtn = (Button) findViewById(R.id.delta_distance_btn);
		m_fuelEconomyBtn = (Button) findViewById(R.id.fuel_economy_btn);

		m_fuelPriceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				buildFuelPriceChart();
			}
		});

		m_fuelAmountBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				buildFuelAmountChart();
			}
		});

		m_distanceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				buildDistanceChart();
			}
		});

		m_fuelEconomyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				buildFuelEconomyChart();
			}
		});
	}

	private void buildFuelPriceChart() {

	}

	private void buildFuelAmountChart() {

	}

	private void buildDistanceChart() {

	}

	private void buildFuelEconomyChart() {

	}
}
