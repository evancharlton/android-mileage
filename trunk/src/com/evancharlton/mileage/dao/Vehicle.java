package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

@DataObject(path = VehiclesTable.VEHICLES_URI)
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

	@Column(type = Column.STRING, name = TITLE)
	protected String mTitle = null;
	@Column(type = Column.STRING, name = DESCRIPTION)
	protected String mDescription = null;
	@Column(type = Column.STRING, name = YEAR)
	protected String mYear = null;
	@Column(type = Column.STRING, name = MAKE)
	protected String mMake = null;
	@Column(type = Column.STRING, name = MODEL)
	protected String mModel = null;
	@Column(type = Column.LONG, name = TITLE)
	protected long mVehicleType = 0L;
	@Column(type = Column.LONG, name = TITLE)
	protected long mDefaultTime = 0L;
	@Column(type = Column.INTEGER, name = PREF_DISTANCE_UNITS, value = Calculator.MI)
	protected int mPrefDistanceUnits;
	@Column(type = Column.INTEGER, name = PREF_VOLUME_UNITS, value = Calculator.GALLONS)
	protected int mPrefVolumeUnits;
	@Column(type = Column.INTEGER, name = PREF_ECONOMY_UNITS, value = Calculator.MI_PER_GALLON)
	protected int mPrefEconomyUnits;

	public Vehicle(ContentValues values) {
		super(values);
	}

	public Vehicle(Cursor cursor) {
		super(cursor);
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
		Uri uri = FillupsTable.BASE_URI;
		String[] projection = FillupsTable.PROJECTION;
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

	public void setVolumeUnits(int volumeUnits) {
		mPrefVolumeUnits = volumeUnits;
	}

	public void setDistanceUnits(int distanceUnits) {
		mPrefDistanceUnits = distanceUnits;
	}

	public void setEconomyUnits(int economyUnits) {
		mPrefEconomyUnits = economyUnits;
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
