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

import javafx.application.Platform;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;

public class ImportBulletinAction implements ActionDoer
{
	public ImportBulletinAction(FxCaseManagementController fxCaseManagementControllerToUse)
	{
		fxCaseManagementController = fxCaseManagementControllerToUse;
		uiMainWindow = fxCaseManagementController.getMainWindow();
	}

	@Override
	public void doAction()
	{
		String fileDialogCategory = "ImportBulletin";

		MartusBulletinArchiveFileFilter mbaFilter = new MartusBulletinArchiveFileFilter(getLocalization());
		XmlFileFilter xmlFilter = new XmlFileFilter(getLocalization());
		
		Vector<FormatFilter> filters = new Vector();
		filters.add(mbaFilter);
		filters.add(xmlFilter);

		File selectedFile = getMainWindow().showFileOpenDialog(fileDialogCategory, filters);
		
		if(selectedFile == null)
			return;
		
		String lowerCaseFileName = selectedFile.getName().toLowerCase();
		if(lowerCaseFileName.endsWith(mbaFilter.getExtension().toLowerCase()))
			importBulletinFromMbaFile(selectedFile);
		else
			importBulletinFromXmlFile(selectedFile);        
	}

	private void importBulletinFromXmlFile(File fileToImport)
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

	private void importBulletinFromMbaFile(File fileToImport)
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
	
	private MartusApp getApp()
	{
		return getMainWindow().getApp();
	}
	
	private MartusLocalization getLocalization()
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
	
	private UiMainWindow uiMainWindow;
	private FxCaseManagementController fxCaseManagementController;
}
