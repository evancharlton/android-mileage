
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class NorthChart extends LatitudeChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_north);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double maximum_north = -10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double north = cursor.getDouble(1);
            if (north != 0) {
                if (north > maximum_north) {
                    maximum_north = north;
                }
                addPoint(cursor.getLong(0), maximum_north);
            }
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
