package com.evancharlton.mileage.dao;

import java.util.Date;

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

	@Validate(R.string.error_invalid_interval_title)
	@Column(type = Column.STRING, name = TITLE)
	protected String mTitle;

	@Validate(R.string.error_invalid_interval_description)
	@Column(type = Column.STRING, name = DESCRIPTION)
	protected String mDescription;

	@Validate
	@Column(type = Column.TIMESTAMP, name = START_DATE)
	protected Date mStartDate;

	@Validate(R.string.error_invalid_interval_odometer)
	@Column(type = Column.DOUBLE, name = START_ODOMETER)
	protected double mStartOdometer;

	@Validate(R.string.error_invalid_interval_vehicle)
	@Range.Positive
	@Column(type = Column.LONG, name = VEHICLE_ID)
	protected long mVehicleId;

	@Validate
	@Column(type = Column.LONG, name = TEMPLATE_ID)
	protected long mTemplateId;

	@Validate(R.string.error_invalid_interval_duration)
	@Column(type = Column.LONG, name = DURATION)
	protected long mDuration;

	@Validate(R.string.error_invalid_interval_distance)
	@Column(type = Column.LONG, name = DISTANCE)
	protected long mDistance;

	public ServiceInterval(ContentValues values) {
		super(values);
	}

	public ServiceInterval(Cursor cursor) {
		super(cursor);
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
		return mStartDate.getTime();
	}

	public void setStartDate(long startDate) {
		if (mStartDate == null) {
			mStartDate = new Date(System.currentTimeMillis());
		} else {
			mStartDate.setTime(startDate);
		}
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
