package org.benetech.secureapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.benetech.secureapp.MartusUploadManager;
import org.benetech.secureapp.R;
import org.benetech.secureapp.application.MainApplication;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.benetech.secureapp.collect.tasks.SecureFormLoaderTask;
import org.benetech.secureapp.collect.tasks.SecureSavePointTask;
import org.benetech.secureapp.collect.tasks.SecureSaveToDiskTask;
import org.javarosa.form.api.FormEntryController;
import org.martus.android.library.common.dialog.ProgressDialogHandler;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileFilter;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.listeners.SavePointListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SavePointTask;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.CompatibilityUtils;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

/**
 * A wrapper around {@link org.odk.collect.android.activities.FormEntryActivity}
 * that uses {@link SecureFileStorageManager} for secure
 * File I/O.
 * Created by nimaa on 7/25/14.
 */
public class MainFormEntryActivity extends FormEntryActivity implements ICacheWordSubscriber, MartusUploadManager.MartusUploadManagerCallback {
	private static final String t = "MainFormEntryActivity";

    private CacheWordHandler mCacheWordActivityHandler;
    private ProgressDialogHandler mProgressDialogHandler;
    private SecureFileStorageManager mStorage;
	private boolean mYieldedToFormGroupActivity = false;


    private ImageButton mNextButton;
    private ImageButton mBackButton;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		// We have to ensure mStorage is mounted before super.onCreate
		// But also on each onResume as we should be eventually unmounting on onPause
		mStorage = ((MainApplication) getApplication()).getMountedSecureStorage();
        mCacheWordActivityHandler = new CacheWordHandler(this);
        mProgressDialogHandler = new ProgressDialogHandler(this);
        super.onCreate(savedInstanceState);
        // Enable "Up" Navigation
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        // Note that calling setContentView after super.onCreate
        // invalidates any View references made there. Any persisted views
        // must be re-connected
        setContentView(org.benetech.secureapp.R.layout.main_form_entry);
        mQuestionHolder = (android.widget.LinearLayout) findViewById(org.benetech.secureapp.R.id.questionholder);

