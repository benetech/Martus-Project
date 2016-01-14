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

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.common.BulletinSummary;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class SyncRecordsTableProvider extends ArrayObservableList<ServerSyncTableRowData>
{
	public SyncRecordsTableProvider(UiMainWindow mainWindowToUse)
	{
		super(INITIAL_CAPACITY);
		mainWindow = mainWindowToUse;
		currentLocation = ServerSyncTableRowData.LOCATION_ANY;
		currentSubFilter = SUB_FILTER_ALL;
		allRows = new ArrayObservableList(INITIAL_CAPACITY);
	}
	
	public int getLocation()
	{
		return currentLocation;
	}
	
	public void show(int location)
	{
		currentLocation = location;
		clear();
		if( location == ServerSyncTableRowData.LOCATION_ANY && currentSubFilter == SUB_FILTER_ALL)
		{
			addAll(allRows);
			return;
		}
		
		String myAccountId = mainWindow.getApp().getAccountId();
		for (Iterator iterator = allRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData rowData = (ServerSyncTableRowData) iterator.next();
			if(location == rowData.getRawLocation() || location == ServerSyncTableRowData.LOCATION_ANY)
			{
				if(subFilterMatches(rowData, myAccountId))
					add(rowData);
			}
		}
	}
		
	private boolean subFilterMatches(ServerSyncTableRowData rowData, String myAccountId)
	{
		if(currentSubFilter == SUB_FILTER_ALL)
			return true;
		if(currentSubFilter == SUB_FILTER_MY_RECORDS)
		{
			if(myAccountId.equals(rowData.getUniversalId().getAccountId()))
				return true;
			return false;
		}
		if(currentSubFilter == SUB_FILTER_SHARED_WITH_ME)
		{
			if(!myAccountId.equals(rowData.getUniversalId().getAccountId()))
				return true;
			return false;
		}
		return false;
	}

	public void setSubFilter(int filter)
	{
		currentSubFilter = filter;
	}
	
	public int getSubFilter()
	{
		return currentSubFilter;
	}
	
	public void filterResults()
	{
		show(currentLocation);
	}

	public void addBulletinsAndSummaries(Set localUidsToUse, Vector myDraftSummaries, Vector mySealedSummaries, Vector hqDraftSummaries, Vector hqSealedSummaries) throws Exception
	{
		localUids = localUidsToUse;
		addAllCanDeleteServerSummaries(myDraftSummaries);
		addAllCanNotDeleteServerSummaries(mySealedSummaries);
		addAllCanNotDeleteServerSummaries(hqDraftSummaries);
		addAllCanNotDeleteServerSummaries(hqSealedSummaries);
		addLocalBulletions();
	}

	private void addAllCanDeleteServerSummaries(Vector summaries) throws Exception
	{
		addAllServerSummaries(summaries, true);
	}

	private void addAllCanNotDeleteServerSummaries(Vector summaries) throws Exception
	{
		addAllServerSummaries(summaries, false);
	}
	
	private void addAllServerSummaries(Vector summaries, boolean canDelete) throws Exception
	{
		for (Iterator iterator = summaries.iterator(); iterator.hasNext();)
		{
			BulletinSummary summary = (BulletinSummary) iterator.next();
			ServerSyncTableRowData bulletinData = new ServerSyncTableRowData(summary, canDelete, mainWindow.getApp());
			addServerRecord(bulletinData);	
		}
	}
	
	private void addServerRecord(ServerSyncTableRowData bulletinData)
	{
		UniversalId serverUid = bulletinData.getUniversalId();
		if(localUids.contains(serverUid))
		{
			localUids.remove(serverUid);
			bulletinData.canUploadToServerProperty().setValue(true);
			bulletinData.setLocation(mainWindow.getApp(), ServerSyncTableRowData.LOCATION_BOTH);
		}
		if(!mainWindow.getStore().isDiscarded(serverUid))
			allRows.add(bulletinData);
	}
	
	private void addLocalBulletions() throws Exception
	{
		for(Iterator iter = localUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			if(mainWindow.getStore().isDiscarded(leafBulletinUid))
				continue;
			ServerSyncTableRowData bulletinData = getLocalBulletinData(leafBulletinUid);
			allRows.add(bulletinData);		
		}
	}

	protected ServerSyncTableRowData getLocalBulletinData(UniversalId leafBulletinUid) throws Exception
	{
		ClientBulletinStore clientBulletinStore = mainWindow.getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		int bulletinSizeBytes = MartusUtilities.getBulletinSize(clientBulletinStore.getDatabase(), bulletin.getBulletinHeaderPacket());
		int location = ServerSyncTableRowData.LOCATION_LOCAL;  //TODO compare with whats on server first.
		ServerSyncTableRowData bulletinData = new ServerSyncTableRowData(bulletin, bulletinSizeBytes, location, mainWindow.getApp());
		return bulletinData;
	}

	public static final int SUB_FILTER_ALL = 0;
	public static final int SUB_FILTER_MY_RECORDS = 1;
	public static final int SUB_FILTER_SHARED_WITH_ME = 2;
	
	private UiMainWindow mainWindow;
	private static final int INITIAL_CAPACITY = 500;
	private ArrayObservableList<ServerSyncTableRowData> allRows;
	private Set localUids;
	private int currentLocation;
	private int currentSubFilter;
}
