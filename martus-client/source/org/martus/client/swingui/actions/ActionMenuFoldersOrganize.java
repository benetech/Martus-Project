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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiSetFolderOrderDlg;

public class ActionMenuFoldersOrganize extends UiMenuAction
{
	public ActionMenuFoldersOrganize(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "OrganizeFolders");
	}

	public void actionPerformed(ActionEvent ae)
	{
		doOrganizeFolders();
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void doOrganizeFolders()
	{
		Vector originalOrderFolders = getFolderTreePane().getAllFolders();
		UiSetFolderOrderDlg dlg = new UiSetFolderOrderDlg(mainWindow, originalOrderFolders);
		dlg.setVisible(true);
		if(!dlg.okPressed())
			return;

		Vector reOrderedFolders = new Vector();
		for(int i = originalOrderFolders.size()-1; i >=0; --i)
		{
			reOrderedFolders.add(originalOrderFolders.get(i));
		}
		try
		{
			getFolderTreePane().setFolderOrder(dlg.getNewFolderOrder());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}