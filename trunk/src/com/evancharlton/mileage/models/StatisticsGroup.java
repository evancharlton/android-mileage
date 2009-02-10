package com.evancharlton.mileage.models;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatisticsGroup {
	private List<Statistic> m_statistics = new ArrayList<Statistic>();
	private String m_heading = "";

	public StatisticsGroup(String heading) {
		m_heading = heading;
	}

	public void add(Statistic statistic) {
		m_statistics.add(statistic);
	}

	public Statistic get(String label) {
		for (Statistic statistic : m_statistics) {
			if (statistic.getLabel().equals(label)) {
				return statistic;
			}
		}
		return null;
	}

	public LinearLayout render(Context context) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView heading = new TextView(context);
		heading.setText(m_heading);
		heading.setGravity(Gravity.CENTER);
		layout.addView(heading);

		for (Statistic statistic : m_statistics) {
			layout.addView(statistic.render(context));
		}

		return layout;
	}
}
