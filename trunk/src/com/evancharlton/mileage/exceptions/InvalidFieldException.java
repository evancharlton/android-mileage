
package com.evancharlton.mileage.exceptions;

import android.widget.TextView;

public class InvalidFieldException extends Exception {
    private static final long serialVersionUID = -4593391149874475407L;

    private final int mErrorMessage;

    private final TextView mField;

    public InvalidFieldException(int errorMessage) {
        this(null, errorMessage);
    }

    public InvalidFieldException(TextView field, int errorMessage) {
        mField = field;
        mErrorMessage = errorMessage;
    }

    public TextView getField() {
        return mField;
    }

    public int getErrorMessage() {
        return mErrorMessage;
    }
}
