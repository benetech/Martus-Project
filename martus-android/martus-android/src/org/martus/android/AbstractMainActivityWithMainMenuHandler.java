package org.martus.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.Exceptions;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.util.StreamableBase64;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * Created by nimaa on 4/29/14.
 */
abstract public class AbstractMainActivityWithMainMenuHandler extends AbstractTorActivity {

    public static final String ACCOUNT_ID_FILENAME = "Mobile_Public_Account_ID.mpi";
    private static final String SERVER_COMMAND_PREFIX = "MartusServer.";
    private final static String RPC2_PATH = "/RPC2";
    private static final int CONFIRMATION_TYPE_DELETE_ACCOUNT = 0;
    protected static final int CONFIRMATION_TYPE_CANCEL_BULLETIN = 1;
    protected static final int CONFIRMATION_TYPE_DELETE_ATTACHMENT = 2;
    protected static final int CONFIRMATION_TYPE_TAMPERED_DESKTOP_FILE = 3;
    protected static final int CONFIRMATION_TYPE_GO_TO_HOME = 4;
    protected static final int CONFIRMATION_TYPE_LOGOUT = 5;
    protected static final int CONFIRMATION_TYPE_LOOSE_DATA = 6;
    private static final String PACKETS_DIR = "packets";

    protected String serverPublicKey;
    private String serverIP;
    private int confirmationType;

