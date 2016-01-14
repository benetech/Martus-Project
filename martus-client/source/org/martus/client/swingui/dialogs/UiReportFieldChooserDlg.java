/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;

public class UiReportFieldChooserDlg extends UIReportFieldDlg
{
	public UiReportFieldChooserDlg(UiMainWindow mainWindow, FieldSpec[] specsToUse)
	{
		super(mainWindow.getSwingFrame());
		setModal(true);
		
		String dialogTag = "ChooseReportFields";
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(dialogTag));
		selectedSpecs = null;

		fieldSelector = new UiReportFieldSelectorPanel(mainWindow, specsToUse);
		
		UiButton okButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancelButton.addActionListener(new CancelButtonHandler());
		Box buttonBar = Box.createHorizontalBox();
		Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(buttonBar, buttons);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new UiWrappedTextPanel(localization.getFieldLabel(dialogTag)), BorderLayout.BEFORE_FIRST_LINE);
		panel.add(fieldSelector, BorderLayout.CENTER);
		panel.add(buttonBar, BorderLayout.AFTER_LAST_LINE);

		getContentPane().add(panel);
		pack();
		Utilities.packAndCenterWindow(this);
	}
	
	
	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			selectedSpecs = fieldSelector.getSelectedItems();
			dispose();
		}
	}
	
	class CancelButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}

	public FieldSpec[] getSelectedSpecs()
	{
		return selectedSpecs;
	}
	

	UiReportFieldSelectorPanel fieldSelector;
	FieldSpec[] selectedSpecs;
}
