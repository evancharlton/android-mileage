
package com.evancharlton.mileage.views;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.DatePicker;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;

public class DateButton extends Button {
    private long mTimestamp = System.currentTimeMillis();
    private final DateFormat mDateFormatter;
    private final Calendar mCalendar;
    private static StaticDatePickerDialog mDialog;

    private final OnDateSetListener mDateSetCallback = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDialog = null;
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDate(mCalendar.getTimeInMillis());
        }
    };

    public DateButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        super.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mDateFormatter = android.text.format.DateFormat.getDateFormat(context);
        mCalendar = Calendar.getInstance();

        setDate(System.currentTimeMillis());
    }

    private void showDialog() {
        mDialog = new StaticDatePickerDialog(getContext(), mDateSetCallback, mCalendar);
        mDialog.show();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle icicle = new Bundle();
        icicle.putParcelable("super", super.onSaveInstanceState());
        icicle.putLong("timestamp", getTimestamp());
        if (mDialog != null) {
            mDialog.dismiss();
        }
        return icicle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle icicle = (Bundle) state;
        super.onRestoreInstanceState(icicle.getParcelable("super"));
        setDate(icicle.getLong("timestamp", System.currentTimeMillis()));
        if (mDialog != null) {
            mDialog.setCallback(mDateSetCallback);
            try {
                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            } catch (BadTokenException e) {
                // silently fail
            }
        }
    }

    public void setDate(Date date) {
        mCalendar.setTime(date);
        mTimestamp = mCalendar.getTimeInMillis();
        setText(mDateFormatter.format(date));
    }

    public void setDate(long timestamp) {
        setDate(new Date(timestamp));
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public Date getDate() {
        return new Date(mTimestamp);
    }

    private static class StaticDatePickerDialog extends DatePickerDialog {
        private OnDateSetListener mCallback;

        public StaticDatePickerDialog(Context context, OnDateSetListener callback, Calendar calendar) {
            super(context, callback, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            mCallback = callback;
        }

        public void setCallback(OnDateSetListener callback) {
            mCallback = callback;
        }

        @Override
        public void onDateChanged(DatePicker picker, int year, int month, int day) {
            super.onDateChanged(picker, year, month, day);
            mCallback.onDateSet(picker, year, month, day);
        }
    }
}
