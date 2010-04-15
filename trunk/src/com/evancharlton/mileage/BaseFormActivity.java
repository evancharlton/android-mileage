package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Dao.InvalidFieldException;

public abstract class BaseFormActivity extends Activity {
	public static final String EXTRA_ITEM_ID = "dao_item_id";

	private Button mSaveBtn;

	protected void onCreate(Bundle savedInstanceState, int layoutResId) {
		super.onCreate(savedInstanceState);
		setContentView(layoutResId);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initUI();

		mSaveBtn = (Button) findViewById(R.id.save_btn);
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
					Toast.makeText(BaseFormActivity.this, getString(e.getErrorMessage()), Toast.LENGTH_LONG).show();
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

	protected boolean postSaveValidation() {
		return true;
	}

	protected void saved() {
		finish();
	}

	abstract protected Dao getDao();

	abstract protected void initUI();

	abstract protected void populateUI();

	abstract protected void setFields();

	abstract protected String[] getProjectionArray();

	abstract protected Uri getUri(long id);
}
