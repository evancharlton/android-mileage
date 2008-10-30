package com.evancharlton.mileage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
		ArrayList<String> history = new ArrayList<String>();

		c.moveToFirst();
		while (c.isAfterLast() == false) {
			String amt = round(c.getDouble(1), 2);
			String cost = round(c.getDouble(2), 2);
			Date d = new Date(c.getLong(3) * 1000);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			String date = df.format(d);
			String filler = " " + getString(R.string.history_description);
			history.add(date + ": " + amt + filler + cost);
			c.moveToNext();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.history, history.toArray(new String[history.size()]));
		setListAdapter(adapter);
	}

	private String round(double d, int precision) {
		d *= Math.pow(10, precision);
		d = Math.round(d);
		d /= Math.pow(10, precision);
		return String.valueOf(d);
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
