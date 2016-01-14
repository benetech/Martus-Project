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

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelContentController;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public abstract class FxFolderBaseController extends DialogWithOkCancelContentController
{
	public FxFolderBaseController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	protected void updateCaseIncidentProjectTitle(Label messageTitle, String code, String foldersLabel)
	{
		try
		{
			MartusLocalization localization = getLocalization();
			String titleWithTokens = localization.getWindowTitle(code);
			String completeTitle = TokenReplacement.replaceToken(titleWithTokens, "#FolderName#", foldersLabel);
			messageTitle.setText(completeTitle);
		} 
		catch (TokenInvalidException e)
		{
			e.printStackTrace();
		}
	}

	
	private void setHintFolderErrorText(String hintText)
	{
		hintFolderError.setText(hintText);
	}

	private void clearHintFolderErrorText()
	{
		setHintFolderErrorText("");
	}

	protected void updateButtonStatusAndFolderHint(String newFolderName)
	{
		String currentNameOfFolder = "";
		updateButtonStatusAndFolderHint(newFolderName, currentNameOfFolder);
	}

	public void updateButtonStatusAndFolderHint(String newFolderName, String currentNameOfFolder)
	{
		MartusLocalization localization = getLocalization();
		ClientBulletinStore store = getMainWindow().getStore();
		boolean isOkButtonDisabled = false;
		if(!store.isFolderNameValid(newFolderName))
		{
			setHintFolderErrorText(localization.getFieldLabel("HintFolderNameInvalid"));
			isOkButtonDisabled = true;
		}
		else if(currentNameOfFolder.equals(newFolderName))
		{
			setHintFolderErrorText(localization.getFieldLabel("HintFolderNameIsSame"));
			isOkButtonDisabled = true;
		}
		else if(store.doesFolderNameAlreadyExist(newFolderName))
		{
			setHintFolderErrorText(localization.getFieldLabel("HintFolderNameAlreadyExists"));
			isOkButtonDisabled = true;
		}
		else
		{
			clearHintFolderErrorText();
		}
		getOkCancelShellController().setOkButtonDisabled(isOkButtonDisabled);
	}


	@FXML
	protected Label hintFolderError;
}
