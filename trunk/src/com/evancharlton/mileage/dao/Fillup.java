package com.evancharlton.mileage.dao;

import java.util.List;

import android.database.Cursor;

import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;

public class Fillup extends Dao {
	public static final String PRICE = "cost"; // price per unit volume
	public static final String VOLUME = "amount";
	public static final String ODOMETER = "mileage"; // odometer, not economy
	public static final String DATE = "date"; // timestamp in milliseconds
	public static final String PARTIAL = "is_partial";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COMMENT = "comment";
	public static final String RESTART = "restart";
	public static final String ECONOMY = "economy";

	private long mId = 0L;
	private long mVehicleId = 0L;
	private long mOdometer = 0L;
	private long mTimestamp = 0L;
	private double mVolume = 0D;
	private double mPrice = 0D;
	private List<FillupField> mFields = null;

	protected Fillup(Cursor cursor) {
		mId = getLong(cursor, Fillup._ID);
		mVehicleId = getLong(cursor, Fillup.VEHICLE_ID);
		mOdometer = getLong(cursor, Fillup.ODOMETER);
		mTimestamp = getLong(cursor, Fillup.DATE);
		mVolume = getDouble(cursor, Fillup.VOLUME);
		mPrice = getDouble(cursor, Fillup.PRICE);
	}

	public List<FillupField> getFields() {
		if (mFields == null) {
			// load the fields from the database
			getContext().getContentResolver().query(createUri("fillups/fields/", mId), FillupsFieldsTable.getFullProjectionArray(), null, null, null);
		}
		return mFields;
	}
}
