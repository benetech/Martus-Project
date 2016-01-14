/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui;

import javax.swing.AbstractAction;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.martus.client.swingui.actions.ActionMenuAbout;
import org.martus.client.swingui.actions.ActionMenuAccountDetails;
import org.martus.client.swingui.actions.ActionMenuAddPermissions;
import org.martus.client.swingui.actions.ActionMenuBackupMyKeyPair;
import org.martus.client.swingui.actions.ActionMenuChangeUserNamePassword;
import org.martus.client.swingui.actions.ActionMenuCharts;
import org.martus.client.swingui.actions.ActionMenuConfigureSpellCheck;
import org.martus.client.swingui.actions.ActionMenuContactInfo;
import org.martus.client.swingui.actions.ActionMenuCopyBulletins;
import org.martus.client.swingui.actions.ActionMenuCreateNewBulletin;
import org.martus.client.swingui.actions.ActionMenuCustomFields;
import org.martus.client.swingui.actions.ActionMenuCutBulletins;
import org.martus.client.swingui.actions.ActionMenuDefaultDetailsFieldContent;
import org.martus.client.swingui.actions.ActionMenuDeleteMyServerDraftBulletins;
import org.martus.client.swingui.actions.ActionMenuDiscardBulletins;
import org.martus.client.swingui.actions.ActionMenuExit;
import org.martus.client.swingui.actions.ActionMenuExportBulletins;
import org.martus.client.swingui.actions.ActionMenuExportFolder;
import org.martus.client.swingui.actions.ActionMenuExportMba;
import org.martus.client.swingui.actions.ActionMenuExportMyPublicKey;
import org.martus.client.swingui.actions.ActionMenuFolderRename;
import org.martus.client.swingui.actions.ActionMenuFoldersOrganize;
import org.martus.client.swingui.actions.ActionMenuHelp;
import org.martus.client.swingui.actions.ActionMenuImportBulletins;
import org.martus.client.swingui.actions.ActionMenuImportMba;
import org.martus.client.swingui.actions.ActionMenuManageContactsWithoutResignIn;
import org.martus.client.swingui.actions.ActionMenuModifyBulletin;
import org.martus.client.swingui.actions.ActionMenuPasteBulletins;
import org.martus.client.swingui.actions.ActionMenuPreferences;
import org.martus.client.swingui.actions.ActionMenuQuickEraseDeleteMyData;
import org.martus.client.swingui.actions.ActionMenuQuickEraseRemoveMartus;
import org.martus.client.swingui.actions.ActionMenuRemoveServer;
import org.martus.client.swingui.actions.ActionMenuReports;
import org.martus.client.swingui.actions.ActionMenuResendBulletins;
import org.martus.client.swingui.actions.ActionMenuRetrieveHQDraftBulletins;
import org.martus.client.swingui.actions.ActionMenuRetrieveHQSealedBulletins;
import org.martus.client.swingui.actions.ActionMenuRetrieveMyDraftBulletins;
import org.martus.client.swingui.actions.ActionMenuRetrieveMySealedBulletins;
import org.martus.client.swingui.actions.ActionMenuSealSelectedBulletins;
import org.martus.client.swingui.actions.ActionMenuSearch;
import org.martus.client.swingui.actions.ActionMenuSelectAllBulletins;
import org.martus.client.swingui.actions.ActionMenuSelectServer;
import org.martus.client.swingui.actions.ActionMenuSwingFolderCreate;
import org.martus.client.swingui.actions.ActionMenuSwingFolderDelete;
import org.martus.client.swingui.actions.ActionPrint;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.UiMenu;

