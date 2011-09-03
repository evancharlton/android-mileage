
package com.evancharlton.mileage.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class SpinnerCursorAdapter extends SimpleCursorAdapter {
    public SpinnerCursorAdapter(Context context, Cursor c, String label) {
        super(context, android.R.layout.simple_spinner_item, c, new String[] {
                label
        }, new int[] {
                android.R.id.text1
        });
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
