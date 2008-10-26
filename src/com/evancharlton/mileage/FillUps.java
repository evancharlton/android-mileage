package com.evancharlton.mileage;

import android.net.Uri;
import android.provider.BaseColumns;

public class FillUps implements BaseColumns {
	private FillUps() {
	}

	public static final String AUTHORITY = "com.evancharlton.provider.Mileage";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fillups");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.fillup";
	public static final String DEFAULT_SORT_ORDER = "date DESC";
	public static final String COST = "cost";
	public static final String AMOUNT = "amount";
	public static final String MILEAGE = "mileage";
	public static final String DATE = "date";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
}
