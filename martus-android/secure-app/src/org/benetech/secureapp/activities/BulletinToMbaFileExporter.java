package org.benetech.secureapp.activities;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.benetech.secureapp.R;
import org.benetech.secureapp.tasks.ZipBulletinTask;
import org.martus.android.library.common.dialog.IndeterminateProgressDialog;
import org.martus.android.library.exceptions.XFormsConstraintViolationException;
import org.martus.android.library.exceptions.XFormsMissingRequiredFieldException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.odk.collect.android.tasks.FormLoaderTask;

import java.io.File;
import java.util.zip.ZipFile;

/**
 * Created by animal@martus.org on 12/22/15.
 */
public class BulletinToMbaFileExporter extends AbstractBulletinCreator {

    private static final String TAG = "BulletinToMbaFileExporter";

    @Override
    public void loadingComplete(FormLoaderTask formLoaderTask) {
        try {
            Bulletin bulletin = createBulletin();
            addAttachmentsToBulletin(bulletin);
            zipBulletin(bulletin);
        } catch (XFormsConstraintViolationException e) {
            handleException(e, R.string.xforms_constraint_error, getString(R.string.error_message_forms_constraint_issue_during_validation));
        } catch (XFormsMissingRequiredFieldException e) {
            handleException(e, R.string.xforms_missing_required_field, getString(R.string.error_message_xforms_required_fields_missing));
        } catch (Exception e){
            handleException(e, R.string.failure_zipping_bulletin, getString(R.string.error_message_exception_thrown_trying_to_populate_record));
            Log.e(TAG, getString(R.string.error_message_exception_thrown_trying_to_populate_record), e);
        }
    }

    private void zipBulletin(Bulletin bulletin)  {
        indeterminateDialog = IndeterminateProgressDialog.newInstance();
        indeterminateDialog.show(getSupportFragmentManager(), "dlg_zipping");

        //turn off user inactivity checking during zipping and encrypting of file
        final AsyncTask<Object, Integer, File> zipTask = new ZipBulletinTask(bulletin, this);
        zipTask.execute(getApplication().getCacheDir(), store, ".mba");
    }

    @Override
    public void onZipped(Bulletin bulletin, File zippedFile) {
        try {
            final File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            final String fileName = getApplicationName() + "_" + bulletin.toFileName() + MBA_FILE_EXTENSION;
            final File destinationFile = new File(externalStoragePublicDirectory, fileName);
            FileUtils.copyFile(zippedFile, destinationFile);

            ZipFile zipFile = new ZipFile(zippedFile);
            BulletinZipUtilities.validateIntegrityOfZipFilePackets(store.getAccountId(), zipFile, getSecurity());
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.error_message_error_verifying_zip_file), e);
            indeterminateDialog.dismissAllowingStateLoss();
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        indeterminateDialog.dismissAllowingStateLoss();
        if (zippedFile == null) {
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_SHORT).show();
            return;
        }

        finish();
    }

    private String getApplicationName() {
        int stringId = getApplication().getApplicationInfo().labelRes;

        return getApplicationContext().getString(stringId);
    }

    @Override
    public void onProgressUpdate(int i) {

    }
}
