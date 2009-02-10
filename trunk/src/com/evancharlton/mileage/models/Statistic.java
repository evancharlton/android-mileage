package com.evancharlton.mileage.models;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Statistic {
	private String m_label = "";
	private String m_statistic = "";
	private String m_units = "";
	private String m_prefix = "";

	public Statistic(String label, String prefix, double statistic) {
		this(label, prefix, statistic, "");
	}

	public Statistic(String label, String prefix, double statistic, String units) {
		this(label, "", units);
		m_prefix = prefix;
		DecimalFormat format = new DecimalFormat("0.00");
		m_statistic = format.format(statistic);
	}

	public Statistic(String label, double statistic, String units) {
		this(label, "", statistic, units);
	}

	public Statistic(String label, String statistic, String units) {
		m_label = label;
		m_statistic = statistic;
		m_units = units;
	}

	public LinearLayout render(Context context) {
		LayoutParams fillWrapParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(fillWrapParams);

		TextView label = new TextView(context);
		label.setText(m_label);
		label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		label.setTypeface(label.getTypeface(), Typeface.BOLD);

		TextView data = new TextView(context);
		data.setText((m_prefix.trim().length() > 0 ? " " + m_prefix.trim() : "") + m_statistic + (m_units.trim().length() > 0 ? " " + m_units.trim() : ""));
		data.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		data.setGravity(Gravity.RIGHT);
		data.setPadding(0, 0, 10, 0);

		layout.addView(label);
		layout.addView(data);

		return layout;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return m_label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		m_label = label;
	}

	/**
	 * @return the statistic
	 */
	public String getStatistic() {
		return m_statistic;
	}

	/**
	 * @param statistic the statistic to set
	 */
	public void setStatistic(String statistic) {
		m_statistic = statistic;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return m_units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		m_units = units;
	}
}
