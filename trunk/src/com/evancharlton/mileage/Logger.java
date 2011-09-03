
package com.evancharlton.mileage;

import android.util.Log;

public final class Logger {
    public static void log(String tag, Object msg) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg.toString());
        }
    }
}
