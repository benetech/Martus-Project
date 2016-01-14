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

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithCloseShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinViewerController;

public class ActionMenuViewFxBulletin extends ActionMenuFxBulletin
{
	public ActionMenuViewFxBulletin(UiMainWindow mainWindowToUse, FxController controllerToUse)
	{
		super(mainWindowToUse, "viewBulletin");
		controller = controllerToUse;
	}
	
	public void doAction()
	{
		try
		{
			BulletinViewerController bulletinViewCotroller = new BulletinViewerController(getMainWindow());
			FxBulletin bulletinToView = new FxBulletin(getLocalization());			
			bulletinToView.copyDataFromBulletin(getBulletin(), getMainWindow().getStore());
			bulletinViewCotroller.setBulletin(bulletinToView);		
			DialogWithCloseShellController shellController = new DialogWithCloseShellController(getMainWindow(), bulletinViewCotroller);
			controller.doAction(shellController);
		} 
		catch (Exception e)
		{
			controller.logAndNotifyUnexpectedError(e);
		} 
	}
	
	private FxController controller;
}