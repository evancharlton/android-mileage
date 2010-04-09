package com.evancharlton.mileage.provider.tables;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.evancharlton.mileage.models.FillUp;

public class FillupsTable extends ContentTable {
	protected static final String TABLE_NAME = "fillups";
	protected static final String DEFAULT_SORT_ORDER = FillUp._ID + " desc";

	// needs to be globally unique
	private static final int FILLUPS = 1;
	private static final int FILLUP_ID = 2;

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.fillup";

	static {
		PROJECTION_MAP.put(FillUp._ID, FillUp._ID);
		PROJECTION_MAP.put(FillUp.PRICE, FillUp.PRICE);
		PROJECTION_MAP.put(FillUp.AMOUNT, FillUp.AMOUNT);
		PROJECTION_MAP.put(FillUp.ODOMETER, FillUp.ODOMETER);
		PROJECTION_MAP.put(FillUp.ECONOMY, FillUp.ECONOMY);
		PROJECTION_MAP.put(FillUp.VEHICLE_ID, FillUp.VEHICLE_ID);
		PROJECTION_MAP.put(FillUp.DATE, FillUp.DATE);
		PROJECTION_MAP.put(FillUp.LATITUDE, FillUp.LATITUDE);
		PROJECTION_MAP.put(FillUp.LONGITUDE, FillUp.LONGITUDE);
		PROJECTION_MAP.put(FillUp.COMMENT, FillUp.COMMENT);
		PROJECTION_MAP.put(FillUp.PARTIAL, FillUp.PARTIAL);
		PROJECTION_MAP.put(FillUp.RESTART, FillUp.RESTART);
	}

	@Override
	public void registerUris(String authority, UriMatcher uriMatcher) {
		uriMatcher.addURI(authority, "fillups", FILLUPS);
		uriMatcher.addURI(authority, "fillups/#", FILLUP_ID);
	}

	@Override
	public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(final int type) {
		switch (type) {
			case FILLUPS:
				return CONTENT_TYPE;
			case FILLUP_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public Uri insert(int type, Uri uri, ContentValues initialValues) {
		if (type == FILLUPS) {
			// save new fillup
			// FIXME
			return ContentUris.withAppendedId(uri, 10);
		}
		return null;
	}

	@Override
	public boolean query(final int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case FILLUPS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(getFullProjectionMap());
				return true;
			case FILLUP_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(getFullProjectionMap());
				queryBuilder.appendWhere(FillUp._ID + " = " + uri.getPathSegments().get(1));
				return true;
		}
		return false;
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case FILLUPS:
				return db.update(getTableName(), values, selection, selectionArgs);
			case FILLUP_ID:
				String fillUpId = uri.getPathSegments().get(1);
				String clause = FillUp._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				return db.update(getTableName(), values, clause, selectionArgs);
		}
		return -1;
	}
}