public class UiMenuBar extends JMenuBar
{
	UiMenuBar(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		MartusLocalization localization = mainWindow.getLocalization();
		applyComponentOrientation(UiLanguageDirection.getComponentOrientation());
		createMenuActions();

		UiMenu file = new UiMenu(localization.getMenuLabel("file"));
		FileMenuListener fileMenuListener = new FileMenuListener();
		file.addMenuListener(fileMenuListener);
		fileMenuListener.initalize();

		file.add(new ActionMenuCreateNewBulletin(mainWindow));
		file.add(actionMenuPrint);
		file.add(new ActionMenuReports(mainWindow));
		file.add(new ActionMenuCharts(mainWindow));
		file.addSeparator();
		file.add(new ActionMenuExportFolder(mainWindow));
		file.add(new ActionMenuExportBulletins(mainWindow));
		
		file.add(new ActionMenuImportBulletins(mainWindow));
		if(UiSession.isAlphaTester)
		{
			file.add(new ActionMenuExportMba(mainWindow));
			file.add(new ActionMenuImportMba(mainWindow));
		}
		file.addSeparator();
		file.add(new ActionMenuExit(mainWindow));


		UiMenu edit = new UiMenu(localization.getMenuLabel("edit"));
		EditMenuListener editMenuListener = new EditMenuListener();
		edit.addMenuListener(editMenuListener);
		editMenuListener.initalize();

		edit.add(new ActionMenuSearch(mainWindow));
		edit.addSeparator();
		edit.add(actionMenuModifyBulletin);
		edit.addSeparator();
		edit.add(actionMenuCutBulletins);
		edit.add(actionMenuCopyBulletins);
		edit.add(actionMenuPasteBulletins);
		edit.add(actionMenuSelectAllBulletins);
		edit.addSeparator();
		edit.add(actionMenuDiscardBulletins);
		edit.addSeparator();
		edit.add(actionMenuAddPermissions);
		edit.add(actionMenuSealSelectedBulletins);

		UiMenu folders = new UiMenu(localization.getMenuLabel("folders"));
		FoldersMenuListener folderMenuListener = new FoldersMenuListener();
		folders.addMenuListener(folderMenuListener);
		folderMenuListener.initalize();

		folders.add(new ActionMenuSwingFolderCreate(mainWindow));
		folders.add(actionMenuFolderRename);
		folders.add(actionMenuFolderDelete);
		folders.add(actionMenuFolderOrganize);


		UiMenu server = new UiMenu(localization.getMenuLabel("server"));
		ServerMenuListener serverMenuListener = new ServerMenuListener();
		server.addMenuListener(serverMenuListener);
		serverMenuListener.initalize();
		server.add(new ActionMenuRetrieveMySealedBulletins(mainWindow));
		server.add(new ActionMenuRetrieveMyDraftBulletins(mainWindow));
		server.add(new ActionMenuDeleteMyServerDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(new ActionMenuRetrieveHQSealedBulletins(mainWindow));
		server.add(new ActionMenuRetrieveHQDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(new ActionMenuSelectServer(mainWindow));
		server.add(new ActionMenuRemoveServer(mainWindow));
		server.addSeparator();
		server.add(actionMenuResendBulletins);



		UiMenu options = new UiMenu(localization.getMenuLabel("options"));
		options.add(new ActionMenuPreferences(mainWindow));
		options.add(new ActionMenuContactInfo(mainWindow));
		options.add(new ActionMenuChangeUserNamePassword(mainWindow));
		options.add(new ActionMenuConfigureSpellCheck(mainWindow));
		options.addSeparator();
		options.add(new ActionMenuDefaultDetailsFieldContent(mainWindow));
		options.add(new ActionMenuCustomFields(mainWindow));
		
		UiMenu tools = new UiMenu(localization.getMenuLabel("tools"));
		tools.add(new ActionMenuQuickEraseDeleteMyData(mainWindow));
		tools.add(new ActionMenuQuickEraseRemoveMartus(mainWindow));
		tools.addSeparator();
		tools.add(new ActionMenuBackupMyKeyPair(mainWindow));
		tools.add(new ActionMenuExportMyPublicKey(mainWindow));
		tools.addSeparator();
		tools.add(new ActionMenuManageContactsWithoutResignIn(mainWindow));
		
		UiMenu help = new UiMenu(localization.getMenuLabel("help"));
		help.add(new ActionMenuHelp(mainWindow));
		help.add(new ActionMenuAbout(mainWindow));
		help.addSeparator();
		help.add(new ActionMenuAccountDetails(mainWindow));

		add(file);
		add(edit);
		add(folders);
		add(server);
		add(options);
		add(tools);
		add(help);
	}

	class FileMenuListener implements MenuListener
	{
		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occurs.
			actionMenuPrint.setEnabled(false);
			actionMenuResendBulletins.setEnabled(false);
		}

		public void menuSelected(MenuEvent e)
		{
			actionMenuPrint.setEnabled(actionMenuPrint.isEnabled());
			actionMenuResendBulletins.setEnabled(actionMenuResendBulletins.isEnabled());
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}
	
	
	class ServerMenuListener implements MenuListener
	{
		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occurs.
			actionMenuResendBulletins.setEnabled(false);
		}

		public void menuSelected(MenuEvent e)
		{
			actionMenuResendBulletins.setEnabled(actionMenuResendBulletins.isEnabled());
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class EditMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuModifyBulletin.setEnabled(actionMenuModifyBulletin.isEnabled());
			actionMenuSelectAllBulletins.setEnabled(actionMenuSelectAllBulletins.isEnabled());
			actionMenuCutBulletins.setEnabled(actionMenuCutBulletins.isEnabled());
			actionMenuCopyBulletins.setEnabled(actionMenuCopyBulletins.isEnabled());
			actionMenuPasteBulletins.setEnabled(actionMenuPasteBulletins.isEnabled());
			actionMenuDiscardBulletins.setEnabled(actionMenuDiscardBulletins.isEnabled());
			actionMenuAddPermissions.setEnabled(actionMenuAddPermissions.isEnabled());
			actionMenuSealSelectedBulletins.setEnabled(actionMenuSealSelectedBulletins.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occurs.
			actionMenuModifyBulletin.setEnabled(false);
			actionMenuSelectAllBulletins.setEnabled(false);
			actionMenuCutBulletins.setEnabled(false);
			actionMenuCopyBulletins.setEnabled(false);
			actionMenuPasteBulletins.setEnabled(false);
			actionMenuDiscardBulletins.setEnabled(false);
			actionMenuAddPermissions.setEnabled(false);
			actionMenuSealSelectedBulletins.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class FoldersMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuFolderRename.setEnabled(actionMenuFolderRename.isEnabled());
			actionMenuFolderDelete.setEnabled(actionMenuFolderDelete.isEnabled());
			actionMenuFolderOrganize.setEnabled(actionMenuFolderOrganize.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occurs.
			actionMenuFolderRename.setEnabled(false);
			actionMenuFolderDelete.setEnabled(false);
			actionMenuFolderOrganize.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	private void createMenuActions()
	{
		actionMenuPrint = ActionPrint.createWithMenuLabel(mainWindow);

		actionMenuModifyBulletin = new ActionMenuModifyBulletin(mainWindow);
		actionMenuSelectAllBulletins = new ActionMenuSelectAllBulletins(mainWindow);
		actionMenuCutBulletins = new ActionMenuCutBulletins(mainWindow);
		actionMenuCopyBulletins = new ActionMenuCopyBulletins(mainWindow);
		actionMenuPasteBulletins = new ActionMenuPasteBulletins(mainWindow);
		actionMenuDiscardBulletins = new ActionMenuDiscardBulletins(mainWindow);
		actionMenuResendBulletins = new ActionMenuResendBulletins(mainWindow);
		
		actionMenuFolderRename = new ActionMenuFolderRename(mainWindow);
		actionMenuFolderDelete = new ActionMenuSwingFolderDelete(mainWindow);
		actionMenuFolderOrganize = new ActionMenuFoldersOrganize(mainWindow);
		
		actionMenuAddPermissions = new ActionMenuAddPermissions(mainWindow);
		actionMenuSealSelectedBulletins = new ActionMenuSealSelectedBulletins(mainWindow);
	}


	UiMainWindow mainWindow;

	AbstractAction actionMenuPrint;
	AbstractAction actionMenuModifyBulletin;
	AbstractAction actionMenuSelectAllBulletins;
	AbstractAction actionMenuCutBulletins;
	AbstractAction actionMenuCopyBulletins;
	AbstractAction actionMenuPasteBulletins;
	AbstractAction actionMenuDiscardBulletins;
	AbstractAction actionMenuResendBulletins;
	AbstractAction actionMenuFolderRename;
	AbstractAction actionMenuFolderDelete;
	AbstractAction actionMenuFolderOrganize;
	AbstractAction actionMenuAddPermissions;
	AbstractAction actionMenuSealSelectedBulletins;
}
