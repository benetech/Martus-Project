package info.guardianproject.onionkit.ui;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;

import org.martus.android.OrbotHandler;
import org.martus.android.R;

public class OrbotHelper {

    private final static int REQUEST_CODE_STATUS = 100;

    public final static String URI_ORBOT = "org.torproject.android";
    private static final String ORBOT_CLASS_NAME = URI_ORBOT + "." + "Orbot";
    public final static String TOR_BIN_PATH = "/data/data/org.torproject.android/app_bin/tor";

    public final static String ACTION_START_TOR = "org.torproject.android.START_TOR";
    public final static String ACTION_REQUEST_HS = "org.torproject.android.REQUEST_HS_PORT";
    public final static int HS_REQUEST_CODE = 9999;
	public static final String MD5_FINGERPRINT_ORBOT = "32eee48b742ffc17c405061a543e2141";

    private Context mContext = null;

    public OrbotHelper(Context context)
    {
        mContext = context;
    }

    public boolean isOrbotStopped()
    {
        return !isOrbotRunning();
    }

    public boolean isOrbotRunning()
    {
        int procId = TorServiceUtils.findProcessId(TOR_BIN_PATH);

        return (procId != -1);
    }

    public boolean isOrbotInstalled() throws SignatureException
    {
        return isAppInstalled(URI_ORBOT);
    }

    private boolean isAppInstalled(String uri) throws SignatureException
    {
        PackageManager pm = mContext.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;

	        for (Signature sig : mContext.getPackageManager().getPackageInfo(uri, PackageManager.GET_SIGNATURES).signatures) {
		        MessageDigest m;
		        String md5 = null;
		        try
		        {
			        m = MessageDigest.getInstance("MD5");
			        m.update(sig.toByteArray());
                    md5 = new BigInteger(1, m.digest()).toString(16);
		        } catch (NoSuchAlgorithmException e)
		        {
			        e.printStackTrace();
		        }
		        if (md5 == null || !(md5.equals(MD5_FINGERPRINT_ORBOT)))
                {
                    throw new SignatureException();
                }
	          }
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public void promptToInstall(Activity activity)
    {
        String uriMarket = activity.getString(R.string.market_orbot);
        // show dialog - install from market, f-droid or direct APK
        showDownloadDialog(activity, activity.getString(R.string.install_orbot_),
                activity.getString(R.string.you_must_have_orbot),
                activity.getString(R.string.yes), activity.getString(R.string.no), uriMarket);
    }

    private static AlertDialog showDownloadDialog(final Activity activity, CharSequence stringTitle, CharSequence stringMessage, CharSequence stringButtonYes, CharSequence stringButtonNo, final String uriString) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(stringTitle);
        downloadDialog.setMessage(stringMessage);
        downloadDialog.setPositiveButton(stringButtonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(stringButtonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ((OrbotHandler)(activity)).onOrbotInstallCanceled();
            }
        });
        return downloadDialog.show();
    }

    public void requestOrbotStart(final Activity activity)
    {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(R.string.start_orbot_);
        downloadDialog.setMessage(R.string.orbot_doesn_t_appear_to_be_running_would_you_like_to_start_it_up_and_connect_to_tor_);
        downloadDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(URI_ORBOT);
                intent.setAction(ACTION_START_TOR);
                activity.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ((OrbotHandler)(activity)).onOrbotInstallCanceled();
            }
        });
        downloadDialog.show();
    }

    public void requestOrbotStop(final Activity activity) {
        if (isOrbotStopped())
            return;

        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(R.string.stop_orbot);
        downloadDialog.setMessage(R.string.orbot_running_should_stop_message);
        downloadDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.startActivity(new Intent().setClassName(URI_ORBOT, ORBOT_CLASS_NAME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        downloadDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ((OrbotHandler)(activity)).onOrbotInstallCanceled();
            }
        });
        downloadDialog.show();
    }

    public void requestHiddenServiceOnPort(Activity activity, int port)
    {
        Intent intent = new Intent(URI_ORBOT);
        intent.setAction(ACTION_REQUEST_HS);
        intent.putExtra("hs_port", port);

        activity.startActivityForResult(intent, HS_REQUEST_CODE);
    }
}