package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.VehicleType;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class VehicleTypesTable extends ContentTable {
	// make sure it's globally unique
	private static final int TYPES = 50;
	private static final int TYPE_ID = 51;

	public static final String URI = "vehicles/types/";

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicle_types";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.vehicle_type";

	@Override
	protected Class<? extends Dao> getDaoType() {
		return VehicleType.class;
	}

	@Override
	public String getTableName() {
		return "vehicle_types";
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case TYPES:
				return CONTENT_TYPE;
			case TYPE_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		// FIXME: hardcoded strings = bad!
		return new InsertBuilder().add(VehicleType.TITLE, "Car").add(VehicleType.DESCRIPTION, "Passenger car").build();
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case TYPES:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case TYPES:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case TYPE_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(VehicleType._ID + " = " + uri.getPathSegments().get(2));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI, TYPES);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI + "#", TYPE_ID);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case TYPE_ID:
				return db.update(getTableName(), values, VehicleType._ID + " = ?", new String[] {
					values.getAsString(VehicleType._ID)
				});
		}
		return -1;
	}

	public static String[] getFullProjectionArray() {
		return new String[] {
				BaseColumns._ID,
				VehicleType.TITLE,
				VehicleType.DESCRIPTION
		};
	}
}
