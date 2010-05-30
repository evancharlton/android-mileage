package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class MinimumCostChart extends CostChart {

	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_min_cost);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double minimum_cost = 10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double cost = cursor.getDouble(0);
			if (cost < minimum_cost) {
				minimum_cost = cost;
			}
			points.add(new ChartPoint(num, minimum_cost));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
