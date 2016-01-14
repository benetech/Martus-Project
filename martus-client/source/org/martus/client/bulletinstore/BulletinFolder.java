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

package org.martus.client.bulletinstore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;

public class BulletinFolder
{
	public final int ASCENDING = 1;
	public final int DESCENDING = -ASCENDING;

	public BulletinFolder(ClientBulletinStore storeToUse, String nameToUse)
	{
		store = storeToUse;
		name = nameToUse;

		rawIdList = new HashSet();
		sortedIdList = null;
		listeners = new HashSet();
	}
	
	public boolean isDiscardedFolder()
	{
		return equals(getStore().getFolderDiscarded());
	}

	public ClientBulletinStore getStore()
	{
		return store;
	}
	
	public void addFolderContentsListener(FolderContentsListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeFolderContentsListener(FolderContentsListener listener)
	{
		listeners.remove(listener);
	}

	public synchronized void setName(String newName)
	{
		if(canRename)
		{
			name = newName;
			listeners.forEach(listener -> listener.folderWasRenamed(newName));
		}
	}

	public String getName()
	{
		return name;
	}
	
	public String getLocalizedName(UiLocalization localization)
	{
		if(isNameLocalized(name))
			return localization.getLocalizedFolderName(name);
		return name;
	}
	

	public void preventRename()
	{
		canRename = false;
	}

	public boolean canRename()
	{
		return canRename;
	}

	public void preventDelete()
	{
		canDelete = false;
	}

	public boolean canDelete()
	{
		return canDelete;
	}
	
	public int getBulletinCount()
	{
		return rawIdList.size();
	}

	public String sortedBy()
	{
		return sortTag;
	}

	public int getSortDirection()
	{
		return sortDir;
	}

	public boolean isVisible()
	{
		return isNameVisible(getName());
	}

	public synchronized void add(Bulletin b) throws BulletinAlreadyExistsException, IOException
	{
		add(b.getUniversalId());
	}

	synchronized void add(UniversalId id) throws BulletinAlreadyExistsException, IOException
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(id);
		ReadableDatabase db = store.getDatabase();
		if(!db.doesRecordExist(key))
			throw new IOException();

		if(rawIdList.contains(id))
		{
			//System.out.println("already contains " + id);
			throw new ClientBulletinStore.BulletinAlreadyExistsException();
		}

		rawIdList.add(id);
		insertIntoSortedList(id);
		listeners.forEach(listener -> listener.bulletinWasAdded(id));
	}

	public synchronized void remove(UniversalId id)
	{
		if(!rawIdList.contains(id))
			return;
		rawIdList.remove(id);
		if(sortedIdList != null)
			sortedIdList.remove(id);
		listeners.forEach(listener -> listener.bulletinWasAdded(id));
	}

	public synchronized void removeAll()
	{
		Set<UniversalId> idsToRemove = new HashSet<UniversalId>(rawIdList);
		idsToRemove.forEach(uid -> remove(uid));
		sortedIdList = null;
	}
	
	public Bulletin getBulletinSorted(int index)
	{
		UniversalId uid = getBulletinUniversalIdSorted(index);
		if(uid == null)
			return null;
		return store.getBulletinRevision(uid);
	}

	public UniversalId getBulletinUniversalIdSorted(int index)
	{
		needSortedIdList();
		if(index < 0 || index >= sortedIdList.size())
			return null;
		return  (UniversalId)sortedIdList.get(index);
	}

	public Set getAllUniversalIdsUnsorted()
	{
		// return a COPY to avoid synchronization problems between threads
		return new HashSet(rawIdList);
	}
	
	public UniversalId[] getAllUniversalIdsUnsortedAsArray()
	{
		return (UniversalId[])getAllUniversalIdsUnsorted().toArray(new UniversalId[0]);
	}

	public boolean contains(Bulletin b)
	{
		return contains(b.getUniversalId());
	}

	public synchronized boolean contains(UniversalId id)
	{
		return rawIdList.contains(id);
	}

	public void sortBy(String tag)
	{
		if(tag.equals(sortedBy()))
		{
			sortDir = -sortDir;
		}
		else
		{
			sortTag = tag;
			sortDir = ASCENDING;
		}
		sortExisting();
	}

	public int find(UniversalId id)
	{
		needSortedIdList();
		return sortedIdList.indexOf(id);
	}
	
	public boolean isClosed()
	{
		return isClosed;
	}
	
	public boolean isOpen()
	{
		return !isClosed;
	}
	
	public void setClosed()
	{
		isClosed = true;
	}

	public void setOpen()
	{
		isClosed = false;
	}
	
	public static boolean isNameVisible(String folderName)
	{
		return !folderName.startsWith("*");
	}

	public static boolean isNameLocalized(String folderName)
	{
		return folderName.startsWith("%");
	}

	public void prepareForBulkOperation()
	{
		sortedIdList = null;
	}

	private void insertIntoSortedList(UniversalId uid)
	{
		if(sortedIdList == null)
			return;
		
		if(!canSort())
		{
			sortedIdList.add(uid);
			return;
		}
		
		String thisValue = store.getFieldData(uid, sortTag);
		int index;
		for(index = 0; index < sortedIdList.size(); ++index)
		{
			UniversalId tryUid = getBulletinUniversalIdSorted(index);
			String tryValue = getStore().getFieldData(tryUid, sortTag);
			if(tryValue.compareTo(thisValue) * sortDir > 0)
				break;
		}
		sortedIdList.insertElementAt(uid, index);
	}

	private synchronized void sortExisting()
	{
		class Sorter implements Comparator
		{
			public Sorter(String tagToSortBy, int direction)
			{
				sorterTag = tagToSortBy;
				sorterDir = direction;
			}
			
			public int compare(Object o1, Object o2)
			{
				String value1 = getStore().getFieldData((UniversalId)o1, sorterTag);
				String value2 = getStore().getFieldData((UniversalId)o2, sorterTag);
				return value1.compareTo(value2) * sorterDir;
			}
			
			String sorterTag;
			int sorterDir;
		}
	
		MartusLogger.logBeginProcess("sortFolder " + name);
		Object[] uids = rawIdList.toArray(); 
		if(canSort() && sortTag.length() > 0)
			Arrays.sort(uids, new Sorter(sortTag, sortDir));

		sortedIdList = new Vector();
		for(int i = 0; i < uids.length; ++i)
			sortedIdList.add(uids[i]);
		listeners.forEach(listener -> listener.folderWasSorted());
		MartusLogger.logEndProcess("sortFolder");
	}

	private void needSortedIdList()
	{
		if(sortedIdList == null)
			sortExisting();
	}
	
	private boolean canSort()
	{
		return (isVisible());
	}

	private ClientBulletinStore store;
	private String name;

	private Set<UniversalId> rawIdList;
	private Vector sortedIdList;
	private boolean canRename = true;
	private boolean canDelete = true;
	private String sortTag = "eventdate";
	private int sortDir = ASCENDING;
	
	private boolean isClosed;
	private HashSet<FolderContentsListener> listeners;
}
