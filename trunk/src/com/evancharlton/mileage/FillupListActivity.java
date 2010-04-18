package com.evancharlton.mileage;

import android.net.Uri;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class FillupListActivity extends BaseListActivity {
	@Override
	protected String[] getFrom() {
		return new String[] {
				Fillup.DATE,
				Fillup.VOLUME,
				Fillup.VOLUME
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
				android.R.id.text2,
				R.id.price
		};
	}

	@Override
	protected int getListLayout() {
		return R.layout.fillup_list_item;
	}
}
