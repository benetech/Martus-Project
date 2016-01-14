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
package org.martus.client.swingui.jfx.contacts;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxDialogHelper;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialogStage;

public class FxInSwingContactsStage extends FxInSwingModalDialogStage
{
	public FxInSwingContactsStage(UiMainWindow mainWindow) throws Exception
	{
		super(mainWindow, new ContactsShellController(mainWindow));
		
		contentController = new FxManageContactsController(getMainWindow());
	}

	@Override
	public void showCurrentPage() throws Exception
	{
		// NOTE: Special case for Contacts because it is still treated 
		// as a wizard page
		loadAndShowCurrentWizardPage(contentController);
	}
	
	public void loadAndShowCurrentWizardPage(FxController contentPaneController) throws Exception
	{
		loadAndShowShell();
		
		contentPaneController.setShellController(getShellController());
		getContactsShellController().loadAndIntegrateContentPane(contentPaneController);
	}
	
	@Override
	protected boolean confirmExit()
	{
		if (!contentController.hasContactsDataChanged())
			return true;
		
		return FxDialogHelper.showConfirmationDialog(getMainWindow(), "ExitManageContacts", "ExitManageContacts");
	}
	
	public ContactsShellController getContactsShellController()
	{
		return (ContactsShellController)getShellController();
	}
	
	private FxManageContactsController contentController;
}
