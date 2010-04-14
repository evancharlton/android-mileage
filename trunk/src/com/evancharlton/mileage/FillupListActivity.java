package com.evancharlton.mileage;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class FillupListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		ListView lv = getListView();
		String[] projection = FillupsTable.getFullProjectionArray();
		Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, FillupsTable.FILLUPS_URI);
		Cursor c = managedQuery(uri, projection, null, null, null);
		String[] from = new String[] {
				Fillup.ODOMETER,
				Fillup.VOLUME
		};
		int[] to = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to);
		lv.setAdapter(adapter);
	}
}
