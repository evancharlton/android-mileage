
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.CursorSpinner;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class VehicleActivity extends BaseFormActivity {
    private EditText mTitle;

    private EditText mDescription;

    private EditText mMake;

    private EditText mModel;

    private EditText mYear;

    private EditText mCurrency;

    private CheckBox mSetDefault;

    private CursorSpinner mVehicleTypes;

    private Spinner mDistances;

    private Spinner mVolumes;

    private Spinner mEconomies;

    private Vehicle mVehicle = new Vehicle(new ContentValues());

    private int mDistanceUnits = Calculator.MI;

    private int mVolumeUnits = Calculator.GALLONS;

    private int mEconomyUnits = Calculator.MI_PER_GALLON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.vehicle);
    }

    @Override
    protected Dao getDao() {
        return mVehicle;
    }

    @Override
    protected String[] getProjectionArray() {
        return VehiclesTable.PROJECTION;
    }

    @Override
    protected Uri getUri(long id) {
        return ContentUris.withAppendedId(VehiclesTable.BASE_URI, id);
    }

    @Override
    protected void initUI() {
        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);
        mMake = (EditText) findViewById(R.id.make);
        mModel = (EditText) findViewById(R.id.model);
        mYear = (EditText) findViewById(R.id.year);
        mCurrency = (EditText) findViewById(R.id.currency);
        mVehicleTypes = (CursorSpinner) findViewById(R.id.type);
        mSetDefault = (CheckBox) findViewById(R.id.make_default);
        mDistances = (Spinner) findViewById(R.id.distance);
        mVolumes = (Spinner) findViewById(R.id.volume);
        mEconomies = (Spinner) findViewById(R.id.economy);

        mCurrency.setText(Calculator.getCurrencySymbol());
    }

    @Override
    protected void populateUI() {
        mTitle.setText(mVehicle.getTitle());
        mDescription.setText(mVehicle.getDescription());
        mMake.setText(mVehicle.getMake());
        mModel.setText(mVehicle.getModel());
        mYear.setText(mVehicle.getYear());

        Uri uri = VehiclesTable.BASE_URI;
        String[] projection = new String[] {
            Vehicle._ID
        };
        Cursor c = managedQuery(uri, projection, null, null, Vehicle.DEFAULT_TIME + " desc");
        if (c.getCount() > 0) {
            c.moveToFirst();
            mSetDefault.setChecked(c.getLong(0) == mVehicle.getId());
        }

        mDistances.setSelection(getDistanceUnits());
        mVolumes.setSelection(getVolumeUnits());
        mEconomies.setSelection(getEconomyUnits());

        if (mVehicle.isExistingObject()) {
            setTitle(mVehicle.getTitle());
        }

        mCurrency.setText(Calculator.getCurrencySymbol(mVehicle));
    }

    @Override
    protected void setFields() throws InvalidFieldException {
        String title = mTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            throw new InvalidFieldException(mTitle, R.string.error_invalid_vehicle_title);
        }
        mVehicle.setTitle(title);

        String year = mYear.getText().toString().trim();
        if (TextUtils.isEmpty(year)) {
            throw new InvalidFieldException(mYear, R.string.error_invalid_vehicle_year);
        }
        mVehicle.setYear(year);

        String make = mMake.getText().toString().trim();
        if (TextUtils.isEmpty(make)) {
            throw new InvalidFieldException(mMake, R.string.error_invalid_vehicle_make);
        }
        mVehicle.setMake(make);

        String model = mModel.getText().toString().trim();
        if (TextUtils.isEmpty(model)) {
            throw new InvalidFieldException(mModel, R.string.error_invalid_vehicle_model);
        }
        mVehicle.setModel(model);

        String description = mDescription.getText().toString().trim();
        mVehicle.setDescription(description);

        mVehicle.setVehicleType(mVehicleTypes.getSelectedItemId());
        if (mSetDefault.isChecked()) {
            mVehicle.setDefaultTime(System.currentTimeMillis());
        }
        mVehicle.setVolumeUnits(getVolume());
        mVehicle.setDistanceUnits(getDistance());
        mVehicle.setEconomyUnits(getEconomy());

        String currency = mCurrency.getText().toString();
        if (TextUtils.isEmpty(currency)) {
            currency = Calculator.getCurrencySymbol();
        }
        mVehicle.setCurrency(mCurrency.getText().toString());
    }

    @Override
    protected void saved() {
        // invalidate the fields if the economy units changed
        if (mVehicle.getVolumeUnits() != mVolumeUnits
                || mVehicle.getDistanceUnits() != mDistanceUnits
                || mVehicle.getEconomyUnits() != mEconomyUnits) {
            ContentValues values = new ContentValues();
            values.put(Fillup.ECONOMY, 0D);
            String where = Fillup.VEHICLE_ID + " = ?";
            String[] selectionArgs = new String[] {
                String.valueOf(mVehicle.getId())
            };
            Uri uri = FillupsTable.BASE_URI;
            getContentResolver().update(uri, values, where, selectionArgs);

            // Also blow away the statistics cache
            getContentResolver().delete(CacheTable.BASE_URI, CachedValue.ITEM + " = ?",
                    selectionArgs);
        }
        super.saved();
    }

    private int getVolume() {
        switch (mVolumes.getSelectedItemPosition()) {
            case 0:
                return Calculator.GALLONS;
            case 1:
                return Calculator.LITRES;
            case 2:
                return Calculator.IMPERIAL_GALLONS;
        }
        return Calculator.GALLONS;
    }

    private int getVolumeUnits() {
        switch (mVehicle.getVolumeUnits()) {
            case Calculator.GALLONS:
                return 0;
            case Calculator.LITRES:
                return 1;
            case Calculator.IMPERIAL_GALLONS:
                return 2;
        }
        return 0;
    }

    private int getDistance() {
        switch (mDistances.getSelectedItemPosition()) {
            case 0:
                return Calculator.MI;
            case 1:
                return Calculator.KM;
        }
        return Calculator.MI;
    }

    private int getDistanceUnits() {
        switch (mVehicle.getDistanceUnits()) {
            case Calculator.MI:
                return 0;
            case Calculator.KM:
                return 1;
        }
        return 0;
    }

    private int getEconomy() {
        switch (mEconomies.getSelectedItemPosition()) {
            case 0:
                return Calculator.MI_PER_GALLON;
            case 1:
                return Calculator.KM_PER_GALLON;
            case 2:
                return Calculator.MI_PER_IMP_GALLON;
            case 3:
                return Calculator.KM_PER_IMP_GALLON;
            case 4:
                return Calculator.MI_PER_LITRE;
            case 5:
                return Calculator.KM_PER_LITRE;
            case 6:
                return Calculator.GALLONS_PER_100KM;
            case 7:
                return Calculator.LITRES_PER_100KM;
            case 8:
                return Calculator.IMP_GAL_PER_100KM;
        }
        return Calculator.MI_PER_GALLON;
    }

    private int getEconomyUnits() {
        switch (mVehicle.getEconomyUnits()) {
            case Calculator.MI_PER_GALLON:
                return 0;
            case Calculator.KM_PER_GALLON:
                return 1;
            case Calculator.MI_PER_IMP_GALLON:
                return 2;
            case Calculator.KM_PER_IMP_GALLON:
                return 3;
            case Calculator.MI_PER_LITRE:
                return 4;
            case Calculator.KM_PER_LITRE:
                return 5;
            case Calculator.GALLONS_PER_100KM:
                return 6;
            case Calculator.LITRES_PER_100KM:
                return 7;
            case Calculator.IMP_GAL_PER_100KM:
                return 8;
        }
        return 0;
    }

    @Override
    protected int getCreateString() {
        return R.string.add_vehicle;
    }

    @Override
    public boolean canDelete() {
        Cursor count = managedQuery(VehiclesTable.BASE_URI, null, null, null, null);
        return count != null && count.getCount() > 1;
    }
}
