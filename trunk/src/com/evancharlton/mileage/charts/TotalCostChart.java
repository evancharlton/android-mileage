package com.evancharlton.mileage.charts;

import android.database.Cursor;

import com.artfulbits.aiCharts.Base.ChartPoint;
import com.artfulbits.aiCharts.Base.ChartPointCollection;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

public class TotalCostChart extends CostChart {
	@Override
	protected String getAxisTitle() {
		return getString(R.string.stat_total_cost);
	}

	@Override
	protected void processCursor(LineChartGenerator generator, ChartPointCollection points, Cursor cursor, Vehicle vehicle) {
		int num = 0;
		double total_cost = 0D;
		while (cursor.isAfterLast() == false) {
			if (generator.isCancelled()) {
				break;
			}
			total_cost += cursor.getDouble(0);
			points.add(new ChartPoint(num, total_cost));
			generator.update(num++);
			cursor.moveToNext();
		}
	}
}
