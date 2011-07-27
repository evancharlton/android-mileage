package com.evancharlton.mileage.tasks;

import com.evancharlton.mileage.FillupInfoActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.tasks.FillupInfoTask.Information;

public class FillupInfoTask extends AttachableAsyncTask<FillupInfoActivity, Void, Information, Void> {
	private final Fillup mFillup;

	public FillupInfoTask(Fillup fillup) {
		mFillup = fillup;
	}

	@Override
	protected void onProgressUpdate(Information... update) {
		for (Information info : update) {
			getParent().addInformation(info);
		}
	}

	private void publish(int key, String data) {
		publishProgress(new Information(key, data));
	}

	@Override
	protected Void doInBackground(Void... params) {
		// do the easy stuff first
		publish(R.string.column_volume, String.valueOf(mFillup.getVolume()));
		publish(R.string.column_odometer, String.valueOf(mFillup.getOdometer()));
		publish(R.string.column_total_cost, String.valueOf(mFillup.getTotalCost()));
		publish(R.string.column_unit_price, String.valueOf(mFillup.getUnitPrice()));

		return null;
	}

	public static class Information {
		public final int key;
		public final String value;

		public Information(int key, String value) {
			this.key = key;
			this.value = value;
		}
	}
}
