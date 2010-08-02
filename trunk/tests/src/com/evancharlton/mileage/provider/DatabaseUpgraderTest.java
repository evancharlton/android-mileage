package com.evancharlton.mileage.provider;

import java.util.ArrayList;
import java.util.Calendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.ContentTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.tests.TestCase;

/**
 * Verifies that a database can be successfully upgraded from a previous version
 * of Mileage. All the database-creation code was pulled from the previous
 * versions, so it should be accurate.
 * 
 */
public class DatabaseUpgraderTest extends TestCase {
	// version 1.X
	public void testUpgradeFromVersion1X_EmptyDatabase() {
		SQLiteDatabase db = createDatabaseForVersion(3);
		DatabaseUpgrader.upgradeDatabase(db);
		verifyDatabase(db);
		db.close();
	}

	public void testUpgradeFromVersion1X_Vehicles() {
		SQLiteDatabase db = createDatabaseForVersion(3);
		verifyVehicles(db);
		db.close();
	}

	// version 2.X
	public void testUpgradeFromVersion2X_EmptyDatabase() {
		SQLiteDatabase db = createDatabaseForVersion(4);
		DatabaseUpgrader.upgradeDatabase(db);
		verifyDatabase(db);
		db.close();
	}

	public void testUpgradeFromVersion2X_Vehicles() {
		SQLiteDatabase db = createDatabaseForVersion(4);
		verifyVehicles(db);
		db.close();
	}

	private void verifyVehicles(SQLiteDatabase db) {
		db.execSQL("INSERT INTO vehicles (make, model, year) VALUES ('make', 'model', '2345');");
		db.execSQL("INSERT INTO vehicles (make, model, year, title) VALUES ('test_make', 'test_model', '1234', 'test_title')");

		DatabaseUpgrader.upgradeDatabase(db);
		verifyDatabase(db);

		String[] columns = new String[] {
				Vehicle.MAKE,
				Vehicle.MODEL,
				Vehicle.TITLE,
				Vehicle.YEAR,
				Vehicle.VEHICLE_TYPE
		};
		Cursor cursor = db.query(VehiclesTable.TABLE_NAME, columns, null, null, null, null, null);
		cursor.moveToLast();
		assertEquals("test_make", cursor.getString(0));
		assertEquals("test_model", cursor.getString(1));
		assertEquals("test_title", cursor.getString(2));
		assertEquals(1234, cursor.getLong(3));
		assertEquals(1, cursor.getLong(4));

		cursor.moveToPrevious();
		assertEquals("make", cursor.getString(0));
		assertEquals("model", cursor.getString(1));
		assertEquals(2345, cursor.getLong(3));
		assertEquals("2345 make model", cursor.getString(2));

		cursor.close();
	}

	private void verifyDatabase(SQLiteDatabase db) {
		assertEquals(FillUpsProvider.DATABASE_VERSION, db.getVersion());

		Cursor cursor = db.query("sqlite_master", new String[] {
			"name"
		}, "type = ?", new String[] {
			"table"
		}, null, null, null);

		ArrayList<String> tables = new ArrayList<String>();

		while (cursor.moveToNext()) {
			String tableName = cursor.getString(0);
			tables.add(tableName);
			assertFalse(tableName.startsWith("OLD_"));
		}

		for (ContentTable table : FillUpsProvider.TABLES) {
			assertTrue(tables.contains(table.getTableName()));
		}

		cursor.close();
	}

	/**
	 * Returns a database that matches <code>version</version> application's
	 * schema.
	 * 
	 * @param version database version
	 * @return a database for that version of the application.
	 */
	private SQLiteDatabase createDatabaseForVersion(int version) {
		SQLiteDatabase db = SQLiteDatabase.create(null);
		db.setVersion(version);
		final StringBuilder sql = new StringBuilder();

		if (version >= 3) {
			sql.append("CREATE TABLE fillups (");
			sql.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append("cost DOUBLE,");
			sql.append("amount DOUBLE,");
			sql.append("mileage DOUBLE,");
			sql.append("vehicle_id INTEGER,");
			sql.append("date INTEGER,");
			sql.append("latitude DOUBLE,");
			sql.append("longitude DOUBLE,");
			sql.append("comment TEXT");
			sql.append(");");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("CREATE TABLE vehicles (");
			sql.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append("make TEXT,");
			sql.append("model TEXT,");
			sql.append("title TEXT,");
			sql.append("year TEXT,");
			sql.append("def INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("INSERT INTO vehicles (make, model, year, title, def)");
			sql.append(" VALUES ");
			sql.append("('Make', 'Model', '");
			sql.append(Calendar.getInstance().get(Calendar.YEAR));
			sql.append("', 'Default Vehicle', '").append(System.currentTimeMillis()).append("');");
			db.execSQL(sql.toString());
		}

		if (version >= 4) {
			sql.setLength(0);
			sql.append("ALTER TABLE fillups ADD COLUMN ").append("is_partial INTEGER;");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("ALTER TABLE fillups ADD COLUMN ").append("restart INTEGER;");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("ALTER TABLE vehicles ADD COLUMN ").append("distance INTEGER DEFAULT -1;");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("ALTER TABLE vehicles ADD COLUMN ").append("volume INTEGER DEFAULT -1;");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("CREATE TABLE maintenance_intervals (");
			sql.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append("creation_date INTEGER,");
			sql.append("creation_odometer DOUBLE,");
			sql.append("description TEXT,");
			sql.append("interval_distance DOUBLE,");
			sql.append("interval_duration INTEGER,");
			sql.append("vehicle_id INTEGER,");
			sql.append("is_repeating INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			sql.setLength(0);
			sql.append("CREATE TABLE version (");
			sql.append("version INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());
		}

		if (version >= 5) {

		}
		return db;
	}
}
