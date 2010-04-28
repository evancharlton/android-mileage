package com.evancharlton.mileage.views;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.evancharlton.mileage.R;

public class FormattedNumberView extends TextView {
	private static final String TAG = "FormattedNumberView";

	private final DecimalFormat mFormatter;

	public FormattedNumberView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FormattedNumberView);
		final String format = arr.getString(R.styleable.FormattedNumberView_numberFormat);
		Log.d(TAG, format == null ? "<null>" : format);
		if (format != null) {
			mFormatter = new DecimalFormat(format);
		} else {
			mFormatter = null;
		}
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		try {
			double value = Double.parseDouble(text.toString());
			super.setText(mFormatter.format(value), type);
		} catch (Exception e) {
			super.setText(text, type);
		}
	}
}
