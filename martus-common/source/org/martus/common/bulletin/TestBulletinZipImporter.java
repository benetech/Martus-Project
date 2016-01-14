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

package org.martus.common.bulletin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;



public class TestBulletinZipImporter extends TestCaseEnhanced
{

	public TestBulletinZipImporter(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		db = new MockClientDatabase();
		security = MockMartusSecurity.createClient();
	}

	public void testLoadFromFile() throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");
		b.setImmutable();

		File tempFile = createTempFileFromName("$$$MartusTest");
		BulletinForTesting.saveToFile(db, b, tempFile, security);

		Bulletin loaded = new Bulletin(security);
		BulletinZipImporter.loadFromFile(loaded, tempFile, security);
		assertEquals("wrong id?", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("status", b.getStatus(), loaded.getStatus());

		tempFile.delete();
	}

	public void testLoadFromFileEmpty() throws Exception
	{
		verifyLoadFromFileInvalid(MODE_EMPTY_FILE, "MODE_EMPTY_FILE");
		verifyLoadFromFileInvalid(MODE_MISNAMED_HEADER, "MODE_MISNAMED_HEADER");
		verifyLoadFromFileInvalid(MODE_INVALID_HEADER, "MODE_INVALID_HEADER");
		verifyLoadFromFileInvalid(MODE_MISSING_DATA, "MODE_MISSING_DATA");
		verifyLoadFromFileInvalid(MODE_INVALID_DATA, "MODE_INVALID_DATA");
	}

	public void verifyLoadFromFileInvalid(int mode, String label) throws Exception
	{
		MartusCrypto signer = security;

		File tempFile = createTempFileFromName("$$$MartusTest");
		if(mode != MODE_EMPTY_FILE)
		{
			Bulletin b = new Bulletin(security);
			BulletinHeaderPacket headerPacket = b.getBulletinHeaderPacket();

			ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
			headerPacket.writeXml(headerOut, signer);
			byte[] headerBytes = headerOut.toByteArray();

			String badData = "Not a valid data packet";
			byte[] dataBytes = badData.getBytes();

			FileOutputStream outputStream = new FileOutputStream(tempFile);
			ZipOutputStream zipOut = new ZipOutputStream(outputStream);

			ZipEntry headerEntry = new ZipEntry(headerPacket.getLocalId());
			if(mode != MODE_MISSING_DATA)
			{
				ZipEntry dataEntry = new ZipEntry(headerPacket.getFieldDataPacketId());
				zipOut.putNextEntry(dataEntry);
				zipOut.write(dataBytes);
			}

			if(mode == MODE_MISNAMED_HEADER)
				headerEntry = new ZipEntry("misnamed header");
			zipOut.putNextEntry(headerEntry);

			if(mode != MODE_INVALID_HEADER)
				zipOut.write(headerBytes);

			zipOut.close();
		}

		Bulletin loaded = new Bulletin(security);
		try
		{
			BulletinZipImporter.loadFromFile(loaded, tempFile, security);
			fail("should have thrown: " + label);
		}
		catch(IOException e)
		{
			//expected exception
		}
		tempFile.delete();
	}

