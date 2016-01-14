/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.swing.UiLabel;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.Utilities;

public class UiProgressMeter extends JPanel implements ProgressMeterInterface
{
	public UiProgressMeter(UiProgressWithCancelDlg dlg, MiniLocalization localizationToUse)
	{
		super();
		localization = localizationToUse;
		
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
		parentDlg = dlg;
		statusMessage = new UiLabel("     ", UiLanguageDirection.getHorizontalAlignment());
		statusMessage.setMinimumSize(new Dimension(60, 25));

		progressMeter = new JProgressBar(0, 10);
		progressMeter.setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		Dimension meterSize = new Dimension(100, 20);
		progressMeter.setMinimumSize(meterSize);
		progressMeter.setMaximumSize(meterSize);
		progressMeter.setPreferredSize(meterSize);
		progressMeter.setBorder( new BevelBorder( BevelBorder.LOWERED ));
		progressMeter.setStringPainted(true);
		
		Component items[] = {statusMessage, progressMeter};
		Utilities.addComponentsRespectingOrientation(this, items);
	}
	
	@Override
	public void setStatusMessage(String tagToShow)
	{
		String message = localization.getFieldLabel(tagToShow);
		if(tagToShow.equals(""))
			message = "";
		statusMessage.setText(" " + message + " ");
	}

	@Override
	public void updateProgressMeter(int currentValue, int maxValue)
	{
		progressMeter.setValue(currentValue);
		progressMeter.setMaximum(maxValue);
		progressMeter.setVisible(true);
	}

	@Override
	public void hideProgressMeter()
	{
		progressMeter.setVisible(false);
	}

	@Override
	public boolean shouldExit()
	{
		if(parentDlg != null)
			return parentDlg.shouldExit();
		return false;
	}

	@Override
	public void finished()
	{
	}

	private JLabel statusMessage;
	private JProgressBar progressMeter;
	private UiProgressWithCancelDlg parentDlg;
	private MiniLocalization localization;
}
