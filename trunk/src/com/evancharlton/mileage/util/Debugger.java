
package com.evancharlton.mileage.util;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public final class Debugger {
    public static final boolean DEBUG = false;

    private static final int DEBUG_NONE = 0;
    private static final int DEBUG_QUIET = 1;
    private static final int DEBUG_WARN = 2;
    private static final int DEBUG_YELL = 3;
    private static final int DEBUG_LEVEL = DEBUG_NONE;

    private static final String TAG = "Debugger";

    public static final boolean isOnUiThread() {
        return DEBUG && Looper.getMainLooper() == Looper.myLooper();
    }

    public static final void checkQueryOnUiThread(Context context) {
        ensureNotUiThread(context, "Query on UI thread!");
    }

    public static final void ensureNotUiThread(Context context, String msg) {
        if (isOnUiThread()) {
            yell(context, msg);
        }
    }

    public static final void ensureOnUiThread(Context context, String msg) {
        if (!isOnUiThread()) {
            yell(context, msg);
        }
    }

    private static final void yell(Context context, String msg) {
        switch (DEBUG_LEVEL) {
            case DEBUG_YELL:
                throw new IllegalStateException(msg);
            case DEBUG_WARN:
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            case DEBUG_QUIET:
                Log.d(TAG, msg + getStackTrace());
        }
    }

    private static final String getStackTrace() {
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        final int count = elements.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < count; i++) {
            sb.append("\n    ").append(elements[i].toString());
        }
        return sb.toString();
    }

    public static final void d(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg);
        }
    }
}
