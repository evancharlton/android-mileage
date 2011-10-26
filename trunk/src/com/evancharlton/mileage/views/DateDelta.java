
package com.evancharlton.mileage.views;

import com.evancharlton.mileage.R;

import android.content.Context;
import android.util.AttributeSet;

public class DateDelta extends DeltaView {
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private static final long[] VALUES = new long[] {
            ONE_DAY,
            ONE_DAY * 7,
            ONE_DAY * 30,
            ONE_DAY * 365
    };
    private static final String[] TEXT = new String[VALUES.length];

    public DateDelta(Context context, AttributeSet attrs) {
        super(context, attrs);

        TEXT[0] = context.getString(R.string.delta_days);
        TEXT[1] = context.getString(R.string.delta_weeks);
        TEXT[2] = context.getString(R.string.delta_months);
        TEXT[3] = context.getString(R.string.delta_years);
    }

    @Override
    protected int getPosition(long delta) {
        int numDays = (int) (delta / ONE_DAY);
        int position = 0;
        switch (numDays) {
            case 1:
                position = 0;
                break;
            case 7:
                position = 1;
                break;
            case 30:
                position = 2;
                break;
            case 365:
                position = 3;
                break;
        }
        return position;
    }

    @Override
    protected String[] getTexts() {
        return TEXT;
    }

    @Override
    protected long[] getValues() {
        return VALUES;
    }
}
