package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class FillupsTable extends ContentTable {
	// make sure it's globally unique
	private static final int FILLUPS = 10;
	private static final int FILLUP_ID = 11;

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.fillup";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.fillup";

	/**
	 * Gets all fillups
	 */
	public static final String FILLUPS_URI = "fillups";

	/**
	 * Gets a specific fillup
	 */
	public static final String FILLUP_URI = "fillup";

	public static final String[] getFullProjectionArray() {
		return new String[] {
				Dao._ID,
				Fillup.TOTAL_COST,
				Fillup.UNIT_PRICE,
				Fillup.VOLUME,
				Fillup.ODOMETER,
				Fillup.ECONOMY,
				Fillup.VEHICLE_ID,
				Fillup.DATE,
				Fillup.LATITUDE,
				Fillup.LONGITUDE,
				Fillup.PARTIAL,
				Fillup.RESTART
		};
	}

	@Override
	public String getTableName() {
		return "fillups";
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, FILLUPS_URI, FILLUPS);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, FILLUP_URI + "/#", FILLUP_ID);
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
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case FILLUPS:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(final int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case FILLUPS:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case FILLUP_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(Fillup._ID + " = " + uri.getPathSegments().get(1));
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
				String clause = Fillup._ID + " = " + fillUpId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				return db.update(getTableName(), values, clause, selectionArgs);
		}
		return -1;
	}

	@Override
	public String init() {
		return null;
	}

	@Override
	public String create() {
		return new TableBuilder().addDouble(Fillup.TOTAL_COST).addDouble(Fillup.UNIT_PRICE).addDouble(Fillup.VOLUME).addDouble(Fillup.ODOMETER)
				.addDouble(Fillup.ECONOMY).addInteger(Fillup.VEHICLE_ID).addInteger(Fillup.DATE).addDouble(Fillup.LATITUDE).addDouble(
						Fillup.LONGITUDE).addText(Fillup.COMMENT).addInteger(Fillup.PARTIAL).addInteger(Fillup.RESTART).toString();
	}

	@Override
	public String upgrade(final int currentVersion) {
		return null;
	}

	@Override
	public String getDefaultSortOrder() {
		return Fillup.ODOMETER + " desc";
	}
}
