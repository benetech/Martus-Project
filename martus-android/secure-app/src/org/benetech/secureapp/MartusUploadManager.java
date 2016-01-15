package org.benetech.secureapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.benetech.secureapp.activities.BulletinActivity;
import org.benetech.secureapp.activities.Util;
import org.benetech.secureapp.application.AppConfig;
import org.benetech.secureapp.application.MainApplication;
import org.benetech.secureapp.tasks.CreateMartusCryptoKeyPairTask;
import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.DefaultServerConnector;
import org.martus.common.network.NetwordResponseHander;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NetworkUtilities;
import org.martus.common.network.PassThroughTransportWrapper;
import org.martus.common.network.PublicKeyTaskPostExecuteHandler;
import org.martus.common.network.UploadRightsTask;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.Vector;

import info.guardianproject.cacheword.CacheWordHandler;

/**
 * Wraps all the necessary steps of transmitting a form to Martus. This provides
 * a simple API for the multi-step Martus transmission, and allows Activities to present
 * a single progress dialog spanning the entire operation instead of one for each sub task.
 *
 * Usage: After construction call {@link #uploadCurrentInstanceForm(info.guardianproject.cacheword.CacheWordHandler)}
 * to transmit the current Form instance as reported by the {@link org.odk.collect.android.logic.FormController},
 * or alternatively pass a {@link android.database.Cursor} representing a Form instance to
 * {@link #uploadInstanceFormFromCursor(android.database.Cursor, info.guardianproject.cacheword.CacheWordHandler)}
 *
 * To be notified of success or error, call
 * {@link #setMartusUploadCallback(MartusUploadManager.MartusUploadManagerCallback)}
 *
 * Mechanism: Upon a call to {@link #uploadCurrentInstanceForm(info.guardianproject.cacheword.CacheWordHandler)}
 * or {@link #uploadInstanceFormFromCursor(android.database.Cursor, info.guardianproject.cacheword.CacheWordHandler)}
 * the following operations are performed in order:
 * <ol>
 *     <li>CreateMartusCryptoKeyPairTask, if no Keypair has been generated</li>
 *     <li>PublicKeyTask</li>
 *     <li>UploadRightsTask</li>
 *     <li>BulletinActivity started to handle final upload. Note: BulletinActivity should probably be made
 *     part of this class, as it doesn't seem right to dedicate an entire Activity to what could be a background process</li>
 * </ol>
 * Created by davidbrodsky on 10/3/14.
 */
public class MartusUploadManager {
    public static final String TAG = "MartusUploadManager";
    public static final String BULLETIN_DISPLAY_NAME_TAG = "bulletin_display_name";
    public static final String BULLETIN_SUB_DISPLAY_NAME_TAG = "bulletin_sub_display_name";
    public static final String BULLETIN_ISTANCE_FILE_PATH_TAG = "instance_file_path";
    public static final String BULLETIN_FORM_ID_TAG = "form_id";
    public static final String BULLETIN_AUTHOR_TAG = "bulletin_author_name";
    public static final String BULLETIN_ORGANIZATION_TAG = "bulletin_organization_name";

    private Activity mHostActivity;
    private MartusUploadManagerCallback mCallback;
    private Cursor mFormToUpload;
    private CacheWordHandler mCacheWordActivityHandler;

    public interface MartusUploadManagerCallback {
        public static enum MartusError {

            /** CreateMartusCryptoKeyPairTask failure */
            CREATE_KEYPAIR_FAILURE,

            /** DefaultServerConnector.isValidServerIp(getServerIp() false */
            INVALID_DEFAULT_SERVER_IP,

            /** No network connection */
            NO_NETWORK,

            /** PublicKeyTaskPostExecuteHandler got no server info */
            INVALID_SERVER_RESPONSE,

            /** PublicKeyTaskPostExecuteHandler
             * experiences !getServerPublicKey().equals(serverPublicKey) */
            INVALID_SERVER_PUB_KEY
        }

