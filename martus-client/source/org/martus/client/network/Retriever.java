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

package org.martus.client.network;

import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiProgressMeter;
import org.martus.client.swingui.dialogs.UiProgressRetrieveBulletinsDlg;
import org.martus.common.MartusLogger;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.UniversalId;

public class Retriever
{

	public Retriever(MartusApp appToUse, UiProgressRetrieveBulletinsDlg retrieve)
	{
		super();
		app = appToUse;
		progressDlg = retrieve;
		result = NetworkInterfaceConstants.INCOMPLETE;
	}

	public void retrieveBulletins(Vector uidList, BulletinFolder retrievedFolder)
	{
		try
		{
			if(!app.isSSLServerAvailable())
			{
				result = NetworkInterfaceConstants.NO_SERVER;
				return;
			}
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			result = NetworkInterfaceConstants.NO_SERVER;
			return;
		}

		RetrieveThread worker = new RetrieveThread(uidList, retrievedFolder) ;
		worker.start();

		if(progressDlg == null)
			waitForThreadToTerminate(worker);
	}

	public void waitForThreadToTerminate(RetrieveThread worker)
	{
		try
		{
			worker.join();
		}
		catch (InterruptedException e)
		{
		}
	}

	public void finishedRetrieve()
	{
		if(progressDlg != null)
			progressDlg.finished();
	}

	public String getResult()
	{
		return result;
	}


	class RetrieveThread extends Thread
	{
		public RetrieveThread(Vector list, BulletinFolder folder)
		{
			uidList = list;
			retrievedFolder = folder;
			if(progressDlg != null)
			{
				String progressTag = "ChunkProgressStatusMessage";
				progressDlg.getChunkCountMeter().setStatusMessage(progressTag);
			}
		}

		public void run()
		{
			boolean gotAllBulletins = true;
			int i = 0;
			int size = uidList.size();
			UiProgressMeter progressMeter = null;
			if(progressDlg != null)
				progressMeter = progressDlg.getChunkCountMeter();
			for(i = 0; i < size; ++i)
			{
				try
				{
					if(progressDlg != null)
					{
						if(progressDlg.shouldExit())
							break;
						progressDlg.updateProgressMeter(i, size);
					}
					UniversalId uid = (UniversalId)uidList.get(i);
					DatabaseKey key = DatabaseKey.createLegacyKey(uid);
					if(app.getStore().doesBulletinRevisionExist(key))
						continue;
					try
					{
						app.retrieveOneBulletinToFolder(uid, retrievedFolder, progressMeter);
					}
					catch(AddOlderVersionToFolderFailedException expected)
					{
					}
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					gotAllBulletins = false;
				}
			}

			if(progressDlg != null)
				progressDlg.updateProgressMeter(i, size);

			if(gotAllBulletins)
				result = NetworkInterfaceConstants.OK;
			else
				result = NetworkInterfaceConstants.INCOMPLETE;

			finishedRetrieve();
		}

		private Vector uidList;
		private BulletinFolder retrievedFolder;
	}

	String result;
	MartusApp app;
	public UiProgressRetrieveBulletinsDlg progressDlg;
}
