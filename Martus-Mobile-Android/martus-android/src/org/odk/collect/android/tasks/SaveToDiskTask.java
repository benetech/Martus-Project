/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.martus.android.AppConfig;
import org.martus.android.MartusApplication;
import org.martus.android.MartusCryptoFileUtils;
import org.martus.android.ODKUtils;
import org.martus.common.crypto.MartusSecurity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {
    private final static String t = "SaveToDiskTask";

    private FormSavedListener mSavedListener;
    private Boolean mSave;
    private Boolean mMarkCompleted;
    private Uri mUri;
    private String mInstanceName;
	private MartusSecurity mMartusCrypto;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;


    public SaveToDiskTask(Uri uri, Boolean saveAndExit, Boolean markCompleted, String updatedName, MartusSecurity crypto) {
        mUri = uri;
        mSave = saveAndExit;
        mMarkCompleted = markCompleted;
        mInstanceName = updatedName;
	    mMartusCrypto = crypto;
    }


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Integer doInBackground(Void... nothing) {

        FormController formController = Collect.getInstance().getFormController();

        // validation failed, pass specific failure
        int validateStatus = formController.validateAnswers(mMarkCompleted);
        if (validateStatus != FormEntryController.ANSWER_OK) {
            return validateStatus;
        }

        if (mMarkCompleted) {
        	formController.postProcessInstance();
        }

    	Collect.getInstance().getActivityLogger().logInstanceAction(this, "save", Boolean.toString(mMarkCompleted));

    	// if there is a meta/instanceName field, be sure we are using the latest value
    	// just in case the validate somehow triggered an update.
    	String updatedSaveName = formController.getSubmissionMetadata().instanceName;
    	if ( updatedSaveName != null ) {
    		mInstanceName = updatedSaveName;
    	}

    	boolean saveOutcome = exportData(mMarkCompleted);

    	// attempt to remove any scratch file
        File shadowInstance = savepointFile(formController.getInstancePath());
        if ( shadowInstance.exists() ) {
        	shadowInstance.delete();
        }

        if (saveOutcome) {
        	return mSave ? SAVED_AND_EXIT : SAVED;
        }

        return SAVE_ERROR;

    }

    private void updateInstanceDatabase(boolean incomplete, boolean canEditAfterCompleted) {

        FormController formController = Collect.getInstance().getFormController();

        // Update the instance database...
        ContentValues values = new ContentValues();
        if (mInstanceName != null) {
            values.put(InstanceColumns.DISPLAY_NAME, mInstanceName);
        }
        if (incomplete || !mMarkCompleted) {
            values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_INCOMPLETE);
        } else {
            values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_COMPLETE);
        }
        // update this whether or not the status is complete...
        values.put(InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(canEditAfterCompleted));

        // If FormEntryActivity was started with an Instance, just update that instance
        if (MartusApplication.getInstance().getContentResolver().getType(mUri) == InstanceColumns.CONTENT_ITEM_TYPE) {
            int updated = MartusApplication.getInstance().getContentResolver().update(mUri, values, null, null);
            if (updated > 1) {
                Log.w(t, "Updated more than one entry, that's not good: " + mUri.toString());
            } else if (updated == 1) {
                Log.i(t, "Instance successfully updated");
            } else {
            	Log.e(t, "Instance doesn't exist but we have its Uri!! " + mUri.toString());
            }
        } else if (MartusApplication.getInstance().getContentResolver().getType(mUri) == FormsColumns.CONTENT_ITEM_TYPE) {
            // If FormEntryActivity was started with a form, then it's likely the first time we're
            // saving.
            // However, it could be a not-first time saving if the user has been using the manual
            // 'save data' option from the menu. So try to update first, then make a new one if that
            // fails.
        	String instancePath = formController.getInstancePath().getAbsolutePath();
            String where = InstanceColumns.INSTANCE_FILE_PATH + "=?";
            String[] whereArgs = {
            		instancePath
            };
            int updated =
		            MartusApplication.getInstance().getContentResolver()
                        .update(InstanceColumns.CONTENT_URI, values, where, whereArgs);
            if (updated > 1) {
                Log.w(t, "Updated more than one entry, that's not good: " + instancePath);
            } else if (updated == 1) {
                Log.i(t, "Instance found and successfully updated: " + instancePath);
                // already existed and updated just fine
            } else {
                Log.i(t, "No instance found, creating");
                // Entry didn't exist, so create it.
                Cursor c = null;
                try {
                	// retrieve the form definition...
                	c = MartusApplication.getInstance().getContentResolver().query(mUri, null, null, null, null);
	                c.moveToFirst();
	                String jrformid = c.getString(c.getColumnIndex(FormsColumns.JR_FORM_ID));
	                String jrversion = c.getString(c.getColumnIndex(FormsColumns.JR_VERSION));
	                String formname = c.getString(c.getColumnIndex(FormsColumns.DISPLAY_NAME));
	                String submissionUri = null;
	                if ( !c.isNull(c.getColumnIndex(FormsColumns.SUBMISSION_URI)) ) {
	                	submissionUri = c.getString(c.getColumnIndex(FormsColumns.SUBMISSION_URI));
	                }

	                // add missing fields into values
	                values.put(InstanceColumns.INSTANCE_FILE_PATH, instancePath);
	                values.put(InstanceColumns.SUBMISSION_URI, submissionUri);
	                if (mInstanceName != null) {
	                    values.put(InstanceColumns.DISPLAY_NAME, mInstanceName);
	                } else {
	                    values.put(InstanceColumns.DISPLAY_NAME, formname);
	                }
	                values.put(InstanceColumns.JR_FORM_ID, jrformid);
	                values.put(InstanceColumns.JR_VERSION, jrversion);
                } finally {
                	if ( c != null ) {
                		c.close();
                	}
                }
                mUri = MartusApplication.getInstance().getContentResolver()
                			.insert(InstanceColumns.CONTENT_URI, values);
            }
        }
    }

    /**
     * Return the name of the savepoint file for a given instance.
     *
     * @param instancePath
     * @return
     */
    public static File savepointFile(File instancePath) {
        File tempDir = new File(Collect.CACHE_PATH);
        File temp = new File(tempDir, instancePath.getName() + ".save");
        return temp;
    }

    /**
     * Blocking write of the instance data to a temp file. Used to safeguard data
     * during intent launches for, e.g., taking photos.
     *
     * @return
     */
    public static String blockingExportTempData() {
        FormController formController = Collect.getInstance().getFormController();

        long start = System.currentTimeMillis();
        File temp = savepointFile(formController.getInstancePath());
        ByteArrayPayload payload;
        try {
        	payload = formController.getFilledInFormXml();
            // write out xml
            if ( exportXmlFile(payload, temp.getAbsolutePath()) ) {
            	return temp.getAbsolutePath();
            }
            return null;
        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return null;
        } finally {
        	long end = System.currentTimeMillis();
        	Log.i(t, "Savepoint ms: " + Long.toString(end - start) + " to file, " + temp.getAbsolutePath());
        }
    }

    /**
     * Write's the data to the sdcard, and updates the instances content provider.
     * In theory we don't have to write to disk, and this is where you'd add
     * other methods.
     * @param markCompleted
     * @return
     */
    private boolean exportData(boolean markCompleted) {
        FormController formController = Collect.getInstance().getFormController();

        ByteArrayPayload payload;
        try {
        	payload = formController.getFilledInFormXml();
            // write out xml
        	//String instancePath = formController.getInstancePath().getAbsolutePath();
	        String instancePath = Collect.INSTANCES_PATH + File.separator + ODKUtils.MARTUS_CUSTOM_ODK_INSTANCE;
            exportXmlFile(payload, instancePath);
	        encryptAndSignXmlFile(payload, mMartusCrypto);

        } catch (IOException e) {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * This method actually writes the xml to disk.
     * @param payload
     * @param path
     * @return
     */
    private static boolean exportXmlFile(ByteArrayPayload payload, String path) {
        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        try {
            int read = is.read(data, 0, len);
            if (read > 0) {
                // write xml file
                try {
                    // String filename = path + File.separator +
                    // path.substring(path.lastIndexOf(File.separator) + 1) + ".xml";
                	FileWriter fw = new FileWriter(path);
                	fw.write(new String(data, "UTF-8"));
                	fw.flush();
                	fw.close();
                    return true;

                } catch (IOException e) {
                    Log.e(t, "Error writing XML file");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(t, "Error reading from payload data stream");
            e.printStackTrace();
            return false;
        }

        return false;
    }

	/**
	     * This method actually writes the xml to disk.
	     * @param payload
	     * @param martusCrypto
	     * @return
	     */
	    private static boolean encryptAndSignXmlFile(ByteArrayPayload payload, MartusSecurity martusCrypto) {
		    try {

	            InputStream is = payload.getPayloadStream();

			    File encryptedDataFile = new File(new File(Collect.INSTANCES_PATH), ODKUtils.MARTUS_CUSTOM_ODK_INSTANCE);
			    File sigFile = new File(new File(Collect.INSTANCES_PATH), ODKUtils.MARTUS_CUSTOM_ODK_INSTANCE_SIG);
			    MartusCryptoFileUtils.encryptAndWriteFileAndSignatureFile(encryptedDataFile, sigFile, is, martusCrypto);
			    return true;
	        } catch (Exception e) {
				Log.e(AppConfig.LOG_LABEL, "problem saving data", e);
		    }
		    return false;
	    }


    @Override
    protected void onPostExecute(Integer result) {
        synchronized (this) {
            if (mSavedListener != null)
                mSavedListener.savingComplete(result);
        }
    }


    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }




}
