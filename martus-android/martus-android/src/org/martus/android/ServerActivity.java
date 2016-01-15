package org.martus.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.martus.android.dialog.LoginDialog;
import org.martus.clientside.MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL;
import org.martus.common.DammCheckDigitAlgorithm;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.DefaultServerConnector;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.network.PublicKeyTask;
import org.martus.common.network.UploadRightsTask;
import org.martus.util.StreamableBase64;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author roms
 *         Date: 12/10/12
 */
public class ServerActivity extends AbstractServerActivity implements TextView.OnEditorActionListener, LoginDialog.LoginDialogListener {

    private EditText textIp;
    private EditText textCode;
	private EditText textMagicWord;
    private Activity myActivity;
    private String serverIP;
    private String serverCode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_server);

        myActivity = this;
        textIp = (EditText)findViewById(R.id.serverIpText);
        textCode = (EditText)findViewById(R.id.serverCodeText);
	    textMagicWord = (EditText)findViewById(R.id.serverMagicText);
	    textMagicWord.setOnEditorActionListener(this);

	    if (haveVerifiedServerInfo()) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            confirmServer(textCode);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onResume() {
        super.onResume();

		if (haveVerifiedServerInfo()) {
            showLoginDialog();
		} else {
			try {
				InputStream inputStream = getAssets().open(SERVER_INFO_FILENAME);
				BufferedReader f = new BufferedReader(new InputStreamReader(inputStream));
				String line = f.readLine();
				int lineNumber = 0;
		        while (line != null) {
			        switch (lineNumber) {
	                    case 0:
		                    textIp.setText(line);
		                    break;
				        case 1:
					        textCode.setText(line);
					        break;
				        case 2:
					        textMagicWord.setText(line);
					        break;
			        }
			        lineNumber++;
			        line = f.readLine();
	            }
				confirmServer(textIp);
			} catch (IOException e) {
	            Log.w(AppConfig.LOG_LABEL, "couldn't read server info");
			}
		}

    }

	@Override
    public void onFinishPasswordDialog(TextView passwordText) {
        char[] password = passwordText.getText().toString().trim().toCharArray();
        boolean confirmed = (password.length >= MIN_PASSWORD_SIZE) && confirmAccount(password);
        if (!confirmed) {
            Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
            showLoginDialog();
        }

        invalidateAllElements(password);
        password = null;
	}

    public void confirmServer(View view) {
        serverIP = textIp.getText().toString().trim();
	    serverIP = serverIP.replace("*", ".");
        serverIP = serverIP.replace("#", ".");
        if ((serverIP.length() < DefaultServerConnector.MIN_SERVER_IP) || (! validate(serverIP))) {
            showErrorMessageWithRetry(getString(R.string.invalid_server_ip), getString(R.string.error_message));
	        textIp.requestFocus();
            return;
        }

        serverCode = textCode.getText().toString().trim();
        if (serverCode.length() < MIN_SERVER_CODE) {
            showErrorMessageWithRetry(getString(R.string.invalid_server_code), getString(R.string.error_message));
	        textCode.requestFocus();
            return;
        }

        showProgressDialog(getString(R.string.progress_connecting_to_server));

	    NonSSLNetworkAPI server = null;
	    try
	    {
		    server = new MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverIP, ((MartusApplication)getApplication()).getTransport());
	    } catch (Exception e)
	    {
		    Log.e(AppConfig.LOG_LABEL, "problem creating client side network handler using xml for non ssl", e);
		    showErrorMessageWithRetry(getString(R.string.error_getting_server_key), getString(R.string.error_message));
		    return;
	    }
	    MartusSecurity martusCrypto = AppConfig.getInstance().getCrypto();

        final AsyncTask <Object, Void, Vector> keyTask = new PublicKeyTask(this);
        keyTask.execute(server, martusCrypto);
    }

    @Override
    public void processResult(Vector serverInformation) {
        dismissProgressDialog();
	    if (! NetworkUtilities.isNetworkAvailable(this)) {
            showErrorMessageWithRetry(getString(R.string.no_network_connection), getString(R.string.error_message));
            return;
        }
        try {
            if (null == serverInformation || serverInformation.isEmpty()) {
                showErrorMessageWithRetry(getString(R.string.invalid_server_info), getString(R.string.error_message));
                return;
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Problem getting server public key", e);
            showErrorMessageWithRetry(getString(R.string.error_getting_server_key), getString(R.string.error_message));
            return;
        }

	    String serverPublicKey = (String)serverInformation.get(1);
        try {
            if (confirmPublicKey(serverCode, serverPublicKey)) {
                SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
                SharedPreferences.Editor editor = serverSettings.edit();
                editor.putString(SettingsActivity.KEY_SERVER_IP, serverIP);
                editor.putString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, serverPublicKey);
                editor.commit();

                SharedPreferences.Editor magicWordEditor = mySettings.edit();
                magicWordEditor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
                magicWordEditor.commit();
                Toast.makeText(this, getString(R.string.successful_server_choice), Toast.LENGTH_SHORT).show();

                File serverIpFile = getPrefsFile(PREFS_SERVER_IP);
                MartusUtilities.createSignatureFileFromFile(serverIpFile, getSecurity());

	            showProgressDialog(getString(R.string.progress_confirming_magic_word));
	            String magicWord = textMagicWord.getText().toString().trim();
                final AsyncTask<Object, Void, NetworkResponse> rightsTask = new UploadRightsTask(this);
                rightsTask.execute(getNetworkGateway(), martusCrypto, magicWord);

            } else {
                showErrorMessageWithRetry(getString(R.string.invalid_server_code), getString(R.string.error_message));
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL,"problem processing server IP", e);
            showErrorMessageWithRetry(getString(R.string.error_computing_public_code), getString(R.string.error_message));
        }


    }

    public static boolean confirmPublicKey(String publicCode, String publicKey) throws StreamableBase64.InvalidBase64Exception, DammCheckDigitAlgorithm.CheckDigitInvalidException, MartusCrypto.CreateDigestException {
        final String normalizedPublicCode20 = MartusCrypto.removeNonDigits(publicCode);
        final String computedPublicCode40 = MartusCrypto.computeFormattedPublicCode40(publicKey);
        final String normalizedComputedPublicCode40 = MartusCrypto.removeNonDigits(computedPublicCode40);
        if (normalizedPublicCode20.equals(normalizedComputedPublicCode40)) {
            return true;
        }

        final String computedPublicCode20 = MartusCrypto.computePublicCode(publicKey);
        final String normalizedComputedPublicCode20 = MartusCrypto.removeNonDigits(computedPublicCode20);
        return normalizedPublicCode20.equals(normalizedComputedPublicCode20);
    }

    private void showErrorMessageWithRetry(String msg, String title){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.retry_server, new RetryButtonHandler())
                .setNegativeButton(R.string.cancel_server, new CancelButtonHandler())
                .show();
    }

    class RetryButtonHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int whichButton) {
            /* Do nothing */
        }
    }

	private String getServerIP() {
		SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
        return serverSettings.getString(SettingsActivity.KEY_SERVER_IP, "");
	}

    private boolean haveVerifiedServerInfo() {
	    boolean canUpload = mySettings.getBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
	    String serverIP =  getServerIP();
        return ((serverIP.length() > 1) && canUpload);
    }

    class CancelButtonHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int whichButton) {

            if (!haveVerifiedServerInfo()) {
                myActivity.setResult(EXIT_RESULT_CODE);
            }
            myActivity.finish();
        }
    }

	@Override
	public void refreshView()
	{
		setContentView(R.layout.choose_server);
	}

	public static boolean validate(final String ip) {
      Pattern pattern = Pattern.compile(DefaultServerConnector.IP_ADDRESS_PATTERN);
      Matcher matcher = pattern.matcher(ip);
      return matcher.matches();
	}

    @Override
    public void processMagicWordResponse(NetworkResponse response) {
        dismissProgressDialog();
        try {
             if (!response.getResultCode().equals(NetworkInterfaceConstants.OK)) {
                 Toast.makeText(this, getString(R.string.no_upload_rights), Toast.LENGTH_SHORT).show();
	             textMagicWord.requestFocus();
             } else {
                 Toast.makeText(this, getString(R.string.success_magic_word), Toast.LENGTH_LONG).show();
                 SharedPreferences.Editor editor = mySettings.edit();
                 editor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, true);
                 editor.commit();
	             this.finish();
             }
        } catch (Exception e) {
             Log.e(AppConfig.LOG_LABEL, "Problem verifying upload rights", e);
             Toast.makeText(this, getString(R.string.problem_confirming_magic_word), Toast.LENGTH_SHORT).show();
        }
    }
}