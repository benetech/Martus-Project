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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;



public class TestMartusUtilities extends TestCaseEnhanced
{
	public TestMartusUtilities(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
    {
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
    }


	public void testToFileName()
	{
		String alphaNumeric		= "123abcABC";
		String alphaSpaces		= "abc def";
		String alphaPunctIn		= "a.b";
		String alphaPunctOut	= "a b";
		String trailingPunctIn	= "abc!";
		String trailingPunctOut	= "abc";
		String leadingPunctIn	= "?abc";
		String leadingPunctOut	= "abc";
		String punctuation1		= "`-=[]\\;',./";
		String punctuation2		= "~!@#%^&*()_+";
		String punctuation3		= "{}|:\"<>?";
		String tooLong			= "abcdefghijklmnopqrstuvwxyz";
		String tooShort			= "ab";
		String minimumLength	= "abc";
		assertEquals(alphaNumeric, MartusUtilities.toFileName(alphaNumeric));
		assertEquals(alphaSpaces, MartusUtilities.toFileName(alphaSpaces));
		assertEquals(alphaPunctOut, MartusUtilities.toFileName(alphaPunctIn));
		assertEquals(trailingPunctOut, MartusUtilities.toFileName(trailingPunctIn));
		assertEquals(leadingPunctOut, MartusUtilities.toFileName(leadingPunctIn));
		assertEquals("Martus-", MartusUtilities.toFileName(punctuation1));
		assertEquals("Martus-", MartusUtilities.toFileName(punctuation2));
		assertEquals("Martus-", MartusUtilities.toFileName(punctuation3));
		assertEquals(tooLong.substring(0, 20), MartusUtilities.toFileName(tooLong));
		assertEquals("Martus-" + tooShort, MartusUtilities.toFileName(tooShort));
		assertEquals(minimumLength, MartusUtilities.toFileName(minimumLength));
	}
	
	// TODO: create tests for all the MartusUtilities methods
	public void testExportServerPublicKey() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportServerPublicKey(security, keyFile);
		
		UnicodeReader reader = new UnicodeReader(keyFile);
		String sigFileIdentifier = reader.readLine();
		String sigFileType = reader.readLine();
		String key = reader.readLine();
		String sig = reader.readLine();
		reader.close();
		assertStartsWith("Martus Public Key", sigFileIdentifier);
		assertEquals("Server", sigFileType);
		assertEquals("wrong public key?", security.getPublicKeyString(), key);
		MartusUtilities.validatePublicInfo(key, sig, security);
		
		File badFile = new File(BAD_FILENAME);
		try
		{
			MartusUtilities.exportServerPublicKey(security, badFile);
			fail("Should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
		
		keyFile.delete();
		badFile.delete();
	}
	
	public void testImportServerPublicKeyFromFile() throws Exception
	{
		String key = security.getPublicKeyString();
		byte[] publicKeyBytes = StreamableBase64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = StreamableBase64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln("Martus Public Key:1.0");
		writer.writeln("Server");
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		Vector result = MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
		assertEquals(2, result.size());
		String gotKey = (String)result.get(0);
		String gotSig = (String)result.get(1);
		assertEquals("wrong public key?", key, gotKey);
		assertEquals("wrong sig?", sig, gotSig);
		
		keyFile.delete();
	}

	public void testImportServerPublicKeyFromFileThatIsClient() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportClientPublicKey(security, keyFile);

		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
		}
		catch (InvalidPublicKeyFileException ignoreExpectedException)
		{
		}

		keyFile.delete();
	}

