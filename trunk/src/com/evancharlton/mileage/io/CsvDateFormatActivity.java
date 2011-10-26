
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.DateFormatAdapter;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.io.importers.CsvWizardActivity;
import com.evancharlton.mileage.tasks.CsvDateReaderTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CsvDateFormatActivity extends CsvWizardActivity {
    public static final String DATE_FORMAT = "date_format";

    private CsvDateReaderTask mDateReaderTask;

    private TextView mRawDateView;
    private Spinner mFormats;
    private TextView mParsedDateView;

    private DateFormat mDateFormatter;
    private DateFormat mTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater.from(this).inflate(R.layout.import_csv_date, mContainer);

        mRawDateView = (TextView) findViewById(R.id.raw_date);
        mFormats = (Spinner) findViewById(R.id.date_formats);
        mParsedDateView = (TextView) findViewById(R.id.parsed_date);

        mDateFormatter = android.text.format.DateFormat.getDateFormat(this);
        mTimeFormatter = android.text.format.DateFormat.getTimeFormat(this);

        mFormats.setAdapter(new DateFormatAdapter(this));
        mFormats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                formatDate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        restoreTask();

        setHeaderText(R.string.import_csv_date_format);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mDateReaderTask;
    }

    private void restoreTask() {
        mDateReaderTask = (CsvDateReaderTask) getLastNonConfigurationInstance();

        if (mDateReaderTask == null) {
            mDateReaderTask = new CsvDateReaderTask(getIntent().getIntExtra(Fillup.DATE, 0));
        }
        mDateReaderTask.attach(this);
        if (mDateReaderTask.getStatus() == AsyncTask.Status.PENDING) {
            mDateReaderTask.execute(getIntent().getStringExtra(ImportActivity.FILENAME));
        }
    }

    public void setRawDate(String date) {
        mRawDateView.setText(date);

        formatDate();
    }

    private String getFormatPattern() {
        return (String) mFormats.getAdapter().getItem(mFormats.getSelectedItemPosition());
    }

    private void formatDate() {
        String format = getFormatPattern();
        SimpleDateFormat df = new SimpleDateFormat(format);

        String parsed;
        try {
            Date d = df.parse(mRawDateView.getText().toString());
            parsed = mDateFormatter.format(d) + " " + mTimeFormatter.format(d);
        } catch (ParseException e) {
            parsed = getString(R.string.error_could_not_parse_date);
        }
        mParsedDateView.setText(parsed);
    }

    @Override
    protected boolean buildIntent(Intent intent) {
        intent.setClass(this, CsvImportActivity.class);
        intent.putExtra(DATE_FORMAT, getFormatPattern());
        setResult(PREVIOUS);
        return true;
    }
}
