
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public abstract class IntervalCostChart extends CostChart {
    protected abstract long getInterval();

    @Override
    protected final void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        final long limit = System.currentTimeMillis() - getInterval();
        double totalCost = 0D;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            if (cursor.getLong(0) > limit) {
                totalCost += cursor.getDouble(1);
                addPoint(cursor.getLong(0), totalCost);
            }
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
