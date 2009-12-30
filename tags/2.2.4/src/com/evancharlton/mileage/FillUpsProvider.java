package com.evancharlton.mileage;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.ServiceInterval;
import com.evancharlton.mileage.models.Vehicle;

/**
 * Note that this app does not currently (as of version > 1.8.4) use a
 * ContentProvider for data access (this should all be done through the
 * appropriate data Model subclasses). As a result, this class might be updated,
 * but its use is highly discouraged (at least until this class is officially
 * supported).
 * 
 */
public class FillUpsProvider extends ContentProvider {
	public static final String DATABASE_NAME = "mileage.db";
	public static final int DATABASE_VERSION = 4;
	public static final String FILLUPS_TABLE_NAME = "fillups";
	public static final String VEHICLES_TABLE_NAME = "vehicles";
	public static final String MAINTENANCE_TABLE_NAME = "maintenance_intervals";
	public static final String VERSION_TABLE_NAME = "version";

	public static final String VERSION = "version";

	private static HashMap<String, String> s_fillUpsProjectionMap;
	private static HashMap<String, String> s_vehiclesProjectionMap;
	private static HashMap<String, String> s_maintenanceIntervalsProjectionMap;

	private static final int FILLUPS = 1;
	private static final int FILLUP_ID = 2;
	private static final int VEHICLES = 3;
	private static final int VEHICLE_ID = 4;
	private static final int MAINTENANCE_INTERVALS = 5;
	private static final int MAINTENANCE_INTERVAL_ID = 6;

	private static final UriMatcher s_uriMatcher;

