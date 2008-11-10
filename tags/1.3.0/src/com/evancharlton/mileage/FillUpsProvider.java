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

public class FillUpsProvider extends ContentProvider {

	// private static final String TAG = "FillUpsProvider";
	public static final String DATABASE_NAME = "mileage.db";
	public static final int DATABASE_VERSION = 3;
	public static final String FILLUPS_TABLE_NAME = "fillups";
	public static final String VEHICLES_TABLE_NAME = "vehicles";

	private static HashMap<String, String> s_fillUpsProjectionMap;
	private static HashMap<String, String> s_vehiclesProjectionMap;

	private static final int FILLUPS = 1;
	private static final int FILLUP_ID = 2;

	private static final int VEHICLES = 3;
	private static final int VEHICLE_ID = 4;

	private static final UriMatcher s_uriMatcher;

	static {
		s_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		s_uriMatcher.addURI(FillUps.AUTHORITY, "fillups", FILLUPS);
		s_uriMatcher.addURI(FillUps.AUTHORITY, "fillups/#", FILLUP_ID);
		s_uriMatcher.addURI(Vehicles.AUTHORITY, "vehicles", VEHICLES);
		s_uriMatcher.addURI(Vehicles.AUTHORITY, "vehicles/#", VEHICLE_ID);

		s_fillUpsProjectionMap = new HashMap<String, String>();
		s_fillUpsProjectionMap.put(FillUps._ID, FillUps._ID);
		s_fillUpsProjectionMap.put(FillUps.COST, FillUps.COST);
		s_fillUpsProjectionMap.put(FillUps.AMOUNT, FillUps.AMOUNT);
		s_fillUpsProjectionMap.put(FillUps.MILEAGE, FillUps.MILEAGE);
		s_fillUpsProjectionMap.put(FillUps.VEHICLE_ID, FillUps.VEHICLE_ID);
		s_fillUpsProjectionMap.put(FillUps.DATE, FillUps.DATE);
		s_fillUpsProjectionMap.put(FillUps.LATITUDE, FillUps.LATITUDE);
		s_fillUpsProjectionMap.put(FillUps.LONGITUDE, FillUps.LONGITUDE);
		s_fillUpsProjectionMap.put(FillUps.COMMENT, FillUps.COMMENT);

		s_vehiclesProjectionMap = new HashMap<String, String>();
		s_vehiclesProjectionMap.put(Vehicles._ID, Vehicles._ID);
		s_vehiclesProjectionMap.put(Vehicles.MAKE, Vehicles.MAKE);
		s_vehiclesProjectionMap.put(Vehicles.MODEL, Vehicles.MODEL);
		s_vehiclesProjectionMap.put(Vehicles.TITLE, Vehicles.TITLE);
		s_vehiclesProjectionMap.put(Vehicles.YEAR, Vehicles.YEAR);
		s_vehiclesProjectionMap.put(Vehicles.DEFAULT, Vehicles.DEFAULT);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuilder sql = new StringBuilder();
			sql.append("CREATE TABLE ").append(FILLUPS_TABLE_NAME).append(" (");
			sql.append(FillUps._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append(FillUps.COST).append(" DOUBLE,");
			sql.append(FillUps.AMOUNT).append(" DOUBLE,");
			sql.append(FillUps.MILEAGE).append(" DOUBLE,");
			sql.append(FillUps.VEHICLE_ID).append(" INTEGER,");
			sql.append(FillUps.DATE).append(" INTEGER,");
			sql.append(FillUps.LATITUDE).append(" DOUBLE,");
			sql.append(FillUps.LONGITUDE).append(" DOUBLE,");
			sql.append(FillUps.COMMENT).append(" TEXT");
			sql.append(");");
			db.execSQL(sql.toString());

			sql = new StringBuilder();
			sql.append("CREATE TABLE ").append(VEHICLES_TABLE_NAME).append(" (");
			sql.append(Vehicles._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			sql.append(Vehicles.MAKE).append(" TEXT,");
			sql.append(Vehicles.MODEL).append(" TEXT,");
			sql.append(Vehicles.TITLE).append(" TEXT,");
			sql.append(Vehicles.YEAR).append(" TEXT,");
			sql.append(Vehicles.DEFAULT).append(" INTEGER");
			sql.append(");");
			db.execSQL(sql.toString());

			sql = new StringBuilder();
			sql.append("INSERT INTO ").append(VEHICLES_TABLE_NAME).append(" (");
			sql.append(Vehicles.MAKE).append(", ").append(Vehicles.MODEL).append(", ");
			sql.append(Vehicles.YEAR).append(", ").append(Vehicles.TITLE).append(", ");
			sql.append(Vehicles.DEFAULT);
			sql.append(") VALUES ('Make', 'Model', '");
			sql.append(Calendar.getInstance().get(Calendar.YEAR));
			sql.append("', 'Default Vehicle', '").append(System.currentTimeMillis()).append("');");
			db.execSQL(sql.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
			// TODO: Abstract this out once we get more DB versions
			if (newVersion == DATABASE_VERSION) {
				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUps.COMMENT).append(" TEXT;");
				db.execSQL(sb.toString());

				sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicles.DEFAULT).append(" INTEGER;");
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
				count = db.delete(FILLUPS_TABLE_NAME, FillUps._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
				break;

			case VEHICLES:
				count = db.delete(VEHICLES_TABLE_NAME, selection, selectionArgs);
				break;

			case VEHICLE_ID:
				String vehicleId = uri.getPathSegments().get(1);
				count = db.delete(VEHICLES_TABLE_NAME, Vehicles._ID + " = " + vehicleId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
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
				return FillUps.CONTENT_TYPE;
			case FILLUP_ID:
				return FillUps.CONTENT_ITEM_TYPE;
			case VEHICLES:
				return Vehicles.CONTENT_TYPE;
			case VEHICLE_ID:
				return Vehicles.CONTENT_ITEM_TYPE;
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
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	private Uri insertFillup(Uri uri, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		// make sure that the values are all set
		if (values.containsKey(FillUps.COST) == false) {
			values.put(FillUps.COST, 0.00D);
		}

		if (values.containsKey(FillUps.MILEAGE) == false) {
			values.put(FillUps.MILEAGE, 0.00D);
		}

		if (values.containsKey(FillUps.AMOUNT) == false) {
			values.put(FillUps.AMOUNT, 0.00D);
		}

		if (values.containsKey(FillUps.DATE) == false) {
			values.put(FillUps.DATE, now);
		}

		if (values.containsKey(FillUps.VEHICLE_ID) == false) {
			values.put(FillUps.VEHICLE_ID, 1);
		}

		if (values.containsKey(FillUps.LATITUDE) == false) {
			values.put(FillUps.LATITUDE, "0.00");
		}

		if (values.containsKey(FillUps.LONGITUDE) == false) {
			values.put(FillUps.LONGITUDE, "0.00");
		}

		if (values.containsKey(FillUps.COMMENT) == false) {
			values.put(FillUps.COMMENT, "");
		}

		SQLiteDatabase db = m_helper.getWritableDatabase();
		long rowId = db.insert(FILLUPS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri fillUpUri = ContentUris.withAppendedId(FillUps.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(fillUpUri, null);
			return fillUpUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	private Uri insertVehicle(Uri uri, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = m_helper.getWritableDatabase();
		long rowId = db.insert(VEHICLES_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri vehicleUri = ContentUris.withAppendedId(Vehicles.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(vehicleUri, null);
			return vehicleUri;
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
				qb.appendWhere(FillUps._ID + " = " + uri.getPathSegments().get(1));
				break;
			case VEHICLES:
				qb.setTables(VEHICLES_TABLE_NAME);
				qb.setProjectionMap(s_vehiclesProjectionMap);
				break;
			case VEHICLE_ID:
				qb.setTables(VEHICLES_TABLE_NAME);
				qb.setProjectionMap(s_vehiclesProjectionMap);
				qb.appendWhere(Vehicles._ID + " = " + uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		String orderBy = null;
		if (TextUtils.isEmpty(sortOrder)) {
			if (qb.getTables() == FILLUPS_TABLE_NAME) {
				orderBy = FillUps.DEFAULT_SORT_ORDER;
			} else {
				orderBy = Vehicles.DEFAULT_SORT_ORDER;
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
				clause = FillUps._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				count = db.update(FILLUPS_TABLE_NAME, values, clause, selectionArgs);
				break;
			case VEHICLES:
				count = db.update(VEHICLES_TABLE_NAME, values, selection, selectionArgs);
				break;
			case VEHICLE_ID:
				String vehicleId = uri.getPathSegments().get(1);
				clause = Vehicles._ID + " = " + vehicleId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
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
