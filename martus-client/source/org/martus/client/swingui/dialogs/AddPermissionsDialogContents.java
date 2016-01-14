/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;

import org.martus.client.swingui.UiBulletinTitleListComponent;
import org.martus.client.swingui.UiHeadquartersTable;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.HeadquartersEditorTableModel;
import org.martus.client.swingui.jfx.generic.SwingDialogContentPane;
import org.martus.clientside.UiLocalization;
import org.martus.common.HeadquartersKeys;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

import com.jhlabs.awt.GridLayoutPlus;

public class AddPermissionsDialogContents extends SwingDialogContentPane
{
	public AddPermissionsDialogContents(UiMainWindow mainWindowToUse, Vector allBulletins, Vector ourBulletins, HeadquartersKeys hqKeys)
	{
		super(mainWindowToUse);
		
		Container contentPane = this;
		contentPane.setLayout(new GridLayoutPlus(0, 1, 2, 2, 2, 2));
		UiLocalization localization = getLocalization();
		setTitle(localization.getWindowTitle("AddPermissions"));

		String overview = localization.getFieldLabel("AddPermissionsOverview");
		contentPane.add(new UiWrappedTextArea(overview));

		
		UiBulletinTitleListComponent list = new UiBulletinTitleListComponent(getMainWindow(), ourBulletins);
		contentPane.add(new UiScrollPane(list));
		
		// if any bulletins are not ours, tell user why they are not listed
		if(ourBulletins.size() != allBulletins.size())
		{
			String skippingBulletinsNotOurs = localization.getFieldLabel("SkippingBulletinsNotOurs");
			contentPane.add(new UiWrappedTextArea(skippingBulletinsNotOurs));
		}
		
		contentPane.add(blankLine());
		String chooseHeadquartersToAdd = localization.getFieldLabel("ChooseHeadquartersToAdd");
		contentPane.add(new UiWrappedTextArea(chooseHeadquartersToAdd));
		
		model = new HeadquartersEditorTableModel(getMainWindow().getApp());
		model.addKeys(hqKeys);
		UiHeadquartersTable hqTable = new UiHeadquartersTable(model);
		hqTable.setMaxColumnWidthToHeaderWidth(0);
		UiScrollPane hqScroller = new UiScrollPane(hqTable);
		contentPane.add(hqScroller);
		contentPane.add(blankLine());

		Box buttonBox = Box.createHorizontalBox();
		UiButton okButton = new UiButton(localization.getButtonLabel("AddPermissions"));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getCancelButtonLabel());
		cancelButton.addActionListener(new CancelButtonHandler());
		Component[] buttons = new Component[] {
				Box.createHorizontalGlue(),
				okButton,
				cancelButton,
		};
		Utilities.addComponentsRespectingOrientation(buttonBox, buttons);
		contentPane.add(buttonBox);
	}
	
	public HeadquartersKeys getSelectedHqKeys()
	{
		return selectedHqKeys;
	}

	private UiLabel blankLine()
	{
		return new UiLabel(" ");
	}
	
	void doOk()
	{
		HeadquartersKeys selectedKeys = model.getAllSelectedHeadQuarterKeys();
		if(selectedKeys.size() == 0)
		{
			getMainWindow().notifyDlg("AddPermissionsZeroHeadquartersSelected");
			return;
		}
		
		selectedHqKeys = selectedKeys;
		
		dispose();
	}
	
	void doCancel()
	{
		dispose();
	}
	
	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			doOk();
		}
	}
	
	class CancelButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			doCancel();
		}
		
	}
	
	HeadquartersEditorTableModel model;
	HeadquartersKeys selectedHqKeys;
}