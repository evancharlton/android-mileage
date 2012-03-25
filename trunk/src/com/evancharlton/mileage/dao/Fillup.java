
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.FillupsFieldsTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;

@DataObject(path = FillupsTable.URI)
public class Fillup extends Dao {
    public static final String TOTAL_COST = "total_cost";

    public static final String UNIT_PRICE = "price";

    public static final String VOLUME = "volume";

    public static final String ODOMETER = "odometer";

    public static final String DATE = "timestamp"; // ms since epoch

    public static final String PARTIAL = "is_partial";

    public static final String VEHICLE_ID = "vehicle_id";

    public static final String LATITUDE = "latitude";

    public static final String LONGITUDE = "longitude";

    public static final String COMMENT = "comment";

    public static final String RESTART = "restart";

    public static final String ECONOMY = "economy";

    @Range.Positive
    @Validate(R.string.error_no_vehicle_specified)
    @Column(type = Column.INTEGER, name = VEHICLE_ID)
    protected long mVehicleId;

    @Range.Positive
    @Validate(R.string.error_no_odometer_specified)
    @Column(type = Column.DOUBLE, name = ODOMETER)
    protected double mOdometer;

    @Past
    @Validate(R.string.error_date_in_past)
    @Column(type = Column.TIMESTAMP, name = DATE)
    protected Date mDate;

    @Range.Positive
    @Validate(R.string.error_no_volume_specified)
    @Column(type = Column.DOUBLE, name = VOLUME)
    protected double mVolume;

    @Range.Positive
    @Validate(R.string.error_no_price_specified)
    @Column(type = Column.DOUBLE, name = UNIT_PRICE)
    protected double mUnitPrice;

    @Range.Positive
    @Validate(R.string.error_no_total_cost_specified)
    @Column(type = Column.DOUBLE, name = TOTAL_COST)
    protected double mTotalCost;

    @Validate
    @Column(type = Column.BOOLEAN, name = PARTIAL, value = 0)
    protected boolean mIsPartial;

    @Validate
    @Column(type = Column.BOOLEAN, name = RESTART)
    protected boolean mIsRestart;

    @Validate
    @Range.Positive
    @Column(type = Column.DOUBLE, name = ECONOMY)
    protected double mEconomy;

    @Validate
    @Column(type = Column.DOUBLE, name = LATITUDE)
    protected double mLatitude;

    @Validate
    @Column(type = Column.DOUBLE, name = LONGITUDE)
    protected double mLongitude;

    private final ArrayList<FillupField> mFields = new ArrayList<FillupField>();

    private Fillup mNext = null;

    private Fillup mPrevious = null;

    public Fillup(ContentValues contentValues) {
        super(contentValues);
    }

    public Fillup(Cursor cursor) {
        super(cursor);
    }

    @Override
    protected void preValidate() {
        getVolume();
        getUnitPrice();
        getTotalCost();
        if (mDate == null) {
            mDate = new Date(System.currentTimeMillis());
        }
    }

    public Fillup loadPrevious(Context context) {
        Fillup previous = null;
        if (!mIsRestart) {
            Uri uri = FillupsTable.BASE_URI;
            String[] projection = FillupsTable.PROJECTION;
            Cursor c =
                    context.getContentResolver().query(uri, projection,
                            Fillup.VEHICLE_ID + " = ? AND " + ODOMETER + " < ?", new String[] {
                                    String.valueOf(getVehicleId()), String.valueOf(getOdometer())
                            }, Fillup.ODOMETER + " desc");
            if (c.getCount() >= 1) {
                c.moveToFirst();
                previous = new Fillup(c);
            }
            c.close();
        }
        return previous;
    }

    public Fillup loadNext(Context context) {
        Fillup next = null;
        if (!mIsRestart) {
            Uri uri = FillupsTable.BASE_URI;
            String[] projection = FillupsTable.PROJECTION;
            Cursor c =
                    context.getContentResolver().query(uri, projection,
                            Fillup.VEHICLE_ID + " = ? AND " + ODOMETER + " > ?", new String[] {
                                    String.valueOf(getVehicleId()), String.valueOf(getOdometer())
                            }, Fillup.ODOMETER + " asc");
            if (c.getCount() >= 1) {
                c.moveToFirst();
                next = new Fillup(c);
            }
            c.close();
        }
        return next;
    }

