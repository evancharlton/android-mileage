package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class ServiceIntervalsTable extends ContentTable {

	private static final int SERVICE_INTERVALS = 70;
	private static final int SERVICE_INTERVAL_ID = 71;

	public static final String URI = "intervals/";

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.interval";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.interval";

	public static String[] getFullProjectionArray() {
		return new String[] {
				ServiceInterval._ID,
				ServiceInterval.TITLE,
				ServiceInterval.DESCRIPTION,
				ServiceInterval.START_DATE,
				ServiceInterval.START_ODOMETER,
				ServiceInterval.TEMPLATE_ID,
				ServiceInterval.VEHICLE_ID,
				ServiceInterval.DURATION,
				ServiceInterval.DISTANCE
		};
	}
	
	@Override
	protected Class<? extends Dao> getDaoType() {
		return ServiceInterval.class;
	}

	@Override
	public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTableName() {
		return "service_intervals";
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case SERVICE_INTERVALS:
				return CONTENT_TYPE;
			case SERVICE_INTERVAL_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		return null;
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case SERVICE_INTERVALS:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case SERVICE_INTERVALS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case SERVICE_INTERVAL_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(ServiceInterval._ID + " = " + uri.getPathSegments().get(1));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI, SERVICE_INTERVALS);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI + "#", SERVICE_INTERVAL_ID);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case SERVICE_INTERVAL_ID:
				return db.update(getTableName(), values, ServiceInterval._ID + " = ?", new String[] {
					values.getAsString(ServiceInterval._ID)
				});
		}
		return -1;
	}

	@Override
	public String upgrade(int currentVersion) {
		return null;
	}
}
