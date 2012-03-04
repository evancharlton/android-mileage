
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

@DataObject(path = VehiclesTable.VEHICLES_URI)
public class Vehicle extends Dao {
    public static final String TITLE = "title";

    public static final String DESCRIPTION = "description";

    public static final String YEAR = "year";

    public static final String MAKE = "make";

    public static final String MODEL = "model";

    public static final String VEHICLE_TYPE = "vehicle_type_id";

    public static final String DEFAULT_TIME = "default_time";

    public static final String PREF_DISTANCE_UNITS = "odometer_units";

    public static final String PREF_VOLUME_UNITS = "volume_units";

    public static final String PREF_ECONOMY_UNITS = "economy_units";

    public static final String PREF_CURRENCY = "currency_units";

    @Validate(R.string.error_invalid_vehicle_title)
    @Column(type = Column.STRING, name = TITLE)
    protected String mTitle;

    @Validate
    @Nullable
    @Column(type = Column.STRING, name = DESCRIPTION)
    protected String mDescription;

    @Validate(R.string.error_invalid_vehicle_year)
    @Column(type = Column.STRING, name = YEAR)
    protected String mYear;

    @Validate(R.string.error_invalid_vehicle_make)
    @Column(type = Column.STRING, name = MAKE)
    protected String mMake;

    @Validate(R.string.error_invalid_vehicle_model)
    @Column(type = Column.STRING, name = MODEL)
    protected String mModel;

    @Validate(R.string.error_invalid_vehicle_type)
    @Range.Positive
    @Column(type = Column.LONG, name = VEHICLE_TYPE)
    protected long mVehicleType;

    @Validate
    @Column(type = Column.LONG, name = DEFAULT_TIME)
    protected long mDefaultTime;

    @Validate
    @Column(type = Column.INTEGER, name = PREF_DISTANCE_UNITS, value = Calculator.MI)
    protected int mPrefDistanceUnits;

    @Validate
    @Column(type = Column.INTEGER, name = PREF_VOLUME_UNITS, value = Calculator.GALLONS)
    protected int mPrefVolumeUnits;

    @Validate
    @Column(type = Column.INTEGER, name = PREF_ECONOMY_UNITS, value = Calculator.MI_PER_GALLON)
    protected int mPrefEconomyUnits;

    @Validate
    @Column(type = Column.STRING, name = PREF_CURRENCY)
    protected String mPrefCurrency;

    public Vehicle(ContentValues values) {
        super(values);
    }

    public Vehicle(Cursor cursor) {
        super(cursor);
    }

    public static final Vehicle loadById(final Context context, final long id) {
        Uri uri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, id);
        Cursor cursor =
                context.getContentResolver().query(uri, VehiclesTable.PROJECTION, null, null, null);
        Vehicle v = null;
        if (cursor.getCount() > 0) {
            v = new Vehicle(cursor);
        }
        cursor.close();
        if (v == null) {
            throw new IllegalArgumentException("Unable to load vehicle #" + id);
        }
        return v;
    }

    public Fillup loadLatestFillup(Context context) {
        Uri uri = FillupsTable.BASE_URI;
        String[] projection = FillupsTable.PROJECTION;
        Cursor c =
                context.getContentResolver().query(uri, projection, Fillup.VEHICLE_ID + " = ?",
                        new String[] {
                            String.valueOf(getId())
                        }, Fillup.ODOMETER + " desc");

        Fillup newest = null;
        if (c.getCount() >= 1) {
            c.moveToFirst();
            newest = new Fillup(c);
        }
        c.close();
        return newest;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public void setMake(String make) {
        mMake = make;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public void setVehicleType(long vehicleType) {
        mVehicleType = vehicleType;
    }

    public void setDefaultTime(long defaultTime) {
        mDefaultTime = defaultTime;
    }

    public void setVolumeUnits(int volumeUnits) {
        mPrefVolumeUnits = volumeUnits;
    }

    public void setDistanceUnits(int distanceUnits) {
        mPrefDistanceUnits = distanceUnits;
    }

    public void setEconomyUnits(int economyUnits) {
        mPrefEconomyUnits = economyUnits;
    }

    public void setCurrency(String currency) {
        mPrefCurrency = currency;
    }

    public String getTitle() {
        if (mTitle.trim().length() == 0) {
            mTitle = String.format("%s %s %s", getYear(), getMake(), getModel());
        }
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getYear() {
        return mYear;
    }

    public String getMake() {
        return mMake;
    }

    public String getModel() {
        return mModel;
    }

    public long getVehicleType() {
        return mVehicleType;
    }

    public long getDefaultTime() {
        return mDefaultTime;
    }

    public int getDistanceUnits() {
        return mPrefDistanceUnits;
    }

    public int getVolumeUnits() {
        return mPrefVolumeUnits;
    }

    public int getEconomyUnits() {
        return mPrefEconomyUnits;
    }

    public String getCurrency() {
        return mPrefCurrency;
    }
}
