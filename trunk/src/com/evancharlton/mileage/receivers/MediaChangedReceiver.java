package com.evancharlton.mileage.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evancharlton.mileage.services.AutomaticBackupService;

public class MediaChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            if (!intent.getBooleanExtra("read-only", false)) {
                AutomaticBackupService.run(context);
            }
        }
    }
}
