package com.evancharlton.mileage;

import java.text.DecimalFormat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.views.CursorSpinner;

public class FillupListActivity extends BaseListActivity {
	private static final DecimalFormat ECONOMY_FORMAT = new DecimalFormat("0.00");
	private CursorSpinner mVehicles;

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
	}

	@Override
	protected void postUI() {
		mAdapter.setViewBinder(mViewBinder);
	}

	@Override
	protected String[] getProjectionArray() {
		return FillupsTable.getFullProjectionArray();
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
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUPS_URI);
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

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			// switch (columnIndex) {
			// case 2:
			// // currency
			// tv.setText("$" + tv.getText());
			// return true;
			// case 3:
			// // economy
			// double economy = cursor.getDouble(columnIndex);
			// tv.setText(ECONOMY_FORMAT.format(economy));
			// return true;
			// }
			return false;
		}
	};
}
