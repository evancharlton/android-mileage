
package com.evancharlton.mileage.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import java.io.File;
import java.util.List;

public final class Util {
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static File getExternalFolder() {
        return new File(Environment.getExternalStorageDirectory() + "/" + "mileage");
    }
}
