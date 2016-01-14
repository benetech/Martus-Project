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
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerProgressThread;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;

public class ActionMenuSealSelectedBulletins extends UiMenuAction
{
	public ActionMenuSealSelectedBulletins(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "SealSelectedBulletins");
	}

	public boolean isEnabled()
	{
		BulletinFolder selectedFolder = mainWindow.getSelectedFolder();
		if(selectedFolder == null)
			return false;
		
		if(selectedFolder.getName().equals(mainWindow.getStore().getFolderDiscarded().getName()))
			return false;
		return (mainWindow.isAnyBulletinSelected());
	}


	public void actionPerformed(ActionEvent event)
	{
		try
		{
			if(!mainWindow.confirmDlg("SealSelectedBulletins"))
				return;
			
			sealSelectedBulletins(mainWindow.getSelectedBulletins("UnexpectedError"));
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
	}

	private void sealSelectedBulletins(Vector selectedBulletins) throws Exception
	{
		Vector ourBulletins = extractOurBulletins(selectedBulletins, mainWindow.getApp().getAccountId());
		if(ourBulletins.size() == 0)
		{
			mainWindow.notifyDlg("SealSelectedZeroBulletinsOurs");
			return;
		}

		UiProgressWithCancelDlg progressDialog = new UiProgressWithCancelDlg(mainWindow, "SealingSelectedBulletins");
		progressDialog.pack();
		BulletinSealerThread thread = new BulletinSealerThread(mainWindow, ourBulletins);
		mainWindow.doBackgroundWork(thread, progressDialog);
		mainWindow.forceRebuildOfPreview();
	}

	// TODO: This method is duplicated from ActionMenuAddPermissions
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

	static class BulletinSealerThread extends WorkerProgressThread
	{
		public BulletinSealerThread(UiMainWindow mainWindowToUse, Vector bulletinsToSeal)
		{
			mainWindow = mainWindowToUse;
			bulletins = bulletinsToSeal;
		}
		
		public void doTheWorkWithNO_SWING_CALLS() throws Exception
		{
			ProgressMeterInterface progressMeter = getProgressMeter();
			for(int i = 0; i < bulletins.size(); ++i)
			{
				if(progressMeter.shouldExit())
					break;
				progressMeter.updateProgressMeter(i, bulletins.size());
				Bulletin bulletin = (Bulletin)bulletins.get(i);
				if(bulletin.isMutable())
					makeBulletinImmutable(bulletin);
			}
		}
		
		private void makeBulletinImmutable(Bulletin bulletin) throws Exception
		{
			MartusApp app = mainWindow.getApp();
			ClientBulletinStore store = app.getStore();
			BulletinFolder draftOutbox = store.getFolderDraftOutbox();
			BulletinFolder sealedOutbox = store.getFolderSealedOutbox();

			store.removeBulletinFromFolder(draftOutbox, bulletin);

			bulletin.setImmutable();
			store.saveBulletin(bulletin);

			store.ensureBulletinIsInFolder(sealedOutbox, bulletin.getUniversalId());
			store.setIsNotOnServer(bulletin);
			store.saveFolders();

		}

		private UiMainWindow mainWindow;
		private Vector bulletins;
	}
}
