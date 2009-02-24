package com.evancharlton.mileage.alarms;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.models.ServiceInterval;
import com.evancharlton.mileage.models.Vehicle;
import com.evancharlton.mileage.views.intervals.ServiceIntervalsView;

public class IntervalReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, ServiceIntervalsView.class);
		long id = intent.getExtras().getLong(ServiceInterval._ID, -1L);

		ServiceInterval interval = new ServiceInterval(id);
		Vehicle v = new Vehicle(interval.getVehicleId());
		String description = String.format("Due for %s", v.getTitle());

		Notification notification = new Notification(R.drawable.gasbuttonx, interval.getDescription(), System.currentTimeMillis());
		i.putExtra(ServiceInterval._ID, id);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);

		notification.setLatestEventInfo(context, interval.getDescription(), description, contentIntent);
		NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
		if (notificationMgr != null) {
			notificationMgr.notify((int) id, notification);
		}
	}
}
