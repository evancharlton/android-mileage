
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.io.importers.CsvWizardActivity;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.tasks.CsvVehicleReaderTask;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CsvVehicleMappingActivity extends CsvWizardActivity {
    private final ArrayList<HashMap<String, String>> mVehicles = new ArrayList<HashMap<String, String>>();
    private final ArrayList<SimpleAdapter> mAdapters = new ArrayList<SimpleAdapter>();
    private final HashMap<Long, Spinner> mVehicleMapping = new HashMap<Long, Spinner>();

    private int mRowCount = 100;
    private CsvVehicleReaderTask mVehicleReaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(this);

        String[] from = new String[] {
                Vehicle.TITLE
        };
        int[] to = new int[] {
                android.R.id.text1
        };

        Cursor vehicleCursor = getContentResolver().query(VehiclesTable.BASE_URI,
                VehiclesTable.PROJECTION, null, null, null);
        while (vehicleCursor.moveToNext()) {
            View row = inflater.inflate(R.layout.import_csv_mapping, mContainer);
            TextView label = (TextView) row.findViewById(R.id.title);
            String title = vehicleCursor.getString(vehicleCursor.getColumnIndex(Vehicle.TITLE));
            label.setText(title);
            label.setId(title.hashCode());

            SimpleAdapter vehicles = new SimpleAdapter(this, mVehicles,
                    android.R.layout.simple_spinner_item, from, to);
            vehicles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner = (Spinner) row.findViewById(R.id.mappings);
            spinner.setAdapter(vehicles);
            spinner.setId(title.hashCode());
            mAdapters.add(vehicles);

            mVehicleMapping.put(vehicleCursor.getLong(vehicleCursor.getColumnIndex(Vehicle._ID)),
                    spinner);
        }
        vehicleCursor.close();

        restoreTask();

        setHeaderText(R.string.import_csv_vehicle_text);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mVehicleReaderTask;
    }

    private void restoreTask() {
        mVehicleReaderTask = (CsvVehicleReaderTask) getLastNonConfigurationInstance();

        if (mVehicleReaderTask == null) {
            mVehicleReaderTask = new CsvVehicleReaderTask(getIntent().getIntExtra(
                    Fillup.VEHICLE_ID, 0));
        }
        mVehicleReaderTask.attach(this);
        if (mVehicleReaderTask.getStatus() == AsyncTask.Status.PENDING) {
            mVehicleReaderTask.execute(getIntent().getStringExtra(ImportActivity.FILENAME));
        }
    }

    @Override
    protected boolean buildIntent(Intent intent) {
        intent.setClass(this, CsvDateFormatActivity.class);

        for (Long vehicleId : mVehicleMapping.keySet()) {
            Spinner spinner = mVehicleMapping.get(vehicleId);
            HashMap<String, String> mapping = mVehicles.get(spinner.getSelectedItemPosition());
            String title = mapping.get(Vehicle.TITLE);
            intent.putExtra("vehicle_" + title, vehicleId);
        }

        intent.putExtra(CsvImportActivity.TOTAL_ROWS, mRowCount);
        setResult(PREVIOUS);
        return true;
    }

    public void dataRead(String vehicle) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Vehicle.TITLE, vehicle);
        mVehicles.add(map);

        for (SimpleAdapter adapter : mAdapters) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setRowCount(int rowCount) {
        mRowCount = rowCount;
    }
}
