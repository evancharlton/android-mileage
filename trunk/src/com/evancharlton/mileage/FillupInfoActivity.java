
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.Statistic;
import com.evancharlton.mileage.provider.Statistics.CostStatistic;
import com.evancharlton.mileage.provider.Statistics.DistanceStatistic;
import com.evancharlton.mileage.provider.Statistics.EconomyStatistic;
import com.evancharlton.mileage.provider.Statistics.FuelStatistic;
import com.evancharlton.mileage.provider.Statistics.PriceStatistic;
import com.evancharlton.mileage.provider.StatisticsGroup;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.tasks.FillupInfoTask;
import com.evancharlton.mileage.tasks.FillupInfoTask.DataHolder;
import com.evancharlton.mileage.views.DividerView;
import com.evancharlton.mileage.views.FormattedDateView;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FillupInfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FillupInfoActivity";

    private static final Statistic[] INFO = {
            new PartialStatistic(R.string.column_partial),
            new FuelStatistic(R.string.column_volume),
            new DistanceStatistic(R.string.column_odometer),
            new PriceStatistic(R.string.column_unit_price),
            new CostStatistic(R.string.column_total_cost)
    };

    private static final Statistic[] STATS = {
            new DistanceStatistic(R.string.info_distance),
            new EconomyStatistic(R.string.info_economy)
    };

    private static final StatisticsGroup[] GROUPS = {
            new StatisticsGroup(R.string.divider_fillup_info, INFO),
            new StatisticsGroup(R.string.divider_fillup_statistics, STATS)
    };

    private final SparseArray<Holder> mLayouts = new SparseArray<Holder>();

    private FillupInfoTask mInfoTask;

    private Fillup mFillup;

    private Vehicle mVehicle;

    private LayoutInflater mInflater;

    private LinearLayout mStatContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fillup_info);

        mInflater = LayoutInflater.from(this);

        Button previous = (Button) findViewById(R.id.previous);
        Button next = (Button) findViewById(R.id.next);

        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);

        mStatContainer = (LinearLayout) findViewById(R.id.stat_container);

        long id = getIntent().getLongExtra(BaseFormActivity.EXTRA_ITEM_ID, -1);
        Uri uri = ContentUris.withAppendedId(FillupsTable.BASE_URI, id);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        mFillup = new Fillup(cursor);
        cursor.close();

        mVehicle = Vehicle.loadById(this, mFillup.getVehicleId());

        for (StatisticsGroup group : GROUPS) {
            DividerView divider =
                    (DividerView) mInflater.inflate(R.layout.divider, mStatContainer, false);
            mStatContainer.addView(divider);
            divider.setText(group.getLabel());

            for (Statistic stat : group.getStatistics()) {
                ViewGroup layout =
                        (ViewGroup) mInflater.inflate(R.layout.statistic, mStatContainer, false);
                mStatContainer.addView(layout);

                mLayouts.append(stat.getLabel(), new Holder(layout, stat));

                ((TextView) layout.findViewById(android.R.id.text1)).setText(stat.getLabel(this,
                        mVehicle));
            }
        }

        mFillup.setPrevious(mFillup.loadPrevious(this));
        mFillup.setNext(mFillup.loadNext(this));

        previous.setEnabled(mFillup.hasPrevious());
        next.setEnabled(mFillup.hasNext());

        FormattedDateView header = (FormattedDateView) findViewById(R.id.header);
        header.setText(mFillup.getTimestamp());

        setTitle(getString(R.string.title_fillup, header.getText()));
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
        Intent intent;
        switch (v.getId()) {
            case R.id.edit:
                intent = new Intent(this, FillupActivity.class);
                intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID,
                        getIntent().getLongExtra(BaseFormActivity.EXTRA_ITEM_ID, -1));
                startActivity(intent);
                break;

            case R.id.previous:
                intent = new Intent(this, FillupInfoActivity.class);
                intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, mFillup.getPrevious().getId());
                startActivity(intent);
                finish();
                Overrider.get(this).overridePendingTransition(R.anim.slide_in_right,
                        R.anim.slide_out_left);
                break;

            case R.id.next:
                intent = new Intent(this, FillupInfoActivity.class);
                intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, mFillup.getNext().getId());
                startActivity(intent);
                finish();
                Overrider.get(this).overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                break;
        }
    }

    public void addInformation(DataHolder update) {
        Holder holder = mLayouts.get(update.key);
        if (holder == null) return;

        ViewGroup view = holder.view;
        TextView stat = (TextView) view.findViewById(android.R.id.text2);

        stat.setText(holder.statistic.format(this, mVehicle, update.data));
    }

    private static final class Holder {
        public final ViewGroup view;

        public final Statistic statistic;

        public Holder(ViewGroup view, Statistic statistic) {
            this.view = view;
            this.statistic = statistic;
        }
    }

    private static final class PartialStatistic extends Statistic {
        public PartialStatistic(int label) {
            super(label);
        }

        @Override
        public String format(Context context, Vehicle vehicle, double value) {
            return String.format("%b", value > 0);
        }
    }

    private static class Overrider {
        public static Overrider get(Activity activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                return new RealOverrider(activity);
            }
            return new Overrider(activity);
        }

        protected final Activity mActivity;

        public Overrider(Activity activity) {
            mActivity = activity;
        }

        public void overridePendingTransition(int in, int out) {
        }

        private static class RealOverrider extends Overrider {
            public RealOverrider(Activity activity) {
                super(activity);
            }

            @Override
            public void overridePendingTransition(int in, int out) {
                mActivity.overridePendingTransition(in, out);
            }
        }
    }
}