	static {
		s_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		s_uriMatcher.addURI(FillUp.AUTHORITY, "fillups", FILLUPS);
		s_uriMatcher.addURI(FillUp.AUTHORITY, "fillups/#", FILLUP_ID);
		s_uriMatcher.addURI(Vehicle.AUTHORITY, "vehicles", VEHICLES);
		s_uriMatcher.addURI(Vehicle.AUTHORITY, "vehicles/#", VEHICLE_ID);
		s_uriMatcher.addURI(ServiceInterval.AUTHORITY, "intervals", MAINTENANCE_INTERVALS);
		s_uriMatcher.addURI(ServiceInterval.AUTHORITY, "intervals/#", MAINTENANCE_INTERVAL_ID);

		s_fillUpsProjectionMap = new HashMap<String, String>();
		s_fillUpsProjectionMap.put(FillUp._ID, FillUp._ID);
		s_fillUpsProjectionMap.put(FillUp.PRICE, FillUp.PRICE);
		s_fillUpsProjectionMap.put(FillUp.AMOUNT, FillUp.AMOUNT);
		s_fillUpsProjectionMap.put(FillUp.ODOMETER, FillUp.ODOMETER);
		s_fillUpsProjectionMap.put(FillUp.VEHICLE_ID, FillUp.VEHICLE_ID);
		s_fillUpsProjectionMap.put(FillUp.DATE, FillUp.DATE);
		s_fillUpsProjectionMap.put(FillUp.LATITUDE, FillUp.LATITUDE);
		s_fillUpsProjectionMap.put(FillUp.LONGITUDE, FillUp.LONGITUDE);
		s_fillUpsProjectionMap.put(FillUp.COMMENT, FillUp.COMMENT);
		s_fillUpsProjectionMap.put(FillUp.PARTIAL, FillUp.PARTIAL);
		s_fillUpsProjectionMap.put(FillUp.RESTART, FillUp.RESTART);

		s_vehiclesProjectionMap = new HashMap<String, String>();
		s_vehiclesProjectionMap.put(Vehicle._ID, Vehicle._ID);
		s_vehiclesProjectionMap.put(Vehicle.MAKE, Vehicle.MAKE);
		s_vehiclesProjectionMap.put(Vehicle.MODEL, Vehicle.MODEL);
		s_vehiclesProjectionMap.put(Vehicle.TITLE, Vehicle.TITLE);
		s_vehiclesProjectionMap.put(Vehicle.YEAR, Vehicle.YEAR);
		s_vehiclesProjectionMap.put(Vehicle.DEFAULT, Vehicle.DEFAULT);
		s_vehiclesProjectionMap.put(Vehicle.DISTANCE_UNITS, Vehicle.DISTANCE_UNITS);
		s_vehiclesProjectionMap.put(Vehicle.VOLUME_UNITS, Vehicle.VOLUME_UNITS);

		s_maintenanceIntervalsProjectionMap = new HashMap<String, String>();
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval._ID, ServiceInterval._ID);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.CREATE_DATE, ServiceInterval.CREATE_DATE);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.CREATE_ODOMETER, ServiceInterval.CREATE_ODOMETER);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.DESCRIPTION, ServiceInterval.DESCRIPTION);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.DISTANCE, ServiceInterval.DISTANCE);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.DURATION, ServiceInterval.DURATION);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.VEHICLE_ID, ServiceInterval.VEHICLE_ID);
		s_maintenanceIntervalsProjectionMap.put(ServiceInterval.REPEATING, ServiceInterval.REPEATING);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createDatabase(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
			upgradeDatabase(db, oldVersion, newVersion);
		}
	}

	public static void initTables(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(VEHICLES_TABLE_NAME).append(" (");
		sql.append(Vehicle.MAKE).append(", ").append(Vehicle.MODEL).append(", ");
		sql.append(Vehicle.YEAR).append(", ").append(Vehicle.TITLE).append(", ");
		sql.append(Vehicle.DEFAULT);
		sql.append(") VALUES ('Make', 'Model', '");
		sql.append(Calendar.getInstance().get(Calendar.YEAR));
		sql.append("', 'Default Vehicle', '").append(System.currentTimeMillis()).append("');");
		db.execSQL(sql.toString());

		sql = new StringBuilder();
		sql.append("INSERT INTO ").append(VERSION_TABLE_NAME).append(" (").append(VERSION).append(") VALUES (?)");
		db.execSQL(sql.toString(), new String[] {
			String.valueOf(DATABASE_VERSION)
		});
	}

	private DatabaseHelper m_helper;

	@Override
	public boolean onCreate() {
		m_helper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		int count;
		switch (s_uriMatcher.match(uri)) {
			case FILLUPS:
				count = db.delete(FILLUPS_TABLE_NAME, selection, selectionArgs);
				break;

			case FILLUP_ID:
				String fillUpId = uri.getPathSegments().get(1);
				count = db.delete(FILLUPS_TABLE_NAME, FillUp._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;

			case VEHICLES:
				count = db.delete(VEHICLES_TABLE_NAME, selection, selectionArgs);
				break;

			case VEHICLE_ID:
				String vehicleId = uri.getPathSegments().get(1);
				count = db.delete(VEHICLES_TABLE_NAME, Vehicle._ID + " = " + vehicleId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;

			case MAINTENANCE_INTERVALS:
				count = db.delete(MAINTENANCE_TABLE_NAME, selection, selectionArgs);
				break;

			case MAINTENANCE_INTERVAL_ID:
				String intervalId = uri.getPathSegments().get(1);
				count = db.delete(MAINTENANCE_TABLE_NAME, ServiceInterval._ID + " = " + intervalId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (s_uriMatcher.match(uri)) {
			case FILLUPS:
				return FillUp.CONTENT_TYPE;
			case FILLUP_ID:
				return FillUp.CONTENT_ITEM_TYPE;
			case VEHICLES:
				return Vehicle.CONTENT_TYPE;
			case VEHICLE_ID:
				return Vehicle.CONTENT_ITEM_TYPE;
			case MAINTENANCE_INTERVALS:
				return ServiceInterval.CONTENT_TYPE;
			case MAINTENANCE_INTERVAL_ID:
				return ServiceInterval.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		int match = s_uriMatcher.match(uri);

		switch (match) {
			case FILLUPS:
				return insertFillup(uri, initialValues);
			case VEHICLES:
				return insertVehicle(uri, initialValues);
			case MAINTENANCE_INTERVALS:
				return insertInterval(uri, initialValues);
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	private Uri insertInterval(Uri uri, ContentValues initialValues) {
		ServiceInterval interval = new ServiceInterval(initialValues);
		if (interval.validate() <= 0) {
			long id = interval.save();
			Uri contentUri = ContentUris.withAppendedId(ServiceInterval.CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(contentUri, null);
			return contentUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	private Uri insertFillup(Uri uri, ContentValues initialValues) {
		FillUp fillup = new FillUp(initialValues);
		if (fillup.validate() <= 0) {
			long id = fillup.save();
			Uri contentUri = ContentUris.withAppendedId(FillUp.CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(contentUri, null);
			return contentUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	private Uri insertVehicle(Uri uri, ContentValues initialValues) {
		Vehicle vehicle = new Vehicle(initialValues);
		if (vehicle.validate() <= 0) {
			long id = vehicle.save();
			Uri contentUri = ContentUris.withAppendedId(Vehicle.CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(contentUri, null);
			return contentUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (s_uriMatcher.match(uri)) {
			case FILLUPS:
				qb.setTables(FILLUPS_TABLE_NAME);
				qb.setProjectionMap(s_fillUpsProjectionMap);
				break;
			case FILLUP_ID:
				qb.setTables(FILLUPS_TABLE_NAME);
				qb.setProjectionMap(s_fillUpsProjectionMap);
				qb.appendWhere(FillUp._ID + " = " + uri.getPathSegments().get(1));
				break;
			case VEHICLES:
				qb.setTables(VEHICLES_TABLE_NAME);
				qb.setProjectionMap(s_vehiclesProjectionMap);
				break;
			case VEHICLE_ID:
				qb.setTables(VEHICLES_TABLE_NAME);
				qb.setProjectionMap(s_vehiclesProjectionMap);
				qb.appendWhere(Vehicle._ID + " = " + uri.getPathSegments().get(1));
				break;
			case MAINTENANCE_INTERVALS:
				qb.setTables(MAINTENANCE_TABLE_NAME);
				qb.setProjectionMap(s_maintenanceIntervalsProjectionMap);
				break;
			case MAINTENANCE_INTERVAL_ID:
				qb.setTables(MAINTENANCE_TABLE_NAME);
				qb.setProjectionMap(s_maintenanceIntervalsProjectionMap);
				qb.appendWhere(ServiceInterval._ID + " = " + uri.getPathSegments().get(1));
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		String orderBy = null;
		if (TextUtils.isEmpty(sortOrder)) {
			if (qb.getTables() == FILLUPS_TABLE_NAME) {
				orderBy = FillUp.DEFAULT_SORT_ORDER;
			} else {
				orderBy = Vehicle.DEFAULT_SORT_ORDER;
			}
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db;
		try {
			db = m_helper.getReadableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
			db = m_helper.getWritableDatabase();
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		int count;
		String clause;
		switch (s_uriMatcher.match(uri)) {
			case FILLUPS:
				count = db.update(FILLUPS_TABLE_NAME, values, selection, selectionArgs);
				break;
			case FILLUP_ID:
				String fillUpId = uri.getPathSegments().get(1);
				clause = FillUp._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				count = db.update(FILLUPS_TABLE_NAME, values, clause, selectionArgs);
				break;
			case VEHICLES:
				count = db.update(VEHICLES_TABLE_NAME, values, selection, selectionArgs);
				break;
			case VEHICLE_ID:
				String vehicleId = uri.getPathSegments().get(1);
				clause = Vehicle._ID + " = " + vehicleId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				count = db.update(VEHICLES_TABLE_NAME, values, clause, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static HashMap<String, String> getFillUpsProjection() {
		return s_fillUpsProjectionMap;
	}

	public static HashMap<String, String> getVehiclesProjection() {
		return s_vehiclesProjectionMap;
	}

	public static boolean upgradeDatabase() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(new File("/data/data/" + Mileage.PACKAGE + "/databases/" + FillUpsProvider.DATABASE_NAME), null);
		boolean result = upgradeDatabase(db);
		db.close();
		return result;
	}

	public static boolean upgradeDatabase(SQLiteDatabase db) {
		// see if we can determine what version we have
		int oldVersion = -1;
		Cursor c = db.rawQuery("PRAGMA table_info(" + VERSION_TABLE_NAME + ");", null);
		if (c.getCount() == 0) {
			oldVersion = 3;
		} else {
			Cursor versionQuery = db.query(VERSION_TABLE_NAME, new String[] {
				VERSION
			}, null, null, null, null, null);
			if (versionQuery.getCount() == 1) {
				versionQuery.moveToFirst();
				oldVersion = versionQuery.getInt(versionQuery.getColumnIndex(VERSION));
			}
			versionQuery.close();
		}
		c.close();

		if (oldVersion > 0 && oldVersion != DATABASE_VERSION) {
			// Let's do this shit!
			upgradeDatabase(db, oldVersion, oldVersion + 1);
			return true;
		}
		return false;
	}

	private static void upgradeDatabase(SQLiteDatabase db, final int oldVersion, final int newVersion) {
		try {
			if (oldVersion == 3) {
				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.PARTIAL).append(" INTEGER;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.RESTART).append(" INTEGER;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.DISTANCE_UNITS).append(" INTEGER DEFAULT -1;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.VOLUME_UNITS).append(" INTEGER DEFAULT -1;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
				sb.append(ServiceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
				sb.append(ServiceInterval.CREATE_DATE).append(" INTEGER,");
				sb.append(ServiceInterval.CREATE_ODOMETER).append(" DOUBLE,");
				sb.append(ServiceInterval.DESCRIPTION).append(" TEXT,");
				sb.append(ServiceInterval.DISTANCE).append(" DOUBLE,");
				sb.append(ServiceInterval.DURATION).append(" INTEGER,");
				sb.append(ServiceInterval.VEHICLE_ID).append(" INTEGER,");
				sb.append(ServiceInterval.REPEATING).append(" INTEGER");
				sb.append(");");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("CREATE TABLE ").append(VERSION_TABLE_NAME).append(" (");
				sb.append(VERSION).append(" INTEGER");
				sb.append(");");
				db.execSQL(sb.toString());
				return;
			} else if (oldVersion == 2) {
				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.COMMENT).append(" TEXT;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.DEFAULT).append(" INTEGER;");
				db.execSQL(sb.toString());
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS ").append(FILLUPS_TABLE_NAME).append(";");
			db.execSQL(sb.toString());

			sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS ").append(VEHICLES_TABLE_NAME).append(";");
			db.execSQL(sb.toString());

			sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS ").append(MAINTENANCE_TABLE_NAME).append(";");
			db.execSQL(sb.toString());

			createDatabase(db);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
	}

	private static void createDatabase(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(FILLUPS_TABLE_NAME).append(" (");
		sql.append(FillUp._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sql.append(FillUp.PRICE).append(" DOUBLE,");
		sql.append(FillUp.AMOUNT).append(" DOUBLE,");
		sql.append(FillUp.ODOMETER).append(" DOUBLE,");
		sql.append(FillUp.VEHICLE_ID).append(" INTEGER,");
		sql.append(FillUp.DATE).append(" INTEGER,");
		sql.append(FillUp.LATITUDE).append(" DOUBLE,");
		sql.append(FillUp.LONGITUDE).append(" DOUBLE,");
		sql.append(FillUp.COMMENT).append(" TEXT,");
		sql.append(FillUp.PARTIAL).append(" INTEGER,");
		sql.append(FillUp.RESTART).append(" INTEGER");
		sql.append(");");
		db.execSQL(sql.toString());

		sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(VEHICLES_TABLE_NAME).append(" (");
		sql.append(Vehicle._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sql.append(Vehicle.MAKE).append(" TEXT,");
		sql.append(Vehicle.MODEL).append(" TEXT,");
		sql.append(Vehicle.TITLE).append(" TEXT,");
		sql.append(Vehicle.YEAR).append(" TEXT,");
		sql.append(Vehicle.DEFAULT).append(" INTEGER,");
		sql.append(Vehicle.DISTANCE_UNITS).append(" INTEGER DEFAULT -1,");
		sql.append(Vehicle.VOLUME_UNITS).append(" INTEGER DEFAULT -1");
		sql.append(");");
		db.execSQL(sql.toString());

		sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
		sql.append(ServiceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sql.append(ServiceInterval.CREATE_DATE).append(" INTEGER,");
		sql.append(ServiceInterval.CREATE_ODOMETER).append(" DOUBLE,");
		sql.append(ServiceInterval.DESCRIPTION).append(" TEXT,");
		sql.append(ServiceInterval.DISTANCE).append(" DOUBLE,");
		sql.append(ServiceInterval.DURATION).append(" INTEGER,");
		sql.append(ServiceInterval.VEHICLE_ID).append(" INTEGER,");
		sql.append(ServiceInterval.REPEATING).append(" INTEGER");
		sql.append(");");
		db.execSQL(sql.toString());

		sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(VERSION_TABLE_NAME).append(" (");
		sql.append(VERSION).append(" INTEGER");
		sql.append(");");
		db.execSQL(sql.toString());

		initTables(db);
	}
}
