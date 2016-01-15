package org.benetech.secureapp.activities;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.benetech.secureapp.MartusUploadManager;
import org.benetech.secureapp.R;
import org.benetech.secureapp.application.AppConfig;
import org.benetech.secureapp.tasks.UploadBulletinTask;
import org.benetech.secureapp.tasks.ZipBulletinTask;
import org.martus.android.library.common.dialog.DeterminateProgressDialog;
import org.martus.android.library.common.dialog.IndeterminateProgressDialog;
import org.martus.android.library.exceptions.XFormsConstraintViolationException;
import org.martus.android.library.exceptions.XFormsMissingRequiredFieldException;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.utilities.BulletinSender;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusKeyPair;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MobileMartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.UniversalId;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.util.zip.ZipFile;

/**
 * @author roms Date: 10/25/12
 *         animal@martus.org 9/20/2014
 */

public class BulletinActivity extends AbstractBulletinCreator implements BulletinSender {

    private static final String TAG = "BulletinActivity";

    private void zipAndUploadBulletin(Bulletin bulletin)  {
        indeterminateDialog = IndeterminateProgressDialog.newInstance();
        indeterminateDialog.show(getSupportFragmentManager(), "dlg_zipping");

        //turn off user inactivity checking during zipping and encrypting of file
        final AsyncTask<Object, Integer, File> zipTask = new ZipBulletinTask(bulletin, this);
        zipTask.execute(getApplication().getCacheDir(), store, ".zip");
    }

    @Override
    public void onZipped(Bulletin bulletin, File zippedFile) {
        try {
            ZipFile zipFile = new ZipFile(zippedFile);
            BulletinZipUtilities.validateIntegrityOfZipFilePackets(store.getAccountId(), zipFile, getSecurity());
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.error_message_error_verifying_zip_file), e);
            indeterminateDialog.dismissAllowingStateLoss();
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (zippedFile == null) {
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_SHORT).show();
            return;
        }

        uploadBulletin(bulletin, zippedFile);
    }

    private void uploadBulletin(Bulletin bulletin, File zippedFile) {
        determinateDialog = DeterminateProgressDialog.newInstance();
        try {
            determinateDialog.show(getSupportFragmentManager(), "dlg_sending");
        } catch (IllegalStateException e) {
            determinateDialog.dismissAllowingStateLoss();
            // just means user has left app - do nothing
        }

        UniversalId bulletinId = bulletin.getUniversalId();
        AsyncTask<Object, Integer, String> uploadTask = new UploadBulletinTask(getApplication(), this, bulletinId);
        MartusSecurity cryptoCopy = cloneSecurity(getSecurity());
        uploadTask.execute(bulletin.getUniversalId(), zippedFile, AppConfig.getInstance(getApplication()).getCurrentNetworkInterfaceGateway(getServerIp(), getServerPublicKey()), cryptoCopy);
    }

    @Override
    public void onSent(String result) {
        try {
            determinateDialog.dismissAllowingStateLoss();
        } catch (IllegalStateException e) {
            //this is okay as the user may have closed this screen
        }

        if (result != null && result.equals(NetworkInterfaceConstants.OK))
            deleteFormAfterSent();

        String message = getResultMessage(result, this);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void deleteFormAfterSent() {
        String instancePath = getIntent().getExtras().getString(MartusUploadManager.BULLETIN_ISTANCE_FILE_PATH_TAG);
        SecureFile secureFileToDelete = new SecureFile(instancePath);
        boolean wasDeleted = secureFileToDelete.delete();
        if (wasDeleted)
            Log.i(TAG, getString(R.string.error_message_form_was_deleted_after_upload_was_completed));
        else
            Log.i(TAG, getString(R.string.error_message_form_could_not_be_deleted));

        MainActivity.deleteOdkInstanceCacheDir(instancePath);
        removeFormFromOdkDatabase();
    }

    private void removeFormFromOdkDatabase() {
        String formId = getIntent().getExtras().getString(MartusUploadManager.BULLETIN_FORM_ID_TAG);
        Cursor cursor = getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        int deleteCount = getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                InstanceProviderAPI.InstanceColumns._ID + " = ?",
                new String[]{formId});

        Log.i(TAG,  getString(R.string.error_message_instance_form_deleted_delete_count, deleteCount));
        cursor.close();
    }

    @Override
    public void onProgressUpdate(int progress) {
        if (null != determinateDialog.getProgressDialog()) {
            determinateDialog.getProgressDialog().setProgress(progress);
        }
    }
    
    public static String getResultMessage(String result, Context context) {
        String message;
        if (result != null && result.equals(NetworkInterfaceConstants.OK)) {
            message = context.getString(R.string.successful_send_notification);
        } else {
            message = context.getString(R.string.failed_send_notification, result);
        }
        return message;
    }

    private MartusSecurity cloneSecurity(MartusSecurity original) {
        MartusSecurity cryptoCopy = null;
        try {
            MartusKeyPair keyPair = original.getKeyPair();
            byte[] data = keyPair.getKeyPairData();
            cryptoCopy = new MobileMartusSecurity();
            cryptoCopy.setKeyPairFromData(data);
            cryptoCopy.setShouldWriteAuthorDecryptableData(false);
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.error_message_problem_copying_crypto), e);
        }
        return cryptoCopy;
    }

    @Override
    public void loadingComplete(FormLoaderTask task) {
        try {
            Bulletin bulletin = createBulletin();
            addAttachmentsToBulletin(bulletin);
            zipAndUploadBulletin(bulletin);
        } catch (XFormsConstraintViolationException e) {
            handleException(e, R.string.xforms_constraint_error, getString(R.string.error_message_forms_constraint_issue_during_validation));
        } catch (XFormsMissingRequiredFieldException e) {
            handleException(e, R.string.xforms_missing_required_field, getString(R.string.error_message_xforms_required_fields_missing));
        } catch (Exception e){
            handleException(e, R.string.failure_zipping_bulletin, getString(R.string.error_message_exception_thrown_trying_to_populate_record));
            Log.e(TAG, getString(R.string.error_message_exception_thrown_trying_to_populate_record), e);
        }
    }

    private String getServerIp(){
        return getString(R.string.martus_server_ip);
    }

    private String getServerPublicKey() {
        return getString(R.string.martus_server_public_key);
    }
}