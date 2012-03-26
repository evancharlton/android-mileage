
package com.evancharlton.mileage;

import com.evancharlton.mileage.io.CsvExportActivity;
import com.evancharlton.mileage.io.DbExportActivity;
import com.evancharlton.mileage.provider.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class ExportActivity extends Activity {
    public static final String FILENAME = "filename";

    private static final String[] FILE_TYPES = new String[] {
            ".db", ".csv"
    };

    @SuppressWarnings("rawtypes")
    private static final Class[] EXPORTERS = new Class[] {
            DbExportActivity.class, CsvExportActivity.class
    };

    private Spinner mFileTypes;

    private EditText mFilename;

    private Button mSubmitButton;

    private TextView mFileExt;

    private FilenameTask mFilenameTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export_form);

        mFileTypes = (Spinner) findViewById(R.id.exporter);
        mFilename = (EditText) findViewById(R.id.output_file);
        mFileExt = (TextView) findViewById(R.id.file_extension);
        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(ExportActivity.this, EXPORTERS[mFileTypes
                                .getSelectedItemPosition()]);
                intent.putExtra(ExportActivity.FILENAME, getFilename());
                startActivity(intent);
                finish();
            }
        });

        mFileTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mFileExt.setText(getExtension());
                startFilenameTask(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mFilenameTask = (FilenameTask) getLastNonConfigurationInstance();
        startFilenameTask(false);
    }

    private void startFilenameTask(boolean cancel) {
        if (cancel && mFilenameTask != null) {
            mFilenameTask.cancel(true);
            mFilenameTask = null;
        }
        if (mFilenameTask == null) {
            mFilenameTask = new FilenameTask();
        }
        mFilenameTask.attach(this);
        if (mFilenameTask.getStatus() == AsyncTask.Status.PENDING) {
            mFilenameTask.execute();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mFilenameTask;
    }

    private final String getExtension() {
        return FILE_TYPES[mFileTypes.getSelectedItemPosition()];
    }

    private String getFilename() {
        return mFilename.getText() + getExtension();
    }

    protected static final class FilenameTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "FilenameTask";

        private static final String BASE_NAME = "mileage-export";

        private ExportActivity mActivity;

        public void attach(ExportActivity activity) {
            mActivity = activity;
        }

        @Override
        protected String doInBackground(Void... args) {
            // Make sure that we have somewhere to put the file
            File destDir = new File(Settings.EXTERNAL_DIR);
            if (!destDir.exists()) {
                Log.d(TAG, "Creating export destination");
                destDir.mkdirs();
            }

            int i = 0;
            while (true) {
                if (isCancelled()) {
                    return null;
                }
                String abs = getAbsoluteFilename(i);
                if (new File(abs).exists() == false) {
                    return getBasename(i);
                }
                i++;
            }
        }

        private String getAbsoluteFilename(int i) {
            return Settings.EXTERNAL_DIR + getBasename(i) + mActivity.getExtension();
        }

        private String getBasename(int i) {
            return BASE_NAME + (i > 0 ? "." + i : "");
        }

        @Override
        protected void onPostExecute(String filename) {
            if (filename == null) {
                return;
            }
            mActivity.mFilename.setText(filename);
        }
    }
}
