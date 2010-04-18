package com.evancharlton.mileage.views;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.evancharlton.mileage.R;

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
		final String format = arr.getString(R.styleable.FormattedDateView_format);
		if (format != null) {
			if (FMT_MEDIUM.equals(format)) {
				mFormat = MEDIUM;
			} else if (FMT_LONG.equals(format)) {
				mFormat = LONG;
			} else if (FMT_TIME.equals(format)) {
				mFormat = TIME;
			} else {
				mFormat = DATE;
			}
		}
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		try {
			long timestamp = Long.parseLong(text.toString());
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
			super.setText(formatter.format(new Date(timestamp)));
		} catch (NumberFormatException e) {
			super.setText(text, type);
		}
	}
}
