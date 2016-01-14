/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
import java.io.File;
import java.util.zip.ZipFile;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class ActionMenuImportMba extends UiMenuAction
{
	public ActionMenuImportMba(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "ImportMBA");
	}

	public void actionPerformed(ActionEvent arg0)
	{
		try
		{
			BulletinFolder destination = mainWindow.getSelectedFolder();
			if(destination == null)
			{
				mainWindow.notifyDlg("MustSelectFolderToImportMba");
				return;
			}
			doImportMba(destination);
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}

	}

	private void doImportMba(BulletinFolder destination) throws Exception
	{
		File from = getFileToImport();
		if(from == null)
			return;
		
		importBulletinFromMba(from, destination);
	}

	private void importBulletinFromMba(File from, BulletinFolder to) throws Exception
	{
		ZipFile zip = new ZipFile(from);
		ClientBulletinStore store = mainWindow.getApp().getStore();
		UniversalId uid = store.importBulletinZipFile(zip);
		Bulletin bulletin = store.getBulletinRevision(uid);
		if(!to.contains(bulletin))
			to.add(bulletin);
		store.saveFolders();
	}

	private File getFileToImport()
	{
		MartusBulletinArchiveFileFilter filter = new MartusBulletinArchiveFileFilter(getLocalization());
		File importFile = mainWindow.showFileOpenDialogWithDirectoryMemory("ImportMBA", filter);
		return importFile;
	}


}
