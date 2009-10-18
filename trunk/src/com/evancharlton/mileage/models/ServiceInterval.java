package com.evancharlton.mileage.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.evancharlton.mileage.FillUpsProvider;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.alarms.IntervalReceiver;
import com.evancharlton.mileage.views.intervals.ServiceIntervalsView;

public class ServiceInterval extends Model {
	public static final String VEHICLE_ID = "vehicle_id";
	public static final String CREATE_DATE = "creation_date";
	public static final String DURATION = "interval_duration";
	public static final String CREATE_ODOMETER = "creation_odometer";
	public static final String DISTANCE = "interval_distance";
	public static final String DESCRIPTION = "description";
	public static final String REPEATING = "is_repeating";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/intervals");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.interval";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.interval";
	public static final String DEFAULT_SORT_ORDER = VEHICLE_ID + " ASC";

	public static final List<String> PROJECTION = new ArrayList<String>();

	static {
		PROJECTION.add(_ID);
		PROJECTION.add(VEHICLE_ID);
		PROJECTION.add(CREATE_DATE);
		PROJECTION.add(DURATION);
		PROJECTION.add(CREATE_ODOMETER);
		PROJECTION.add(DISTANCE);
		PROJECTION.add(DESCRIPTION);
		// PROJECTION.add(REPEATING);
	}

	protected Calendar m_createDate = Calendar.getInstance();
	protected long m_duration = 0L;
	protected double m_createOdometer = 0D;
	protected double m_distance = 0D;
	protected String m_description = "";
	protected long m_vehicleId = 0L;
	protected boolean m_isRepeating = false;

	public ServiceInterval() {
		super(FillUpsProvider.MAINTENANCE_TABLE_NAME);
	}

	public ServiceInterval(ContentValues initialValues) {
		this();
	}

	public ServiceInterval(long id) {
		this();
		openDatabase();
		Cursor c = m_db.query(FillUpsProvider.MAINTENANCE_TABLE_NAME, ServiceInterval.getProjection(), ServiceInterval._ID + " = ?", new String[] {
			String.valueOf(id)
		}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToFirst();
			load(c);
			closeDatabase(c);
		} else {
			throw new IllegalArgumentException("Invalid service interval ID (" + id + ") given!");
		}
	}

	public ServiceInterval(Cursor c) {
		this();
		load(c);
	}

	protected void load(Cursor c) {
		int index = c.getColumnIndex(_ID);
		if (index >= 0) {
			setId(c.getLong(index));
		}

		index = c.getColumnIndex(CREATE_DATE);
		if (index >= 0) {
			setCreateDate(c.getLong(index));
		}

		index = c.getColumnIndex(CREATE_ODOMETER);
		if (index >= 0) {
			setCreateOdometer(c.getDouble(index));
		}

		index = c.getColumnIndex(DISTANCE);
		if (index >= 0) {
			setDistance(c.getDouble(index));
		}

		index = c.getColumnIndex(DESCRIPTION);
		if (index >= 0) {
			setDescription(c.getString(index));
		}

		index = c.getColumnIndex(DURATION);
		if (index >= 0) {
			setDuration(c.getLong(index));
		}

		index = c.getColumnIndex(VEHICLE_ID);
		if (index >= 0) {
			setVehicleId(c.getLong(index));
		}

		index = c.getColumnIndex(REPEATING);
		if (index >= 0) {
			setRepeating(c.getInt(index) == 1);
		}
	}

	public static List<ServiceInterval> getAll() {
		return new ServiceInterval().loadAll();
	}

	public static String[] getProjection() {
		return PROJECTION.toArray(new String[PROJECTION.size()]);
	}

	public List<ServiceInterval> loadAll() {
		List<ServiceInterval> intervals = new ArrayList<ServiceInterval>();
		openDatabase();
		String[] projection = ServiceInterval.getProjection();
		Cursor c = m_db.query(FillUpsProvider.MAINTENANCE_TABLE_NAME, projection, null, null, null, null, DEFAULT_SORT_ORDER);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			intervals.add(new ServiceInterval(c));
			c.moveToNext();
		}

