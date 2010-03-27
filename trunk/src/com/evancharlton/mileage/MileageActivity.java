package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

public abstract class MileageActivity extends Activity {
	private static final int MENU_HELP = 0xDEADBEEF;

	protected abstract String getTag();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_HELP, Menu.FIRST, R.string.help).setIcon(R.drawable.ic_menu_help);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HELP:
				String url = "http://evancharlton.com/projects/mileage/docs/" + getTag().toLowerCase();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
