package org.martus.android;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.martus.android.dialog.ConfirmationDialog;
import org.martus.android.dialog.InstallExplorerDialog;
import org.martus.android.dialog.LoginDialog;
import org.martus.android.dialog.LoginRequiredDialog;
import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusKeyPair;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MobileMartusSecurity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.bugsense.trace.BugSenseHandler;

/**
 * @author roms
 *         Date: 12/19/12
 */
public class BaseActivity extends SherlockFragmentActivity implements ConfirmationDialog.ConfirmationDialogListener,
        LoginRequiredDialog.LoginRequiredDialogListener {

    private static final long MINUTE_MILLIS = 60000;

    public static final int EXIT_RESULT_CODE = 10;
    public static final int EXIT_REQUEST_CODE = 10;
	public static final int CLOSE_FORM_RESULT_CODE = 101;
	public static final int REQUEST_SEND_FORM = 102;
    public static final String PREFS_DESKTOP_KEY = "desktopHQ";
    public static final String PREFS_SERVER_IP = "serverIP";
    protected static final String PREFS_DIR = "shared_prefs";
	private static final String LOGIN_DIALOG_TAG = "dlg_login";
	static final int MIN_PASSWORD_SIZE = 8;

	public final static String PROXY_HOST = "127.0.0.1"; //test the local device proxy provided by Orbot/Tor
    public final static int PROXY_HTTP_PORT = 8118; //default for Orbot/Tor
    public final static int PROXY_SOCKS_PORT = 9050; //default for Orbot/Tor

    protected MartusApplication parentApp;
    private String confirmationDialogTitle;
    private ProgressDialog dialog;
    SharedPreferences mySettings;
	private static List<String> assetsList;

    private static Handler inactivityHandler;
    private LogOutProcess inactivityCallback;
    private static long inactivityTimeout;
	MartusSecurity martusCrypto;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Comment out for production build
        //BugSenseHandler.initAndStartSession(this, ExternalKeys.BUGSENSE_KEY);
        parentApp = (MartusApplication) this.getApplication();
	    AppConfig.setLang(getActivityName(), getResources().getConfiguration().locale.getLanguage());
        mySettings = PreferenceManager.getDefaultSharedPreferences(this);
	    confirmationDialogTitle = getString(R.string.confirm_default);
	    initInactivityHandler();
	    martusCrypto = AppConfig.getInstance().getCrypto();
    }

	private void initInactivityHandler() {
		int timeoutSetting = Integer.valueOf(mySettings.getString(SettingsActivity.KEY_TIMEOUT_MINUTES, SettingsActivity.DEFAULT_TIMEOUT_MINUTES));
	        setTimeout(timeoutSetting);
		if (inactivityHandler == null)
			inactivityHandler = new EmptyHandler();
		if (inactivityCallback == null) {
			inactivityCallback = new LogOutProcess(this);
		}
	}

    public void resetInactivityTimer(){
	    stopInactivityTimer();
        if (!MartusApplication.isSendInProgress()) {
            inactivityHandler.postDelayed(inactivityCallback, inactivityTimeout);
        } else {
            Log.i(AppConfig.LOG_LABEL, "is ignore in resetInactivityTimer");
        }
    }

    public void stopInactivityTimer(){
        Log.d(AppConfig.LOG_LABEL, "start stopInactivityTimer");
        inactivityHandler.removeCallbacksAndMessages(null);
    }

    void showLoginDialog() {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(LOGIN_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

        LoginDialog loginDialog = LoginDialog.newInstance();
        loginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
    }

	boolean confirmAccount(char[] password)  {

        String keyPairString = mySettings.getString(SettingsActivity.KEY_KEY_PAIR, "");

        // construct keypair from value read from prefs
        byte[] decodedKeyPair = Base64.decode(keyPairString, Base64.NO_WRAP);
        InputStream is = new ByteArrayInputStream(decodedKeyPair);
        try {
            martusCrypto.readKeyPair(is, password);
        } catch (Exception e) {
            //Log.e(AppConfig.LOG_LABEL, "Problem confirming password", e);
            return false;
        }

        martusCrypto.setShouldWriteAuthorDecryptableData(false);
        return true;
    }

	public void onCancelPasswordDialog() {
        this.finish();
    }

    public void onFinishLoginRequiredDialog() {
        BaseActivity.this.finish();
        Intent intent = new Intent(BaseActivity.this, MartusActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra(MartusActivity.RETURN_TO, MartusActivity.ACTIVITY_BULLETIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    /**
     *
     * @param timeoutSettingInMinutes number of minutes before timeout
     */
    public static void setTimeout(int timeoutSettingInMinutes) {
        BaseActivity.inactivityTimeout = timeoutSettingInMinutes * MINUTE_MILLIS;
    }

    public void showInstallExplorerDialog() {
        InstallExplorerDialog explorerDialog = InstallExplorerDialog.newInstance();
        explorerDialog.show(getSupportFragmentManager(), "dlg_install");
    }

    public void showConfirmationDialog() {
        ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance();
        confirmationDialog.show(getSupportFragmentManager(), "dlg_confirmation");
    }

    public void onConfirmationAccepted() {
        //do nothing
    }

    public void onConfirmationCancelled() {
        //do nothing
    }

    @Override
    public String getConfirmationTitle() {
        return confirmationDialogTitle;
    }

    @Override
    public String getConfirmationMessage() {
        return "";
    }

    @Override
    public void onUserInteraction(){
        resetInactivityTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetInactivityTimer();
	    confirmLanguage();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Comment out for production build
        //BugSenseHandler.closeSession(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    protected void showMessage(Context context, String msg, String title){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(android.R.drawable.ic_dialog_alert)
             .setTitle(title)
             .setMessage(msg)
             .setPositiveButton(R.string.alert_dialog_ok, new SimpleOkayButtonListener())
             .show();
    }

    public class SimpleOkayButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //do nothing
        }
    }

    public void close() {
        setResult(EXIT_RESULT_CODE);
        finish();
    }

    protected MartusCrypto getSecurity()
    {
        return AppConfig.getInstance().getCrypto();
    }

	protected MobileClientSideNetworkGateway getNetworkGateway() {
		return AppConfig.getInstance().getCurrentNetworkInterfaceGateway();
	}

    protected void verifySignedPrefsFile(String fileName) throws MartusUtilities.FileVerificationException {
        File prefsFile = getPrefsFile(fileName);
        if (prefsFile.exists()) {
            File sigFile = new File(prefsFile.getParent(), prefsFile.getName() + ".sig");
            MartusUtilities.verifyFileAndSignature(prefsFile, sigFile, getSecurity(), getSecurity().getPublicKeyString());
        }
    }

    protected void verifySavedDesktopKeyFile() throws MartusUtilities.FileVerificationException {
        verifySignedPrefsFile(PREFS_DESKTOP_KEY);
    }

    protected void verifyServerIPFile() throws MartusUtilities.FileVerificationException {
        verifySignedPrefsFile(PREFS_SERVER_IP);
    }

    protected File getPrefsFile(String fileName) {
        File prefsDir = new File(getAppDir(), PREFS_DIR);
        return new File(prefsDir, fileName + ".xml");
    }

	protected File getAppDir() {
		return getCacheDir().getParentFile();
	}


    public static MartusSecurity cloneSecurity(MartusSecurity original) {
        MartusSecurity cryptoCopy = null;
        try {
            MartusKeyPair keyPair = original.getKeyPair();
            byte[] data = keyPair.getKeyPairData();
            cryptoCopy = new MobileMartusSecurity();
            cryptoCopy.setKeyPairFromData(data);
            cryptoCopy.setShouldWriteAuthorDecryptableData(false);
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Problem copying crypto", e);
        }
        return cryptoCopy;
    }

    protected void showProgressDialog(String title) {
        dialog = new ProgressDialog(this);
        dialog.setTitle(title);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    protected void dismissProgressDialog(){
        dialog.dismiss();
    }

	public long getInactivityTimeout()
	{
		return inactivityTimeout;
	}

	protected void confirmLanguage()
	{
		Resources res = getResources();
		Configuration conf = res.getConfiguration();

		String currentLanguage = AppConfig.getLang(getActivityName());
		boolean useZawgyi = mySettings.getBoolean(SettingsActivity.KEY_USE_ZAWGYI, false);

		String lang = (useZawgyi) ?SettingsActivity. ZAWGYI_LANGUAGE_CODE : Locale.getDefault().getLanguage();
        if (! "".equals(lang) && ! currentLanguage.equals(lang))
        {
	        conf.locale = new Locale(lang);
            getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());
	        AppConfig.setLang(getActivityName(),lang);
	        invalidateOptionsMenu();
	        refreshView();
        }
	}

	public String getActivityName()
	{
		return this.getClass().getSimpleName();
	}

	public void refreshView()
	{

	}

	protected boolean confirmServerPublicKey() {
	    SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
        if ( serverSettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "").isEmpty()) {
            return false;
        }
	    AppConfig.getInstance().invalidateCurrentHandlerAndGateway();
        return true;
    }

	private static boolean assetsContains(String assetName, Context context) {
	    if (assetsList == null) {
	        try {
		        assetsList = Arrays.asList(context.getAssets().list(""));
	        } catch (IOException e) {
		        return false;
	        }
	    }
	    return assetsList.contains(assetName);
	}

	protected static File getFileFromAssets(String assetFileName, File outputFile, Context context) {
		Log.i(AppConfig.LOG_LABEL, "start getFileFromAssets  assetFileName = " + assetFileName + " outputFile = " + outputFile.getAbsolutePath());
	    try {
		    if (!assetsContains(assetFileName, context))
			    return null;

		    Log.i(AppConfig.LOG_LABEL, " assets contains " + assetFileName);
		    InputStream is = context.getAssets().open(assetFileName);
		    int size = is.available();
		    byte[] buffer = new byte[size];
		    is.read(buffer);
		    is.close();

		    FileOutputStream fos = new FileOutputStream(outputFile);
		    fos.write(buffer);
		    fos.close();
			return outputFile;
		  } catch (Exception e) {
			  Log.e(AppConfig.LOG_LABEL, "problem getting asset " + assetFileName, e);
		  }
		return null;
	}

	protected static void clearDirectory(final File dir) {
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {
                    if (child.isDirectory()) {
                        clearDirectory(child);
                    }
                    child.delete();
                }
            }
            catch(Exception e) {
                Log.e(AppConfig.LOG_LABEL, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
    }

    protected void showErrorMessage(String msg, String title){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.show();
    }

    public static void makeTextViewClickableHyperlink(TextView textView) {
        SpannableStringBuilder hyperLinkStringBuilder = new SpannableStringBuilder();
        hyperLinkStringBuilder.append(textView.getText());
        URLSpan textUsedAsUrl = new URLSpan(textView.getText().toString());
        hyperLinkStringBuilder.setSpan(textUsedAsUrl, 0, hyperLinkStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(hyperLinkStringBuilder, TextView.BufferType.SPANNABLE);
    }

    protected void invalidateAllElements(char[] arrayToInvalidate) {
        for (int index = 0; index < arrayToInvalidate.length; ++index) {
            arrayToInvalidate[index] = ' ';
        }
    }
}