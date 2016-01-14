package org.martus.android;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.util.StreamableBase64;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author roms
 *         Date: 10/24/12
 */
public class DesktopKeyActivity extends BaseActivity implements TextView.OnEditorActionListener {

    final int ACTIVITY_CHOOSE_FILE = 1;
	public static final String ACCOUNT_ID_FILENAME = "HQ_Public_Account_ID.mpi";

    private EditText editTextPublicCode;
	private Button chooseFileButton;
	private ImageView accountFileStatusImage;
    private Activity activity;
    private boolean shouldShowInstallExplorer = false;
	private String extractedPublicKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop_sync);
        activity = this;

        editTextPublicCode = (EditText)findViewById(R.id.desktopCodeText);
	    editTextPublicCode.setOnEditorActionListener(this);
	    chooseFileButton = (Button)findViewById(R.id.desktopKeyChooseFile);
	    accountFileStatusImage = (ImageView)findViewById(R.id.desktopFileStatus);
	    extractedPublicKey = null;
	    chooseFileButton.requestFocus();
    }

    public void chooseKeyFile(View view) {
        shouldShowInstallExplorer = false;

        try {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("file/*");
            Intent intent = Intent.createChooser(chooseFile, getString(R.string.select_file_picker));
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Failed choosing file", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    extractedPublicKey = null;
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    try {
                        extractedPublicKey = extractPublicInfo(new File(path));
                        accountFileStatusImage.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        displayInvalidAccountFile();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    shouldShowInstallExplorer = true;
                }
                break;
            }
        }
    }

	private void displayInvalidAccountFile()
	{
		showMessage(activity, getString(R.string.invalid_public_account_file), getString(R.string.error_message));
		chooseFileButton.requestFocus();
		accountFileStatusImage.setVisibility(View.INVISIBLE);
	}

	@Override
    public void onResume() {
        super.onResume();

		File prefsDir = new File(getAppDir(), PREFS_DIR);
		File mpiFile = new File(prefsDir, "account.mpi");
		File accountIDFile =  getFileFromAssets(ACCOUNT_ID_FILENAME, mpiFile, this);
		if (accountIDFile != null && accountIDFile.exists()) {
			try {
				extractedPublicKey = extractPublicInfo(accountIDFile);
				storePublicKey();
		        finish();
			} catch (Exception e) {
				Log.e(AppConfig.LOG_LABEL, "Problem reading hq public key ", e);
			}
		}

        if (shouldShowInstallExplorer) {
            showInstallExplorerDialog();
            shouldShowInstallExplorer = false;
        }
    }

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
	        confirmKey(editTextPublicCode);
            return true;
        }
        return false;
    }

	public void confirmKey(View view) {
		String code = editTextPublicCode.getText().toString().trim();
        if ("".equals(code)) {
            editTextPublicCode.requestFocus();
            showMessage(activity, getString(R.string.public_code_validation_empty), getString(R.string.error_message));
            return;
        }

		if (extractedPublicKey == null) {
			displayInvalidAccountFile();
			return;
		}

		try {
            setPublicKey();
        } catch (Exception e) {
            showMessage(activity, getString(R.string.invalid_public_account_file), getString(R.string.error_message));
            Log.e("martus", "problem getting HQ key", e);
        }
	}

    public void setPublicKey() throws Exception {
        final String publicCode = editTextPublicCode.getText().toString().trim();
        if(!ServerActivity.confirmPublicKey(publicCode, extractedPublicKey)) {
            showMessage(activity, getString(R.string.invalid_public_code), getString(R.string.error_message));
	        editTextPublicCode.requestFocus();
            return;
        }

        SharedPreferences HQSettings = getSharedPreferences(PREFS_DESKTOP_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = HQSettings.edit();

        editor.putString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, extractedPublicKey);
        editor.commit();


        File desktopKeyFile = getPrefsFile(PREFS_DESKTOP_KEY);
        MartusUtilities.createSignatureFileFromFile(desktopKeyFile, getSecurity());
	    Toast.makeText(this, getString(R.string.success_import_hq_key), Toast.LENGTH_LONG).show();
        finish();
    }

	private void storePublicKey() throws IOException, MartusCrypto.MartusSignatureException
	{
		SharedPreferences HQSettings = getSharedPreferences(PREFS_DESKTOP_KEY, MODE_PRIVATE);
		SharedPreferences.Editor editor = HQSettings.edit();

		editor.putString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, extractedPublicKey);
		editor.commit();


		File desktopKeyFile = getPrefsFile(PREFS_DESKTOP_KEY);
		MartusUtilities.createSignatureFileFromFile(desktopKeyFile, getSecurity());
	}

    public String extractPublicInfo(File file) throws
            IOException,
            StreamableBase64.InvalidBase64Exception,
                MartusUtilities.PublicInformationInvalidException
    {
        Vector importedPublicKeyInfo = MartusUtilities.importClientPublicKeyFromFile(file);
        String publicKey = (String) importedPublicKeyInfo.get(0);
        String signature = (String) importedPublicKeyInfo.get(1);
        MartusUtilities.validatePublicInfo(publicKey, signature, getSecurity());
        return publicKey;
    }
}
