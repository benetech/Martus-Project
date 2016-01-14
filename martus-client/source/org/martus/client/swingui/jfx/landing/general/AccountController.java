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
package org.martus.client.swingui.jfx.landing.general;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxTabbedShellController;

public class AccountController extends FxTabbedShellController
{
	public AccountController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		setFirstTabToDisplay(ACCOUNT_INFORMATION_TAB_CODE);
	}
	
	@Override
	public Parent createContents() throws Exception
	{
		Parent shellContents = super.createContents();
		loadControllerAndEmbedInPane(new AccountInformationController(getMainWindow()), accountInformationPane);
		loadControllerAndEmbedInPane(new AuthorInformationController(getMainWindow()), authorInformationPane);
		loadControllerAndEmbedInPane(new KeyBackupController(getMainWindow()), keyBackupPane);
		selectInitialTabView();
		
		return shellContents;
	}

	private void selectInitialTabView()
	{
		accountTabPane.getSelectionModel().select(getToBeSelectedTab());
	}
	
	private Tab getToBeSelectedTab()
	{
		if (getFirstTabToDisplay().equals(ACCOUNT_INFORMATION_TAB_CODE))
			return accountInformationTab;
		
		if(getFirstTabToDisplay().equals(KEY_BACKUP_TAB_CODE))
			return keyBackupTab;
		
		if(getFirstTabToDisplay().equals(CONTACT_INFORMATION_TAB_CODE))
			return authorInformationTab;

		return null;
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/Account.fxml";
	}
	
	public static final String ACCOUNT_INFORMATION_TAB_CODE = "accountInformationTab";
	public static final String CONTACT_INFORMATION_TAB_CODE = "contactInformationTab";
	public static final String KEY_BACKUP_TAB_CODE = "keyBackupTab";
	
	@FXML
	private TabPane accountTabPane;
		
	@FXML
	private Tab accountInformationTab;
	
	@FXML
	private Tab authorInformationTab;

	@FXML
	private Tab keyBackupTab;
	
	@FXML
	private Pane accountInformationPane;

	@FXML
	private Pane authorInformationPane;

	@FXML
	private Pane keyBackupPane;
}
