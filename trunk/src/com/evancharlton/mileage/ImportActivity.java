
package com.evancharlton.mileage;

import com.evancharlton.mileage.io.CsvColumnMappingActivity;
import com.evancharlton.mileage.io.DbImportActivity;
import com.evancharlton.mileage.provider.Settings;
import com.evancharlton.mileage.util.Debugger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";

    public static final String FILENAME = "filename";

    public static final String WIPE_DATA = "wipe data";

    private static final int DIALOG_NO_FILES = 1;

    private static final String[] FILE_TYPES = new String[] {
            ".db", ".csv"
    };

    private static final Class<?>[] IMPORTERS = new Class[] {
            DbImportActivity.class, CsvColumnMappingActivity.class
    };

    private FileLoader mFileLoader;

    private Spinner mFileTypes;

    private Spinner mInputFile;

    private Button mSubmitButton;

    private FileAdapter mFileAdapter;

    private CheckBox mWipeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.import_form);

        mFileAdapter = new FileAdapter(this);

        mFileTypes = (Spinner) findViewById(R.id.importer);
        mInputFile = (Spinner) findViewById(R.id.files);
        mWipeData = (CheckBox) findViewById(R.id.erase_database);
        mSubmitButton = (Button) findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(ImportActivity.this, IMPORTERS[mFileTypes
                                .getSelectedItemPosition()]);
                intent.putExtra(ImportActivity.FILENAME, getFilename());
                intent.putExtra(ImportActivity.WIPE_DATA, mWipeData.isChecked());
                startActivity(intent);
                finish();
            }
        });

        mInputFile.setAdapter(mFileAdapter);

        mFileLoader = (FileLoader) getLastNonConfigurationInstance();

        mFileTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (mFileLoader == null) {
                    mFileLoader = new FileLoader();
                }
                mFileLoader.attach(ImportActivity.this);
                if (mFileLoader.getStatus() == AsyncTask.Status.PENDING) {
                    mFileLoader.execute();
                }

                mWipeData.setChecked(position == 0);
                mWipeData.setEnabled(position != 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_NO_FILES:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_missing_files)
                        .setMessage(R.string.dialog_message_missing_files)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeDialog(id);
                                    }
                                }).create();
            default:
                return super.onCreateDialog(id);
        }
    }

    private FilenameFilter getFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                final String extension = FILE_TYPES[mFileTypes.getSelectedItemPosition()];
                return filename.toLowerCase().endsWith(extension);
            }
        };
    }

    private void setFiles(String[] filenames) {
        mFileAdapter.setData(filenames);
        mFileLoader = null;
        mSubmitButton.setEnabled(!mFileAdapter.isEmpty());

        if (filenames.length == 0) {
            showDialog(DIALOG_NO_FILES);
        }
    }

    private String getFilename() {
        return mFileAdapter.getItem(mInputFile.getSelectedItemPosition()).toString();
    }

    private static class FileLoader extends AsyncTask<Void, Void, String[]> {
        private ImportActivity mActivity;

        public void attach(ImportActivity activity) {
            mActivity = activity;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            return new File(Settings.EXTERNAL_DIR).list(mActivity.getFilter());
        }

        @Override
        protected void onPostExecute(String[] files) {
            if (files == null) {
                Log.d(TAG, "No files found!");
                files = new String[0];
            }
            mActivity.setFiles(files);
        }
    }

    private static class FileAdapter extends BaseAdapter implements SpinnerAdapter {
        private final LayoutInflater mInflater;

        private String[] values = new String[0];

        public FileAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public Object getItem(int position) {
            return values[position];
        }

        @Override
        public long getItemId(int position) {
            return values[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView tv = (TextView) convertView.getTag();
            if (tv == null) {
                tv = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(tv);
            }
            tv.setText(values[position]);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,
                                false);
            }

            CheckedTextView ctv = (CheckedTextView) convertView.getTag();
            if (ctv == null) {
                ctv = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(ctv);
            }
            ctv.setText(values[position]);

            return convertView;
        }

        public void setData(String[] data) {
            Debugger.ensureOnUiThread(mInflater.getContext(), "Altering data off UI thread!");
            values = data;
            notifyDataSetChanged();
        }
    }
}
