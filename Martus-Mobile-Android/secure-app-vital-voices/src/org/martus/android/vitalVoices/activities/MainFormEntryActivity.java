package org.martus.android.vitalVoices.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.io.SecureFileStorageManager;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SecureFormLoaderTask;

/**
 * A wrapper around {@link org.odk.collect.android.activities.FormEntryActivity}
 * that uses {@link org.odk.collect.android.io.SecureFileStorageManager} for secure
 * File I/O.
 * Created by nimaa on 7/25/14.
 */
public class MainFormEntryActivity extends FormEntryActivity {
	private static final String TAG = "MainFormEntryActivity";
	
	private SecureFileStorageManager mStorage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// We have to ensure mStorage is mounted before super.onCreate
		// But also on each onResume as we should be eventually unmounting on onPause
		mStorage = ((Collect) getApplication()).mountSecureStorage();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mStorage == null || !mStorage.isFilesystemMounted()) {
			mStorage = ((Collect) getApplication()).mountSecureStorage();
		}
	}
	
	@Override
    protected FormLoaderTask getFormLoaderTask(String instancePath, String startingXPath, String waitingXPath) {
		return new SecureFormLoaderTask(mStorage, instancePath, startingXPath, waitingXPath);
    }
}
