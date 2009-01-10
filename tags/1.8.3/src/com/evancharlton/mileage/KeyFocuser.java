package com.evancharlton.mileage;

import java.util.ArrayList;
import java.util.List;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;

public class KeyFocuser implements KeyListener {
	private View m_next;
	private List<Integer> m_codes = new ArrayList<Integer>();

	public KeyFocuser(View next) {
		this(next, new int[] {
				KeyEvent.KEYCODE_SHIFT_LEFT,
				KeyEvent.KEYCODE_SHIFT_RIGHT
		});
	}

	public KeyFocuser(View next, int code) {
		this(next, new int[] {
			code
		});
	}

	public KeyFocuser(View next, int[] codes) {
		for (int i = 0; i < codes.length; i++) {
			m_codes.add(codes[i]);
		}
		m_next = next;
	}

	public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
		return false;
	}

	public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
		if (m_codes.contains(keyCode)) {
			m_next.requestFocus();
			return true;
		}
		return false;
	}
}