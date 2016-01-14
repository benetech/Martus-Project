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

import java.util.Iterator;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.FolderContentsListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinListProvider extends ArrayObservableList<BulletinTableRowData> implements FolderSelectionListener, FolderContentsListener
{
	public BulletinListProvider(UiMainWindow mainWindowToUse)
	{
		super(INITIAL_CAPACITY);
		mainWindow = mainWindowToUse;
		trashFolderBeingDisplayedProperty = new SimpleBooleanProperty();
		allFolderBeingDisplayedProperty = new SimpleBooleanProperty();
		searchFolderBeingDisplayedProperty = new SimpleBooleanProperty();
		numberOfRecordsBeingDisplayedProperty = new SimpleStringProperty();
	}
	
	@Override
	public void folderWasSelected(BulletinFolder newFolder)
	{
		setFolder(newFolder);
	}

	public void setFolder(BulletinFolder newFolder)
	{
		if(folder != null)
			folder.removeFolderContentsListener(this);
		folder = newFolder;
		boolean isTrashBeingDisplayed = false;
		boolean isAllBeingDisplayed = false;
		boolean isSearchBeingDisplayed = false;
		if(folder == FxCaseManagementController.ALL_FOLDER)
		{
			isAllBeingDisplayed = true;
		}
		else
		{
			folder.addFolderContentsListener(this);
			isTrashBeingDisplayed = folder.isDiscardedFolder();
		}
		
		allFolderBeingDisplayedProperty.set(isAllBeingDisplayed);
		trashFolderBeingDisplayedProperty.set(isTrashBeingDisplayed);
		searchFolderBeingDisplayedProperty.set(isSearchBeingDisplayed);
		updateContents();
	}
	
	public void showingSearchResults()
	{
		searchFolderBeingDisplayedProperty.set(true);
	}

	
	public BulletinFolder getFolder()
	{
		return folder;
	}
	
	public BooleanProperty getTrashFolderBeingDisplayedBooleanProperty()
	{
		return trashFolderBeingDisplayedProperty;
	}
	
	public BooleanProperty getAllFolderBeingDisplayedBooleanProperty()
	{
		return allFolderBeingDisplayedProperty;
	}
	
	public BooleanProperty getSearchResultsFolderBeingDisplayedBooleanProperty()
	{
		return searchFolderBeingDisplayedProperty;
	}

	public void updateContents()
	{
		Platform.runLater(() -> loadBulletinData(getUniversalIds()));
	}

	private Set getUniversalIds()
	{
		if(folder == FxCaseManagementController.ALL_FOLDER)
			return getAllBulletinUidsIncludingDiscardedItems();
		return folder.getAllUniversalIdsUnsorted();
	}

	public Set getAllBulletinUidsIncludingDiscardedItems()
	{
		Set allBulletinUids = mainWindow.getStore().getAllBulletinLeafUids();
		return allBulletinUids;
	}

	@Override
	public void folderWasRenamed(String newName)
	{
	}

	@Override
	public void bulletinWasAdded(UniversalId added)
	{
		updateContents();
	}

	@Override
	public void bulletinWasRemoved(UniversalId removed)
	{
		updateContents();
	}

	@Override
	public void folderWasSorted()
	{
		updateContents();
	}

	void updateAllItemsInCurrentFolder()
	{
		setFolder(folder);
	}

	protected void loadAllBulletinsSelectInitialCaseFolder()
	{
		setFolder(FxCaseManagementController.ALL_FOLDER);
	}

	public void loadBulletinData(Set bulletinUids)
	{
		// FIXME: To avoid the bulletin list flickering, 
		// we should just add or remove as needed, instead of 
		// clearing and re-populating from scratch
		clear();
		try
		{
			for(Iterator iter = bulletinUids.iterator(); iter.hasNext();)
			{
				UniversalId leafBulletinUid = (UniversalId) iter.next();
				BulletinTableRowData bulletinData = getCurrentBulletinData(leafBulletinUid);
				add(bulletinData);		
			}
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		updateNumberOfRecordsBeingDisplayed();
	}

	private void updateNumberOfRecordsBeingDisplayed()
	{
		int currentCount = size();
		String countString = "(" + String.valueOf(currentCount) + ")";
		numberOfRecordsBeingDisplayedProperty.setValue(countString);
	}
	
	public StringProperty getRecordsBeingDisplayedProperty()
	{
		return numberOfRecordsBeingDisplayedProperty;
	}

	protected BulletinTableRowData getCurrentBulletinData(UniversalId leafBulletinUid) throws Exception
	{
		ClientBulletinStore clientBulletinStore = mainWindow.getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		boolean onServer = clientBulletinStore.isProbablyOnServer(leafBulletinUid);
		Integer authorsValidation = mainWindow.getApp().getKeyVerificationStatus(bulletin.getAccount());
		MiniLocalization localization = mainWindow.getLocalization();
		BulletinTableRowData bulletinData = new BulletinTableRowData(bulletin, onServer, authorsValidation, localization);
		return bulletinData;
	}

	protected int findBulletinIndexInTable(UniversalId uid)
	{
		for (int currentIndex = 0; currentIndex < size(); currentIndex++)
		{
			if(uid.equals(get(currentIndex).getUniversalId()))
				return currentIndex;
		}
		return BULLETIN_NOT_IN_TABLE;
	}

	public boolean updateBulletin(Bulletin bulletin) throws Exception
	{
		UniversalId bulletinId = bulletin.getUniversalId();
		BulletinTableRowData updatedBulletinData = getCurrentBulletinData(bulletinId);
		int bulletinIndexInTable = findBulletinIndexInTable(bulletinId);
		if(bulletinIndexInTable <= BULLETIN_NOT_IN_TABLE)
		{
			updateAllItemsInCurrentFolder();
		}
		else
		{
			set(bulletinIndexInTable, updatedBulletinData);
		}
		return false;
	}

	private static final int BULLETIN_NOT_IN_TABLE = -1;
	private static final int INITIAL_CAPACITY = 1000;
	
	private UiMainWindow mainWindow;
	private BulletinFolder folder;
	private BooleanProperty trashFolderBeingDisplayedProperty;
	private BooleanProperty allFolderBeingDisplayedProperty;
	private BooleanProperty searchFolderBeingDisplayedProperty;
	private StringProperty numberOfRecordsBeingDisplayedProperty;
}
