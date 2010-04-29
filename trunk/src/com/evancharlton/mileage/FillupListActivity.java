package com.evancharlton.mileage;

import java.text.DecimalFormat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.evancharlton.mileage.dao.Fillup;
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
		return FillupsTable.PROJECTION;
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

	private final ViewBinder mViewBinder = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			switch (columnIndex) {
				case 2:
					// currency
					return false;
				case 3:
					// economy
					return false;
			}
			return false;
		}
	};
}