	public void testImportServerPublicKeyFromFileBad() throws Exception
	{
		File keyFile = new File(BAD_FILENAME);
		keyFile.deleteOnExit();
		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
			fail("should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
		keyFile.delete();
	}

	public void testImportServerPublicKeyFromFileBadSig() throws Exception
	{
		MockMartusSecurity other = MockMartusSecurity.createOtherServer();
		String key = other.getPublicKeyString();
		byte[] publicKeyBytes = StreamableBase64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = StreamableBase64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln("Martus Public Key:1.0");
		writer.writeln("Server");
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		try
		{
			MartusUtilities.importServerPublicKeyFromFile(keyFile, security);
			fail("should have thrown");
		}
		catch (PublicInformationInvalidException ignoreExpectedException)
		{
		}
		keyFile.delete();
		}

	public void testExportClientPublicKey() throws Exception
	{
		File keyFile = createTempFile();
		MartusUtilities.exportClientPublicKey(security, keyFile);
		
		UnicodeReader reader = new UnicodeReader(keyFile);
		String key = reader.readLine();
		String sig = reader.readLine();
		reader.close();
		assertEquals("wrong public key?", security.getPublicKeyString(), key);
		MartusUtilities.validatePublicInfo(key, sig, security);
		keyFile.delete();
		
		File badFile = new File(BAD_FILENAME);
		badFile.deleteOnExit();
		try
		{
			MartusUtilities.exportClientPublicKey(security, badFile);
			fail("Should have thrown");
		}
		catch (IOException ignoreExpectedException)
		{
		}
		badFile.delete();
	}
	
	public void testImportClientPublicKeyFromFile() throws Exception
	{
		String key = security.getPublicKeyString();
		byte[] publicKeyBytes = StreamableBase64.decode(key);
		InputStream in = new ByteArrayInputStream(publicKeyBytes);
		byte[] sigBytes = security.createSignatureOfStream(in);
		String sig = StreamableBase64.encode(sigBytes);

		File keyFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(keyFile);
		writer.writeln(key);
		writer.writeln(sig);
		writer.close();

		Vector result = MartusUtilities.importClientPublicKeyFromFile(keyFile);
		keyFile.delete();
		
		assertEquals(2, result.size());
		String gotKey = (String)result.get(0);
		String gotSig = (String)result.get(1);
		assertEquals("wrong public key?", key, gotKey);
		assertEquals("wrong sig?", sig, gotSig);
	
	}

	public void testValidateIntegrityOfZipFilePackets() throws Exception
	{
		Bulletin b = new Bulletin(security);
		testValidateIntegrityOfZipFilePackets(b);
	}

	public void testValidateIntegrityOfZipFilePacketsNewNoteType() throws Exception
	{
		FieldSpecCollection publicSpec = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection privateSpec = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin b = new Bulletin(security, Bulletin.BulletinType.NOTE, publicSpec, privateSpec);
		assertEquals("Not a Note?", b.getBulletinType(), Bulletin.BulletinType.NOTE);

		testValidateIntegrityOfZipFilePackets(b);
	}

	private void testValidateIntegrityOfZipFilePackets(Bulletin b) throws Exception, IOException,
			EncryptionException, FileNotFoundException, ZipException
	{
		BulletinStore store = new MockBulletinStore(this);
		ReadableDatabase db = store.getDatabase();

		File sampleAttachment = createTempFileFromName("$$$Martus_This is some data");
		AttachmentProxy ap = new AttachmentProxy(sampleAttachment);
		b.addPublicAttachment(ap);
		store.saveEncryptedBulletinForTesting(b);
		String accountId = b.getAccount();
		DatabaseKey key = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());

		File originalZipFile = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, key, originalZipFile, security);
		validateZipFile(accountId, originalZipFile);

		File copiedZipFile = createCopyOfZipFile(originalZipFile, null, null);
		validateZipFile(accountId, copiedZipFile);

