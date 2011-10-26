
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.ServiceIntervalTemplate;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.ServiceIntervalTemplatesTable;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ServiceIntervalTemplateListActivity extends BaseListActivity implements
        View.OnClickListener {
    private static final int MENU_CREATE = 1;

    @Override
    protected String[] getFrom() {
        return new String[] {
                ServiceIntervalTemplate.TITLE,
                ServiceIntervalTemplate.DESCRIPTION
        };
    }

    @Override
    protected Uri getUri() {
        return Uri.withAppendedPath(FillUpsProvider.BASE_URI, ServiceIntervalTemplatesTable.URI);
    }

    @Override
    public void onItemClick(long id) {
        loadItem(id, ServiceIntervalTemplateActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.add_service_interval_template)
                .setIcon(R.drawable.ic_menu_add);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CREATE:
                startActivity(new Intent(this, ServiceIntervalTemplateActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupEmptyView() {
        mEmptyView.removeAllViews();
        View emptyView = LayoutInflater.from(this).inflate(
                R.layout.empty_service_interval_templates, mEmptyView);
        emptyView.findViewById(R.id.empty_add_default_templates).setOnClickListener(this);
        emptyView.findViewById(R.id.empty_add_interval_template).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_add_interval_template:
                startActivity(new Intent(ServiceIntervalTemplateListActivity.this,
                        ServiceIntervalTemplateActivity.class));
                break;
            case R.id.empty_add_default_templates:
                final ContentResolver resolver = getContentResolver();
                new Thread() {
                    @Override
                    public void run() {
                        resolver.bulkInsert(ServiceIntervalTemplatesTable.BASE_URI,
                                ServiceIntervalTemplatesTable.TEMPLATES);
                    }
                }.start();
                break;
        }
    }
}
