
package com.evancharlton.mileage.services;

import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.util.Util;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AutomaticBackupService extends IntentService {
    private static final String TAG = "Mileage.ABS";

    private static final long MIN_DELTA = 1000; // one second

    public static void run(Context context) {
        context.startService(new Intent(context, AutomaticBackupService.class));
    }

    private static long mLastBackupTime = 0;

    public AutomaticBackupService() {
        super(AutomaticBackupService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long delta = System.currentTimeMillis() - mLastBackupTime;
        if (delta < MIN_DELTA) {
            Log.d(TAG, "Not backing up; " + delta);
            return;
        }
        mLastBackupTime = System.currentTimeMillis();

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File src = getDatabasePath(FillUpsProvider.DATABASE_NAME);
            File dest = new File(Util.getExternalFolder(), "mileage-backup.db");

            FileChannel source = null;
            FileChannel destination = null;

            try {
                if (!dest.exists()) {
                    if (!dest.getParentFile().exists()) {
                        dest.getParentFile().mkdirs();
                    }
                    if (!dest.createNewFile()) {
                        Wtf.get(this).wtf("Unable to create file!");
                    }
                }
                source = new FileInputStream(src).getChannel();
                destination = new FileOutputStream(dest).getChannel();
                destination.transferFrom(source, 0, source.size());
                Log.d(TAG, "Finished backup");
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

    private void error(Exception e) {
        Log.e(TAG, "Backup failed!", e);
        Wtf.get(this).wtf(e);
    }

    private static class Wtf {
        public static Wtf get(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                return new RealWtf(context);
            }
            return new Wtf(context);
        }

        protected final Context mContext;

        public Wtf(Context context) {
            mContext = context;
        }

        public void wtf(Exception e) {
            Log.e(TAG, "WTF!", e);
        }

        public void wtf(String text) {
            Log.e(TAG, "WTF: " + text);
        }

        @TargetApi(8)
        private static class RealWtf extends Wtf {
            public RealWtf(Context context) {
                super(context);
            }

            @Override
            public void wtf(Exception e) {
                Log.wtf(TAG, e);
                DropBoxManager dropbox =
                        (DropBoxManager) mContext.getSystemService(DROPBOX_SERVICE);

                StringBuilder message = new StringBuilder();
                message.append(e.getMessage());
                message.append("|");

                StackTraceElement[] traces = e.getStackTrace();
                final int N = Math.min(10, traces.length);
                for (int i = 0; i < N; i++) {
                    StackTraceElement trace = traces[i];
                    message.append(trace.getClassName()).append(".").append(trace.getMethodName())
                            .append(":").append(trace.getLineNumber()).append("\t");
                }
                dropbox.addText("Mileage.AutomaticBackupService", message.toString());
            }

            @Override
            public void wtf(String text) {
                Log.wtf(TAG, text);
            }
        }
    }
}
