package org.martus.android.library.common.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by animal@martus.org on 10/1/14.
 */
public class ProgressDialogHandler {

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public ProgressDialogHandler(Context context) {
        mContext = context;
    }

    public void showProgressDialog(String title) {
        initializeProgressDialog();
        getProgressDialog().setTitle(title);
        getProgressDialog().setIndeterminate(true);
        getProgressDialog().setCanceledOnTouchOutside(false);
        getProgressDialog().show();
    }

    public void dismissProgressDialog(){
        getProgressDialog().dismiss();
    }

    public boolean isShowing() {
        if (getProgressDialog() == null)
            return false;

        return getProgressDialog().isShowing();
    }

    private void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext());
    }

    private ProgressDialog getProgressDialog() {
        return mProgressDialog;
    }

    private Context getContext() {
        return mContext;
    }
}
