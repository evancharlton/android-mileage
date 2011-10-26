
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class TotalCostChart extends CostChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_total_cost);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double total_cost = 0D;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            total_cost += cursor.getDouble(1);
            addPoint(cursor.getLong(0), total_cost);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
