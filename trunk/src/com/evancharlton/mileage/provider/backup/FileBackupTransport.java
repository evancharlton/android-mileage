
package com.evancharlton.mileage.provider.backup;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.SettingsActivity;
import com.evancharlton.mileage.provider.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileBackupTransport extends BackupTransport {
    private final Handler mErrorHandler = new Handler();

    @Override
    public int getName() {
        return R.string.transport_file_name;
    }

    @Override
    public int getDescription(boolean enabled) {
        return enabled ? R.string.transport_file_description_on
                : R.string.transport_file_description_off;
    }

    @Override
    public void performIncrementalBackup(Context context, Uri changedUri) {
        // for the file backup, these are identical
        performCompleteBackup(context);
    }

    @Override
    public void performCompleteBackup(final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SettingsActivity.NAME,
                Context.MODE_PRIVATE);

        final String srcFile = preferences.getString(Settings.DATABASE_PATH, null);
        final String destFolder = Environment.getExternalStorageDirectory() + "/mileage/";
        final String destName = preferences.getString(TransportSettings.FILENAME, "mileage");
        final String destFile = destFolder + destName + ".db";

        new Thread() {
            @Override
            public void run() {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    File src = new File(srcFile);
                    File dest = new File(destFile);

                    FileChannel source = null;
                    FileChannel destination = null;

                    try {
                        if (!dest.exists()) {
                            if (!dest.getParentFile().exists()) {
                                dest.getParentFile().mkdirs();
                            }
                            dest.createNewFile();
                        }
                        source = new FileInputStream(src).getChannel();
                        destination = new FileOutputStream(dest).getChannel();
                        destination.transferFrom(source, 0, source.size());
                    } catch (FileNotFoundException fnfe) {
                        error(fnfe);
                    } catch (IOException e) {
                        error(e);
                    } finally {
                        if (source != null) {
                            try {
                                source.close();
                            } catch (IOException e) {
                                // silently fail
                            }
                        }
                        if (destination != null) {
                            try {
                                destination.close();
                            } catch (IOException e) {
                                // silently fail
                            }
                        }
                    }
                }
            }

            private void error(final Exception e) {
                mErrorHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.start();
    }

    @Override
    public int getSettings() {
        return R.layout.transport_file_settings;
    }

    @Override
    public boolean isEnabled(SharedPreferences preferences) {
        return preferences.getBoolean(TransportSettings.ENABLED, true);
    }

    public static final class TransportSettings {
        public static final String ENABLED = "transport_file_enabled";
        public static final String FILENAME = "transport_file_filename";
    }
}
