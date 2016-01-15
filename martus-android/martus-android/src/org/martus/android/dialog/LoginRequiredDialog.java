package org.martus.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.martus.android.R;
import org.martus.android.library.common.dialog.BackButtonIgnorer;

/**
 * @author roms
 *         Date: 12/23/12
 */
public class LoginRequiredDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public interface LoginRequiredDialogListener {
        void onFinishLoginRequiredDialog();
    }

    public LoginRequiredDialog() {
        // Empty constructor required for DialogFragment
    }

    public static LoginRequiredDialog newInstance() {
        LoginRequiredDialog frag = new LoginRequiredDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.must_login_first_title)
            .setMessage(R.string.must_login_first_message)
            .setCancelable(false)
            .setOnKeyListener(new BackButtonIgnorer())
            .setPositiveButton(R.string.alert_dialog_ok,this)
            .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        ((LoginRequiredDialogListener) getActivity()).onFinishLoginRequiredDialog();
    }
}
