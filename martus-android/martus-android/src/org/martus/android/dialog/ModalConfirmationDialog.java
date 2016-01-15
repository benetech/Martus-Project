package org.martus.android.dialog;

import org.martus.android.R;
import org.martus.android.library.common.dialog.BackButtonIgnorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

/**
 * @author roms
 *         Date: 2/26/13
 */
public class ModalConfirmationDialog extends ConfirmationDialog {

    public static ModalConfirmationDialog newInstance() {
        ModalConfirmationDialog frag = new ModalConfirmationDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(((ConfirmationDialogListener) getActivity()).getConfirmationTitle())
                .setMessage(((ConfirmationDialogListener) getActivity()).getConfirmationMessage())
                .setPositiveButton(R.string.yes, new PositiveButtonHandler())
                .setNegativeButton(R.string.no, new NegativeButtonHandler())
                .create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new BackButtonIgnorer());
        if (!((ConfirmationDialogListener) getActivity()).getConfirmationMessage().isEmpty()) {
            dialog.setMessage(((ConfirmationDialogListener) getActivity()).getConfirmationMessage());
        }
        return dialog;
    }

}
