package com.evancharlton.mileage.views;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;

import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;

public class FuelEconomyChart extends ChartDisplay {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Fuel Economy");
	}

	@Override
	protected void buildChart() {
		m_chart.freeze();
		CalculationEngine calc = m_prefs.getCalculator();

		m_chart.setYAxisLabel(calc.getEconomyUnits());
		ArrayList<Float> data = new ArrayList<Float>();

		final int size = m_fillups.size();
		FillUp f;
		double total_fuel = 0F;
		double total_distance = m_fillups.get(m_fillups.size() - 1).getOdometer() - m_fillups.get(0).getOdometer();
		float economy;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		String min_label = "";
		String max_label = "";
		for (int i = 1; i < size; i++) {
			f = m_fillups.get(i);
			economy = (float) f.calcEconomy();
			total_fuel += (float) f.getAmount();
			if (economy == -1F) {
				continue;
			}
			data.add((float) i);
			data.add(economy);

			if (calc.better(min, economy)) {
				min = economy;
				Calendar d = f.getDate();
				min_label = m_prefs.format(d.getTime());
			}
			if (calc.better(economy, max)) {
				max = economy;
				Calendar d = f.getDate();
				max_label = m_prefs.format(d.getTime());
			}
		}

		m_chart.setBetterOnBottom(calc.isInverted());

		m_chart.setXAxisLabels(min_label, max_label);
		m_chart.setYAxisLabels(m_format.format(min) + calc.getEconomyUnits(), m_format.format(max) + calc.getEconomyUnits());
		float avg = (float) calc.calculateEconomy(total_distance, total_fuel);
		m_chart.setAverageLabel(m_format.format(avg) + calc.getEconomyUnits());

		m_chart.setDataPoints(data.toArray(new Float[data.size()]));
		m_chart.thaw();
	}
}
