package com.evancharlton.mileage.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Spinner;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.SpinnerCursorAdapter;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class CursorSpinner extends Spinner {
	public CursorSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CursorSpinner);
		final String displayField = arr.getString(R.styleable.CursorSpinner_display_field);
		final String uriPath = arr.getString(R.styleable.CursorSpinner_uri);

		Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, uriPath);
		Log.d("CursorSpinner", uri.toString());
		Cursor cursor = getContext().getContentResolver().query(uri, new String[] {
				Dao._ID,
				displayField
		}, null, null, null);
		SpinnerCursorAdapter adapter = new SpinnerCursorAdapter(getContext(), cursor, displayField);
		setAdapter(adapter);

		if (cursor.getCount() == 1) {
			// setVisibility(View.GONE);
		}
	}

	public void setSelectedId(long id) {
		Adapter adapter = getAdapter();
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			if (adapter.getItemId(i) == id) {
				setSelection(i);
				break;
			}
		}
	}
}
