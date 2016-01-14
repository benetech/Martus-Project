package org.martus.android;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author roms
 *         Date: 10/8/13
 */
public class CreateAccountActivity extends BaseActivity implements TextWatcher, TextView.OnEditorActionListener
{

	public static final int MIN_PASSWORD_SIZE = 8;
    private EditText newPasswordText;
    private EditText confirmPasswordText;
    private TextView error;
	public static final String EMPTY_TEXT = "";

	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.create_account);

		newPasswordText = (EditText) findViewById(R.id.new_password_field);
        confirmPasswordText = (EditText) findViewById(R.id.confirm_password_field);
        confirmPasswordText.setOnEditorActionListener(this);
        error = (TextView) findViewById(R.id.password_problem_text);

        confirmPasswordText.addTextChangedListener(this);
	    newPasswordText.addTextChangedListener(this);

	        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	    }

	public void onFinishNewAccountEntry(View view) {

        boolean failed = false;
        char[] password = newPasswordText.getText().toString().trim().toCharArray();
        char[] confirmPassword = confirmPasswordText.getText().toString().trim().toCharArray();
        if (password.length < MIN_PASSWORD_SIZE) {
	        showMessage(this, getString(R.string.invalid_password), getString(R.string.error_create_account));
	        newPasswordText.setText(EMPTY_TEXT);
            confirmPasswordText.setText(EMPTY_TEXT);
            newPasswordText.requestFocus();
            failed = true;
        }
        if (!Arrays.equals(password, confirmPassword)) {
	        showMessage(this, getString(R.string.settings_pwd_not_equal), getString(R.string.error_create_account));
	        newPasswordText.setText(EMPTY_TEXT);
	        confirmPasswordText.setText(EMPTY_TEXT);
	        newPasswordText.requestFocus();
            failed = true;
        }

        if (!failed) {
	        showProgressDialog(getString(R.string.progress_creating_account));
            final AsyncTask<Object, Void, Void> createAccountTask = new CreateAccountTask();
	        createAccountTask.execute(new Object[]{password});
        }
    }

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if (actionId == EditorInfo.IME_ACTION_DONE) {
            onFinishNewAccountEntry(newPasswordText);
            return true;
        }
        return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		//do nothing
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		// do nothing
	}

	@Override
	public void afterTextChanged(Editable s)
	{
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

	class CreateAccountTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {

	        martusCrypto.createKeyPair();
	        char[] passwordArray = (char[])params[0];

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                martusCrypto.writeKeyPair(out, passwordArray);
                out.close();
                byte[] keyPairData = out.toByteArray();

                // write keypair to prefs
                // need to first base64 encode so we can write to prefs
                String encodedKeyPair = Base64.encodeToString(keyPairData, Base64.NO_WRAP);

                // write to prefs
                SharedPreferences.Editor editor = mySettings.edit();
                editor.putString(SettingsActivity.KEY_KEY_PAIR, encodedKeyPair);
                editor.commit();
            } catch (Exception e) {
                Log.e(AppConfig.LOG_LABEL, "Problem creating account", e);
                showMessage(CreateAccountActivity.this, getString(R.string.error_create_account), getString(R.string.error_message));
            }
	        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
	        dismissProgressDialog();
            finish();
        }
    }
}
