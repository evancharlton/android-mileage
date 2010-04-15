package com.evancharlton.mileage;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public abstract class BaseListActivity extends ListActivity implements AdapterView.OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
	}

	@Override
	protected void onResume() {
		super.onResume();

		ListView lv = getListView();
		Cursor c = managedQuery(getUri(), getProjectionArray(), null, null, null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, getListLayout(), c, getFrom(), getTo());
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	abstract protected Uri getUri();

	abstract protected String[] getProjectionArray();

	abstract protected String[] getFrom();

	protected int[] getTo() {
		return new int[] {
				android.R.id.text1,
				android.R.id.text2
		};
	}

	protected int getListLayout() {
		return android.R.layout.simple_list_item_2;
	}

	protected void loadItem(long id, Class<? extends Activity> cls) {
		Intent intent = new Intent(this, cls);
		intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, id);
		startActivity(intent);
	}

	@Override
	abstract public void onItemClick(AdapterView<?> list, View row, int position, long id);
}
