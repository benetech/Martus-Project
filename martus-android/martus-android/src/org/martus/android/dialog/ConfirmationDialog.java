package org.martus.android.dialog;

import org.martus.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author roms
 *         Date: 12/23/12
 */
public class ConfirmationDialog extends DialogFragment {

    public interface ConfirmationDialogListener {
        void onConfirmationAccepted();
        void onConfirmationCancelled();
        String getConfirmationTitle();
        String getConfirmationMessage();
    }

    public ConfirmationDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ConfirmationDialog newInstance() {
        ConfirmationDialog frag = new ConfirmationDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(((ConfirmationDialogListener) getActivity()).getConfirmationTitle())
                .setPositiveButton(R.string.yes, new PositiveButtonHandler())
                .setNegativeButton(R.string.no, new NegativeButtonHandler())
                .create();
        if (!((ConfirmationDialogListener) getActivity()).getConfirmationMessage().isEmpty()) {
            dialog.setMessage(((ConfirmationDialogListener) getActivity()).getConfirmationMessage());
        }
        return dialog;
    }

    protected class PositiveButtonHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int whichButton) {
            ((ConfirmationDialogListener) getActivity()).onConfirmationAccepted();
        }
    }

    protected class NegativeButtonHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int whichButton) {
            ((ConfirmationDialogListener) getActivity()).onConfirmationCancelled();
        }
    }
}
