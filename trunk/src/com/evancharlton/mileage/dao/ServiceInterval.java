package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.net.Uri;

public class ServiceInterval extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String START_DATE = "start_timestamp";
	public static final String START_ODOMETER = "start_odometer";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String TEMPLATE_ID = "service_interval_template_id";

	public ServiceInterval(ContentValues values) {
		super(values);
	}

	@Override
	protected Uri getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void validate(ContentValues values) {
		// TODO Auto-generated method stub

	}
}
