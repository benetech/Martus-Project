/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.common.bulletinstore;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

public class BulletinHistoryAndHqCache extends BulletinStoreCache implements Database.PacketVisitor
{
	public BulletinHistoryAndHqCache(BulletinStore storeToUse)
	{
		store = storeToUse;
		clear();
	}
	
	public synchronized void storeWasCleared()
	{
		clear();
	}
	
	public void fillCache()
	{
		fill();
	}

	public synchronized void revisionWasSaved(UniversalId uid)
	{
		fill();
		DatabaseKey key = findKey(store.getDatabase(), uid);
		if(key == null)
		{
			System.out.println("BulletinHistoryAndHqCache.revisionWasSaved null key for " + uid.getLocalId());
			return;
		}
		visit(key);
	}
	
	public synchronized void revisionWasSaved(Bulletin b)
	{
		revisionWasSaved(b.getUniversalId());
	}
	
	public synchronized void revisionWasRemoved(UniversalId uid)
	{
		// No action required
	}
	
	public synchronized Set getAllKnownDescendents(UniversalId uid)
	{
		fill();
		Set descendents = Collections.synchronizedSet(new HashSet());
		Set children = getChildren(uid);
		Iterator it = children.iterator();
		while(it.hasNext())
		{
			UniversalId child = (UniversalId)it.next();
			descendents.add(child);
			descendents.addAll(getAllKnownDescendents(child));
		}
		
		return descendents;
	}
	
	public synchronized Vector getFieldOffices(String hqAccountId)
	{
		fill();
		return new Vector(internalGetFieldOffices(hqAccountId));
	}
	
	// TODO: NOTE! There is a corner case where this could return an incorrect value:
	// Client A starts a scan, and gets an error
	// Client B clears the cache and starts their own scan
	// Client A asks if there were errors, and is told "no"
	// The proper way to handle it is for all the getXxx calls to return an 
	// object that contains both the data, and whether or not there was an error
	public synchronized boolean hadErrors()
	{
		return hitErrorsDuringScan;
	}
	
	
	
	
	
	
	
	private void fill()
	{
		if(isValid)
			return;

		String STATUS_TEXT = "BulletinHistoryAndHqCache rebuild";
		MartusLogger.logBeginProcess(STATUS_TEXT);
		clear();
		store.visitAllBulletinRevisions(this);
		isValid = true;
		MartusLogger.logEndProcess(STATUS_TEXT);
	}

	public synchronized void clear()
	{
		MartusLogger.log("BulletinHistoryAndHqCache cleared");
		isValid = false;
		hitErrorsDuringScan = false;
		uidToChildrenMap = Collections.synchronizedMap(new HashMap());
		fieldOfficesPerHq = Collections.synchronizedMap(new HashMap());
	}

	private Set internalGetFieldOffices(String hqAccountId)
	{
		Set results = (Set)fieldOfficesPerHq.get(hqAccountId);
		if(results == null)
			results = Collections.synchronizedSet(new HashSet());
		
		return results;
	}

	protected synchronized boolean isCacheValid()
	{
		return isValid;
	}
	
	public void visit(DatabaseKey key)
	{
		try
		{
			BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(getDatabase(), key, getSecurity());
			addToCachedHqInformation(key, bhp.getAuthorizedToReadKeys());
			addToCachedLeafInformation(key, bhp.getHistory());
		}
		catch(Exception e)
		{
			hitErrorsDuringScan = true;
			// FIXME: Before next server release: change this to use a logger so we see problems on the server!
			//e.printStackTrace();
		}
	}

	private ReadableDatabase getDatabase()
	{
		return store.getDatabase();
	}
	
	private MartusCrypto getSecurity()
	{
		return store.getSignatureVerifier();
	}
	
	private void addToCachedHqInformation(DatabaseKey key, HeadquartersKeys hqs)
	{
		for(int i=0; i < hqs.size(); ++i)
		{
			String thisHqKey = hqs.get(i).getPublicKey();
			Set fieldOffices = internalGetFieldOffices(thisHqKey);
			if(!fieldOffices.contains(key.getAccountId()))
			{
				fieldOffices.add(key.getAccountId());
				fieldOfficesPerHq.put(thisHqKey, fieldOffices);
			}
		}
	}

	protected void addToCachedLeafInformation(DatabaseKey key, BulletinHistory history)
	{
		UniversalId child = key.getUniversalId();
		uidToChildrenMap.put(child, getChildren(child));

		for(int i=history.size() - 1; i >= 0; --i)
		{
			String thisLocalId = history.get(i);
			UniversalId parent = UniversalId.createFromAccountAndLocalId(key.getAccountId(), thisLocalId);
			Set children = getChildren(parent);
			children.add(child);
			uidToChildrenMap.put(parent, children);
			
			child = parent;
		}
	}

	private Set getChildren(UniversalId parent)
	{
		Set children = (Set)uidToChildrenMap.get(parent);
		if(children == null)
			children = Collections.synchronizedSet(new HashSet());
		return children;
	}
	
	private BulletinStore store;
	private boolean isValid;
	private boolean hitErrorsDuringScan;
	private Map uidToChildrenMap;
	private Map fieldOfficesPerHq;
}

