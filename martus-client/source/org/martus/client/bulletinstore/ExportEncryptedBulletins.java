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
package org.martus.client.bulletinstore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.martus.client.core.TransferableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuExportMba;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;

public class ExportEncryptedBulletins extends AbstractExport
{
	public ExportEncryptedBulletins(UiMainWindow mainWindowToUse, ProgressMeterInterface progressDlgToUse)
	{
		super(mainWindowToUse, progressDlgToUse);
	}
	
	@Override
	public void doExport(File destinationFolderToUse, Vector bulletinsToUse) 
	{
		destinationFolder = destinationFolderToUse;
		bulletinsToExport = bulletinsToUse;
		ExporterThread exporterThread = new ExporterThread(getProgressDlg());
		exporterThread.start();
	}
	
	protected void updateExportMessage(ExporterThread exporterThread,
			int bulletinsExported) 
	{
		setExportErrorMessage("ExportComplete");
		if(didErrorOccur())
			setExportErrorMessage("ErrorExportingBulletins");
		setExportErrorMessageTokensMap(getTokenReplacementImporter(bulletinsExported, bulletinsToExport.size()));
	}

	Map getTokenReplacementImporter(int numberOfBulletinsExported, int totalNumberOfBulletins) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsExported#", Integer.toString(numberOfBulletinsExported));
		map.put("#TotalBulletinsToExport#", Integer.toString(totalNumberOfBulletins));
		return map;
	}

	class ExporterThread extends Thread
	{
		public ExporterThread(ProgressMeterInterface progressRetrieveDlgToUse)
		{
			clientStore = getMainWindow().getStore();
			progressMeter = progressRetrieveDlgToUse;
		}

		public void run()
		{
			int bulletinCount = bulletinsToExport.size();
			int bulletinsExported = 0;
			for (int i = 0; i < bulletinCount; i++)
			{
				Bulletin bulletinToExport = (Bulletin)bulletinsToExport.get(i);
				if(progressMeter != null)
				{
					progressMeter.updateProgressMeter(i+1, bulletinCount);
					if(progressMeter.shouldExit())
						break;
				}
				try
				{
					File fullDestinationFile = getDestinationFile(bulletinToExport);
					ActionMenuExportMba.exportBulletinToMba(getMainWindow(), bulletinToExport, fullDestinationFile);
					++bulletinsExported;
				} 
				catch (Exception e)
				{
					MartusLogger.logException(e);
					setErrorOccured(true);
				} 
			}
			updateExportMessage(this, bulletinsExported);
			progressMeter.finished();
		}

		private File getDestinationFile(Bulletin bulletinToExport) throws IOException
		{
			if(doesDestinationIncludeFileName())
				return destinationFolder;
			String summary = bulletinToExport.toFileName();
			return File.createTempFile(summary, TransferableBulletinList.BULLETIN_FILE_EXTENSION, destinationFolder);
		}

		private boolean doesDestinationIncludeFileName()
		{
			return !destinationFolder.isDirectory();
		}

		ProgressMeterInterface progressMeter;
		ClientBulletinStore clientStore;
	}

	protected Vector bulletinsToExport;
	protected File destinationFolder;
}


