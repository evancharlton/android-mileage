
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class MinimumCostChart extends CostChart {

    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_min_cost);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double minimum_cost = 10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double cost = cursor.getDouble(1);
            if (cost < minimum_cost) {
                minimum_cost = cost;
            }
            addPoint(cursor.getLong(0), minimum_cost);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
