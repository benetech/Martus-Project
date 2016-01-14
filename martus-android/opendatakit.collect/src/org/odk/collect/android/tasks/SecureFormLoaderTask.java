package org.odk.collect.android.tasks;

import info.guardianproject.iocipher.File;

import java.io.FileNotFoundException;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalAnswerResolver;
import org.odk.collect.android.io.SecureFileStorageManager;
import org.odk.collect.android.utilities.FileUtils;

import android.util.Log;

/**
 * A drop-in replacement for FormLoaderTask that interfaces with 
 * {@link org.odk.collect.android.io.SecureFileStorageManager} for 
 * encrypted File I/O. Initially only the form data, not definition, 
 * will be handled by SecureFileStorageManager.
 * 
 * @author David Brodsky
 *
 */
public class SecureFormLoaderTask extends FormLoaderTask {
	private static final String TAG = "SecureFormLoaderTask";
	
	private SecureFileStorageManager mStorage;

	public SecureFormLoaderTask(SecureFileStorageManager storage, String instancePath, String XPath,
			String waitingXPath) {
		super(instancePath, XPath, waitingXPath);
		mStorage = storage;
	}
	
	/**
	 * Construct a {@link info.guardianproject.iocipher.File} 
	 * from a {@link java.io.File} to determine whether it
	 * exists on the secure filesystem. 
	 * 
	 * Note: Not all Form related files are yet stored in the secure storage,
	 * so we must be careful only to perform this behavior on the expected files.
	 */
	@Override
	protected boolean exists(java.io.File file) {
		if (file.getAbsolutePath().contains("instances")) {
			return new File(file.getAbsolutePath()).exists();
		}
		return super.exists(file);
	}
	
	@Override
	protected byte[] readFileToByteArray(java.io.File file) {
		try {
			return mStorage.readFile(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Unable to open file " + file.getAbsolutePath());
			// TODO: What to do here?
			e.printStackTrace();
		}
		return null;
	}


}
