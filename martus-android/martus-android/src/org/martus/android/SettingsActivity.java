package org.martus.android;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;

import org.martus.common.crypto.MartusCrypto;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
//import com.bugsense.trace.BugSenseHandler;

/**
 * @author roms
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_HAVE_UPLOAD_RIGHTS = "server_upload_rights";
    public static final String KEY_DEFAULT_LANGUAGE = "language_preference";
    public static final String KEY_TIMEOUT_MINUTES = "timeout_preference";
    public static final String KEY_SERVER_IP = "server_ip_preference";
    public static final String KEY_AUTHOR = "author_preference";
    public static final String KEY_WIFI_ONLY = "wifi_only_preference";
    public static final String KEY_DESKTOP_PUBLIC_KEY = "desktop_public_keystring";
    public static final String KEY_SERVER_PUBLIC_KEY = "server_public_keystring";
    public static final String KEY_KEY_PAIR = "key_pair";
	public static final String KEY_USE_ZAWGYI = "zawgyi_preference";
	public static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_CHOOSE_CONNECTION = "choose_connection_preference_key";
    private static final String KEY_REPLACE_CONTACT = "replace_contact_preference_key";
    public static final String DEFAULT_TIMEOUT_MINUTES = "7";
	public static final String ZAWGYI_LANGUAGE_CODE = "my";

	public static final String NAVIGATION_SWIPE = "swipe";
	public static final String NAVIGATION_BUTTONS = "buttons";

	public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
	public static final String CONSTRAINT_BEHAVIOR_DEFAULT = "on_swipe";

	public static final String KEY_COMPLETED_DEFAULT = "default_completed";

	public static final String KEY_NAVIGATION = "navigation";
	public static final String KEY_CONSTRAINT_BEHAVIOR = "constraint_behavior";

    String[] languageNamesArray;
    String[] languageCodesArray;

    String[] timeoutNamesArray;
    String[] timeoutValuesArray;

	String[] navigationNamesArray;
	String[] navigationValuesArray;

	String[] fontSizeNamesArray;
	String[] fontSizeValuesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Comment out for production build
        //BugSenseHandler.initAndStartSession(SettingsActivity.this, ExternalKeys.BUGSENSE_KEY);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        languageNamesArray = getResources().getStringArray(R.array.entries_language_preference);
        languageCodesArray = getResources().getStringArray(R.array.values_language_preference);

        timeoutNamesArray = getResources().getStringArray(R.array.entries_timeout_preference);
        timeoutValuesArray = getResources().getStringArray(R.array.values_timeout_preference);

	    navigationNamesArray = getResources().getStringArray(R.array.navigation_entries);
	    navigationValuesArray = getResources().getStringArray(R.array.navigation_entry_values);

	    fontSizeNamesArray = getResources().getStringArray(R.array.font_size_entries);
	    fontSizeValuesArray = getResources().getStringArray(R.array.font_size_entry_values);

        addPreferencesFromResource(R.xml.settings);
        SharedPreferences mySettings = getPreferenceScreen().getSharedPreferences();

        setReplaceContactSummaryValue();
        attachIntentToContactSummaryItem();

        setChooseConnectionSummaryValue();
        attachIntentToConnectionSummaryItem();

        Map<String, ?> allPrefs = mySettings.getAll();

        //Initialize summaries of previously set settings
        Set<String> prefKeys = allPrefs.keySet();
        for (String key : prefKeys) {
            setPreferenceSummary(mySettings, key);
        }
    }

    private void attachIntentToContactSummaryItem() {
        addClickHandler(KEY_REPLACE_CONTACT, ContactImportChoiceActivity.class);
    }

    private void attachIntentToConnectionSummaryItem() {
        addClickHandler(KEY_CHOOSE_CONNECTION, ChooseConnectionActivity.class);
    }

    private void addClickHandler(String preferenceItemKey, final Class intentClassForPreference) {
        Preference replaceContactPreference = findPreference(preferenceItemKey);
        replaceContactPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, intentClassForPreference);
                startActivity(intent);
                return true;
            }
        });
    }

    private void setChooseConnectionSummaryValue() {
        try {
            Preference chooseConnectionPreference = findPreference(KEY_CHOOSE_CONNECTION);
            SharedPreferences serverSettings = getSharedPreferences(BaseActivity.PREFS_SERVER_IP, MODE_PRIVATE);
            String serverPublicKey = serverSettings.getString(SettingsActivity.KEY_SERVER_PUBLIC_KEY, "");
            String serverPublicCode40 = MartusCrypto.computeFormattedPublicCode40(serverPublicKey);
            String serverSummaryLabel = serverSettings.getString(SettingsActivity.KEY_SERVER_IP, "") + "\n" + serverPublicCode40;
            chooseConnectionPreference.setSummary(serverSummaryLabel);
        }
        catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Could not format public code", e);
        }

    }

    private void setReplaceContactSummaryValue() {
        try {
            SharedPreferences HQSettings = getSharedPreferences(MartusActivity.PREFS_DESKTOP_KEY, MODE_PRIVATE);
            String desktopPublicKeyString = HQSettings.getString(SettingsActivity.KEY_DESKTOP_PUBLIC_KEY, "");
            String publicCode = MartusCrypto.computeFormattedPublicCode40(desktopPublicKeyString);
            Preference replaceContactPreference = findPreference(KEY_REPLACE_CONTACT);
            replaceContactPreference.setSummary(publicCode);
        }catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "Could not format public code", e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setPreferenceSummary(sharedPreferences, key);
    }

    private void setPreferenceSummary(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (key.equals(KEY_DEFAULT_LANGUAGE)) {
                //need to show description of language as the summary, not the language code
                final String languageCode = sharedPreferences.getString(key, "?");
                final int index = Arrays.asList(languageCodesArray).indexOf(languageCode);
                preference.setSummary(languageNamesArray[index]);
            } else if (key.equals(KEY_TIMEOUT_MINUTES)) {
                final String timeoutValue = sharedPreferences.getString(key, DEFAULT_TIMEOUT_MINUTES);
                final int index = Arrays.asList(timeoutValuesArray).indexOf(timeoutValue);
                preference.setSummary(timeoutNamesArray[index]);
                BaseActivity.setTimeout(Integer.valueOf(timeoutValue));
            } else if (key.equals(KEY_USE_ZAWGYI)) {
	            boolean useZawgyi = sharedPreferences.getBoolean(key, false);
	            confirmLanguage(useZawgyi);
            } else if (key.equals(KEY_WIFI_ONLY)) {
                //do nothing
/*            } else if (key.equals(KEY_NAVIGATION)) {
	            final String navigationValue = sharedPreferences.getString(key, "?");
	            final int index = Arrays.asList(navigationValuesArray).indexOf(navigationValue);
	            preference.setSummary(navigationNamesArray[index]);
            } else if (key.equals(KEY_FONT_SIZE)) {
                final String sizeValue = sharedPreferences.getString(key, "?");
                final int index = Arrays.asList(fontSizeValuesArray).indexOf(sizeValue);
                preference.setSummary(fontSizeNamesArray[index]);*/
            } else {
                // Set summary to be the selected value
                preference.setSummary(sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MartusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Comment out for production build
        //BugSenseHandler.closeSession(SettingsActivity.this);
    }

	protected void confirmLanguage(boolean useZawgyi)
		{
		Resources res = getResources();
		Configuration conf = res.getConfiguration();

		String lang = (useZawgyi) ? "my" : Locale.getDefault().getLanguage();
        if (! "".equals(lang) && ! conf.locale.getLanguage().equals(lang))
        {
	        conf.locale = new Locale(lang);
            getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());
	        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
	        {
	            invalidateOptionsMenu();
	        }
	        onCreate(null);
        }
	}
}
