
package com.evancharlton.mileage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class FieldView extends EditText {
    private long mFieldId = 0;

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFieldId(long id) {
        mFieldId = id;
    }

    public long getFieldId() {
        return mFieldId;
    }

    public String getKey() {
        return "fieldView_" + getFieldId();
    }
}
