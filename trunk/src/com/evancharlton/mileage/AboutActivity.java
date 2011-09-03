
package com.evancharlton.mileage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aicharts:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
                        .parse("http://www.artfulbits.com/products/android/aicharts.aspx")));
                break;
            case R.id.clutchpad:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
                        .parse("http://market.android.com/details?id=com.stevealbright.clutch")));
                break;
        }
    }
}
