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
package org.martus.client.swingui.actions;

import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.clientside.CurrentUiState;
import org.martus.common.MartusLogger;

public class ActionMenuQuickSearch extends ActionSearch
{

	public ActionMenuQuickSearch(UiMainWindow mainWindowToUse, String storableSimpleSearchString)
	{
		super(mainWindowToUse);
		CurrentUiState uiState = getMainWindow().getUiState();
		uiState.setSearchFinalBulletinsOnly(true);
		searchString = storableSimpleSearchString;
	}

	@Override
	public void doAction()
	{
		try
		{
			SortableBulletinList bulletinIdsFromSearch = doSearch(searchString);
			FxMainStage stage = mainWindow.getMainStage();
			BulletinsListController controller = stage.getBulletinsListController();
			controller.updateSearchResultsTable(bulletinIdsFromSearch);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	private String searchString;
}
