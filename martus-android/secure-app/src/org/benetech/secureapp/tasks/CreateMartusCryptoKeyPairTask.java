package org.benetech.secureapp.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.benetech.secureapp.R;
import org.benetech.secureapp.activities.AbstractLoginActivity;
import org.benetech.secureapp.application.MainApplication;
import org.martus.common.crypto.MartusCrypto;

import java.io.ByteArrayOutputStream;

/**
 * Created by animal@martus.org on 10/3/14.
 */
public class CreateMartusCryptoKeyPairTask extends AsyncTask<Object, Void, Boolean> {

    private static final String TAG = "CreateMartusCryptoKeyPairTask";
    private MartusCrypto mMartusCrypto;
    private CreateMartusCryptoKeyPairCallback mCallback;
    private SharedPreferences mSettings;

    /** Callback used by clients of this class */
    public interface CreateMartusCryptoKeyPairCallback {
        public void onCreateKeyPairError();
        public void onCreateKeyPairSuccess();
    }

    public CreateMartusCryptoKeyPairTask(MartusCrypto martusCrypto, CreateMartusCryptoKeyPairCallback callback, SharedPreferences settings) {
        mMartusCrypto = martusCrypto;
        mCallback = callback;
        mSettings = settings;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            getMartusCrypto().createKeyPair();
            char[] passwordArray = (char[]) params[0];

            getMartusCrypto().writeKeyPair(out, passwordArray);
            out.close();

            byte[] keyPairData = out.toByteArray();
            String encodedKeyPair = Base64.encodeToString(keyPairData, Base64.NO_WRAP);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(AbstractLoginActivity.KEY_KEY_PAIR, encodedKeyPair);
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_problem_creating_account), e);
            mCallback.onCreateKeyPairError();
            return false;
        }

        return true;
    }

    private MartusCrypto getMartusCrypto() {
        return mMartusCrypto;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        if (success)
            mCallback.onCreateKeyPairSuccess();
    }
}
