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
import javax.swing.Box;
import javax.swing.JPanel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;

public class UiImportExportProgressMeterDlg extends UiProgressRetrieveDlg
{

	public UiImportExportProgressMeterDlg(UiMainWindow window, String tag)
	{
		super(window, tag);
		getContentPane().setLayout(new BorderLayout());
		Box hBox = Box.createHorizontalBox();
		UiLabel bulletinTitle = new UiLabel(window.getLocalization().getFieldLabel("ImportExportBulletinTitle") + " ");
		currentBulletinTitle = new UiTextField(30);
		currentBulletinTitle.setEditable(false);
		
		Component[] items = {bulletinTitle,currentBulletinTitle};
		Utilities.addComponentsRespectingOrientation(hBox, items);
		
		JPanel cancelPanel = new JPanel();
		cancelPanel.add(cancel);
		
		JPanel meterPanel = new JPanel();
		meterPanel.add(progressMeter);
		
		getContentPane().add(hBox, BorderLayout.NORTH);
		getContentPane().add(meterPanel, BorderLayout.CENTER);
		getContentPane().add(cancelPanel, BorderLayout.SOUTH);
		Utilities.packAndCenterWindow(this);
	}
	
	public void updateBulletinTitle(String bulletinTitle)
	{
		currentBulletinTitle.setText(bulletinTitle);
	}
	
	public boolean confirmDialog(String baseTag)
	{
		return mainWindow.confirmDlg(baseTag);
	}
	
	private UiTextField currentBulletinTitle;

}
