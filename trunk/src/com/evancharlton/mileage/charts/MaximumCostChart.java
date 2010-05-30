package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class MaximumCostChart extends CostChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_max_cost);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double maximum_cost = -10000;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			double cost = cursor.getDouble(0);
			if (cost > maximum_cost) {
				maximum_cost = cost;
			}
			points.add(new ChartPoint(num, maximum_cost));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
