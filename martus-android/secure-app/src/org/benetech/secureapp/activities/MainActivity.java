package org.benetech.secureapp.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.benetech.secureapp.FormFromAssetFolderExtractor;
import org.benetech.secureapp.MartusUploadManager;
import org.benetech.secureapp.R;
import org.benetech.secureapp.adapters.FormAdapter;
import org.benetech.secureapp.adapters.FormAdapter.FormAdapterItemClickListener;
import org.benetech.secureapp.application.Constants;
import org.benetech.secureapp.application.MainApplication;
import org.martus.android.library.common.dialog.ProgressDialogHandler;
import org.martus.common.crypto.MartusCrypto;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.io.File;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;


public class MainActivity extends ListActivity implements ICacheWordSubscriber, FormAdapterItemClickListener, LoaderCallbacks<Cursor>, MartusUploadManager.MartusUploadManagerCallback, LogoutActivityHandler {

    private static final String TAG = "MainActivity";
    private CacheWordHandler cacheWordActivityHandler;
    private ProgressDialogHandler mProgressDialogHandler;
    public static final String ATTACHMENTS_FOLDER_NAME = "attachments";
    public static final String GALLARY_FOLDER_NAME = "secureGallary";
    public static final String CURRENT_FORM_ID_TAG = "currentFormId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        cacheWordActivityHandler = new CacheWordHandler(this);
        mProgressDialogHandler = new ProgressDialogHandler(this);

        extractFormFromAssetFolderToSecureStorage();
        initialiseExistingFormList();

