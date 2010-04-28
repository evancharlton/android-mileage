package com.evancharlton.mileage.provider.backup;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class BackupTransport {
	abstract public int getName();

	abstract public int getDescription(boolean enabled);

	abstract public int getSettings();

	abstract public void performIncrementalBackup(Context context);

	abstract public void performCompleteBackup(Context context);

	abstract public boolean isEnabled(SharedPreferences preferences);
}
