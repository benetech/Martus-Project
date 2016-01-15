package org.martus.android;

import org.martus.common.FieldSpecCollection;
import org.martus.common.network.PassThroughTransportWrapper;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author roms
 *         Date: 10/24/12
 */
public class MartusApplication extends Application {

    public static boolean isSendInProgress = false;

	public static final String DEFAULT_FONTSIZE = "21";
	private FieldSpecCollection customTopSectionSpecs;
	private FieldSpecCollection customBottomSectionSpecs;
	private static MartusApplication singleton = null;

    public static MartusApplication getInstance() {
        return singleton;
    }

    public void setIsSendInProgress(boolean ignoreSendingInProgress) {
        isSendInProgress = ignoreSendingInProgress;
    }

    public static boolean isSendInProgress() {
        return isSendInProgress;
    }

    @Override
    public void onCreate()
    {
	    singleton = this;

	    PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        super.onCreate();

	    PRNGFixes.apply();
        initSingletons();
    }

    protected void initSingletons()
    {
        AppConfig.initInstance(this.getApplicationContext());
    }

	public PassThroughTransportWrapper getTransport()
	{
		return AppConfig.getInstance().getTransport();
	}

	public FieldSpecCollection getCustomTopSectionSpecs()
	{
		return customTopSectionSpecs;
	}

	public void setCustomTopSectionSpecs(FieldSpecCollection customTopSectionSpecs)
	{
		this.customTopSectionSpecs = customTopSectionSpecs;
	}

	public FieldSpecCollection getCustomBottomSectionSpecs()
	{
		return customBottomSectionSpecs;
	}

	public void setCustomBottomSectionSpecs(FieldSpecCollection customBottomSectionSpecs)
	{
		this.customBottomSectionSpecs = customBottomSectionSpecs;
	}

	public static int getQuestionFontsize() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MartusApplication
		        .getInstance());
        String question_font = settings.getString(SettingsActivity.KEY_FONT_SIZE,
		        MartusApplication.DEFAULT_FONTSIZE);
        return Integer.valueOf(question_font);
    }

}
