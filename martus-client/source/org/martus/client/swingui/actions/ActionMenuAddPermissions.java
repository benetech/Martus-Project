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
package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerProgressThread;
import org.martus.client.swingui.dialogs.AddPermissionsDialogContents;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.client.swingui.jfx.generic.ModalDialogWithSwingContents;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;


public class ActionMenuAddPermissions extends UiMenuAction
{
	public ActionMenuAddPermissions(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "AddPermissions");
	}

	public boolean isEnabled()
	{
		if(!mainWindow.isAnyBulletinSelected())
			return false;
		
		try
		{
			return (mainWindow.getApp().getAllHQKeys().size() > 0);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return false;
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			addPermissionsToBulletins(mainWindow.getSelectedBulletins("UnexpectedError"));
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
	}
	
	private void addPermissionsToBulletins(Vector selectedBulletins) throws Exception
	{
		Vector ourBulletins = extractOurBulletins(selectedBulletins, mainWindow.getApp().getAccountId());
		if(ourBulletins.size() == 0)
		{
			mainWindow.notifyDlg("AddPermissionsZeroBulletinsOurs");
			return;
		}

		HeadquartersKeys allHqKeys = mainWindow.getApp().getAllHQKeys();
		AddPermissionsDialogContents contents = new AddPermissionsDialogContents(mainWindow, selectedBulletins, ourBulletins, allHqKeys);
		ModalDialogWithSwingContents.show(contents);
		
		HeadquartersKeys selectedHqKeys = contents.getSelectedHqKeys();
		if(selectedHqKeys == null)
			return;
		
		UiProgressWithCancelDlg progressDialog = new UiProgressWithCancelDlg(mainWindow, "AddingPermissionsToBulletins");
		progressDialog.pack();
		KeyAdderThread thread = new KeyAdderThread(mainWindow, ourBulletins, selectedHqKeys);
		mainWindow.doBackgroundWork(thread, progressDialog);
		mainWindow.forceRebuildOfPreview();

	}
	
	static class KeyAdderThread extends WorkerProgressThread
	{
		public KeyAdderThread(UiMainWindow mainWindowToUse, Vector bulletinsToModify, HeadquartersKeys keysToAdd)
		{
			mainWindow = mainWindowToUse;
			bulletins = bulletinsToModify;
			hqKeys = keysToAdd;
		}
		
		public void doTheWorkWithNO_SWING_CALLS() throws Exception
		{
			ProgressMeterInterface progressMeter = getProgressMeter();
			for(int i = 0; i < bulletins.size(); ++i)
			{
				if(progressMeter.shouldExit())
					break;
				progressMeter.updateProgressMeter(i, bulletins.size());
				Bulletin oldBulletin = (Bulletin)bulletins.get(i);
				createNewVersionWithHqs(oldBulletin);
			}
		}

		private void createNewVersionWithHqs(Bulletin oldBulletin) throws Exception
		{
			MartusApp app = mainWindow.getApp();
			Bulletin newBulletin = oldBulletin;
			BulletinFolder outbox = app.getFolderDraftOutbox();
			if(oldBulletin.isImmutable())
			{
				newBulletin = app.createBulletin();
				newBulletin.createDraftCopyOf(oldBulletin, app.getStore().getDatabase());
				newBulletin.setImmutable();
				outbox = app.getFolderSealedOutbox();
			}
			newBulletin.addAuthorizedToReadKeys(hqKeys);
			app.saveBulletin(newBulletin, outbox);
			mainWindow.folderContentsHaveChanged(outbox);
		}

		UiMainWindow mainWindow;
		Vector bulletins;
		HeadquartersKeys hqKeys;
	}

	// TODO: This method is duplicated in ActionMenuSealSelectedBulletins
	private Vector extractOurBulletins(Vector allBulletins, String ourAccountId)
	{
		Vector ourBulletins = new Vector();
		
		for(int i = 0; i < allBulletins.size(); ++i)
		{
			Bulletin b = (Bulletin)allBulletins.get(i);
			if(b.getAccount().equals(ourAccountId))
				ourBulletins.add(b);
		}
		
		return ourBulletins;
	}
}