		File zipWithoutHeaderPacket = createCopyOfZipFile(originalZipFile, "B-", null);
		try
		{
			validateZipFile(accountId, zipWithoutHeaderPacket);
			fail("Should have thrown for missing header");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutDataPackets = createCopyOfZipFile(originalZipFile, "F-", null);
		try
		{
			validateZipFile(accountId, zipWithoutDataPackets);
			fail("Should have thrown for missing data packets");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithoutAttachmentPackets = createCopyOfZipFile(originalZipFile, "A-", null);
		try
		{
			validateZipFile(accountId, zipWithoutAttachmentPackets);
			fail("Should have thrown for missing attachment");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithExtraEntry = createCopyOfZipFile(originalZipFile, null, "unexpected");
		try
		{
			validateZipFile(accountId, zipWithExtraEntry);
			fail("Should have thrown for extra entry");
		}
		catch (IOException ignoreExpectedException)
		{
		}

		File zipWithRelativePathInformation = createCopyOfZipFile(originalZipFile, null, "../../../acctmap.txt");
		try
		{
			validateZipFile(accountId, zipWithRelativePathInformation);
			fail("Should have thrown for relative path in name");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}

		File zipWithAbsolutePathInformation = createCopyOfZipFile(originalZipFile, null, "c:/MartusServer/packets/acctmap.txt");
		try
		{
			validateZipFile(accountId, zipWithAbsolutePathInformation);
			fail("Should have thrown for absolute path in name");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}
		
		sampleAttachment.delete();
		originalZipFile.delete();
		copiedZipFile.delete();
		zipWithoutHeaderPacket.delete();
		zipWithoutDataPackets.delete();
		zipWithoutAttachmentPackets.delete();
		zipWithExtraEntry.delete();
		zipWithRelativePathInformation.delete();
		zipWithAbsolutePathInformation.delete();
	}

	private void validateZipFile(String accountId, File copiedZipFile)
		throws Exception
	{
		ZipFile copiedZip = new ZipFile(copiedZipFile);
		try
		{
			BulletinZipUtilities.validateIntegrityOfZipFilePackets(accountId, copiedZip, security);
		}
		finally
		{
			copiedZip.close();
		}
	}

	private File createCopyOfZipFile(File tempZipFile, String excludeStartsWith, String entryToAdd)
		throws IOException, FileNotFoundException, ZipException
	{
		File copiedZipFile = createTempFile();
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(copiedZipFile));

		ZipFile zip = new ZipFile(tempZipFile);
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			if(excludeStartsWith != null && entry.getName().startsWith(excludeStartsWith))
				continue;
			InputStream in = new BufferedInputStream(zip.getInputStream(entry));
			zipOut.putNextEntry(entry);
			int dataLength = (int)entry.getSize();
			byte[] data = new byte[dataLength];
			in.read(data);
			zipOut.write(data);
		}
		if(entryToAdd != null)
		{
			ZipEntry newEntry = new ZipEntry(entryToAdd);
			zipOut.putNextEntry(newEntry);
		}
		zip.close();
		zipOut.close();
		return copiedZipFile;
	}

	public void testGetBulletinSize() throws Exception
	{
		byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
		BulletinStore store = new MockBulletinStore(this);
		ReadableDatabase db = store.getDatabase();

		Bulletin b1 = new Bulletin(security);
		store.saveEncryptedBulletinForTesting(b1);
		BulletinHeaderPacket bhp = b1.getBulletinHeaderPacket();
		int emptySize = MartusUtilities.getBulletinSize(db, bhp);
		// NOTE: The following limits are arbitrary, 
		// and may need to be adjusted periodically
		assertTrue("empty size too small?", emptySize > 4000);
		assertTrue("empty size too big?", emptySize < 8000);
		b1.set(Bulletin.TAGTITLE, "Title");
		b1.set(Bulletin.TAGPUBLICINFO, "Details1");
		b1.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		File attachment = createTempFile();
		FileOutputStream out = new FileOutputStream(attachment);
		out.write(b1AttachmentBytes);
		out.close();
		b1.addPublicAttachment(new AttachmentProxy(attachment));
		b1.addPrivateAttachment(new AttachmentProxy(attachment));
		store.saveEncryptedBulletinForTesting(b1);
		b1 = BulletinLoader.loadFromDatabase(db, DatabaseKey.createImmutableKey(b1.getUniversalId()), security);

		int size = MartusUtilities.getBulletinSize(db, bhp);
		b1.set(Bulletin.TAGTITLE, "This is an very long title and should change the size of the result if things are working correctly");
		store.saveEncryptedBulletinForTesting(b1);
		int size2 = MartusUtilities.getBulletinSize(db, bhp);
		assertTrue("Size too small?", size > 4000);
		assertNotEquals("Sizes match?", size, size2);
	}

	public void testCreateSignatureFromFile()
		throws Exception
	{
		String string1 = "The string to write into the file to sign.";
		String string2 = "The other string to write to another file to sign.";

		File normalFile = createTempFileWithData(string1);
		File anotherFile = createTempFileWithData(string2);

		File normalFileSigBySecurity = MartusUtilities.createSignatureFileFromFile(normalFile, security);

		MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security, security.getPublicKeyString());

