package com.evancharlton.mileage;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class HistoryView extends ListActivity {

	public static final String TAG = "HistoryList";

	private static final String[] PROJECTIONS = new String[] {
			FillUps._ID, FillUps.AMOUNT, FillUps.COST, FillUps.DATE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(FillUps.CONTENT_URI);
		}

		getListView().setOnCreateContextMenuListener(this);

		Cursor c = managedQuery(intent.getData(), PROJECTIONS, null, null, FillUps.DEFAULT_SORT_ORDER);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.history, c, new String[] {
			FillUps.AMOUNT
		}, new int[] {
			android.R.id.text1
		});
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(HistoryView.this, FillUpView.class);
		startActivity(intent);
	}
}
