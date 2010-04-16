package com.evancharlton.mileage.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.SpinnerCursorAdapter;
import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class CursorSpinner extends Spinner {
	private final Cursor mCursor;
	private final SpinnerCursorAdapter mAdapter;

	public CursorSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CursorSpinner);
		final String displayField = arr.getString(R.styleable.CursorSpinner_display_field);
		final String uriPath = arr.getString(R.styleable.CursorSpinner_uri);

		Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, uriPath);
		Log.d("CursorSpinner", uri.toString());
		mCursor = getContext().getContentResolver().query(uri, new String[] {
				Dao._ID,
				displayField
		}, null, null, null);
		mAdapter = new SpinnerCursorAdapter(getContext(), mCursor, displayField);
		setAdapter(mAdapter);

		final boolean autoHide = arr.getBoolean(R.styleable.CursorSpinner_auto_hide, true);
		if (autoHide && mCursor.getCount() == 1) {
			setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mCursor.close();
	}

	public void setSelectedId(long id) {
		final SpinnerCursorAdapter adapter = mAdapter;
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			if (adapter.getItemId(i) == id) {
				setSelection(i);
				break;
			}
		}
	}
}
