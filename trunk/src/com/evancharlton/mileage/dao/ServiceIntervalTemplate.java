package com.evancharlton.mileage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

@DataObject(path = ServiceIntervalTemplatesTable.URI)
public class ServiceIntervalTemplate extends Dao {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String VEHICLE_TYPE = "vehicle_type";

	@Column(type = Column.STRING, name = TITLE)
	protected String mTitle;
	@Column(type = Column.STRING, name = DESCRIPTION)
	protected String mDescription;
	@Column(type = Column.LONG, name = DISTANCE)
	protected long mDistance;
	@Column(type = Column.LONG, name = DURATION)
	protected long mDuration;
	@Column(type = Column.LONG, name = VEHICLE_TYPE)
	protected long mVehicleTypeId;

	public ServiceIntervalTemplate(ContentValues values) {
		super(values);
	}

	public ServiceIntervalTemplate(Cursor cursor) {
		super(cursor);
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

	public long getDistance() {
		return mDistance;
	}

	public void setDistance(long distance) {
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
