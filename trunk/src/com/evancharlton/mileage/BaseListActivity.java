package com.evancharlton.mileage;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.evancharlton.mileage.dao.Dao;

public abstract class BaseListActivity extends ListActivity implements AdapterView.OnItemClickListener {
	protected ListView mListView;
	protected SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mListView = getListView();
		Uri uri = getUri();
		Log.d("BaseListActivity", uri.toString());
		Cursor c = managedQuery(uri, getProjectionArray(), null, null, null);
		mAdapter = new SimpleCursorAdapter(this, getListLayout(), c, getFrom(), getTo());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	protected String[] getProjectionArray() {
		final String[] from = getFrom();
		final int length = from.length;
		final String[] projection = new String[length + 1];
		projection[0] = Dao._ID;
		for (int i = 0; i < length; i++) {
			projection[1 + i] = from[i];
		}
		return projection;
	}

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
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		onItemClick(id);
	}

	abstract protected String[] getFrom();

	abstract protected Uri getUri();

	abstract public void onItemClick(long id);
}
