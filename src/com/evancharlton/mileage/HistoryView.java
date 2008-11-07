package com.evancharlton.mileage;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HistoryView extends ListActivity {
	public static final int MENU_IMPORT_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT = Menu.FIRST;
	public static final int MENU_EXPORT_DB = Menu.FIRST + 1;
	public static final int MENU_EXPORT_SQL = Menu.FIRST + 2;
	public static final int MENU_EXPORT_CSV = Menu.FIRST + 3;
	public static final int MENU_IMPORT = Menu.FIRST + 4;
	public static final int MENU_IMPORT_DB = Menu.FIRST + 5;
	public static final int MENU_IMPORT_SQL = Menu.FIRST + 6;
	public static final int MENU_IMPORT_CSV = Menu.FIRST + 7;
	public static final String TAG = "HistoryList";

	private static final String[] PROJECTIONS = new String[] {
			FillUps._ID,
			FillUps.AMOUNT,
			FillUps.COST,
			FillUps.DATE,
			FillUps.COMMENT
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
		String[] from = new String[] {
				FillUps.AMOUNT,
				FillUps.COST,
				FillUps.DATE,
				FillUps.COMMENT
		};
		int[] to = new int[] {
				R.id.history_amount,
				R.id.history_price,
				R.id.history_date,
				R.id.history_comment
		};

		Cursor historyCursor = managedQuery(FillUps.CONTENT_URI, PROJECTIONS, null, null, FillUps.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.history, historyCursor, from, to);
		adapter.setViewBinder(m_viewBinder);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		super.onListItemClick(lv, v, position, id);
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Intent intent = new Intent();
		intent.setData(uri);
		intent.setClass(HistoryView.this, FillUpView.class);
		startActivity(intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_IMPORT_EXPORT, Menu.NONE, R.string.import_export).setShortcut('1', 'i');
		HelpDialog.injectHelp(menu, 'h');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_IMPORT_EXPORT:
				Intent i = new Intent();
				i.setClass(HistoryView.this, ImportExportView.class);
				startActivity(i);
				break;
			case HelpDialog.MENU_HELP:
				HelpDialog.create(this, R.string.help_title_history, R.string.help_history);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private SimpleCursorAdapter.ViewBinder m_viewBinder = new SimpleCursorAdapter.ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (columnIndex == 1) {
				String val = cursor.getString(columnIndex) + " g";
				((TextView) view).setText(val);
				return true;
			} else if (columnIndex == 2) {
				String val = "$" + cursor.getString(columnIndex) + "/g";
				((TextView) view).setText(val);
				return true;
			} else if (columnIndex == 3) {
				long time = cursor.getLong(columnIndex);
				Date date = new Date(time);
				DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
				String text = format.format(date);
				((TextView) view).setText(text);
				return true;
			} else if (columnIndex == 4) {
				String val = cursor.getString(columnIndex);
				if (val == null || val.trim().length() == 0) {
					view.setVisibility(View.GONE);
					return true;
				}
			}
			return false;
		}
	};
}
