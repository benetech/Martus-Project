package org.martus.android.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author roms
 *         Date: 1/17/13
 */
public class IndeterminateProgressDialog extends DialogFragment {

    private Activity myActivity;

    public interface IndeterminateProgressDialogListener {
        String getIndeterminateDialogMessage();
    }

	public static IndeterminateProgressDialog newInstance() {
        return new IndeterminateProgressDialog ();
	}

    public IndeterminateProgressDialog() {
        // Empty constructor required for DialogFragment
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        myActivity = getActivity();
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(((IndeterminateProgressDialogListener) getActivity()).getIndeterminateDialogMessage());
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new BackButtonIgnorer());
		return dialog;
	}

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        myActivity.finish();
    }

}
