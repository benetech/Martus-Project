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

import java.awt.Dimension;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelShellController;

public class FxFolderCreateController extends FxFolderBaseController
{
	public FxFolderCreateController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	public void addFolderCreatedListener(ChangeListener folderListenerToUse)
	{
		folderCreatedListener = folderListenerToUse;
	}	

	@Override
	public void initialize()
	{
		MartusLocalization localization = getLocalization();
		String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), localization);
		updateCaseIncidentProjectTitle(messageTitle, "CreateCaseIncidentProject", foldersLabel);
		String defaultFolderNewName = localization.getFieldLabel("defaultCaseName");
		folderName.textProperty().addListener(new FolderNameChangeListener());
		folderName.setText(defaultFolderNewName);
		DialogWithOkCancelShellController shellController = getOkCancelShellController();
		shellController.setOkButtonText(localization.getButtonLabel("CreateFolder"));
	}
	
	@Override
	protected Dimension getPreferredDimension()
	{
		return UiMainWindow.SMALL_PREFERRED_DIALOG_SIZE;
	}

	@Override
	public void save()
	{
		String newFolderName = folderName.getText();
		BulletinFolder newFolder = getApp().createUniqueFolder(newFolderName);
		if(newFolder == null)
		{
			showNotifyDialog("UnableToCreateFolder");
			return; 
		}
		folderCreatedListener.changed(null, null, newFolder.getName());
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_CREATE_FXML;
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
			updateButtonStatusAndFolderHint(newFolderName);
		}
		
	}
	
	@FXML
	private Label messageTitle;

	@FXML
	private TextField folderName;
		
	private static final String LOCATION_FOLDER_CREATE_FXML = "landing/cases/FolderCreate.fxml";
	private ChangeListener folderCreatedListener;
}
