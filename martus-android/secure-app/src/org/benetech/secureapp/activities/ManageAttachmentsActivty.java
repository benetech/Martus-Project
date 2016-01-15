package org.benetech.secureapp.activities;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.ipaulpro.afilechooser.FileChooserActivity;

import org.benetech.secureapp.application.MainApplication;
import org.martus.android.library.io.SecureFile;
import org.benetech.secureapp.utilities.Utility;
import org.benetech.secureapp.R;
import org.benetech.secureapp.adapters.AttachmentAdapter;
import org.benetech.secureapp.adapters.AttachmentAdapterItemClickListener;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import info.guardianproject.iocipher.camera.StillCameraActivity;

/**
 * Created by animal@martus.org on 4/28/15.
 */
public class ManageAttachmentsActivty extends ListActivity implements AttachmentAdapterItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ManageAttachmentsActivty";
    private static final int REQUEST_TAKE_PICTURE = 1000;
    private static final int REQUEST_ATTACH_FILE = 1010;
    private static final int REQUEST_GALLARY_ATTACHMENT = 1020;

    private String mCurrentFormId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentFormId = getIntent().getStringExtra(MainActivity.CURRENT_FORM_ID_TAG);

        setContentView(R.layout.attachments_landing_page);

        initializeAttachmentsListAdapter();
    }

    private void initializeAttachmentsListAdapter() {
        ArrayList<String> attachmentFileNames = getAttachmentFileNames();
        AttachmentAdapter adapter = new AttachmentAdapter(getLayoutInflater(), attachmentFileNames, this);
        setListAdapter(adapter);
    }

    public void onStartAddingAttachments(View view) {
        try {
            Intent intent = new Intent(this, FileChooserActivity.class);
            startActivityForResult(intent, REQUEST_ATTACH_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.failure_choosing_file), Toast.LENGTH_LONG).show();
        }
    }

    public void onStartStillCamera(View view) {

        Intent intent = new Intent(this, StillCameraActivity.class);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        SecureFile attachmentsDir = getAttachmentsDir();
        intent.putExtra("basepath", attachmentsDir.getAbsolutePath());
        intent.putExtra("selfie", hasOnlySelfieCamera());

        startActivityForResult(intent, REQUEST_TAKE_PICTURE);
    }

    public void onStartShowGallary(View view) {
        Intent gallaryIntent = new Intent(this, SecureGallery.class);
        gallaryIntent.putExtra(SecureGallery.SECURE_GALLERY_PATH_TAG, getGallaryDir().getAbsolutePath());
        startActivityForResult(gallaryIntent, REQUEST_GALLARY_ATTACHMENT);
    }

    private boolean hasOnlySelfieCamera() {
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return false;

        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLARY_ATTACHMENT) {
            if (resultCode == RESULT_OK) {
                String[] imageFilePaths = data.getExtras().getStringArray(MediaStore.EXTRA_OUTPUT);
                for (String imagePath : imageFilePaths) {
                    SecureFile imageFile = new SecureFile(imagePath);
                    copyImageToAttachmentsDir(imageFile);

                }
                updateListAdapter();
            }
        }
        if (requestCode == REQUEST_TAKE_PICTURE) {
             if (resultCode == RESULT_OK){
                String[] imageFileNames = data.getExtras().getStringArray(MediaStore.EXTRA_OUTPUT);
                if (imageFileNames == null)
                    return;

                getCurrentForm().moveToFirst();
                ((MainApplication) getApplication()).getMountedSecureStorage().getXFormsDir();

                for (String imageFileName : imageFileNames) {
                    SecureFile imageFile = new SecureFile(imageFileName);
                    copyImageToGallaryDir(imageFile);
                    Uri uri = Uri.parse(imageFile.getAbsolutePath());
                    String mimeType = "image/*";
                    data.setDataAndType(uri, mimeType);
                    data.putExtra(Intent.EXTRA_STREAM, uri);
                }
                 setResult(resultCode, data);
                 updateListAdapter();
            }
        }

        if (requestCode == REQUEST_ATTACH_FILE) {
            if (resultCode == RESULT_OK) {
                if (data != null)
                    copySelectedFileToSecureAttachmentsDir(data);
            }
        }
    }

    private void copyImageToGallaryDir(SecureFile imageFile) {
        copyFileToDir(imageFile, getGallaryDir());
    }

    private void copyImageToAttachmentsDir(SecureFile imageFile) {
        copyFileToDir(imageFile, getAttachmentsDir());
    }

    private void copyFileToDir(SecureFile fileToCopy, SecureFile parentDir) {
        try {
            Utility.copySecureFile(fileToCopy, new SecureFile(parentDir, fileToCopy.getName()));
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.error_message_could_not_copy_image_to_gallary), e);
        }
    }

    private void copySelectedFileToSecureAttachmentsDir(Intent data) {
        try {
            Uri uri = data.getData();
            String filePath = com.ipaulpro.afilechooser.utils.FileUtils.getPath(getApplicationContext(), uri);
            if (null != filePath) {
                File unsecureFileToMoveToAttachmentsDir = new File(filePath);
                File unsecureAttachmentFile = new File(getAttachmentsDir(), unsecureFileToMoveToAttachmentsDir.getName());
                FileInputStream fileInputStream = new FileInputStream(unsecureFileToMoveToAttachmentsDir);
                ((MainApplication) getApplication()).getMountedSecureStorage().writeFile(unsecureAttachmentFile.getAbsolutePath(), fileInputStream);

                updateListAdapter();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateListAdapter() {
        ArrayList<String> attachmentFileNames = getAttachmentFileNames();
        ((AttachmentAdapter)getListAdapter()).updateAttachmentFiles(attachmentFileNames);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";
        return new CursorLoader(this,
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                sortOrder);
    }

    public void onDeleteRequested(String attachmentFileName) {
        SecureFile attachmentsDir = getAttachmentsDir();
        SecureFile attachmentFileToDelete = new SecureFile(attachmentsDir, attachmentFileName);
        boolean wasAttachmentFileDeleted = attachmentFileToDelete.delete();
        if (wasAttachmentFileDeleted) {
            updateListAdapter();
            return;
        }

        Toast.makeText(this, getString(R.string.could_not_delete_attachment, attachmentFileName), Toast.LENGTH_SHORT).show();
    }

    private Cursor getCurrentForm() {
        Cursor cursor = getContentResolver().query(
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                null,
                InstanceProviderAPI.InstanceColumns._ID + " = " + mCurrentFormId,
                null,
                null);

        cursor.moveToFirst();

        return cursor;
    }

    private ArrayList<String> getAttachmentFileNames() {
        SecureFile attachmentDir = getAttachmentsDir(getCurrentForm());
        SecureFile[] attachmentFiles = attachmentDir.listFiles();
        ArrayList<String> attachmentFileNames = new ArrayList<String>();
        for (SecureFile attachmentFile : attachmentFiles) {
            attachmentFileNames.add(attachmentFile.getName());
        }

        return attachmentFileNames;
    }

    private SecureFile getAttachmentsDir() {
        return getAttachmentsDir(getCurrentForm());
    }

    private static SecureFile getAttachmentsDir(Cursor currentFormCursor) {
        int instancefilepathColumnIndex = currentFormCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
        String instanceFilepath = currentFormCursor.getString(instancefilepathColumnIndex);

        return getAttachmentsDir(instanceFilepath);
    }

    public static SecureFile getAttachmentsDir(String instanceFilepath) {
        SecureFile instanceFile = new SecureFile(instanceFilepath);
        SecureFile parentDir = instanceFile.getParentFile();

        return new SecureFile(parentDir, MainActivity.ATTACHMENTS_FOLDER_NAME);
    }

    private SecureFile getGallaryDir() {
        return ((MainApplication)getApplication()).getGallaryDir();
    }
}
