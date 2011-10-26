
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class MaximumCostChart extends CostChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_max_cost);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double maximum_cost = -10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double cost = cursor.getDouble(1);
            if (cost > maximum_cost) {
                maximum_cost = cost;
            }
            addPoint(cursor.getLong(0), maximum_cost);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
