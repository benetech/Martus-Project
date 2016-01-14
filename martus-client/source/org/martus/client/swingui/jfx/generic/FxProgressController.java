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

import javafx.application.Platform;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractAppTask;
import org.martus.common.ProgressMeterInterface;


public class FxProgressController extends FxBackgroundActivityController implements ProgressMeterInterface
{
	public FxProgressController(UiMainWindow mainWindowToUse, String messageToUse, AbstractAppTask taskToUse)
	{
		super(mainWindowToUse, messageToUse, taskToUse);
		taskToUse.setProgressInterface(this);
	}

	@Override
	public void initialize()
	{
		super.initialize();
		updateProgressBar(0.0);
	}

	@Override
	public void finished()
	{
		Platform.runLater(new CloseThisDialog());
	}

	@Override
	public void forceCloseDialog()
	{
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
	
	@Override
	public void setStatusMessage(String message)
	{
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		double percentComplete = (double)currentValue/(double)maxValue;
		fxProgressBar.setProgress(percentComplete);
		if(currentValue == maxValue)
			Platform.runLater(new CloseThisDialog());
	}

	class CloseThisDialog implements Runnable
	{
		public void run()
		{
			forceCloseDialog();
		}
	}
	
	class ProgressMeterHider implements Runnable
	{
		public void run()
		{
			fxProgressBar.setVisible(false);
		}
	}

	@Override
	public boolean shouldExit()
	{
		return userCancelled;
	}

	@Override
	public void hideProgressMeter()
	{
		Platform.runLater(new ProgressMeterHider());
	}
	
	@Override
	public void taskSucceeded()
	{
	}
	
	protected int currentProgressMade;
	private boolean userCancelled;

}
