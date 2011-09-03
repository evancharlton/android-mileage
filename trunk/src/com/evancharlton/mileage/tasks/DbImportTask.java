
package com.evancharlton.mileage.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.io.DbImportActivity;
import com.evancharlton.mileage.provider.DatabaseUpgrader;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DbImportTask extends AttachableAsyncTask<DbImportActivity, Void, String, Boolean> {
    private static final String TAG = "DbImportTask";

    private static final String TEMP_FILE = Settings.EXTERNAL_DIR + ".import.db";
    private final String mInput;

    public DbImportTask(String input) {
        mInput = input;
    }

    @Override
    protected void onPreExecute() {
        getParent().setWorking(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            publishProgress(getParent().getString(R.string.update_starting_import));
            makeBackup();
            publishProgress(getParent().getString(R.string.update_made_backup));
            publishProgress(getParent().getString(R.string.update_upgrading_database));
            upgradeDatabase();
            publishProgress(getParent().getString(R.string.update_upgraded_database));
            publishProgress(getParent().getString(R.string.update_cleaning_up));
            cleanUp();
            publishProgress(getParent().getString(R.string.update_finished_importing));
            return true;
        } catch (IOException e) {
            publishProgress(e.getMessage());
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(String... updates) {
        getParent().log(updates[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        getParent().setWorking(false);
    }

    private void makeBackup() throws IOException {
        FileChannel input = new FileInputStream(Settings.EXTERNAL_DIR + mInput).getChannel();
        FileChannel output = new FileOutputStream(TEMP_FILE).getChannel();
        input.transferTo(0, input.size(), output);
        input.close();
        output.close();
    }

    private void upgradeDatabase() {
        Log.d(TAG, "Upgrading " + TEMP_FILE);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(TEMP_FILE, null,
                SQLiteDatabase.OPEN_READWRITE);
        DatabaseUpgrader.upgradeDatabase(db);
        db.close();
    }

    private void cleanUp() throws IOException {
        SharedPreferences prefs = getParent().getSharedPreferences(Settings.NAME,
                Context.MODE_PRIVATE);
        String path = prefs.getString(Settings.DATABASE_PATH, null);
        FileChannel input = new FileInputStream(TEMP_FILE).getChannel();
        FileChannel output = new FileOutputStream(path).getChannel();
        long bytes = input.transferTo(0, input.size(), output);
        input.close();
        output.close();
        Log.d(TAG, "Wrote " + bytes + " bytes to " + path + " from " + TEMP_FILE);

        File tempDatabase = new File(TEMP_FILE);
        tempDatabase.delete();

        getParent().getContentResolver().getType(
                Uri.withAppendedPath(FillUpsProvider.BASE_URI, "reset"));
    }
}
