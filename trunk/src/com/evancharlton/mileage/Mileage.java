package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class Mileage extends Activity {
	private static final int MENU_FIELDS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startActivity(new Intent(this, FillupActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_FIELDS, Menu.NONE, R.string.edit_fields);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_FIELDS:
				startActivity(new Intent(this, FieldListActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}