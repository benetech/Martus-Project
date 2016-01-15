package org.martus.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import org.martus.android.dialog.LoginDialog;
import org.martus.android.dialog.ModalConfirmationDialog;
import org.martus.common.MartusUtilities;

public class MartusActivity extends AbstractMainActivityWithMainMenuHandler implements LoginDialog.LoginDialogListener, OrbotHandler {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private int invalidLogins;

    static final int ACTIVITY_DESKTOP_KEY = 2;
    public static final int ACTIVITY_BULLETIN = 3;
    public static final String RETURN_TO = "return_to";
    public static final String HAVE_FORM= "haveForm";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateSettings();
    }

    @Override
    protected int getLayoutName() {
        return R.layout.main;
    }

    protected void onStart() {
        super.onStart();
        invalidLogins = 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        if (martusCrypto.hasKeyPair()) {
	        boolean canUpload = mySettings.getBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
            if (!confirmServerPublicKey() || !canUpload) {
                Intent intent = new Intent(MartusActivity.this, TorIntroActivity.class);
                startActivity(intent);
                return;
            }
	        if (!checkDesktopKey()) {
                return;
            }

            syncTorToggleToMatchOrbotState();

	        verifySetupInfo();
            Intent bulletinIntent = new Intent(this, BulletinActivity.class);
            startActivity(bulletinIntent);
        } else {
            if (isAccountCreated()) {
                showLoginDialog();
            } else {
	            Intent intent = new Intent(MartusActivity.this, CreateAccountActivity.class);
                startActivityForResult(intent, EXIT_REQUEST_CODE);
                return;
            }
        }
        updateSettings();
    }

    @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXIT_REQUEST_CODE && resultCode == EXIT_RESULT_CODE) {
            AppConfig.getInstance().getCrypto().clearKeyPair();
            finish();
        }
    }

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	        if (event.getAction() == KeyEvent.ACTION_UP &&
	            keyCode == KeyEvent.KEYCODE_MENU) {
	            openOptionsMenu();
	            return true;
	        }
	    }
	    return super.onKeyUp(keyCode, event);
	}

    @Override
    protected void onNewIntent(Intent intent) {

        String filePath = intent.getStringExtra(BulletinActivity.EXTRA_ATTACHMENT);
        if (null != filePath) {
            Intent bulletinIntent = new Intent(MartusActivity.this, BulletinActivity.class);
            bulletinIntent.putExtra(BulletinActivity.EXTRA_ATTACHMENT, filePath);
            startActivity(bulletinIntent);
        }
    }

    private void verifySetupInfo() {
        try {
            verifySavedDesktopKeyFile();
            verifyServerIPFile();
        } catch (MartusUtilities.FileVerificationException e) {
            Log.e(AppConfig.LOG_LABEL, "Desktop key file corrupted in checkDesktopKey");
            setConfirmationType(CONFIRMATION_TYPE_TAMPERED_DESKTOP_FILE);
            showModalConfirmationDialog();
        }
    }

    private void showModalConfirmationDialog() {
        ModalConfirmationDialog confirmationDialog = ModalConfirmationDialog.newInstance();
        confirmationDialog.show(getSupportFragmentManager(), "dlg_confirmation");
    }

    private boolean checkDesktopKey() {
        SharedPreferences HQSettings = getSharedPreferences(PREFS_DESKTOP_KEY, MODE_PRIVATE);
        String desktopPublicKeyString = HQSettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, "");

        if (desktopPublicKeyString.length() < 1) {
            Intent intent = new Intent(MartusActivity.this, ContactImportChoiceActivity.class);
            startActivityForResult(intent, ACTIVITY_DESKTOP_KEY);
            return false;
        }
        return true;
    }

    private boolean isAccountCreated() {
        String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");
        return keyPairString.length() > 1;
    }

    @Override
    public void onFinishPasswordDialog(TextView passwordText) {
        char[] password = passwordText.getText().toString().trim().toCharArray();
        boolean confirmed = (password.length >= MIN_PASSWORD_SIZE) && confirmAccount(password);
        if (!confirmed) {
            if (++invalidLogins == MAX_LOGIN_ATTEMPTS) {
                finish();
            }
            Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
            showLoginDialog();
            return;
        }

        SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
        serverPublicKey = serverSettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
	    AppConfig.getInstance().invalidateCurrentHandlerAndGateway();

        onResume();
        invalidateAllElements(password);
        password = null;

        int count = getNumberOfUnsentBulletins();
        if (count != 0) {
            Resources res = getResources();
            showMessage(this, res.getQuantityString(R.plurals.show_unsent_count, count, count), getString(R.string.show_unsent_title));
        }
    }

    @Override
    public void onOrbotInstallCanceled() {
        turnOffTorToggle();
    }

    @Override
    public void onOrbotStartCanceled() {
        turnOffTorToggle();
    }

    @Override
    public String getConfirmationTitle() {
        if (getConfirmationType() == CONFIRMATION_TYPE_TAMPERED_DESKTOP_FILE) {
            return getString(R.string.confirm_tamper_reset_title);
        }

        return super.getConfirmationTitle();
    }

    @Override
    public String getConfirmationMessage() {
        if (getConfirmationType() == CONFIRMATION_TYPE_TAMPERED_DESKTOP_FILE) {
            return getString(R.string.confirm_tamper_reset_message);
        } else {
            int count = getNumberOfUnsentBulletins();
            if (count == 0) {
              return getString(R.string.confirm_reset_install_extra_no_pending);
            } else {
                Resources res = getResources();
                return res.getQuantityString(R.plurals.confirm_reset_install_extra, count, count);
            }
        }
    }

    @Override
    public void onConfirmationCancelled() {
        if (getConfirmationType() == CONFIRMATION_TYPE_TAMPERED_DESKTOP_FILE) {
            martusCrypto.clearKeyPair();
            finish();
        }
    }

	public void refreshView()
	{
		setContentView(R.layout.main);
	}
}