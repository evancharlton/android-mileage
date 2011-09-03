
package com.evancharlton.mileage.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evancharlton.mileage.dao.ServiceInterval;

public class IntervalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getExtras().getLong(ServiceInterval._ID, -1L);

        ServiceInterval interval = ServiceInterval.loadById(context, id);
        interval.raiseNotification(context);
    }
}