        mNextButton = (ImageButton) findViewById(R.id.form_forward_button);
        mBackButton = getBackButton();
    }
	
	@Override
	public void onResume() {
		super.onResume();
        mCacheWordActivityHandler.connectToService();
		if (mStorage == null || !mStorage.isFilesystemMounted()) {
			mStorage = ((MainApplication) getApplication()).getMountedSecureStorage();
		}
	}

    public void onShowPreviousView(View view) {
        showPreviousView();
    }

    public void onShowNextView(View view) {
        showNextView();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCacheWordActivityHandler.disconnectFromService();
    }
	
	/**
	 * Search cache locations for any save data related to
	 * a blank form. The value returned by this method should
	 * be used to set the value of instancePath in onCreate
	 * 
	 * @param formCursor A cursor representing the blank form
	 */
	@Override
	protected String findSavepointFileForForm(Cursor formCursor) {
		formCursor.moveToFirst();
		mFormPath = formCursor.getString(formCursor.getColumnIndex(FormsColumns.FORM_FILE_PATH));
		// This is the fill-blank-form code path.
		// See if there is a savepoint for this form that
		// has never been
		// explicitly saved
		// by the user. If there is, open this savepoint
		// (resume this filled-in
		// form).
		// Savepoints for forms that were explicitly saved
		// will be recovered
		// when that
		// explicitly saved instance is edited via
		// edit-saved-form.
		final String filePrefix = mFormPath.substring(
				mFormPath.lastIndexOf('/') + 1,
				mFormPath.lastIndexOf('.'))
				+ "_";
		final String fileSuffix = ".xml.save";
        SecureFile cacheDir = new SecureFile(Collect.CACHE_PATH);
		SecureFile[] files = cacheDir.listFiles(new SecureFileFilter(filePrefix, fileSuffix));
		// see if any of these savepoints are for a
		// filled-in form that has never been
		// explicitly saved by the user...
		for (int i = 0; i < files.length; ++i) {
			SecureFile candidate = files[i];
			String instanceDirName = candidate.getName()
					.substring(
							0,
							candidate.getName().length()
									- fileSuffix.length());
            SecureFile instanceDir = new SecureFile(
					Collect.INSTANCES_PATH + SecureFile.separator
							+ instanceDirName);
            SecureFile instanceFile = new SecureFile(instanceDir,
					instanceDirName + ".xml");
			if (instanceDir.exists()
					&& instanceDir.isDirectory()
					&& !instanceFile.exists()) {
				// yes! -- use this savepoint file
				return instanceFile
						.getAbsolutePath();
			}
		}
		return null;
	}
	
	@Override
    protected FormLoaderTask getFormLoaderTask(String instancePath, String startingXPath, String waitingXPath) {
		return new SecureFormLoaderTask(mStorage, instancePath, startingXPath, waitingXPath);
    }

	@Override
	protected SaveToDiskTask getSaveToDiskTask(Uri uri, Boolean saveAndExit, Boolean markCompleted, String updatedName) {
    	return new SecureSaveToDiskTask(uri, mStorage, saveAndExit, markCompleted, updatedName);
    }
	
	@Override
	protected SavePointTask getSavePointTask(SavePointListener listener) {
    	return new SecureSavePointTask(mStorage, listener);
    }

    @Override
    public void loadingComplete(FormLoaderTask task) {
        super.loadingComplete(task);

        if (!mYieldedToFormGroupActivity) {
            showFormHierarchyActivity(false);
        }
    }

    //@Override
    protected void showFormHierarchyActivity(boolean forResult) {
        mYieldedToFormGroupActivity = true;
        Intent i = new Intent(this, FormGroupActivity.class);
        if (forResult)
            startActivityForResult(i, HIERARCHY_ACTIVITY);
        else
            startActivity(i);
    }

    private void showMainActivity() {
        // Unregister dismiss listener -- Will call crash
        mProgressDialog.setOnDismissListener(null);

        Intent i = new Intent(this, MainActivity.class);
        // Make MainActivity the new root of the task. e.g: hitting the back button
        // after this transfer will close the app, and not return here
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    /**
     * This method is identical to its superclass implementation except the ending call to finish()
     * is replaced by a call to {@link #showFormHierarchyActivity(boolean)}
     *
     * Our Vital Voices app flow has the user shuttling between {@link FormGroupActivity}
     * to navigate groups, and this activity for modifying them. If we allow this Activity to finish we'd have
     * to marshal the form uri between FormGroupActivity and here on each back-and-forth. This would also involve
     * re-loading the entire form definition every time you select a group to edit from {@link FormGroupActivity}
     */
    @Override
    protected void finishReturnInstance() {
        FormController formController = Collect.getInstance()
                .getFormController();
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_EDIT.equals(action)) {
            // caller is waiting on a picked form
            String selection = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
            String[] selectionArgs = { formController.getInstancePath()
                    .getAbsolutePath() };
            Cursor c = null;
            try {
                c = getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                        null, selection, selectionArgs, null);
                if (c.getCount() > 0) {
                    // should only be one...
                    c.moveToFirst();
                    String id = c.getString(c
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));
                    Uri instance = Uri.withAppendedPath(
                            InstanceProviderAPI.InstanceColumns.CONTENT_URI, id);
                    setResult(RESULT_OK, new Intent().setData(instance));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        showFormHierarchyActivity(false);
    }

    /**
     * This createView skips the beginning of form page, proceeding to the first group
     *
     * @param event
     * @param advancingPage
     *            -- true if this results from advancing through the form
     * @return newly created View
     */
    @Override
    protected View createView(int event, boolean advancingPage) {
        FormController formController = Collect.getInstance().getFormController();
        setTitle(getString(org.odk.collect.android.R.string.app_name) + " > " + formController.getFormTitle());
        try {
            switch (event) {
                case FormEntryController.EVENT_BEGINNING_OF_FORM:
                    try {
                        event = formController.stepToNextScreenEvent();
                    } catch (JavaRosaException e1) {
                        Log.e(t, e1.getMessage(), e1);
                        createErrorDialog(e1.getMessage() + "\n\n" + e1.getCause().getMessage(), DO_NOT_EXIT);
                    }

                    return createView(event, advancingPage);
                default:
                    View view = super.createView(event, advancingPage);
                    mBackButton = getBackButton();
                    if (formController.getFormIndex().getLocalIndex() == 0)
                        mBackButton.setEnabled(false);
                    else
                        mBackButton.setEnabled(true);

                    return view;
            }
        } finally {
            // Make sure we always override any Activity (Action Bar) title set by our parent class
            String instanceTitle = Util.getFormInstanceTitle(this);
            if (instanceTitle != null && instanceTitle.length() > 0) {
                setTitle(instanceTitle);
            } else {
                // For ActionBar use Form title if no instance available
                setTitle(Collect.getInstance().getFormController().getFormTitle());
            }
        }
    }

    private ImageButton getBackButton() {
        if (mBackButton == null)
            mBackButton = (ImageButton) findViewById(R.id.form_back_button);

        return mBackButton;
    }

    /**
     * Unlike the super implementation, return to {@link MainActivity}
     * when a {@link org.odk.collect.android.tasks.SaveToDiskTask#SAVED_AND_EXIT} task completes
     */
    @Override
    public void savingComplete(SaveResult saveResult) {
        int saveStatus = saveResult.getSaveResult();
        if (saveStatus == SaveToDiskTask.SAVED_AND_EXIT) {
            dismissDialog(SAVING_DIALOG);
            Log.i(t, "savingComplete. Beginning upload");
            showProgressDialog(getString(org.benetech.secureapp.R.string.syncing_form));
            Toast.makeText(this, getString(org.odk.collect.android.R.string.data_saved_ok), Toast.LENGTH_SHORT).show();
            sendSavedBroadcast();
            MartusUploadManager martusUploadManager = new MartusUploadManager(this);
            martusUploadManager.setMartusUploadCallback(this);
            martusUploadManager.uploadCurrentInstanceForm(mCacheWordActivityHandler);
        } else {
            super.savingComplete(saveResult);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "onCreateOptionsMenu", "show");

        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_LANGUAGES, 0, org.odk.collect.android.R.string.change_language)
                        .setIcon(org.odk.collect.android.R.drawable.ic_menu_start_conversation),
                MenuItem.SHOW_AS_ACTION_NEVER);

        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_PREFERENCES, 0, org.odk.collect.android.R.string.general_preferences)
                        .setIcon(org.odk.collect.android.R.drawable.ic_menu_preferences),
                MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FormController formController = Collect.getInstance()
                .getFormController();

        boolean useability;

        useability = mAdminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_CHANGE_LANGUAGE, true)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(MENU_LANGUAGES).setVisible(useability)
                .setEnabled(useability);

        useability = mAdminPreferences.getBoolean(
                AdminPreferencesActivity.KEY_ACCESS_SETTINGS, true);

        menu.findItem(MENU_PREFERENCES).setVisible(useability)
                .setEnabled(useability);
        return true;
    }

    public void saveFormButtonClicked(View v) {
        saveForm();
    }

    private void saveForm() {
        String instanceTitle = Util.getFormInstanceTitle(this);
        if (instanceTitle == null || instanceTitle.length() == 0) {
            instanceTitle = Collect.getInstance().getFormController().getFormTitle();
        }
        saveDataToDisk(EXIT, false, instanceTitle);
    }

    private void showProgressDialog(String message) {
        getProgressDialogHandler().showProgressDialog(message);
    }

    private void dismissProgressDialog() {
        getProgressDialogHandler().dismissProgressDialog();
    }

    private ProgressDialogHandler getProgressDialogHandler() {
        return mProgressDialogHandler;
    }

    @Override
    public void onCacheWordUninitialized() {

    }

    @Override
    public void onCacheWordLocked() {

    }

    @Override
    public void onCacheWordOpened() {

    }

    @Override
    public void onMartusUploadSuccess() {
        Log.i(t, "Martus upload success!");
        dismissProgressDialog();
        showFormHierarchyActivity(false);
    }

    @Override
    public void onMartusUploadError(MartusError error) {
        dismissProgressDialog();
        switch (error) {
            case CREATE_KEYPAIR_FAILURE:
                Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.keypair_creation_failed));
                break;
            case INVALID_DEFAULT_SERVER_IP:
                Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.invalid_server_ip));
                break;
            case NO_NETWORK:
                Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.no_network_connection));
                break;
            case INVALID_SERVER_RESPONSE:
                Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.invalid_server_info));
                break;
            case INVALID_SERVER_PUB_KEY:
                Util.showErrorMessage(this, getString(org.benetech.secureapp.R.string.invalid_server_public_code));
                break;
        }
    }

    protected void endOfFormEvent(int event) {
        saveForm();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
