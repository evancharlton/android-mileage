package com.evancharlton.mileage.views;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;

import com.evancharlton.mileage.calculators.CalculationEngine;
import com.evancharlton.mileage.models.FillUp;

public class DistanceChart extends ChartDisplay {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Distance Between Fill-Ups");
	}

	@Override
	protected void buildChart() {
		m_chart.freeze();
		CalculationEngine calc = m_prefs.getCalculator();
		m_chart.setYAxisLabel(calc.getDistanceUnits());

		final int size = m_fillups.size() - 1;

		ArrayList<Float> data = new ArrayList<Float>();
		FillUp f;
		float total = (float) (m_fillups.get(size).getOdometer() - m_fillups.get(0).getOdometer());
		float distance;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		String min_label = "";
		String max_label = "";
		for (int i = 1; i <= size; i++) {
			f = m_fillups.get(i);
			distance = (float) f.calcDistance();
			if (distance == -1) {
				continue;
			}
			data.add((float) i);
			data.add(distance);

			if (distance < min) {
				min = distance;
				Calendar d = f.getDate();
				min_label = m_prefs.format(d.getTime());
			}
			if (distance > max) {
				max = distance;
				Calendar d = f.getDate();
				max_label = m_prefs.format(d.getTime());
			}
		}

		m_chart.setXAxisLabels(min_label, max_label);
		m_chart.setYAxisLabels(m_format.format(min) + calc.getDistanceUnitsAbbr(), m_format.format(max) + calc.getDistanceUnitsAbbr());
		float avg = (total / size);
		m_chart.setAverageLabel(m_format.format(avg) + calc.getDistanceUnitsAbbr());

		m_chart.setDataPoints(data.toArray(new Float[data.size()]));
		m_chart.thaw();
	}
}
