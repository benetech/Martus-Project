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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;

public class FxProgressMeter extends Pane implements ProgressMeterInterface
{

	public FxProgressMeter(MiniLocalization localizationToUse)
	{
		super();
		localization = localizationToUse;
		HBox hBox = new HBox();
		fxProgressBar = new ProgressBar();
		updateProgressBar(0.0);
		statusMessage = new Label();
		hBox.getChildren().add(statusMessage);
		hBox.getChildren().add(fxProgressBar);
		getChildren().add(hBox);
	}
	@Override
	public void setStatusMessage(String tagToShow)
	{
		String message = localization.getFieldLabel(tagToShow);
		if(tagToShow.equals(""))
			message = "";
		Platform.runLater(new UpdateStatusMessage(message));
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		double percentComplete = (double)currentValue/(double)maxValue;
		Platform.runLater(new UpdateProgressMeter(percentComplete));
	}

	class UpdateStatusMessage implements Runnable
	{
		public UpdateStatusMessage(String messageToUse)
		{
			message = messageToUse;
		}
		public void run()
		{
			statusMessage.setText(" " + message + " ");
		}
		String message;
	}
	
	class UpdateProgressMeter implements Runnable
	{
		public UpdateProgressMeter(double percentCompleteToUse)
		{
			percentComplete = percentCompleteToUse;
		}
		public void run()
		{
			fxProgressBar.setProgress(percentComplete);
			fxProgressBar.setVisible(true);
		}
		double percentComplete;
	}

	class HideProgressMeter implements Runnable
	{
		public HideProgressMeter()
		{
		}
		public void run()
		{
			fxProgressBar.setVisible(false);
		}
		double percentComplete;
	}

	@Override
	public boolean shouldExit()
	{
		return false;
	}

	@Override
	public void hideProgressMeter()
	{
		Platform.runLater(new HideProgressMeter());
	}

	@Override
	public void finished()
	{
	}
	
	public void updateProgressBar(double currentProgress)
	{
		fxProgressBar.setProgress(currentProgress);
	}
	

	Label statusMessage;	
	ProgressBar fxProgressBar;
	private MiniLocalization localization;
}
