package org.benetech.secureapp.activities;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iangclifton.android.floatlabel.FloatLabel;

import org.apache.commons.io.FileUtils;
import org.benetech.secureapp.R;
import org.benetech.secureapp.utilities.Utility;
import org.benetech.secureapp.application.AppConfig;
import org.benetech.secureapp.tasks.CreateMartusCryptoKeyPairTask;
import org.martus.common.crypto.MartusSecurity;
import org.odk.collect.android.application.Collect;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.cacheword.Wiper;

/**
 * Created by animal@martus.org on 8/21/14.
 */

public class CreatePassphraseActivity extends AbstractLoginActivity implements ICacheWordSubscriber, TextWatcher {

    private static final String TAG = "CreatePassphraseActivity";
    private static final int MIN_PASSPHRASE_LENGTH = 8;
    private static final int NO_ERRORS_FOUND_MESSAGE_ID = -1;
    private FloatLabel passphraseEditField;
    private FloatLabel passphraseConfirmationEditField;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(org.benetech.secureapp.R.layout.create_account);

        passphraseEditField = (FloatLabel) findViewById(org.benetech.secureapp.R.id.passphrase_edit_field);
        passphraseConfirmationEditField = (FloatLabel) findViewById(org.benetech.secureapp.R.id.passphrase_confirm_edit_field);
        createAccountButton = (Button) findViewById(org.benetech.secureapp.R.id.create_account_button);
        createAccountButton.setEnabled(false);

        Util.setupFloatingLabelEditTextForPassword(passphraseEditField.getEditText());
        Util.setupFloatingLabelEditTextForPassword(passphraseConfirmationEditField.getEditText());

        passphraseConfirmationEditField.getEditText().addTextChangedListener(this);
        passphraseEditField.getEditText().addTextChangedListener(this);

