package org.martus.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * @author roms
 *         Date: 9/12/13
 */
public class NetworkUtilities
{
	public static boolean isNetworkAvailable(Context context) {
	        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    boolean isDesiredConnectionType = true;
		    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		    boolean wifiOnly = settings.getBoolean(SettingsActivity.KEY_WIFI_ONLY, false);
		    if (wifiOnly) {
			    isDesiredConnectionType = netInfo != null && (netInfo.getType() == ConnectivityManager.TYPE_WIFI);
		    }
	        return netInfo != null && netInfo.isConnected() && isDesiredConnectionType;
	    }
}