        enableOdkSwipeAndButtonNavigations();
        setTitle(getString(R.string.app_name));
        ((MainApplication)getApplication()).registerLogoutHandler(this);
    }

    @Override
    public void onUserInteraction() {
        ((MainApplication)getApplication()).resetInactivityTimer();

        super.onUserInteraction();
    }

    @Override
    public void logout() {
        cacheWordActivityHandler.lock();
        cacheWordActivityHandler.disconnectFromService();

        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        logout();

        super.onBackPressed();
    }

    private void enableOdkSwipeAndButtonNavigations() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();//getString(PreferencesActivity.KEY_NAVIGATION
        editor.putString(PreferencesActivity.KEY_NAVIGATION, PreferencesActivity.NAVIGATION_SWIPE_BUTTONS);
        editor.commit();
    }

    private void extractFormFromAssetFolderToSecureStorage() {

        try {
            new FormFromAssetFolderExtractor((MainApplication)getApplication()).extractXForm();
        } catch (Exception e) {
            Log.e(TAG, getString(R.string.error_message_copying_assets_to_secure_storage_failed), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        cacheWordActivityHandler.connectToService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        cacheWordActivityHandler.disconnectFromService();
    }

    private void initialiseExistingFormList() {
        FormAdapter adapter = new FormAdapter(this.getApplicationContext(), null, this);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, (LoaderCallbacks<Cursor>) this);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(cursor);
        Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

        Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", instanceUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(instanceUri));
        } else {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
            String canEditWhenCompleteSetting = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

            boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE) || Boolean.parseBoolean(canEditWhenCompleteSetting);
            if (canEdit) {
                startForm(instanceUri);
            }
        }
    }

    public void startNewForm(View view) {
        try {
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, 1);
            startForm(formUri);

        } catch (Exception e) {
            Log.e(Constants.LOG_LABEL, getString(R.string.error_message_problem_finding_form_file), e);
        }
    }

    private void startForm(Uri formUri) {
        Intent intent = new Intent(this, MainFormEntryActivity.class);
        intent.setData(formUri);

        Cursor cursor = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);
        printContentResolver(cursor);
        startActivity(intent);
    }

    private void printContentResolver(Cursor cursor) {
        cursor.moveToFirst();
//        System.out.println("===========================================START");
//        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
//        {
//            System.out.println("===========================================");
//            String[] columnNames = cursor.getColumnNames();
//            for (int index = 0; index < columnNames.length; ++index) {
//                System.out.println("---------------------------COLUMN name = " + columnNames[index]);
//
//                int nameColumn = cursor.getColumnIndex(columnNames[index]);
//
//                String name = cursor.getString(nameColumn);
//                System.out.println("---------------------------- COL Value = " + name);
//                System.out.println("-------------------------------------------------------------------------");
//            }
//            System.out.println("===========================================");
//        }
//        System.out.println("===========================================END");
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
    public void onSyncRequested(Cursor form) {
        if (!isNetworkAvailable()) {
            Util.showErrorMessage(this, getString(R.string.no_network_connection));
            return;
        }

        showProgressDialog(getString(R.string.syncing_form));
        MartusUploadManager martusUploadManager = new MartusUploadManager(this);
        martusUploadManager.setMartusUploadCallback(this);
        martusUploadManager.uploadInstanceFormFromCursor(form, cacheWordActivityHandler);
        // We also store the form to upload in this Activity in case Util.uploadForm fails
        // because we don't have a server keypair generated. In that case, we'll have to
        // wait until UploadRightsTask completes and notifies us to retry upload on processMagicWordResponse()

//        setFormToUpload(form);
//        showProgressDialog(getString(R.string.progress_preparing_to_connect));
//        Util.uploadForm(this, form, this, cacheWordActivityHandler.getEncryptionKey());
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null;
    }

    @Override
    public void onDeleteRequested(Cursor form) {
        form.moveToFirst();
        int idColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
        String formId = form.getString(idColumnIndex);
        int deleteCount = getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                InstanceProviderAPI.InstanceColumns._ID + " = ?",
                new String[] { formId });

        form.moveToFirst();
        int instancefilepathColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
        String instanceFilepath = form.getString(instancefilepathColumnIndex);
        deleteOdkInstanceCacheDir(instanceFilepath);

        Log.i(TAG, "Instance form deleted. Delete count = " + deleteCount);
        form.close();
    }

    public static void deleteOdkInstanceCacheDir(String instancePath) {
        File odkInstanceDir = new File(instancePath);
        try {
            FileUtils.deleteDirectory(odkInstanceDir);
            if (odkInstanceDir.exists())
                Log.e(TAG, "odk form instance dir was not deleted : " + instancePath);
            else
                Log.i(TAG, "odk form instance dir deleted successfully:" + instancePath);

        } catch (Exception e) {
            Log.e(TAG, "Exception during deletion of dir: " + odkInstanceDir, e);
        }
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    public void showCreatingMartusCryptoErrorMessage() {
        Util.showErrorMessage(this, getString(R.string.error_create_account), getString(R.string.error_message));
    }

    public static void showShortToast(Context contenxt, String message) {
        Toast.makeText(contenxt, message, Toast.LENGTH_SHORT).show();
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
    public void onMartusUploadSuccess() {
        dismissProgressDialog();
    }

    @Override
    public void onMartusUploadError(MartusError error) {
        dismissProgressDialog();
        switch (error) {
            case CREATE_KEYPAIR_FAILURE:
                Util.showErrorMessage(this, getString(R.string.keypair_creation_failed));
                break;
            case INVALID_DEFAULT_SERVER_IP:
                Util.showErrorMessage(this, getString(R.string.invalid_server_ip));
                break;
            case NO_NETWORK:
                Util.showErrorMessage(this, getString(R.string.no_network_connection));
                break;
            case INVALID_SERVER_RESPONSE:
                Util.showErrorMessage(this, getString(R.string.invalid_server_info));
                break;
            case INVALID_SERVER_PUB_KEY:
                Util.showErrorMessage(this, getString(R.string.invalid_server_public_code));
                break;
        }
    }

    @Override
    public void onManageAttachments(Cursor form) {
        Intent intent = new Intent(this, ManageAttachmentsActivty.class);
        form.moveToFirst();
        int idColumnIndex = form.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
        String formId = form.getString(idColumnIndex);
        intent.putExtra(CURRENT_FORM_ID_TAG, formId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_version_menu_item) {
            showVersionNumberAsToast(this);
            return true;
        }
        if (id == R.id.show_receiving_contact_public_key_menu_item) {
            showReceivingContactPublicKey(this);
            return true;
        }

        if (id == R.id.menu_item_export_all_records) {
            exportRecordAllRecords();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void showReceivingContactPublicKey(Activity context) {
        try {
            String desktopPublicKey = context.getString(R.string.public_key_desktop);
            String publicCode40 = MartusCrypto.computeFormattedPublicCode40(desktopPublicKey);
            Util.showMessage(context, publicCode40, context.getString(R.string.receiving_contact_public_key));
        }
        catch (Exception e) {
            Log.e(TAG, "Could not format public key", e);
            showShortToast(context, context.getString(R.string.error_message));
        }
    }

    public static void showVersionNumberAsToast(Context context) {
        String versionLabel;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionNameLabel = context.getString(R.string.version_name_label, pInfo.versionName);
            String versionCodeLabel = context.getString(R.string.version_code_label, pInfo.versionCode);
            versionLabel = versionNameLabel + "\n" + versionCodeLabel;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            versionLabel = "Error";
        }

        Toast.makeText(context, versionLabel, Toast.LENGTH_LONG).show();
    }

    public void exportRecordAllRecords() {
        Cursor cursor = getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();

        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
            int formLabelColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
            int displaySubTextColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT);
            int instancefilepathColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
            int formIdColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
            int formAuthorColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_AUTHOR);
            int formOrganizationColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_ORGANIZATION);

            Intent intent = new Intent(this, BulletinToMbaFileExporter.class);
            intent.putExtra(MartusUploadManager.BULLETIN_DISPLAY_NAME_TAG, cursor.getString(formLabelColumnIndex));
            intent.putExtra(MartusUploadManager.BULLETIN_SUB_DISPLAY_NAME_TAG, cursor.getString(displaySubTextColumnIndex));
            intent.putExtra(MartusUploadManager.BULLETIN_ISTANCE_FILE_PATH_TAG, cursor.getString(instancefilepathColumnIndex));
            intent.putExtra(MartusUploadManager.BULLETIN_FORM_ID_TAG, cursor.getString(formIdColumnIndex));
            intent.putExtra(MartusUploadManager.BULLETIN_AUTHOR_TAG, cursor.getString(formAuthorColumnIndex));
            intent.putExtra(MartusUploadManager.BULLETIN_ORGANIZATION_TAG, cursor.getString(formOrganizationColumnIndex));
            startActivity(intent);
        }
    }
}