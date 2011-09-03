
package com.evancharlton.mileage.tasks;

import android.os.AsyncTask;

public abstract class AttachableAsyncTask<Parent, Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {
    private Parent mParent;

    public void attach(Parent parent) {
        mParent = parent;
    }

    protected final Parent getParent() {
        return mParent;
    }
}
