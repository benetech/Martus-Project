/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.Utilities;
import org.martus.util.DirectoryUtils;


public class ActionMenuQuickEraseRemoveMartus extends ActionQuickErase
{
	public ActionMenuQuickEraseRemoveMartus(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "QuickEraseRemoveMartus");
	}

	public void actionPerformed(ActionEvent arg0)
	{
		if(!confirmQuickErase(WILL_UNINSTALL_MARTUS))
			return;
		prepareAndDeleteMyData();
		uninstallMartus();
		exitMartus();
	}
	
	
	private void uninstallMartus()
	{
		deleteAndScrubAllAccountKeyPairsAndRelatedFiles();

		app.cleanupWhenCompleteQuickErase();
		File martusDataRootDirectory = app.getMartusDataRootDirectory();
		mainWindow.unLock();

		if(Utilities.isMSWindows())
		{
			DirectoryUtils.deleteAllFilesOnlyInDirectory(martusDataRootDirectory);				
			File uninstallFile = new File(martusDataRootDirectory,"/bin/uninst.exe");
			try
			{
				File silentUnInstallNotificationFile = new File(martusDataRootDirectory,"$$$silent");
				FileOutputStream out = new FileOutputStream(silentUnInstallNotificationFile);
				out.write(1);
				out.close();
				Runtime.getRuntime().exec("\""+uninstallFile.getAbsolutePath()+"\"");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			DirectoryUtils.deleteEntireDirectoryTree(martusDataRootDirectory);
		}
	}

	private void deleteAndScrubAllAccountKeyPairsAndRelatedFiles()
	{
		Vector martusAccounts = app.getAllAccountDirectories();
		for (Iterator iter = martusAccounts.iterator(); iter.hasNext();)
		{
			File accountDir = (File) iter.next();
			app.deleteKeypairAndRelatedFilesForAccount(accountDir);
		}
	}
	

}
