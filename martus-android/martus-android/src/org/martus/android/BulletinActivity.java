package org.martus.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.martus.android.dialog.ConfirmationDialog;
import org.martus.android.dialog.LoginDialog;
import org.martus.android.library.common.dialog.DeterminateProgressDialog;
import org.martus.android.library.common.dialog.IndeterminateProgressDialog;
import org.martus.android.library.utilities.BulletinSender;
import org.martus.clientside.MobileClientBulletinStore;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamCopier;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author roms
 *         Date: 10/25/12
 */
public class BulletinActivity extends AbstractMainActivityWithMainMenuHandler implements BulletinSender,
        ConfirmationDialog.ConfirmationDialogListener, IndeterminateProgressDialog.IndeterminateProgressDialogListener,
        DeterminateProgressDialog.DeterminateProgressDialogListener, AdapterView.OnItemLongClickListener, LoginDialog.LoginDialogListener {

    final int ACTIVITY_CHOOSE_ATTACHMENT = 2;
    public static final String EXTRA_ATTACHMENT = "org.martus.android.filePath";
    public static final String EXTRA_ATTACHMENTS = "org.martus.android.filePaths";
    public static final String EXTRA_ACCOUNT_ID = "org.martus.android.accountId";
    public static final String EXTRA_LOCAL_ID = "org.martus.android.localId";
    public static final String EXTRA_BULLETIN_TITLE = "org.martus.android.title";

    private static final String PICASA_INDICATOR = "picasa";
	private static final SimpleDateFormat MARTUS_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    private MobileClientBulletinStore store;
    private HeadquartersKey hqKey;
    private boolean autoLogout;

    private Map<String, File> attachmentNameToFileMap;
    private String attachmentToRemoveName;
    private EditText titleText;
    private EditText summaryText;
	private TextView attachmentsHelpText;
	private TextView customFormHelp;
    private RowAdapterWithCorrectRightToLeftTextView attachmentAdapter;
    private boolean shouldShowInstallExplorer = false;
    private IndeterminateProgressDialog indeterminateDialog;
    private DeterminateProgressDialog determinateDialog;

	boolean haveFormInfo = false;

    @Override
    protected int getLayoutName() {
        return R.layout.send_bulletin_linear;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!martusCrypto.hasKeyPair()) {
            showLoginDialog();
        }

        SharedPreferences HQSettings = getSharedPreferences(PREFS_DESKTOP_KEY, MODE_PRIVATE);
        hqKey = new HeadquartersKey(HQSettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, ""));
        store = AppConfig.getInstance().getStore();


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout listHeaderView = (LinearLayout)inflater.inflate(R.layout.bulletin_list_header_view, null);

        titleText = (EditText)listHeaderView.findViewById(R.id.createBulletinTitle);
        summaryText = (EditText)listHeaderView.findViewById(R.id.bulletinSummary);
        attachmentsHelpText = (TextView)listHeaderView.findViewById(R.id.attachments_help_text);
        customFormHelp = (TextView)listHeaderView.findViewById(R.id.custom_form_transition);

        if (null == attachmentNameToFileMap) {
            attachmentAdapter = new RowAdapterWithCorrectRightToLeftTextView(this);
            clearFieldsAndAttachmentsMap();
        }

        ListView list = (ListView)findViewById(android.R.id.list);
        list.addHeaderView(listHeaderView);
        list.setTextFilterEnabled(true);
        list.setAdapter(attachmentAdapter);
        list.setLongClickable(true);
        list.setOnItemLongClickListener(this);

        addAttachmentFromIntent();
    }

    private void clearFieldsAndAttachmentsMap() {
        try {
            attachmentNameToFileMap = new ConcurrentHashMap<String, File>(2);
            titleText.setText("");
            summaryText.setText("");
            attachmentAdapter.clear();
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "problem creating bulletin", e);
            showMessage(this, getString(R.string.problem_creating_bulletin), getString(R.string.error_message));
        }
    }

    public void addAttachment(View view) {
        shouldShowInstallExplorer = false;
        try {
	        Intent intent = new Intent();
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, ACTIVITY_CHOOSE_ATTACHMENT);
        } catch (ActivityNotFoundException e) {
            Log.e(AppConfig.LOG_LABEL, "Failed choosing file", e);
            Toast.makeText(this, getString(R.string.failure_choosing_file), Toast.LENGTH_LONG).show();
        }
    }

    private void addAttachmentsAndSendBulletin() {
        try {
            if (!AppConfig.getInstance().getCrypto().hasKeyPair()) {
                showLoginDialog();
                return;
            }
            
            Bulletin bulletin = createBulletin();
            Iterator<Map.Entry<String,File>> iterator = attachmentNameToFileMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String,File> entry = iterator.next();
                File attachmentFile = entry.getValue();
                if (!addAttachmentToBulletin(bulletin, attachmentFile)) {
                    iterator.remove();
                    attachmentAdapter.remove(attachmentFile.getName());
                    Toast.makeText(this, getString(R.string.attachment_no_longer_exists, attachmentFile.getName()), Toast.LENGTH_LONG).show();

                    return;
                }
            }
	        bulletin.getTopSectionFieldSpecs();
            zipBulletin(bulletin);
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Failed zipping bulletin", e);
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_LONG).show();
        }
    }

    private void addAttachmentFromIntent() {

        Intent intent = getIntent();
        ArrayList<File> attachments = getFilesFromIntent(intent);

        try {
	        if (!attachments.isEmpty()) {
		        hideActionBarBackButton();
	        }

            for (File attachment : attachments) {
                addAttachmentToMap(attachment);
            }
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "problem adding attachment to bulletin", e);
            showMessage(this, getString(R.string.problem_adding_attachment), getString(R.string.error_message));
        }
    }

	private void hideActionBarBackButton()
	{
            ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(false);
	}

	private boolean addAttachmentToBulletin(Bulletin bulletin, File attachment) throws IOException, MartusCrypto.EncryptionException {
        AttachmentProxy attProxy = new AttachmentProxy(attachment);
        if (!attachment.exists()) {
            return false;
        }
        else {
            bulletin.addPublicAttachment(attProxy);
        }
        return true;
    }

    private void addAttachmentToMap(File attachment) {
        attachmentAdapter.add(attachment.getName());
        attachmentNameToFileMap.put(attachment.getName(), attachment);
	    attachmentsHelpText.setText(R.string.attachments_added_label);
    }

    private ArrayList<File> getFilesFromIntent(Intent intent) {
        ArrayList<File> attachments = new ArrayList<File>(1);
        String filePath;
        String[] filePaths;
        filePath = intent.getStringExtra(EXTRA_ATTACHMENT);
        filePaths = intent.getStringArrayExtra(EXTRA_ATTACHMENTS);

        try {
            if (null != filePath) {
                attachments.add(new File(filePath));
            } else if (null != filePaths) {
                for (String path : filePaths) {
                    attachments.add(new File(path));
                }
            } else {
                //check if file uri was passed via Android Send
                Bundle bundle = intent.getExtras();
                if (null != bundle) {
                    if (bundle.containsKey(Intent.EXTRA_STREAM)) {
                        ArrayList<Uri> uris;
                        Object payload = bundle.get(Intent.EXTRA_STREAM);
                        if (payload instanceof Uri) {
                            uris = new ArrayList<Uri>(1);
                            final Uri payloadUri = (Uri)payload;
                            if (isPicasaUri(payloadUri)) {
                                final AsyncTask<Uri, Void, File> picasaImageTask = new PicasaImageTask();
                                picasaImageTask.execute(payloadUri);
                            } else {
                                uris.add((Uri)payload);
                            }
                        } else {
                            uris = (ArrayList<Uri>)payload;
                        }
                        for (Uri uri : uris) {
                            attachments.add(getFileFromUri(uri));
                        }
                    }
                }
            }


        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "problem getting files for attachments", e);
            showMessage(this, getString(R.string.problem_adding_attachment), getString(R.string.error_message));
        }

        return attachments;
    }

    private boolean isPicasaUri(Uri payloadUri) {
        return payloadUri.toString().contains(PICASA_INDICATOR);
    }

    private void processPicasaResult(File result) {
        if (null != result) {
            addAttachmentToMap(result);
            Toast.makeText(BulletinActivity.this, getString(R.string.fetched_picasa_image), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BulletinActivity.this, "fetching Picasa image failed", Toast.LENGTH_LONG).show();
        }
    }

    private File getFileFromUri(Uri uri) throws URISyntaxException {
        String filePath = FileUtils.getPath(this, uri);
        return new File(filePath);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_ATTACHMENT: {
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        Uri uri = data.getData();
                        try {
                            String filePath = FileUtils.getPath(this, uri);
                            if (null != filePath) {
                                File file = new File(filePath);
                                addAttachmentToMap(file);
                            } else if (isPicasaUri(uri)) {
                                final AsyncTask<Uri, Void, File> picasaImageTask = new PicasaImageTask();
                                picasaImageTask.execute(uri);
                            }
                        } catch (Exception e) {
                            Log.e(AppConfig.LOG_LABEL, "problem getting attachment", e);
                            Toast.makeText(this, getString(R.string.problem_adding_attachment), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    shouldShowInstallExplorer = true;
                }
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

	    int id = item.getItemId();
        if (id == android.R.id.home) {
            showCancelBulletinConfirmationDialog();
            return true;
        } else if (id == R.id.send_bulletin_menu_item) {
            addAttachmentsAndSendBulletin();
            return true;
        } else if (id == R.id.cancel_bulletin_menu_item) {
            showCancelBulletinConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (shouldShowInstallExplorer) {
            showInstallExplorerDialog();
            shouldShowInstallExplorer = false;
        }
        if (! NetworkUtilities.isNetworkAvailable(this)) {
            showMessage(this, getString(R.string.no_network_create_bulletin_warning),
                    getString(R.string.no_network_connection));
        }

	    Intent intent = getIntent();
        if (intent != null) {
	        haveFormInfo = intent.getBooleanExtra(MartusActivity.HAVE_FORM, false);
	        if (haveFormInfo) {
		        titleText.setVisibility(View.GONE);
		        summaryText.setVisibility(View.GONE);
		        customFormHelp.setVisibility(View.VISIBLE);
		        if (attachmentNameToFileMap.isEmpty()) {
		            clearFieldsAndAttachmentsMap();
		        }
		        return;
	        }
        }
	    haveFormInfo = false;
	    customFormHelp.setVisibility(View.GONE);
        syncTorToggleToMatchOrbotState();
    }

	private void showCancelBulletinConfirmationDialog()
	{
		setConfirmationType(CONFIRMATION_TYPE_CANCEL_BULLETIN);
		showConfirmationDialog();
	}

    private void showGoToDeviceHomePageConfirmationDialog() {
        setConfirmationType(CONFIRMATION_TYPE_GO_TO_HOME);
        showConfirmationDialog();
    }

	@Override
	public void onBackPressed()
	{
        if (!haveFormInfo)
            showGoToDeviceHomePageConfirmationDialog();
	}

    @Override
    public String getIndeterminateDialogMessage() {
        return getString(R.string.bulletin_packaging_progress);
    }

    @Override
    public String getDeterminateDialogMessage() {
        return getString(R.string.bulletin_sending_progress);
    }

    @Override
    public void onDeterminateDialogCancel() {
        if (autoLogout) {
            setResult(EXIT_RESULT_CODE);
        }
        finish();
    }

    private void zipBulletin(Bulletin bulletin)  {
        indeterminateDialog = IndeterminateProgressDialog.newInstance();
        indeterminateDialog.show(getSupportFragmentManager(), "dlg_zipping");

	    if (haveFormInfo) {
	    }  else {

	        String title = titleText.getText().toString().trim();
	        String summary = summaryText.getText().toString().trim();
		    String author = mySettings.getString(SettingsActivity.KEY_AUTHOR, getString(R.string.default_author));
		    /*boolean useZawgyi = mySettings.getBoolean(SettingsActivity.KEY_USE_ZAWGYI, false);
		    if (useZawgyi)
		    {
			    try
			    {
				    author = BurmeseUtilities.getStorable(author);
				    title = BurmeseUtilities.getStorable(title);
				    summary = BurmeseUtilities.getStorable(summary);
			    }  catch (PatternSyntaxException e)
			    {
				    Log.e(AppConfig.LOG_LABEL, "problem converting to unicode from Zawgyi", e);
				    indeterminateDialog.dismiss();
				    throw e;
			    }
		    }*/

	        bulletin.set(Bulletin.TAGTITLE, title);
	        bulletin.set(Bulletin.TAGSUMMARY, summary);
		    bulletin.set(Bulletin.TAGAUTHOR, author);
	    }
	    bulletin.set(Bulletin.TAGENTRYDATE, MARTUS_FORMAT.format(new Date()));
	    String enteredAuthor = bulletin.get(Bulletin.TAGAUTHOR);
	    if (enteredAuthor == null || enteredAuthor.length() < 1) {
		    bulletin.set(Bulletin.TAGAUTHOR, getString(R.string.default_author));
	    }

	    //turn off user inactivity checking during zipping and encrypting of file
        stopInactivityTimer();
        parentApp.setIsSendInProgress(true);

        bulletin.set(Bulletin.TAGLANGUAGE, getDefaultLanguageForNewBulletin());
        final AsyncTask<Object, Integer, File> zipTask = new ZipBulletinTask(bulletin, this);
        zipTask.execute(getAppDir(), store);

    }

	private Bulletin createBulletin() throws Exception
    {
        Bulletin bulletin;
	    if (haveFormInfo) {
	    	bulletin = store.createEmptyBulletin(MartusApplication.getInstance().getCustomTopSectionSpecs(), MartusApplication.getInstance().getCustomBottomSectionSpecs());
	    } else  {
			bulletin = store.createEmptyBulletin();
	    }
        bulletin.set(Bulletin.TAGLANGUAGE, getDefaultLanguageForNewBulletin());
        bulletin.setAuthorizedToReadKeys(new HeadquartersKeys(hqKey));
        bulletin.setMutable();
        bulletin.changeState(Bulletin.BulletinState.STATE_SHARED);
        bulletin.setAllPrivate(true);
        return bulletin;
    }

    private String getDefaultLanguageForNewBulletin()
    {
        return mySettings.getString(SettingsActivity.KEY_DEFAULT_LANGUAGE, Locale.getDefault().getLanguage());
    }

    @Override
    public void onSent(String result) {
        try {
            determinateDialog.dismissAllowingStateLoss();
        } catch (IllegalStateException e) {
            //this is okay as the user may have closed this screen
        }
        String message = getResultMessage(result, this);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (autoLogout) {
            MartusActivity.logout();
            setResult(EXIT_RESULT_CODE);
        }
	    if (haveFormInfo)
		    setResult(CLOSE_FORM_RESULT_CODE);
        finish();
    }

    public static String getResultMessage(String result, Context context) {
        String message;
        if (result != null && result.equals(NetworkInterfaceConstants.OK)) {
            message = context.getString(R.string.successful_send_notification);
        } else {
            message = context.getString(R.string.failed_send_notification, result);
        }
        return message;
    }

    @Override
    public void onZipped(Bulletin bulletin, File zippedFile) {
        try {
            indeterminateDialog.dismissAllowingStateLoss();
        } catch (Exception e) {
            //this is okay as the user may have closed this screen
        }
        if (zippedFile == null) {
            Toast.makeText(this, getString(R.string.failure_zipping_bulletin), Toast.LENGTH_SHORT).show();
            startInactivityTimer();
            return;
        }

        sendZippedBulletin(bulletin, zippedFile);
    }

    private void sendZippedBulletin(Bulletin bulletin, File zippedFile) {
        determinateDialog = DeterminateProgressDialog.newInstance();
        try {
            determinateDialog.show(getSupportFragmentManager(), "dlg_sending");
        } catch (IllegalStateException e) {
            // just means user has left app - do nothing
        }

        UniversalId bulletinId = bulletin.getUniversalId();
        try {
            removeCachedUriAttachments(bulletin);
            store.destroyBulletin(bulletin);
        } catch (IOException e) {
            Log.e(AppConfig.LOG_LABEL, "problem destroying bulletin", e);
        }
        AsyncTask<Object, Integer, String> uploadTask = new UploadBulletinTask(getApplication(), this, bulletinId);
        MartusSecurity cryptoCopy = cloneSecurity(AppConfig.getInstance().getCrypto());
        uploadTask.execute(bulletin.getUniversalId(), zippedFile, getNetworkGateway(), cryptoCopy);
        clearFieldsAndAttachmentsMap();
        startInactivityTimer();
    }

    private void startInactivityTimer() {
        parentApp.setIsSendInProgress(false);
        resetInactivityTimer();
    }

    private void removeCachedUriAttachments(Bulletin bulletin) {
        AttachmentProxy[] attachmentProxies = bulletin.getPublicAttachments();
        for (AttachmentProxy proxy : attachmentProxies) {
            String label = proxy.getLabel();
            File file = new File(getAppDir(), label);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void removeCachedUnsentAttachments() {
        Set<String> filenames = attachmentNameToFileMap.keySet();
        for (String filename : filenames) {
            File file = new File(getAppDir(), filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void onProgressUpdate(int progress) {
        if (null != determinateDialog.getProgressDialog()) {
            determinateDialog.getProgressDialog().setProgress(progress);
        }
    }

    @Override
    public void onConfirmationAccepted() {
        switch (getConfirmationType()) {
            case CONFIRMATION_TYPE_CANCEL_BULLETIN :
                removeCachedUnsentAttachments();
                this.finish();
                break;
            case CONFIRMATION_TYPE_DELETE_ATTACHMENT :
                attachmentNameToFileMap.remove(attachmentToRemoveName);
                attachmentAdapter.remove(attachmentToRemoveName);
	            if (attachmentAdapter.isEmpty()) {
		            attachmentsHelpText.setText(R.string.attachments_add_label);
	            }
                break;
            case CONFIRMATION_TYPE_GO_TO_HOME :
                this.finish();
                goToHomeScreen();
                break;
        }

        super.onConfirmationAccepted();
    }

    @Override
    public String getConfirmationMessage() {
        if (getConfirmationType() == CONFIRMATION_TYPE_CANCEL_BULLETIN) {
            return getString(R.string.confirm_cancel_bulletin);
        } else if (getConfirmationType() == CONFIRMATION_TYPE_DELETE_ATTACHMENT ){
            return getString(R.string.confirm_remove_attachment, attachmentToRemoveName);
        } else if (getConfirmationType() == CONFIRMATION_TYPE_GO_TO_HOME) {
            return getString(R.string.confirm_go_to_device_home);
        }

        return super.getConfirmationMessage();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Object item = adapterView.getItemAtPosition(i);
        if (item != null) {
            attachmentToRemoveName = item.toString();
            showRemoveDialog();
            return true;
        }
        return false;
    }

    public void showRemoveDialog() {
        setConfirmationType(CONFIRMATION_TYPE_DELETE_ATTACHMENT);
        ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance();
        confirmationDialog.show(getSupportFragmentManager(), "dlg_delete_attachment");
    }

    private File createFileFromInputStream(InputStream inputStream, String fileName) throws IOException {

        File file = new File(getAppDir(), fileName);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        StreamCopier streamCopier = new StreamCopier();
        streamCopier.copyStream(inputStream, outputStream);
        outputStream.flush();
        outputStream.close();
        return file;
    }

	@Override
	public void refreshView()
	{
		setContentView(R.layout.send_bulletin_linear);
	}

	@Override
	public void onFinishPasswordDialog(TextView passwordText)
	{
		char[] password = passwordText.getText().toString().trim().toCharArray();
        boolean confirmed = (password.length >= MIN_PASSWORD_SIZE) && confirmAccount(password);
        if (!confirmed) {
            Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
            showLoginDialog();
        }

        invalidateAllElements(password);
        password = null;
	}

    class PicasaImageTask extends AsyncTask<Uri, Void, File> {
        @Override
        protected File doInBackground(Uri... uris) {

            final Uri uri = uris[0];
            File file = null;
            try {
                file = getFileFromPicasaUri(uri);
            } catch (Exception e) {
                Log.e(AppConfig.LOG_LABEL, "Fetching Picasa image failed", e);
            }
            return file;
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            processPicasaResult(result);
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(BulletinActivity.this, getString(R.string.fetching_picasa_image), Toast.LENGTH_SHORT).show();
        }

        private File getFileFromPicasaUri(Uri payloadUri) throws IOException {
            File file;
            final String[] filePathColumn = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };
            final Cursor cursor = getContentResolver().query(payloadUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            final int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (columnIndex == -1) {
                return null;
            }
            final InputStream is = getContentResolver().openInputStream(payloadUri);
            if (is == null) {
                return null;
            }
            final String path = payloadUri.getPath();
            final String filename = new File(path).getName();
            file = createFileFromInputStream(is, PICASA_INDICATOR + filename + ".jpg");
            is.close();

            return file;
        }
    }

    private class RowAdapterWithCorrectRightToLeftTextView extends ArrayAdapter<String> {

        protected LayoutInflater inflater;

        public RowAdapterWithCorrectRightToLeftTextView(final Context context) {
            super(context, 0);

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = inflater.inflate(R.layout.custom_listview_row, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.text_field);
            textView.setText(getItem(position));

            return rowView;
        }
    }
}