
package com.evancharlton.mileage.views;

import com.evancharlton.mileage.R;

import android.content.Context;
import android.util.AttributeSet;

public class DistanceDelta extends DeltaView {

    private static final long ONE_METER = 100; // centimeters

    private static final long[] VALUES = new long[] {
            1609 * ONE_METER, // mile
            1000 * ONE_METER
    };

    private static final String[] TEXTS = new String[VALUES.length];

    public DistanceDelta(Context context, AttributeSet attrs) {
        super(context, attrs);
        TEXTS[0] = context.getString(R.string.units_miles);
        TEXTS[1] = context.getString(R.string.units_kilometers);
    }

    @Override
    protected int getPosition(long delta) {
        int unit = (int) (delta / ONE_METER);
        switch (unit) {
            case 1000:
                return 1;
        }
        return 0;
    }

    @Override
    protected String[] getTexts() {
        return TEXTS;
    }

    @Override
    protected long[] getValues() {
        return VALUES;
    }

}
