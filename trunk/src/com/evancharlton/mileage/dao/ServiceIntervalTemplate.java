
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

import android.content.ContentValues;
import android.database.Cursor;

@DataObject(path = ServiceIntervalTemplatesTable.URI)
public class ServiceIntervalTemplate extends Dao {
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String VEHICLE_TYPE = "vehicle_type";

    @Validate(R.string.error_invalid_template_title)
    @Column(type = Column.STRING, name = TITLE)
    protected String mTitle;

    @Validate(R.string.error_invalid_template_description)
    @CanBeEmpty
    @Column(type = Column.STRING, name = DESCRIPTION)
    protected String mDescription;

    @Validate(R.string.error_invalid_template_distance)
    @Range.Positive
    @Column(type = Column.LONG, name = DISTANCE)
    protected long mDistance;

    @Validate(R.string.error_invalid_template_duration)
    @Range.Positive
    @Column(type = Column.LONG, name = DURATION)
    protected long mDuration;

    @Validate(R.string.error_invalid_template_vehicle_type)
    @Range.Positive
    @Column(type = Column.LONG, name = VEHICLE_TYPE)
    protected long mVehicleTypeId;

    public ServiceIntervalTemplate(ContentValues values) {
        super(values);
    }

    public ServiceIntervalTemplate(Cursor cursor) {
        super(cursor);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public long getDistance() {
        return mDistance;
    }

    public void setDistance(long distance) {
        mDistance = distance;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public long getVehicleTypeId() {
        return mVehicleTypeId;
    }

    public void setVehicleTypeId(long vehicleTypeId) {
        mVehicleTypeId = vehicleTypeId;
    }
}
