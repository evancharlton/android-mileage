
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.ServiceIntervalsListActivity;
import com.evancharlton.mileage.alarms.IntervalReceiver;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import java.util.Date;

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
        Cursor cursor = context.getContentResolver().query(uri, ServiceIntervalsTable.PROJECTION,
                null, null, null);
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

    public void scheduleAlarm(final Context context, long when) {
        // schedule the alarm
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date trigger = new Date(when);

        mgr.set(AlarmManager.RTC, trigger.getTime(), getPendingIntent(context));
        String date = Calculator.getDateString(context, Calculator.DATE_DATE, trigger);
        Toast.makeText(context, context.getString(R.string.service_interval_set, date),
                Toast.LENGTH_LONG).show();
    }

    public void deleteAlarm(final Context context) {
        if (isExistingObject()) {
            // cancel the alarm
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.cancel(getPendingIntent(context));
            Toast.makeText(context, context.getString(R.string.service_interval_canceled),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent action = new Intent(context, IntervalReceiver.class);
        action.putExtra(ServiceInterval._ID, getId());
        return PendingIntent.getBroadcast(context, (int) getId(), action,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void raiseNotification(Context context) {
        // TODO(3.2) - Support per-interval notification settings

        SharedPreferences prefs = context.getSharedPreferences(Settings.NAME, Context.MODE_PRIVATE);

        if (prefs.getBoolean(Settings.NOTIFICATIONS_ENABLED, true)) {
            Intent i = new Intent(context, ServiceIntervalsListActivity.class);

            Vehicle v = Vehicle.loadById(context, getVehicleId());
            String description = context.getString(R.string.service_interval_due, v.getTitle());

            Notification notification = new Notification(R.drawable.icon, getDescription(),
                    System.currentTimeMillis());
            i.putExtra(ServiceInterval._ID, getId());
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);

            notification.flags = Notification.FLAG_AUTO_CANCEL;

            if (prefs.getBoolean(Settings.NOTIFICATIONS_LED, true)) {
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                notification.ledARGB = 0xFFFCAF15;
                notification.ledOffMS = 500;
                notification.ledOnMS = 500;
            }

            if (prefs.getBoolean(Settings.NOTIFICATIONS_VIBRATE, true)) {
                notification.vibrate = new long[] {
                        250,
                        250,
                        250,
                        250
                };
            }

            String uri = prefs.getString(Settings.NOTIFICATIONS_RINGTONE, "");
            if (uri != null && uri.length() > 0) {
                notification.sound = Uri.parse(uri);
            }

            notification.defaults = Notification.DEFAULT_ALL;
            notification.setLatestEventInfo(context, getDescription(), description, contentIntent);
            NotificationManager notificationMgr = (NotificationManager) context
                    .getSystemService(Activity.NOTIFICATION_SERVICE);
            if (notificationMgr != null) {
                notificationMgr.notify((int) getId(), notification);
            }
        }
    }

    @Override
    public boolean delete(final Context context) {
        deleteAlarm(context);
        return super.delete(context);
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
