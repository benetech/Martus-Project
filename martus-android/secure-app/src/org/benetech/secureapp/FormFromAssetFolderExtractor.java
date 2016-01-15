package org.benetech.secureapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;

import org.benetech.secureapp.application.MainApplication;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileInputStream;
import org.benetech.secureapp.utilities.Utility;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.validate.FormValidator;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by animal@martus.org on 9/5/14.
 */
public class FormFromAssetFolderExtractor {

    public static final String XFORMS_ASSETS_DIR_NAME = "xforms";
    private static final String TAG = "FormFromAssetFE";
    private MainApplication mApplication;

    public FormFromAssetFolderExtractor(MainApplication application) throws IllegalStateException {
        mApplication = application;

        if (!getMountedSecureStorage().isFilesystemMounted())
            throw new IllegalStateException(mApplication.getString(R.string.error_message_secure_file_storage_not_mounted));
    }
    
    /**
     * Return the relative path of the first xform asset
     * e.g: "xforms/sample.xml"
     * @throws IOException
     */
    private String relativePathForFormAsset() throws IOException {
    	AssetManager assetManager = getResources().getAssets();
        String[] files = assetManager.list(XFORMS_ASSETS_DIR_NAME);
        if (files.length != 1)
        	throw new RuntimeException(getApplication().getString(R.string.error_message_incorrect_number_of_forms_copied));
        return SecureFile.separator + XFORMS_ASSETS_DIR_NAME + SecureFile.separator + files[0];
    }
    
    public String getXFormsModelAsString() throws Exception {
        SecureFile formFile = copyFormFilesFromAssetsFolderToSecureStorage();

        return Utility.ioCipherFileToString(formFile);
    }
    
    public String extractXForm() {
        SecureFile formFile = copyFormFilesFromAssetsFolderToSecureStorage();
        try {

            byte[] readToVerify = getMountedSecureStorage().readFile(formFile.getAbsolutePath());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(readToVerify);
            validateXForm(byteArrayInputStream);
		} catch (FileNotFoundException e) {
			Log.e(TAG, getApplication().getString(R.string.error_message_unable_to_validate_form_not_found_in_filesystem, formFile.getAbsolutePath()), e);
		} catch (Exception e) {
            Log.e(TAG, getApplication().getString(R.string.error_message_failed_to_read_secure_file, formFile.getAbsolutePath()), e);
        }
        return formFile.getAbsolutePath();
    }

    private void validateXForm(InputStream form) {
        new FormValidator().validate(form);
    }

    private SecureFile copyFormFilesFromAssetsFolderToSecureStorage() {
        try {
            writeFormsToSecureStorageFromAssetsFolder();
            SecureFile[] formFiles = getXFormsDir().listFiles();
            if (formFiles.length != 1)
                throw new RuntimeException(getApplication().getString(R.string.error_message_incorrect_number_of_form_files_found, formFiles.length));

            SecureFile formFile = formFiles[0];
            if (!formFile.exists())
                throw new RuntimeException(getApplication().getString(R.string.error_message_form_file_could_not_be_found, formFile.getAbsolutePath()));

            return formFile;

        } catch (IOException e) {
            Log.e(TAG, getApplication().getString(R.string.error_message_error_handling_form_file), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, getApplication().getString(R.string.error_message_error_copying_from_file), e);
            return null;
        }
    }

    private void writeFormsToSecureStorageFromAssetsFolder() throws Exception {
        AssetManager assetManager = getResources().getAssets();
        String[] formNames = assetManager.list(SecureFileStorageManager.XFORMS_DIR_NAME);
        for(int index = 0; index < formNames.length; ++index) {
            String formNameToCopy = formNames[index];
            copyForm(assetManager, formNameToCopy);
        }
    }