		closeDatabase(c);
		return intervals;
	}

	@Override
	public long save() {
		openDatabase();
		ContentValues values = new ContentValues();
		values.put(ServiceInterval.CREATE_DATE, m_createDate.getTimeInMillis());
		values.put(ServiceInterval.CREATE_ODOMETER, m_createOdometer);
		values.put(ServiceInterval.DESCRIPTION, m_description);
		values.put(ServiceInterval.DISTANCE, m_distance);
		values.put(ServiceInterval.DURATION, m_duration);
		values.put(ServiceInterval.VEHICLE_ID, m_vehicleId);
		if (m_id == -1) {
			// save a new record
			m_id = m_db.insert(FillUpsProvider.MAINTENANCE_TABLE_NAME, null, values);
		} else {
			// update an existing record
			m_db.update(FillUpsProvider.MAINTENANCE_TABLE_NAME, values, ServiceInterval._ID + " = ?", new String[] {
				String.valueOf(m_id)
			});
		}
		closeDatabase(null);

		return m_id;
	}

	public void scheduleAlarm(Context context) {
		if (m_id < 0) {
			throw new IllegalStateException("The ServiceInterval must first be saved (or loaded)!");
		}

		Intent i = new Intent(context, IntervalReceiver.class);
		i.putExtra(ServiceInterval._ID, m_id);
		PendingIntent p = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		long when = m_createDate.getTimeInMillis();
		when += m_duration;

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (m_isRepeating) {
			// TODO: this case isn't actually supported yet because it raises
			// all kinds of interesting cases and complexities. For example: if
			// a repeating alarm is set and then the ServiceInterval is deleted,
			// what happens to the alarm? Also, does the date get updated every
			// time a repeating alarm is triggered? If not, it could be awkward
			// to the user. If so, how do we handle when the user gets an oil
			// change (for example) early? Choices, choices...
			am.setRepeating(AlarmManager.RTC_WAKEUP, when, m_duration, p);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, when, p);
		}
	}

	public void cancelAlarm(Context context) {
		if (m_id < 0) {
			throw new IllegalStateException("The ServiceInterval must first be saved (or loaded)!");
		}

		Intent i = new Intent(context, IntervalReceiver.class);
		i.putExtra(ServiceInterval._ID, m_id);
		PendingIntent p = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(p);
	}

	@Override
	public int validate() {
		if (m_createDate.getTimeInMillis() == 0) {
			return R.string.service_interval_error_create_date;
		}
		if (m_createOdometer < 0) {
			return R.string.service_interval_error_create_odometer;
		}
		if (m_duration < 0) {
			return R.string.service_interval_error_duration;
		}
		if (m_distance < 0) {
			return R.string.service_interval_error_distance;
		}
		if (m_vehicleId < 0) {
			return R.string.service_interval_error_vehicle_id;
		}
		if (m_description == null || m_description.length() == 0) {
			return R.string.service_interval_error_description;
		}
		return 0;
	}

	public void raiseNotification(Context context) {
		Intent i = new Intent(context, ServiceIntervalsView.class);

		Vehicle v = new Vehicle(m_vehicleId);
		String description = String.format(context.getString(R.string.service_interval_due), v.getTitle());

		Notification notification = new Notification(R.drawable.gasbuttonx, getDescription(), System.currentTimeMillis());
		i.putExtra(ServiceInterval._ID, m_id);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);

		notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		notification.ledARGB = 0xFFFCAF15;
		notification.ledOffMS = 500;
		notification.ledOnMS = 500;
		notification.vibrate = new long[] {
				250,
				250,
				250,
				250
		};
		notification.defaults = Notification.DEFAULT_ALL;
		notification.setLatestEventInfo(context, getDescription(), description, contentIntent);
		NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
		if (notificationMgr != null) {
			notificationMgr.notify((int) m_id, notification);
		}
	}

	/**
	 * @return the createDate
	 */
	public Calendar getCreateDate() {
		return m_createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Calendar createDate) {
		m_createDate = createDate;
	}

	public void setCreateDate(long milliseconds) {
		m_createDate.setTimeInMillis(milliseconds);
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return m_duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		m_duration = duration;
	}

	/**
	 * @return the createOdometer
	 */
	public double getCreateOdometer() {
		return m_createOdometer;
	}

	/**
	 * @param createOdometer the createOdometer to set
	 */
	public void setCreateOdometer(double createOdometer) {
		m_createOdometer = createOdometer;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return m_distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		m_distance = distance;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		m_description = description;
	}

	/**
	 * @return the vehicleId
	 */
	public long getVehicleId() {
		return m_vehicleId;
	}

	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(long vehicleId) {
		m_vehicleId = vehicleId;
	}

	/**
	 * @return the isRepeating
	 */
	public boolean isRepeating() {
		return m_isRepeating;
	}

	/**
	 * @param isRepeating the isRepeating to set
	 */
	public void setRepeating(boolean isRepeating) {
		m_isRepeating = isRepeating;
	}

	public void setRepeating(int isRepeating) {
		setRepeating(isRepeating == 1);
	}
}