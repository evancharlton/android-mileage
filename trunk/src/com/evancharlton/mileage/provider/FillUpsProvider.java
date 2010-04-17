package com.evancharlton.mileage.provider;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentUris;
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
import android.util.Log;

import com.evancharlton.mileage.provider.tables.ContentTable;
import com.evancharlton.mileage.provider.tables.FieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

public class FillUpsProvider extends ContentProvider {
	public static final String AUTHORITY = "com.evancharlton.mileage";
	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

	private static final String DATABASE_NAME = "mileage.db";
	private static final int DATABASE_VERSION = 50;
	private static final ArrayList<ContentTable> TABLES = new ArrayList<ContentTable>();
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private DatabaseHelper mDatabaseHelper;

	static {
		TABLES.add(new FillupsTable());
		TABLES.add(new FillupsFieldsTable());
		TABLES.add(new FieldsTable());
		TABLES.add(new VehiclesTable());
		TABLES.add(new VehicleTypesTable());
		TABLES.add(new ServiceIntervalsTable());
		TABLES.add(new ServiceIntervalTemplatesTable());

		for (ContentTable table : TABLES) {
			table.registerUris(URI_MATCHER);
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("FillupsProvider", "Creating database");
			for (ContentTable table : TABLES) {
				String sql = table.create();
				if (sql != null) {
					Log.d(table.getClass().getCanonicalName(), sql);
					db.execSQL(sql);
				}
			}

			initTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if (oldVersion < newVersion) {
				Log.d("FillupsProvider", "Upgrading from " + String.valueOf(oldVersion));
				for (ContentTable table : TABLES) {
					String sql = table.upgrade(oldVersion);
					if (sql != null) {
						Log.d(table.getClass().getCanonicalName(), "Upgrading ...");
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
		long newId = -1L;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		for (ContentTable table : TABLES) {
			newId = table.insert(match, db, initialValues);
			if (newId >= 0) {
				uri = ContentUris.withAppendedId(uri, newId);
				getContext().getContentResolver().notifyChange(uri, null);
				db.close();
				return uri;
			}
		}
		db.close();
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
}
