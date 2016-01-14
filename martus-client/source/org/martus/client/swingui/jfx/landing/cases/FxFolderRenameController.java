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
package org.martus.client.swingui.jfx.landing.cases;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelShellController;

public class FxFolderRenameController extends FxFolderBaseController
{
	public FxFolderRenameController(UiMainWindow mainWindowToUse, String currentFolderNameToUse)
	{
		super(mainWindowToUse);
		currentFolderName = currentFolderNameToUse;
	}
	
	public void addFolderRenameListener(ChangeListener folderListenerToUse)
	{
		folderRenameListener = folderListenerToUse;
	}	

	@Override
	public void initialize()
	{
		MartusLocalization localization = getLocalization();
		String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), localization);
		updateCaseIncidentProjectTitle(messageTitle, "RenameCaseIncidentProject", foldersLabel);
		folderName.textProperty().addListener(new FolderNameChangeListener());
		folderName.setText(currentFolderName);
		DialogWithOkCancelShellController shellController = getOkCancelShellController();
		shellController.setOkButtonText(localization.getButtonLabel("RenameFolder"));
	}

	@Override
	public void save()
	{
		String newFolderName = folderName.getText();
		ClientBulletinStore store = getApp().getStore();
		BulletinFolder currentFolder = store.findFolder(currentFolderName);
		if(currentFolder == null)
		{
			logAndNotifyUnexpectedError(new NullPointerException("Bulletin Folder Not Found"));
			return;
		}
		if(!store.renameFolder(currentFolderName, newFolderName))
		{
			logAndNotifyUnexpectedError(new Exception("Bulletin Folder failed to rename old:"+currentFolderName+", new:"+ newFolderName));
			return;
		}
		
		folderRenameListener.changed(null, null, newFolderName);
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_RENAME_FXML;
	}
	
	private class FolderNameChangeListener implements ChangeListener<String>
	{
		public FolderNameChangeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends String> observableValue,
				String oldFolderName, String newFolderName)
		{
			updateButtonStatusAndFolderHint(newFolderName, getCurrentFoldersName());
		}		
	}
	
	protected String getCurrentFoldersName()
	{
		return currentFolderName;
	}
	private static final String LOCATION_FOLDER_RENAME_FXML = "landing/cases/FolderRename.fxml";

	@FXML
	private Label messageTitle;

	@FXML
	private TextField folderName;
		
	private ChangeListener folderRenameListener;
	private final String currentFolderName;
}
