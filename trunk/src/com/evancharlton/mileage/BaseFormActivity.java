
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.exceptions.InvalidFieldException;
import com.evancharlton.mileage.provider.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseFormActivity extends Activity {
    public static final String EXTRA_ITEM_ID = "dao_item_id";

    protected SharedPreferences mPreferences;

    private Button mSaveBtn;

    protected void onCreate(Bundle savedInstanceState, int layoutResId) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_form);
        LinearLayout stub = (LinearLayout) findViewById(R.id.contents);
        LayoutInflater.from(this).inflate(layoutResId, stub);
        mPreferences = getSharedPreferences(Settings.NAME, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initUI();

        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mSaveBtn.setText(getString(getCreateString()));
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setFields();
                    if (getDao().save(BaseFormActivity.this)) {
                        if (postSaveValidation()) {
                            saved();
                        }
                    }
                } catch (InvalidFieldException e) {
                    handleInvalidField(e);
                }
            }
        });

        Intent intent = getIntent();
        Long id = intent.getLongExtra(EXTRA_ITEM_ID, getDao().getId());
        if (id != null && id != getDao().getId()) {
            Uri uri = getUri(id);
            Cursor cursor = managedQuery(uri, getProjectionArray(), null, null, null);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                getDao().load(cursor);
                populateUI();
                mSaveBtn.setText(R.string.save_changes);
            }
        }
    }

    protected void handleInvalidField(InvalidFieldException e) {
        TextView field = e.getField();
        if (field == null) {
            Toast.makeText(BaseFormActivity.this, getString(e.getErrorMessage()), Toast.LENGTH_LONG)
                    .show();
        } else {
            field.setError(getString(e.getErrorMessage()));
            field.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getDao().isExistingObject() && canDelete()) {
            menu.add(Menu.NONE, R.string.delete, Menu.NONE, R.string.delete).setIcon(
                    R.drawable.ic_menu_delete);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.delete:
                showDialog(R.string.delete);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case R.string.delete:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_delete)
                        .setMessage(R.string.dialog_message_delete)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeDialog(id);
                                        if (getDao().delete(BaseFormActivity.this)) {
                                            deleted();
                                        }
                                    }
                                })
                        .setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeDialog(id);
                                    }
                                }).create();
        }
        return super.onCreateDialog(id);
    }

    protected void deleted() {
        finish();
    }

    protected boolean postSaveValidation() {
        return true;
    }

    protected void saved() {
        finish();
    }

    protected boolean canDelete() {
        return true;
    }

    abstract protected int getCreateString();

    abstract protected Dao getDao();

    abstract protected void initUI();

    abstract protected void populateUI();

    abstract protected void setFields() throws InvalidFieldException;

    abstract protected String[] getProjectionArray();

    abstract protected Uri getUri(long id);
}
