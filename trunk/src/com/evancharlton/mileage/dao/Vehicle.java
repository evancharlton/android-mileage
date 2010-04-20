package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

public class Vehicle extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String YEAR = "year";
	public static final String MAKE = "make";
	public static final String MODEL = "model";
	public static final String VEHICLE_TYPE = "vehicle_type_id";
	public static final String DEFAULT_TIME = "default_time";
	public static final String PREF_DISTANCE_UNITS = "odometer_units";
	public static final String PREF_VOLUME_UNITS = "volume_units";
	public static final String PREF_ECONOMY_UNITS = "economy_units";
	public static final String PREF_CURRENCY = "currency_units";

	private String mTitle = null;
	private String mDescription = null;
	private String mYear = null;
	private String mMake = null;
	private String mModel = null;
	private long mVehicleType = 0L;
	private long mDefaultTime = 0L;
	private int mPrefDistanceUnits = Calculator.MI;
	private int mPrefVolumeUnits = Calculator.GALLONS;
	private int mPrefEconomyUnits = Calculator.MI_PER_GALLON;

	public Vehicle(ContentValues values) {
		super(values);
		// TODO: Finish loading
		mPrefDistanceUnits = getInt(values, PREF_DISTANCE_UNITS, Calculator.MI);
		mPrefVolumeUnits = getInt(values, PREF_VOLUME_UNITS, Calculator.GALLONS);
		mPrefEconomyUnits = getInt(values, PREF_ECONOMY_UNITS, Calculator.MI_PER_GALLON);
	}

	public Vehicle(Cursor cursor) {
		super(cursor);
		load(cursor);
	}

	@Override
	public void load(Cursor cursor) {
		super.load(cursor);
		mTitle = getString(cursor, TITLE);
		mDescription = getString(cursor, DESCRIPTION);
		mYear = getString(cursor, YEAR);
		mMake = getString(cursor, MAKE);
		mModel = getString(cursor, MODEL);
		mVehicleType = getLong(cursor, VEHICLE_TYPE);
		mDefaultTime = getLong(cursor, DEFAULT_TIME);

		// build the preferences
		mPrefDistanceUnits = getInt(cursor, PREF_DISTANCE_UNITS);
		mPrefVolumeUnits = getInt(cursor, PREF_VOLUME_UNITS);
		mPrefEconomyUnits = getInt(cursor, PREF_ECONOMY_UNITS);
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, VehiclesTable.VEHICLE_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, VehiclesTable.VEHICLES_URI);
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTitle == null || mTitle.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_title);
		}
		values.put(TITLE, mTitle);

		if (mDescription == null) {
			mDescription = "";
		}
		values.put(DESCRIPTION, mDescription);

		if (mYear == null || mYear.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_year);
		}
		values.put(YEAR, mYear);

		if (mMake == null || mMake.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_make);
		}
		values.put(MAKE, mMake);

		if (mModel == null || mModel.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_model);
		}
		values.put(MODEL, mModel);

		if (mVehicleType == 0) {
			throw new InvalidFieldException(R.string.error_invalid_vehicle_type);
		}
		values.put(VEHICLE_TYPE, mVehicleType);

		values.put(DEFAULT_TIME, mDefaultTime);
		// values.put(PREF_CURRENCY, mCurrency);
		values.put(PREF_DISTANCE_UNITS, mPrefDistanceUnits);
		values.put(PREF_ECONOMY_UNITS, mPrefEconomyUnits);
		values.put(PREF_VOLUME_UNITS, mPrefVolumeUnits);
	}

	public Fillup loadLatestFillup(Context context) {
		Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUPS_URI);
		String[] projection = FillupsTable.getFullProjectionArray();
		Cursor c = context.getContentResolver().query(uri, projection, Fillup.VEHICLE_ID + " = ?", new String[] {
			String.valueOf(getId())
		}, Fillup.ODOMETER + " desc");
		if (c.getCount() >= 1) {
			c.moveToFirst();
			return new Fillup(c);
		}
		return null;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public void setYear(String year) {
		mYear = year;
	}

	public void setMake(String make) {
		mMake = make;
	}

	public void setModel(String model) {
		mModel = model;
	}

	public void setVehicleType(long vehicleType) {
		mVehicleType = vehicleType;
	}

	public void setDefaultTime(long defaultTime) {
		mDefaultTime = defaultTime;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getYear() {
		return mYear;
	}

	public String getMake() {
		return mMake;
	}

	public String getModel() {
		return mModel;
	}

	public long getVehicleType() {
		return mVehicleType;
	}

	public long getDefaultTime() {
		return mDefaultTime;
	}

	public int getDistanceUnits() {
		return mPrefDistanceUnits;
	}

	public int getVolumeUnits() {
		return mPrefVolumeUnits;
	}

	public int getEconomyUnits() {
		return mPrefEconomyUnits;
	}
}
