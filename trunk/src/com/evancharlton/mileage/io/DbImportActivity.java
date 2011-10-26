
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.tasks.DbImportTask;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DbImportActivity extends Activity {
    private DbImportTask mTask;

    private TextView mLog;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_csv_progress);

        mLog = (TextView) findViewById(R.id.log);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        restoreTask();
    }

    private void restoreTask() {
        mTask = (DbImportTask) getLastNonConfigurationInstance();

        if (mTask == null) {
            mTask = new DbImportTask(getIntent().getStringExtra(ImportActivity.FILENAME));
        }
        mTask.attach(this);

        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        }
    }

    public void log(String msg) {
        mLog.append(msg + "\n");
    }

    public void setWorking(boolean isWorking) {
        mProgressBar.setIndeterminate(isWorking);
        if (!isWorking) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
