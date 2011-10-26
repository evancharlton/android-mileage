
package com.evancharlton.mileage.views;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.SpinnerCursorAdapter;
import com.evancharlton.mileage.provider.FillUpsProvider;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Spinner;

public class CursorSpinner extends Spinner {
    private Cursor mCursor;
    private SpinnerCursorAdapter mAdapter;
    private final String mUriPath;
    private final String mDisplayField;
    private final boolean mAutoHide;

    public CursorSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CursorSpinner);
        mDisplayField = arr.getString(R.styleable.CursorSpinner_display_field);
        mUriPath = arr.getString(R.styleable.CursorSpinner_uri);
        mAutoHide = arr.getBoolean(R.styleable.CursorSpinner_auto_hide, true);

        filter(null, null);
    }

    public void filter(String selection, String[] selectionArgs) {
        // TODO(3.2) - run this on a background thread
        Uri uri = Uri.withAppendedPath(FillUpsProvider.BASE_URI, mUriPath);
        mCursor = getContext().getContentResolver().query(uri, new String[] {
                BaseColumns._ID,
                mDisplayField
        }, selection, selectionArgs, null);
        if (mAdapter == null) {
            mAdapter = new SpinnerCursorAdapter(getContext(), mCursor, mDisplayField);
        } else {
            mAdapter.changeCursor(mCursor);
            mAdapter.notifyDataSetChanged();
        }
        setAdapter(mAdapter);

        if (mAutoHide && mCursor.getCount() == 1) {
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
