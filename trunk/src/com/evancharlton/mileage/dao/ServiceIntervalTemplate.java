package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.net.Uri;

public class ServiceIntervalTemplate extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String VEHICLE_TYPE = "vehicle_type";

	public ServiceIntervalTemplate(ContentValues values) {
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