        // TODO: Make BulletinActivity return result so we
        // can call onMartusUploadSuccess() after actual upload complete
        // not transmission start
        public void onMartusUploadSuccess();
        public void onMartusUploadError(MartusError error);

    }

    public MartusUploadManager(Activity host) {
        mHostActivity = host;
    }

    public void setMartusUploadCallback(MartusUploadManagerCallback cb) {
        mCallback = cb;
    }

    /**
     * Use the {@link org.odk.collect.android.logic.FormController} to
     * retrieve the currently active Form Instance, and upload it to Martus
     */
    public void uploadCurrentInstanceForm(CacheWordHandler cacheWordActivityHandler) {
        mCacheWordActivityHandler = cacheWordActivityHandler;
        // TODO: Move Util or extract method
        Cursor instanceForm = Util.getFormCursorForCurrentInstance(mHostActivity);
        uploadInstanceFormFromCursor(instanceForm, mCacheWordActivityHandler);
    }

    public void uploadInstanceFormFromCursor(Cursor form, CacheWordHandler cacheWordActivityHandler) {
        mFormToUpload = form;
        uploadForm(mHostActivity, mFormToUpload);
    }

    private MartusSecurity getMartusCrypto(Application app) {
        return AppConfig.getInstance(app).getCrypto();
    }

    /** Callback for CreateMartusCryptoKeyPairTask */
    private CreateMartusCryptoKeyPairTask.CreateMartusCryptoKeyPairCallback mCreateMartusCryptoKeyPairCallback = new CreateMartusCryptoKeyPairTask.CreateMartusCryptoKeyPairCallback() {
        @Override
        public void onCreateKeyPairError() {
            if (mCallback != null) {
                mCallback.onMartusUploadError(MartusUploadManagerCallback.MartusError.CREATE_KEYPAIR_FAILURE);
            }
        }

        @Override
        public void onCreateKeyPairSuccess() {
            connectToDefaultServer();
        }
    };

    private void connectToDefaultServer() {
        Application app = mHostActivity.getApplication();
        if (DefaultServerConnector.isValidServerIp(getServerIp())) {
            if (mCallback != null) {
                mCallback.onMartusUploadError(MartusUploadManagerCallback.MartusError.INVALID_DEFAULT_SERVER_IP);
            }
            return;
        }

        String serverIp = getServerIp();
        PassThroughTransportWrapper transport = AppConfig.getInstance(app).getTransport();
        MartusSecurity martusCrypto = getMartusCrypto(app);

        DefaultServerConnector.connectToServer(martusCrypto, transport, serverIp, mPublicKeyTaskPostExecuteHandler);
    }

    private String getServerIp(){
        return mHostActivity.getString(R.string.martus_server_ip);
    }

    private PublicKeyTaskPostExecuteHandler mPublicKeyTaskPostExecuteHandler = new PublicKeyTaskPostExecuteHandler() {
        @Override
        public void processResult(Vector serverInformation) {
            if (! NetworkUtilities.isNetworkAvailable(mHostActivity)) {
                if (mCallback != null) {
                    mCallback.onMartusUploadError(MartusUploadManagerCallback.MartusError.NO_NETWORK);
                }
                return;
            }
            if (null == serverInformation || serverInformation.isEmpty()) {
                if (mCallback != null) {
                    mCallback.onMartusUploadError(MartusUploadManagerCallback.MartusError.INVALID_SERVER_RESPONSE);
                }
                return;
            }

            String serverPublicKey = (String)serverInformation.get(1);
            try {
                if (getServerPublicKey().equals(serverPublicKey)) {
//                SharedPreferences serverSettings = getSharedPreferences(PREFS_SERVER_IP, MODE_PRIVATE);
//                SharedPreferences.Editor editor = serverSettings.edit();
//                editor.putString(SettingsActivity.KEY_SERVER_IP, getServerIp());
//                editor.putString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, serverPublicKey);
//                editor.commit();

                    // SharedPreferences.Editor magicWordEditor = mySettings.edit();
                    // magicWordEditor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, false);
                    // magicWordEditor.commit();
//                    showShortToast(this, getString(R.string.successful_server_choice));

                    //File serverIpFile = getPrefsFile(PREFS_SERVER_IP);
                    //MartusUtilities.createSignatureFileFromFile(serverIpFile, AppConfig.getInstance().getCrypto());

//                    showProgressDialog(getString(R.string.progress_confirming_upload_rights));
                    final AsyncTask<Object, Void, NetworkResponse> rightsTask = new UploadRightsTask(mNetwordResponseHandler);
                    rightsTask.execute(getNetworkGateway(), getMartusCrypto(mHostActivity.getApplication()), DefaultServerConnector.DEFAULT_SERVER_MAGIC_WORD);

                } else {
                    if (mCallback != null) {
                        mCallback.onMartusUploadError(MartusUploadManagerCallback.MartusError.INVALID_SERVER_PUB_KEY);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_problem_processing_server_ip), e);
            }
        }
    };

