
package com.evancharlton.mileage.dao;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Dao.DataObject;
import com.evancharlton.mileage.provider.tables.CacheTable;

import android.content.ContentValues;
import android.database.Cursor;

@DataObject(path = CacheTable.URI)
public class CachedValue extends Dao {
    public static final String ITEM = "item";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String VALID = "is_valid";
    public static final String GROUP = "statistics_group";
    public static final String ORDER = "statistics_order";

    @Validate(R.string.error_invalid_statistic_item)
    @Column(type = Column.STRING, name = ITEM)
    protected String mItem;

    @Validate(R.string.error_invalid_statistic_key)
    @Column(type = Column.STRING, name = KEY)
    protected String mKey;

    @Validate
    @Column(type = Column.DOUBLE, name = VALUE)
    protected double mValue;

    @Validate
    @Column(type = Column.BOOLEAN, name = VALID)
    protected boolean mIsValid;

    @Validate
    @Column(type = Column.LONG, name = GROUP, value = 0)
    protected long mGroup;

    @Validate
    @Column(type = Column.LONG, name = ORDER, value = 0)
    protected long mOrder;

    public CachedValue(String key) {
        this(new ContentValues());
        mKey = key;
    }

    public CachedValue(ContentValues values) {
        super(values);

        mItem = getString(values, ITEM, null);
        mKey = getString(values, KEY, null);
        mValue = getDouble(values, VALUE, 0D);
        mIsValid = getBoolean(values, VALID, false);
        mGroup = getLong(values, GROUP, 0);
        mOrder = getLong(values, ORDER, 0);
    }

    public CachedValue(Cursor cursor) {
        super(cursor);
    }

    public String getKey() {
        return mKey;
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double value) {
        mValue = value;
    }

    public long getGroup() {
        return mGroup;
    }

    public void setGroup(long group) {
        mGroup = group;
    }

    public long getOrder() {
        return mOrder;
    }

    public void setOrder(long order) {
        mOrder = order;
    }
}
