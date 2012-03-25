
package com.evancharlton.mileage.tasks;

import com.evancharlton.mileage.FillupInfoActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.tasks.FillupInfoTask.DataHolder;

public class FillupInfoTask extends AttachableAsyncTask<FillupInfoActivity, Void, DataHolder, Void> {
    private final Fillup mFillup;

    public FillupInfoTask(Fillup fillup) {
        mFillup = fillup;
    }

    @Override
    protected void onProgressUpdate(DataHolder... update) {
        for (DataHolder info : update) {
            getParent().addInformation(info);
        }
    }

    private void publish(int key, double data) {
        publishProgress(new DataHolder(key, data));
    }

    @Override
    protected Void doInBackground(Void... params) {
        Fillup prev = mFillup.loadPrevious(getParent());
        mFillup.setPrevious(prev);

        // do the easy stuff first
        publish(R.string.column_partial, mFillup.isPartial() ? 1D : 0D);
        publish(R.string.column_volume, mFillup.getVolume());
        publish(R.string.column_odometer, mFillup.getOdometer());
        publish(R.string.column_total_cost, mFillup.getTotalCost());
        publish(R.string.column_unit_price, mFillup.getUnitPrice());

        // some of the statistics
        if (mFillup.hasPrevious()) {
            publish(R.string.info_distance, mFillup.getDistance());
            publish(R.string.info_economy, mFillup.getEconomy());
        }

        return null;
    }

    public static final class DataHolder {
        public final int key;

        public final double data;

        public DataHolder(int key, double data) {
            this.key = key;
            this.data = data;
        }
    }
}
