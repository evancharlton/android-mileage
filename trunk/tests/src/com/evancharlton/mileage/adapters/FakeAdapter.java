package com.evancharlton.mileage.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FakeAdapter extends BaseAdapter {
	private int mCount;
	private Object mItem;
	private long mItemId;

	public void setCount(int count) {
		mCount = count;
	}

	public void setItem(Object item) {
		mItem = item;
	}

	public void setItemId(long itemId) {
		mItemId = itemId;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public Object getItem(int position) {
		return mItem;
	}

	@Override
	public long getItemId(int position) {
		return mItemId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
}
