package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
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
	private int mPrefDistanceUnits = Preferences.UNITS_MI;
	private int mPrefVolumeUnits = Preferences.UNITS_GALLONS;
	private int mPrefEconomyUnits = Preferences.ECONOMY_MI_PER_GALLON;

	public Vehicle(ContentValues values) {
		super(values);
		// TODO
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

	// TODO: split this out into a separate class? there's no reason for this to
	// be an inner class.
	// TODO: This should probably be renamed too.
	public static final class Preferences {
		// distance
		public static final int UNITS_KM = 1;
		public static final int UNITS_MI = 2;

		// volume
		public static final int UNITS_GALLONS = 3;
		public static final int UNITS_LITRES = 4;
		public static final int UNITS_IMPERIAL_GALLONS = 5;

		// economy
		public static final int ECONOMY_MI_PER_GALLON = 6;
		public static final int ECONOMY_KM_PER_GALLON = 7;
		public static final int ECONOMY_MI_PER_IMP_GALLON = 8;
		public static final int ECONOMY_KM_PER_IMP_GALLON = 9;
		public static final int ECONOMY_MI_PER_LITRE = 10;
		public static final int ECONOMY_KM_PER_LITRE = 11;
		public static final int ECONOMY_GALLONS_PER_100KM = 12;
		public static final int ECONOMY_LITRES_PER_100KM = 13;
		public static final int ECONOMY_IMP_GAL_PER_100KM = 14;

		public static double averageEconomy(Vehicle vehicle, FillupSeries series) {
			// ALL CALCULATIONS ARE DONE IN MPG AND CONVERTED LATER
			double miles = convert(series.getTotalDistance(), vehicle.getDistanceUnits(), UNITS_MI);
			double gallons = convert(series.getTotalVolume(), vehicle.getVolumeUnits(), UNITS_GALLONS);

			switch (vehicle.getEconomyUnits()) {
				case ECONOMY_KM_PER_GALLON:
					return convert(miles, UNITS_KM) / gallons;
				case ECONOMY_MI_PER_IMP_GALLON:
					return miles / convert(gallons, UNITS_IMPERIAL_GALLONS);
				case ECONOMY_KM_PER_IMP_GALLON:
					return convert(miles, UNITS_KM) / convert(gallons, UNITS_IMPERIAL_GALLONS);
				case ECONOMY_MI_PER_LITRE:
					return miles / convert(gallons, UNITS_LITRES);
				case ECONOMY_KM_PER_LITRE:
					return convert(miles, UNITS_KM) / convert(gallons, UNITS_LITRES);
				case ECONOMY_GALLONS_PER_100KM:
					return gallons / (100 * convert(miles, UNITS_KM));
				case ECONOMY_LITRES_PER_100KM:
					return convert(gallons, UNITS_LITRES) / (100 * convert(miles, UNITS_KM));
				case ECONOMY_IMP_GAL_PER_100KM:
					return convert(gallons, UNITS_IMPERIAL_GALLONS) / (100 * convert(miles, UNITS_KM));
				case ECONOMY_MI_PER_GALLON:
				default:
					return miles / gallons;
			}
		}

		// yes, this method makes it possible to convert from miles to litres.
		// if you do this, I'll hunt you down and beat you with a rubber hose.
		private static double convert(double value, int from, int to) {
			// going from whatever to miles or gallons (depending on context)
			switch (from) {
				case UNITS_MI:
					break;
				case UNITS_KM:
					value *= 0.621371192;
				case UNITS_GALLONS:
					break;
				case UNITS_LITRES:
					value *= 0.264172052;
				case UNITS_IMPERIAL_GALLONS:
					value *= 1.20095042;
			}
			// at this point, "value" is either miles or gallons
			return convert(value, to);
		}

		// convert from (miles|gallons) to the other unit
		private static double convert(double value, int to) {
			// value is now converted to miles or gallons
			switch (to) {
				case UNITS_MI:
					return value;
				case UNITS_KM:
					value /= 0.621371192;
				case UNITS_GALLONS:
					return value;
				case UNITS_LITRES:
					value /= 0.264172052;
				case UNITS_IMPERIAL_GALLONS:
					value /= 1.20095042;

			}
			return value;
		}
	}
}
