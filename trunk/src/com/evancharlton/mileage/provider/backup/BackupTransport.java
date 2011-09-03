
package com.evancharlton.mileage.provider.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public abstract class BackupTransport {
    abstract public int getName();

    abstract public int getDescription(boolean enabled);

    abstract public int getSettings();

    abstract public void performIncrementalBackup(Context context, final Uri changedUri);

    /**
     * Perform a complete backup. It's fine to run this on the UI thread--the
     * transport should do its work on a separate thread.
     * 
     * @param context
     */
    abstract public void performCompleteBackup(Context context);

    abstract public boolean isEnabled(SharedPreferences preferences);
}
