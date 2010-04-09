package com.evancharlton.mileage;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.evancharlton.mileage.provider.tables.ContentTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

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
	public static final int DATABASE_VERSION = 50;
	private static final String AUTHORITY = "com.evancharlton.mileage";
	public static final ArrayList<ContentTable> TABLES = new ArrayList<ContentTable>();
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private DatabaseHelper mDatabaseHelper;

	static {
		TABLES.add(new FillupsTable());

		for (ContentTable table : TABLES) {
			table.registerUris(AUTHORITY, URI_MATCHER);
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			for (ContentTable table : TABLES) {
				String sql = table.create();
				if (sql != null) {
					db.execSQL(sql);
				}
			}

			initTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if (oldVersion < newVersion) {
				for (ContentTable table : TABLES) {
					String sql = table.upgrade(oldVersion);
					if (sql != null) {
						db.execSQL(sql);
					}
				}
				onUpgrade(db, oldVersion + 1, newVersion);
			}
		}
	}

	public static void initTables(SQLiteDatabase db) {
		for (ContentTable table : TABLES) {
			String sql = table.init();
			if (sql != null) {
				db.execSQL(sql);
			}
		}
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

		int count = -1;
		for (ContentTable table : TABLES) {
			count = table.delete(db, uri, selection, selectionArgs);
			if (count > 0) {
				break;
			}
		}

		if (count < 0) {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		final int type = URI_MATCHER.match(uri);
		String result = null;
		for (ContentTable table : TABLES) {
			result = table.getType(type);
			if (result != null) {
				return result;
			}
		}
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		final int match = URI_MATCHER.match(uri);
		Uri result = null;
		for (ContentTable table : TABLES) {
			result = table.insert(match, uri, initialValues);
			if (result != null) {
				getContext().getContentResolver().notifyChange(result, null);
				return result;
			}
		}
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		final int match = URI_MATCHER.match(uri);
		boolean changed = false;
		ContentTable queryTable = null;
		for (ContentTable table : TABLES) {
			changed = table.query(match, uri, qb);
			if (changed) {
				queryTable = table;
				break;
			}
		}
		if (!changed) {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		String orderBy = TextUtils.isEmpty(sortOrder) ? queryTable.getDefaultSortOrder() : sortOrder;

		SQLiteDatabase db;
		try {
			db = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			db = mDatabaseHelper.getWritableDatabase();
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final int match = URI_MATCHER.match(uri);
		if (match >= 0) {
			SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
			int count = -1;
			for (ContentTable table : TABLES) {
				count = table.update(match, db, uri, values, selection, selectionArgs);
				if (count >= 0) {
					getContext().getContentResolver().notifyChange(uri, null);
					return count;
				}
			}
		}
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	// private static void upgradeDatabase(SQLiteDatabase db, final int
	// oldVersion, final int newVersion) {
	// try {
	// StringBuilder sb = new StringBuilder();
	// if (oldVersion == 4) {
	// sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.ECONOMY).append(" DOUBLE;");
	// db.execSQL(sb.toString());
	// return;
	// } else if (oldVersion == 3) {
	// sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.PARTIAL).append(" INTEGER;");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.RESTART).append(" INTEGER;");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.DISTANCE_UNITS).append(" INTEGER DEFAULT -1;");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.VOLUME_UNITS).append(" INTEGER DEFAULT -1;");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
	// sb.append(ServiceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
	// sb.append(ServiceInterval.CREATE_DATE).append(" INTEGER,");
	// sb.append(ServiceInterval.CREATE_ODOMETER).append(" DOUBLE,");
	// sb.append(ServiceInterval.DESCRIPTION).append(" TEXT,");
	// sb.append(ServiceInterval.DISTANCE).append(" DOUBLE,");
	// sb.append(ServiceInterval.DURATION).append(" INTEGER,");
	// sb.append(ServiceInterval.VEHICLE_ID).append(" INTEGER,");
	// sb.append(ServiceInterval.REPEATING).append(" INTEGER");
	// sb.append(");");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("CREATE TABLE ").append(VERSION_TABLE_NAME).append(" (");
	// sb.append(VERSION).append(" INTEGER");
	// sb.append(");");
	// db.execSQL(sb.toString());
	// return;
	// } else if (oldVersion == 2) {
	// sb.append("ALTER TABLE ").append(FILLUPS_TABLE_NAME).append(" ADD COLUMN ").append(FillUp.COMMENT).append(" TEXT;");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("ALTER TABLE ").append(VEHICLES_TABLE_NAME).append(" ADD COLUMN ").append(Vehicle.DEFAULT).append(" INTEGER;");
	// db.execSQL(sb.toString());
	// return;
	// }
	// sb.setLength(0);
	// sb.append("DROP TABLE IF EXISTS ").append(FILLUPS_TABLE_NAME).append(";");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("DROP TABLE IF EXISTS ").append(VEHICLES_TABLE_NAME).append(";");
	// db.execSQL(sb.toString());
	//
	// sb.setLength(0);
	// sb.append("DROP TABLE IF EXISTS ").append(MAINTENANCE_TABLE_NAME).append(";");
	// db.execSQL(sb.toString());
	//
	// createDatabase(db);
	// } catch (SQLiteException e) {
	// e.printStackTrace();
	// }
	// }

	private static void createDatabase(SQLiteDatabase db) {
		// sql = new StringBuilder();
		// sql.append("CREATE TABLE ").append(VEHICLES_TABLE_NAME).append(" (");
		// sql.append(Vehicle._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		// sql.append(Vehicle.MAKE).append(" TEXT,");
		// sql.append(Vehicle.MODEL).append(" TEXT,");
		// sql.append(Vehicle.TITLE).append(" TEXT,");
		// sql.append(Vehicle.YEAR).append(" TEXT,");
		// sql.append(Vehicle.DEFAULT).append(" INTEGER,");
		// sql.append(Vehicle.DISTANCE_UNITS).append(" INTEGER DEFAULT -1,");
		// sql.append(Vehicle.VOLUME_UNITS).append(" INTEGER DEFAULT -1");
		// sql.append(");");
		// db.execSQL(sql.toString());
		//
		// sql = new StringBuilder();
		// sql.append("CREATE TABLE ").append(MAINTENANCE_TABLE_NAME).append(" (");
		// sql.append(ServiceInterval._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		// sql.append(ServiceInterval.CREATE_DATE).append(" INTEGER,");
		// sql.append(ServiceInterval.CREATE_ODOMETER).append(" DOUBLE,");
		// sql.append(ServiceInterval.DESCRIPTION).append(" TEXT,");
		// sql.append(ServiceInterval.DISTANCE).append(" DOUBLE,");
		// sql.append(ServiceInterval.DURATION).append(" INTEGER,");
		// sql.append(ServiceInterval.VEHICLE_ID).append(" INTEGER,");
		// sql.append(ServiceInterval.REPEATING).append(" INTEGER");
		// sql.append(");");
		// db.execSQL(sql.toString());
		//
		// sql = new StringBuilder();
		// sql.append("CREATE TABLE ").append(VERSION_TABLE_NAME).append(" (");
		// sql.append(VERSION).append(" INTEGER");
		// sql.append(");");
		// db.execSQL(sql.toString());
	}
}
