package com.evancharlton.mileage;

import android.net.Uri;

import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

public class ServiceIntervalsListActivity extends BaseListActivity {
	@Override
	protected String[] getFrom() {
		return new String[] {
				ServiceInterval.TITLE,
				ServiceInterval.DESCRIPTION
		};
	}

	@Override
	protected Uri getUri() {
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalsTable.SERVICE_INTERVALS_URI);
	}

	@Override
	public void onItemClick(long id) {
		loadItem(id, ServiceIntervalActivity.class);
	}
}
