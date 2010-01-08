package com.evancharlton.mileage;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.Button;

public class AboutDialog extends Dialog {
	public AboutDialog(Context context) {
		super(context);
		setContentView(R.layout.about);

		Button close = (Button) findViewById(R.id.close_about_btn);
		close.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
		String title;
		try {
			title = getContext().getString(R.string.app_name) + " : " + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
		} catch (NameNotFoundException e) {
			title = getContext().getString(R.string.unknown_version);
		}
		setTitle(title);
	}

	public static AboutDialog create(Context context) {
		AboutDialog dlg = new AboutDialog(context);
		dlg.show();
		return dlg;
	}
}