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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiImportExportProgressMeterDlg;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.client.tools.XmlFileVersionTooNew;
import org.martus.client.tools.XmlFileVersionTooOld;
import org.martus.client.tools.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.common.MartusLogger;

public class ImportBulletins
{
	
	public ImportBulletins(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	public void doImport(File xmlFileToImport, String importingFolderName)
	{
		try
		{
			File[] xmlFilesToImport = new File[] {xmlFileToImport};
			UiImportExportProgressMeterDlg progressDlg = new UiImportExportProgressMeterDlg(mainWindow, "ImportProgress");
			BulletinFolder importFolder = mainWindow.getStore().createOrFindFolder(importingFolderName);
			ImporterThread importThread = new ImporterThread(xmlFilesToImport, importingFolderName, progressDlg);
			importThread.start();
			progressDlg.setVisible(true);
			mainWindow.selectFolder(importFolder);
			mainWindow.folderContentsHaveChanged(importFolder);
			mainWindow.folderTreeContentsHaveChanged();
			
			if(importThread.hasMissingAttachments())
				showAttachmentErrors(importThread);
			if(importThread.hasBulletinsNotImported())		
				showBulletinsNotImported(importThread);
			int numberOfBulletinsImported = importThread.getNumberOfBulletinsImported();
			int totalBulletins = importThread.getTotalBulletins();
			mainWindow.notifyDlg("ImportComplete", getTokenReplacementImporter(numberOfBulletinsImported, totalBulletins, importingFolderName));
		}
		catch (Exception e)
		{
			mainWindow.notifyDlg("ErrorImportingBulletins");
		}
	}

	private void showAttachmentErrors(ImporterThread importThread)
	{
		HashMap missingAttachmentsMap = importThread.getMissingAttachmentsMap();
		mainWindow.notifyDlg("ImportMissingAttachments", getTokenReplacementImportErrors(missingAttachmentsMap, "#ImportMissingAttachments#"));
	}
	
	private void showBulletinsNotImported(ImporterThread importThread)
	{
		HashMap missingBulletins = importThread.getBulletinsNotImported();
		mainWindow.notifyDlg("ImportBulletinsNotImported", getTokenReplacementImportErrors(missingBulletins, "#ImportBulletinsNotImported#"));
	}
	
	class ImporterThread extends Thread
	{
		public ImporterThread(File[] xmlFilesToImport, String importingFolderName, UiImportExportProgressMeterDlg progressRetrieveDlgToUse)
		{
			clientStore = mainWindow.getStore();
			BulletinFolder folder = clientStore.createOrFindFolder(importingFolderName);
			filesToImport = xmlFilesToImport;
			importFolder = folder;
			progressMeter = progressRetrieveDlgToUse;
			
		}

		public void run()
		{
			try
			{
				importer = new ImporterOfXmlFilesOfBulletins(filesToImport, clientStore, importFolder, progressMeter);
				importer.setAttachmentsDirectory(filesToImport[0].getParentFile());
				importer.importFiles();
			}
			catch(FieldSpecVerificationException e)
			{
				e.printStackTrace(MartusLogger.getDestination());
				Vector errors = e.getErrors();
				for(int i = 0; i < errors.size(); ++i)
					MartusLogger.log(errors.get(i).toString());
			}
			catch(XmlFileVersionTooOld e)
			{
				e.printStackTrace();
				mainWindow.notifyDlg("ErrorImportingBulletinsTooOld");
			}
			catch(XmlFileVersionTooNew e)
			{
				e.printStackTrace();
				mainWindow.notifyDlg("ErrorImportingBulletinsTooNew");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				mainWindow.notifyDlg("ErrorImportingBulletins");
			}
			finally
			{
				progressMeter.finished();
			}
		}

		public int getNumberOfBulletinsImported()
		{
			return importer.getNumberOfBulletinsImported();
		}
		
		public int getTotalBulletins()
		{
			return importer.getTotalNumberOfBulletins();
		}
		
		public boolean hasMissingAttachments()
		{
			return importer.hasMissingAttachments();
		}
		
		public HashMap getMissingAttachmentsMap()
		{
			return importer.getMissingAttachmentsMap();
		}
		
		public boolean hasBulletinsNotImported()
		{
			return importer.hasBulletinsNotImported();		
		}

		public HashMap getBulletinsNotImported()
		{
			return importer.getBulletinsNotImported();
		}
		
		private File[] filesToImport;
		private BulletinFolder importFolder;
		private UiImportExportProgressMeterDlg progressMeter;
		private ClientBulletinStore clientStore;
		private ImporterOfXmlFilesOfBulletins importer;
	}
	

	Map getTokenReplacementImporter(int bulletinsImported, int totalBulletins, String folder) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsSuccessfullyImported#", Integer.toString(bulletinsImported));
		map.put("#TotalBulletinsToImport#", Integer.toString(totalBulletins));
		map.put("#ImportFolder#", folder);
		return map;
	}

	Map getTokenReplacementImportErrors(HashMap importErrors, String tokenToUse) 
	{
		
		String[] bulletinTitles = new String[importErrors.size()];
		importErrors.keySet().toArray(bulletinTitles);
		StringBuffer listOfErrors = new StringBuffer();
		for(int i = 0; i < bulletinTitles.length; i++)
		{
			String bulletinTitle = bulletinTitles[i];
			listOfErrors.append(bulletinTitle);
			String specificProblem = (String)importErrors.get(bulletinTitle);
			if(specificProblem != null)
			{
				listOfErrors.append(" : ");
				listOfErrors.append(specificProblem);
			}
			listOfErrors.append("\n");
		}

		
		HashMap map = new HashMap();
		map.put(tokenToUse, listOfErrors.toString());
		return map;
	}

	UiMainWindow mainWindow;
}
