
package com.evancharlton.mileage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class FormattedCurrencyView extends TextView {
    private static final NumberFormat FORMAT = DecimalFormat.getCurrencyInstance();

    public FormattedCurrencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCurrencySymbol(String symbol) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol(symbol);
        ((DecimalFormat) FORMAT).setDecimalFormatSymbols(dfs);
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
