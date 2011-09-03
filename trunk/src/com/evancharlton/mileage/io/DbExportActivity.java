
package com.evancharlton.mileage.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DbExportActivity extends BaseExportActivity {
    @Override
    protected ExportTask createExportTask() {
        return new DbExportTask();
    }

    private static final class DbExportTask extends ExportTask {
        @Override
        public String performExport(String inputFile, String outputFile) {
            try {
                FileChannel input = new FileInputStream(inputFile).getChannel();
                FileChannel output = new FileOutputStream(outputFile).getChannel();
                input.transferTo(0, input.size(), output);
                input.close();
                output.close();
                return outputFile;
            } catch (IOException e) {
            }
            return null;
        }
    }
}
