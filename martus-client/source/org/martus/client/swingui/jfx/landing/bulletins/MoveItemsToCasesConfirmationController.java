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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.landing.cases.CaseListItem;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.swingui.jfx.landing.cases.FxFolderSettingsController;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;


public class MoveItemsToCasesConfirmationController extends FxController
{

	public MoveItemsToCasesConfirmationController(UiMainWindow mainWindowToUse, CaseListProvider casesToMoveTo, BulletinFolder currentFolderToUse)
	{
		super(mainWindowToUse);
		availableCasesToMove = casesToMoveTo;
		currentFolder = currentFolderToUse;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		updateMoveMessage();
		updateCaseList();
		updateRemoveFromExistingCase();
	}

	private void updateRemoveFromExistingCase()
	{
		if(currentFolder == FxCaseManagementController.ALL_FOLDER ||
		   currentFolder == FxCaseManagementController.SEARCH_FOLDER)
		{
			removeFromExistingCase.setVisible(false);
			return;
		}
		
		removeFromExistingCase.selectedProperty().set(true);
		
		try
		{
			String currentFolderName = currentFolder.getLocalizedName(getLocalization());
			String originalMessage = getLocalization().getButtonLabel("RemoveFromExistingCase");
			String completeMessage = TokenReplacement.replaceToken(originalMessage, "#FolderName#", currentFolderName);
			removeFromExistingCase.setText(completeMessage);
		} 
		catch (TokenInvalidException e)
		{
			logAndNotifyUnexpectedError(e);
		}
		
	}

	private void updateMoveMessage()
	{
		try
		{
			MartusLocalization localization = getLocalization();
			String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), localization);
			String messageWithTokens = localization.getFieldLabel("moveToCaseProjectIncidents");
			String completeMessage = TokenReplacement.replaceToken(messageWithTokens, "#FolderName#", foldersLabel);
			caseProjectIncidentMessageLabel.setText(completeMessage);
		} 
		catch (TokenInvalidException e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void updateCaseList()
	{
		casesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		casesListView.setItems(availableCasesToMove);
	}
	
	public ObservableList<CaseListItem> getSelectedCases()
	{
		return casesListView.getSelectionModel().getSelectedItems();
	}
	
	public boolean deleteFromCurrentCase()
	{
		return removeFromExistingCase.isSelected();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxMoveItemsToCases.fxml";
	}

	@FXML
	private ListView casesListView;
	
	@FXML
	private CheckBox removeFromExistingCase;
	
	@FXML
	private Label caseProjectIncidentMessageLabel;
	
	private CaseListProvider availableCasesToMove;
	private BulletinFolder currentFolder;
}
