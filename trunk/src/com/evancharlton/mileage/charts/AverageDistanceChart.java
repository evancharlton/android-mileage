
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class AverageDistanceChart extends DistanceChart {

    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_avg_distance);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double last_odometer = 0;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double odometer = cursor.getDouble(1);
            if (num > 0) {
                addPoint(cursor.getLong(0), odometer - last_odometer);
            }
            last_odometer = odometer;
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
