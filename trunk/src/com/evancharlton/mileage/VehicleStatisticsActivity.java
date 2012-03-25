
package com.evancharlton.mileage;

import com.evancharlton.mileage.adapters.VehicleStatisticsAdapter;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.Statistic;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.StatisticsGroup;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.tasks.VehicleStatisticsTask;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class VehicleStatisticsActivity extends Activity {
    private static final String TAG = "VehicleStatisticsActivity";

    private static final Statistic[] ECONOMIES = {
            Statistics.AVG_ECONOMY, Statistics.MIN_ECONOMY, Statistics.MAX_ECONOMY
    };

    private static final Statistic[] DISTANCES = {
            Statistics.AVG_DISTANCE, Statistics.MIN_DISTANCE, Statistics.MAX_DISTANCE
    };

    private static final Statistic[] COSTS = {
            Statistics.AVG_COST, Statistics.MIN_COST, Statistics.MAX_COST, Statistics.TOTAL_COST,
            Statistics.LAST_MONTH_COST, Statistics.AVG_MONTHLY_COST, Statistics.LAST_YEAR_COST,
            Statistics.AVG_YEARLY_COST
    };

    private static final Statistic[] COSTS_PER_DISTANCE = {
            Statistics.AVG_COST_PER_DISTANCE, Statistics.MIN_COST_PER_DISTANCE,
            Statistics.MAX_COST_PER_DISTANCE
    };

    private static final Statistic[] PRICES = {
            Statistics.AVG_PRICE, Statistics.MIN_PRICE, Statistics.MAX_PRICE
    };

    private static final Statistic[] CONSUMPTIONS = {
            Statistics.MIN_FUEL, Statistics.MAX_FUEL, Statistics.AVG_FUEL, Statistics.TOTAL_FUEL,
            Statistics.FUEL_PER_YEAR
    };

    private static final Statistic[] LOCATIONS = {
            Statistics.NORTH, Statistics.SOUTH, Statistics.EAST, Statistics.WEST
    };

    private static final StatisticsGroup[] GROUPS = {
            new StatisticsGroup(R.string.stat_fuel_economy, ECONOMIES),
            new StatisticsGroup(R.string.stat_distance_between_fillups, DISTANCES),
            new StatisticsGroup(R.string.stat_fillup_cost, COSTS),
            new StatisticsGroup(R.string.stat_cost_per_distance, COSTS_PER_DISTANCE),
            new StatisticsGroup(R.string.stat_price, PRICES),
            new StatisticsGroup(R.string.stat_fuel, CONSUMPTIONS),
            new StatisticsGroup(R.string.stat_location, LOCATIONS)
    };

    private final Vehicle mVehicle = new Vehicle(new ContentValues());

    private Spinner mVehicleSpinner;

    private ListView mListView;

    private VehicleStatisticsTask mCalculationTask = null;

    private LinearLayout mContainer;

    private ProgressBar mProgressBar;

    private ImageView mCancel;

    private VehicleStatisticsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_statistics);

        Object[] saved = (Object[]) getLastNonConfigurationInstance();
        if (saved != null) {
            mCalculationTask = (VehicleStatisticsTask) saved[0];
            mAdapter = null;
        }
        if (mCalculationTask != null) {
            mCalculationTask.attach(this);
        }

        mListView = (ListView) findViewById(android.R.id.list);
        mVehicleSpinner = (Spinner) findViewById(R.id.vehicle);
        mContainer = (LinearLayout) findViewById(R.id.progress_container);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mCancel = (ImageView) findViewById(R.id.cancel);

        mVehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> list, View row, int position, long id) {
                if (mVehicle.getId() != id) {
                    loadVehicle();
                    cancelTask();
                    recalculate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        loadVehicle();
        recalculate();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View row, int position, long id) {
                position -= mAdapter.getNumHeadersAbove(position);
                Statistic statistic = Statistics.STATISTICS.get(position);
                Class<? extends ChartActivity> target = statistic.getChartClass();
                if (target != null) {
                    Intent intent = new Intent(VehicleStatisticsActivity.this, target);
                    intent.putExtra(ChartActivity.VEHICLE_ID, String.valueOf(mVehicle.getId()));
                    startActivity(intent);
                } else {
                    Toast.makeText(VehicleStatisticsActivity.this, getString(R.string.no_chart),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTask();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadVehicle();
        recalculate();
    }

    private void recalculate() {
        Log.d(TAG, "Checking recalculation ...");
        Cursor c = getCacheCursor();
        if (c.getCount() < Statistics.STATISTICS.size()) {
            calculate();
            Log.d(TAG, "Recalculation started!");
        } else {
            Log.d(TAG, "Recalculation not necessary.");
        }
        setAdapter(c);
    }

    private void cancelTask() {
        if (mCalculationTask != null && mCalculationTask.getStatus() == AsyncTask.Status.RUNNING) {
            mCalculationTask.cancel(true);
        }
    }

    public Cursor getCacheCursor() {
        return managedQuery(CacheTable.BASE_URI, CacheTable.PROJECTION, CachedValue.ITEM
                + " = ? and " + CachedValue.VALID + " = ?", new String[] {
                String.valueOf(mVehicle.getId()), "1"
        }, CachedValue.GROUP + " asc, " + CachedValue.ORDER + " asc");
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setAdapter(Cursor c) {
        if (mAdapter == null) {
            mAdapter = new VehicleStatisticsAdapter(this, mVehicle, GROUPS);
        }
        mAdapter.changeCursor(c);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void calculate() {
        Log.d(TAG, "Recalculating statistics");
        mCalculationTask = new VehicleStatisticsTask();
        mCalculationTask.attach(this);
        mCalculationTask.execute();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return new Object[] {
                mCalculationTask, mAdapter
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Recalculate").setIcon(R.drawable.ic_menu_recalculate);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                calculate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadVehicle() {
        long id = mVehicleSpinner.getSelectedItemId();
        Uri uri = ContentUris.withAppendedId(VehiclesTable.BASE_URI, id);
        Cursor vehicle = managedQuery(uri, VehiclesTable.PROJECTION, null, null, null);
        vehicle.moveToFirst();
        mVehicle.load(vehicle);
    }

    public Vehicle getVehicle() {
        return mVehicle;
    }

    public VehicleStatisticsAdapter getAdapter() {
        return mAdapter;
    }

    public void startCalculations() {
        mContainer.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
    }

    public void stopCalculations() {
        mContainer.setVisibility(View.GONE);
    }
}
