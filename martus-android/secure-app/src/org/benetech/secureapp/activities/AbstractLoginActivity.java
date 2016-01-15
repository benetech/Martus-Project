package org.benetech.secureapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import org.benetech.secureapp.application.MainApplication;
import org.martus.android.library.common.dialog.ProgressDialogHandler;
import org.benetech.secureapp.utilities.Utility;
import org.benetech.secureapp.R;
import org.benetech.secureapp.application.AppConfig;
import org.martus.common.crypto.MartusSecurity;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.cacheword.Wiper;

/**
 * Created by animal@martus.org on 9/4/14.
 */
abstract public class AbstractLoginActivity extends Activity implements ICacheWordSubscriber {

    private ProgressDialogHandler mProgressDialogHandler;
    public static final String KEY_KEY_PAIR = "martus_crypto_key_pair";
    private CacheWordHandler cacheWordActivityHandler;
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheWordActivityHandler = new CacheWordHandler(this);
        mProgressDialogHandler = new ProgressDialogHandler(this);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCacheWordActivityHandler().connectToService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        safelyDismissProgressDialog();

        getCacheWordActivityHandler().disconnectFromService();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        safelyDismissProgressDialog();
    }

    protected CacheWordHandler getCacheWordActivityHandler() {
        return cacheWordActivityHandler;
    }

    @Override
    public void onCacheWordOpened() {
        mountSecureStorage();
    }

    protected void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCacheWordUninitialized() {
    }

    protected void mountSecureStorage() {
        char[] passphraseChars = null;
        try {
            CharSequence passphrase = getPassPhraseTextField().getText();
            passphraseChars = Utility.convertToCharArray(passphrase);

            showProgressDialog(getString(R.string.label_setup_secure_storage));
            final AsyncTask<char[], Void, Void> mountSecureStorage = new MountSecureStorageTask();
            mountSecureStorage.execute(passphraseChars);
        } finally {
            Wiper.wipe(passphraseChars);
        }
    }

    protected void lockTextView(TextView view) {
        view.clearComposingText();
        view.setText("");
        view.setEnabled(false);
    }

    private class MountSecureStorageTask extends AsyncTask<char[], Void, Void> {

        @Override
        protected Void doInBackground(char[]... params) {
            char[] passphraseChars = params[0];
            try {
                ((MainApplication) getApplication()).mountSecureStorage(getCacheWordActivityHandler());
            } catch (Exception e) {
                Log.e(getLogTag(), getString(R.string.error_message_failed_to_mount_storage), e);
            } finally {
                Wiper.wipe(passphraseChars);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            postMountStorageExecute();
        }
    }

    private void safelyDismissProgressDialog() {
        if (getProgressDialogHandler().isShowing()){
            getProgressDialogHandler().dismissProgressDialog();
        }
    }

    protected void postMountStorageExecute() {
        dismissProgressDialog();
        startMainActivity();
    }

    private void showProgressDialog(String message) {
        getProgressDialogHandler().showProgressDialog(message);
    }

    protected void dismissProgressDialog() {
       if (getProgressDialogHandler().isShowing())
            getProgressDialogHandler().dismissProgressDialog();
    }

    private ProgressDialogHandler getProgressDialogHandler() {
        return mProgressDialogHandler;
    }

    protected MartusSecurity getMartusCrypto() {
        return AppConfig.getInstance(getApplication()).getCrypto();
    }

    public SharedPreferences getSettings() {
        return mSettings;
    }

    abstract protected TextView getPassPhraseTextField();

    abstract protected String getLogTag();
}
