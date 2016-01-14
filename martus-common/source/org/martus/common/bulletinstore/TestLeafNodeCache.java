/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeStringWriter;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;

public class TestLeafNodeCache extends TestCaseEnhanced
{
	public TestLeafNodeCache(String name)
	{
		super(name);
	}
	
	/*
	 * This test method is the beginning of exercising a speed optimization that would
	 * update the cache when new bulletins are saved, rather than flushing it.
	 * 
	 * It needs to be fleshed out with more asserts after each call to revisionWasSaved, 
	 * and it need to verify that the HQ cache is updated as well. 2006-02-23 kbs.
	 */
	public void testSaveUpdatesCache() throws Exception
	{
		File tempDirectory = createTempDirectory();
		BulletinStore store = new BulletinStore();
		store.doAfterSigninInitialization(tempDirectory, new MockServerDatabase());
		
		MockMartusSecurity client = MockMartusSecurity.createClient();
		
		BulletinHeaderPacket bhp1 = new BulletinHeaderPacket(client);
		BulletinHeaderPacket bhp2 = new BulletinHeaderPacket(client);
		bhp2.getHistory().add(bhp1.getLocalId());
		BulletinHeaderPacket bhp3 = new BulletinHeaderPacket(client);
		bhp3.getHistory().add(bhp1.getLocalId());
		bhp3.getHistory().add(bhp2.getLocalId());

		BulletinHistoryAndHqCache cache = store.getHistoryAndHqCache();
		assertFalse("cache already valid?", cache.isCacheValid());
		
		// write original, then next revision, then final revision
		saveHeaderPacket(store, bhp1, client);
		cache.revisionWasSaved(bhp1.getUniversalId());
		assertTrue("cache cleared 1?", cache.isCacheValid());
		assertTrue("bhp1 not a leaf?", store.isLeaf(bhp1.getUniversalId()));
		assertFalse("bhp1 is a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		
		saveHeaderPacket(store, bhp2, client);
		cache.revisionWasSaved(bhp2.getUniversalId());
		assertTrue("cache cleared 2?", cache.isCacheValid());
		assertTrue("bhp2 not a leaf?", store.isLeaf(bhp2.getUniversalId()));
		assertTrue("bhp1 not a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		
		saveHeaderPacket(store, bhp3, client);
		cache.revisionWasSaved(bhp3.getUniversalId());
		assertTrue("cache cleared 3?", cache.isCacheValid());
		assertTrue("bhp3 not a leaf?", store.isLeaf(bhp3.getUniversalId()));
		assertTrue("bhp1 not a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		assertTrue("bhp2 not a non-leaf?", store.hasNewerRevision(bhp2.getUniversalId()));

		store.deleteAllBulletins();
		assertFalse("deleted bhp1 is leaf?", store.isLeaf(bhp1.getUniversalId()));
		assertFalse("deleted bhp2 is leaf?", store.isLeaf(bhp2.getUniversalId()));
		assertFalse("deleted bhp1 has newer?", store.hasNewerRevision(bhp1.getUniversalId()));
		assertFalse("deleted bhp2 has newer?", store.hasNewerRevision(bhp2.getUniversalId()));

		// write final revision, then earlier, then original
		saveHeaderPacket(store, bhp3, client);
		cache.revisionWasSaved(bhp3.getUniversalId());
		assertTrue("cache cleared 4?", cache.isCacheValid());
		assertEquals(1, store.getAllBulletinLeafUids().size());
		assertTrue("bhp3 not a leaf?", store.isLeaf(bhp3.getUniversalId()));
		assertTrue("bhp1 not a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		assertTrue("bhp2 not a non-leaf?", store.hasNewerRevision(bhp2.getUniversalId()));
		
		saveHeaderPacket(store, bhp2, client);
		cache.revisionWasSaved(bhp2.getUniversalId());
		assertTrue("cache cleared 5?", cache.isCacheValid());
		assertEquals(1, store.getAllBulletinLeafUids().size());
		assertTrue("bhp3 not a leaf?", store.isLeaf(bhp3.getUniversalId()));
		assertTrue("bhp1 not a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		assertTrue("bhp2 not a non-leaf?", store.hasNewerRevision(bhp2.getUniversalId()));
		
		saveHeaderPacket(store, bhp1, client);
		cache.revisionWasSaved(bhp1.getUniversalId());
		assertTrue("cache cleared 6?", cache.isCacheValid());
		assertEquals(1, store.getAllBulletinLeafUids().size());
		assertTrue("bhp3 not a leaf?", store.isLeaf(bhp3.getUniversalId()));
		assertTrue("bhp1 not a non-leaf?", store.hasNewerRevision(bhp1.getUniversalId()));
		assertTrue("bhp2 not a non-leaf?", store.hasNewerRevision(bhp2.getUniversalId()));
	}
	
	public void testRemove() throws Exception
	{
		File tempDirectory = createTempDirectory();
		BulletinStore store = new BulletinStore();
		store.doAfterSigninInitialization(tempDirectory, new MockServerDatabase());
		BulletinHistoryAndHqCache cache = store.getHistoryAndHqCache();

		MockMartusSecurity client = MockMartusSecurity.createClient();
		
		BulletinHeaderPacket grandparent = new BulletinHeaderPacket(client);
		saveHeaderPacket(store, grandparent, client);
		cache.revisionWasSaved(grandparent.getUniversalId());
		assertEquals(0, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		store.deleteSpecificPacket(getKey(grandparent));
		cache.revisionWasRemoved(grandparent.getUniversalId());
		assertTrue("cache was invalidated?", cache.isCacheValid());
		assertEquals(0, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		saveHeaderPacket(store, grandparent, client);
		cache.revisionWasSaved(grandparent.getUniversalId());
		assertEquals(0, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		

		BulletinHeaderPacket parent = new BulletinHeaderPacket(client);
		parent.getHistory().add(grandparent.getLocalId());
		saveHeaderPacket(store, parent, client);
		cache.revisionWasSaved(parent.getUniversalId());

		BulletinHeaderPacket child = new BulletinHeaderPacket(client);
		child.getHistory().add(grandparent.getLocalId());
		child.getHistory().add(parent.getLocalId());
		saveHeaderPacket(store, child, client);
		cache.revisionWasSaved(child.getUniversalId());

		// removing parent should leave child as a leaf, but not grandparent
		store.deleteSpecificPacket(getKey(parent));
		cache.revisionWasRemoved(parent.getUniversalId());
		assertEquals(2, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		assertEquals(1, cache.getAllKnownDescendents(parent.getUniversalId()).size());
		assertEquals(0, cache.getAllKnownDescendents(child.getUniversalId()).size());
		// and then removing child should make grandparent a leaf
		store.deleteSpecificPacket(getKey(child));
		cache.revisionWasRemoved(child.getUniversalId());
		assertEquals(2, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		assertEquals(1, cache.getAllKnownDescendents(parent.getUniversalId()).size());
		assertEquals(0, cache.getAllKnownDescendents(child.getUniversalId()).size());
		saveHeaderPacket(store, parent, client);
		cache.revisionWasSaved(parent.getUniversalId());
		saveHeaderPacket(store, child, client);
		cache.revisionWasSaved(child.getUniversalId());
		assertEquals(2, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		assertEquals(1, cache.getAllKnownDescendents(parent.getUniversalId()).size());
		assertEquals(0, cache.getAllKnownDescendents(child.getUniversalId()).size());

		// removing child should make parent leaf again
		store.deleteSpecificPacket(getKey(child));
		cache.revisionWasRemoved(child.getUniversalId());
		assertEquals(2, cache.getAllKnownDescendents(grandparent.getUniversalId()).size());
		assertEquals(1, cache.getAllKnownDescendents(parent.getUniversalId()).size());
		assertEquals(0, cache.getAllKnownDescendents(child.getUniversalId()).size());
		saveHeaderPacket(store, child, client);
		cache.revisionWasSaved(child.getUniversalId());
	}

	private void saveHeaderPacket(BulletinStore store, BulletinHeaderPacket bhp, MockMartusSecurity client) throws IOException, RecordHiddenException
	{
		StringWriter writer = new StringWriter();
		bhp.writeXml(writer, client);
		String xml = writer.toString();
		store.getWriteableDatabase().writeRecord(getKey(bhp), xml);
	}

	private DatabaseKey getKey(BulletinHeaderPacket bhp)
	{
		return bhp.createKeyWithHeaderStatus(bhp.getUniversalId());
	}

	public void testLeafNodeCacheSpeed()
	{
		if(!DO_SPEED_TESTS)
			return;
		
		BulletinStore store = new BulletinStore();
		BulletinHistoryAndHqCache cache = new BulletinHistoryAndHqCache(store);
		BulletinHistory history = new BulletinHistory();
		for(int i = 0; i < 10; ++i)
			history.add(UniversalIdForTesting.createDummyUniversalId().getLocalId());
		
		Stopwatch watch = new Stopwatch();
		for(int i = 0; i < 500; ++i)
		{
			UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
			DatabaseKey key = DatabaseKey.createImmutableKey(uid);
			cache.addToCachedLeafInformation(key, history);
		}
		long millisFor500 = watch.elapsed();

		for(int i = 0; i < 1000; ++i)
		{
			UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
			DatabaseKey key = DatabaseKey.createImmutableKey(uid);
			cache.addToCachedLeafInformation(key, history);
		}
		long millisFor1000 = watch.elapsed();
		assertTrue("Took too long?", millisFor1000 < millisFor500 * 4);
	}
	
	public void testLeafNodeCacheDiskSpeed() throws Exception
	{
		if(!DO_SPEED_TESTS)
			return;
		
		BulletinStore store = new BulletinStore();
		File tempDirectory = createTempDirectory();
		MartusSecurity security = MockMartusSecurity.createServer();
		ServerFileDatabase db = new ServerFileDatabase(tempDirectory, security);
		store.doAfterSigninInitialization(tempDirectory, db);
		
		MartusSecurity clientSecurity = MockMartusSecurity.createClient();
		Stopwatch watch = new Stopwatch();
		for(int i = 0; i < 100; ++i)
		{
			Bulletin testBulletin = new Bulletin(clientSecurity);
			store.saveBulletinForTesting(testBulletin);
		}
//		System.out.println("Time to create bulletins: " + watch.elapsed());

		watch.start();
		BulletinHistoryAndHqCache cache = new BulletinHistoryAndHqCache(store);
		assertFalse("cache not flushed?", cache.isCacheValid());
		store.getAllBulletinLeafUids();
		long buildCacheTime = watch.elapsed();
//		System.out.println("Time for initial cache build: " + buildCacheTime);
		assertTrue("Leaf key cache  build too slow? (was " + buildCacheTime + ")", buildCacheTime < 1000);
		
		watch.start();
		store.getAllBulletinLeafUids();
		long queryCacheTime = watch.elapsed();
//		System.out.println("Time to query using the cache: " + queryCacheTime);
		assertTrue("Time to query using cache too slow? (was " + queryCacheTime + ")", queryCacheTime < 100);
		
		Bulletin b = new Bulletin(clientSecurity);
		UnicodeStringWriter writer = UnicodeStringWriter.create();
		watch.start();
		b.getBulletinHeaderPacket().writeXml(writer, clientSecurity);
//		long timeToWrite = watch.elapsed();
//		System.out.println(timeToWrite);
		String xml = writer.toString();
		
		BulletinHeaderPacket bhp = new BulletinHeaderPacket();
		watch.start();
		for(int i = 0; i < 1000; ++i)
		{
			StringInputStreamWithSeek in = new StringInputStreamWithSeek(xml);
			try
			{
				bhp.loadFromXml(in, null, clientSecurity);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw(e);
			}
		}
		long readHeadersTime = watch.elapsed();
//		System.out.println("Time to read header packets: " + readHeadersTime);
		assertTrue("Read packets too slow? (was " + readHeadersTime + ")", readHeadersTime < 10000);
	}
	
	static boolean DO_SPEED_TESTS = false;
}
