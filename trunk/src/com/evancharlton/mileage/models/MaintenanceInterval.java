package com.evancharlton.mileage.models;

import android.content.ContentValues;
import android.net.Uri;

import com.evancharlton.mileage.FillUpsProvider;

public class MaintenanceInterval extends Model {
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/intervals");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.interval";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.interval";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String CREATE_DATE = "creation_date";
	public static final String DURATION = "interval_duration";
	public static final String CREATE_ODOMETER = "creation_odometer";
	public static final String DISTANCE = "interval_distance";
	public static final String DESCRIPTION = "description";

	public MaintenanceInterval() {
		super(FillUpsProvider.MAINTENANCE_TABLE_NAME);
	}

	public MaintenanceInterval(ContentValues initialValues) {
		this();
	}

	@Override
	public long save() {
		return 0;
	}

	@Override
	public int validate() {
		return 0;
	}

}
