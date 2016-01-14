package org.martus.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import java.security.SignatureException;

import info.guardianproject.onionkit.ui.OrbotHelper;

/**
 * Created by nimaa on 4/17/14.
 */
abstract public class AbstractTorActivity extends BaseActivity  implements OrbotHandler {

    private CompoundButton torToggleButton;

    private static final String PROXY_HOST_PROPERTY_NAME = "proxyHost";
    private static final String PROXY_PORT_PROPERTY_NAME = "proxyPort";
    private static final String SOCKS_PROXY_PORT_PROPERTY_NAME = "socksProxyPort";
    private static final String SOCKS_PROXY_HOST_PROPERTY_NAME = "socksProxyHost";

    abstract protected int getLayoutName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutName());
    }

    protected void syncTorToggleToMatchOrbotState() {
        if (torToggleButton == null)
            return;

        final boolean shouldTurnOffTorSwitch = shouldTurnOffTorSwitch();
        if (shouldTurnOffTorSwitch) {
            turnOffTorToggle();
        }
        else {
            turnOnTorToggle();
        }
    }

    private boolean shouldTurnOffTorSwitch() {
        try {
            OrbotHelper oc = new OrbotHelper(this);
            if (!oc.isOrbotInstalled())
                return true;

            if (oc.isOrbotStopped())
                return true;

            return false;
        }
        catch (SignatureException e)
        {
            return true;
        }
    }

    protected void synchronizeTorSwitchWithCurrentSystemProperties() {
        final boolean shouldTurnTorSwitchOn = shouldTurnTorSwitchOn();
        if (shouldTurnTorSwitchOn) {
            turnOnTorToggle();
        }
    }

    private boolean shouldTurnTorSwitchOn() {

        if (torToggleButton == null)
            return false;

        if (!PROXY_HOST.equals(System.getProperty(PROXY_HOST_PROPERTY_NAME)))
            return false;

        if (!String.valueOf(PROXY_HTTP_PORT).equals(System.getProperty(PROXY_PORT_PROPERTY_NAME)))
            return false;

        if (!PROXY_HOST.equals(System.getProperty(SOCKS_PROXY_HOST_PROPERTY_NAME)))
            return false;

        if (!String.valueOf(PROXY_SOCKS_PORT).equals(System.getProperty(SOCKS_PROXY_PORT_PROPERTY_NAME)))
            return false;

        return true;
    }

    protected void turnOffTorToggle() {
        torToggleButton.setChecked(false);
    }

    protected void turnOnTorToggle() {
        torToggleButton.setChecked(true);
    }

    private void torToggleStateChanged(boolean isChecked) {
        if  (isChecked) {
            System.setProperty(PROXY_HOST_PROPERTY_NAME, PROXY_HOST);
            System.setProperty(PROXY_PORT_PROPERTY_NAME, String.valueOf(PROXY_HTTP_PORT));

            System.setProperty(SOCKS_PROXY_HOST_PROPERTY_NAME, PROXY_HOST);
            System.setProperty(SOCKS_PROXY_PORT_PROPERTY_NAME, String.valueOf(PROXY_SOCKS_PORT));

            try {

                OrbotHelper oc = new OrbotHelper(this);

                if (!oc.isOrbotInstalled())
                {
                    oc.promptToInstall(this);
                }
                else if (!oc.isOrbotRunning())
                {
                    oc.requestOrbotStart(this);
                }
            } catch (Exception e) {
                Log.e(AppConfig.LOG_LABEL, "Tor check failed", e);
                showMessage(this, getString(R.string.invalid_orbot_message), getString(R.string.invalid_orbot_title));
                torToggleButton.setChecked(false);
            }

        } else {
            System.clearProperty(PROXY_HOST_PROPERTY_NAME);
            System.clearProperty(PROXY_PORT_PROPERTY_NAME);

            System.clearProperty(SOCKS_PROXY_HOST_PROPERTY_NAME);
            System.clearProperty(SOCKS_PROXY_PORT_PROPERTY_NAME);

            try {
            OrbotHelper oc = new OrbotHelper(this);

            if (!oc.isOrbotInstalled()) {
                oc.promptToInstall(this);
            } else {
                oc.requestOrbotStop(this);
            }
            } catch (Exception e) {
                Log.e(AppConfig.LOG_LABEL, "Tor check failed", e);
                showMessage(this, getString(R.string.invalid_orbot_message), getString(R.string.invalid_orbot_title));
                torToggleButton.setChecked(false);
            }
        }
    }

    public void setTorToggleButton(CompoundButton torToggleButtonToUse) {
        this.torToggleButton = torToggleButtonToUse;
    }

    public CompoundButton getTorToggleButton() {
        return torToggleButton;
    }

    @Override
    public void onOrbotInstallCanceled() {
        turnOffTorToggle();
    }

    @Override
    public void onOrbotStartCanceled() {
        turnOffTorToggle();
    }

    protected class TorToggleChangeHandler implements CompoundButton.OnCheckedChangeListener{
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            torToggleStateChanged(isChecked);
        }
    }
}
