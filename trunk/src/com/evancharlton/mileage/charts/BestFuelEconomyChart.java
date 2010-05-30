package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;

public class BestFuelEconomyChart extends FuelEconomyChart {
	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double best_fuel_economy = -100000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			if (num > 0) {
				double economy = cursor.getDouble(0);
				if (Calculator.isBetterEconomy(vehicle, economy, best_fuel_economy)) {
					best_fuel_economy = economy;
				}
				points.add(new ChartPoint(num, best_fuel_economy));
			}
			generator.update(num++);
			cursor.moveToNext();
		}
	}

	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_min_economy);
	}
}
