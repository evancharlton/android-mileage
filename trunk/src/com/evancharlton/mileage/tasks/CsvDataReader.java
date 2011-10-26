
package com.evancharlton.mileage.tasks;

import com.evancharlton.mileage.io.CsvColumnMappingActivity;
import com.evancharlton.mileage.provider.Settings;

import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvDataReader extends
        AttachableAsyncTask<CsvColumnMappingActivity, String, String[], Void> {
    @Override
    protected Void doInBackground(String... inputFiles) {
        final String inputFile = inputFiles[0];
        final String absoluteInputFile = Settings.EXTERNAL_DIR + inputFile;
        BufferedReader reader = null;
        CSVReader csvReader = null;
        try {
            reader = new BufferedReader(new FileReader(absoluteInputFile));
            csvReader = new CSVReader(reader);
            // Note: it's assumed that the first row is the column names.
            publishProgress(csvReader.readNext());
        } catch (IOException e) {
            Log.e("ColumnReaderTask", "Could not get columns!", e);
        }
        try {
            if (reader != null) {
                reader.close();
            }
            if (csvReader != null) {
                csvReader.close();
            }
        } catch (IOException e) {
            // nothing to do
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String[]... rows) {
        // getParent().addRow(rows[0]);
    }

    @Override
    protected void onPostExecute(Void v) {

    }
}
