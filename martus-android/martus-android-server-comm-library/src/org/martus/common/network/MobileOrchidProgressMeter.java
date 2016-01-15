package org.martus.common.network;

import android.util.Log;

import org.martus.common.ProgressMeterInterface;

/**
 * @author roms
 *         Date: 7/8/13
 */
public class MobileOrchidProgressMeter implements ProgressMeterInterface
{
    private static final String TAG = "MobileOrchidProgressMeter";
	private String message;

	@Override
	public void setStatusMessage(String message)
	{
   	    this.message = message;
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		Log.i(TAG, "Tor initialization: " + currentValue + "% - " + message);
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

    @Override
    public void finished() {
    }
}
