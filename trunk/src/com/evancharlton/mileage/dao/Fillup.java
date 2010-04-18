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
	public static final String TOTAL_COST = "total_cost";
	public static final String UNIT_PRICE = "price";
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
	private double mUnitPrice = 0D;
	private double mTotalCost = 0D;
	private List<FillupField> mFields = null;
	private boolean mIsPartial = false;
	private Fillup mNext = null;
	private Fillup mPrevious = null;
	private boolean mIsRestart = false;
	private double mEconomy = 0D;

	public Fillup(ContentValues contentValues) {
		super(contentValues);
	}

	public Fillup(Cursor cursor) {
		this(new ContentValues());
		load(cursor);
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
	public void load(Cursor cursor) {
		super.load(cursor);
		mVehicleId = getLong(cursor, VEHICLE_ID);
		mOdometer = getLong(cursor, ODOMETER);
		mTimestamp = getLong(cursor, DATE);
		mVolume = getDouble(cursor, VOLUME);
		mUnitPrice = getDouble(cursor, UNIT_PRICE);
		mIsPartial = getBoolean(cursor, PARTIAL);
		mIsRestart = getBoolean(cursor, RESTART);
		// should the economy be stored in the cache table? I say no because the
		// added performance benefit of being able to load this oft-used field
		// from the same table will outweigh the ugliness of putting calculated
		// fields in here. Things to watch out for:
		// - when changing a vehicle's unit preferences, invalidate the field
		// - when editing a field before it, invalidate the field (partials)
		mEconomy = getDouble(cursor, ECONOMY);
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
		values.put(ODOMETER, mOdometer);

		if (mTimestamp <= 0) {
			mTimestamp = System.currentTimeMillis();
		}
		values.put(DATE, mTimestamp);

		if (getVolume() <= 0) {
			throw new InvalidFieldException(R.string.error_no_volume_specified);
		}
		values.put(VOLUME, getVolume());

		if (getUnitPrice() <= 0) {
			throw new InvalidFieldException(R.string.error_no_price_specified);
		}
		values.put(UNIT_PRICE, getUnitPrice());

		if (getTotalCost() <= 0) {
			throw new InvalidFieldException(R.string.error_no_total_cost_specified);
		}
		values.put(TOTAL_COST, getTotalCost());

		values.put(PARTIAL, mIsPartial);
		values.put(RESTART, mIsRestart);
		values.put(ECONOMY, mEconomy);
	}

	public Fillup loadPrevious(Context context) {
		if (!mIsRestart) {
			Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUPS_URI);
			String[] projection = FillupsTable.getFullProjectionArray();
			Cursor c = context.getContentResolver().query(uri, projection, Fillup.VEHICLE_ID + " = ? AND " + ODOMETER + " < ?", new String[] {
					String.valueOf(getVehicleId()),
					String.valueOf(getOdometer())
			}, Fillup.ODOMETER + " desc");
			if (c.getCount() >= 1) {
				c.moveToFirst();
				return new Fillup(c);
			}
		}
		return null;
	}

	public double getEconomy() {
		return mEconomy;
	}

	public List<FillupField> getFields() {
		return mFields;
	}

	public List<FillupField> getFields(Context context) {
		if (mFields == null) {
			// load the fields from the database
		}
		return mFields;
	}

	public Fillup getNext() {
		return mNext;
	}

	public double getOdometer() {
		return mOdometer;
	}

	public Fillup getPrevious() {
		return mPrevious;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public double getTotalCost() {
		if (mTotalCost == 0 && (mVolume > 0 && mUnitPrice > 0)) {
			mTotalCost = mVolume * mUnitPrice;
		}
		return mTotalCost;
	}

	public double getUnitPrice() {
		if (mUnitPrice == 0 && (mVolume > 0 && mTotalCost > 0)) {
			mUnitPrice = mTotalCost / mVolume;
		}
		return mUnitPrice;
	}

	public long getVehicleId() {
		return mVehicleId;
	}

	public double getVolume() {
		if (mVolume == 0 && (mTotalCost > 0 && mUnitPrice > 0)) {
			mVolume = mTotalCost / mUnitPrice;
		}
		return mVolume;
	}

	public boolean hasNext() {
		return mNext != null;
	}

	public boolean hasPrevious() {
		return mPrevious != null;
	}

	public boolean isPartial() {
		return mIsPartial;
	}

	public boolean isRestart() {
		return mIsRestart;
	}

	public void setEconomy(double economy) {
		mEconomy = economy;
	}

	public void setFields(List<FillupField> fields) {
		mFields = fields;
	}

	public void setNext(Fillup next) {
		mNext = next;
	}

	public void setOdometer(double odometer) {
		mOdometer = odometer;
	}

	public void setPartial(boolean partial) {
		mIsPartial = partial;
	}

	public void setPrevious(Fillup previous) {
		mPrevious = previous;
	}

	public void setRestart(boolean restart) {
		mIsRestart = restart;
	}

	public void setTimestamp(long timestamp) {
		mTimestamp = timestamp;
	}

	public void setTotalCost(double totalCost) {
		mTotalCost = totalCost;
	}

	public void setUnitPrice(double unitPrice) {
		mUnitPrice = unitPrice;
	}

	public void setVehicleId(long id) {
		mVehicleId = id;
	}

	public void setVolume(double volume) {
		mVolume = volume;
	}
}
