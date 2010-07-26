package com.evancharlton.mileage.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.database.sqlite.SQLiteDatabase;

import com.evancharlton.mileage.io.DbImportActivity;
import com.evancharlton.mileage.provider.DatabaseUpgrader;
import com.evancharlton.mileage.provider.Settings;

public class DbImportTask extends AttachableAsyncTask<DbImportActivity, Void, String, Boolean> {
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
			makeBackup();
			upgradeDatabase();
			cleanUp();
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
		SQLiteDatabase db = SQLiteDatabase.openDatabase(TEMP_FILE, null, SQLiteDatabase.OPEN_READWRITE);
		// TODO(3.0) - determine the actual database version
		DatabaseUpgrader.upgradeDatabase(4, db);
		db.close();
	}

	private void cleanUp() throws IOException {
		FileChannel input = new FileInputStream(TEMP_FILE).getChannel();
		FileChannel output = new FileOutputStream(Settings.DATABASE_PATH).getChannel();
		input.transferTo(0, input.size(), output);
		input.close();
		output.close();

		File tempDatabase = new File(TEMP_FILE);
		tempDatabase.delete();
	}
}
