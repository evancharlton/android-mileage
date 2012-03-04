
package com.evancharlton.mileage.views;

import com.evancharlton.mileage.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public abstract class DeltaView extends LinearLayout {
    private EditText mValue;

    private Spinner mUnits;

    public DeltaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOrientation(HORIZONTAL);
        super.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.delta, this);

        mValue = (EditText) findViewById(R.id.value);
        mUnits = (Spinner) findViewById(R.id.units);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.DeltaView);
        mValue.setHint(arr.getString(R.styleable.DeltaView_hint));

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                        android.R.id.text1, getTexts());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnits.setAdapter(adapter);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle icicle = new Bundle();
        icicle.putParcelable("super", super.onSaveInstanceState());
        icicle.putString("value", mValue.getText().toString());
        icicle.putInt("units", mUnits.getSelectedItemPosition());
        return icicle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle saved = (Bundle) state;
        super.onRestoreInstanceState(saved.getParcelable("super"));
        mValue.setText(saved.getString("value"));
        mUnits.setSelection(saved.getInt("units"));
    }

    public final long getDelta() {
        if (mValue.getText().length() > 0) {
            long value = Long.parseLong(mValue.getText().toString());
            return value * getValues()[mUnits.getSelectedItemPosition()];
        }
        return 0;
    }

    public final void setDelta(long delta) {
        final int position = getPosition(delta);
        final long multiplier = getValues()[position];
        mValue.setText(String.valueOf(delta / multiplier));
        mUnits.setSelection(position);
    }

    public final TextView getEditField() {
        return mValue;
    }

    abstract protected int getPosition(long delta);

    abstract protected String[] getTexts();

    abstract protected long[] getValues();
}
