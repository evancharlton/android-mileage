
package com.evancharlton.mileage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class DividerView extends TextView {

    public DividerView(Context context) {
        this(context, null, 0);
    }

    public DividerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DividerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, android.R.attr.listSeparatorTextViewStyle);
        setGravity(Gravity.CENTER_HORIZONTAL);
        int color = context.getResources().getColor(android.R.color.primary_text_dark);
        setTextColor(color);
        setId(android.R.id.text1);
    }
}
