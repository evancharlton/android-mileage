package com.evancharlton.mileage;

import java.text.DecimalFormat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.provider.tables.VehiclesTable;
import com.evancharlton.mileage.views.CursorSpinner;

public class FillupListActivity extends BaseListActivity {
	private static final DecimalFormat ECONOMY_FORMAT = new DecimalFormat("0.00");
	private static final DecimalFormat VOLUME_FORMAT = new DecimalFormat("0.00");

	private CursorSpinner mVehicles;
	private Vehicle mVehicle;
	private double mAvgEconomy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.fillup_list);
	}

	@Override
	protected void initUI() {
		mVehicles = (CursorSpinner) findViewById(R.id.vehicle);
		mVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mAdapter.changeCursor(getCursor());
				mAdapter.notifyDataSetInvalidated();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		String selection = CachedValue.KEY + " = ? AND " + CachedValue.ITEM + " = ? AND " + CachedValue.VALID + " = 1";
		String[] selectionArgs = new String[] {
				Statistics.AVG_ECONOMY.getKey(),
				String.valueOf(mVehicles.getSelectedItemId())
		};

		Cursor cursor = managedQuery(CacheTable.BASE_URI, new String[] {
			CachedValue.VALUE
		}, selection, selectionArgs, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			mAvgEconomy = cursor.getDouble(0);
		} else {
			mAvgEconomy = 0D;
		}

		// have to round the average economy
		mAvgEconomy *= 100;
		mAvgEconomy = Math.floor(mAvgEconomy);
		mAvgEconomy /= 100;

		mVehicle = getVehicle();
	}

	protected final Vehicle getVehicle() {
		Cursor cursor = managedQuery(VehiclesTable.BASE_URI, VehiclesTable.PROJECTION, Vehicle._ID + " = ?", new String[] {
			String.valueOf(mVehicles.getSelectedItemId())
		}, null);
		return new Vehicle(cursor);
	}

	@Override
	protected void postUI() {
		mAdapter.setViewBinder(mViewBinder);
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
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
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
						return true;
					}
					if (Calculator.isBetterEconomy(mVehicle, economy, mAvgEconomy)) {
						tv.setTextColor(0xFF0AB807);
					} else {
						Log.d("ViewBinder", economy + " < " + mAvgEconomy);
						tv.setTextColor(0xFFD90000);
					}
					units = Calculator.getEconomyUnitsAbbr(FillupListActivity.this, mVehicle);
					tv.setText(ECONOMY_FORMAT.format(economy) + " " + units);
					return true;
			}
			return false;
		}
	};
}
