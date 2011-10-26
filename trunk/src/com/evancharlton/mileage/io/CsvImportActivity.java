
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.io.importers.CsvWizardActivity;
import com.evancharlton.mileage.tasks.CsvImportTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CsvImportActivity extends CsvWizardActivity {
    public static final String TOTAL_ROWS = "total_rows";
    private ProgressBar mProgress;
    private TextView mLog;
    private CsvImportTask mCsvImportTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.import_csv_progress, mContainer);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mLog = (TextView) view.findViewById(R.id.log);

        restoreTask();

        mProgress.setMax(getIntent().getIntExtra(TOTAL_ROWS, 100));

        getPreviousButton().setEnabled(false);
        getNextButton().setText(R.string.done);
        getNextButton().setEnabled(false);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mCsvImportTask;
    }

    private void restoreTask() {
        mCsvImportTask = (CsvImportTask) getLastNonConfigurationInstance();

        if (mCsvImportTask == null) {
            mCsvImportTask = new CsvImportTask();
        }
        mCsvImportTask.attach(this);
        if (mCsvImportTask.getStatus() == AsyncTask.Status.PENDING) {
            mCsvImportTask.execute(getIntent().getExtras());
        }
    }

    public void update(int update) {
        mProgress.setIndeterminate(update == 0);
        mProgress.setProgress(update);

        if (update % 10 == 0) {
            mLog.append(getString(R.string.update_read_rows, update, mProgress.getMax()) + "\n");
        }
    }

    public void error(int error) {
        mLog.append(getString(error) + "\n");
        getNextButton().setEnabled(true);
    }

    public void completed(int numRecords) {
        mLog.append(getString(R.string.update_imported, numRecords) + "\n");
        getNextButton().setEnabled(true);
    }

    @Override
    protected boolean buildIntent(Intent intent) {
        setResult(FINISH);
        return false;
    }
}
