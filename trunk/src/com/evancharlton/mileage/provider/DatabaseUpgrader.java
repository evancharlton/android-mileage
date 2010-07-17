package com.evancharlton.mileage.provider;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.util.Log;

import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.ContentTable;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

public class DatabaseUpgrader {
	private static final String TAG = "DatabaseUpgrader";

	// TODO: Replace these with actual version numbers
	private static final int V2 = 2;
	private static final int V3 = 3;
	private static final int V4 = 4;
	private static final int V5 = 5;

	private static final StringBuilder BUILDER = new StringBuilder();

	private static SQLiteDatabase sDatabase;

	// Note: these columns are hard-coded for now. I should back-port the old
	// column name constants but I likely won't.
	public static void upgradeDatabase(final int oldVersion, final SQLiteDatabase database) {
		sDatabase = database;
		switch (oldVersion) {
			case V2:
				// add the comment field
				exec("ALTER TABLE fillups ADD COLUMN comment TEXT;");

				// add the default setting for vehicles
				exec("ALTER TABLE vehicles ADD COLUMN def INTEGER;");
			case V3:
				// add the partial flag
				exec("ALTER TABLE fillups ADD COLUMN is_partial INTEGER;");

				// add the restart flag
				exec("ALTER TABLE fillups ADD COLUMN restart INTEGER;");

				// add the distance units
				exec("ALTER TABLE vehicles ADD COLUMN distance INTEGER DEFAULT -1;");

				// add the volume units
				exec("ALTER TABLE vehicles ADD COLUMN volume INTEGER DEFAULT -1;");

				// create the service interval table
				BUILDER.append("CREATE TABLE maintenance_intervals (");
				BUILDER.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
				BUILDER.append("creation_date INTEGER,");
				BUILDER.append("creation_odometer DOUBLE,");
				BUILDER.append("description TEXT,");
				BUILDER.append("interval_distance DOUBLE,");
				BUILDER.append("interval_duration INTEGER,");
				BUILDER.append("vehicle_id INTEGER,");
				BUILDER.append("is_repeating INTEGER");
				BUILDER.append(");");
				flush();

				// create the version table
				BUILDER.append("CREATE TABLE version (");
				BUILDER.append("version INTEGER");
				BUILDER.append(");");
				flush();
			case V4:
				// add the economy field
				exec("ALTER TABLE fillups ADD COLUMN economy DOUBLE;");
			case V5:
				// This is the upgrade to 3.0 -- brace for impact!

				if (backupExistingTables() && createNewTables() && migrateOldData() && populateCalculatedFields() && cleanUpOldTables()) {
					Log.d(TAG, "Completed migration!");
				} else {
					Log.e(TAG, "Unable to complete migration!");
				}
		}
	}

	private static final void flush() {
		exec(BUILDER.toString());
		BUILDER.setLength(0);
	}

	private static final void exec(final String query) {
		log(query);
		sDatabase.execSQL(query);
	}

	private static final void log(final String msg) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, msg);
		}
	}

	private static boolean backupExistingTables() {
		String[] tables = new String[] {
				"fillups",
				"vehicles",
				"maintenance_intervals"
		};

		try {
			for (String table : tables) {
				BUILDER.append("ALTER TABLE ").append(table).append(" RENAME TO OLD_").append(table);
				flush();
			}
			return true;
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to backup existing tables!", e);
		}
		return false;
	}

	private static boolean createNewTables() {
		ContentTable[] tables = new ContentTable[] {
				new FillupsTable(),
				new FillupsFieldsTable(),
				new FieldsTable(),
				new VehiclesTable(),
				new VehicleTypesTable(),
				new ServiceIntervalsTable(),
				new ServiceIntervalTemplatesTable(),
				new CacheTable()
		};

		try {
			for (ContentTable table : tables) {
				exec(table.create());
				String init = table.init();
				if (init != null) {
					exec(init);
				}
			}
			return true;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Unable to create new table!", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Unable to create new table!", e);
		}
		return false;
	}

	private static boolean migrateOldData() {
		try {
			// migrate vehicle data
			BUILDER.append("INSERT INTO ").append(VehiclesTable.TABLE_NAME).append("(");
			BUILDER.append(Vehicle.MAKE).append(", ");
			BUILDER.append(Vehicle.MODEL).append(", ");
			BUILDER.append(Vehicle.TITLE).append(", ");
			BUILDER.append(Vehicle.DEFAULT_TIME);
			BUILDER.append(") SELECT make, model, title, def FROM OLD_vehicles;");
			flush();
			// migrate service intervals
			// migrate fillup data
			return true;
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to migrate data!", e);
		}
		return false;
	}

	private static boolean populateCalculatedFields() {
		return true;
	}

	private static boolean cleanUpOldTables() {
		try {
			exec("DROP TABLE OLD_vehicles");
			exec("DROP TABLE OLD_fillups");
			exec("DROP TABLE OLD_maintenance_intervals");
			return true;
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to clean up old tables!", e);
			return false;
		}
	}
}
