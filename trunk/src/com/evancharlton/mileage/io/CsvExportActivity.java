
package com.evancharlton.mileage.io;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.tables.FillupsTable;

import android.database.Cursor;
import android.net.Uri;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class CsvExportActivity extends BaseExportActivity {
    @Override
    protected ExportTask createExportTask() {
        return new CsvExportTask();
    }

    private static final class CsvExportTask extends ExportTask {
        @Override
        public String performExport(String inputFile, String outputFile) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                CSVWriter csvWriter = new CSVWriter(writer);

                // TODO(3.5) - export more than just fillup data
                Uri uri = FillupsTable.BASE_URI;
                Cursor fillups = mActivity.getContentResolver().query(uri,
                        FillupsTable.CSV_PROJECTION, null, null, null);
                final int FILLUP_COUNT = fillups.getCount();
                publishProgress(new Update(0, FILLUP_COUNT + 1));

                final int COLUMN_COUNT = FillupsTable.CSV_PROJECTION.length;
                String[] data = new String[COLUMN_COUNT];

                // write the column data first
                for (int i = 0; i < COLUMN_COUNT; i++) {
                    data[i] = mActivity.getString(FillupsTable.PLAINTEXT[i]);
                }
                csvWriter.writeNext(data);
                csvWriter.flush();
                publishProgress(new Update(mActivity.getString(R.string.update_wrote_headers), 1));

                // figure out what columns are where, for formatting purposes
                int COLUMN_DATE = -1;
                for (int i = 0; i < COLUMN_COUNT; i++) {
                    if (FillupsTable.CSV_PROJECTION[i].equals(Fillup.DATE)) {
                        COLUMN_DATE = i;
                    }
                }

                final DateFormat DATE_FORMAT = android.text.format.DateFormat
                        .getDateFormat(mActivity);

                // now write the real data
                int numWritten = 0;
                while (fillups.moveToNext()) {
                    for (int i = 0; i < COLUMN_COUNT; i++) {
                        if (i == COLUMN_DATE) {
                            data[i] = DATE_FORMAT.format(new Date(fillups.getLong(i)));
                        } else {
                            data[i] = fillups.getString(i);
                        }
                    }
                    csvWriter.writeNext(data);
                    if (++numWritten % 10 == 0) {
                        sendUpdate(numWritten, FILLUP_COUNT);
                        csvWriter.flush();
                    } else {
                        publishProgress(new Update(numWritten));
                    }
                }
                sendUpdate(numWritten, FILLUP_COUNT);
                csvWriter.flush();
                csvWriter.close();

                fillups.close();

                return outputFile;
            } catch (IOException e) {
            }
            return null;
        }

        private void sendUpdate(int num_written, final int total) {
            publishProgress(new Update(mActivity.getString(R.string.update_wrote_rows, num_written,
                    total), num_written));
        }
    }
}
