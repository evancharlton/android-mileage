package com.evancharlton.mileage;

import java.util.ArrayList;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.provider.backup.BackupTransport;

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

		Preference about = findPreference("about");
		String version;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
		} catch (NameNotFoundException e) {
			version = "<unknown version>";
		}
		about.setSummary(getString(R.string.settings_about_summary, version));
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				return true;
			}
		});
	}
}
