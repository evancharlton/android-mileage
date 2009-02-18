package com.evancharlton.mileage;

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
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.evancharlton.mileage.models.FillUp;
import com.evancharlton.mileage.models.MaintenanceInterval;
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
		s_uriMatcher.addURI(MaintenanceInterval.AUTHORITY, "intervals", MAINTENANCE_INTERVALS);
		s_uriMatcher.addURI(MaintenanceInterval.AUTHORITY, "intervals/#", MAINTENANCE_INTERVAL_ID);

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

		s_vehiclesProjectionMap = new HashMap<String, String>();
		s_vehiclesProjectionMap.put(Vehicle._ID, Vehicle._ID);
		s_vehiclesProjectionMap.put(Vehicle.MAKE, Vehicle.MAKE);
		s_vehiclesProjectionMap.put(Vehicle.MODEL, Vehicle.MODEL);
		s_vehiclesProjectionMap.put(Vehicle.TITLE, Vehicle.TITLE);
		s_vehiclesProjectionMap.put(Vehicle.YEAR, Vehicle.YEAR);
		s_vehiclesProjectionMap.put(Vehicle.DEFAULT, Vehicle.DEFAULT);

		s_maintenanceIntervalsProjectionMap = new HashMap<String, String>();
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval._ID, MaintenanceInterval._ID);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.CREATE_DATE, MaintenanceInterval.CREATE_DATE);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.CREATE_ODOMETER, MaintenanceInterval.CREATE_ODOMETER);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.DESCRIPTION, MaintenanceInterval.DESCRIPTION);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.DISTANCE, MaintenanceInterval.DISTANCE);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.DURATION, MaintenanceInterval.DURATION);
		s_maintenanceIntervalsProjectionMap.put(MaintenanceInterval.VEHICLE_ID, MaintenanceInterval.VEHICLE_ID);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
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
			sql.append(FillUp.PARTIAL).append(" INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			sql = new StringBuilder();
			sql.append("CREATE TABLE ").append(VEHICLES_TABLE_NAME).append(" (");
			sql.append(Vehicle._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append(Vehicle.MAKE).append(" TEXT,");
			sql.append(Vehicle.MODEL).append(" TEXT,");
			sql.append(Vehicle.TITLE).append(" TEXT,");
			sql.append(Vehicle.YEAR).append(" TEXT,");
			sql.append(Vehicle.DEFAULT).append(" INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			sql = new StringBuilder();
			sql.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
			sql.append(MaintenanceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append(MaintenanceInterval.CREATE_DATE).append(" INTEGER,");
			sql.append(MaintenanceInterval.CREATE_ODOMETER).append(" DOUBLE,");
			sql.append(MaintenanceInterval.DESCRIPTION).append(" TEXT,");
			sql.append(MaintenanceInterval.DISTANCE).append(" DOUBLE,");
			sql.append(MaintenanceInterval.DURATION).append(" INTEGER,");
			sql.append(MaintenanceInterval.VEHICLE_ID).append(" INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			initTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if (oldVersion == 3) {
				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.PARTIAL).append(" INTEGER;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
				sb.append(MaintenanceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
				sb.append(MaintenanceInterval.CREATE_DATE).append(" INTEGER,");
				sb.append(MaintenanceInterval.CREATE_ODOMETER).append(" DOUBLE,");
				sb.append(MaintenanceInterval.DESCRIPTION).append(" TEXT,");
				sb.append(MaintenanceInterval.DISTANCE).append(" DOUBLE,");
				sb.append(MaintenanceInterval.DURATION).append(" INTEGER,");
				sb.append(MaintenanceInterval.VEHICLE_ID).append(" INTEGER");
				sb.append(");");
				db.execSQL(sb.toString());
			} else if (newVersion == 2) {
				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.COMMENT).append(" TEXT;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.DEFAULT).append(" INTEGER;");
				db.execSQL(sb.toString());
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("DROP TABLE IF EXISTS ").append(FILLUPS_TABLE_NAME).append(";");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("DROP TABLE IF EXISTS ").append(VEHICLES_TABLE_NAME).append(";");
				db.execSQL(sb.toString());
				onCreate(db);
			}
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
				count = db.delete(MAINTENANCE_TABLE_NAME, MaintenanceInterval._ID + " = " + intervalId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
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
				return MaintenanceInterval.CONTENT_TYPE;
			case MAINTENANCE_INTERVAL_ID:
				return MaintenanceInterval.CONTENT_ITEM_TYPE;
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
		MaintenanceInterval interval = new MaintenanceInterval(initialValues);
		if (interval.validate() <= 0) {
			long id = interval.save();
			Uri contentUri = ContentUris.withAppendedId(MaintenanceInterval.CONTENT_URI, id);
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
				qb.appendWhere(MaintenanceInterval._ID + " = " + uri.getPathSegments().get(1));
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

		SQLiteDatabase db = m_helper.getReadableDatabase();
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
}
