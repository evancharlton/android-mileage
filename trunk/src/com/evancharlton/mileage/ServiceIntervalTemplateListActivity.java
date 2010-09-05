package com.evancharlton.mileage;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

public class ServiceIntervalTemplateListActivity extends BaseListActivity {
	private static final int MENU_CREATE = 1;

	@Override
	protected String[] getFrom() {
		return new String[] {
				ServiceIntervalTemplate.TITLE,
				ServiceIntervalTemplate.DESCRIPTION
		};
	}

	@Override
	protected Uri getUri() {
		return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalTemplatesTable.URI);
	}

	@Override
	public void onItemClick(long id) {
		loadItem(id, ServiceIntervalTemplateActivity.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_service_interval_template);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_CREATE:
				startActivity(new Intent(this, ServiceIntervalTemplateActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean canDelete(int position) {
		return getAdapter().getCount() > 1;
	}
}
