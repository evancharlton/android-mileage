package com.evancharlton.mileage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

public class Mileage extends Activity {

	public static final int DATE_DIALOG_ID = 0;

	public static final int MENU_HISTORY = Menu.FIRST;
	public static final int MENU_GRAPHS = Menu.FIRST + 1;

	private int m_year;
	private int m_month;
	private int m_day;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Calendar c = Calendar.getInstance();
		m_year = c.get(Calendar.YEAR);
		m_month = c.get(Calendar.MONTH);
		m_day = c.get(Calendar.DAY_OF_MONTH);

		setContentView(R.layout.main);

		initData();
		initHandlers();
		updateDate();
	}

	private void initData() {
		Spinner vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);
		ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(this, R.array.vehicles, android.R.layout.simple_spinner_item);
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vehicleSpinner.setAdapter(vehicleAdapter);
	}

	private void initHandlers() {
		Button addFillupBtn = (Button) findViewById(R.id.add_fillup_btn);
		addFillupBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save the new fill-up
			}
		});

		Button customDateBtn = (Button) findViewById(R.id.change_date_btn);
		customDateBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// show a date picker
				showDialog(DATE_DIALOG_ID);
			}
		});
	}

	private void updateDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd");

		StringBuilder changedText = new StringBuilder();
		changedText.append(sdf.format(new Date(m_year, m_month, m_day)));
		changedText.append(" (").append(getString(R.string.custom_date)).append(")");

		Button customDateBtn = (Button) findViewById(R.id.change_date_btn);
		customDateBtn.setText(changedText.toString());
	}

	private void showHistory() {
	}

	private void showGraphs() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_HISTORY, 0, R.string.fillup_history).setShortcut('1', 'h');
		menu.add(Menu.NONE, MENU_GRAPHS, 0, R.string.graphs).setShortcut('2', 'g');

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_HISTORY:
			showHistory();
			break;
		case MENU_GRAPHS:
			showGraphs();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, m_dateSetListener, m_year, m_month, m_day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener m_dateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int month, int day) {
			m_year = year;
			m_month = month;
			m_day = day;
			updateDate();
		}
	};
}