
package com.evancharlton.mileage;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.tasks.FillupInfoTask;
import com.evancharlton.mileage.tasks.FillupInfoTask.Information;

public class FillupInfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FillupInfoActivity";

    private FillupInfoTask mInfoTask;

    private Fillup mFillup;

    private LinearLayout mInfoContainer;
    private LinearLayout mStatsContainer;
    private LinearLayout mFieldsContainer;

    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fillup_info);

        mInflater = LayoutInflater.from(this);

        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);

        mInfoContainer = (LinearLayout) findViewById(R.id.info);

        long id = getIntent().getLongExtra(Fillup._ID, -1);
        Uri uri = ContentUris.withAppendedId(FillupsTable.BASE_URI, id);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        mFillup = new Fillup(cursor);
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInfoTask = new FillupInfoTask(mFillup);
        mInfoTask.attach(this);
        mInfoTask.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:
                Intent intent = new Intent(this, FillupActivity.class);
                intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID,
                        getIntent().getLongExtra(Fillup._ID, -1));
                startActivity(intent);
                break;

            case R.id.close:
                finish();
                break;
        }
    }

    public void addInformation(Information update) {
        ViewGroup container = null;
        switch (update.key) {
            case R.string.column_unit_price:
                container = mInfoContainer;
                break;
        }
        View statistic = mInflater.inflate(R.layout.statistic, container);
        ((TextView) statistic.findViewById(android.R.id.text1)).setText(update.key);
        ((TextView) statistic.findViewById(android.R.id.text2)).setText(update.value);
    }
}
