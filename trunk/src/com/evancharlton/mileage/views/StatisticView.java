package com.evancharlton.mileage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.Statistics.Statistic;

public class StatisticView extends LinearLayout {
	private TextView mLabel;
	private TextView mValue;

	public StatisticView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// super.setOrientation(HORIZONTAL);
		// super.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT));

		// LayoutInflater inflater = LayoutInflater.from(context);
		// inflater.inflate(R.layout.statistic, this);

		mLabel = (TextView) findViewById(R.id.label);
		mValue = (TextView) findViewById(R.id.value);
	}

	public void update(Statistic statistic) {
		mLabel.setText(statistic.getLabel());
		mValue.setText(String.valueOf(statistic.getValue()));
	}
}
