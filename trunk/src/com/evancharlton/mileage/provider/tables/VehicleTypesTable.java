
package com.evancharlton.mileage.provider.tables;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.VehicleType;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class VehicleTypesTable extends ContentTable {
    // make sure it's globally unique
    private static final int TYPES = 50;

    private static final int TYPE_ID = 51;

    public static final String URI = "vehicles/types/";

    public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, URI);

    private static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.evancharlton.vehicle_types";

    private static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.evancharlton.vehicle_type";

    public static final String[] PROJECTION = new String[] {
            VehicleType._ID, VehicleType.TITLE, VehicleType.DESCRIPTION
    };

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
    public String[] init(boolean isUpgrade) {
        // FIXME: hardcoded strings = bad!
        return new String[] {
            new InsertBuilder().add(VehicleType.TITLE, "Car")
                    .add(VehicleType.DESCRIPTION, "Passenger car").build()
        };
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
    public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder, Context context,
            String[] projection) {
        switch (type) {
            case TYPES:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                return true;
            case TYPE_ID:
                queryBuilder.setTables(getTableName());
                queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
                queryBuilder.appendWhere(VehicleType._ID + " = " + uri.getPathSegments().get(2));
                return true;
        }
        return false;
    }

    @Override
    public void registerUris() {
        FillUpsProvider.registerUri(this, URI, TYPES);
        FillUpsProvider.registerUri(this, URI + "#", TYPE_ID);
    }

    @Override
    public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        switch (match) {
            case TYPE_ID:
                return db.update(getTableName(), values, VehicleType._ID + " = ?", new String[] {
                    values.getAsString(VehicleType._ID)
                });
            case TYPES:
                return db.update(getTableName(), values, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }
}
