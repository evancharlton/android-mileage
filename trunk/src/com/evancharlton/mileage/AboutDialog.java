package com.evancharlton.mileage;

import android.app.Dialog;
import android.content.Context;
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
		String title = getContext().getString(R.string.app_name) + " : " + getContext().getString(R.string.app_version);
		setTitle(title);
	}

	public static AboutDialog create(Context context) {
		AboutDialog dlg = new AboutDialog(context);
		dlg.show();
		return dlg;
	}
}