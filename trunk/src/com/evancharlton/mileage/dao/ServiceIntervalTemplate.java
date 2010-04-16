package com.evancharlton.mileage.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

public class ServiceIntervalTemplate extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String VEHICLE_TYPE = "vehicle_type";

	private String mTitle = null;
	private String mDescription = null;
	private double mDistance = 0L;
	private long mDuration = 0L;
	private long mVehicleTypeId = 0L;

	public ServiceIntervalTemplate(ContentValues values) {
		super(values);
	}

	@Override
	public void load(Cursor cursor) {
		super.load(cursor);
		mTitle = getString(cursor, TITLE);
		mDescription = getString(cursor, DESCRIPTION);
		mDistance = getDouble(cursor, DISTANCE);
		mDuration = getLong(cursor, DURATION);
		mVehicleTypeId = getLong(cursor, VEHICLE_TYPE);
	}

	@Override
	protected Uri getUri() {
		Uri base = FillUpsProvider.BASE_URI;
		if (isExistingObject()) {
			base = Uri.withAppendedPath(base, ServiceIntervalTemplatesTable.SERVICE_TEMPLATE_URI);
			base = ContentUris.withAppendedId(base, getId());
		} else {
			base = Uri.withAppendedPath(base, ServiceIntervalTemplatesTable.SERVICE_TEMPLATES_URI);
		}
		return base;
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTitle == null || mTitle.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_template_title);
		}
		values.put(TITLE, mTitle);

		if (mDescription == null) {
			throw new InvalidFieldException(R.string.error_invalid_template_description);
		}
		values.put(DESCRIPTION, mDescription);

		if (mDistance <= 0) {
			throw new InvalidFieldException(R.string.error_invalid_template_distance);
		}
		values.put(DISTANCE, mDistance);

		if (mDuration <= 0) {
			throw new InvalidFieldException(R.string.error_invalid_template_duration);
		}
		values.put(DURATION, mDuration);

		if (mVehicleTypeId <= 0) {
			throw new InvalidFieldException(R.string.error_invalid_template_vehicle_type);
		}
		values.put(VEHICLE_TYPE, mVehicleTypeId);
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public double getDistance() {
		return mDistance;
	}

	public void setDistance(double distance) {
		mDistance = distance;
	}

	public long getDuration() {
		return mDuration;
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public long getVehicleTypeId() {
		return mVehicleTypeId;
	}

	public void setVehicleTypeId(long vehicleTypeId) {
		mVehicleTypeId = vehicleTypeId;
	}
}
