
package com.evancharlton.mileage;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.backup.BackupTransport;

public class TransportSettingsActivity extends PreferenceActivity {
    public static final String PACKAGE_NAME = "package_name";

    private static final int MENU_BACKUP = 1;

    private BackupTransport mTransport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packageName = getIntent().getStringExtra(PACKAGE_NAME);
        mTransport = FillUpsProvider.getBackupTransport(packageName);
        addPreferencesFromResource(mTransport.getSettings());
        setTitle(mTransport.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_BACKUP, Menu.NONE, R.string.perform_backup);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_BACKUP:
                mTransport.performCompleteBackup(this);
                return true;
        }
        return false;
    }
}
