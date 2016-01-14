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
import java.util.Arrays;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;

public class UiReportFieldOrganizerDlg extends UIReportFieldDlg
{
	public UiReportFieldOrganizerDlg(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse.getSwingFrame());
		setModal(true);
		mainWindow = mainWindowToUse;
		
		String dialogTag = "OrganizeReportFields";
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(dialogTag));
		
		fieldSelector = new UiReportFieldSelectorPanel(mainWindow, getEmptyFieldSpecsToStart());
		fieldSelector.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		UiButton addButton = new UiButton(localization.getButtonLabel("AddFieldToReport"));
		addButton.addActionListener(new AddButtonHandler());
		removeButton = new UiButton(localization.getButtonLabel("RemoveFieldFromReport"));
		removeButton.addActionListener(new RemoveButtonHandler());
		upButton = new UiButton(localization.getButtonLabel("MoveFieldUpInReport"));
		upButton.addActionListener(new UpButtonHandler());
		downButton = new UiButton(localization.getButtonLabel("MoveFieldDownInReport"));
		downButton.addActionListener(new DownButtonHandler());

		UiVBox sideButtonBar = new UiVBox();
		sideButtonBar.add(addButton);
		sideButtonBar.add(removeButton);
		sideButtonBar.add(upButton);
		sideButtonBar.add(downButton);
		
		okButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancelButton.addActionListener(new CancelButtonHandler());
		Box bottomButtonBar = Box.createHorizontalBox();
		Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(bottomButtonBar, buttons);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new UiWrappedTextPanel(localization.getFieldLabel(dialogTag)), BorderLayout.BEFORE_FIRST_LINE);
		panel.add(new UiScrollPane(fieldSelector), BorderLayout.CENTER);
		panel.add(sideButtonBar, BorderLayout.EAST);
		panel.add(bottomButtonBar, BorderLayout.AFTER_LAST_LINE);

		getContentPane().add(panel);
		pack();
		Utilities.packAndCenterWindow(this);
		updateButtons();
	}
	
	class AddButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			FieldSpec[] allFieldSpecs = getAllFieldSpecs();
			Vector possibleSpecsToAdd = new Vector(Arrays.asList(allFieldSpecs));
			FieldSpec[] currentSpecs = fieldSelector.getAllItems();
			if(currentSpecs != null)
				possibleSpecsToAdd.removeAll(Arrays.asList(currentSpecs));
			UiReportFieldChooserDlg dlg = new UiReportFieldChooserDlg(mainWindow, (FieldSpec[])possibleSpecsToAdd.toArray(new FieldSpec[0]));
			dlg.setVisible(true);
			fieldSelector.addSpecs(dlg.getSelectedSpecs());
			updateButtons();
		}
	}

	class RemoveButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = fieldSelector.getSelectedRow();
			fieldSelector.removeSpec(selectedRow);
			if(selectedRow == fieldSelector.getSpecCount())
				--selectedRow;
			if(selectedRow >= 0)
				selectRow(selectedRow);
			updateButtons();
		}
	}

	class UpButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = fieldSelector.getSelectedRow();
			int rowSelectionWillMoveTo = selectedRow-1;
			if(rowSelectionWillMoveTo < 0)
				return;
			fieldSelector.moveSpecUp(selectedRow);
			selectRow(rowSelectionWillMoveTo);
			updateButtons();
		}
	}

	class DownButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = fieldSelector.getSelectedRow();
			int rowSelectionWillMoveTo = selectedRow+1;
			if(selectedRow < 0 || rowSelectionWillMoveTo >= fieldSelector.getSpecCount())
				return;
			fieldSelector.moveSpecDown(selectedRow);
			selectRow(rowSelectionWillMoveTo);
			updateButtons();
		}
	}

	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			orderedSpecs = fieldSelector.getAllItems();
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
	
	public void updateButtons()
	{
		disableButtons();
		
		int specCount = fieldSelector.getSpecCount();
		if(specCount > 0)
		{
			okButton.setEnabled(true);
			removeButton.setEnabled(true);
		}
		if(specCount > 1)
		{
			upButton.setEnabled(true);
			downButton.setEnabled(true);
		}
	}

	private void disableButtons()
	{
		okButton.setEnabled(false);
		removeButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
	}

	public FieldSpec[] getSelectedSpecs()
	{
		return orderedSpecs;
	}
	
	void selectRow(int rowToSelect)
	{
		fieldSelector.selectRow(rowToSelect);
	}

	FieldSpec[] getAllFieldSpecs()
	{
		FieldChooserSpecBuilder allFieldSpecBuilder = new FieldChooserSpecBuilder(mainWindow.getLocalization());
		FieldSpec[] allFieldSpecs = allFieldSpecBuilder.createFieldSpecArray(mainWindow.getStore());
		return allFieldSpecs;
	}
	
	FieldSpec[] getEmptyFieldSpecsToStart()
	{
		return new FieldSpec[0];
	}

	UiButton okButton;
	UiButton removeButton;
	UiButton upButton;
	UiButton downButton;

	UiMainWindow mainWindow;
	UiReportFieldSelectorPanel fieldSelector;
	FieldSpec[] orderedSpecs;
}
