package com.evancharlton.mileage.views;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.Statistics.Statistic;

public class StatisticView {
	private TextView mLabel;
	private TextView mValue;

	public StatisticView(LinearLayout contents) {
		mLabel = (TextView) contents.findViewById(R.id.label);
		mValue = (TextView) contents.findViewById(R.id.value);
	}

	public void update(Statistic statistic) {
		mLabel.setText(statistic.getLabel());
		mValue.setText(String.valueOf(statistic.getValue()));
	}
}
