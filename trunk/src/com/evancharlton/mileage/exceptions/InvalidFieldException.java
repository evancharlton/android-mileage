package com.evancharlton.mileage.exceptions;

public class InvalidFieldException extends Exception {
	private static final long serialVersionUID = -4593391149874475407L;

	private int mErrorMessage = 0;

	public InvalidFieldException(int errorMessage) {
		mErrorMessage = errorMessage;
	}

	public int getErrorMessage() {
		return mErrorMessage;
	}
}
