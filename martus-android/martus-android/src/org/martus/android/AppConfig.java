package org.martus.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.clientside.MobileClientBulletinStore;
import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MobileMartusSecurity;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.PassThroughTransportWrapper;

import java.io.File;
import java.util.HashMap;

/**
 * @author roms
 *         Date: 10/24/12
 */
public class AppConfig {

    public static final String LOG_LABEL = "martus";

    private static AppConfig instance;
    private MobileClientBulletinStore store;
    private MartusSecurity martusCrypto;
	private Context context;
	private PassThroughTransportWrapper transport;
	private String serverPublicKey;
    private String serverIP;
	private ClientSideNetworkInterface currentNetworkInterfaceHandler;
	private MobileClientSideNetworkGateway currentNetworkInterfaceGateway;
	private static HashMap<String, String> langMap;

    public static void initInstance(Context context ) {
        if (instance == null) {
            instance = new AppConfig(context);
        }
    }

    public static AppConfig getInstance() {
        return instance;
    }

    private AppConfig(Context context) {
        // Constructor hidden because this is a singleton

	    this.context = context;

        transport = new PassThroughTransportWrapper();
        File torDirectory = getOrchidDirectory();
        torDirectory.mkdirs();

        try {
            martusCrypto = new MobileMartusSecurity();
        } catch (Exception e) {
            Log.e(LOG_LABEL, "unable to initialize crypto", e);
        }

        store = new MobileClientBulletinStore(martusCrypto);
        try {
            store.doAfterSigninInitialization(getAppDir(context));
        } catch (Exception e) {
            Log.e(LOG_LABEL, "unable to initialize store", e);
        }

        store.setTopSectionFieldSpecs(StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
        store.setBottomSectionFieldSpecs(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());

	    startOrStopTorAsRequested();

    }

	public void startOrStopTorAsRequested() {
		boolean isTorEnabled = false;
		int newTimeout;
		if(isTorEnabled)
			newTimeout = ClientSideNetworkHandlerUsingXmlRpc.TOR_GET_SERVER_INFO_TIMEOUT_SECONDS;
		else
			newTimeout = ClientSideNetworkHandlerUsingXmlRpc.WITHOUT_TOR_GET_SERVER_INFO_TIMEOUT_SECONDS;

		// NOTE: force the handler to be created if it wasn't already
		getCurrentNetworkInterfaceHandler();
		boolean isServerConfigured = (currentNetworkInterfaceHandler != null);
		if(isServerConfigured)
			currentNetworkInterfaceHandler.setTimeoutGetServerInfo(newTimeout);
	}

    public MartusSecurity getCrypto() {
        return martusCrypto;
    }

    public MobileClientBulletinStore getStore() {
        return store;
    }

	public File getOrchidDirectory() {
		return new File(getAppDir(context), BaseActivity.PREFS_DIR);
	}

	public PassThroughTransportWrapper getTransport() {
		return transport;
	}

	public MobileClientSideNetworkGateway getCurrentNetworkInterfaceGateway()
	{

		if(currentNetworkInterfaceGateway == null)
		{
			currentNetworkInterfaceGateway = new MobileClientSideNetworkGateway(getCurrentNetworkInterfaceHandler());
		}

		return currentNetworkInterfaceGateway;
	}

	private ClientSideNetworkInterface getCurrentNetworkInterfaceHandler()
	{
		updateSettings();
		if(currentNetworkInterfaceHandler == null) {
			currentNetworkInterfaceHandler = createXmlRpcNetworkInterfaceHandler();
		}

		return currentNetworkInterfaceHandler;
	}

	private ClientSideNetworkInterface createXmlRpcNetworkInterfaceHandler()
	{
		return MobileClientSideNetworkGateway.buildNetworkInterface(serverIP , serverPublicKey, transport);
	}

	public void invalidateCurrentHandlerAndGateway()
	{
		currentNetworkInterfaceHandler = null;
		currentNetworkInterfaceGateway = null;
	}

	private void updateSettings() {
        SharedPreferences serverSettings = context.getSharedPreferences(BaseActivity.PREFS_SERVER_IP, Context.MODE_PRIVATE);
        serverIP = serverSettings.getString(SettingsActivity.KEY_SERVER_IP, "");
        serverPublicKey = serverSettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
    }

	public static void setLang(String activity, String languageCode)
	{
		if (langMap == null)
			langMap = new HashMap<String, String>();
		langMap.put(activity, languageCode);
	}

	public static String getLang(String activity)
	{
		if (langMap.isEmpty())
			return null;
		return langMap.get(activity);
	}

	public static File getAppDir(Context myContext) {
		return myContext.getCacheDir().getParentFile();
	}

}