        safelyClearOdkDbFilesFromPreviousAccount();
    }

    private void safelyClearOdkDbFilesFromPreviousAccount() {
        try {
            clearOdkDbFilesFromPreviousAccount();
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getApplicationContext(), getString(org.benetech.secureapp.R.string.error_cleaning_up_odk_folders), Toast.LENGTH_LONG).show();
        }
    }

    private void clearOdkDbFilesFromPreviousAccount() throws Exception {
        String externalStorageStatus = Environment.getExternalStorageState();
        final boolean isStorageAvailable = externalStorageStatus.equals(Environment.MEDIA_MOUNTED);
        if (!isStorageAvailable)
            throw new Exception(Collect.getInstance().getString(org.odk.collect.android.R.string.sdcard_unmounted, externalStorageStatus));
        

        String[] odkPathsToDelete = {Collect.ODK_ROOT, Collect.FORMS_PATH, Collect.INSTANCES_PATH, Collect.CACHE_PATH, Collect.METADATA_PATH,};
        for (String dirPath : odkPathsToDelete) {
            File odkDirToDelete = new File(dirPath);
            FileUtils.deleteDirectory(odkDirToDelete);
            if (odkDirToDelete.exists())
                throw new Exception(getString(R.string.error_message_could_not_clear_odk_folders_from_prvious_account));
            else
                Log.i(TAG, getString(R.string.error_message_odk_folders_deleted_as_part_of_cleanup, odkDirToDelete.getAbsolutePath()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        passphraseEditField.getEditText().setText("");
        passphraseConfirmationEditField.getEditText().setText("");
    }

    public void createAccount(View view) {
        int errorMessageId = getPossibleValidErrorMessage();
        if (errorMessageId != NO_ERRORS_FOUND_MESSAGE_ID)  {
            clearTextFields();
            Toast.makeText(getApplicationContext(), getString(errorMessageId), Toast.LENGTH_SHORT).show();
            return;
        }

        char[] passphrase = passphraseEditField.getEditText().getText().toString().toCharArray();
        try {
            getCacheWordActivityHandler().setPassphrase(passphrase);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, getString(R.string.error_message_cacheword_initialization_failed), e);
        }
        finally {
            Wiper.wipe(passphrase);
        }
    }

    private int getPossibleValidErrorMessage() {
        if (!doPassPhrasesMatch())
            return org.benetech.secureapp.R.string.settings_pwd_not_equal;

        if (!isValidPassPhraseLength())
            return org.benetech.secureapp.R.string.error_incorrect_passphrase;

        if (!containsOnlyValidChars())
            return org.benetech.secureapp.R.string.error_message_invalid_chars;

        return NO_ERRORS_FOUND_MESSAGE_ID;
    }

    private boolean containsOnlyValidChars() {
        char[] passPhrase = getPassphraseAsCharArray();
        try {
            for (int index = 0; index < passPhrase.length; ++index) {
                char charToValidate = passPhrase[index];
                if (!isCharAllowed(charToValidate)) {
                    return false;
                }
            }

            return true;
        } finally {
            Wiper.wipe(passPhrase);
        }
    }

    private boolean isCharAllowed(char charToValidate) {
        if (charToValidate == ' ')
            return false;

        return true;
    }

    private void clearTextFields() {
        getPassPhraseTextField().setText("");
        passphraseConfirmationEditField.getEditText().setText("");
        passphraseEditField.requestFocus();
    }

    private boolean doPassPhrasesMatch() {
        char[] passPhrase = getPassphraseAsCharArray();
        char[] confirmationPassphrase = getConfirmationPassphraseCharArray();
        try {
            return Arrays.equals(passPhrase, confirmationPassphrase);
        } finally {
            Wiper.wipe(passPhrase);
            Wiper.wipe(confirmationPassphrase);
        }
    }

    private boolean isValidPassPhraseLength() {
        if (getPassPhraseTextField().length() >= MIN_PASSPHRASE_LENGTH)
            return true;

        return false;
    }

    @Override
    public void onCacheWordLocked() {
       lockTextView(getPassPhraseTextField());
       lockTextView(passphraseConfirmationEditField.getEditText());
    }

    @Override
    protected TextView getPassPhraseTextField() {
        return passphraseEditField.getEditText();
    }

    private char[] getPassphraseAsCharArray() {
        return Utility.convertToCharArray(getPassPhraseTextField().getText());
    }

    private char[] getConfirmationPassphraseCharArray() {
        return Utility.convertToCharArray(passphraseConfirmationEditField.getEditText().getText());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // do nothing
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        char[] password = getPassphraseAsCharArray();
        char[] confirmPassword = getConfirmationPassphraseCharArray();
        if (password.length < 8) {
            passphraseEditField.getEditText().setError(getString(org.benetech.secureapp.R.string.invalid_password));
            return;
        }
        if (Arrays.equals(password, confirmPassword)) {
            createAccountButton.setEnabled(true);
        } else {
            passphraseConfirmationEditField.getEditText().setError(getString(org.benetech.secureapp.R.string.settings_pwd_not_equal));
        }
    }

    @Override
    protected void postMountStorageExecute() {
        if (getMartusCrypto(getApplication()).hasKeyPair())
            return;

        char[] passphrase = passphraseEditField.getEditText().getText().toString().toCharArray();
        try {
            final AsyncTask<Object, Void, Boolean> createAccountTask = new CreateMartusCryptoKeyPairTask(getMartusCrypto(getApplication()), mCreateMartusCryptoKeyPairCallback, getSettings());

            char[] pw = Utility.convertToCharArray(getCacheWordActivityHandler().getEncryptionKey());
            createAccountTask.execute(new Object[]{pw});
        } catch (Exception e) {
            Log.e("uploadForm", getString(R.string.error_message_failed_to_create_account), e);
        }
        finally {
            Wiper.wipe(passphrase);
        }
    }

    private MartusSecurity getMartusCrypto(Application app) {
        return AppConfig.getInstance(app).getCrypto();
    }

    private void showKeyPairErrorMessage() {
        Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.keypair_creation_failed));
    }

    /** Callback for CreateMartusCryptoKeyPairTask */
    private CreateMartusCryptoKeyPairTask.CreateMartusCryptoKeyPairCallback mCreateMartusCryptoKeyPairCallback = new CreateMartusCryptoKeyPairTask.CreateMartusCryptoKeyPairCallback() {
        @Override
        public void onCreateKeyPairError() {
            showKeyPairErrorMessage();
            throw new RuntimeException(getString(R.string.error_message_could_not_create_martus_crypto_key_pair));
        }

        @Override
        public void onCreateKeyPairSuccess() {
            dismissProgressDialog();
            startMainActivity();
        }
    };
}
