package com.evancharlton.mileage;

import android.net.Uri;

import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

public class ServiceIntervalTemplateListActivity extends BaseListActivity {
	@Override
	protected String[] getFrom() {
		return new String[] {
				ServiceIntervalTemplate.TITLE,
				ServiceIntervalTemplate.DESCRIPTION
		};
	}

	@Override
	protected Uri getUri() {
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalTemplatesTable.SERVICE_TEMPLATES_URI);
	}

	@Override
	public void onItemClick(long id) {
		loadItem(id, ServiceIntervalTemplateActivity.class);
	}
}
