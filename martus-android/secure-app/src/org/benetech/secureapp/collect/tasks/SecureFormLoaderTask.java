package org.benetech.secureapp.collect.tasks;

import android.util.Log;

import org.benetech.secureapp.R;
import org.benetech.secureapp.application.MainApplication;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.javarosa.core.model.FormDef;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileInputStream;
import org.martus.android.library.io.SecureFileOutputStream;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A drop-in replacement for FormLoaderTask that interfaces with 
 * {@link SecureFileStorageManager} for
 * encrypted File I/O. Initially only the form data, not definition, 
 * will be handled by SecureFileStorageManager.
 * 
 * @author David Brodsky (davidpbrodsky@gmail.com)
 *
 */
public class SecureFormLoaderTask extends FormLoaderTask {
    
	private static final String TAG = "SecureFormLoaderTask";
	private SecureFileStorageManager mStorage;

	public SecureFormLoaderTask(SecureFileStorageManager storage, String instancePath, String XPath, String waitingXPath) {
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
			return new SecureFile(file.getAbsolutePath()).exists();
		}
		return super.exists(file);
	}
	
	@Override
	protected byte[] getByteArrayForFile(java.io.File file) {
		try {
			return mStorage.readFile(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_unable_to_open_file, file.getAbsolutePath()), e);
		} catch (Exception e) {
			Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_error_reading_secure_file, file.getAbsolutePath()), e);
		}

		return null;
	}
	
	protected InputStream getInputStreamForFile(java.io.File file) throws FileNotFoundException {
        return new SecureFileInputStream(new SecureFile(file.getAbsolutePath()));
	}
	
	protected OutputStream getOutputStreamForFile(java.io.File file) throws FileNotFoundException {
		return new SecureFileOutputStream(new SecureFile(file.getAbsolutePath()));
	}
	
	/**
     * Write the FormDef to the file system as a binary blog.
     *
     * @param filepath path to the form file
     */
	@Override
    public void serializeFormDef(FormDef fd, String filepath) {
		Log.i(TAG, "secure form loader task serializeFormDef called");
        // calculate unique md5 identifier
		try {
			InputStream formDefStream = new SecureFileInputStream(new SecureFile(filepath));
			String hash = FileUtils.getMd5Hash(formDefStream);
            SecureFile formDef = new SecureFile(getCacheFilePathForHash(hash));
	        // formdef does not exist, create one.
	        if (!exists(formDef)) {
	        	// Recreate formDefStream as it was just read by FileUtils.getMd5Hash
	        	formDefStream.close();
	        	formDefStream = new SecureFileInputStream(new SecureFile(filepath));
	        	mStorage.writeFile(formDef.getAbsolutePath(), formDefStream);
	        }
		} catch (FileNotFoundException e) {
            Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_file_not_found_exception, filepath), e);
		} catch (IOException e) {
            Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_file_io_exception, filepath), e);
        } catch (Exception e) {
			Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_error_serializing_form_def, filepath), e);
		}
    }
}
