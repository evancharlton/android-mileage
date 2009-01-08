package com.evancharlton.mileage;

import android.os.Bundle;

public interface Persistent {
	public void saveState(Bundle outState);

	public void restoreState(Bundle inState);
}
