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
package org.martus.amplifier.attachment.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.martus.amplifier.attachment.AttachmentStorageException;
import org.martus.amplifier.attachment.DataManager;
import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.datasynch.BulletinExtractor;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinField;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.DirectoryUtils;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;


public class TestFileSystemDataManager 
	extends TestAbstractDataManager
{
	
	public TestFileSystemDataManager(String name) 
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPair();
		dataManager = new FileSystemDataManager(getTestBasePath(), security);
	}
	
	public void testMissingAccountMap() throws Exception
	{
		File missingAccountMap = null;
		File emptyAccount = null;
		try
		{
			missingAccountMap = createTempDirectory();
			emptyAccount = new File(missingAccountMap.getAbsolutePath(), "ab00");
			emptyAccount.deleteOnExit();
			emptyAccount.mkdir();
			new FileSystemDataManager(missingAccountMap.getAbsolutePath());
			fail("Should have thrown");
		}
		catch (MissingAccountMapException expectedException)
		{
		}		
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(missingAccountMap);
		}
	}

	public void testInvalidAccountMap() throws Exception
	{
		File baseDir = null;
		File accountDir = null;
		try
		{
			baseDir = createTempDirectory();
			accountDir = new File(baseDir.getAbsolutePath(), "ab00");
			accountDir.deleteOnExit();
			accountDir.mkdir();
			File accountMap = new File(baseDir.getAbsolutePath(), "acctmap.txt");
			accountMap.deleteOnExit();
			accountMap.createNewFile();
			new FileSystemDataManager(baseDir.getAbsolutePath());
			fail("Should have thrown");
		}
		catch (MissingAccountMapSignatureException expectedException)
		{
		}		
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(baseDir);
		}
	}

	public void testFileSystemClearAllAttachments() 
		throws AttachmentStorageException, IOException
	{
		UniversalId id = UniversalIdForTesting.createDummyUniversalId();
		String testString = "FileSystemClearAll";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			dataManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		
		dataManager.clearAllAttachments();
		File attachmentDir = new File(
			getTestBasePath());
		Assert.assertNull(
			"attachments directory not empty", 
			attachmentDir.listFiles());
	}
	
	public void testPutGetFieldDataPackets() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		security.createKeyPair();
		Bulletin b = new Bulletin(security);
		b.set(BulletinField.TAGAUTHOR, "paul");
		b.set(BulletinField.TAGKEYWORDS, "testing");
		b.set(BulletinField.TAGENTRYDATE, "2003-04-30");
		b.setAllPrivate(false);
		b.setSealed();
		b.getFieldDataPacket().setEncrypted(false);

		File tempFile = createTempFileFromName("$$$MartusAmpBulletinExtractorTest");
		BulletinForTesting.saveToFile(dataManager.getDatabase(), b, tempFile, security);
		FieldDataPacket publicData = b.getFieldDataPacket();

		ZipFile bulletinZipFile = new ZipFile(tempFile);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();

		ZipEntryInputStreamWithSeek zipEntryPointForFieldDataPacket = BulletinExtractor.getInputStreamForZipEntry(bulletinZipFile, bhp.getAccountId(), bhp.getFieldDataPacketId());
		dataManager.putDataPacket(publicData.getUniversalId(),zipEntryPointForFieldDataPacket);
		zipEntryPointForFieldDataPacket.close();
		bulletinZipFile.close();

		FieldDataPacket publicDataPacketRetrieved = dataManager.getFieldDataPacket(publicData.getUniversalId());
		assertNotNull(publicDataPacketRetrieved);

		assertEquals("Uids not the same?",publicData.getUniversalId(), publicDataPacketRetrieved.getUniversalId());
		
		MiniLocalization localization = new MiniLocalization();
		BulletinHtmlGenerator htmlGenerator = new BulletinHtmlGenerator(localization);
		assertEquals("HTML representation not the same?", htmlGenerator.getSectionHtmlString(publicData), htmlGenerator.getSectionHtmlString(publicDataPacketRetrieved));
	}
	
	public void testAccountWithFileSeparators() 
		throws IOException, AttachmentStorageException
	{
		UniversalId id = UniversalIdForTesting.createFromAccountAndPrefix(
			"AnAccount/With/Slashes", "Test");
		String testString = "AccountWithFileSeparators";
		InputStream sin = new StringInputStreamWithSeek(testString);
		try {
			dataManager.putAttachment(id, sin);
		} finally {
			sin.close();
		}
		
		InputStream in = null;
		try {
			in = dataManager.getAttachment(id);
			Assert.assertEquals(testString, inputStreamToString(in));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void testGetContactInfoFile() throws Exception
	{
		String accountId = "test";
		File info = dataManager.getContactInfoFile(accountId);
		assertTrue("file should end in contactInfo.dat", info.getAbsolutePath().endsWith("contactInfo.dat"));
	}
	
	public void testWriteContactInfoToFile() throws Exception
	{
		String id = "abc";
		File infoFile = dataManager.getContactInfoFile(id);
		infoFile.delete();
		assertFalse("should not contain any contact info yet", infoFile.exists());

		Vector info = new Vector();
		info.add(id);
		info.add(new Integer(1));
		info.add("data");
		info.add("signature");
		dataManager.writeContactInfoToFile(id, info);
		infoFile = dataManager.getContactInfoFile(id);		
		assertTrue("should have a valid file", infoFile.exists());

	}

	
	public void testGetContactInfo() throws Exception
	{
		MockMartusSecurity client = new MockMartusSecurity();
		client.createKeyPair();
		MartusAmplifier.setStaticSecurity(client);

		String id = "test";
		String data1 = "data 1";
		String data2 = "data 2";
		String invalidSignature = "invalid sig";
		Vector contactInfo = dataManager.getContactInfo(id);
		assertNull("Data not saved yet should return null", contactInfo);

		Vector original = new Vector();
		original.add(id);
		original.add(new Integer(2));
		original.add(data1);
		original.add(data2);
		original.add(invalidSignature);		
		dataManager.writeContactInfoToFile(id, original);
		contactInfo = dataManager.getContactInfo(id);
		assertNull("contactInfo should be null, invalid signature", contactInfo);

		original.clear();
		String accountId = client.getPublicKeyString();		
		original.add(accountId);
		original.add(new Integer(3));
		original.add(data1);
		original.add("");
		original.add(data2);
		String signature = client.createSignatureOfVectorOfStrings(original);
		original.add(signature);		

		dataManager.writeContactInfoToFile(accountId, original);
		contactInfo = dataManager.getContactInfo(accountId);
		assertNotNull("contactInfo should not be null, valid signature", contactInfo);

		assertEquals(2, contactInfo.size());
		assertEquals(data1, contactInfo.get(0));
		assertEquals(data2, contactInfo.get(1));

		original.clear();
		original.add(accountId);
		original.add(new Integer(10));
		original.add("");
		original.add("");
		original.add("");
		original.add(data1);
		original.add("");
		original.add("");
		original.add("");
		original.add(data2);
		original.add("");
		original.add("");
		signature = client.createSignatureOfVectorOfStrings(original);
		original.add(signature);		

		dataManager.writeContactInfoToFile(accountId, original);
		contactInfo = dataManager.getContactInfo(accountId);
		assertEquals("Empty data elements should be stripped", 2, contactInfo.size());
		assertEquals(data1, contactInfo.get(0));
		assertEquals(data2, contactInfo.get(1));
	}
	
	protected DataManager getAttachmentManager()
	{
		return dataManager;
	}
	private FileSystemDataManager dataManager;

}