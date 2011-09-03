
package com.evancharlton.mileage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DateFormatAdapter extends BaseAdapter {
    private static final String[] FORMATS = new String[] {
            "YYYY-MM-DD HH:MM:SS",
            "YYYY-MM-DD HH:MM",
            "YYYY-MM-DD",
            "YYYY/MM/DD HH:MM:SS",
            "YYYY/MM/DD HH:MM",
            "YYYY/MM/DD",
            "MM-DD-YYYY HH:MM:SS",
            "MM-DD-YYYY HH:MM",
            "MM-DD-YYYY",
            "MM/DD/YYYY HH:MM:SS",
            "MM/DD/YYYY HH:MM",
            "MM/DD/YYYY",
            "DD-MM-YYYY HH:MM:SS",
            "DD-MM-YYYY HH:MM",
            "DD-MM-YYYY",
            "DD/MM/YYYY HH:MM:SS",
            "DD/MM/YYYY HH:MM",
            "DD/MM/YYYY"
    };

    private static final String[] FORMAT_STRINGS = new String[] {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd",
            "MM-dd-yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm",
            "MM-dd-yyyy",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy"
    };

    private Context mContext;

    public DateFormatAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return FORMATS.length;
    }

    @Override
    public Object getItem(int position) {
        return FORMAT_STRINGS[position];
    }

    @Override
    public long getItemId(int position) {
        return FORMAT_STRINGS[position].hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getView(android.R.layout.simple_spinner_item, position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(android.R.layout.simple_spinner_dropdown_item, position, convertView, parent);
    }

    private View getView(int resource, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resource, parent, false);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder(convertView);
        }

        holder.text1.setText(FORMATS[position]);

        return convertView;
    }

    private static class ViewHolder {
        public final TextView text1;

        public ViewHolder(View v) {
            text1 = (TextView) v.findViewById(android.R.id.text1);
            v.setTag(this);
        }
    }
}
