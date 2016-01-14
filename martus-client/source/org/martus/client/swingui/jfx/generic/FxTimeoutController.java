/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.swingui.jfx.generic;

import java.util.TimerTask;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.tasks.TaskWithTimeout;
import org.martus.common.MartusUtilities;


public class FxTimeoutController extends FxBackgroundActivityController
{
	public FxTimeoutController(UiMainWindow mainWindowToUse, String messageToUse, TaskWithTimeout taskToUse, int maxSecondsToCompleteTaskToUse)
	{
		super(mainWindowToUse, messageToUse, taskToUse);
		maxSecondsToCompleteTask = maxSecondsToCompleteTaskToUse;
	}

	@Override
	public void initialize()
	{
		super.initialize();
		backgroundTick = new TimeoutTimerTask();
		MartusUtilities.startTimer(backgroundTick, BACKGROUND_TIMEOUT_CHECK_EVERY_SECOND);
	}

	class TimeoutTimerTask extends TimerTask
	{
		public void run()
		{
			double percentComplete = (double)currentNumberOfSecondsCompleted/(double)maxSecondsToCompleteTask;
			updateProgressBar(percentComplete);
			++currentNumberOfSecondsCompleted;
			if(currentNumberOfSecondsCompleted > maxSecondsToCompleteTask)
			{
				backgroundTick.cancel();
				//forceCloseDialog(); //TODO figure out how to close this dialog from main thread not this thread.
			}
		}
	}

	@Override
	public void forceCloseDialog()
	{
		if(backgroundTick != null)
		{
			backgroundTick.cancel();
			backgroundTick = null;
		}
		super.forceCloseDialog();
	}

	@Override
	public void cancelPressed()
	{
		userCancelled = true;
		forceCloseDialog();
	}

	@Override
	public boolean didUserCancel()
	{
		return userCancelled;
	}
	
	protected int maxSecondsToCompleteTask;
	protected int currentNumberOfSecondsCompleted;
	protected TimeoutTimerTask backgroundTick;
	private boolean userCancelled;

	final int BACKGROUND_TIMEOUT_CHECK_EVERY_SECOND = 1000;
}
