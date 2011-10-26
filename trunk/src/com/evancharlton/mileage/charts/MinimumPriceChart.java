
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;

import android.database.Cursor;

public class MinimumPriceChart extends PriceChart {
    @Override
    protected String getAxisTitle() {
        return getString(R.string.stat_min_price);
    }

    @Override
    protected void processCursor(LineChartGenerator generator, Cursor cursor, Vehicle vehicle) {
        int num = 0;
        double minimum_price = 10000;
        while (cursor.isAfterLast() == false) {
            if (generator.isCancelled()) {
                break;
            }
            double price = cursor.getDouble(1);
            if (price < minimum_price) {
                minimum_price = price;
            }
            addPoint(cursor.getLong(0), minimum_price);
            generator.update(num++);
            cursor.moveToNext();
        }
    }
}