    @Override
    public void onResume() {
        super.onResume();

        updateSettings();
        synchronizeTorSwitchWithCurrentSystemProperties();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        setTorToggleButton((CompoundButton)  menu.findItem(R.id.tor_button).getActionView());
        getTorToggleButton().setOnCheckedChangeListener(new AbstractTorActivity.TorToggleChangeHandler());
        getTorToggleButton().setText(R.string.tor_label);
        synchronizeTorSwitchWithCurrentSystemProperties();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        int id = item.getItemId();
        if (id == R.id.settings_menu_item) {
            confirmStartSettingsActivity();
            return true;
        } else if (id == R.id.quit_menu_item) {
            quit();
            return true;
        } else if (id == R.id.ping_server_menu_item) {
            pingServer();
            return true;
        } else if (id == R.id.resend_menu_item) {
            resendFailedBulletins();
            return true;
        } else if (id == R.id.view_public_code_menu_item) {
            showPublicKeyDialog();
            return true;
        } else if (id == R.id.view_access_token_menu_item) {
            showAccessToken();
        } else if (id == R.id.reset_install_menu_item) {
            deleteUserAccount();
            return true;
        } else if (id == R.id.show_version_menu_item) {
            showVersionNumberAsToast();
            return true;
        } else if (id == R.id.export_mpi_menu_item) {
            File mpiFile = getMpiFile();
            showMessage(this, mpiFile.getAbsolutePath(), getString(R.string.exported_account_id_file_confirmation));
            return true;
        } else if (id == R.id.email_mpi_menu_item) {
            sendAccountIDAsEmail();
            return true;
        } else if (id == R.id.send_mpi_menu_item_via_bulletin) {
            sendAccountIDAsBulletin();
            return true;
        } else if (id == R.id.feedback_menu_item) {
            showContactUs();
            return true;
        } else if (id == R.id.view_docs_menu_item) {
            showViewDocs();
            return true;
        } else if (id == R.id.view_tor_message_menu_item) {
            showTorMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void updateSettings() {
        SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
        serverPublicKey = serverSettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
        serverIP = serverSettings.getString(SettingsActivity.KEY_SERVER_IP, "");
    }

    private void deleteUserAccount() {
        if (MartusApplication.isSendInProgress()) {
            showMessage(this, getString(R.string.logout_while_sending_message),
                    getString(R.string.reset_while_sending_title));
        } else {
            setConfirmationType(CONFIRMATION_TYPE_DELETE_ACCOUNT);
            showConfirmationDialog();
        }
    }

    private void confirmStartSettingsActivity() {
        setConfirmationType(CONFIRMATION_TYPE_LOOSE_DATA);
        showConfirmationDialog();
    }

    private void startSettingsAtivitys() {
        Intent intent;
        intent = new Intent(AbstractMainActivityWithMainMenuHandler.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void quit() {
        if (MartusApplication.isSendInProgress()) {
            showMessage(this, getString(R.string.logout_while_sending_message),
                    getString(R.string.logout_while_sending_title));
        } else {
            setConfirmationType(CONFIRMATION_TYPE_LOGOUT);
            showConfirmationDialog();
        }
    }

    private void showAccessToken() {
        showProgressDialog(getString(R.string.progress_connecting_to_server));
        final AsyncTask <Object, Void, NetworkResponse> keyTask = new RetrieveAccessTokenTask();
        keyTask.execute();
    }

    private void showPublicKeyDialog() {
        try {
            String publicCode40Digit = MartusCrypto.computeFormattedPublicCode40(getSecurity().getPublicKeyString());
            String publicCode = MartusCrypto.getFormattedPublicCode(getSecurity().getPublicKeyString());

            String newPublicCodeMessageSection = getString(R.string.view_new_public_code_message, publicCode40Digit);
            String oldPublicCodeMessageSection = getString(R.string.view_old_public_code_message, publicCode);
            String entireMessage = newPublicCodeMessageSection + "\n\n" + oldPublicCodeMessageSection;

            showMessage(this, entireMessage, getString(R.string.view_public_code_dialog_title));
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "couldn't get public code", e);
            showMessage(this, getString(R.string.view_public_code_dialog_error), getString(R.string.view_public_code_dialog_title));
        }
    }

    private void showVersionNumberAsToast() {
        PackageInfo pInfo;
        String versionLabel;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionNameLabel = getString(R.string.version_name_label, pInfo.versionName);
            String versionCodeLabel = getString(R.string.version_code_label, pInfo.versionCode);
            versionLabel = versionNameLabel + "\n" + versionCodeLabel;
        } catch (PackageManager.NameNotFoundException e) {
            versionLabel = "?";
        }
        Toast.makeText(this, versionLabel, Toast.LENGTH_LONG).show();
    }

    public static void logout() {
        AppConfig.getInstance().getCrypto().clearKeyPair();
    }

    private void sendAccountIDAsEmail()
    {
        File mpiFile = getMpiFile();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        Uri uri = Uri.parse("file://" + mpiFile.getAbsolutePath());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email_title)));
    }

    private void sendAccountIDAsBulletin()
    {
        File mpiFile = getMpiFile();
        String filePath = mpiFile.getPath();
        Intent bulletinIntent = new Intent(this, BulletinActivity.class);
        bulletinIntent.putExtra(BulletinActivity.EXTRA_ATTACHMENT, filePath);
        startActivity(bulletinIntent);
    }

    protected File getMpiFile()
    {
        File externalDir;
        File mpiFile;
        externalDir = Environment.getExternalStorageDirectory();
        mpiFile = new File(externalDir, ACCOUNT_ID_FILENAME);
        try {
            exportPublicInfo(mpiFile);
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "couldn't export public id", e);
            showMessage(this, getString(R.string.export_public_account_id_dialog_error),
                    getString(R.string.export_public_account_id_dialog_title));
        }
        return mpiFile;
    }

    private void showTorMessage() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.view_tor_help_message, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.tor_label)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new SimpleOkayButtonListener())
                .show();
    }

    private void showContactUs()
    {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.contact_us, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(android.R.drawable.ic_dialog_email)
                .setTitle(R.string.contact_us_menu_item)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new SimpleOkayButtonListener())
                .show();
    }

    private void showViewDocs()
    {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.view_docs, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.view_docs_menu)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, new SimpleOkayButtonListener())
                .show();
    }

    private void exportPublicInfo(File exportFile) throws IOException,
            StreamableBase64.InvalidBase64Exception,
            MartusCrypto.MartusSignatureException {
        MartusUtilities.exportClientPublicKey(getSecurity(), exportFile);
    }

    private void pingServer() {
        if (! NetworkUtilities.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
            return;
        }
        showProgressDialog(getString(R.string.progress_connecting_to_server));
        try {
            String pingUrl = "http://" + serverIP + RPC2_PATH;
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(pingUrl));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            final AsyncTask<XmlRpcClient, Void, String> pingTask = new PingTask();
            pingTask.execute(client);
        } catch (MalformedURLException e) {
            // do nothing
        }
    }

    private void resendFailedBulletins()
    {
        int count = getNumberOfUnsentBulletins();
        if (count < 1) {
            Toast.makeText(this, getString(R.string.resending_no_bulletins), Toast.LENGTH_LONG).show();
            return;
        }
        if (!NetworkUtilities.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.resending_no_network), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.resending), Toast.LENGTH_LONG).show();
        Intent resendService = new Intent(AbstractMainActivityWithMainMenuHandler.this, ResendService.class);
        resendService.putExtra(SettingsActivity.KEY_SERVER_IP, serverIP);
        resendService.putExtra(SettingsActivity.KEY_SERVER_PUBLIC_KEY, serverPublicKey);
        startService(resendService);
    }

    protected int getNumberOfUnsentBulletins() {
        int pendingBulletins;
        final File unsentBulletinsDir = getAppDir();
        final String[] sendingBulletinNames = unsentBulletinsDir.list(new ZipFileFilter());
        pendingBulletins = sendingBulletinNames.length;

        File failedDir = new File (unsentBulletinsDir, UploadBulletinTask.FAILED_BULLETINS_DIR);
        if (failedDir.exists()) {
            final String[] failedBulletins = failedDir.list(new ZipFileFilter());
            pendingBulletins += failedBulletins.length;
        }
        return pendingBulletins;
    }

    private void processPingResult(String result) {
        dismissProgressDialog();
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private void processResult(NetworkResponse response) {
        try
        {
            if (response == null) {
                showErrorMessage(getString(R.string.error_occured), getString(R.string.error_message));
                return;
            }

            dismissProgressDialog();
            if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
            {
                if(response.getResultCode().equals(NetworkInterfaceConstants.NO_TOKEN_AVAILABLE))
                    throw new MartusAccountAccessToken.TokenNotFoundException();

                throw new Exceptions.ServerNotAvailableException();
            }

            Vector<String> resultVector = response.getResultVector();
            if (resultVector == null || resultVector.isEmpty()){
                Log.e(AppConfig.LOG_LABEL, "Server response was empty");
                throw new Exception();
            }

            String accessToken = resultVector.get(0);
            showMessage(this, getString(R.string.account_access_token_label, accessToken), "");
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Exception retrieving access token", e);
            showErrorMessage(getString(R.string.error_occured), getString(R.string.error_message));
        }
    }

    @Override
    public String getConfirmationMessage() {
        if (getConfirmationType() == CONFIRMATION_TYPE_DELETE_ACCOUNT) {
            return getString(R.string.confirm_reset_install);
        }

        if (getConfirmationType() == CONFIRMATION_TYPE_LOGOUT) {
            return getString(R.string.confirm_logout);
        }

        if (getConfirmationType() == CONFIRMATION_TYPE_LOOSE_DATA) {
            return getString(R.string.confirm_data_loss);
        }

        return super.getConfirmationTitle();
    }

    @Override
    public void onConfirmationAccepted() {
        if (getConfirmationType() == CONFIRMATION_TYPE_DELETE_ACCOUNT) {
            deleteAccount();
        }

        if (getConfirmationType() == CONFIRMATION_TYPE_LOGOUT) {
            logout();
            finish();
            goToHomeScreen();
        }

        if (getConfirmationType() == CONFIRMATION_TYPE_LOOSE_DATA) {
            startSettingsAtivitys();
        }

        super.onConfirmationAccepted();
    }

    protected void goToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void deleteAccount() {
        removePacketsDir();
        clearPreferences(mySettings.edit());
        clearPreferences(getSharedPreferences(PREFS_DESKTOP_KEY, MODE_PRIVATE).edit());
        clearPreferences(getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE).edit());
        logout();
        clearPrefsDir();
        clearFailedBulletinsDir();
        clearCacheDir();
        final File unsentBulletinsDir = getAppDir();
        final String[] names = unsentBulletinsDir.list(new ZipFileFilter());
        for (String name : names) {
            File zipFile = new File(unsentBulletinsDir, name);
            zipFile.delete();
        }
        finish();
    }

    private void clearCacheDir() {
        clearDirectory(getCacheDir());
    }

    private void clearPrefsDir() {
        File prefsDirFile = new File(getAppDir(), PREFS_DIR);
        clearDirectory(prefsDirFile);
    }

    private void clearFailedBulletinsDir() {
        File prefsDirFile = new File(getAppDir(), UploadBulletinTask.FAILED_BULLETINS_DIR);
        clearDirectory(prefsDirFile);
        prefsDirFile.delete();
    }

    private void removePacketsDir() {
        File packetsDirFile = new File(getAppDir(), PACKETS_DIR);
        clearDirectory(packetsDirFile);
        packetsDirFile.delete();
    }

    private void clearPreferences(SharedPreferences.Editor editor) {
        editor.clear();
        editor.commit();
    }


    protected void setConfirmationType(int type) {
        confirmationType = type;
    }

    protected int getConfirmationType() {
        return confirmationType;
    }

    public class CancelSendButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //do nothing
        }
    }

    private class PingTask extends AsyncTask<XmlRpcClient, Void, String> {
        @Override
        protected String doInBackground(XmlRpcClient... clients) {

            final Vector params = new Vector();
            final XmlRpcClient client = clients[0];
            String result = getString(R.string.ping_result_ok);
            try {
                client.execute(SERVER_COMMAND_PREFIX + NetworkInterfaceXmlRpcConstants.CMD_PING, params);
            } catch (XmlRpcException e) {
                Log.e(AppConfig.LOG_LABEL, "Ping failed", e);
                result = getString(R.string.ping_result_down);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            processPingResult(result);
        }
    }

    private class RetrieveAccessTokenTask extends AsyncTask<Object, Void, NetworkResponse> {
        @Override
        protected NetworkResponse doInBackground(Object... params) {

            try
            {
                MobileClientSideNetworkGateway gateway = getNetworkGateway();
                MartusSecurity martusCrypto = AppConfig.getInstance().getCrypto();
                NetworkResponse response = gateway.getMartusAccountAccessToken(martusCrypto);

                return response;
            }
            catch (Exception e){
                Log.e(AppConfig.LOG_LABEL, "Server connection failed!", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(NetworkResponse result) {
            super.onPostExecute(result);

            processResult(result);
        }
    }
}
