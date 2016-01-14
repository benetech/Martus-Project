/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiProgressMeter;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.swing.UiButton;

public class UiProgressWithCancelDlg extends JDialog implements ProgressMeterInterface
{
	public UiProgressWithCancelDlg(UiMainWindow mainWindowToUse, String tagToUse)
	{
		super(mainWindowToUse.getSwingFrame(), true);
		mainWindow = mainWindowToUse;
		
		tag = tagToUse;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(tagToUse));
		
		cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		cancel.setAlignmentX(JButton.CENTER_ALIGNMENT);

		progressMeter = new UiProgressMeter(this, localization);
		progressMeter.setStatusMessage(tag);
		updateProgressMeter(0, 1);

		getContentPane().add(progressMeter, BorderLayout.CENTER);
		getContentPane().add(cancel, BorderLayout.AFTER_LAST_LINE);

		addWindowListener(new WindowEventHandler());
	}

	protected void requestExit()
	{
		isExitRequested = true;
		cancel.setEnabled(false);
	}

	public boolean shouldExit()
	{
		return isExitRequested;
	}

	public void hideProgressMeter()
	{
		progressMeter.hideProgressMeter();
	}

	public void updateProgressMeter(int currentValue, int maxValue)
	{
		progressMeter.updateProgressMeter(currentValue, maxValue);
	}

	public void setStatusMessage(String message)
	{
		MartusLogger.log("UiProgressWithCancelDlg cannot setStatusMessage: " + message);
	}

	// TODO: Remove one of these two redundant methods
	public void workerFinished()
	{
		dispose();
	}

	@Override
	public void finished()
	{
		workerFinished();
	}

	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			requestExit();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(mainWindow.confirmDlg(tag + "Cancel"))
				requestExit();
		}
	}

	UiMainWindow mainWindow;
	String tag;
	
	private boolean isExitRequested;
	public JButton cancel;
	public UiProgressMeter progressMeter;


}
