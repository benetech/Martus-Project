package org.martus.common.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * @author animal@martus.org Date: 9/11/14
 */
public class NetworkUtilities
{
	public static boolean isNetworkAvailable(Context context) {
	        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    boolean isDesiredConnectionType = true;
		    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

	        return netInfo != null && netInfo.isConnected() && isDesiredConnectionType;
	    }
}
