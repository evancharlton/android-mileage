package com.evancharlton.mileage.dao;

import java.util.Date;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

	public static final ServiceInterval loadById(final Context context, final long id) {
		Uri uri = ContentUris.withAppendedId(ServiceIntervalsTable.BASE_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, ServiceIntervalsTable.PROJECTION, null, null, null);
		ServiceInterval interval = null;
		if (cursor.getCount() > 0) {
			interval = new ServiceInterval(cursor);
		}
		cursor.close();
		if (interval == null) {
			throw new IllegalArgumentException("Unable to load service interval #" + id);
		}
		return interval;
	}

	public void raiseNotification(Context context) {
		// TODO(3.2) - Support per-interval notification settings

		// TODO(6/18) - Finish this. I'm too tired tonight.
		// Intent i = new Intent(context, ServiceIntervalsListActivity.class);
		//
		// Vehicle v = new Vehicle(m_vehicleId);
		// String description =
		// String.format(context.getString(R.string.service_interval_due),
		// v.getTitle());
		//
		// Notification notification = new Notification(R.drawable.gasbuttonx,
		// getDescription(), System.currentTimeMillis());
		// i.putExtra(ServiceInterval._ID, m_id);
		// PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		// i, 0);
		//
		// notification.flags = Notification.FLAG_SHOW_LIGHTS |
		// Notification.FLAG_AUTO_CANCEL;
		// notification.ledARGB = 0xFFFCAF15;
		// notification.ledOffMS = 500;
		// notification.ledOnMS = 500;
		// notification.vibrate = new long[] {
		// 250,
		// 250,
		// 250,
		// 250
		// };
		// notification.defaults = Notification.DEFAULT_ALL;
		// notification.setLatestEventInfo(context, getDescription(),
		// description, contentIntent);
		// NotificationManager notificationMgr = (NotificationManager)
		// context.getSystemService(Activity.NOTIFICATION_SERVICE);
		// if (notificationMgr != null) {
		// notificationMgr.notify((int) m_id, notification);
		// }
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
