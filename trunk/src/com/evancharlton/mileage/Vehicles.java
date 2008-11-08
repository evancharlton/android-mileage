package com.evancharlton.mileage;

import android.net.Uri;
import android.provider.BaseColumns;

public class Vehicles implements BaseColumns {
	private Vehicles() {
	}

	public static final String AUTHORITY = "com.evancharlton.provider.Mileage";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/vehicles");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicle";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.vehicle";
	public static final String TITLE = "title";
	public static final String MAKE = "make";
	public static final String MODEL = "model";
	public static final String YEAR = "year";
	public static final String DEFAULT = "def";
	public static final String DEFAULT_SORT_ORDER = DEFAULT + " DESC, " + TITLE + " ASC";
}
