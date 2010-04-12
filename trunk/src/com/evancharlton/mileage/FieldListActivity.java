package com.evancharlton.mileage;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.provider.tables.FieldsTable;

public class FieldListActivity extends ListActivity {

	private static final int MENU_ADD_FIELD = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
	}

	@Override
	protected void onResume() {
		super.onResume();

		ListView lv = getListView();
		Cursor c = getContentResolver().query(Dao.createUri(FieldsTable.FIELDS_URI), FieldsTable.getFullProjectionArray(), null, null, null);
		String[] from = new String[] {
				Field.TITLE,
				Field.DESCRIPTION
		};
		int[] to = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View row, int position, long id) {
				Intent intent = new Intent(FieldListActivity.this, FieldActivity.class);
				intent.putExtra(FieldActivity.EXTRA_FIELD_ID, id);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ADD_FIELD, Menu.FIRST, R.string.add_field);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_FIELD:
				Intent intent = new Intent(this, FieldActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
