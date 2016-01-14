package org.martus.android.vitalVoices.application;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.martus.android.vitalVoices.activities.MainActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.validate.FormValidator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Created by nimaa on 7/18/14.
 */
public class MainApplication extends Collect {

    private static final String XFORMS_DIR_NAME = "xforms";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Vector<String> formPaths = writeFormsToDiskFromAssestsFolder();
            if (formPaths.size() != 1)
                throw new RuntimeException("Incorrect number of forms found in assets folder!");

            String formPath = formPaths.firstElement();
            File formFile = new File(formPath);
            if (!formFile.exists())
                throw new RuntimeException("Form file could not be found = " + formPath);

            validateXForm(formPath);

            Intent intent = new Intent(this, MainActivity.class);
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, 1);
            intent.setData(formUri);

            Cursor cursor = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);

            //getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);

            ContentValues formPathValues = new ContentValues();
            formPathValues.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, formPath);
            formPathValues.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, "1");
            getContentResolver().insert(FormsProviderAPI.FormsColumns.CONTENT_URI, formPathValues);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Cursor instanceCursor = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);
            System.out.println("------------------------------------" + instanceCursor.getCount());
            if (instanceCursor.getCount() != 1) {
                System.out.println("------------------------------------INCORRECT NUMBER IF INSTANCE CURSORS!");
                return;
            }

            startActivity(intent);

        } catch (Exception e) {
            Log.e(Constants.LOG_LABEL, "problem finding form file", e);
        }
    }

    private void validateXForm(String formPath) {
        new FormValidator(formPath);
    }

    private Vector<String> writeFormsToDiskFromAssestsFolder() throws Exception {
        String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String basepath = externalStorageDirectory + "/secureApp";

        File xformsDir = new File(basepath + "/" + XFORMS_DIR_NAME + "/");
        xformsDir.mkdirs();
        return copyXFormsToExternalStorage(basepath);
    }

    private Vector<String> copyXFormsToExternalStorage(String basepath) throws Exception {
        AssetManager assetManager = getResources().getAssets();
        String[] files = assetManager.list(XFORMS_DIR_NAME);
        Vector<String> copiedToPaths = new Vector<String>();
        for(int index = 0; index < files.length; ++index) {
            String fileNameToCopy = files[index];
            String copyToPath = copyFile(assetManager, basepath, fileNameToCopy);
            copiedToPaths.add(copyToPath);
        }

        return copiedToPaths;
    }

    private String copyFile(AssetManager assetManager, String basepath, String fileNameToCopy) throws IOException {
        InputStream in = assetManager.open(XFORMS_DIR_NAME + "/" + fileNameToCopy);
        String copyToPath = basepath + "/" + XFORMS_DIR_NAME + "/" + fileNameToCopy + ".xml";
        OutputStream out = new FileOutputStream(copyToPath);
        try {
            copyStream(in, out);
        }
        finally {
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
        }

        return copyToPath;
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}