
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.ServiceInterval;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalsTable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ServiceIntervalsListActivity extends BaseListActivity implements
        DialogInterface.OnClickListener, View.OnClickListener {
    private static final int MENU_CREATE = 1;
    private static final int MENU_TEMPLATES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        long id = intent.getLongExtra(ServiceInterval._ID, -1);
        if (id > 0) {
            showDialog(R.string.delete_service_interval);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        ServiceInterval interval = ServiceInterval.loadById(this,
                getIntent().getLongExtra(ServiceInterval._ID, -1));
        Vehicle vehicle = Vehicle.loadById(this, interval.getVehicleId());
        switch (id) {
            case R.string.delete_service_interval:
                return new AlertDialog.Builder(this)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .setNeutralButton(R.string.remind_later, this)
                        .setTitle(R.string.delete_service_interval)
                        .setMessage(
                                getString(R.string.service_interval_reminder_message,
                                        interval.getTitle(), interval.getDescription(),
                                        vehicle.getTitle())).create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected String[] getFrom() {
        return new String[] {
                ServiceInterval.TITLE,
                ServiceInterval.DESCRIPTION
        };
    }

    @Override
    protected Uri getUri() {
        return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalsTable.URI);
    }

    @Override
    public void onItemClick(long id) {
        loadItem(id, ServiceIntervalActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_service_interval).setIcon(
                R.drawable.ic_menu_add);
        menu.add(Menu.NONE, MENU_TEMPLATES, Menu.NONE, R.string.service_interval_templates)
                .setIcon(R.drawable.ic_menu_edit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CREATE:
                startActivity(new Intent(this, ServiceIntervalActivity.class));
                return true;
            case MENU_TEMPLATES:
                startActivity(new Intent(this, ServiceIntervalTemplateListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ServiceInterval interval = ServiceInterval.loadById(this,
                getIntent().getLongExtra(ServiceInterval._ID, -1));
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // delete the interval
                interval.delete(this);
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                // remind tomorrow
                interval.deleteAlarm(this);
                interval.scheduleAlarm(this, System.currentTimeMillis() + Calculator.DAY_MILLIS);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                // no action
        }
        dialog.dismiss();
    }

    @Override
    protected void setupEmptyView() {
        mEmptyView.removeAllViews();
        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_service_intervals,
                mEmptyView);
        emptyView.findViewById(R.id.empty_add_interval).setOnClickListener(this);
        emptyView.findViewById(R.id.empty_edit_templates).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_add_interval:
                startActivity(new Intent(ServiceIntervalsListActivity.this,
                        ServiceIntervalActivity.class));
                break;
            case R.id.empty_edit_templates:
                startActivity(new Intent(this, ServiceIntervalTemplateListActivity.class));
                break;
        }
    }
}
