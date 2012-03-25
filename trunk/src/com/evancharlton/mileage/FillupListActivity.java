
package com.evancharlton.mileage;

import com.evancharlton.mileage.adapters.FillupAdapter;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.tables.FillupsTable;
import com.evancharlton.mileage.services.RecalculateEconomyService;
import com.evancharlton.mileage.views.CursorSpinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class FillupListActivity extends Activity {
    private static final String TAG = "FillupListActivity";

    private CursorSpinner mVehicles;

    private Vehicle mVehicle;

    private FillupAdapter mAdapter;

    private ListView mList;

    private final BroadcastReceiver mCalculationFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.calculationFinished(intent.getDoubleExtra(
                    RecalculateEconomyService.EXTRA_AVERAGE_ECONOMY, 0D));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fillup_list);

        initUI();
    }

    @Override
    protected void onResume() {
        mAdapter.requery();
        super.onResume();
        registerReceiver(mCalculationFinishedReceiver, new IntentFilter(
                RecalculateEconomyService.CALCULATION_FINISHED));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mCalculationFinishedReceiver);
        super.onPause();
    }

    protected void initUI() {
        mVehicles = (CursorSpinner) findViewById(R.id.vehicle);
        mVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> list, View row, int position, long id) {
                mVehicle = getVehicle();

                // Change the adapter's vehicle
                mAdapter.setVehicle(mVehicle);

                // Start a new task
                calculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mVehicle = getVehicle();

        mAdapter = new FillupAdapter(this, getVehicle());
        mList = (ListView) findViewById(android.R.id.list);
        mList.setAdapter(mAdapter);
        registerForContextMenu(mList);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> row, View view, int position, long id) {
                openFillup(id);
            }
        });

        calculate();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, R.string.edit, Menu.NONE, R.string.edit);
        menu.add(Menu.NONE, R.string.delete, Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.string.edit:
                editFillup(info.id);
                return true;
            case R.string.delete:
                showDeleteDialog(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openFillup(long id) {
        Intent intent = new Intent(this, FillupInfoActivity.class);
        intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, id);
        startActivity(intent);
    }

    private void editFillup(long id) {
        Intent intent = new Intent(this, FillupActivity.class);
        intent.putExtra(BaseFormActivity.EXTRA_ITEM_ID, id);
        startActivity(intent);
    }

    private void showDeleteDialog(final long id) {
        showDeleteDialog(new Runnable() {
            @Override
            public void run() {
                Uri uri = ContentUris.withAppendedId(FillupsTable.BASE_URI, id);
                getContentResolver().delete(uri, null, null);
            }
        });
    }

    protected void showDeleteDialog(final Runnable deleteAction) {
        // TODO(3.1) - This dialog doesn't persist through rotations.
        Dialog deleteDialog =
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_delete)
                        .setMessage(R.string.dialog_message_delete)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteAction.run();
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
        deleteDialog.show();
    }

    private void calculate() {
        RecalculateEconomyService.run(this, mVehicle);
    }

    protected final Vehicle getVehicle() {
        Vehicle vehicle = Vehicle.loadById(this, mVehicles.getSelectedItemId());
        if (vehicle == null) {
            Log.e(TAG, "Unable to load vehicle #" + mVehicles.getSelectedItemId());
            throw new IllegalStateException("Unable to load vehicle #"
                    + mVehicles.getSelectedItemId());
        }
        return vehicle;
    }
}
