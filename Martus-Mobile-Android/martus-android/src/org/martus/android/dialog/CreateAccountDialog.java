package org.martus.android.dialog;

import java.util.Arrays;

import org.martus.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author roms
 *         Date: 12/22/12
 */
public class CreateAccountDialog extends DialogFragment implements DialogInterface.OnClickListener, TextWatcher,
        TextView.OnEditorActionListener {

    public static final int MIN_PASSWORD_SIZE = 8;
    private EditText newPasswordText;
    private EditText confirmPasswordText;
    private TextView error;

    public interface CreateAccountDialogListener {
        void onFinishNewAccountDialog(TextView passwordText, TextView confirmPasswordText);
        void onCancelNewAccountDialog();
    }

    public CreateAccountDialog() {
        // Empty constructor required for DialogFragment
    }

    public static CreateAccountDialog newInstance() {
        CreateAccountDialog frag = new CreateAccountDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View createAccountDialog = factory.inflate(R.layout.create_account, null);
        newPasswordText = (EditText) createAccountDialog.findViewById(R.id.new_password_field);
        confirmPasswordText = (EditText) createAccountDialog.findViewById(R.id.confirm_password_field);
        confirmPasswordText.setOnEditorActionListener(this);
        error = (TextView) createAccountDialog.findViewById(R.id.password_problem_text);

        confirmPasswordText.addTextChangedListener(this);
	    newPasswordText.addTextChangedListener(this);

        return new AlertDialog.Builder(getActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.create_account_dialog_title)
            .setView(createAccountDialog)
            .setPositiveButton(R.string.alert_dialog_ok, this)
            .setNegativeButton(R.string.create_account_dialog_cancel,this)
            .create();
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        switch (whichButton) {
            case -1:    ((CreateAccountDialogListener) getActivity()).onFinishNewAccountDialog(newPasswordText, confirmPasswordText);
                        break;
            case -2:    ((CreateAccountDialogListener) getActivity()).onCancelNewAccountDialog();
                        break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            ((CreateAccountDialogListener) getActivity()).onFinishNewAccountDialog(newPasswordText, confirmPasswordText);
            this.dismiss();
            return true;
        }
        return false;
    }

    public void afterTextChanged(Editable s) {
        char[] password = newPasswordText.getText().toString().trim().toCharArray();
        char[] confirmPassword = confirmPasswordText.getText().toString().trim().toCharArray();
        if (password.length < MIN_PASSWORD_SIZE) {
            error.setText(R.string.invalid_password);
            return;
        }
        if (Arrays.equals(password, confirmPassword)) {
            error.setText(R.string.settings_pwd_equal);
        } else {
            error.setText(R.string.settings_pwd_not_equal);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
