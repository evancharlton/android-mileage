package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

@DataObject(path = ServiceIntervalsTable.URI)
public class ServiceInterval extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String START_DATE = "start_timestamp";
	public static final String START_ODOMETER = "start_odometer";
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String TEMPLATE_ID = "service_interval_template_id";
	public static final String DURATION = "duration";
	public static final String DISTANCE = "distance";

	private String mTitle = null;
	private String mDescription = null;
	private long mStartDate = 0L;
	private double mStartOdometer = 0L;
	private long mVehicleId = 0L;
	private long mTemplateId = 0L;
	private long mDuration = 0L;
	private long mDistance = 0L;

	public ServiceInterval(ContentValues values) {
		super(values);
	}

	public ServiceInterval(Cursor cursor) {
		super(cursor);
	}

	@Override
	protected void validate(ContentValues values) {
		if (mTitle == null || mTitle.length() == 0) {
			throw new InvalidFieldException(R.string.error_invalid_interval_title);
		}
		values.put(TITLE, mTitle);

		if (mDescription == null) {
			throw new InvalidFieldException(R.string.error_invalid_interval_description);
		}
		values.put(DESCRIPTION, mDescription);

		if (mStartDate == 0) {
			mStartDate = System.currentTimeMillis();
		}
		values.put(START_DATE, mStartDate);

		if (mStartOdometer == 0) {
			throw new InvalidFieldException(R.string.error_invalid_interval_odometer);
		}
		values.put(START_ODOMETER, mStartOdometer);

		if (mVehicleId == 0) {
			throw new InvalidFieldException(R.string.error_invalid_interval_vehicle);
		}
		values.put(VEHICLE_ID, mVehicleId);

		if (mDuration == 0) {
			throw new InvalidFieldException(R.string.error_invalid_interval_duration);
		}
		values.put(DURATION, mDuration);

		if (mDistance == 0) {
			throw new InvalidFieldException(R.string.error_invalid_interval_distance);
		}
		values.put(DISTANCE, mDistance);

		values.put(TEMPLATE_ID, mTemplateId);
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

	public long getStartDate() {
		return mStartDate;
	}

	public void setStartDate(long startDate) {
		mStartDate = startDate;
	}

	public double getStartOdometer() {
		return mStartOdometer;
	}

	public void setStartOdometer(double startOdometer) {
		mStartOdometer = startOdometer;
	}

	public long getVehicleId() {
		return mVehicleId;
	}

	public void setVehicleId(long vehicleId) {
		mVehicleId = vehicleId;
	}

	public long getTemplateId() {
		return mTemplateId;
	}

	public void setTemplateId(long templateId) {
		mTemplateId = templateId;
	}

	public long getDuration() {
		return mDuration;
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public long getDistance() {
		return mDistance;
	}

	public void setDistance(long distance) {
		mDistance = distance;
	}
}
