package com.evancharlton.mileage.dao;

import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class Fillup extends Dao {
	public static final String PRICE = "price";
	public static final String VOLUME = "volume";
	public static final String ODOMETER = "odometer";
	public static final String DATE = "timestamp"; // ms since epoch
	public static final String PARTIAL = "is_partial";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COMMENT = "comment";
	public static final String RESTART = "restart";
	public static final String ECONOMY = "economy";

	private long mVehicleId = 0L;
	private double mOdometer = 0L;
	private long mTimestamp = 0L;
	private double mVolume = 0D;
	private double mPrice = 0D;
	private List<FillupField> mFields = null;
	private boolean mIsPartial = false;

	public Fillup(Cursor cursor) {
		super(cursor);
		mVehicleId = getLong(cursor, Fillup.VEHICLE_ID);
		mOdometer = getLong(cursor, Fillup.ODOMETER);
		mTimestamp = getLong(cursor, Fillup.DATE);
		mVolume = getDouble(cursor, Fillup.VOLUME);
		mPrice = getDouble(cursor, Fillup.PRICE);
	}

	public Fillup(ContentValues contentValues) {
		super(contentValues);
	}

	public List<FillupField> getFields(Context context) {
		if (mFields == null) {
			// load the fields from the database
		}
		return mFields;
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, FillupsTable.FILLUP_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, FillupsTable.FILLUPS_URI);
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mVehicleId <= 0) {
			throw new InvalidFieldException(R.string.error_no_vehicle_specified);
		}
		values.put(VEHICLE_ID, mVehicleId);

		if (mOdometer <= 0) {
			throw new InvalidFieldException(R.string.error_no_odometer_specified);
		}
		values.put(ODOMETER, mVehicleId);

		if (mTimestamp <= 0) {
			mTimestamp = System.currentTimeMillis();
		}
		values.put(DATE, mTimestamp);

		if (mVolume <= 0) {
			throw new InvalidFieldException(R.string.error_no_volume_specified);
		}
		values.put(VOLUME, mVolume);

		if (mPrice <= 0) {
			throw new InvalidFieldException(R.string.error_no_price_specified);
		}
		values.put(PRICE, mPrice);

		values.put(PARTIAL, mIsPartial);
	}

	public void setVolume(double volume) {
		mVolume = volume;
	}

	public void setOdometer(double odometer) {
		mOdometer = odometer;
	}

	public void setPrice(double price) {
		mPrice = price;
	}

	public void setPartial(boolean partial) {
		mIsPartial = partial;
	}
}