    private void copyForm(AssetManager assetManager, String fileNameToCopy) throws Exception {
        InputStream in = assetManager.open(SecureFileStorageManager.XFORMS_DIR_NAME + "/" + fileNameToCopy);

        SecureFile xFormDir = getXFormsDir();

        SecureFile xFormFile = new SecureFile(xFormDir, fileNameToCopy);
        if (!isAssetFormOnSecureDisk(xFormFile.getAbsolutePath())) {
            Log.i(TAG, "Extracted form  to: " + xFormFile.getAbsolutePath());
        } else {
            Log.i(TAG, "Form already exists, replacing with: " + fileNameToCopy);
        }

       getMountedSecureStorage().writeFile(xFormFile.getAbsolutePath(), in);

        byte[] readToVerify = getMountedSecureStorage().readFile(xFormFile.getAbsolutePath());
        if (readToVerify.length == 0)
            throw new RuntimeException(getApplication().getString(R.string.error_message_form_file_from_assets_folder_was_not_copied_to_secure_storage));
        if (!isAssetFormInContentProvider(xFormFile.getAbsolutePath())) {
            registerFormWithFormsProvider(xFormFile);
            Log.i(TAG, "Registered sample.xml with ContentProvider");
        } else {
        	Log.i(TAG, "sample.xml already registered with ContentProvider");
        }
    }
    
    private void registerFormWithFormsProvider(SecureFile formFile) {
    	// Insert a record for the form into our ContentProvider
        // Get the Asset form's properties, notably the formId, which we need to create
        // an entry within FormsProvider
		InputStream assetFormInputStream;
		try {
			assetFormInputStream = new SecureFileInputStream(formFile);
			HashMap<String, String> xmlMap = FileUtils.parseXML(assetFormInputStream);
			if (!xmlMap.containsKey(FileUtils.FORMID)) {
				throw new IllegalStateException(getApplication().getString(R.string.error_message_bundled_Asset_form_does_not_contain_any_formId_information));
			}
			String formId = xmlMap.get(FileUtils.FORMID);
			
	        // Insert a record for the form into FormProvider
	    	ContentValues formPathValues = new ContentValues();
	        formPathValues.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, formFile.getAbsolutePath());
	        formPathValues.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formId);
	        Log.i(TAG, "Pre-insert to FormsProvider");
	        getContentResolver().insert(FormsProviderAPI.FormsColumns.CONTENT_URI, formPathValues); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Returns whether the given asset has been copied into our secure filesystem
     * TODO: The definitive check for a file's existence should be a check of both the content provider
     * and the virtual secure filesystem.
     * 
     * @param assetFormRelativePath the relative asset path returned by {@link android.content.res.AssetManager}, 
     */
    private boolean isAssetFormInContentProvider(String assetFormRelativePath) {
    	Cursor result = null;
    	try {
    		result = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, 
					new String[] { FormsProviderAPI.FormsColumns._ID }, 
					FormsProviderAPI.FormsColumns.FORM_FILE_PATH + "= ?", 
					new String[] { assetFormRelativePath }, 
					null);

    		Log.i("ASSET", "asset query " + assetFormRelativePath + " exists: " + result.getCount());
    		return (result != null && result.getCount() == 1);
    	} finally {
    		if (result != null) result.close();
    	}
    }
    
    private boolean isAssetFormOnSecureDisk(String assetFormRelativePath) {
    	boolean exists = new SecureFile(assetFormRelativePath).exists();
    	Log.i("ASSET", "file " + assetFormRelativePath + " exists: " + exists);
    	return exists;
    }

    private SecureFile getXFormsDir() {
        return getMountedSecureStorage().getXFormsDir();
    }

    private ContentResolver getContentResolver() {
        return getApplication().getContentResolver();
    }

    private MainApplication getApplication() {
        return mApplication;
    }

    private Resources getResources() {
        return getApplication().getResources();
    }

    private SecureFileStorageManager getMountedSecureStorage() {
        return getApplication().getMountedSecureStorage();
    }
}
