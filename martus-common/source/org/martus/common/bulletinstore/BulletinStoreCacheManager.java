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

import java.util.Vector;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinStoreCacheManager
{
	public BulletinStoreCacheManager()
	{
		caches = new Vector();
	}
	
	public void addCache(BulletinStoreCache cacheToAdd)
	{
		caches.add(cacheToAdd);
	}
	
	public synchronized void storeWasCleared()
	{
		for(int i = 0; i < caches.size(); ++i)
		{
			BulletinStoreCache cache = (BulletinStoreCache)caches.get(i);
			cache.storeWasCleared();
		}
	}
	
	public synchronized void revisionWasSaved(UniversalId uid) throws Exception
	{
		for(int i = 0; i < caches.size(); ++i)
		{
			BulletinStoreCache cache = (BulletinStoreCache)caches.get(i);
			cache.revisionWasSaved(uid);
		}
	}
	
	public synchronized void revisionWasSaved(Bulletin b) throws Exception
	{
		for(int i = 0; i < caches.size(); ++i)
		{
			BulletinStoreCache cache = (BulletinStoreCache)caches.get(i);
			cache.revisionWasSaved(b);
		}
	}
	
	public synchronized void revisionWasRemoved(UniversalId uid)
	{
		for(int i = 0; i < caches.size(); ++i)
		{
			BulletinStoreCache cache = (BulletinStoreCache)caches.get(i);
			cache.revisionWasRemoved(uid);
		}
	}

	public void clearCache()
	{
		for(int i = 0; i < caches.size(); ++i)
		{
			BulletinStoreCache cache = (BulletinStoreCache)caches.get(i);
			cache.clear();
		}
	}

	private Vector caches;

}