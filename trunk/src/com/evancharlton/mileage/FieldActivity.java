
package com.evancharlton.mileage;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Field;
import com.evancharlton.mileage.provider.FillUpsProvider;
import com.evancharlton.mileage.provider.tables.FieldsTable;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

public class FieldActivity extends BaseFormActivity {
    public static final String EXTRA_FIELD_ID = "field_id";

    private EditText mTitle;
    private EditText mDescription;
    private final Field mField = new Field(new ContentValues());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.field_form);
    }

    @Override
    protected Dao getDao() {
        return mField;
    }

    @Override
    protected String[] getProjectionArray() {
        return FieldsTable.PROJECTION;
    }

    @Override
    protected Uri getUri(long id) {
        return ContentUris.withAppendedId(
                Uri.withAppendedPath(FillUpsProvider.BASE_URI, FieldsTable.URI_PATH), id);
    }

    @Override
    protected void initUI() {
        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);
    }

    @Override
    protected void populateUI() {
        mTitle.setText(mField.getTitle());
        mDescription.setText(mField.getDescription());
    }

    @Override
    protected void setFields() {
        mField.setTitle(mTitle.getText().toString());
        mField.setDescription(mDescription.getText().toString());
    }

    @Override
    protected int getCreateString() {
        return R.string.add_field;
    }
}
