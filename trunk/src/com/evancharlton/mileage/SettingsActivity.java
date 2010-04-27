package com.evancharlton.mileage;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.providers.backup.BackupTransport;

public class SettingsActivity extends PreferenceActivity {
	public static final String NAME = "com.evancharlton.mileage_preferences";

	private PreferenceCategory mBackupCategory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);

		mBackupCategory = (PreferenceCategory) findPreference(Settings.BACKUPS);
		final int count = mBackupCategory.getPreferenceCount();
		ArrayList<BackupTransport> transports = FillUpsProvider.getBackupTransports();
		for (int i = 0; i < count; i++) {
			BackupTransport transport = transports.get(i);
			PreferenceScreen screen = (PreferenceScreen) mBackupCategory.getPreference(i);
			Intent intent = screen.getIntent();
			intent.putExtra(TransportSettingsActivity.PACKAGE_NAME, transport.getClass().getName());
			screen.setIntent(intent);
			screen.setTitle(transport.getName());
		}
	}

	private static final class Settings {
		public static final String STORE_LOCATION = "location_data";
		public static final String BACKUPS = "backups";
		public static final String BACKUPS_ENABLED = "backups_enabled";
		public static final String BACKUP_TRANSPORTS = "backup_transports";
	}
}