    public double getEconomy() {
        return mEconomy;
    }

    public ArrayList<FillupField> getFields() {
        return mFields;
    }

    /**
     * Note that this is run synchronously!
     * 
     * @param context
     * @return
     */
    public ArrayList<FillupField> getFields(Context context) {
        if (mFields.size() == 0) {
            Uri uri = ContentUris.withAppendedId(FillupsFieldsTable.FILLUPS_FIELDS_URI, getId());
            Cursor c =
                    context.getContentResolver().query(uri, FillupsFieldsTable.PROJECTION, null,
                            null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                FillupField field = new FillupField(c);
                mFields.add(field);
            }
            c.close();
        }
        return mFields;
    }

    public Fillup getNext() {
        return mNext;
    }

    public double getOdometer() {
        return mOdometer;
    }

    public double getDistance() {
        return getOdometer() - mPrevious.getOdometer();
    }

    public Fillup getPrevious() {
        return mPrevious;
    }

    public long getTimestamp() {
        if (mDate == null) {
            mDate = new Date();
        }
        return mDate.getTime();
    }

    public double getTotalCost() {
        if (mTotalCost == 0 && (mVolume > 0 && mUnitPrice > 0)) {
            mTotalCost = mVolume * mUnitPrice;
            setInMemoryDataChanged();
        }
        return mTotalCost;
    }

    public double getUnitPrice() {
        if (mUnitPrice == 0 && (mVolume > 0 && mTotalCost > 0)) {
            mUnitPrice = mTotalCost / mVolume;
            setInMemoryDataChanged();
        }
        return mUnitPrice;
    }

    public long getVehicleId() {
        return mVehicleId;
    }

    public double getVolume() {
        if (mVolume == 0 && (mTotalCost > 0 && mUnitPrice > 0)) {
            mVolume = mTotalCost / mUnitPrice;
            setInMemoryDataChanged();
        }
        return mVolume;
    }

    public double getCostPerDistance() {
        if (hasPrevious() && getDistance() > 0) {
            return getTotalCost() / getDistance();
        }
        return -1D;
    }

    public boolean hasNext() {
        return mNext != null;
    }

    public boolean hasPrevious() {
        return mPrevious != null;
    }

    public boolean isPartial() {
        return mIsPartial;
    }

    public boolean isRestart() {
        return mIsRestart;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setEconomy(double economy) {
        mEconomy = economy;
        setInMemoryDataChanged();
    }

    public void setFields(ArrayList<FillupField> fields) {
        mFields.clear();
        mFields.addAll(fields);
    }

    public void setNext(Fillup next) {
        mNext = next;
    }

    public void setOdometer(double odometer) {
        mOdometer = odometer;
    }

    public void setPartial(boolean partial) {
        mIsPartial = partial;
    }

    public void setPrevious(Fillup previous) {
        mPrevious = previous;
    }

    public void setRestart(boolean restart) {
        mIsRestart = restart;
    }

    public void setTimestamp(long timestamp) {
        if (mDate == null) {
            mDate = new Date(timestamp);
        } else {
            mDate.setTime(timestamp);
        }
    }

    public void setTotalCost(double totalCost) {
        mTotalCost = totalCost;
    }

    public void setUnitPrice(double unitPrice) {
        mUnitPrice = unitPrice;
    }

    public void setVehicleId(long id) {
        mVehicleId = id;
    }

    public void setVolume(double volume) {
        mVolume = volume;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    /**
     * @return true if this fillup should be counted in an economy calculation.
     */
    public boolean validForEconomy() {
        if (!hasPrevious()) {
            return false;
        }
        if (isPartial()) {
            // Walk the chain to see if there's a following complete fillup.
            Fillup fillup = this;
            while (fillup.hasNext()) {
                if (fillup.isPartial() == false) {
                    // Found a following complete; this is a valid fillup.
                    return true;
                }
                fillup = fillup.getNext();
            }
            return false;
        }
        return true;
    }
}
