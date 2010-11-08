package com.evancharlton.mileage;

import java.text.DecimalFormat;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.tasks.AverageEconomyTask;
import com.evancharlton.mileage.views.CursorSpinner;

public class FillupListActivity extends BaseListActivity implements AverageEconomyTask.AsyncCallback {
	private static final DecimalFormat ECONOMY_FORMAT = new DecimalFormat("0.00");
	private static final DecimalFormat VOLUME_FORMAT = new DecimalFormat("0.00");

	private CursorSpinner mVehicles;
	private Vehicle mVehicle;
	private double mAvgEconomy;
	private AverageEconomyTask mAverageTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.fillup_list);
	}

	@Override
	protected void initUI() {
		mVehicles = (CursorSpinner) findViewById(R.id.vehicle);
		mVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> list, View row, int position, long id) {
				if (mAverageTask.getStatus() == AsyncTask.Status.RUNNING) {
					mAverageTask.cancel(true);
				}
				mVehicle = getVehicle();
				mAverageTask = new AverageEconomyTask();
				mAverageTask.attach(FillupListActivity.this);
				mAverageTask.execute(mVehicle);

				((SimpleCursorAdapter) getAdapter()).changeCursor(getCursor());
				getAdapter().notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		mVehicle = getVehicle();

		Object saved = getLastNonConfigurationInstance();
		if (saved != null) {
			mAverageTask = (AverageEconomyTask) saved;
		} else {
			mAverageTask = new AverageEconomyTask();
		}
		mAverageTask.attach(this);
		if (mAverageTask.getStatus() != AsyncTask.Status.RUNNING) {
			mAverageTask.execute(mVehicle);
		}
	}

	private void calculate() {
		if (mAverageTask.getStatus() != AsyncTask.Status.FINISHED) {
			mAverageTask.cancel(true);
		}
		mAverageTask = new AverageEconomyTask();
		mAverageTask.attach(this);
		mAverageTask.execute(mVehicle);
	}

	protected final Vehicle getVehicle() {
		Cursor cursor = managedQuery(VehiclesTable.BASE_URI, VehiclesTable.PROJECTION, Vehicle._ID + " = ?", new String[] {
			String.valueOf(mVehicles.getSelectedItemId())
		}, null);
		return new Vehicle(cursor);
	}

	@Override
	public void calculationFinished(double avgEconomy) {
		mAvgEconomy = avgEconomy;
		getAdapter().notifyDataSetChanged();
	}

	@Override
	protected void postUI() {
		((SimpleCursorAdapter) getAdapter()).setViewBinder(mViewBinder);
	}

	@Override
	protected String[] getProjectionArray() {
		return FillupsTable.PROJECTION;
	}

	@Override
	protected String getSelection() {
		return Fillup.VEHICLE_ID + " = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		return new String[] {
			String.valueOf(mVehicles.getSelectedItemId())
		};
	}

	@Override
	protected Uri getUri() {
		return FillupsTable.BASE_URI;
	}

	@Override
	public void onItemClick(long id) {
		loadItem(id, FillupActivity.class);
	}

	@Override
	protected int[] getTo() {
		return new int[] {
				android.R.id.text1,
				R.id.volume,
				R.id.price,
				R.id.economy
		};
	}

	@Override
	protected int getListLayout() {
		return R.layout.fillup_list_item;
	}

	@Override
	protected String[] getFrom() {
		return new String[] {
				Fillup.DATE,
				Fillup.VOLUME,
				Fillup.UNIT_PRICE,
				Fillup.ECONOMY
		};
	}

	@Override
	protected void setupEmptyView() {
		mEmptyView.removeAllViews();
		LayoutInflater.from(this).inflate(R.layout.empty_fillups, mEmptyView);
	}

	@Override
	protected void itemDeleted(long itemId) {
		// Clear out the cache
		getContentResolver().delete(CacheTable.BASE_URI, null, null);
		calculate();
	}

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			switch (columnIndex) {
				case 3: // Fillup.VOLUME
					String volume = VOLUME_FORMAT.format(cursor.getDouble(columnIndex));
					String units = Calculator.getVolumeUnits(FillupListActivity.this, mVehicle);
					tv.setText(volume + " " + units);
					return true;
				case 5: // Fillup.ECONOMY
					double economy = cursor.getDouble(columnIndex);
					if (economy == 0) {
						tv.setText("");
						return true;
					}
					if (mAvgEconomy > 0) {
						if (Calculator.isBetterEconomy(mVehicle, economy, mAvgEconomy)) {
							tv.setTextColor(0xFF0AB807);
						} else {
							tv.setTextColor(0xFFD90000);
						}
					}
					units = Calculator.getEconomyUnitsAbbr(FillupListActivity.this, mVehicle);
					tv.setText(ECONOMY_FORMAT.format(economy) + " " + units);
					return true;
			}
			return false;
		}
	};
}
