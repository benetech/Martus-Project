package org.benetech.secureapp.activities;

import android.os.Handler;
import android.os.Message;

import org.benetech.secureapp.application.AppConfig;
import org.benetech.secureapp.application.MainApplication;
import org.martus.common.crypto.MartusSecurity;

import java.util.concurrent.TimeUnit;

/**
 * Created by animal@martus.org on 11/18/15.
 */
public class AppTimoutManager {

    private MainApplication mainApplication;
    private static Handler inactivityHandler;
    private LogOutProcess inactivityCallback;
    private LogoutActivityHandler logoutActivityHandler;
    private static final int MINUTES = 7;
    private static final long INACTIVITY_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(MINUTES);

    public AppTimoutManager(MainApplication mainApplicationToUse) {
        mainApplication = mainApplicationToUse;
        initInactivityHandler();
    }

    private void initInactivityHandler() {
        if (inactivityHandler == null)
            inactivityHandler = new EmptyHandler();

        if (inactivityCallback == null) {
            inactivityCallback = new LogOutProcess();
        }
    }

    public void registerLogoutHandler(LogoutActivityHandler logoutActivityHandlerToUse) {
        logoutActivityHandler = logoutActivityHandlerToUse;
        resetInactivityTimer();
    }

    public void resetInactivityTimer(){
        inactivityHandler.postDelayed(inactivityCallback, INACTIVITY_TIMEOUT_MILLIS);
    }

    public void disableInactivityTimer(){
        inactivityHandler.removeCallbacksAndMessages(null);
    }

    private MainApplication getMainApplication() {
        return mainApplication;
    }

    private class EmptyHandler extends Handler {
        public void handleMessage(Message msg) {}
    }

    private class LogOutProcess implements Runnable {

        @Override
        public void run() {
            MartusSecurity martusCrypto = AppConfig.getInstance(getMainApplication()).getCrypto();
            if (martusCrypto != null) {
                martusCrypto.clearKeyPair();
            }

            logoutActivityHandler.logout();
            logoutActivityHandler.finish();

            disableInactivityTimer();
        }
    }
}
