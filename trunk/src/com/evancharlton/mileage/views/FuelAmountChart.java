package com.evancharlton.mileage.views;

import java.util.Calendar;

import android.os.Bundle;

import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;

public class FuelAmountChart extends ChartDisplay {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Amount of Fuel");
	}

	@Override
	protected void buildChart() {
		m_chart.freeze();
		CalculationEngine calc = m_prefs.getCalculator();
		m_chart.setYAxisLabel(calc.getVolumeUnits());

		float[] data = new float[m_fillups.size() * 2];

		final int size = m_fillups.size();
		int j = 0;
		FillUp f;
		float total = 0F;
		float amount;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		String min_label = "";
		String max_label = "";
		for (int i = 0; i < size; i++) {
			f = m_fillups.get(i);
			data[j++] = (float) i;
			amount = (float) f.getAmount();
			data[j++] = amount;
			total += amount;

			if (amount < min) {
				min = amount;
				Calendar d = f.getDate();
				min_label = m_prefs.format(d.getTime());
			}
			if (amount > max) {
				max = amount;
				Calendar d = f.getDate();
				max_label = m_prefs.format(d.getTime());
			}
		}

		m_chart.setBetterOnBottom(true);

		m_chart.setXAxisLabels(min_label, max_label);
		m_chart.setYAxisLabels(m_format.format(min) + calc.getVolumeUnitsAbbr(), m_format.format(max) + calc.getVolumeUnitsAbbr());
		float avg = (total / m_fillups.size());
		m_chart.setAverageLabel(m_format.format(avg) + calc.getVolumeUnitsAbbr());

		m_chart.setDataPoints(data);
		m_chart.thaw();
	}
}
