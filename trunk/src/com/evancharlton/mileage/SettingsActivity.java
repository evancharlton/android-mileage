
package com.evancharlton.mileage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.provider.tables.FieldsTable;

public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceClickListener {
    public static final String NAME = "com.evancharlton.mileage_preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);

        Preference about = findPreference("about");
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_ACTIVITIES).versionName;
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

        findPreference("units").setOnPreferenceClickListener(this);
        // findPreference(Settings.META_FIELD).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("units".equals(preference.getKey())) {
            showDialog(R.string.settings_units);
            return true;
        } else if (Settings.META_FIELD.equals(preference.getKey())) {
            showDialog(R.string.settings_meta_field_title);
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case R.string.settings_units:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.units_title)
                        .setMessage(R.string.units_description)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeDialog(id);
                                    }
                                }).create();
            case R.string.settings_meta_field_title:
                final Cursor c = managedQuery(FieldsTable.URI, FieldsTable.PROJECTION, null, null,
                        null);
                final SharedPreferences prefs = getSharedPreferences(Settings.NAME,
                        Context.MODE_PRIVATE);
                return new AlertDialog.Builder(this)
                        .setSingleChoiceItems(c, -1, Field.TITLE,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long id = -1;
                                        if (c.moveToPosition(which)) {
                                            id = c.getLong(c.getColumnIndex(Field._ID));
                                        }
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putLong(Settings.META_FIELD, id);
                                        editor.commit();
                                    }
                                })
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeDialog(id);
                                    }
                                }).setTitle(R.string.dialog_title_meta_fields).create();
            default:
                return super.onCreateDialog(id);
        }
    }
}
