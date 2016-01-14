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

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelShellController;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FxFolderDeleteController extends FxFolderBaseController
{
	public FxFolderDeleteController(UiMainWindow mainWindowToUse, BulletinFolder folderToDeleteToUse)
	{
		super(mainWindowToUse);
		folderToDelete = folderToDeleteToUse;
	}

	public void addFolderDeletedListener(FolderDeletedListener folderListenerToUse)
	{
		folderDeletedListener = folderListenerToUse;
	}

	@Override
	public void initialize()
	{
		MartusLocalization localization = getLocalization();
		String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), localization);
		updateCaseIncidentProjectTitle(messageTitle, "DeleteCaseIncidentProject", foldersLabel);
		messageTextArea.setEditable(false);
		String deleteFolderMessage = localization.getFieldLabel("DeleteFolderMessage");
		try
		{
			String fullMessage = TokenReplacement.replaceToken(deleteFolderMessage, "#FolderName#", folderToDelete.getLocalizedName(localization));
			messageTextArea.setText(fullMessage);
		} 
		catch (TokenInvalidException e)
		{
			logAndNotifyUnexpectedError(e);
		}
		DialogWithOkCancelShellController shellController = getOkCancelShellController();
		shellController.setOkButtonText(localization.getButtonLabel("DeleteFolder"));
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_DELETE_FXML;
	}
	
	@Override
	public void save()
	{
		boolean folderWasDeleted = getApp().getStore().deleteFolder(folderToDelete.getName());
		if(folderWasDeleted)
			folderDeletedListener.folderWasDeleted();
		else
			showNotifyDialog("ErrorDeletingFolder");
	}
	
	public static interface FolderDeletedListener
	{
		public void folderWasDeleted();
	}

	private static final String LOCATION_FOLDER_DELETE_FXML = "landing/cases/FolderDelete.fxml";

	@FXML 
	private Label messageTitle;
	
	@FXML 
	private TextArea messageTextArea;

	private FolderDeletedListener folderDeletedListener;	
	private BulletinFolder folderToDelete;
}
