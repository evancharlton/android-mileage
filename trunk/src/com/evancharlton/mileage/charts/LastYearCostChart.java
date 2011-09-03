
package com.evancharlton.mileage.charts;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.math.Calculator;

public class LastYearCostChart extends IntervalCostChart {
    @Override
    protected final String getAxisTitle() {
        return getString(R.string.stat_last_year_cost);
    }

    @Override
    protected final long getInterval() {
        return Calculator.YEAR_MS;
    }
}
