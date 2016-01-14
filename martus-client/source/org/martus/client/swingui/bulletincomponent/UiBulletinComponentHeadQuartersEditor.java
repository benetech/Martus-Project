/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
package org.martus.client.swingui.bulletincomponent;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.HeadquartersSelectionListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HeadquartersKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;

public class UiBulletinComponentHeadQuartersEditor extends UiBulletinComponentHeadQuartersSection
{
	public UiBulletinComponentHeadQuartersEditor(HeadquartersSelectionListener hqSelectionListener, UiMainWindow mainWindowToUse, Bulletin bulletinToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse, bulletinToUse, tagQualifierToUse, mainWindowToUse.getEditingTextFieldColumns());

		MartusApp app = mainWindow.getApp();
		tableModel = new HeadquartersEditorTableModel(app);

		HeadquartersKeys bulletinAuthorizedHeadquartersKeys = bulletinToUse.getAuthorizedToReadKeys();
		app.addHQLabelsWherePossible(bulletinAuthorizedHeadquartersKeys);
		tableModel.addKeys(bulletinAuthorizedHeadquartersKeys);
		tableModel.selectKeys(bulletinAuthorizedHeadquartersKeys);
		
		HeadquartersKeys appAvailableHeadquartersKeys = app.getAllHQKeysWithFallback();
		tableModel.addKeys(appAvailableHeadquartersKeys);

		UiLabel hqLabel = new UiLabel(getLabel("Headquarters"));
		if(tableModel.getRowCount() == 0 )
		{
			addComponents(hqLabel, new UiLabel(getLocalization().getFieldLabel("NoHQsConfigured")));
			return;
		}
		
		tableModel.setHQSelectionListener(hqSelectionListener);

		UiTable hqTable = createHeadquartersTable(tableModel);
		hqTable.setMaxColumnWidthToHeaderWidth(0);
		UiScrollPane hqScroller = new UiScrollPane(hqTable);

		addComponents(hqLabel, hqScroller);
	}

	public void copyDataToBulletin(Bulletin bulletinToCopyInto) 
	{
		if(tableModel == null)
			return;
		bulletinToCopyInto.setAuthorizedToReadKeys(tableModel.getAllSelectedHeadQuarterKeys());
	}
	

}
