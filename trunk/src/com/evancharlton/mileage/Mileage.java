
package com.evancharlton.mileage;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Mileage extends TabActivity {
    public static final String VISIBLE_TAB = "visible_tab";

    public static final String TAG_FILLUP = "fillups";
    public static final String TAG_HISTORY = "history";
    public static final String TAG_STATISTICS = "statistics";
    public static final String TAG_VEHICLES = "vehicles";

    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);

        mTabHost = getTabHost();
        mTabHost.addTab(createTabSpec(TAG_FILLUP, FillupActivity.class, R.string.fillup,
                R.drawable.ic_tab_fillup));
        mTabHost.addTab(createTabSpec(TAG_HISTORY, FillupListActivity.class, R.string.history,
                R.drawable.ic_tab_history));
        mTabHost.addTab(createTabSpec(TAG_STATISTICS, VehicleStatisticsActivity.class,
                R.string.statistics, R.drawable.ic_tab_statistics));
        mTabHost.addTab(createTabSpec(TAG_VEHICLES, VehicleListActivity.class, R.string.vehicles,
                R.drawable.ic_tab_vehicles));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // hide the virtual keyboard when switching tabs
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTabHost.getApplicationWindowToken(), 0);
            }
        });

        String requestedTab = getIntent().getStringExtra(VISIBLE_TAB);
        if (requestedTab != null) {
            switchTo(requestedTab);
        }
    }

    public void switchToHistoryTab() {
        switchTo(TAG_HISTORY);
    }

    public void switchTo(String tag) {
        mTabHost.setCurrentTabByTag(tag);
    }

    private TabSpec createTabSpec(String tag, Class<? extends Activity> cls, int string, int icon) {
        TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setContent(new Intent(this, cls));
        spec.setIndicator(getString(string), getResources().getDrawable(icon));
        return spec;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        add(menu, R.string.service_intervals, ServiceIntervalsListActivity.class).setIcon(
                R.drawable.ic_menu_intervals);
        add(menu, R.string.import_export, ImportExportActivity.class)
                .setIcon(R.drawable.ic_menu_ie);
        add(menu, R.string.settings, SettingsActivity.class)
                .setIcon(R.drawable.ic_menu_preferences);
        return super.onCreateOptionsMenu(menu);
    }

    private final MenuItem add(final Menu menu, final int string,
            final Class<? extends Activity> cls) {
        return menu.add(Menu.NONE, string, Menu.NONE, string).setIntent(new Intent(this, cls));
    }
}
