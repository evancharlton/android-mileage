
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.ExportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.Settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseExportActivity extends Activity {
    private ProgressBar mProgressBar;

    private TextView mLog;

    private ExportTask mExportTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export_progress);

        final String filename = getIntent().getStringExtra(ExportActivity.FILENAME);
        setTitle(getString(R.string.exporting, filename));

        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mLog = (TextView) findViewById(R.id.log);

        mProgressBar.setIndeterminate(true);

        mExportTask = (ExportTask) getLastNonConfigurationInstance();
        if (mExportTask == null) {
            mExportTask = createExportTask();
        }
        mExportTask.attach(this);

        if (mExportTask.getStatus() == AsyncTask.Status.PENDING) {
            String dbPath = getDatabasePath(FillUpsProvider.DATABASE_NAME).getAbsolutePath();
            mExportTask.execute(dbPath, filename);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mExportTask;
    }

    public void update(Update update) {
        if (update.message != null && update.message.length() > 0) {
            mLog.append(update.message + "\n");
        }
        if (update.progress > 0) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(update.progress);
        }
        if (update.max > 0) {
            mProgressBar.setMax(update.max);
        }
    }

    public void completed(String message) {
        update(new Update(message, mProgressBar.getMax()));
    }

    abstract protected ExportTask createExportTask();

    protected static abstract class ExportTask extends AsyncTask<String, Update, String> {
        protected BaseExportActivity mActivity = null;

        public final void attach(BaseExportActivity activity) {
            mActivity = activity;
        }

        @Override
        protected final void onPreExecute() {
            mActivity.update(new Update(mActivity.getTitle().toString(), 0));
        }

        @Override
        protected final String doInBackground(String... params) {
            final String inputFile = params[0];
            final String outputFile = params[1];
            return performExport(inputFile, Settings.EXTERNAL_DIR + outputFile.trim());
        }

        @Override
        protected final void onProgressUpdate(Update... updates) {
            mActivity.update(updates[0]);
        }

        @Override
        protected final void onPostExecute(String result) {
            final String msg;
            if (result != null) {
                msg =
                        mActivity.getString(R.string.exported,
                                result.substring(result.lastIndexOf('/') + 1));
            } else {
                msg = mActivity.getString(R.string.export_error);
            }
            mActivity.completed(msg);
        }

        abstract public String performExport(final String inputFile, final String outputFile);
    }

    protected static final class Update {
        public final String message;

        public final int progress;

        public final int max;

        public Update(String message, int progress) {
            this(message, progress, 100);
        }

        public Update(int progress, int max) {
            this(null, progress, max);
        }

        public Update(int progress) {
            this(null, progress, 0);
        }

        private Update(String message, int progress, int max) {
            this.message = message;
            this.progress = progress;
            this.max = max;
        }
    }
}
