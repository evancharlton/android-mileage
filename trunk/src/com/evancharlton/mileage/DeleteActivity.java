package com.evancharlton.mileage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class DeleteActivity extends MileageActivity implements AdapterView.OnItemClickListener {
	protected static final int DELETE_DIALOG_ID = 100;
	private ListView mListView;

	@Override
	protected void onResume() {
		super.onResume();
		ListView lv = getListView();
		if (lv != null) {
			lv.setOnItemClickListener(this);
		}
	}

	protected DialogInterface.OnClickListener m_deleteListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			removeDialog(DELETE_DIALOG_ID);
			if (which == Dialog.BUTTON1) {
				delete();
			}
		}
	};

	protected void delete() {
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DELETE_DIALOG_ID:
				return new AlertDialog.Builder(this).setMessage(R.string.confirm_delete).setPositiveButton(android.R.string.yes, m_deleteListener).setNegativeButton(android.R.string.no, m_deleteListener).setCancelable(false).create();
		}
		return super.onCreateDialog(id);
	}

	public ListView getListView() {
		if (mListView == null) {
			mListView = (ListView) findViewById(android.R.id.list);
		}
		return mListView;
	}

	public void setListAdapter(ListAdapter adapter) {
		ListView lv = getListView();
		if (lv != null) {
			lv.setAdapter(adapter);
		}
	}

	public ListAdapter getListAdapter() {
		ListView lv = getListView();
		if (lv != null) {
			return lv.getAdapter();
		}
		return null;
	}

	protected void onListItemClick(ListView lv, View v, int position, long id) {
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		onListItemClick(getListView(), row, position, id);
	}
}
