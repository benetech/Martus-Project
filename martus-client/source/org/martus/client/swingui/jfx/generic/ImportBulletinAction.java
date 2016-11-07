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
package org.martus.client.swingui.jfx.generic;

import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.client.swingui.jfx.generic.FxController.UserCancelledException;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractAppTask;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;

import javafx.application.Platform;

public class ImportBulletinAction implements ActionDoer
{
	public ImportBulletinAction(FxCaseManagementController fxCaseManagementControllerToUse)
	{
		fxCaseManagementController = fxCaseManagementControllerToUse;
		uiMainWindow = fxCaseManagementController.getMainWindow();
	}

	public void doAction()
	{
		String fileDialogCategory = "ImportBulletin";

		MartusBulletinArchiveFileFilter mbaFilter = new MartusBulletinArchiveFileFilter(getLocalization());
		XmlFileFilter xmlFilter = new XmlFileFilter(getLocalization());
		
		Vector<FormatFilter> filters = new Vector();
		filters.add(mbaFilter);
		filters.add(xmlFilter);

		File[] selectedFiles = getMainWindow().showMultiFileOpenDialog(fileDialogCategory, filters);
		if (selectedFiles.length == 0)
			return;
		
		Platform.runLater(new ImportRunner(selectedFiles));
	}
	
	protected void importBulletinFromXmlFile(File fileToImport)
	{
		try
		{
			BulletinFolder folderToImportInto = getImportFolder();
			ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(fileToImport, getApp().getStore(), folderToImportInto, System.out);
			importer.importFiles();
			updateCases();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	protected void importBulletinFromMbaFile(File fileToImport)
	{
		try
		{
			BulletinFolder folderToImportInto = getImportFolder();
			getApp().getStore().importZipFileBulletin(fileToImport, folderToImportInto, false);
			updateCases();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	private void updateCases()
	{
		Platform.runLater(new UpdateCasesWorker());
	}
	
	protected FxCaseManagementController getFxCaseManagementController()
	{
		return fxCaseManagementController;
	}
	
	protected BulletinFolder getImportFolder()
	{
		return getApp().getStore().getFolderImport();
	}
	
	private UiMainWindow getMainWindow()
	{
		return uiMainWindow;
	}
	
	protected MartusApp getApp()
	{
		return getMainWindow().getApp();
	}
	
	protected MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	protected class UpdateCasesWorker implements Runnable
	{
		@Override
		public void run()
		{
			BulletinFolder folderToImportInto = getImportFolder();
			getFxCaseManagementController().updateCases(folderToImportInto.getName());
		}
	}
	
	public class ImportRunner implements Runnable
	{
		public ImportRunner(File[] selectedFilesToUse)
		{
			selectedFiles = selectedFilesToUse;
		}

		@Override
		public void run()
		{
			ImportTask importTask =  new ImportTask(getApp(), selectedFiles);
			try
			{
				String message = getLocalization().getFieldLabel("ImportingRecords");
				getFxCaseManagementController().showProgressDialog(message, importTask);
			}
			catch (UserCancelledException e)
			{	
				MartusLogger.logVerbose("User canceled importer, by clicking cancel in progress dialog");
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
		
		private File[] selectedFiles;
	}
	
	private class ImportTask extends AbstractAppTask 
	{
		public ImportTask(MartusApp appToUse, File[] selectedFilesToUse)
		{
			super(appToUse);
			
			selectedFiles = selectedFilesToUse;
		}

		@Override
		protected Void call() throws Exception
		{
			MartusBulletinArchiveFileFilter mbaFilter = new MartusBulletinArchiveFileFilter(getLocalization());
			for (int index = 0; index < selectedFiles.length; ++index)
			{
				if (getProgressMeter().shouldExit())
					break;
				
				getProgressMeter().updateProgressMeter(index + 1, selectedFiles.length);
				File selectedFile = selectedFiles[index];
				String lowerCaseFileName = selectedFile.getName().toLowerCase();
				if(lowerCaseFileName.endsWith(mbaFilter.getExtension().toLowerCase()))
					importBulletinFromMbaFile(selectedFile);
				else
					importBulletinFromXmlFile(selectedFile);		
			}
			
			return null;
		}
		
		private File[] selectedFiles;
	}
		
	private UiMainWindow uiMainWindow;
	private FxCaseManagementController fxCaseManagementController;
}
