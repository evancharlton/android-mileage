
package com.evancharlton.mileage.views;

import com.evancharlton.mileage.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class FormattedDateView extends TextView {
    private static final int DATE = 1;

    private static final String FMT_DATE = "date";

    private static final int MEDIUM = 2;

    private static final String FMT_MEDIUM = "medium";

    private static final int LONG = 3;

    private static final String FMT_LONG = "long";

    private static final int TIME = 4;

    private static final String FMT_TIME = "time";

    private int mFormat = 1;

    public FormattedDateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FormattedDateView);
        final String format = arr.getString(R.styleable.FormattedDateView_dateFormat);
        if (format != null) {
            if (FMT_MEDIUM.equals(format)) {
                mFormat = MEDIUM;
            } else if (FMT_LONG.equals(format)) {
                mFormat = LONG;
            } else if (FMT_TIME.equals(format)) {
                mFormat = TIME;
            } else if (FMT_DATE.equals(format)) {
                mFormat = DATE;
            } else {
                throw new IllegalArgumentException("Unknown date format!");
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        DateFormat formatter;
        switch (mFormat) {
            case MEDIUM:
                formatter = android.text.format.DateFormat.getMediumDateFormat(getContext());
                break;
            case LONG:
                formatter = android.text.format.DateFormat.getLongDateFormat(getContext());
                break;
            case TIME:
                formatter = android.text.format.DateFormat.getTimeFormat(getContext());
                break;
            case DATE:
            default:
                formatter = android.text.format.DateFormat.getDateFormat(getContext());
                break;
        }
        try {
            long timestamp = Long.parseLong(text.toString());
            super.setText(formatter.format(new Date(timestamp)), type);
        } catch (NumberFormatException e) {
            try {
                super.setText(formatter.format(new Date(text.toString())), type);
            } catch (Exception ex) {
                super.setText(text, type);
            }
        }
    }

    public void setText(long timestamp) {
        setText(String.valueOf(timestamp));
    }
}
