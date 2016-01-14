package org.martus.android;

import org.martus.common.crypto.MartusSecurity;

import android.util.Log;

/**
 * @author roms
 *         Date: 1/9/13
 */
public class LogOutProcess implements Runnable {

    private BaseActivity myActivity;

    public LogOutProcess(BaseActivity myActivity) {
        this.myActivity = myActivity;
	    Log.i(AppConfig.LOG_LABEL, "logout process my activity is " + myActivity.getClass().getName() +
			    " timeout is " + myActivity.getInactivityTimeout());
    }

    @Override
    public void run() {
        if (MartusApplication.isSendInProgress()) {
            return;
        }

        MartusSecurity martusCrypto = AppConfig.getInstance().getCrypto();
        if (null != martusCrypto) {
            Log.i(AppConfig.LOG_LABEL, "!!!! About to clear keypair !!!!!");
            martusCrypto.clearKeyPair();
        }
        if (null != myActivity) {
	        Log.i(AppConfig.LOG_LABEL, "should attempt to close " + myActivity.getClass().getName());
            myActivity.close();
        }
    }
}
