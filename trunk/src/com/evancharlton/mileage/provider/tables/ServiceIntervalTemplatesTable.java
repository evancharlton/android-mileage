package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class ServiceIntervalTemplatesTable extends ContentTable {

	private static final int SERVICE_TEMPLATES = 60;
	private static final int SERVICE_TEMPLATE_ID = 61;

	public static final String URI = "intervals/templates";
	public static final String SERVICE_TEMPLATE_URI = "intervals/template";

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.interval_template";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.interval_template";

	public static final String[] getFullProjectionArray() {
		return new String[] {
				ServiceIntervalTemplate._ID,
				ServiceIntervalTemplate.TITLE,
				ServiceIntervalTemplate.DESCRIPTION,
				ServiceIntervalTemplate.DISTANCE,
				ServiceIntervalTemplate.DURATION,
				ServiceIntervalTemplate.VEHICLE_TYPE
		};
	}
	
	@Override
	protected Class<? extends Dao> getDaoType() {
		return ServiceIntervalTemplate.class;
	}

	@Override
	public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTableName() {
		return "service_interval_templates";
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case SERVICE_TEMPLATES:
				return CONTENT_TYPE;
			case SERVICE_TEMPLATE_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		return new InsertBuilder().add(ServiceIntervalTemplate.TITLE, "Oil change").add(ServiceIntervalTemplate.DESCRIPTION, "Standard oil change")
				.add(ServiceIntervalTemplate.DISTANCE, 3000).add(ServiceIntervalTemplate.DURATION, 1000 * 60 * 60 * 24 * 30 * 3).add(
						ServiceIntervalTemplate.VEHICLE_TYPE, 1).build();
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case SERVICE_TEMPLATES:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case SERVICE_TEMPLATES:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				return true;
			case SERVICE_TEMPLATE_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(getFullProjectionArray()));
				queryBuilder.appendWhere(Dao._ID + " = " + uri.getPathSegments().get(2));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, URI, SERVICE_TEMPLATES);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, SERVICE_TEMPLATE_URI + "/#", SERVICE_TEMPLATE_ID);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case SERVICE_TEMPLATE_ID:
				return db.update(getTableName(), values, ServiceIntervalTemplate._ID + " = ?", new String[] {
					values.getAsString(ServiceIntervalTemplate._ID)
				});
		}
		return -1;
	}

	@Override
	public String upgrade(int currentVersion) {
		return null;
	}

}
