package com.evancharlton.mileage.views;

import java.util.Calendar;

import android.os.Bundle;

import com.evancharlton.mileage.models.FillUp;

public class FuelPriceChart extends ChartDisplay {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Price of Fuel");
	}

	@Override
	protected void buildChart() {
		m_chart.freeze();
		m_chart.setYAxisLabel("Price");

		float[] data = new float[m_fillups.size() * 2];

		final int size = m_fillups.size();
		int j = 0;
		FillUp f;
		float total = 0F;
		float price;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		String min_label = "";
		String max_label = "";
		for (int i = 0; i < size; i++) {
			f = m_fillups.get(i);
			data[j++] = (float) i;
			price = (float) f.getPrice();
			data[j++] = price;
			total += price;

			if (price < min) {
				min = price;
				Calendar d = f.getDate();
				min_label = m_prefs.format(d.getTime());
			}
			if (price > max) {
				max = price;
				Calendar d = f.getDate();
				max_label = m_prefs.format(d.getTime());
			}
		}

		m_chart.setXAxisLabels(min_label, max_label);
		m_chart.setYAxisLabels(m_prefs.getCurrency() + m_format.format(min), m_prefs.getCurrency() + m_format.format(max));
		float avg = (total / m_fillups.size());
		m_chart.setAverageLabel(m_prefs.getCurrency() + m_format.format(avg));
		m_chart.setBetterOnBottom(true);

		m_chart.setDataPoints(data);
		m_chart.thaw();
	}
}
