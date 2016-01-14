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

package org.martus.common.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockDatabase;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinStoreSaveBulletin extends TestCaseEnhanced
{
	public TestBulletinStoreSaveBulletin(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(tempFile1 == null)
		{
			tempFile1 = createTempFileWithData(sampleBytes1);
			tempFile2 = createTempFileWithData(sampleBytes2);
			tempFile3 = createTempFileWithData(sampleBytes3);
			tempFile4 = createTempFileWithData(sampleBytes4);
			tempFile5 = createTempFileWithData(sampleBytes5);
			tempFile6 = createTempFileWithData(sampleBytes6);
		}
		proxy1 = new AttachmentProxy(tempFile1);
		proxy2 = new AttachmentProxy(tempFile2);
		proxy3 = new AttachmentProxy(tempFile3);
		proxy4 = new AttachmentProxy(tempFile4);
		proxy5 = new AttachmentProxy(tempFile5);
		proxy6 = new AttachmentProxy(tempFile6);

		security = MockMartusSecurity.createClient();
		store = new MockBulletinStore(this);
		store.setSignatureGenerator(security);
	}

	public void testSaveToDatabase() throws Exception
	{
		assertEquals(0, getDatabase().getAllKeys().size());

		Bulletin b = new Bulletin(security);
		b.set("summary", "New bulletin");
		store.saveEncryptedBulletinForTesting(b);
		DatabaseKey headerKey1 = DatabaseKey.createLegacyKey(b.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey1 = DatabaseKey.createLegacyKey(b.getFieldDataPacket().getUniversalId());
		assertEquals("saved 1", 3, getDatabase().getAllKeys().size());
		assertEquals("saved 1 header key", true,getDatabase().doesRecordExist(headerKey1));
		assertEquals("saved 1 data key", true,getDatabase().doesRecordExist(dataKey1));

		// re-saving the same bulletin replaces the old one
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("resaved 1", 3, getDatabase().getAllKeys().size());
		assertEquals("resaved 1 header key", true,getDatabase().doesRecordExist(headerKey1));
		assertEquals("resaved 1 data key", true,getDatabase().doesRecordExist(dataKey1));

		Bulletin b3 = BulletinLoader.loadFromDatabase(getDatabase(), headerKey1, security);
		assertEquals("id", b.getLocalId(), b3.getLocalId());
		assertEquals("summary", b.get("summary"), b3.get("summary"));

		// unsaved bulletin changes should not be in the store
		Bulletin b2 = BulletinLoader.loadFromDatabase(getDatabase(), headerKey1, security);
		b2.set("summary", "not saved yet");
		Bulletin b4 = BulletinLoader.loadFromDatabase(getDatabase(), headerKey1, security);
		assertEquals("id", b.getLocalId(), b4.getLocalId());
		assertEquals("summary", b.get("summary"), b4.get("summary"));

		// saving a new bulletin with a non-empty id should retain that id
		b = new Bulletin(security);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved another", 6, getDatabase().getAllKeys().size());
		assertEquals("old header key", true, getDatabase().doesRecordExist(headerKey1));
		assertEquals("old data key", true, getDatabase().doesRecordExist(dataKey1));
		DatabaseKey newHeaderKey = DatabaseKey.createLegacyKey(b.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey newDataKey = DatabaseKey.createLegacyKey(b.getFieldDataPacket().getUniversalId());
		assertEquals("new header key", true, getDatabase().doesRecordExist(newHeaderKey));
		assertEquals("new data key", true, getDatabase().doesRecordExist(newDataKey));
	}

	public void testSaveToDatabaseWithPendingAttachment() throws Exception
	{
		Bulletin b = new Bulletin(security);
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		b.addPublicAttachment(a);
		String[] attachmentIds = b.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("one attachment", 1, attachmentIds.length);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved", 4, getDatabase().getAllKeys().size());

		Bulletin got = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(b.getUniversalId()), security);
		assertEquals("id", b.getLocalId(), got.getLocalId());
		assertEquals("attachment count", b.getPublicAttachments().length, got.getPublicAttachments().length);
	}

	public void testSaveToDatabaseWithAttachment() throws Exception
	{
		Bulletin b = new Bulletin(security);
		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved", 5, getDatabase().getAllKeys().size());

		Bulletin got1 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		verifyLoadedBulletin("First load", b, got1);
	}

	public void testSaveToDatabaseAllPrivate() throws Exception
	{
		Bulletin somePublicMutable = new Bulletin(security);
		somePublicMutable.setAllPrivate(false);
		somePublicMutable.setMutable();
		store.saveEncryptedBulletinForTesting(somePublicMutable);
		assertEquals("public mutable was not encrypted?", true, somePublicMutable.getFieldDataPacket().isEncrypted());

		Bulletin allPrivateMutable = new Bulletin(security);
		allPrivateMutable.setAllPrivate(true);
		allPrivateMutable.setMutable();
		store.saveEncryptedBulletinForTesting(allPrivateMutable);
		assertEquals("private mutable was not encrypted?", true, allPrivateMutable.getFieldDataPacket().isEncrypted());

		Bulletin somePublicSealed = new Bulletin(security);
		somePublicSealed.setAllPrivate(false);
		somePublicSealed.setImmutable();
		store.saveEncryptedBulletinForTesting(somePublicSealed);
		assertEquals("public sealed was encrypted?", false, somePublicSealed.getFieldDataPacket().isEncrypted());

		Bulletin allPrivateSealed = new Bulletin(security);
		allPrivateSealed.setAllPrivate(true);
		allPrivateSealed.setImmutable();
		store.saveEncryptedBulletinForTesting(somePublicSealed);
		assertEquals("private sealed was encrypted?", true, allPrivateSealed.getFieldDataPacket().isEncrypted());
	}


	public void testReSaveToDatabaseWithAttachments() throws Exception
	{
		Bulletin b = new Bulletin(security);
		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved", 5, getDatabase().getAllKeys().size());
		Bulletin got1 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		store.saveEncryptedBulletinForTesting(got1);
		assertEquals("resaved", 5, getDatabase().getAllKeys().size());

		Bulletin got2 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		verifyLoadedBulletin("Reload after save", got1, got2);
	}

	public void testReSaveToDatabaseAddAttachments() throws Exception
	{
		Bulletin b = new Bulletin(security);
		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.addPrivateAttachment(proxy4);
		b.addPrivateAttachment(proxy5);
		store.saveEncryptedBulletinForTesting(b);
		Bulletin got1 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);

		got1.clearAllUserData();
		got1.addPublicAttachment(proxy1);
		got1.addPublicAttachment(proxy2);
		got1.addPublicAttachment(proxy3);
		got1.addPrivateAttachment(proxy4);
		got1.addPrivateAttachment(proxy5);
		got1.addPrivateAttachment(proxy6);
		store.saveEncryptedBulletinForTesting(got1);
		assertEquals("resaved", 9, getDatabase().getAllKeys().size());

		Bulletin got3 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		verifyLoadedBulletin("Reload after save", got1, got3);

		String[] publicAttachmentIds = got3.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("wrong public attachment count in bhp?", 3, publicAttachmentIds.length);
		String[] privateAttachmentIds = got3.getBulletinHeaderPacket().getPrivateAttachmentIds();
		assertEquals("wrong private attachment count in bhp?", 3, privateAttachmentIds.length);
	}

	public void testReSaveToDatabaseRemoveAttachment() throws Exception
	{
		Bulletin b = new Bulletin(security);
		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		b.addPublicAttachment(proxy1);
		b.addPublicAttachment(proxy2);
		b.addPrivateAttachment(proxy3);
		b.addPrivateAttachment(proxy4);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved key count", 7, getDatabase().getAllKeys().size());
		Bulletin got1 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		AttachmentProxy keep = got1.getPublicAttachments()[1];
		AttachmentProxy keepPrivate = got1.getPrivateAttachments()[1];

		got1.clearAllUserData();
		got1.addPublicAttachment(keep);
		got1.addPrivateAttachment(keepPrivate);
		store.saveEncryptedBulletinForTesting(got1);
		assertEquals("resaved modified", 5, getDatabase().getAllKeys().size());

		Bulletin got3 = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		verifyLoadedBulletin("Reload after save", got1, got3);

		String[] publicAttachmentIds = got3.getBulletinHeaderPacket().getPublicAttachmentIds();
		assertEquals("wrong public attachment count in bhp?", 1, publicAttachmentIds.length);
		String[] privateAttachmentIds = got3.getBulletinHeaderPacket().getPrivateAttachmentIds();
		assertEquals("wrong private attachment count in bhp?", 1, privateAttachmentIds.length);
	}


	protected void verifyLoadedBulletin(String tag, Bulletin original, Bulletin got) throws Exception
	{
		assertEquals(tag + " id", original.getUniversalId(), got.getUniversalId());
		AttachmentProxy[] originalAttachments = got.getPublicAttachments();
		assertEquals(tag + " wrong public attachment count?", original.getPublicAttachments().length, originalAttachments.length);
		verifyAttachments(tag + "public", got, originalAttachments);

		AttachmentProxy[] originalPrivateAttachments = got.getPrivateAttachments();
		assertEquals(tag + " wrong private attachment count?", original.getPrivateAttachments().length, originalPrivateAttachments.length);
		verifyAttachments(tag + "private", got, originalPrivateAttachments);
	}

	protected void verifyAttachments(String tag, Bulletin got, AttachmentProxy[] originalAttachments) throws Exception
	{
		for(int i=0; i < originalAttachments.length; ++i)
		{
			AttachmentProxy gotA = originalAttachments[i];
			String localId = gotA.getUniversalId().getLocalId();
			DatabaseKey key1 = DatabaseKey.createLegacyKey(gotA.getUniversalId());
			assertEquals(tag + i + " missing original record?", true,  getDatabase().doesRecordExist(key1));

			File tempFile = createTempFileFromName("$$$MartusTestBullSvAtt");
			BulletinLoader.extractAttachmentToFile(getDatabase(), gotA, security, tempFile);
			FileInputStream in = new FileInputStream(tempFile);
			byte[] gotBytes = new byte[in.available()];
			in.read(gotBytes);
			in.close();
			byte[] expectedBytes = null;
			if(localId.equals(proxy1.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes1;
			else if(localId.equals(proxy2.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes2;
			else if(localId.equals(proxy3.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes3;
			else if(localId.equals(proxy4.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes4;
			else if(localId.equals(proxy5.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes5;
			else if(localId.equals(proxy6.getUniversalId().getLocalId()))
				expectedBytes = sampleBytes6;


			assertEquals(tag + i + "got wrong data length?", expectedBytes.length, gotBytes.length);
			assertEquals(tag + i + "got bad data?", true, Arrays.equals(gotBytes, expectedBytes));
			tempFile.delete();
		}
	}

	public void testExtractAttachment() throws Exception
	{
		Bulletin original = new Bulletin(security);
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		original.addPublicAttachment(a1);
		original.addPublicAttachment(a2);
		store.saveEncryptedBulletinForTesting(original);
		assertEquals("wrong record count", 5, getDatabase().getRecordCount());

		Bulletin loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(original.getUniversalId()), security);
		assertNotNull("not saved?", loaded);
		AttachmentProxy[] list = loaded.getPublicAttachments();
		assertEquals("count wrong?", 2, list.length);

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		BulletinLoader.extractAttachmentToStream(getDatabase(), list[0], security, result);
		assertTrue("Wrong bytes?", Arrays.equals(result.toByteArray(), sampleBytes1));
	}

	MockDatabase getDatabase()
	{
		return (MockDatabase)store.getDatabase();
	}

	static File tempFile1;
	static File tempFile2;
	static File tempFile3;
	static File tempFile4;
	static File tempFile5;
	static File tempFile6;

	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	static final byte[] sampleBytes3 = {6,5,0,4,7,5,5,4,4,0};
	static final byte[] sampleBytes4 = {12,34,56};
	static final byte[] sampleBytes5 = {9,8,7,6,5};
	static final byte[] sampleBytes6 = {1,3,5,7,9,11,13};

	static AttachmentProxy proxy1;
	static AttachmentProxy proxy2;
	static AttachmentProxy proxy3;
	static AttachmentProxy proxy4;
	static AttachmentProxy proxy5;
	static AttachmentProxy proxy6;

	BulletinStore store;
	MartusCrypto security;

}
