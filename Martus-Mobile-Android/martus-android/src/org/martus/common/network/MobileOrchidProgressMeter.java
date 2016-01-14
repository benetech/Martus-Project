package org.martus.common.network;

import org.martus.android.AppConfig;
import org.martus.common.ProgressMeterInterface;

import android.util.Log;

/**
 * @author roms
 *         Date: 7/8/13
 */
public class MobileOrchidProgressMeter implements ProgressMeterInterface
{

	private String message;

	@Override
	public void setStatusMessage(String message)
	{
   	    this.message = message;
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		Log.i(AppConfig.LOG_LABEL, "Tor initialization: " + currentValue + "% - " + message);
	}

	@Override
	public boolean shouldExit()
	{
		return false;
	}

	@Override
	public void hideProgressMeter()
	{

	}
}
