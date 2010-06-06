package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;

// TODO: Merge with LastMonthCostChart
public class LastYearCostChart extends CostChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_last_year_cost);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		final long lastYear = System.currentTimeMillis() - Calculator.YEAR_MS;
		double totalCost = 0D;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			if (cursor.getLong(1) > lastYear) {
				totalCost += cursor.getDouble(0);
				points.add(new ChartPoint(num, totalCost));
			}
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
