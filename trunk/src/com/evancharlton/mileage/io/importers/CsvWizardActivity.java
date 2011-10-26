
package com.evancharlton.mileage.io.importers;

import com.evancharlton.mileage.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class CsvWizardActivity extends Activity implements View.OnClickListener {
    protected static final int REQUEST_NEXT = 0;
    protected static final int FINISH = 1;
    protected static final int PREVIOUS = 2;

    private Button mNextButton;
    private Button mPrevButton;
    protected LinearLayout mContainer;
    private TextView mHeaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wizard);

        mContainer = (LinearLayout) findViewById(R.id.container);

        mNextButton = (Button) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        mPrevButton = (Button) findViewById(R.id.previous);
        mPrevButton.setOnClickListener(this);
    }

    protected final Button getPreviousButton() {
        return mPrevButton;
    }

    protected final Button getNextButton() {
        return mNextButton;
    }

    protected final void setHeaderText(int resId) {
        if (mHeaderText == null) {
            mHeaderText = (TextView) findViewById(android.R.id.text1);
        }
        mHeaderText.setText(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                Intent intent = getIntent();
                if (!buildIntent(intent)) {
                    finish();
                } else {
                    startActivityForResult(intent, REQUEST_NEXT);
                }
                break;
            case R.id.previous:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_NEXT:
                if (resultCode == FINISH) {
                    setResult(resultCode);
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract boolean buildIntent(Intent intent);
}
