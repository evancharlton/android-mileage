
package com.evancharlton.mileage;

import com.artfulbits.aiCharts.ChartView;
import com.artfulbits.aiCharts.Base.ChartArea;
import com.artfulbits.aiCharts.Base.ChartSeries;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ZoomControls;

public abstract class ChartActivity extends Activity implements DialogInterface.OnCancelListener {
    public static final String VEHICLE_ID = "vehicle_id";

    private static final int PROGRESS_DIALOG = 1;

    private ChartView mChart;
    private ZoomControls mZoomControls;
    private ChartGenerator mChartGenerator;
    private ProgressDialog mProgressDialog;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        mChart = (ChartView) findViewById(R.id.chart);
        mZoomControls = (ZoomControls) findViewById(R.id.zoom_controls);

        restoreLastNonConfigurationInstance();

        mChart.setPanning(ChartView.PANNING_HORIZONTAL);

        mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                zoom(0.5);
            }
        });

        mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                zoom(2);
            }
        });
    }

    protected void zoom(double factor) {
        getChart().getAreas().get(0).getDefaultXAxis().getScale().mulZoom(factor);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return new Object[] {
                mChartGenerator,
                serializeData()
        };
    }

    private void restoreLastNonConfigurationInstance() {
        Object saved = getLastNonConfigurationInstance();
        if (saved != null) {
            Object[] array = (Object[]) saved;
            mChartGenerator = (ChartGenerator) array[0];
            unserializeData(array[1]);
        } else {
            mChartGenerator = createChartGenerator();
        }
        mChartGenerator.attach(this);
        if (mChartGenerator.getStatus() == AsyncTask.Status.PENDING) {
            mChartGenerator.execute(getExecuteParameters());
        }
    }

    protected abstract Object serializeData();

    protected abstract void unserializeData(Object saved);

    protected final ChartView getChart() {
        return mChart;
    }

    protected abstract ChartGenerator createChartGenerator();

    protected Object[] getExecuteParameters() {
        return null;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                if (mProgressDialog != null) {
                    removeDialog(PROGRESS_DIALOG);
                }
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setTitle(R.string.creating_chart);
                mProgressDialog.setOnCancelListener(this);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                return mProgressDialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mChartGenerator != null && mChartGenerator.getStatus() == AsyncTask.Status.RUNNING) {
            mChartGenerator.cancel(true);
        }
        finish();
    }

    protected ProgressDialog getProgressDialog() {
        return mProgressDialog;
    }

    protected void addChartSeries(ChartSeries series) {
        ChartArea area = new ChartArea();
        area.getDefaultXAxis().setFormat(DateFormat.getDateFormat(this));
        mChart.getSeries().add(series);
        mChart.getAreas().add(area);
    }

    public abstract static class ChartGenerator extends AsyncTask<Object, Integer, ChartSeries[]> {
        private ChartActivity mActivity;
        private ProgressDialog mCachedProgressDialog;

        public final void attach(ChartActivity activity) {
            mActivity = activity;
            mCachedProgressDialog = mActivity.getProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            mActivity.showDialog(PROGRESS_DIALOG);
            mCachedProgressDialog = mActivity.getProgressDialog();
        }

        @Override
        protected void onProgressUpdate(Integer... updates) {
            mCachedProgressDialog.setProgress(updates[0]);
            if (updates.length > 1) {
                mCachedProgressDialog.setMax(updates[1]);
            }
        }

        @Override
        protected void onPostExecute(ChartSeries[] series) {
            if (isCancelled()) {
                return;
            }
            mActivity.removeDialog(PROGRESS_DIALOG);
            final int length = series.length;
            for (int i = 0; i < length; i++) {
                mActivity.addChartSeries(series[i]);
            }
        }

        protected final ChartActivity getActivity() {
            return mActivity;
        }
    }
}
