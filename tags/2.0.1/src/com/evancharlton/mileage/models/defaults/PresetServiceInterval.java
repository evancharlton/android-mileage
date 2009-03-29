package com.evancharlton.mileage.models.defaults;

import com.evancharlton.mileage.models.ServiceInterval;

public class PresetServiceInterval extends ServiceInterval {

	public PresetServiceInterval() {
		super();
	}

	public PresetServiceInterval(double distance, long duration, String description) {
		super();
		m_distance = distance;
		m_duration = duration;
		m_description = description;
	}

	/**
	 * Make sure that this element can not be saved, ever.
	 * 
	 * @return 0
	 */
	@Override
	public long save() {
		return 0L;
	}

	public String toString() {
		return m_description;
	}
}