    private String getServerPublicKey() {
        return mHostActivity.getString(R.string.martus_server_public_key);
    }

    private MobileClientSideNetworkGateway getNetworkGateway() {
        return AppConfig.getInstance(mHostActivity.getApplication()).getCurrentNetworkInterfaceGateway(getServerIp(), getServerPublicKey());
    }

    private NetwordResponseHander mNetwordResponseHandler = new NetwordResponseHander() {
        @Override
        public void processMagicWordResponse(NetworkResponse response) {
            if (response == null) {
                showShortToast(mHostActivity, mHostActivity.getString(R.string.error_establishing_upload_rights));
                return;
            }

            try {
                if (!response.getResultCode().equals(NetworkInterfaceConstants.OK)) {
                    showShortToast(mHostActivity, mHostActivity.getString(R.string.no_upload_rights));
                } else {
                    showShortToast(mHostActivity, mHostActivity.getString(R.string.label_server_connection_setup_successful));
//                SharedPreferences.Editor editor = mySettings.edit();
//                editor.putBoolean(SettingsActivity.KEY_HAVE_UPLOAD_RIGHTS, true);
//                editor.commit();
                  uploadForm(mHostActivity, mFormToUpload);
                }
            } catch (Exception e) {
                Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_problem_verifying_upload_rights), e);
                showShortToast(mHostActivity, mHostActivity.getString(R.string.error_connecting_to_server));
            }
        }
    };

    private void uploadForm(Activity activity, Cursor form) {
        // Ensure our Cursor is positioned on the first, and only, element
        form.moveToFirst();

        int formLabelColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
        String formDisplayName = form.getString(formLabelColumnIndex);

        int displaySubTextColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT);
        String displaySubText = form.getString(displaySubTextColumnIndex);

        int instancefilepathColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
        String instanceFilepath = form.getString(instancefilepathColumnIndex);

        int formIdColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
        String formId = form.getString(formIdColumnIndex);

        int formAuthorColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_AUTHOR);
        String authorName = form.getString(formAuthorColumnIndex);

        int formOrganizationColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_ORGANIZATION);
        String organizationName = form.getString(formOrganizationColumnIndex);

        Intent intent = new Intent(activity, BulletinActivity.class);
        intent.putExtra(BULLETIN_DISPLAY_NAME_TAG, formDisplayName);
        intent.putExtra(BULLETIN_SUB_DISPLAY_NAME_TAG, displaySubText);
        intent.putExtra(BULLETIN_ISTANCE_FILE_PATH_TAG, instanceFilepath);
        intent.putExtra(BULLETIN_FORM_ID_TAG, formId);
        intent.putExtra(BULLETIN_AUTHOR_TAG, authorName);
        intent.putExtra(BULLETIN_ORGANIZATION_TAG, organizationName);
        activity.startActivity(intent);
        form.close();
        if (mCallback != null) {
            mCallback.onMartusUploadSuccess();
        }
    }

    private void showShortToast(Context contenxt, String message) {
        Toast.makeText(contenxt, message, Toast.LENGTH_SHORT).show();
    }
}
