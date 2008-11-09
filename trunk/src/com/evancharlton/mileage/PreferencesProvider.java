package com.evancharlton.mileage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;

public class PreferencesProvider {
	private static PreferencesProvider s_instance = null;
	private Activity m_activity;
	private SharedPreferences m_settings;
	private CalculationEngine m_calcEngine;

	private static final String PREFS_NAME = "MileageSettings";
	private static final int BOOLEAN = 0;
	private static final int FLOAT = 1;
	private static final int INT = 2;
	private static final int LONG = 3;
	private static final int STRING = 4;

	private static final int US_ENGINE = 0;

	private PreferencesProvider(Activity activity) {
		m_activity = activity;
		m_settings = m_activity.getSharedPreferences(PREFS_NAME, 0);
	}

	public static PreferencesProvider getInstance(Activity activity) {
		if (s_instance == null) {
			s_instance = new PreferencesProvider(activity);
		}
		return s_instance;
	}

	public boolean write(String key, boolean value) {
		return write(key, value, BOOLEAN);
	}

	public boolean write(String key, float value) {
		return write(key, value, FLOAT);
	}

	public boolean write(String key, int value) {
		return write(key, value, INT);
	}

	public boolean write(String key, long value) {
		return write(key, value, LONG);
	}

	public boolean write(String key, String value) {
		return write(key, value, STRING);
	}

	private boolean write(String key, Object value, int type) {
		SharedPreferences.Editor editor = m_settings.edit();
		switch (type) {
			case BOOLEAN:
				editor.putBoolean(key, (Boolean) value);
				break;
			case FLOAT:
				editor.putFloat(key, (Float) value);
				break;
			case INT:
				editor.putInt(key, (Integer) value);
				break;
			case LONG:
				editor.putLong(key, (Long) value);
				break;
			case STRING:
				editor.putString(key, (String) value);
				break;
			default:
				throw new IllegalArgumentException("Unrecognized type: " + value.toString());
		}
		return editor.commit();
	}

	public boolean getBoolean(String key, boolean defValue) {
		return m_settings.getBoolean(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return m_settings.getFloat(key, defValue);
	}

	public int getInt(String key, int defValue) {
		return m_settings.getInt(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return m_settings.getLong(key, defValue);
	}

	public String getString(String key, String defValue) {
		return m_settings.getString(key, defValue);
	}

	public String getString(int array, String key) {
		try {
			int index = getInt(key, 0);
			String data = m_activity.getResources().getStringArray(array)[index];
			return data;
		} catch (IndexOutOfBoundsException e) {
			return "";
		}
	}

	public CalculationEngine getCalculator() {
		int calc = getInt(SettingsView.CALCULATIONS, 0);
		if (calc == US_ENGINE) {
			m_calcEngine = new USCalculationEngine();
		} else {
			m_calcEngine = new MetricCalculationEngine();
		}
		return m_calcEngine;
	}

	public String format(double number) {
		// TODO: support this in the future
		DecimalFormat formatter = new DecimalFormat("#,##0.00");
		return formatter.format(number);
	}

	public String format(Date d) {
		SimpleDateFormat format = new SimpleDateFormat();
		format.applyPattern(getString(R.array.date_patterns, SettingsView.DATE));
		return format.format(d);
	}
}
