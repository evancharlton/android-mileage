package com.evancharlton.mileage;

import android.app.Dialog;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpDialog extends Dialog {
	private Context m_context;
	public static final int MENU_GROUP = 10;
	public static final int MENU_HELP = 11;

	public HelpDialog(Context context) {
		super(context);
		m_context = context;
		setContentView(R.layout.help);

		Button closeBtn = (Button) findViewById(R.id.close_help_btn);
		Button aboutBtn = (Button) findViewById(R.id.about_app_btn);

		closeBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		aboutBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AboutDialog.create(m_context);
			}
		});
	}

	public void setContents(int[] strings) {
		LinearLayout container = (LinearLayout) findViewById(R.id.help_container);
		for (int i : strings) {
			TextView text = new TextView(m_context);
			text.setText(i);
			text.setPadding(5, 10, 5, 10);
			container.addView(text);
		}
	}

	public static HelpDialog create(Context context, int title, int content) {
		return HelpDialog.create(context, title, new int[] {
			content
		});
	}

	public static HelpDialog create(Context context, int title, int[] contents) {
		HelpDialog dlg = new HelpDialog(context);
		dlg.setTitle(title);
		dlg.setContents(contents);
		dlg.show();
		return dlg;
	}

	public static boolean injectHelp(Menu menu, char letter) {
		int count = menu.size();
		count++;
		if (count >= 10) {
			return false;
		}
		count += 48; // convert to ASCII
		return HelpDialog.injectHelp(menu, (char) count, letter);
	}

	public static boolean injectHelp(Menu menu, char number, char letter) {
		menu.add(Menu.NONE, MENU_HELP, MENU_GROUP, R.string.help).setShortcut(number, letter).setIcon(R.drawable.ic_menu_help);
		return true;
	}
}
