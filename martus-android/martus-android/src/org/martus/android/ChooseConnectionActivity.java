package org.martus.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.martus.android.network.Constants;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.DefaultServerConnector;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.PassThroughTransportWrapper;
import org.martus.common.network.UploadRightsTask;

import java.io.File;
import java.util.Vector;

/**
 * Created by nimaa on 3/31/14.
 */
public class ChooseConnectionActivity extends AbstractServerActivity {

    private Button useDefaultServer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_connection);

        TextView advancedServerOptionsTextView = (TextView) findViewById(R.id.useAdvancedServerOptions);
        makeTextViewClickableHyperlink(advancedServerOptionsTextView);
    }

    public void useAdvancedServerOptions(View view) {
        Intent intent = new Intent(ChooseConnectionActivity.this, ServerActivity.class);
        startActivityForResult(intent, EXIT_REQUEST_CODE);
        finish();
    }

    public void useDefaultServer(View view) throws Exception {
        if (DefaultServerConnector.isValidServerIp(getServerIp())) {
            showErrorMessage(getString(R.string.invalid_server_ip), getString(R.string.error_message));
            return;
        }

        showProgressDialog(getString(R.string.progress_connecting_to_server));
        String serverIp = getServerIp();
        PassThroughTransportWrapper transport = ((MartusApplication) getApplication()).getTransport();
        MartusSecurity martusCrypto = AppConfig.getInstance().getCrypto();

        DefaultServerConnector.connectToServer(martusCrypto, transport, serverIp, this);
    }

    @Override
    public void onResume() {
        super.onResume();

       //NOTE need to do anything here?
    }

    private String getServerIp(){
        return Constants.getCurrentServerIp();
    }

    private String getServerPublicKey() {
        return Constants.getCurrentSeverKey();
    }

    @Override
    public void processResult(Vector serverInformation) {
        dismissProgressDialog();
        if (! NetworkUtilities.isNetworkAvailable(this)) {
            showErrorMessage(getString(R.string.no_network_connection), getString(R.string.error_message));
            return;
        }
        try {
            if (null == serverInformation || serverInformation.isEmpty()) {
                showErrorMessage(getString(R.string.invalid_server_info), getString(R.string.error_message));
                return;
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Problem getting server public key", e);
            showErrorMessage(getString(R.string.error_getting_server_key), getString(R.string.error_message));

            return;
        }

        String serverPublicKey = (String)serverInformation.get(1);
        try {
            if (getServerPublicKey().equals(serverPublicKey)) {
                SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
                SharedPreferences.Editor editor = serverSettings.edit();
                editor.putString(SettingsActivity.KEY_SERVER_IP, getServerIp());
                editor.putString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, serverPublicKey);
                editor.commit();

                SharedPreferences.Editor magicWordEditor = mySettings.edit();
                magicWordEditor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
                magicWordEditor.commit();
                Toast.makeText(this, getString(R.string.successful_server_choice), Toast.LENGTH_SHORT).show();

                File serverIpFile = getPrefsFile(PREFS_SERVER_IP);
                MartusUtilities.createSignatureFileFromFile(serverIpFile, getSecurity());

                showProgressDialog(getString(R.string.progress_confirming_magic_word));
                final AsyncTask<Object, Void, NetworkResponse> rightsTask = new UploadRightsTask(this);
                rightsTask.execute(getNetworkGateway(), martusCrypto, getMagicWord());

            } else {
                showErrorMessage(getString(R.string.invalid_server_code), getString(R.string.error_message));
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL,"problem processing server IP", e);
        }
    }

    private String getMagicWord() {
        return "martus";
    }

    @Override
    public void processMagicWordResponse(NetworkResponse response) {
        dismissProgressDialog();
        try {
            if (!response.getResultCode().equals(NetworkInterfaceConstants.OK)) {
                Toast.makeText(this, getString(R.string.no_upload_rights), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.success_magic_word), Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = mySettings.edit();
                editor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, true);
                editor.commit();
                this.finish();
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Problem verifying upload rights", e);
            Toast.makeText(this, getString(R.string.problem_confirming_magic_word), Toast.LENGTH_SHORT).show();
        }
    }
}
