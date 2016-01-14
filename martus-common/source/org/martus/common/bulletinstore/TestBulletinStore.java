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

package org.martus.common.bulletinstore;

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.martus.common.LoggerToNull;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.ReadableDatabase.PacketVisitor;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinStore extends TestCaseEnhanced
{
	static Stopwatch sw = new Stopwatch();
	
    public TestBulletinStore(String name) {
        super(name);
    }

	public void TRACE(String text)
	{
		//System.out.println("before " + text + ": " + sw.elapsed());
		sw.start();
	}


    public void setUp() throws Exception
    {
    	super.setUp();
    	db = new MockClientDatabase();
    	security1 = MockMartusSecurity.createClient();
    	security2 = MockMartusSecurity.createOtherClient();
		store = new BulletinStore();
		store.doAfterSigninInitialization(createTempDirectory(), db);
		store.setSignatureGenerator(security1);

    	if(tempFile1 == null)
    	{
			tempFile1 = createTempFileWithData(sampleBytes1);
    	}
    }

    public void tearDown() throws Exception
    {
    	assertEquals("Still some mock streams open?", 0, db.getOpenStreamCount());
		store.deleteAllData();
		super.tearDown();
	}
    
    public void testLeafKeyCache() throws Exception
	{
    	Bulletin one = createAndSaveBulletin(security1);
    	store.saveBulletinForTesting(one);
       	assertEquals("Leaf Keys should be 1?", 1, store.getBulletinCount());
       	assertTrue("Saved should be leaf", store.isLeaf(one.getUniversalId()));
       	Bulletin clone = createAndSaveClone(one);
    	assertEquals("not just clone?", 1, store.getBulletinCount());
       	assertTrue("Clone should be leaf", store.isLeaf(clone.getUniversalId()));
       	assertFalse("Original should not be leaf", store.isLeaf(one.getUniversalId()));
       	Bulletin clone2 = createAndSaveClone(clone);
    	assertEquals("not just clone2?", 1, store.getBulletinCount());
       	assertTrue("Clone2 should be leaf", store.isLeaf(clone2.getUniversalId()));
       	assertFalse("Clone should not be leaf", store.isLeaf(clone.getUniversalId()));
       	assertFalse("Original should not be leaf", store.isLeaf(one.getUniversalId()));

       	Bulletin anotherCloneOfOriginal = createAndSaveClone(one);
       	assertTrue("Another clone should be leaf", store.isLeaf(anotherCloneOfOriginal.getUniversalId()));
       	assertTrue("Clone2 should still be leaf", store.isLeaf(clone2.getUniversalId()));
       	assertFalse("Clone should still not be leaf", store.isLeaf(clone.getUniversalId()));
       	assertFalse("Original should still not be leaf", store.isLeaf(one.getUniversalId()));
       	
       	store.deleteBulletinRevisionFromDatabase(clone2.getBulletinHeaderPacket());
    	store.deleteBulletinRevisionFromDatabase(clone.getBulletinHeaderPacket());
    	assertEquals("didn't delete?", 1, store.getBulletinCount());
       	assertTrue("Another clone should still be leaf", store.isLeaf(anotherCloneOfOriginal.getUniversalId()));
       	assertFalse("Clone2 should no longer be leaf", store.isLeaf(clone2.getUniversalId()));
       	assertFalse("Clone should no longer not be leaf", store.isLeaf(clone.getUniversalId()));
       	assertFalse("Original should still not be leaf again", store.isLeaf(one.getUniversalId()));

    	store.deleteBulletinRevisionFromDatabase(anotherCloneOfOriginal.getBulletinHeaderPacket());
       	assertFalse("Another clone should no longer be leaf", store.isLeaf(anotherCloneOfOriginal.getUniversalId()));
       	assertTrue("Original should be leaf again", store.isLeaf(one.getUniversalId()));
       	
    	File tempZip = createTempFile();
    	store.saveBulletinForTesting(one);
    	BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), one.getDatabaseKey(), tempZip, store.getSignatureVerifier());
    	store.deleteBulletinRevision(one.getDatabaseKey());
    	assertEquals("not ready for import?", 0, store.getBulletinCount());
    	store.importBulletinZipFile(new ZipFile(tempZip));
    	assertEquals("didn't import?", 1, store.getBulletinCount());
    	tempZip.delete();

    	Vector toHide = new Vector();
    	toHide.add(one.getUniversalId());
    	store.hidePackets(toHide, new LoggerToNull());
    	assertEquals("didn't hide?", 0, store.getBulletinCount());
    	
    	store.saveBulletinForTesting(clone);
    	store.deleteAllData();
    	assertEquals("didn't delete all?", 0, store.getBulletinCount());
	}
    
	public void testMissingInvalidAttachment() throws Exception
	{
		Bulletin b1 = new Bulletin(security1);

		File tempFile2 = createTempFileWithData(sampleBytes2);
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b1.addPublicAttachment(a1);
		b1.addPrivateAttachment(a2);
		assertEquals("Should have 1 public attachment", 1, b1.getPublicAttachments().length);
		assertEquals("Should have 1 private attachment", 1, b1.getPrivateAttachments().length);
		b1.setImmutable();
		store.saveEncryptedBulletinForTesting(b1);

		Bulletin loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(b1.getUniversalId()), security1);
		assertEquals("not valid attachments?", true, store.areAttachmentsValid(loaded));
		assertEquals("not valid bulletin?", true, store.isBulletinValid(loaded));

		AttachmentProxy[] privateProxy = loaded.getPrivateAttachments();
		UniversalId id = privateProxy[0].getUniversalId();
		DatabaseKey key = DatabaseKey.createImmutableKey(id);
		
		assertTrue("Attachment should exist",getDatabase().doesRecordExist(key));

		getDatabase().discardRecord(key);
		assertFalse("Attachment should not exist",getDatabase().doesRecordExist(key));
		loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(b1.getUniversalId()), security1);
		assertEquals("not invalid for private attachment missing?", false, store.areAttachmentsValid(loaded));
		assertEquals("not invalid for private attachment missing, Bulletin valid?", false, store.isBulletinValid(loaded));

		b1.addPrivateAttachment(a2);
		store.saveEncryptedBulletinForTesting(b1);
		
		loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(b1.getUniversalId()), security1);
		assertEquals("Should now be valid both attachments are present.", true, store.areAttachmentsValid(loaded));
		assertEquals("Should now be valid both attachments are present, Bulletin Not Valid.", true, store.isBulletinValid(loaded));
		
		loaded.setIsNonAttachmentDataValid(false);
		assertEquals("Attachments should still be valid.", true, store.areAttachmentsValid(loaded));
		assertEquals("Bulletin should not be valid.", false, store.isBulletinValid(loaded));

		AttachmentProxy[] publicProxy = loaded.getPublicAttachments();
		id = publicProxy[0].getUniversalId();
		key = DatabaseKey.createImmutableKey(id);
		getDatabase().writeRecordEncrypted(key,sampleBytes2.toString(), security1);
		
		loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(b1.getUniversalId()), security1);
		assertEquals("not invalid for modified public attachment?", false, store.areAttachmentsValid(loaded));
		assertEquals("not invalid for modified public attachment, Bulletin Valid?", false, store.isBulletinValid(loaded));
	}
	
	public void testPendingAttachment() throws Exception
	{
		Bulletin b1 = new Bulletin(security1);

		File tempFile2 = createTempFileWithData(sampleBytes2);
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b1.addPublicAttachment(a1);
		b1.addPrivateAttachment(a2);
		b1.setImmutable();
		assertTrue("Pending attachments not valid?", store.areAttachmentsValid(b1));
		store.saveEncryptedBulletinForTesting(b1);
	}
	

    public void testHasNewerRevision() throws Exception
	{
		Bulletin original = createAndSaveBulletin(security1);
		Bulletin clone = createAndSaveClone(original);
		
		assertFalse("has newer than the clone?", store.hasNewerRevision(clone.getUniversalId()));
		assertTrue("didn't find the clone?", store.hasNewerRevision(original.getUniversalId()));
	}
    
    public void testRemoveBulletinFromStore() throws Exception
	{
    	Bulletin unrelated = createAndSaveBulletin(security1);
		assertEquals("didn't create unrelated bulletin?", 1, store.getBulletinCount());

		Bulletin original = createAndSaveBulletin(security1);
		Bulletin clone = createAndSaveClone(original);
		store.removeBulletinFromStore(clone);
		assertEquals("didn't delete clone and ancestor?", 1, store.getBulletinCount());
		
		store.removeBulletinFromStore(unrelated);
		assertEquals("didn't delete unrelated?", 0, store.getBulletinCount());
	}
    
    public void testRemoveBulletinWithIncompleteHistory() throws Exception
	{
		Bulletin original = createAndSaveBulletin(security1);
		Bulletin version1 = createAndSaveClone(original);
		Bulletin version2 = createAndSaveClone(version1);
		Bulletin version3 = createAndSaveClone(version2);
		store.removeBulletinFromStore(version3);
		store.saveBulletinForTesting(version3);
		store.removeBulletinFromStore(version3);
	}

	public void testGetBulletinCount() throws Exception
	{
		Bulletin original = createAndSaveBulletin(security1);
		createAndSaveClone(original);
		assertEquals(1, store.getBulletinCount());
	}
	
    

	public void testVisitAllBulletinRevisions() throws Exception
	{
		TRACE("testVisitAllBulletins");

		class BulletinUidCollector implements Database.PacketVisitor
		{
			BulletinUidCollector(BulletinStore store)
			{
				store.visitAllBulletinRevisions(this);
			}

			public void visit(DatabaseKey key)
			{
				uids.add(key.getUniversalId());
			}

			Vector uids = new Vector();
		}

		assertEquals("not empty?", 0, new BulletinUidCollector(store).uids.size());

		Bulletin b = createAndSaveBulletin(security1);
		Vector one = new BulletinUidCollector(store).uids;
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = createAndSaveBulletin(security2);
		Vector two = new BulletinUidCollector(store).uids;
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}

	public void testScanForLeafUids() throws Exception
	{
		Bulletin other = createAndSaveBulletin(security1);
		
		Bulletin one = new Bulletin(security1);
		one.setImmutable();

		Bulletin two = new Bulletin(security1);
		two.setImmutable();
		
		verifyCloneIsLeaf("Test1", one, two, other.getUniversalId());
		store.deleteAllBulletins();
		other = createAndSaveBulletin(security1);
		verifyCloneIsLeaf("Test2", two, one, other.getUniversalId());
	}
	
	public void testVisitAllBulletins() throws Exception
	{
		Bulletin original1 = createAndSaveBulletin(security1);
		Bulletin clone1 = createAndSaveClone(original1);
		
		Bulletin original2 = createAndSaveBulletin(security2);
		Bulletin clone2a = createAndSaveClone(original2);
		Bulletin clone2b = createAndSaveClone(original2);
		Bulletin clone2bx = createAndSaveClone(clone2b);
		
		class SimpleCollector implements PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				result.add(key.getLocalId());
			}

			Vector result = new Vector();
		}
		
		SimpleCollector collector = new SimpleCollector();
		store.visitAllBulletins(collector);
		
		assertEquals(3, collector.result.size());
		assertContains(clone1.getLocalId(), collector.result);
		assertContains(clone2a.getLocalId(), collector.result);
		assertContains(clone2bx.getLocalId(), collector.result);
		
	}

	public void testVisitAllBulletinsForAccount() throws Exception
	{
		Bulletin original1 = createAndSaveBulletin(security1);
		createAndSaveClone(original1);
		
		Bulletin original2 = createAndSaveBulletin(security2);
		Bulletin clone2a = createAndSaveClone(original2);
		Bulletin clone2b = createAndSaveClone(original2);
		Bulletin clone2bx = createAndSaveClone(clone2b);
		
		class SimpleCollector implements PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				result.add(key.getLocalId());
			}

			Vector result = new Vector();
		}
		
		SimpleCollector collector = new SimpleCollector();
		store.visitAllBulletinsForAccount(collector, security2.getPublicKeyString());
		
		assertEquals(2, collector.result.size());
		assertContains(clone2a.getLocalId(), collector.result);
		assertContains(clone2bx.getLocalId(), collector.result);
		
	}

	public void testImportBulletinPacketsFromZipFileToDatabase() throws Exception
	{
		MartusCrypto authorSecurity = MockMartusSecurity.createClient();
		BulletinStore fromStore = new MockBulletinStore(this);
		Bulletin b = new Bulletin(authorSecurity);
		b.setAllPrivate(false);
		b.setImmutable();
		fromStore.saveBulletinForTesting(b);
		
		File destFile = createTempFile();
		DatabaseKey key = DatabaseKey.createImmutableKey(b.getUniversalId());
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(fromStore.getDatabase(), key, destFile, authorSecurity);
		ZipFile zip = new ZipFile(destFile);
		
		BulletinStore hqStore = new MockBulletinStore(this);
		hqStore.setSignatureGenerator(MockMartusSecurity.createHQ());
		verifyImportZip(hqStore, key, zip);
		hqStore.deleteAllData();
		
		BulletinStore otherStore = new MockBulletinStore(this);
		otherStore.setSignatureGenerator(MockMartusSecurity.createOtherClient());
		verifyImportZip(otherStore, key, zip);
		otherStore.deleteAllData();

		verifyImportZip(store, key, zip);
	}

	private void verifyImportZip(BulletinStore storeToUse, DatabaseKey key, ZipFile zip) throws Exception
	{
		storeToUse.importBulletinZipFile(zip);
		BulletinLoader.loadFromDatabase(storeToUse.getDatabase(), key, storeToUse.getSignatureGenerator());
	}
	
	private void verifyCloneIsLeaf(String msg, Bulletin original, Bulletin clone, UniversalId otherUid) throws Exception
	{
		original.setHistory(new BulletinHistory());
		store.saveBulletinForTesting(original);

		BulletinHistory history = new BulletinHistory();
		history.add(original.getLocalId());
		clone.setHistory(history);
		store.saveBulletinForTesting(clone);

		Set leafUids = store.getAllBulletinLeafUids();
		assertContains(msg+ ": missing clone?", clone.getUniversalId(), leafUids);
		assertContains(msg+ ": missing other?", otherUid, leafUids);
		assertEquals(msg+ ": wrong leaf count?", 2, leafUids.size());
		assertTrue(msg+ ": clone not leaf?", store.isLeaf(clone.getUniversalId()));
		assertFalse(msg+ ": original is leaf?", store.isLeaf(original.getUniversalId()));
	}

	private Bulletin createAndSaveBulletin(MockMartusSecurity security) throws Exception
	{
		Bulletin b = new Bulletin(security);
		store.saveBulletinForTesting(b);
		return b;
	}

	private Bulletin createAndSaveClone(Bulletin original) throws Exception
	{
		if(original.getFieldDataPacket().getAttachments().length > 0)
			fail("Not tested for attachments!");
		if(original.getPrivateFieldDataPacket().getAttachments().length > 0)
			fail("Not tested for attachments!");
		Bulletin clone = new Bulletin(original.getSignatureGenerator());
		BulletinHistory history = new BulletinHistory();
		history.add(original.getLocalId());
		clone.setHistory(history);
		store.saveBulletinForTesting(clone);
		return clone;
	}
	
	private Database getDatabase()
	{
		return db;
	}


	private static BulletinStore store;
	private static MockMartusSecurity security1;
	private static MockMartusSecurity security2;
	private static MockClientDatabase db;

	private static File tempFile1;
	private static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	private static final byte[] sampleBytes2 = {9, 17, 45, 0, 77};
}
