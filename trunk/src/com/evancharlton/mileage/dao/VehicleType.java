
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.VehicleTypesTable;

import android.content.ContentValues;
import android.database.Cursor;

@DataObject(path = VehicleTypesTable.URI)
public class VehicleType extends Dao {
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    @Validate(R.string.error_invalid_vehicle_type_title)
    @Column(type = Column.STRING, name = TITLE)
    protected String mTitle;

    @Validate(R.string.error_invalid_vehicle_type_description)
    @CanBeEmpty
    @Column(type = Column.STRING, name = DESCRIPTION)
    protected String mDescription;

    public VehicleType(ContentValues values) {
        super(values);
    }

    public VehicleType(Cursor cursor) {
        super(cursor);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }
}
