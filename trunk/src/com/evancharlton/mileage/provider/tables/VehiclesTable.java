
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class VehiclesTable extends ContentTable {
    // make sure it's globally unique
    private static final int VEHICLES = 40;

    private static final int VEHICLE_ID = 41;

    public static final String TABLE_NAME = "vehicles";

    public static final String VEHICLES_URI = "vehicles/";

    public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, VEHICLES_URI);

    private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicles";

    private static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.evancharlton.vehicle";

    public static String[] PROJECTION = new String[] {
            Vehicle._ID, Vehicle.TITLE, Vehicle.DESCRIPTION, Vehicle.YEAR, Vehicle.MAKE,
            Vehicle.MODEL, Vehicle.VEHICLE_TYPE, Vehicle.DEFAULT_TIME, Vehicle.PREF_DISTANCE_UNITS,
            Vehicle.PREF_VOLUME_UNITS, Vehicle.PREF_ECONOMY_UNITS, Vehicle.PREF_CURRENCY
    };

    @Override
    protected Class<? extends Dao> getDaoType() {
        return Vehicle.class;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getType(int type) {
        switch (type) {
            case VEHICLES:
                return CONTENT_TYPE;
            case VEHICLE_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public String[] init(boolean isUpgrade) {
        if (isUpgrade) {
            return null;
        }
        // FIXME: hardcoded strings = bad!
        return new String[] {
            new InsertBuilder().add(Vehicle.TITLE, "Default vehicle")
                    .add(Vehicle.DESCRIPTION, "Auto-generated vehicle")
                    .add(Vehicle.DEFAULT_TIME, System.currentTimeMillis())
                    .add(Vehicle.MAKE, "Android").add(Vehicle.MODEL, "Mileage")
                    .add(Vehicle.YEAR, "2010").add(Vehicle.VEHICLE_TYPE, 1).build()
        };
    }

    @Override
    public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
        switch (type) {
            case VEHICLES:
                return db.insert(getTableName(), null, initialValues);
        }
        return -1L;
    }

    @Override
    public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder, Context context,
            String[] projection) {
        switch (type) {
            case VEHICLES:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                return true;
            case VEHICLE_ID:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                queryBuilder.appendWhere(BaseColumns._ID + " = " + uri.getPathSegments().get(1));
                return true;
        }
        return false;
    }

    @Override
    public void registerUris() {
        FillUpsProvider.registerUri(this, VEHICLES_URI, VEHICLES);
        FillUpsProvider.registerUri(this, VEHICLES_URI + "#", VEHICLE_ID);
    }

    @Override
    public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        switch (match) {
            case VEHICLE_ID:
                return db.update(getTableName(), values, Vehicle._ID + " = ?", new String[] {
                    uri.getPathSegments().get(1)
                });
            case VEHICLES:
                return db.update(getTableName(), values, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String getDefaultSortOrder() {
        return Vehicle.DEFAULT_TIME + " desc";
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }
}