	public void testSaveToFileWithAttachment() throws Exception
	{
		BulletinStore store = new MockBulletinStore(this);
		ReadableDatabase dbWithAttachments = store.getDatabase();
		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		UniversalId dummyUid = UniversalIdForTesting.createDummyUniversalId();

		Bulletin original = new Bulletin(security);
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setImmutable();
		store.saveBulletinForTesting(original);
		UniversalId uid = original.getUniversalId();

		original = BulletinLoader.loadFromDatabase(dbWithAttachments, DatabaseKey.createImmutableKey(uid), security);
		AttachmentProxy[] originalAttachments = original.getPublicAttachments();
		assertEquals("not one attachment?", 1, originalAttachments.length);
		DatabaseKey key2 = DatabaseKey.createImmutableKey(originalAttachments[0].getUniversalId());
		assertEquals("public attachment wasn't saved?", true,  dbWithAttachments.doesRecordExist(key2));

		AttachmentProxy[] originalPrivateAttachments = original.getPrivateAttachments();
		assertEquals("not one attachment in private?", 1, originalPrivateAttachments.length);
		DatabaseKey keyPrivate = DatabaseKey.createImmutableKey(originalPrivateAttachments[0].getUniversalId());
		assertEquals("private attachment wasn't saved?", true,  dbWithAttachments.doesRecordExist(keyPrivate));

		File tmpFile = createTempFileFromName("$$$MartusTestBullSaveFileAtta1");
		BulletinForTesting.saveToFile(dbWithAttachments, original, tmpFile, security);
		assertTrue("unreasonable file size?", tmpFile.length() > 20);

		ZipFile zip = new ZipFile(tmpFile);
		Enumeration entries = zip.entries();

		ZipEntry dataEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("data id wrong?", "F", dataEntry.getName());
		FieldDataPacket fdp = new FieldDataPacket(dummyUid, StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
		fdp.loadFromXml(new ZipEntryInputStreamWithSeek(zip, dataEntry), security);
		assertEquals("fdp id?", original.getFieldDataPacket().getUniversalId(), fdp.getUniversalId());

		ZipEntry privateEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("private id wrong?", "F", privateEntry.getName());
		FieldDataPacket pdp = new FieldDataPacket(dummyUid, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		pdp.loadFromXml(new ZipEntryInputStreamWithSeek(zip, privateEntry), security);
		assertEquals("pdp id?", original.getPrivateFieldDataPacket().getUniversalId(), pdp.getUniversalId());

		ZipEntry attachmentEntry = (ZipEntry)entries.nextElement();
		verifyAttachmentInZipFile("public", a, sampleBytes1, zip, attachmentEntry);

		ZipEntry attachmentPrivateEntry = (ZipEntry)entries.nextElement();
		verifyAttachmentInZipFile("private", aPrivate, sampleBytes2, zip, attachmentPrivateEntry);

		ZipEntry headerEntry = (ZipEntry)entries.nextElement();
		assertStartsWith("header id wrong?", "B", headerEntry.getName());
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		bhp.loadFromXml(new ZipEntryInputStreamWithSeek(zip, headerEntry), security);
		assertEquals("bhp id?", original.getUniversalId(), bhp.getUniversalId());

		assertEquals("too many entries?", false, entries.hasMoreElements());

		tmpFile.delete();
	}

	public void verifyAttachmentInZipFile(String label, AttachmentProxy a, byte[] bytes, ZipFile zip, ZipEntry attachmentEntry) throws Exception
	{
		assertStartsWith(label + " attachment id wrong?", "A", attachmentEntry.getName());
		ZipEntryInputStreamWithSeek in = new ZipEntryInputStreamWithSeek(zip, attachmentEntry);

		File tempRawFile = createTempFileFromName("$$$MartusTestBullSaveFileAtt2");
		FileOutputStream out = new FileOutputStream(tempRawFile);
		AttachmentPacket.exportRawFileFromXml(in, a.getSessionKey(), security, out);
		out.close();
		assertEquals(label + " wrong size2?", bytes.length, tempRawFile.length());

		byte[] raw = new byte[bytes.length];
		FileInputStream inRaw = new FileInputStream(tempRawFile);
		assertEquals(label + " read count?", raw.length, inRaw.read(raw));
		inRaw.close();
		assertEquals(label + " wrong bytes?", true, Arrays.equals(bytes, raw));

		tempRawFile.delete();
	}

	public void testSaveToFile() throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");

		File tempFile = createTempFileFromName("$$$MartusTest");
		BulletinForTesting.saveToFile(db, b, tempFile, security);
		assertTrue("unreasonable file size?", tempFile.length() > 20);

		ZipFile zip = new ZipFile(tempFile);
		Enumeration entries = zip.entries();

		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();

		assertEquals("no data?", true, entries.hasMoreElements());
		ZipEntry dataEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null data?", dataEntry);
		InputStreamWithSeek dataIn = new ZipEntryInputStreamWithSeek(zip, dataEntry);
		FieldDataPacket data = new FieldDataPacket(uid, StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
		data.loadFromXml(dataIn, security);
		assertEquals("data wrong?", b.get(Bulletin.TAGPUBLICINFO), data.get(Bulletin.TAGPUBLICINFO));

		assertEquals("no private data?", true, entries.hasMoreElements());
		ZipEntry privateDataEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null data?", privateDataEntry);
		InputStreamWithSeek privateDataIn = new ZipEntryInputStreamWithSeek(zip, privateDataEntry);
		FieldDataPacket privateData = new FieldDataPacket(uid, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		privateData.loadFromXml(privateDataIn, security);
		assertEquals("data wrong?", b.get(Bulletin.TAGPRIVATEINFO), privateData.get(Bulletin.TAGPRIVATEINFO));

		assertEquals("no header?", true, entries.hasMoreElements());
		ZipEntry headerEntry = (ZipEntry)entries.nextElement();
		assertNotNull("null header?", headerEntry);
		assertEquals("wrong header name?", b.getLocalId(), headerEntry.getName());
		InputStreamWithSeek headerIn = new ZipEntryInputStreamWithSeek(zip, headerEntry);
		BulletinHeaderPacket header = new BulletinHeaderPacket(security);
		header.loadFromXml(headerIn, security);
		headerIn.close();
		assertEquals("header id wrong?", b.getLocalId(), header.getLocalId());
		assertEquals("wrong data name?", header.getFieldDataPacketId(), dataEntry.getName());
		assertEquals("wrong privatedata name?", header.getPrivateFieldDataPacketId(), privateDataEntry.getName());

		assertEquals("too many entries?", false, entries.hasMoreElements());

		tempFile.delete();
	}

	public void testExportAndImportZipBetweenAccounts() throws Exception
	{
		Bulletin original = new Bulletin(security);
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		File tempFile = createTempFile();

		MartusCrypto otherSecurity = MockMartusSecurity.createOtherClient();

		original.setMutable();
		original.setAllPrivate(true);
		BulletinForTesting.saveToFile(db, original, tempFile, security);
		Bulletin loaded2 = new Bulletin(security);
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("draft private could get public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setMutable();
		original.setAllPrivate(false);
		BulletinForTesting.saveToFile(db,original, tempFile, security);
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("draft public could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("draft public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setImmutable();
		original.setAllPrivate(true);
		BulletinForTesting.saveToFile(db,original, tempFile, security);
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("sealed private could get encrypted public?", "", loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed private could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));

		original.setImmutable();
		original.setAllPrivate(false);
		BulletinForTesting.saveToFile(db,original, tempFile, security);
		BulletinZipImporter.loadFromFile(loaded2, tempFile, otherSecurity);
		assertEquals("sealed public couldn't get encrypted public?", original.get(Bulletin.TAGPUBLICINFO), loaded2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("sealed public could get private?", "", loaded2.get(Bulletin.TAGPRIVATEINFO));
	}

	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};

	static final int MODE_EMPTY_FILE = 0;
	static final int MODE_INVALID_HEADER = 1;
	static final int MODE_MISNAMED_HEADER = 2;
	static final int MODE_MISSING_DATA = 3;
	static final int MODE_INVALID_DATA = 4;

	static MockDatabase db;
	static MartusCrypto security;
}
