package org.martus.common.network;

import android.os.AsyncTask;
import android.util.Log;

import org.martus.clientside.MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL;
import org.martus.common.crypto.MartusSecurity;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by animal@martus.org on 9/11/14.
 */
public class DefaultServerConnector {

    private static final String TAG = "DefaultServerConnector";
    public static final String DEFAULT_SERVER_MAGIC_WORD = "martus";
    public static final int MIN_SERVER_IP = 7;
    public static final String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";


    public static void connectToServer(MartusSecurity martusCrypto, PassThroughTransportWrapper transport, String serverIp, PublicKeyTaskPostExecuteHandler postExecuteHandler) {
        NonSSLNetworkAPI server = null;
        try
        {

            server = new MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverIp, transport);
        } catch (Exception e)
        {
            Log.e(TAG, "problem creating client side network handler using xml for non ssl", e);
            return;
        }

        final AsyncTask<Object, Void, Vector> keyTask = new PublicKeyTask(postExecuteHandler);
        keyTask.execute(server, martusCrypto);
    }

    public static boolean isValidServerIp(String serverIp) {
        return (serverIp.length() < MIN_SERVER_IP) || (! validate(serverIp));
    }

    public static boolean validate(final String ip) {
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);

        return matcher.matches();
    }
}
