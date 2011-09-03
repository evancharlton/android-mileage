
package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

public class ImportExportActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.import_export);

        map(R.id.import_button, ImportActivity.class);
        map(R.id.export_button, ExportActivity.class);

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) {
            findViewById(R.id.import_button).setEnabled(false);
            findViewById(R.id.export_button).setEnabled(false);

            Toast.makeText(this, getString(R.string.error_media_unmounted), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private final void map(final int id, final Class<? extends Activity> cls) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImportExportActivity.this, cls));
            }
        });
    }
}
