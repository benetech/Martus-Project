package org.martus.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.martus.android.R;

/**
 * @author roms
 *         Date: 12/22/12
 */
public class LoginDialog extends DialogFragment implements TextView.OnEditorActionListener {

    private EditText passwordText;

    public interface LoginDialogListener {
        void onFinishPasswordDialog(TextView inputText);
        void onCancelPasswordDialog();
    }

    public LoginDialog() {
        // Empty constructor required for DialogFragment
    }

    public static LoginDialog newInstance() {
        LoginDialog frag = new LoginDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater factory = LayoutInflater.from(getActivity());
        View passwordEntryView = factory.inflate(R.layout.password_dialog, null);
        passwordText = (EditText) passwordEntryView.findViewById(R.id.password_edit);
        passwordText.setOnEditorActionListener(this);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setView(passwordEntryView);

        final AlertDialog alertDialog1 = alertDialog.create();
        Button okButton= (Button) passwordEntryView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new OkButtonClickHandler());

        Button cancelButton = (Button) passwordEntryView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new CancelButtonClickHandler());

        return alertDialog1;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            ((LoginDialogListener) getActivity()).onFinishPasswordDialog(passwordText);
            this.dismiss();
            return true;
        }
        return false;
    }

    private class CancelButtonClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            ((LoginDialogListener) getActivity()).onCancelPasswordDialog();
        }
    }

    public class OkButtonClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
                ((LoginDialogListener) getActivity()).onFinishPasswordDialog(passwordText);
                dismiss();
        }
    }
}
