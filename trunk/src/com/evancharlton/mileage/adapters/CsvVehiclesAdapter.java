
package com.evancharlton.mileage.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CsvVehiclesAdapter implements SpinnerAdapter {
    private final ArrayList<String> mVehicles = new ArrayList<String>();
    private final LayoutInflater mInflater;

    public CsvVehiclesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void add(String vehicle) {
        if (!mVehicles.contains(vehicle)) {
            mVehicles.add(vehicle);
        }
    }

    public String getText(int position) {
        return mVehicles.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent,
                    false);
        }

        TextView text = (TextView) convertView.getTag();
        if (text == null) {
            text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(text);
        }
        text.setText(getText(position));

        return convertView;
    }

    @Override
    public int getCount() {
        return mVehicles.size();
    }

    @Override
    public Object getItem(int position) {
        return getText(position);
    }

    @Override
    public long getItemId(int position) {
        return getText(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView tv = (TextView) convertView.getTag();
        if (tv == null) {
            tv = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(tv);
        }
        tv.setText(getText(position));

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return mVehicles.size() == 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
}
