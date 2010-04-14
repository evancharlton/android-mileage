package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

public class Vehicle extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String YEAR = "year";
	public static final String MAKE = "make";
	public static final String MODEL = "model";
	public static final String VEHICLE_TYPE = "vehicle_type_id";
	public static final String DEFAULT_TIME = "default_time";

	private String mTitle = null;
	private String mDescription = null;
	private String mYear = null;
	private String mMake = null;
	private String mModel = null;
	private long mVehicleType = 0L;
	private long mDefaultTime = System.currentTimeMillis();

	public Vehicle(Cursor cursor) {
		super(cursor);
		// TODO
	}

	public Vehicle(ContentValues values) {
		super(values);
		// TODO
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
}
