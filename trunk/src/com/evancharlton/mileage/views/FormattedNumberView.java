
package com.evancharlton.mileage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormattedNumberView extends TextView {
    private static final NumberFormat FORMAT = DecimalFormat.getNumberInstance();

    public FormattedNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            double value = Double.parseDouble(text.toString());
            super.setText(FORMAT.format(value), type);
        } catch (Exception e) {
            super.setText(text, type);
        }
    }
}