		try
		{
			MartusUtilities.verifyFileAndSignature(normalFile, normalFileSigBySecurity, security, "this would be a different public key");
			fail("signature file's public key is not the verifiers public key should have thrown.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}

		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security, security.getPublicKeyString());
			fail("testCreateSignatureFromFile 1: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}

		normalFileSigBySecurity.delete();
		normalFile.delete();
		anotherFile.delete();

		try
		{
			MartusUtilities.verifyFileAndSignature(anotherFile, normalFileSigBySecurity, security, security.getPublicKeyString());
			fail("testCreateSignatureFromFile 2: Should have thrown FileVerificationException.");
		}
		catch (FileVerificationException ignoreExpectedException)
		{
		}
	}

	public void testDoesPacketNeedLocalEncryption() throws Exception
	{
		BulletinHeaderPacket bhpWithoutFlag = new BulletinHeaderPacket(security);
		BulletinHeaderPacket bhpWithFlagPrivate = new BulletinHeaderPacket(security);
		bhpWithFlagPrivate.setAllPrivate(true);
		BulletinHeaderPacket bhpWithFlagPublic = new BulletinHeaderPacket(security);
		bhpWithFlagPublic.setAllPrivate(false);
		byte[] binaryEncryptedData = {0, 1, 2, 3};
		byte[] tagEncryptedData = new String("blah blah blah\n<Encrypted> blah blah").getBytes();
		byte[] plainTextData = new String("There is nothing here\nto indicate that it is Encrypted!").getBytes();
		String fdpLocalId = FieldDataPacket.createUniversalId(security).getLocalId();

		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, binary encrypted",
						false, bhpWithFlagPublic, fdpLocalId, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, tag encrypted",
						false, bhpWithFlagPublic, fdpLocalId, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, plain text",
						true, bhpWithFlagPublic, fdpLocalId, plainTextData);

		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, binary encrypted",
						false, bhpWithoutFlag, fdpLocalId, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, tag encrypted",
						false, bhpWithoutFlag, fdpLocalId, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, plain text",
						true, bhpWithoutFlag, fdpLocalId, plainTextData);

		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, binary encrypted",
						false, bhpWithFlagPrivate, fdpLocalId, binaryEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, tag encrypted",
						false, bhpWithFlagPrivate, fdpLocalId, tagEncryptedData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPrivate, plain text",
						false, bhpWithFlagPrivate, fdpLocalId, plainTextData);

		String attachmentLocalId = AttachmentPacket.createUniversalId(security).getLocalId();
		verifyDoesPacketNeedLocalEncryption("headerDoesntKnow, plain text, attachment",
				false, bhpWithoutFlag, attachmentLocalId, plainTextData);
		verifyDoesPacketNeedLocalEncryption("headerSaysPublic, plain text, attachment",
				false, bhpWithFlagPublic, attachmentLocalId, plainTextData);
	}
	
	public void testHeaderInWrongPlace() throws Exception
	{
		MockMartusSecurity client1 = MockMartusSecurity.createClient();
		MockMartusSecurity client2 = MockMartusSecurity.createOtherClient();
		UniversalId realUid = BulletinHeaderPacket.createUniversalId(client1);
		UniversalId wrongUid = BulletinHeaderPacket.createUniversalId(client2);
		BulletinHeaderPacket bhp1 = new BulletinHeaderPacket(realUid);

		Database db = new MockServerDatabase();
		DatabaseKey wrongKey = DatabaseKey.createImmutableKey(wrongUid);
		bhp1.writeXmlToDatabase(db, wrongKey, false, client1);

		File tempFile = createTempFile();
		try
		{
			BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, wrongKey, tempFile, client1);
			fail("should have thrown");
		}
		catch(InvalidPacketException ignoreExpectedException)
		{
		}
		tempFile.delete();
	}
	
	public void verifyDoesPacketNeedLocalEncryption(String label, boolean expected,
							BulletinHeaderPacket bhp, String packetLocalId, byte[] bytes1)
		throws IOException
	{
		InputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes1);
		int firstByte = in.read();
		in.seek(0);
		assertEquals(label, expected,
				MartusUtilities.doesPacketNeedLocalEncryption(packetLocalId, bhp, in));
		assertEquals(label + " didn't reset?", firstByte, in.read());
	}

	static MartusCrypto security;
}
